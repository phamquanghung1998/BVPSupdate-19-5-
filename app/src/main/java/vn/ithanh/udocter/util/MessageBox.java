package vn.ithanh.udocter.util;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by iThanh on 11/19/2017.
 */

public class MessageBox {
    public static void showToast(Context ctx, String string) {
        Toast.makeText(ctx, string, Toast.LENGTH_LONG).show();
    }
}
