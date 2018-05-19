package vn.ithanh.udocter.service;


import com.bumptech.glide.util.Util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import vn.ithanh.udocter.model.DOCTER_INFO;
import vn.ithanh.udocter.model.PATIENT_INFO;
import vn.ithanh.udocter.model.USER_INFO;
import vn.ithanh.udocter.util.Config;
import vn.ithanh.udocter.util.Utils;

/**
 * Created by iThanh on 11/19/2017.
 */

public class ServiceRequest {

    private String mQuery;
    private int mCmd;
    private final static String API = "api/";

    public ServiceRequest(int cmd, String query) {
        this.mQuery = query;
        this.mCmd = cmd;
    }

    public void setQuery(String query) {
        this.mQuery = query;
    }

    public String getQuery() {
        return this.mQuery;
    }

    public void setCmd(int cmd) {
        this.mCmd = cmd;
    }

    public int getCmd() {
        return this.mCmd;
    }

    public static String getDictrct() {
        String url = Config.API_URL
                + "getDictrct";
        Utils.log("url", url);
        return url;
    }

    public static String getSetting() {
        String url = Config.API_URL
                + "getSetting";
        Utils.log("url", url);
        return url;
    }

    public static String getReason() {
        String url = Config.API_URL
                + "getReason";
        Utils.log("url", url);
        return url;
    }

    public static String rateBooking(int id, double rate) {
        String url = Config.API_URL
                + "rateBooking?id="+ id
                + "&r="+ rate;
        Utils.log("url", url);
        return url;
    }

    public static String getBooking(int id, String p) {
        String url = Config.API_URL
                + "getBooking?"+p+"="+ id;
        Utils.log("url", url);
        return url;
    }
    public static String getOneBooking(int id, int t) {
        String url = Config.API_URL
                + "getOneBooking?id="+ id
                + "&t="+ t;
        Utils.log("url", url);
        return url;
    }

    public static String bookingStatus(int id, int stt, String text) throws UnsupportedEncodingException {
        String url = Config.API_URL
                + "bookingStatus?id="+ id
                + "&stt="+ stt
                + "&t="+ URLEncoder.encode(text, "UTF-8");
        Utils.log("url", url);
        return url;
    }

    public static String Booking(int p_id, int d_id,String sdt, String dt, int tam, int vs, float log, float lat) throws UnsupportedEncodingException {
        String url = Config.API_URL
                + "booking?p_id="+ p_id
                + "&d_id="+ d_id
                + "&tam="+ tam
                + "&vs="+ vs
                + "&log="+ log
                + "&lat="+ lat
                + "&sdt="+ sdt
                + "&dt=" +  URLEncoder.encode(dt, "UTF-8");
        Utils.log("url", url);
        return url;
    }

    public static String getAllDocter() {
        String url = Config.API_URL
                + "getAllDocter";
        Utils.log("url", url);
        return url;
    }
    public static String getDocter(int u_id) {
        String url = Config.API_URL
                + "getDocter?u_id="+ u_id;
        Utils.log("url", url);
        return url;
    }
    public static String getPatient(int u_id) {
        String url = Config.API_URL
                + "getPatient?u_id="+ u_id;
        Utils.log("url", url);
        return url;
    }
    public static String getDocterDetail(int d_id) {
        String url = Config.API_URL
                + "getDocterDetail?d_id="+ d_id;
        Utils.log("url", url);
        return url;
    }

    public static String SaveDocter(DOCTER_INFO docter_info) throws UnsupportedEncodingException {
        String url = Config.API_URL
                + "SaveDocter?u_id="+ docter_info.getUser_id()
                + "&name=" + URLEncoder.encode(docter_info.getName(), "UTF-8")
                + "&dob=" + docter_info.getBod()
                + "&info=" + URLEncoder.encode(docter_info.getInfo_p(), "UTF-8")
                + "&hosp=" + URLEncoder.encode(docter_info.getHospital(), "UTF-8")
                + "&addr=" + URLEncoder.encode(docter_info.getAddress(), "UTF-8")
                + "&ya=" + docter_info.getYear()
                + "&sex=" + docter_info.getSex_id()
                + "&lat=" + docter_info.getLatitude()
                + "&lon=" + docter_info.getLongitude();
        Utils.log("url", url);
        return url;
    }

    public static String SavePatient(PATIENT_INFO patient_info) throws UnsupportedEncodingException {
        String url = Config.API_URL
                + "SavePatient?u_id="+ patient_info.getUser_id()
                + "&name=" + URLEncoder.encode(patient_info.getName(), "UTF-8")
                + "&dob=" + patient_info.getBod()
                + "&info=" + URLEncoder.encode(patient_info.getInfo_p(), "UTF-8")
                + "&addr=" + URLEncoder.encode(patient_info.getAddress(), "UTF-8")
                + "&phone=" + patient_info.getPhone()
                + "&sex=" + patient_info.getSex_id()
                + "&lat=" + patient_info.getLatitude()
                + "&lon=" + patient_info.getLongitude();
        Utils.log("url", url);
        return url;
    }

    public static String Register(USER_INFO userInfo) throws UnsupportedEncodingException {
        String url = Config.API_URL
                + "SignUp?username="+ userInfo.getUsername()
                + "&password=" + userInfo.getPassword()
                + "&full_name=" + URLEncoder.encode(userInfo.getFull_name(), "UTF-8")
                + "&address=" + URLEncoder.encode(userInfo.getAddress(), "UTF-8")
                + "&phone=" + userInfo.getPhone()
                + "&email=" + userInfo.getEmail()
                + "&gender=" + userInfo.getGender()
                + "&user_type=" + userInfo.getUser_type()
                ;
        Utils.log("url", url);
        return url;
    }
    public static String Login(String u, String p) {
        String url = Config.API_URL
                + "SignIn?u="+ u
                + "&p=" + p;
        Utils.log("url", url);
        return url;
    }
    public static String FBLogin(String id, String first_name, String email) {
        String url = Config.API_URL
                + "FBLogin?id="+ id
                + "&first_name=" + first_name
                + "&email=" + email;
        Utils.log("url", url);
        return url;
    }
    public static String UpUType(int id, int t) {
        String url = Config.API_URL
                + "UpdateUType?id="+ id
                + "&t=" + t;
        Utils.log("url", url);
        return url;
    }

}
