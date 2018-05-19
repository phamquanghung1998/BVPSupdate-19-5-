package vn.ithanh.udocter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.FacebookDialog;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.ui.PlacePicker;
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
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import vn.ithanh.udocter.model.DICTRCT_INFO;
import vn.ithanh.udocter.model.DOCTER_INFO;
import vn.ithanh.udocter.service.IServiceCallback;
import vn.ithanh.udocter.service.ServiceRequest;
import vn.ithanh.udocter.service.UDocterServices;
import vn.ithanh.udocter.util.Config;
import vn.ithanh.udocter.util.ConnectionDetector;
import vn.ithanh.udocter.util.MessageBox;
import vn.ithanh.udocter.util.MySharedPreferences;
import vn.ithanh.udocter.util.Utils;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener, IServiceCallback {


    static {
        System.loadLibrary("native-lib");
    }


    MySharedPreferences mySharedPreferences;
    private static final int REQUEST_ACCESS_FINE_LOCATION = 1;
    private static String[] PERMISSIONS_MAPS = {
            Manifest.permission.ACCESS_FINE_LOCATION};
    GoogleMap mGoogleMap;
    LatLng latlng;
    private HashMap<Marker, Integer> mHashMap = new HashMap<Marker, Integer>();
    //Data
    private ArrayList<DOCTER_INFO> dt = new ArrayList<DOCTER_INFO>();
    private ArrayList<DICTRCT_INFO> dtDictrct = new ArrayList<DICTRCT_INFO>();

    boolean firstLoad = true;

    private boolean isLoading = false;
    ProgressDialog progress;

    private Spinner dropdownDictrct;

    private ImageView imageViewNav;

    private ImageLoader uilImageLoader;
    private DisplayImageOptions options;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FacebookSdk.sdkInitialize(getApplicationContext());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        this.mySharedPreferences = new MySharedPreferences(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.main_map);
        mapFragment.getMapAsync(this);

        View headerView = navigationView.getHeaderView(0);

        TextView tvRs_name = headerView.findViewById(R.id.textView_name);
        TextView tvRs_address =  headerView.findViewById(R.id.textView_email);

        uilImageLoader = MyApplication.uilImageLoader;
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.icon_docter)
                .showImageForEmptyUri(R.drawable.icon_docter)
                .showImageOnFail(R.drawable.icon_docter)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .displayer(new RoundedBitmapDisplayer(Config.CORNER_RADIUS_PIXELS))
                .bitmapConfig(Bitmap.Config.RGB_565).build();

        imageViewNav = headerView.findViewById(R.id.imageViewNav);

        if (mySharedPreferences.getFBID() != null && mySharedPreferences.getFBID().length() > 0)
            uilImageLoader.displayImage(Utils.getAvatarfacebook(mySharedPreferences.getFBID()), imageViewNav, options);
