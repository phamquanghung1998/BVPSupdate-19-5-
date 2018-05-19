package vn.ithanh.udocter;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import vn.ithanh.udocter.model.BOOKING_INFO;
import vn.ithanh.udocter.model.DICTRCT_INFO;
import vn.ithanh.udocter.model.DOCTER_INFO;
import vn.ithanh.udocter.service.IServiceCallback;
import vn.ithanh.udocter.service.ServiceRequest;
import vn.ithanh.udocter.service.UDocterServices;
import vn.ithanh.udocter.util.Config;
import vn.ithanh.udocter.util.ConnectionDetector;
import vn.ithanh.udocter.util.MessageBox;
import vn.ithanh.udocter.util.MySharedPreferences;
import vn.ithanh.udocter.util.TimeUtils;
import vn.ithanh.udocter.util.Utils;

public class BookingDetailActivity extends AppCompatActivity implements OnMapReadyCallback, IServiceCallback, RoutingListener {


    MySharedPreferences mySharedPreferences;
    private static final int REQUEST_ACCESS_FINE_LOCATION = 1;
    private static String[] PERMISSIONS_MAPS = {
            Manifest.permission.ACCESS_FINE_LOCATION};
    private static String[] PERMISSIONS_CALL = {
            Manifest.permission.CALL_PHONE};
    GoogleMap mGoogleMap;
    LatLng latlng;
    private HashMap<Marker, Integer> mHashMap = new HashMap<Marker, Integer>();
    //Data
    private ArrayList<DOCTER_INFO> dt = new ArrayList<DOCTER_INFO>();

    boolean firstLoad = true;

    private boolean isLoading = false;

    public static final String EXTRA_BOOKING_ID = "BOOKING_ID";

    private int bk_id;

    private TextView tvTongTien;
    private TextView tv_PhoneNote;
    private TextView tvPhoneTitle;
    private TextView tvTimeCreate;

    private TextView tvKhoangcach;
    private TextView tvTam;
    private TextView tvVS;
    private Button btnGreen;
    private Button btnYellow;
    private Button btnCallNow;

    private Button btnSubmitrate;
    private RatingBar ratingBooking;

    private BOOKING_INFO booking_info;

    ProgressDialog progress;

    private ArrayList<Polyline> polylines;

    private List<String> listReason = new ArrayList<String>();

    private static final int[] COLORS = new int[]{R.color.primary_dark,
            R.color.primary, R.color.primary_light, R.color.accent,
            R.color.primary_dark_material_light};

    private boolean isLoadMap = false;
    private boolean isLoadData = false;
    private boolean isLoadDuong = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.booking_detail_layout);

        Intent intent = getIntent();
        bk_id = Integer.parseInt(intent.getStringExtra(EXTRA_BOOKING_ID));

        this.mySharedPreferences = new MySharedPreferences(this);

        polylines = new ArrayList<Polyline>();

        tvTongTien = findViewById(R.id.tvTongTien);
        tv_PhoneNote = findViewById(R.id.tvPhoneNote);
        tvPhoneTitle = findViewById(R.id.tvPhoneTitle);
        tvTimeCreate = findViewById(R.id.tvTimeCreate);

        tvKhoangcach = findViewById(R.id.tvKhoangcach);
        tvTam = findViewById(R.id.tvTam);
        tvVS = findViewById(R.id.tvVS);

        btnGreen = findViewById(R.id.btnGreen);
        btnYellow = findViewById(R.id.btnYellow);
        btnCallNow = findViewById(R.id.btnCallNow);

        btnSubmitrate = findViewById(R.id.btnSubmitrate);
        ratingBooking = findViewById(R.id.ratingBooking);


        final Toolbar toolbar = findViewById(R.id.toolbar_booking);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.main_map);
        mapFragment.getMapAsync(this);

        btnGreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (booking_info.getStatus() == 0) {
                    doChangeStatus(booking_info.getId(), 1, "");
                }
                if (booking_info.getStatus() == 1) {
                    doChangeStatus(booking_info.getId(), 2, "");
                }
            }
        });
