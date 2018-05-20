#include <jni.h>
#include <string>
#include "sqlite3.h"
extern "C" {
#define SQLITE_SOFT_HEAP_LIMIT (4 * 1024 * 1024)
sqlite3 *handle;
sqlite3_stmt*   mpStmt;
int mColumnCount;
char stder[1024];
JNIEXPORT void JNICALL Java_com_giaynhap_kmasqlite_Statement_init(JNIEnv *env,
                            jobject,jlong kmasqlite){
  handle = (sqlite3 *)kmasqlite;
    printf("Hello: %p",handle);

}

JNIEXPORT jboolean JNICALL Java_com_giaynhap_kmasqlite_Statement_initQuery(JNIEnv *env,
jobject,jstring jquery)
{
    const char * query = env->GetStringUTFChars(jquery,0);
    const int ret = sqlite3_prepare_v2(handle, query, strlen(query), &mpStmt, NULL);
    mColumnCount = sqlite3_column_count(mpStmt);
    if (ret != SQLITE_OK)
    {
        sprintf(stder,"Failure %d (%s) on %p when preparing '%s'.\n", ret, sqlite3_errmsg(handle), handle, query);
        return false;
    }

    return true;
}

JNIEXPORT jstring
JNICALL
Java_com_giaynhap_kmasqlite_Statement_getLastError(
        JNIEnv *env,
        jobject ) {
    return env->NewStringUTF(stder);
}


}