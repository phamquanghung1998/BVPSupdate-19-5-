package vn.ithanh.udocter.service;


import com.google.gson.annotations.SerializedName;

/**
 * Created by iThanh on 11/19/2017.
 */

public class ServiceResult<T> {

    @SerializedName("ERR_CODE")
    private int errorCode = -1;

    @SerializedName("TotalPages")
    private int totalPage = 1;

    @SerializedName("DATA")
    private T result;

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public int getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

}
