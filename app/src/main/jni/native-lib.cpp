#include <jni.h>
#include <string>
#include "sqlite3.h"
extern "C"{
#define SQLITE_SOFT_HEAP_LIMIT (4 * 1024 * 1024)
sqlite3 *handle;
char stder[1024];

JNIEXPORT jboolean
JNICALL
Java_com_giaynhap_kmasqlite_KMASQlite_exec(
        JNIEnv *env,
        jobject , jstring jsql) {
    int err;
    int stepErr;
    sqlite3_stmt * statement = NULL;
    const char *sql = env->GetStringUTFChars(jsql,0);
    size_t sqlLen =strlen(sql);

    if (sql == NULL || sqlLen == 0) {

        sprintf(stder,"You must supply an SQL string");
        return false;
    }
    err = sqlite3_prepare16_v2(handle, sql, sqlLen * 2, &statement, NULL);
    if (err != SQLITE_OK) {

        sprintf(stder,"Failure %d (%s) on %p when preparing '%s'.\n", err, sqlite3_errmsg(handle), handle, sql);
        return false;
    }
    stepErr = sqlite3_step(statement);
    err = sqlite3_finalize(statement);
    if (stepErr != SQLITE_DONE) {
        if (stepErr == SQLITE_ROW) {
            sprintf(stder,"Queries cannot be performed using execSQL(), use query() instead.");
            return false;
        }
        else {
            sprintf(stder,"Failure %d (%s) on %p when executing '%s'\n", err, sqlite3_errmsg(handle), handle, sql);
            return false;
        }
    }
    else  {
        return true;
    }

}
JNIEXPORT jboolean
JNICALL
Java_com_giaynhap_kmasqlite_KMASQlite_execRaw(
        JNIEnv *env,
        jobject , jstring jsql) {
    char *zErrMsg = 0;
    const char *sql = env->GetStringUTFChars(jsql,0);

    int status = sqlite3_exec(handle, sql, NULL, 0, &zErrMsg);

    if (status != SQLITE_OK) {
        sprintf(stder, "%s", zErrMsg);
        sqlite3_free(zErrMsg);
        return false;
    }
    return true;

}

JNIEXPORT jboolean
JNICALL
Java_com_giaynhap_kmasqlite_KMASQlite_opendb(
        JNIEnv *env,
        jobject , jstring pathDB, jint flag) {
    bool success = true;
    int rc;
    const char *path = env->GetStringUTFChars( pathDB, 0);
    int sqliteFlags = flag;
    rc = sqlite3_open_v2(path, &handle, sqliteFlags, 0);
    if( rc != SQLITE_OK ){
        sprintf(stder, "Can't Open Database");
        success = false;
        goto done;
    }
    sqlite3_soft_heap_limit(SQLITE_SOFT_HEAP_LIMIT);
    rc = sqlite3_busy_timeout(handle, 1000);
    if (rc != SQLITE_OK) {
        sprintf(stder, "Could not set busy timeout");
        success = false;
        goto done;
    }
    sqlite3_enable_load_extension(handle, 1);
    done:
    return success;
}

void sqlTrace(void *databaseName, const char *sql) {

}
void  sqlProfile(void *databaseName, const char *sql, sqlite3_uint64 tm) {
    double d = tm / 1000000.0;

}

JNIEXPORT jboolean
JNICALL
Java_com_giaynhap_kmasqlite_KMASQlite_closedb(
        JNIEnv *env,
        jobject , jstring pathDB, jint flag) {

    if (handle == NULL)  return false;

    void *traceFuncArg = sqlite3_trace(handle, &sqlTrace, NULL);
    if (traceFuncArg != NULL) {
        free(traceFuncArg);
    }

    traceFuncArg = sqlite3_profile(handle, &sqlProfile, NULL);
    if (traceFuncArg != NULL) {
        free(traceFuncArg);
    }
    int result = sqlite3_close(handle);
    if (result == SQLITE_OK) {
        return true;
        handle = NULL;
    }
    else {

        sprintf(stder,"sqlite3_close(%p) failed: %d\n", handle, result);
        return false;
    }
}


JNIEXPORT sqlite3*
JNICALL
Java_com_giaynhap_kmasqlite_KMASQlite_sqlite(
        JNIEnv *env,
        jobject ) {
    return handle;
}

JNIEXPORT jstring
JNICALL
Java_com_giaynhap_kmasqlite_KMASQlite_getLastError(
        JNIEnv *env,
        jobject ) {
        return env->NewStringUTF(stder);
}

}