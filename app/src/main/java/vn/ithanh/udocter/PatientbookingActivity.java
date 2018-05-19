package vn.ithanh.udocter;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import vn.ithanh.udocter.model.BOOKING_INFO;
import vn.ithanh.udocter.service.IServiceCallback;
import vn.ithanh.udocter.service.ServiceRequest;
import vn.ithanh.udocter.service.UDocterServices;
import vn.ithanh.udocter.util.Config;
import vn.ithanh.udocter.util.ConnectionDetector;
import vn.ithanh.udocter.util.MessageBox;
import vn.ithanh.udocter.util.MySharedPreferences;
import vn.ithanh.udocter.util.Utils;

/**
 * Created by iThanh on 12/24/2017.
 */

public class PatientbookingActivity extends AppCompatActivity implements IServiceCallback {

    private ImageLoader uilImageLoader;
    private DisplayImageOptions options;
    private ImageView imageView;
    private RecyclerView rv;

    private ArrayList<BOOKING_INFO> dt = new ArrayList<BOOKING_INFO>();

    private boolean isLoading = false;
    ProgressDialog progress;

    MySharedPreferences mySharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.patient_booking_activity);

        final Toolbar toolbar = findViewById(R.id.toolbar_pat);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Lịch sử đặt");

        rv = findViewById(R.id.recyclerview_booking);

        this.mySharedPreferences = new MySharedPreferences(this);

        uilImageLoader = MyApplication.uilImageLoader;
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.img_loadding)
                .showImageForEmptyUri(R.drawable.img_loadding)
                .showImageOnFail(R.drawable.img_loadding)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .displayer(
                        new RoundedBitmapDisplayer(Config.CORNER_RADIUS_PIXELS))
                .bitmapConfig(Bitmap.Config.RGB_565).build();

        doRequest();
    }
    public void showProgress(){
        progress = ProgressDialog.show(PatientbookingActivity.this, "", "Loading...", true);
    }
    private void doRequest() {
        ConnectionDetector cd = new ConnectionDetector(this);
        if (!cd.isConnectingToInternet()) {
            MessageBox.showToast(this, getString(R.string.msg_no_internet_access));
            return;
        }
        if (isLoading)
            return;
        try {
            ServiceRequest serviceRequest = new ServiceRequest(Config.CMD_DOC_GET_BOOKING, ServiceRequest.getBooking(mySharedPreferences.getP_ID(), "p_id"));
            UDocterServices.invoke(this, serviceRequest).execute();
            isLoading = true;
            showProgress();
            //viewState(Config.ViewState.Loading);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void doChangeStatus(int b_id, int status){
        ConnectionDetector cd = new ConnectionDetector(this);
        if (!cd.isConnectingToInternet()) {
            MessageBox.showToast(this,
                    getString(R.string.msg_no_internet_access));
            return;
        }
        try {
            ServiceRequest serviceRequest = new ServiceRequest(Config.CMD_BOOKING_UPDATE, ServiceRequest.bookingStatus(b_id, status, ""));
            UDocterServices.invoke(this, serviceRequest).execute();
            isLoading = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public void setView()
    {
        rv.setLayoutManager(new LinearLayoutManager(rv.getContext()));
        rv.setAdapter(new PatientRecyclerViewAdapter(this, dt));
    }

    @Override
    public void onResponseReceived(int cmd, String resp) {
        //viewState(Config.ViewState.Loaded);
        isLoading = false;
        progress.dismiss();
        if (Utils.isNullOrEmpty(resp)) {
            return;
        }
        if (cmd == Config.CMD_BOOKING_UPDATE) {
            finish();
            startActivity(getIntent());
        }
        if (cmd == Config.CMD_DOC_GET_BOOKING) {
            final String mResp = resp;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    try {
                        JSONObject jObj = new JSONObject(mResp);
                        if ((jObj != null) && (jObj.getInt("ERR_CODE") == 0)) {
                            //viewState(Config.ViewState.Loaded);
                            JSONArray jsArr = jObj.getJSONArray(Config.KEY_DATA);

                            //Log.i("jsArr", jsArr.toString());
                            for (int i = 0; i < jsArr.length(); i++) {
                                JSONObject tmp = jsArr.getJSONObject(i);
                                BOOKING_INFO c = new BOOKING_INFO();
                                c.setId(tmp.getInt(Config.BOOKING_INFO.id));
                                c.setPatient_id(tmp.getInt(Config.BOOKING_INFO.patient_id));
                                c.setPatient_name(tmp.getString(Config.BOOKING_INFO.patient_name));
                                c.setPatient_phone(tmp.getString(Config.BOOKING_INFO.patient_phone));
                                c.setDocter_id(tmp.getInt(Config.BOOKING_INFO.docter_id));
                                c.setDocter_name(tmp.getString(Config.BOOKING_INFO.docter_name));
                                c.setDocter_phone(tmp.getString(Config.BOOKING_INFO.docter_phone));
                                c.setComment(tmp.getString(Config.BOOKING_INFO.comment));
                                c.setCreated(tmp.getString(Config.BOOKING_INFO.created));
                                c.setUpdated(tmp.getString(Config.BOOKING_INFO.updated));
                                c.setStatus(tmp.getInt(Config.BOOKING_INFO.status));
                                c.setTam_date(tmp.getInt(Config.BOOKING_INFO.tam_date));
                                c.setVs_date(tmp.getInt(Config.BOOKING_INFO.vs_date));
                                c.setRate(tmp.getDouble("rate"));
                                dt.add(c);
                            }
                            setView();
                        } else {
                            MessageBox.showToast(getApplicationContext(),
                                    getString(R.string.no_data));
                        }

                    } catch (JSONException e) {
                        MessageBox.showToast(getApplicationContext(),
                                getString(R.string.msg_request_fail_retry));
                    }
                    isLoading = false;
                }

            });
            return;
        }

    }
}
