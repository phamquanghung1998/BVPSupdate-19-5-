package vn.ithanh.udocter;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import vn.ithanh.udocter.model.DOCTER_INFO;
import vn.ithanh.udocter.service.IServiceCallback;
import vn.ithanh.udocter.service.ServiceRequest;
import vn.ithanh.udocter.service.UDocterServices;
import vn.ithanh.udocter.util.Config;
import vn.ithanh.udocter.util.ConnectionDetector;
import vn.ithanh.udocter.util.MessageBox;
import vn.ithanh.udocter.util.MySharedPreferences;
import vn.ithanh.udocter.util.Utils;

/**
 * Created by iThanh on 12/5/2017.
 */

public class DocterInfoActivity extends AppCompatActivity implements IServiceCallback {

    private static final int REQUEST_ACCESS_FINE_LOCATION = 1;
    private static String[] PERMISSIONS_MAPS = {
            Manifest.permission.ACCESS_FINE_LOCATION};


    private EditText docter_input_name;
    private EditText docter_input_dob;
    private RadioGroup gender_radio_group_docter;//gender_radio_group_docter
    private EditText docter_input_info_p;
    private EditText docter_input_hospital;
    private EditText docter_input_year;
    private EditText docter_input_address;
    private EditText docter_input_location;

    private RadioButton female_radio_btn_docter;

    //private View mDocFormView;
    //private View mProgressView;

    private boolean isLoading = false;

    MySharedPreferences mySharedPreferences;

    LocationManager locationManager;

    Location currentlocation;

    final Calendar myCalendar = Calendar.getInstance();
    DatePickerDialog.OnDateSetListener date;

