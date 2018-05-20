package com.giaynhap.kmasqlite;

import android.util.Log;

/**
 * Created by GiayNhap on 18/5/2018.
 */

public class Statement {
    static {
        System.loadLibrary("statement");
    }
    public Statement(KMASQlite sqlite,String query)
    {
        Log.i("[SQL] struct","anh"+sqlite.sqlite() );
        init(sqlite.sqlite());
        boolean err = initQuery(query);
            if (!err){
                Log.i("[SQL] Error",getLastError() );
            }
    }
    public native String getLastError();
    public native void init(long handle);
    public native boolean initQuery(String statement);


}
