package vn.ithanh.udocter.util;

/**
 * Created by iThanh on 12/3/2017.
 */

public class Config {
    public static final boolean IS_DEBUG = true;
    public static final int CORNER_RADIUS_PIXELS = 5;

//    public static final String API_URL = "http://192.168.1.159:8081/API/";
//    public static final String URL_IMAGE = "http://192.168.1.159:8081/";
    public static final String API_URL = "http://backup.hipcorp.vn/API/";
    public static final String URL_IMAGE = "http://backup.hipcorp.vn/API/";

    public static final String KEY_DATA = "DATA";
    public static final String TOTAL_PAGE = "TotalPages";
    public static final String KEY_MESSAGE = "Message";

    public static enum ViewState {
        Init, Loading, Loaded, Empty, MoreLoading, MoreLoaded
    }

    public static final int CMD_REGISTER = 0;
    public static final int CMD_LOGIN = 1;
    public static final int CMD_SAVE_DOCTER = 2;
    public static final int CMD_GET_DOCTER = 3;
    public static final int CMD_GET_ALL_DOCTER = 4;
    public static final int CMD_GET_DOCTER_DETAIL = 5;
    public static final int CMD_P_BOOKING = 6;
    public static final int CMD_DOC_GET_BOOKING = 7;
    public static final int CMD_BOOKING_UPDATE = 8;
    public static final int CMD_FB_LOGIN= 9;
    public static final int CMD_SAVE_PATIENT = 10;
    public static final int CMD_GET_PATIENT = 11;
    public static final int CMD_DOC_GET_ONE_BOOKING = 12;
    public static final int CMD_GET_DICTRCT= 13;
    public static final int CMD_RATE_BOOKING= 14;
    public static final int CMD_GET_REASON = 15;
    public static final int CMD_GET_SETTING = 16;


    public interface SETTING{
        public static final String KEY_USER_ID = "USER_ID";
        public static final String AVATAR_DOCTER_DEFAULT = "AVATAR_DOCTER_DEFAULT";
        public static final String ADDRESS_DOCTER_DEFAULT = "ADDRESS_DOCTER_DEFAULT";
        public static final String PRICE_SERVICE = "PRICE_SERVICE";
    }

    public interface USER_UD{
        public static final String KEY_ID = "id";
        public static final String KEY_username = "username";
        public static final String KEY_full_name = "full_name";
        public static final String KEY_address = "address";
        public static final String KEY_phone = "phone";
        public static final String KEY_user_type = "user_type";
        public static final String KEY_d_id = "d_id";
        public static final String KEY_p_id = "p_id";
        public static final String KEY_longitude = "u_longitude";
        public static final String KEY_latitude = "u_latitude";
    }
    public interface BOOKING_INFO{
        public static final String id = "id";
        public static final String patient_id = "patient_id";
        public static final String patient_name = "patient_name";
        public static final String patient_phone = "patient_phone";
        public static final String docter_id = "docter_id";

        public static final String docter_name = "docter_name";
        public static final String docter_phone = "docter_phone";
        public static final String comment = "comment";
        public static final String created = "created";
        public static final String updated = "updated";

        public static final String status = "status";
        public static final String tam_date = "tam_date";
        public static final String vs_date = "vs_date";

    }

}