    private final static int PLACE_PICKER_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.docter_info_layout);

        this.mySharedPreferences = new MySharedPreferences(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_general);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(getString(R.string.title_my_info));

        // Get contro
        docter_input_name = findViewById(R.id.docter_input_name);
        docter_input_dob = findViewById(R.id.docter_input_dob);
        gender_radio_group_docter = findViewById(R.id.gender_radio_group_docter);
        docter_input_info_p = findViewById(R.id.docter_input_info_p);
        docter_input_hospital = findViewById(R.id.docter_input_hospital);
        docter_input_year = findViewById(R.id.docter_input_year);
        docter_input_address = findViewById(R.id.docter_input_address);
        docter_input_location = findViewById(R.id.docter_input_location);

        female_radio_btn_docter = findViewById(R.id.female_radio_btn_docter);
        //mProgressView = findViewById(R.id.info_doc_progress);
        //mDocFormView = findViewById(R.id.frm_doc_info);

        if (!checkPermission()) {
            verifyMapsPermissions(this);
        }

        date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }

        };
        docter_input_dob.setInputType(InputType.TYPE_NULL);
        docter_input_dob.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //View view = this.getCurrentFocus();
                if (v != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
                // TODO Auto-generated method stub
                DatePickerDialog datePickerDialog = new DatePickerDialog(DocterInfoActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),myCalendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.getDatePicker().getTouchables().get( 0 ).performClick();
                datePickerDialog.show();
            }
        });

        docter_input_location.setInputType(InputType.TYPE_NULL);
        docter_input_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                Intent intent;
                try {
                    intent = builder.build(DocterInfoActivity.this);
                    startActivityForResult(intent, PLACE_PICKER_REQUEST);
                    //System.out.println("start activity for result");
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }

            }
        });


        Button btn = (Button) findViewById(R.id.btn_save_info_docter);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSave();
            }
        });

        getInfo();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                //String toastMsg = String.format("Place: %s", place.getName());
                //Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
                docter_input_location.setText(place.getLatLng().longitude + "," + place.getLatLng().latitude);
            }
        }
    }
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        //checkPermissionOnActivityResult(requestCode, resultCode, data);
//
//        if (resultCode == RESULT_OK) {
//            switch (requestCode){
//                case PLACE_PICKER_REQUEST:
//                    Place place = PlacePicker.getPlace(this, data);
//                    String placeName = String.format("Place: %s", place.getName());
//                    double latitude = place.getLatLng().latitude;
//                    double longitude = place.getLatLng().longitude;
//
//            }
//        }
//    }
    private void updateLabel() {
        String myFormat = "yyyy-MM-dd"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.FRANCE);
        docter_input_dob.setText(sdf.format(myCalendar.getTime()));
    }
    public void attemptSave(){
        ConnectionDetector cd = new ConnectionDetector(this);
        if (!cd.isConnectingToInternet()) {
            MessageBox.showToast(this, getString(R.string.msg_no_internet_access));
            return;
        }
        if (isLoading)
            return;

        DOCTER_INFO docter_info = new DOCTER_INFO();
        docter_info.setUser_id(mySharedPreferences.getUSER_ID());
        docter_info.setName(docter_input_name.getText().toString());
        if (docter_input_dob.getText().length() > 6)
            docter_info.setBod(docter_input_dob.getText().toString());
        docter_info.setInfo_p(docter_input_info_p.getText().toString());
        docter_info.setHospital(docter_input_hospital.getText().toString());
        docter_info.setYear(Integer.parseInt(docter_input_year.getText().toString()));
        docter_info.setAddress(docter_input_address.getText().toString());
        if (docter_input_location.getText().toString().contains(",")){
            String[] location = docter_input_location.getText().toString().split(",");
            docter_info.setLongitude(Float.parseFloat(location[0]));
            docter_info.setLatitude(Float.parseFloat(location[1]));

        }

        RadioButton rbgender = findViewById(gender_radio_group_docter.getCheckedRadioButtonId());
        docter_info.setSex_id(0);
        if (rbgender.getText().toString().equals(getString(R.string.male)))
        {
            docter_info.setSex_id(1);
        }

        try {
            ServiceRequest serviceRequest = new ServiceRequest(Config.CMD_SAVE_DOCTER, ServiceRequest.SaveDocter(docter_info));
            UDocterServices.invoke(this, serviceRequest).execute();
            isLoading = true;
            //viewState(Config.ViewState.Loading);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void getInfo(){
        try {
            ServiceRequest serviceRequest = new ServiceRequest(Config.CMD_GET_DOCTER, ServiceRequest.getDocter(mySharedPreferences.getUSER_ID()));
            UDocterServices.invoke(this, serviceRequest).execute();
            isLoading = true;
            //viewState(Config.ViewState.Loading);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private boolean checkPermission() {
        // Ask for permission if it wasn't granted yet
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED );
    }
    public static void verifyMapsPermissions(FragmentActivity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_MAPS,
                    REQUEST_ACCESS_FINE_LOCATION
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, false);

        if (!checkPermission()) {
            verifyMapsPermissions(this);
        }
    }
    @Override
    public void onResponseReceived(int cmd, String resp) {
        //viewState(Config.ViewState.Loaded);
        isLoading = false;
        if (Utils.isNullOrEmpty(resp)) {
            return;
        }
        if (cmd == Config.CMD_GET_DOCTER)
        {
            final String mResp = resp;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    try {
                        JSONObject jObj = new JSONObject(mResp);
                        if ((jObj != null)) {
                            if ((jObj.getInt("ERR_CODE") == 0)) {
                                JSONObject docInfo = jObj.getJSONObject(Config.KEY_DATA);
                                if (docInfo.getString("name")!= null)
                                    docter_input_name.setText(docInfo.getString("name"));
                                if (docInfo.getString("dob").length() > 6)
                                    docter_input_dob.setText(docInfo.getString("dob"));
                                if (docInfo.getString("info_p")!= null || docInfo.getString("info_p").equals("null"))
                                    docter_input_info_p.setText(docInfo.getString("info_p"));
                                if (docInfo.getString("hospital")!= null || docInfo.getString("hospital").equals("null"))
                                    docter_input_hospital.setText(docInfo.getString("hospital"));
                                if (docInfo.getString("year")!= null || docInfo.getString("year").equals("null"))
                                    docter_input_year.setText(docInfo.getInt("year") + "");
                                if (docInfo.getString("address")!= null)
                                    docter_input_address.setText(docInfo.getString("address"));
                                if (docInfo.getString("latitude")!= null)
                                    docter_input_location.setText(docInfo.getDouble("longitude") + "," + docInfo.getDouble("latitude"));
                                if (docInfo.getInt("sex_id") == 0)
                                    female_radio_btn_docter.setChecked(true);
                            }else{
                                String message = jObj.getString(Config.KEY_MESSAGE);
                                MessageBox.showToast(getApplicationContext(), message);
                            }
                        } else {
                            MessageBox.showToast(getApplicationContext(), getString(R.string.no_data));
                        }

                    } catch (JSONException e) {
                        //viewState(Config.ViewState.Empty);
                        MessageBox.showToast(getApplicationContext(),
                                getString(R.string.msg_request_fail_retry));
                    }
                }
            });
            return;
        }
        if (cmd == Config.CMD_SAVE_DOCTER)
        {
            final String mResp = resp;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    try {
                        JSONObject jObj = new JSONObject(mResp);
                        if ((jObj != null)) {
                            String message = jObj.getString(Config.KEY_MESSAGE);
                            MessageBox.showToast(getApplicationContext(), message);
                        } else {
                            MessageBox.showToast(getApplicationContext(), getString(R.string.no_data));
                        }

                    } catch (JSONException e) {
                        //viewState(Config.ViewState.Empty);
                        MessageBox.showToast(getApplicationContext(),
                                getString(R.string.msg_request_fail_retry));
                    }
                }

            });
            return;
        }
    }
}
