
#include "sqlite3.h"
 

/*
 * SQLite3 codec implementation.
 */
typedef struct codec { 
    struct codec *reader, *writer;
    unsigned char key[32], salt[16];
    void *pagebuf;
    int pagesize;
    const void *zKey;
    int nKey;
} Codec;

Codec *codec_new(const char *zKey, int nKey)
{
    Codec *codec;
    if ((codec = sqlite3_malloc(sizeof(Codec)))) {
        codec->reader = codec->writer = codec;
        memset(codec->key, 0, sizeof(codec->key));
        memset(codec->salt, 0, sizeof(codec->salt));
        codec->pagebuf = NULL;
        codec->pagesize = 0;
        codec->zKey = zKey;
        codec->nKey = nKey;
    }
    return codec;
}

void codec_free(void *pcodec)
{
    if (pcodec) {
        int i;
        volatile char *p;
        Codec *codec = pcodec;
        if (codec->zKey) {
            p = (void *)codec->zKey;
            for (i = 0; i < codec->nKey; p[i++] = '\0');
            /* zKey memory is allocated by the user */
        }
        if (codec->pagebuf) {
            p = codec->pagebuf;
            for (i = 0; i < codec->pagesize; p[i++] = '\0');
            sqlite3_free(codec->pagebuf);
        }
        p = pcodec;
        for (i = 0; i < sizeof(Codec); p[i++] = '\0');
        sqlite3_free(codec);
    }
}

Codec *codec_dup(Codec *src)
{
    Codec *codec;
    if ((codec = codec_new(src->zKey, src->nKey))) {
        codec->reader = (src->reader == src) ? codec : src->reader;
        codec->writer = (src->writer == src) ? codec : src->writer;
        memcpy(codec->salt, src->salt, 16);
        memcpy(codec->key, src->key, 32);
    }
    return codec;
}

void codec_kdf(Codec *codec)
{
    pbkdf2_hmac_sha256(codec->zKey, codec->nKey, codec->salt, 16, 12345,
                       codec->key, 32);
    while (codec->nKey) ((volatile char *)codec->zKey)[--codec->nKey] = '\0';
    codec->zKey = NULL;
    codec->nKey = 0;
}

/*
 * The encrypted database page format.
 *
 * +----------------------------------------+----------------+----------------+
 * | Encrypted data                         | 16-byte nonce  | 16-byte tag    |
 * +----------------------------------------+----------------+----------------+
 *
 * As the only exception, the first page (page_no=1) starts with a plaintext
 * salt contained in the first 16 bytes of the database file. The "master" key
 * is derived from a user-given password with the salt and 12345 iterations of
 * PBKDF-HMAC-SHA256. Future plans include switching to BLAKE2 and Argon2.
 *
 * - The data is encrypted by XORing with the ChaCha20 keystream produced from
 *   the 16-byte nonce and a 32-byte encryption key derived from the master key.
 *   - OK, I lied a little: ChaCha20 uses only the first 12 bytes as the nonce.
 *     However, ChaCha20 also requires an initial value for a counter of 4 bytes
 *     that encodes a block position in the output stream. We derive the counter
 *     value from the last 4 bytes, effectively extending the nonce to 16 bytes.
 *   - Specifically, counter = LOAD32_LE(nonce[12..15])^page_no is first applied
 *     to generate a single 64-byte block from nonce[0..11] and the master key.
 *     The block consists of two 32-byte one-time keys, the former is a Poly1305
 *     key for the authentication tag, and the latter is a ChaCha20 key for the
 *     data encryption. The encryption with the one-time key uses nonce[0..11]
 *     and the initial counter value of counter+1.
 *   - The XOR with page_no prevents malicious reordering of the pages.
 *
 * - The nonce consists of 128 randomly generated bits, which should be enough
 *   to guarantee uniqueness with a reasonable pseudorandom number generator.
 *   - Given a perfect RNG, the adversary needs to observe at least 2^61 nonces
 *     to break Poly1305 with the birthday attack at a success rate of 1%.
 *   - If a nonce is reused, we lose confidentiality of the associated messages.
 *     Moreover, the compromised nonce can also be used to forge valid tags for
 *     new messages having the same nonce (basically, the one-time Poly1305 key
 *     can be recovered from distinct messages with identical nonces).
 *
 * - The tag is a Poly1305 MAC calculated over the encrypted data and the nonce
 *   with the one-time key generated from the master key and the nonce.
 */

#define PAGE_NONCE_LEN 16
#define PAGE_TAG_LEN 16
#define PAGE_RESERVED_LEN (PAGE_NONCE_LEN + PAGE_TAG_LEN)