//        btnYellow.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                final AlertDialog.Builder builder = new AlertDialog.Builder(BookingDetailActivity.this);
//                builder.setTitle("Lý do hủy");
//                final EditText input = new EditText(BookingDetailActivity.this);
//                input.setInputType(InputType.TYPE_CLASS_TEXT);
//                input.setHint("Lý do hủy");
//                input.setText("");
//                builder.setView(input);
//                builder.setPositiveButton("Đồng ý",
//                        new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                int status = 4;
//                                if (mySharedPreferences.getUSER_TYPE() == 2) {
//                                    status = 3;
//                                }
//
//                                doChangeStatus(booking_info.getId(), status, input.getText().toString());
//                            }
//                        });
//                builder.setNegativeButton("Đóng", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.cancel();
//                    }
//                });
//                builder.show();
//            }
//        });
        btnYellow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(BookingDetailActivity.this);
                LayoutInflater inflater = BookingDetailActivity.this.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.dialog_confirm_cancel_booking, null);
                dialogBuilder.setView(dialogView);

                //settitle
                TextView tvTitle = dialogView.findViewById(R.id.tvTitle);
                String title = "LÝ DO HỦY";
                tvTitle.setText(title.toUpperCase());


                final Spinner dropdownReason = dialogView.findViewById(R.id.spinnerReason);;

                String[] stringArray = listReason.toArray(new String[0]);
                ArrayAdapter<String> adapter = new ArrayAdapter<>(BookingDetailActivity.this, R.layout.spinner_dropdown_item, stringArray);
                dropdownReason.setAdapter(adapter);


                final AlertDialog alertDialog = dialogBuilder.create();
                Button btnok = dialogView.findViewById(R.id.button_positive);
                btnok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int status = 4;
                        if (mySharedPreferences.getUSER_TYPE() == 2) {
                            status = 3;
                        }
                        doChangeStatus(booking_info.getId(), status, dropdownReason.getSelectedItem().toString());
                        alertDialog.dismiss();
                    }
                });
                Button btnHuy = dialogView.findViewById(R.id.button_negative);
                btnHuy.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.dismiss();
                    }
                });
                alertDialog.show();
            }
        });
        btnCallNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (booking_info.getPatient_phone().length() > 7) {
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:" + booking_info.getPatient_phone()));
                    if (checkPermissionCall()) {
                        startActivity(callIntent);
                    }
                    else{
                        verifyMapsPermissionsCall(BookingDetailActivity.this);
                    }


                }
            }
        });

        btnSubmitrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (booking_info.getRate() == 0){
                    Log.i("btnSubmitrate", "btnSubmitrate: " + ratingBooking.getRating());
                    doRate(booking_info.getId(), ratingBooking.getRating());
                }


            }
        });
        if (mySharedPreferences.getUSER_TYPE() == 2) {
            isParient();
        }
    }

    public void GET_REASON(){
        ConnectionDetector cd = new ConnectionDetector(this);
        if (!cd.isConnectingToInternet()) {
            MessageBox.showToast(this, getString(R.string.msg_no_internet_access));
            return;
        }
        try {
            ServiceRequest serviceRequest = new ServiceRequest(Config.CMD_GET_REASON, ServiceRequest.getReason());
            UDocterServices.invoke(this, serviceRequest).execute();
            isLoading = true;
            showProgress();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showProgress(){
        progress = ProgressDialog.show(BookingDetailActivity.this, "", "Loading...", true);
    }
    public void isParient(){
        btnCallNow.setVisibility(View.GONE);
        btnGreen.setVisibility(View.GONE);
        tvPhoneTitle.setVisibility(View.GONE);
        tv_PhoneNote.setVisibility(View.GONE);

    }

    public void showView(){
        String time = "";
        if (booking_info.getCreated().length() > 6) {
            try {
                time = TimeUtils.millisToLongDHMS(booking_info.getCreated());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            int tongtien = (booking_info.getTam_date() + booking_info.getVs_date())*300000;
            tvTimeCreate.setText(time + "/" + Utils.priceWithout(tongtien));
        }

        setTitle("Mã đặt lịch : " + booking_info.getId());

        tvTam.setText("Tắm : " + booking_info.getTam_date() + " N");
        tvVS.setText("VS : " + booking_info.getVs_date() + " N");

        //tvTongTien.setText("Thành tiền : " + );
        tvTongTien.setVisibility(View.GONE);

        //tv_nameDate.setText(booking_info.getPatient_name());
        if (booking_info.getStatus() == 1) {
            if (mySharedPreferences.getUSER_TYPE() != 2) {
                tv_PhoneNote.setText(booking_info.getPatient_phone() + " - " + booking_info.getComment());
                btnCallNow.setVisibility(View.VISIBLE);
                btnGreen.setText("Hoàn thành");
            }
        }else if (booking_info.getStatus() == 0) {
            tv_PhoneNote.setText("Xác nhận để lấy số liên hệ");
            btnGreen.setText("Xác nhận");
        }else{
            btnGreen.setVisibility(View.GONE);
            btnYellow.setVisibility(View.GONE);
        }
        if (mySharedPreferences.getUSER_TYPE() != 2){
            btnYellow.setVisibility(View.GONE);
        }
        if (isLoadMap){
            if (!isLoadDuong)
                DiectionsMaps();
        }

        Log.i("getStatus", "showView: "+ booking_info.getStatus());
        Log.i("getUSER_TYPE", "showView: "+ mySharedPreferences.getUSER_TYPE());
        //Show Rate
        if (booking_info.getStatus() == 2 && mySharedPreferences.getUSER_TYPE() == 2){
            //Log.i("showView", "showView: Show Rate");
            btnSubmitrate.setVisibility(View.VISIBLE);
            ratingBooking.setVisibility(View.VISIBLE);
            if (booking_info.getRate() > 0){
                ratingBooking.setRating((float) booking_info.getRate());
                btnSubmitrate.setVisibility(View.GONE);
            }
        }
    }

    public void doRate(int b_id, double rate){
        ConnectionDetector cd = new ConnectionDetector(this);
        if (!cd.isConnectingToInternet()) {
            MessageBox.showToast(this,
                    getString(R.string.msg_no_internet_access));
            return;
        }
        try {
            ServiceRequest serviceRequest = new ServiceRequest(Config.CMD_RATE_BOOKING, ServiceRequest.rateBooking(b_id, rate));
            UDocterServices.invoke(this, serviceRequest).execute();
            isLoading = true;
            //showProgress();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void doChangeStatus(int b_id, int status, String txt){
        ConnectionDetector cd = new ConnectionDetector(this);
        if (!cd.isConnectingToInternet()) {
            MessageBox.showToast(this,
                    getString(R.string.msg_no_internet_access));
            return;
        }
        try {
            ServiceRequest serviceRequest = new ServiceRequest(Config.CMD_BOOKING_UPDATE, ServiceRequest.bookingStatus(b_id, status, txt));
            UDocterServices.invoke(this, serviceRequest).execute();
            isLoading = true;
            showProgress();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void doRequest(){
        ConnectionDetector cd = new ConnectionDetector(this);
        if (!cd.isConnectingToInternet()) {
            MessageBox.showToast(this,
                    getString(R.string.msg_no_internet_access));
            return;
        }
        try {
            int t = 0;
            if (mySharedPreferences.getUSER_TYPE() == 2) {
                t = 1;
            }
            ServiceRequest serviceRequest = new ServiceRequest(Config.CMD_DOC_GET_ONE_BOOKING, ServiceRequest.getOneBooking(bk_id, t));
            UDocterServices.invoke(this, serviceRequest).execute();
            isLoading = true;
            //showProgress();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;

        GET_REASON();

        if (checkPermission()) {
            try {
                mGoogleMap.setMyLocationEnabled(true);
            }
            catch (Exception e){
            }
        }
        else{
            verifyMapsPermissions(this);
        }
        //mGoogleMap.setOnInfoWindowClickListener(this);
        mGoogleMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                isLoadMap = true;
                mySharedPreferences.setLongitude((float) location.getLongitude());
                mySharedPreferences.setLatitude((float) location.getLatitude());

                latlng = new LatLng(location.getLatitude(), location
                        .getLongitude());

                if (isLoadData){
                    if (!isLoadDuong)
                        DiectionsMaps();
                }
                if (firstLoad) {
                    CameraUpdate center = CameraUpdateFactory
                            .newLatLng(new LatLng(location
                                    .getLatitude(), location
                                    .getLongitude()));
                    CameraUpdate zoom = CameraUpdateFactory.zoomTo(12);
                    mGoogleMap.moveCamera(center);
                    mGoogleMap.animateCamera(zoom);
                    firstLoad = false;
                }
            }
        });
    }

    @Override
    public void onResponseReceived(int cmd, String resp) {
        isLoading = false;
        progress.dismiss();
        if (Utils.isNullOrEmpty(resp)) {
            return;
        }
        if (cmd == Config.CMD_BOOKING_UPDATE) {
            finish();
            startActivity(getIntent());
        }
        if (cmd == Config.CMD_DOC_GET_ONE_BOOKING) {
            final String mResp = resp;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    try {
                        JSONObject jObj = new JSONObject(mResp);
                        if ((jObj != null) && (jObj.getInt("ERR_CODE") == 0)) {
                            isLoadData = true;

                            JSONObject tmp = jObj.getJSONObject(Config.KEY_DATA);

                            booking_info = new BOOKING_INFO();
                            booking_info.setId(tmp.getInt(Config.BOOKING_INFO.id));
                            booking_info.setPatient_id(tmp.getInt(Config.BOOKING_INFO.patient_id));
                            booking_info.setPatient_name(tmp.getString(Config.BOOKING_INFO.patient_name));
                            booking_info.setPatient_phone(tmp.getString(Config.BOOKING_INFO.patient_phone));
                            booking_info.setDocter_id(tmp.getInt(Config.BOOKING_INFO.docter_id));
                            booking_info.setDocter_name(tmp.getString(Config.BOOKING_INFO.docter_name));
                            booking_info.setDocter_phone(tmp.getString(Config.BOOKING_INFO.docter_phone));
                            booking_info.setComment(tmp.getString(Config.BOOKING_INFO.comment));
                            booking_info.setCreated(tmp.getString(Config.BOOKING_INFO.created));
                            booking_info.setUpdated(tmp.getString(Config.BOOKING_INFO.updated));
                            booking_info.setStatus(tmp.getInt(Config.BOOKING_INFO.status));
                            booking_info.setTam_date(tmp.getInt(Config.BOOKING_INFO.tam_date));
                            booking_info.setVs_date(tmp.getInt(Config.BOOKING_INFO.vs_date));
                            booking_info.setLongitude(tmp.getDouble("longitude"));
                            booking_info.setLatitude(tmp.getDouble("latitude"));
                            booking_info.setRate(tmp.getDouble("rate"));

                            showView();

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
        if (cmd == Config.CMD_GET_REASON)
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
                                JSONArray jsArr = jObj.getJSONArray(Config.KEY_DATA);
                                listReason = new ArrayList<>();
                                for (int i = 0; i < jsArr.length(); i++) {
                                    JSONObject docInfo = jsArr.getJSONObject(i);
                                    listReason.add(docInfo.getString("Name"));
                                    doRequest();
                                }
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
        if (cmd == Config.CMD_RATE_BOOKING) {
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
                            btnSubmitrate.setVisibility(View.GONE);
                        } else {
                            MessageBox.showToast(getApplicationContext(), getString(R.string.no_data));
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

    private boolean checkPermissionCall() {
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)== PackageManager.PERMISSION_GRANTED );
    }
    public static void verifyMapsPermissionsCall(FragmentActivity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.CALL_PHONE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_CALL,
                    1
            );
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

    private void DiectionsMaps() {
        isLoadDuong = true;
        Log.i("DiectionsMaps", booking_info.getLatitude() + ": " + booking_info.getLongitude());
        Log.i("DiectionsMaps", mySharedPreferences.getLatitude() + ": " + mySharedPreferences.getLongitude());
        LatLng tolatlng = new LatLng(booking_info.getLatitude(),
                booking_info.getLongitude());

        addMarker(tolatlng);

        latlng = new LatLng(mySharedPreferences.getLatitude(), mySharedPreferences.getLongitude());

        Routing routing = new Routing.Builder()
                .travelMode(Routing.TravelMode.DRIVING)
                .withListener(BookingDetailActivity.this).waypoints(latlng, tolatlng)
                //.key(getString(R.string.google_maps_key))
                .build();
        routing.execute();
    }
    public void addMarker(LatLng latLng) {
        //atLng latLng = new LatLng(p.getLatitude(), p.getLongitude());
        BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.location_pin);
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).icon(icon);

        Marker marker = mGoogleMap.addMarker(markerOptions);
        mHashMap.put(marker, 0);
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
//        Criteria criteria = new Criteria();
//        String bestProvider = locationManager.getBestProvider(criteria, false);
//
//        if (checkPermission()) {
//            try {
//                Location location = locationManager.getLastKnownLocation(bestProvider);
//                CameraUpdate center = CameraUpdateFactory
//                        .newLatLng(new LatLng(location
//                                .getLatitude(), location
//                                .getLongitude()));
//                CameraUpdate zoom = CameraUpdateFactory.zoomTo(12);
//                mGoogleMap.moveCamera(center);
//                mGoogleMap.animateCamera(zoom);
//            }
//            catch (Exception e){
//            }
//        }
//        else{
//            verifyMapsPermissions(this);
//        }
//    }

    @Override
    public void onRoutingFailure(RouteException e) {

    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int j) {
        Log.i("onRoutingSuccess", "onRoutingSuccess: ");
        CameraUpdate center = CameraUpdateFactory.newLatLng(latlng);
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);

        mGoogleMap.moveCamera(center);
        mGoogleMap.animateCamera(zoom);

        if (polylines.size() > 0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<Polyline>();
        // add route(s) to the map.
        for (int i = 0; i < route.size(); i++) {

            // In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mGoogleMap.addPolyline(polyOptions);
            polylines.add(polyline);
            tvKhoangcach.setText(Utils.getDistance(route.get(i).getDistanceValue()));
        }

    }

    @Override
    public void onRoutingCancelled() {
        Log.i("onRoutingCancelled", "onRoutingCancelled: ");

    }
}
