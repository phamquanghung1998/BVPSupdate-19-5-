package vn.ithanh.udocter.service;

/**
 * Created by iThanh on 11/19/2017.
 */

public class ServiceException extends Exception{
    private int errorCode = 0;
    private static final long serialVersionUID = 1L;

    public static final String ERR_IOEXCEPTION = "ION";
    public static final String ERR_INVALID_RESPONSE = "INV";
    public static final String ERR_UNKNOWN = "UNN";

    public ServiceException(String reason) {
        super(reason);
    }

    public ServiceException(String reason, Throwable e) {
        super(reason, e);
    }

    public ServiceException(String reason, int errorCode) {
        super(reason);
        this.errorCode = errorCode;
    }

    public ServiceException(String reason, int errorCode, Throwable e) {
        super(reason, e);
        this.errorCode = errorCode;
    }

    public final int getErrorCode() {
        return errorCode;
    }
}