void *codec_handle(void *codec, void *pdata, Pgno page, int mode)
{
    uint32_t counter;
    unsigned char otk[64], tag[16];
    Codec *reader, *writer;
    unsigned char *data = pdata;
    if (!codec) return data;
    reader = ((Codec *)codec)->reader;
    writer = ((Codec *)codec)->writer;

    switch (mode) {
    case 0: /* Journal decryption */
    case 2: /* Reload a page */
    case 3: /* Load a page */
        if (reader) {
            int n = reader->pagesize - PAGE_RESERVED_LEN;
            if (page == 1 && reader->zKey) {
                memcpy(reader->salt, data, 16);
                codec_kdf(reader);
            }

            /* Generate one-time keys */
            memset(otk, 0, 64);
            counter = LOAD32_LE(data + n + PAGE_NONCE_LEN-4) ^ page;
            chacha20_xor(otk, 64, reader->key, data + n, counter);

            /* Verify the MAC */
            poly1305(data, n + PAGE_NONCE_LEN, otk, tag);
            if (!poly1305_tagcmp(data + n + PAGE_NONCE_LEN, tag)) {
                /* Decrypt */
                chacha20_xor(data, n, otk+32, data + n, counter+1);
                if (page == 1) memcpy(data, "SQLite format 3", 16);
            } else {
                data = NULL;
            }
        }
        break;

    case 7: /* Encrypt a journal page (with the reader key) */
        writer = reader;
        /* fall-through */
    case 6: /* Encrypt a main database page */
        if (writer) {
            int n = writer->pagesize - PAGE_RESERVED_LEN;
            data = memcpy(writer->pagebuf, data, writer->pagesize);

            /* Generate one-time keys */
            memset(otk, 0, 64);
            chacha20_rng(data + n, 16);
            counter = LOAD32_LE(data + n + PAGE_NONCE_LEN-4) ^ page;
            chacha20_xor(otk, 64, writer->key, data + n, counter);

            /* Encrypt and authenticate */
            chacha20_xor(data, n, otk+32, data + n, counter+1);
            if (page == 1) memcpy(data, writer->salt, 16);
            poly1305(data, n + PAGE_NONCE_LEN, otk, data + n + PAGE_NONCE_LEN);
        }
        break;
    }

    return data;
}

/* The caller must hold the database mutex. */
static int codec_set_to(Codec *codec, Btree *pBt)
{
    int pagesize;
    Pager *pager;

    /* Allocate page buffer */
    pagesize = sqlite3BtreeGetPageSize(pBt);
    if (codec && (!codec->pagebuf || codec->pagesize != pagesize)) {
        void *new = sqlite3_malloc(pagesize);
        if (!new) {
            codec_free(codec);
            return SQLITE_NOMEM;
        }
        if (codec->pagebuf) {
            int i = 0;
            while (i < codec->pagesize)
                ((volatile char *)codec->pagebuf)[i++] = '\0';
            sqlite3_free(codec->pagebuf);
        }
        codec->pagebuf = new;
        codec->pagesize = pagesize;
    }

    /* Set pager codec */
    pager = sqlite3BtreePager(pBt);
    sqlite3PagerSetCodec(pager, codec_handle, NULL, codec_free, codec);

    /* Force secure delete */
    sqlite3BtreeSecureDelete(pBt, 1);

    /* Adjust the page size and the reserved area */
    if (pager->nReserve != PAGE_RESERVED_LEN) {
        pBt->pBt->btsFlags &= ~BTS_PAGESIZE_FIXED;
        sqlite3BtreeSetPageSize(pBt, pagesize, PAGE_RESERVED_LEN, 0);
    }

    return SQLITE_OK;
}

void sqlite3CodecGetKey(sqlite3 *db, int nDb, void **zKey, int *nKey)
{
    /*
     * sqlite3.c calls this function to decide if a database attached without a
     * password should use the encryption scheme of the main database. Returns
     * *nKey == 1 to indicate that the main database encryption is available.
     */
    *zKey = NULL;
    *nKey = !!sqlite3PagerGetCodec(sqlite3BtreePager(db->aDb[nDb].pBt));
}

