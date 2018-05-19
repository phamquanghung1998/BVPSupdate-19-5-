package vn.ithanh.udocter;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.login.LoginManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.acl.Group;
import java.util.ArrayList;
import java.util.List;

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
 * Created by iThanh on 12/4/2017.
 */

public class DocterActivity extends AppCompatActivity implements IServiceCallback {

    private ProgressBar pb;

    Adapter adapter;
    private TabLayout tabLayout;
    private ViewPager mViewPager;
    private Toolbar toolbar;

    /* Data */
    private ArrayList<BOOKING_INFO> dt = new ArrayList<BOOKING_INFO>();
    private boolean isLoading = false;

    private String[] ListTabs = {"Chờ xác nhận", "Đã xác nhận", "Kết thúc", "Khách hủy", "Bạn hủy"};

    MySharedPreferences mySharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.docter_booking_layout);

        this.mySharedPreferences = new MySharedPreferences(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_docter);
        setSupportActionBar(toolbar);

        setTitle(getString(R.string.docter_activity_title));


        mViewPager = (ViewPager) findViewById(R.id.container);

        adapter = new Adapter(getSupportFragmentManager());
        mViewPager.setAdapter(adapter);


        tabLayout = findViewById(R.id.tabs);

        tabLayout.setTabGravity(TabLayout.GRAVITY_CENTER);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);

        for (int i = 0; i < ListTabs.length; i++) {
            TabLayout.Tab tab = tabLayout.newTab();
            tab.setTag(i);
            tab.setText(ListTabs[i]);
            tabLayout.addTab(tab);
        }
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//            }
//        });

        pb = findViewById(R.id.list_loading);

        doRequest();
    }

//    public void doChangeStatus(int b_id, int status){
//        ConnectionDetector cd = new ConnectionDetector(this);
//        if (!cd.isConnectingToInternet()) {
//            MessageBox.showToast(this,
//                    getString(R.string.msg_no_internet_access));
//            return;
//        }
//        try {
//            ServiceRequest serviceRequest = new ServiceRequest(Config.CMD_BOOKING_UPDATE, ServiceRequest.bookingStatus(b_id, status, ""));
//            UDocterServices.invoke(this, serviceRequest).execute();
//            isLoading = true;
//            viewState(Config.ViewState.Loading);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }

    public void doRequest(){
        ConnectionDetector cd = new ConnectionDetector(this);
        if (!cd.isConnectingToInternet()) {
            MessageBox.showToast(this,
                    getString(R.string.msg_no_internet_access));
            return;
        }
        try {
            ServiceRequest serviceRequest = new ServiceRequest(Config.CMD_DOC_GET_BOOKING, ServiceRequest.getBooking(mySharedPreferences.getD_ID(), "d_id"));
            UDocterServices.invoke(this, serviceRequest).execute();
            isLoading = true;
            viewState(Config.ViewState.Loading);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_docter, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings_docter) {
            Intent docterIntent = new Intent(getApplicationContext(), DocterInfoActivity.class);
            startActivity(docterIntent);
            return true;
        }

        if (id == R.id.action_docter_logout) {
            mySharedPreferences.setUSER_ID(0);

            if (AccessToken.getCurrentAccessToken() != null) {
                LoginManager.getInstance().logOut();
            }
            Intent signupIntent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(signupIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    private void viewState(Config.ViewState state) {
        // Init
        if (Config.ViewState.Init.equals(state)) {
            pb.setVisibility(View.VISIBLE);
            return;
        }

        if (Config.ViewState.Loading.equals(state)) {
            pb.setVisibility(View.VISIBLE);
            return;
        }
        if (Config.ViewState.Loaded.equals(state)) {
            pb.setVisibility(View.INVISIBLE);
            return;
        }
        if (Config.ViewState.Empty.equals(state)) {
            pb.setVisibility(View.INVISIBLE);
            return;
        }
    }

    @Override
    public void onResponseReceived(int cmd, String resp) {
        viewState(Config.ViewState.Loaded);
        if (Utils.isNullOrEmpty(resp)) {
            isLoading = false;
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
                            dt = new ArrayList<>();
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
                                dt.add(c);
                            }
                            setupViewPager();
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

    private void setupViewPager() {
        for (int i = 0; i < ListTabs.length; i++) {
            //Group group = dt.get(i);

            ArrayList<BOOKING_INFO> newlist =  new ArrayList<BOOKING_INFO>();
            for (int j = 0;j < dt.size(); j++) {
                BOOKING_INFO bk = dt.get(j);
                if (bk.getStatus() == i)
                    newlist.add(bk);
            }
            BookingListFragment fragment = new BookingListFragment();
            fragment.setItems(newlist);
            adapter.addFragment(fragment, ListTabs[i]);
        }
        adapter.notifyDataSetChanged();
        //mViewPager.setCurrentItem(GlobalVariable.group_p);
    }

    static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();

        public Adapter(FragmentManager fm) {
            super(fm);
        }

        public void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
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
