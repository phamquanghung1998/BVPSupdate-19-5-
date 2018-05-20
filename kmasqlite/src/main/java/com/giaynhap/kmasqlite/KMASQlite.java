package com.giaynhap.kmasqlite;

import android.util.Log;

/**
 * Created by GiayNhap on 18/5/2018.
 */

public class KMASQlite {
    static {
        System.loadLibrary("native-lib");
    }

    public static int SQLITE_OPEN_READONLY       =  0x00000001 ; /* Ok for sqlite3_open_v2() */
    public static int SQLITE_OPEN_READWRITE      =  0x00000002  ;/* Ok for sqlite3_open_v2() */
    public static int SQLITE_OPEN_CREATE         =  0x00000004 ; /* Ok for sqlite3_open_v2() */
    public static int SQLITE_OPEN_DELETEONCLOSE  =  0x00000008  ;/* VFS only */
    public static int SQLITE_OPEN_EXCLUSIVE      =  0x00000010  ;/* VFS only */
    public static int SQLITE_OPEN_AUTOPROXY      =  0x00000020  ;/* VFS only */
    public static int SQLITE_OPEN_URI            =  0x00000040  ;/* Ok for sqlite3_open_v2() */
    public static int SQLITE_OPEN_MEMORY         =  0x00000080  ;/* Ok for sqlite3_open_v2() */
    public static int SQLITE_OPEN_MAIN_DB        =  0x00000100  ;/* VFS only */
    public static int SQLITE_OPEN_TEMP_DB        =  0x00000200  ;/* VFS only */
    public static int SQLITE_OPEN_MAIN_JOURNAL   =  0x00000800  ;/* VFS only */
    public static int SQLITE_OPEN_TEMP_JOURNAL   = 0x00001000  ;/* VFS only */
    public static int SQLITE_OPEN_SUBJOURNAL     =  0x00002000  ;/* VFS only */
    public static int SQLITE_OPEN_MASTER_JOURNAL = 0x00004000  ;/* VFS only */
    public static int SQLITE_OPEN_NOMUTEX        =  0x00008000  ;/* Ok for sqlite3_open_v2() */
    public static int SQLITE_OPEN_FULLMUTEX      =  0x00010000  ;/* Ok for sqlite3_open_v2() */
    public static int SQLITE_OPEN_SHAREDCACHE    =  0x00020000  ;/* Ok for sqlite3_open_v2() */
    public static int SQLITE_OPEN_PRIVATECACHE   =  0x00040000  ;/* Ok for sqlite3_open_v2() */
    public static int SQLITE_OPEN_WAL            =  0x00080000  ;/* VFS only */

    public KMASQlite (String sqliteDBName){
    Boolean db = this.opendb(sqliteDBName);
    if (!db)
    {
        String error = this.getLastError();
        Log.i("[SQL] Error",error);
    }


    }
    public  boolean opendb(String dbname){
        return this.opendb(dbname,this.SQLITE_OPEN_READWRITE| this.SQLITE_OPEN_CREATE);
    }
    public native boolean opendb(String dbname,int flag);
    public native boolean exec(String sql);
    public native boolean execRaw(String sql);
    public native boolean closedb();
    public native String getLastError();
    public native long sqlite();
}