int sqlite3CodecAttach(sqlite3 *db, int nDb, const void *zKey, int nKey)
{
    int rc;
    Codec *codec;

    rc = SQLITE_ERROR;
    sqlite3_mutex_enter(db->mutex);
    if (zKey) {
        /* Attach with the provided key */
        Btree *pBt = db->aDb[nDb].pBt;
        if (pBt && (codec = codec_new(zKey, nKey))
                && (rc = codec_set_to(codec, pBt)) == SQLITE_OK) {
            int count;
            Pager *pager;
            DbPage *page;
            sqlite3PagerSharedLock((pager = sqlite3BtreePager(pBt)));
            sqlite3PagerPagecount(pager, &count);
            if (count > 0) {
                /* Read page1 (to read the salt and trigger codec_kdf) */
                sqlite3PcacheTruncate(pager->pPCache, 0);
                if ((rc = sqlite3PagerGet(pager, 1, &page, 0)) == SQLITE_OK) {
                    sqlite3PagerUnref(page);
                } else {
                    codec_set_to(NULL, pBt);
                    rc = SQLITE_NOTADB;
                }
            } else {
                /* Generate a new salt for an empty database */
                chacha20_rng(codec->salt, 16);
                codec_kdf(codec);
            }
            pager_unlock(pager);
        }
    } else if (nDb != 0 && nKey > 0) {
        /* Use the main database's encryption */
        Codec *mcodec = sqlite3PagerGetCodec(sqlite3BtreePager(db->aDb[0].pBt));
        if (mcodec) {
            Btree *pBt = db->aDb[nDb].pBt;
            if (pBt && (codec = codec_dup(mcodec)))
                rc = codec_set_to(codec, pBt);
        } else {
            /* Main DB's codec unavailable */
            rc = SQLITE_CANTOPEN;
        }
    }
    sqlite3_mutex_leave(db->mutex);
    return rc;
}

static int db_index_of(sqlite3 *db, const char *zDbName)
{
    int i;
    if (zDbName) {
        for (i = 0; i < db->nDb; i++) {
            if (!strcmp(db->aDb[i].zDbSName, zDbName))
                return i;
        }
    }
    return 0;
}

int sqlite3_key_v2(sqlite3 *db, const char *zDbName, const void *zKey, int nKey)
{
    return sqlite3CodecAttach(db, db_index_of(db, zDbName), zKey, nKey);
}

int sqlite3_key(sqlite3 *db, const void *zKey, int nKey)
{
    return sqlite3_key_v2(db, "main", zKey, nKey);
}

int sqlite3_rekey_v2(sqlite3 *db, const char *zDbName,
                     const void *zKey, int nKey)
{
    Btree *pBt;
    int nDb, rc;

    if (!db || (!nKey && !zKey))
        return SQLITE_ERROR;

    rc = SQLITE_ERROR;
    sqlite3_mutex_enter(db->mutex);
    if ((pBt = db->aDb[(nDb = db_index_of(db, zDbName))].pBt)) {
        Pgno pgno;
        DbPage *page;
        Codec *reader, *codec;
        Pager *pager = pBt->pBt->pPager;

        if ((reader = sqlite3PagerGetCodec(pager)) && !nKey) {
            /* Decrypt */
            reader->writer = NULL;
            /* Truncating the reserved space is dangerous (may corrupt the DB)
            sqlite3BtreeSetPageSize(pBt, sqlite3BtreeGetPageSize(pBt), 0, 0); */
            goto vacuum;
        }

        if (!(codec = codec_new(zKey, nKey))) {
            rc = SQLITE_NOMEM;
            goto leave;
        }

        /* Allocate page buffer */
        codec->pagesize = sqlite3BtreeGetPageSize(pBt);
        codec->pagebuf = sqlite3_malloc(codec->pagesize);
        if (!codec->pagebuf) {
            codec_free(codec);
            rc = SQLITE_NOMEM;
            goto leave;
        }

        /* Generate a new salt */
        chacha20_rng(codec->salt, 16);
        codec_kdf(codec);

        if (!reader) {
            /* Encrypt */
            codec->reader = NULL;
            if ((rc = codec_set_to(codec, pBt)) == SQLITE_OK)
                goto vacuum;
            codec_free(codec);
            goto leave;
        }

        /* Change key */
        reader->writer = codec;
        rc = sqlite3BtreeBeginTrans(pBt, 1);
        for (pgno = 1; rc == SQLITE_OK && pgno <= pager->dbSize; pgno++) {
            /* The DB page occupied by the PENDING_BYTE is never used */
            if (pgno == PENDING_BYTE_PAGE(pager))
                continue;
            if ((rc = sqlite3PagerGet(pager, pgno, &page, 0)) == SQLITE_OK) {
                rc = sqlite3PagerWrite(page);
                sqlite3PagerUnref(page);
            }
        }
        if (rc == SQLITE_OK) {
            sqlite3BtreeCommit(pBt);
            rc = codec_set_to(codec, pBt);
        } else {
            reader->writer = reader;
            sqlite3BtreeRollback(pBt, SQLITE_ABORT_ROLLBACK, 0);
        }
    }

leave:
    sqlite3_mutex_leave(db->mutex);
    return rc;

vacuum:
    {
        char *err = NULL;
        rc = sqlite3RunVacuum(&err, db, nDb);
    }
    goto leave;
}

int sqlite3_rekey(sqlite3 *db, const void *zKey, int nKey)
{
    return sqlite3_rekey_v2(db, "main", zKey, nKey);
}

void sqlite3_activate_see(const char *info)
{
}