//        else{
//            String avatar = Config.URL_IMAGE + mySharedPreferences.getDEFAULT_AVATAR();
//            uilImageLoader.displayImage(avatar, imageViewNav, options);
//
//        }
//        imageViewNav.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                Intent mainIntent = new Intent(getApplicationContext(), EditProfileActivity.class);
////                startActivity(mainIntent);
//            }
//        });

        tvRs_name.setText(mySharedPreferences.getFULL_NAME());
        tvRs_address.setText(mySharedPreferences.gettEMAIL());
        //get the spinner from the xml.
        dropdownDictrct = findViewById(R.id.spinnerdictrct);
        dropdownDictrct.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d("onItemSelected", "onItemSelected: " + i);
                if (i > 0){
                    DICTRCT_INFO dictrct_info = dtDictrct.get(i-1);
                    if (dictrct_info.getLatitude() > 0 && dictrct_info.getLongitude() >0 ){
                        setCameraMap(dictrct_info.getLongitude(), dictrct_info.getLatitude());
                    }
                }else{
                    setCameraMap(mySharedPreferences.getLatitude(), mySharedPreferences.getLongitude());
                }
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });
        doRequest();
    }

    public void showProgress(){
        progress = ProgressDialog.show(MainActivity.this, "", "Loading...", true);
    }

    public void setUpDictrct(){
        //Log.i("setUpDictrct", "setUpDictrct: " + dtDictrct.size());
        List<String> list = new ArrayList<String>();
        list.add("Vị trí của bạn");
        for (int i = 0; i < dtDictrct.size(); i++){
            list.add(dtDictrct.get(i).getName());
        }
        String[] stringArray = list.toArray(new String[0]);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_dropdown_item, stringArray);
        dropdownDictrct.setAdapter(adapter);
    }

    public void doRequest(){
        ConnectionDetector cd = new ConnectionDetector(this);
        if (!cd.isConnectingToInternet()) {
            MessageBox.showToast(this, getString(R.string.msg_no_internet_access));
            return;
        }
        if (isLoading)
            return;
        try {
            ServiceRequest serviceRequest = new ServiceRequest(Config.CMD_GET_ALL_DOCTER, ServiceRequest.getAllDocter());
            UDocterServices.invoke(this, serviceRequest).execute();
            isLoading = true;
            showProgress();
            //viewState(Config.ViewState.Loading);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void GET_DICTRCT(){
        ConnectionDetector cd = new ConnectionDetector(this);
        if (!cd.isConnectingToInternet()) {
            MessageBox.showToast(this, getString(R.string.msg_no_internet_access));
            return;
        }
        try {
            ServiceRequest serviceRequest = new ServiceRequest(Config.CMD_GET_DICTRCT, ServiceRequest.getDictrct());
            UDocterServices.invoke(this, serviceRequest).execute();
            isLoading = true;
            showProgress();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_booking) {
            // Handle the nav_booking action
            Intent signupIntent = new Intent(getApplicationContext(), PatientbookingActivity.class);
            startActivity(signupIntent);

        } else if (id == R.id.nav_info) {
            Intent signupIntent = new Intent(getApplicationContext(), PatientInfoActivity.class);
            startActivity(signupIntent);

        } else if (id == R.id.nav_share) {

            ShareDialog shareDialog = new ShareDialog(this);

            ShareLinkContent linkContent = new ShareLinkContent.Builder()
                    .setContentUrl(Uri.parse("market://details?id=vn.ithanh.udocter"))
                    .build();
            shareDialog.show(linkContent);
        } else if (id == R.id.nav_logout) {
            mySharedPreferences.setUSER_ID(0);

            if (AccessToken.getCurrentAccessToken() != null) {
                LoginManager.getInstance().logOut();
            }
            openLogin();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void openLogin() {
        Intent signupIntent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(signupIntent);
    }

    public void addDocterToMaps(DOCTER_INFO p, int i) {
        LatLng latLng = new LatLng(p.getLatitude(), p.getLongitude());
        BitmapDescriptor icon;
        if (p.getSex_id() == 1)
            icon = BitmapDescriptorFactory.fromResource(R.drawable.doctor_male_avt);
        else
            icon = BitmapDescriptorFactory.fromResource(R.drawable.doctor_female_avt);
        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title(p.getName()).snippet(p.getHospital() + " - " +p.getStar() + " ★").icon(icon);

        Marker marker = mGoogleMap.addMarker(markerOptions);
        mHashMap.put(marker, i);
    }

    public void setCameraMap(double lat, double lng){
        CameraUpdate center = CameraUpdateFactory
                .newLatLng(new LatLng(lat, lng));
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(14);
        mGoogleMap.moveCamera(center);
        mGoogleMap.animateCamera(zoom);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;

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
        mGoogleMap.setOnInfoWindowClickListener(this);
        mGoogleMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                    @Override
                    public void onMyLocationChange(Location location) {

                        mySharedPreferences.setLongitude((float) location.getLongitude());
                        mySharedPreferences.setLatitude((float) location.getLatitude());

                        //Log.i("onMyLocationChange", "onMyLocationChange: " + location.getLongitude());

                        latlng = new LatLng(location.getLatitude(), location
                                .getLongitude());
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
    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            CameraUpdate center = CameraUpdateFactory
                    .newLatLng(new LatLng(location
                            .getLatitude(), location
                            .getLongitude()));
            CameraUpdate zoom = CameraUpdateFactory.zoomTo(12);
            mGoogleMap.moveCamera(center);
            mGoogleMap.animateCamera(zoom);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, false);

        if (checkPermission()) {
            try {
                //locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
            }
            catch (Exception e){
            }
        }
        else{
            verifyMapsPermissions(this);
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        int pos = mHashMap.get(marker);
        Log.i("POS", pos + "");
        DOCTER_INFO docter_info = dt.get(pos);

        Intent intent = new Intent(this, DocterDetailActivity.class);
        intent.putExtra(DocterDetailActivity.EXTRA_DOCTER_ID, docter_info.getId() +"");
        this.startActivity(intent);

    }

    @Override
    public void onResponseReceived(int cmd, String resp) {
        //Log.i("resp", resp);
        isLoading = false;
        progress.dismiss();
        if (Utils.isNullOrEmpty(resp)) {
            return;
        }
        if (cmd == Config.CMD_GET_ALL_DOCTER)
        {
            //Log.i("CMD_GET_ALL_DOCTER", "CMD_GET_ALL_DOCTER: ");
            GET_DICTRCT();
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
                                for (int i = 0; i < jsArr.length(); i++) {
                                    JSONObject docInfo = jsArr.getJSONObject(i);
                                    DOCTER_INFO docter_info = new DOCTER_INFO();
                                    docter_info.setId(docInfo.getInt("id"));
                                    docter_info.setUser_id(docInfo.getInt("user_id"));
                                    docter_info.setName(docInfo.getString("name"));
                                    docter_info.setBod(docInfo.getString("dob"));
                                    docter_info.setSex_id(docInfo.getInt("sex_id"));
                                    docter_info.setInfo_p(docInfo.getString("info_p"));
                                    docter_info.setHospital(docInfo.getString("hospital"));
                                    docter_info.setYear(docInfo.getInt("year"));
                                    docter_info.setStar(docInfo.getInt("star"));
                                    docter_info.setAddress(docInfo.getString("address"));
                                    docter_info.setLatitude(docInfo.getDouble("latitude"));
                                    docter_info.setLongitude(docInfo.getDouble("longitude"));
                                    dt.add(docter_info);
                                    addDocterToMaps(docter_info, i);

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
        if (cmd == Config.CMD_GET_DICTRCT)
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
                                for (int i = 0; i < jsArr.length(); i++) {
                                    JSONObject docInfo = jsArr.getJSONObject(i);
                                    DICTRCT_INFO dictrct_info = new DICTRCT_INFO();
                                    dictrct_info.setId(docInfo.getInt("id"));
                                    dictrct_info.setName(docInfo.getString("name"));
                                    dictrct_info.setLatitude(docInfo.getDouble("latitude"));
                                    dictrct_info.setLongitude(docInfo.getDouble("longitude"));
                                    dtDictrct.add(dictrct_info);
                                    setUpDictrct();
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
    }

    private boolean doubleBackToExitPressedOnce;
    private Handler mHandler = new Handler();

    private final Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            doubleBackToExitPressedOnce = false;
        }
    };

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        if (mHandler != null) { mHandler.removeCallbacks(mRunnable); }
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            moveTaskToBack(true);
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        mHandler.postDelayed(mRunnable, 2000);
    }

}
