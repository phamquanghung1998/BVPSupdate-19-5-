package vn.ithanh.udocter.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by iThanh on 11/19/2017.
 */

public class MySharedPreferences {
    public final String PREFS_NAME = "ithanh.uHealth";

    SharedPreferences my_share;
    SharedPreferences.Editor editor;

    public MySharedPreferences(Context mContext) {
        my_share = mContext.getSharedPreferences(PREFS_NAME, 0);
        editor = my_share.edit();
    }
    public void setFBID(String fbid){
        editor.putString("facebook_id", fbid);
        editor.commit();
    }
    public String getDEFAULT_ADDRESS() {
        return my_share.getString(Config.SETTING.ADDRESS_DOCTER_DEFAULT, "");
    }

    public void setDEFAULT_ADDRESS(String address){
        editor.putString(Config.SETTING.ADDRESS_DOCTER_DEFAULT, address);
        editor.commit();
    }

    public String getDEFAULT_AVATAR() {
        return my_share.getString(Config.SETTING.AVATAR_DOCTER_DEFAULT, "");
    }

    public void setDEFAULT_AVATAR(String avatar){
        editor.putString(Config.SETTING.AVATAR_DOCTER_DEFAULT, avatar);
        editor.commit();
    }
    public String getFBID() {
        return my_share.getString("facebook_id", "");
    }


    public int getUSER_ID() {
        return my_share.getInt(Config.SETTING.KEY_USER_ID, 0);
    }
    public int getUSER_TYPE() {
        return my_share.getInt(Config.USER_UD.KEY_user_type, 0);
    }
    public void setUSER_ID(int user_id){
        editor.putInt(Config.SETTING.KEY_USER_ID, user_id);
        editor.commit();
    }
    public void setFULL_NAME(String name){
        editor.putString(Config.USER_UD.KEY_full_name, name);
        editor.commit();
    }
    public void setEMAIL(String name){
        editor.putString(Config.USER_UD.KEY_username, name);
        editor.commit();
    }
    public void setPHONE(String name){
        editor.putString(Config.USER_UD.KEY_phone, name);
        editor.commit();
    }
    public void setUSER_TYPE(int type){
        editor.putInt(Config.USER_UD.KEY_user_type, type);
        editor.commit();
    }
    //
    public void setD_ID(int d_id){
        editor.putInt(Config.USER_UD.KEY_d_id, d_id);
        editor.commit();
    }
    public String getPHONE() {
        return my_share.getString(Config.USER_UD.KEY_phone, "");
    }
    public String gettEMAIL() {
        return my_share.getString(Config.USER_UD.KEY_username, "");
    }
    public String getFULL_NAME() {
        return my_share.getString(Config.USER_UD.KEY_full_name, "");
    }
    public int getD_ID() {
        return my_share.getInt(Config.USER_UD.KEY_d_id, 0);
    }

    public void setP_ID(int p_id){
        editor.putInt(Config.USER_UD.KEY_p_id, p_id);
        editor.commit();
    }
    public int getP_ID() {
        return my_share.getInt(Config.USER_UD.KEY_p_id, 0);
    }

    public void setLongitude(float p_id){
        editor.putFloat(Config.USER_UD.KEY_longitude, p_id);
        editor.commit();
    }
    public float getLongitude() {
        return my_share.getFloat(Config.USER_UD.KEY_longitude, 0);
    }
    public void setLatitude(float p_id){
        editor.putFloat(Config.USER_UD.KEY_latitude, p_id);
        editor.commit();
    }
    public float getLatitude() {
        return my_share.getFloat(Config.USER_UD.KEY_latitude, 0);
    }

}
