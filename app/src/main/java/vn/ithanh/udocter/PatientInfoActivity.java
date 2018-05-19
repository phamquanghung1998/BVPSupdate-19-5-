package vn.ithanh.udocter;

import android.Manifest;
import android.app.Activity;
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

import vn.ithanh.udocter.model.PATIENT_INFO;
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

public class PatientInfoActivity extends AppCompatActivity implements IServiceCallback {
    private static final int REQUEST_ACCESS_FINE_LOCATION = 1;
    private static String[] PERMISSIONS_MAPS = {
            Manifest.permission.ACCESS_FINE_LOCATION};

    private EditText ptt_input_name;
    private EditText ptt_input_dob;
    private RadioGroup gender_radio_group_ptt;//gender_radio_group_ptt
    private EditText ptt_input_info_p;
    private EditText ptt_input_address;
    private EditText ptt_input_phone;
    private EditText ptt_input_location;

    private RadioButton female_radio_btn_ptt;

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
        setContentView(R.layout.patient_info_layout);

        this.mySharedPreferences = new MySharedPreferences(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_general);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(getString(R.string.title_my_info));

        // Get contro
        ptt_input_name = findViewById(R.id.ptt_input_name);
        ptt_input_dob = findViewById(R.id.ptt_input_dob);
        gender_radio_group_ptt = findViewById(R.id.gender_radio_group_ptt);
        ptt_input_info_p = findViewById(R.id.ptt_input_info_p);
        ptt_input_address = findViewById(R.id.ptt_input_address);
        ptt_input_phone = findViewById(R.id.ptt_input_phone);
        ptt_input_location = findViewById(R.id.ptt_input_location);

        female_radio_btn_ptt = findViewById(R.id.female_radio_btn_ptt);
        //mProgressView = findViewById(R.id.info_doc_progress);
        //mDocFormView = findViewById(R.id.frm_doc_info);

        if (currentlocation!=null)
            ptt_input_location.setText(currentlocation.getLongitude() +"," +currentlocation.getLatitude());

        Button btn = (Button) findViewById(R.id.btn_save_info_ptt);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSave();
            }
        });

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

        ptt_input_dob.setInputType(InputType.TYPE_NULL);
        ptt_input_dob.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //View view = this.getCurrentFocus();
                if (v != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
                // TODO Auto-generated method stub
                DatePickerDialog datePickerDialog = new DatePickerDialog(PatientInfoActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),myCalendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.getDatePicker().getTouchables().get( 0 ).performClick();
                datePickerDialog.show();
            }
        });

        ptt_input_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                    Intent intent = builder.build(PatientInfoActivity.this);
                    startActivityForResult(intent, PLACE_PICKER_REQUEST);
                    //System.out.println("start activity for result");
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                    Log.i("LOG",  "GooglePlayServicesRepairableException : " + e.getMessage());
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                    Log.i("LOG",  "GooglePlayServicesNotAvailableException : " + e.getMessage());
                }

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
                ptt_input_location.setText(place.getLatLng().longitude + "," + place.getLatLng().latitude);
            }
        }
    }


    private void updateLabel() {
        String myFormat = "yyyy-MM-dd"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.FRANCE);
        ptt_input_dob.setText(sdf.format(myCalendar.getTime()));
    }
    private void viewState(Config.ViewState state) {
        if (Config.ViewState.Loading.equals(state)) {
            //mDocFormView.setVisibility(View.GONE);
            //mProgressView.setVisibility(View.VISIBLE);
            return;
        }
        // Loaded
        if (Config.ViewState.Loaded.equals(state)) {
            //mDocFormView.setVisibility(View.VISIBLE);
            //mProgressView.setVisibility(View.GONE);
            return;
        }
    }

    public void attemptSave(){
        ConnectionDetector cd = new ConnectionDetector(this);
        if (!cd.isConnectingToInternet()) {
            MessageBox.showToast(this, getString(R.string.msg_no_internet_access));
            return;
        }
        if (isLoading)
            return;

        PATIENT_INFO patient_info = new PATIENT_INFO();
        patient_info.setUser_id(mySharedPreferences.getUSER_ID());
        patient_info.setName(ptt_input_name.getText().toString());
        patient_info.setBod(ptt_input_dob.getText().toString());
        patient_info.setInfo_p(ptt_input_info_p.getText().toString());
        patient_info.setAddress(ptt_input_address.getText().toString());
        patient_info.setPhone(ptt_input_phone.getText().toString());

        if (ptt_input_location.getText().toString().contains(",")){
            String[] location = ptt_input_location.getText().toString().split(",");
            patient_info.setLongitude(Float.parseFloat(location[0]));
            patient_info.setLatitude(Float.parseFloat(location[1]));

        }

        RadioButton rbgender = findViewById(gender_radio_group_ptt.getCheckedRadioButtonId());
        patient_info.setSex_id(0);
        if (rbgender.getText().toString().equals(getString(R.string.male)))
        {
            patient_info.setSex_id(1);
        }

        try {
            ServiceRequest serviceRequest = new ServiceRequest(Config.CMD_SAVE_PATIENT, ServiceRequest.SavePatient(patient_info));
            UDocterServices.invoke(this, serviceRequest).execute();
            isLoading = true;
            viewState(Config.ViewState.Loading);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void getInfo(){
        try {
            ServiceRequest serviceRequest = new ServiceRequest(Config.CMD_GET_PATIENT, ServiceRequest.getPatient(mySharedPreferences.getUSER_ID()));
            UDocterServices.invoke(this, serviceRequest).execute();
            isLoading = true;
            viewState(Config.ViewState.Loading);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        if (!checkPermission()) {
            verifyMapsPermissions(this);
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
    public void onResponseReceived(int cmd, String resp) {
        viewState(Config.ViewState.Loaded);
        isLoading = false;
        if (Utils.isNullOrEmpty(resp)) {
            return;
        }
        if (cmd == Config.CMD_GET_PATIENT)
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

                                ptt_input_name.setText(docInfo.getString("full_name"));
                                if (docInfo.getString("dob").length() > 6)
                                    ptt_input_dob.setText(docInfo.getString("dob"));
                                ptt_input_info_p.setText(docInfo.getString("info_p"));
                                ptt_input_address.setText(docInfo.getString("address"));
                                ptt_input_phone.setText(docInfo.getString("phone"));
                                ptt_input_location.setText(docInfo.getDouble("longitude") + "," + docInfo.getDouble("latitude"));
                                if (docInfo.getInt("sex_id") == 0)
                                    female_radio_btn_ptt.setChecked(true);
                            }else{
                                String message = jObj.getString(Config.KEY_MESSAGE);
                                MessageBox.showToast(getApplicationContext(), message);
                            }
                        } else {
                            MessageBox.showToast(getApplicationContext(), getString(R.string.no_data));
                        }

                    } catch (JSONException e) {
                        viewState(Config.ViewState.Empty);
                        MessageBox.showToast(getApplicationContext(),
                                getString(R.string.msg_request_fail_retry));
                    }
                }
            });
            return;
        }
        if (cmd == Config.CMD_SAVE_PATIENT)
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
                        viewState(Config.ViewState.Empty);
                        MessageBox.showToast(getApplicationContext(),
                                getString(R.string.msg_request_fail_retry));
                    }
                }

            });
            return;
        }
    }
}
