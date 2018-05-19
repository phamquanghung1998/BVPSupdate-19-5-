package vn.ithanh.udocter.service;


import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

import vn.ithanh.udocter.util.Utils;

/**
 * Created by iThanh on 11/19/2017.
 */

public class UDocterServices extends AsyncTask<Void, Void, String> {
    private final String TAG = "APKdroidService";

    private ServiceRequest mRequest;
    private IServiceCallback mCallback;

    public static UDocterServices invoke(IServiceCallback callback,
                                         ServiceRequest req) {
        return new UDocterServices(callback, req);
    }
    public UDocterServices(IServiceCallback callback, ServiceRequest req) {
        this.mCallback = callback;
        this.mRequest = req;
    }


    @Override
    protected String doInBackground(Void... voids) {
        String jsonResp = null;
        try {
            jsonResp = GET(mRequest.getQuery());
        } catch (IOException e) {
            Utils.log(TAG, "Failed to invoke request: " + e.getMessage());
        }

        return jsonResp;
    }
    @Override
    protected void onPostExecute(String result) {
        if (mCallback != null) {
            mCallback.onResponseReceived(mRequest.getCmd(), result);
        }
    }

    protected String GET(String url) throws IOException {

        InputStream inputStream = null;
        String result = "";
        try {
            // create new client
            HttpClient httpClient = new DefaultHttpClient();

            // make GET request to the given URL
            HttpResponse httpResponse = httpClient.execute(new HttpGet(url));

            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // convert inputstream to string
            if (inputStream != null)
                result = Utils.convertStreamToString(inputStream);
            else
                result = "";

        } catch (IOException e) {
            throw e;
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    public static <T> T toObject(String jsonResp, Type type)
            throws ServiceException {
        Gson gson = new Gson();

        ServiceResult<T> result = null;
        try {
            result = (ServiceResult<T>) gson.fromJson(jsonResp, type);
        } catch (JsonParseException e) {
            throw new ServiceException(ServiceException.ERR_INVALID_RESPONSE, e);
        }

        if (result.getErrorCode() != 0) {
            throw new ServiceException(ServiceException.ERR_INVALID_RESPONSE,
                    result.getErrorCode());
        }

        return result.getResult();
    }

    @SuppressWarnings("unchecked")
    public static <T> T toObject2(String jsonResp, Type type)
            throws ServiceException {
        Gson gson = new Gson();

        T result = null;
        try {
            result = (T) gson.fromJson(jsonResp, type);
        } catch (JsonParseException e) {
            throw new ServiceException(ServiceException.ERR_INVALID_RESPONSE, e);
        }

        return result;
    }
}
