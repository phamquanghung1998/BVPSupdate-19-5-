package vn.ithanh.udocter;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import vn.ithanh.udocter.model.DOCTER_INFO;
import vn.ithanh.udocter.model.SERVICES_UD;
import vn.ithanh.udocter.service.IServiceCallback;
import vn.ithanh.udocter.service.ServiceRequest;
import vn.ithanh.udocter.service.UDocterServices;
import vn.ithanh.udocter.util.Config;
import vn.ithanh.udocter.util.ConnectionDetector;
import vn.ithanh.udocter.util.MessageBox;
import vn.ithanh.udocter.util.MySharedPreferences;
import vn.ithanh.udocter.util.Utils;

/**
 * Created by iThanh on 12/6/2017.
 */

public class DocterDetailActivity extends AppCompatActivity implements IServiceCallback {

    public static final String EXTRA_DOCTER_ID = "EXTRA_DOCTER_ID";
    //Adapter adapter;
    CollapsingToolbarLayout collapsingToolbar;

    private int docter_id;

    //private ArrayList<ItemDetail> dt = new ArrayList<ItemDetail>();

    private TextView mTextView;
    private TextView mTextViewDes;

    private ImageLoader uilImageLoader;
    private DisplayImageOptions options;
    private ImageView imageView;
    private ImageView ivAvatar_pic;
    private RecyclerView rv;

    private View mDocFormView;
    private View mProgressView;

    private RadioGroup rgTam;
    private RadioGroup rgVeSinh;

    private boolean isLoading = false;


    private Button btn_booking;

    private DOCTER_INFO Mdocter;

    MySharedPreferences mySharedPreferences;

    private boolean checkPhone = false;

    private ArrayList<SERVICES_UD> dt = new ArrayList<SERVICES_UD>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.docter_detail_layout);

        this.mySharedPreferences = new MySharedPreferences(this);

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle(" ");

        mTextView = findViewById(R.id.tvinfo_name);
        mTextViewDes = findViewById(R.id.tv_docter_info);
        rgTam = findViewById(R.id.tam_radio_group);
        rgVeSinh = findViewById(R.id.vs_radio_group);


        imageView = findViewById(R.id.backdrop);
        ivAvatar_pic = findViewById(R.id.ivAvatar_pic);

        collapsingToolbar = findViewById(R.id.collapsing_toolbar);
        Intent intent = getIntent();
        docter_id = Integer.parseInt(intent.getStringExtra(EXTRA_DOCTER_ID));

        //rv = findViewById(R.id.lvService);

        uilImageLoader = MyApplication.uilImageLoader;
        options = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.icon_docter)
                .showImageForEmptyUri(R.drawable.icon_docter)
                .showImageOnFail(R.drawable.icon_docter)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .considerExifParams(true)
                .displayer(
                        new RoundedBitmapDisplayer(Config.CORNER_RADIUS_PIXELS))
                .bitmapConfig(Bitmap.Config.RGB_565).build();

        mProgressView = findViewById(R.id.info_detail_progress);
        //mDocFormView = findViewById(R.id.frm_doc_detail);

        //Log.i("getTinhtien :", getTinhtien());

        btn_booking = findViewById(R.id.btn_booking);


        String dienthoai = mySharedPreferences.getPHONE();
        if (dienthoai.isEmpty() || dienthoai.equals(null) || dienthoai.equals("null"))
            dienthoai = "";

        final String finalDienthoai = dienthoai;
        btn_booking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(DocterDetailActivity.this);
                LayoutInflater inflater = DocterDetailActivity.this.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.dialog_confirm_booking, null);
                dialogBuilder.setView(dialogView);

                //settitle
                final TextView tvTitle = dialogView.findViewById(R.id.tvTitle);
                String title = "NHÂN VIÊN : " + Mdocter.getName();
                tvTitle.setText(title.toUpperCase());

                // Set price
                TextView tvBookPrice = dialogView.findViewById(R.id.tvBookPrice);
                //String price = "Thành tiền" + Mdocter.getName();
                tvBookPrice.setText(getTinhtien().toUpperCase());

                final EditText input = dialogView.findViewById(R.id.etPhone);
                input.setHint("Số điện thoại");
                input.setText(finalDienthoai);

                final EditText txtDate = dialogView.findViewById(R.id.etdate);
                txtDate.setHint("Chọn ngày");

                final Calendar myCalendar = Calendar.getInstance();
                final DatePickerDialog.OnDateSetListener date;


                SimpleDateFormat dft=null;
                dft=new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                String strDate=dft.format(myCalendar.getTime());
                //hiển thị lên giao diện
                txtDate.setText(strDate);

                date = new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear,
                                          int dayOfMonth) {
                        // TODO Auto-generated method stub
                        myCalendar.set(Calendar.YEAR, year);
                        myCalendar.set(Calendar.MONTH, monthOfYear);
                        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        String myFormat = "dd/MM/yyyy"; //In which you need put here
                        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.FRANCE);
                        txtDate.setText(sdf.format(myCalendar.getTime()));
                    }

                };
                txtDate.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        //View view = this.getCurrentFocus();
                        if (v != null) {
                            InputMethodManager imm = (InputMethodManager)getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        }
                        String s=txtDate.getText()+"";
                        String strArrtmp[]=s.split("/");
                        int ngay=Integer.parseInt(strArrtmp[0]);
                        int thang=Integer.parseInt(strArrtmp[1])-1;
                        int nam=Integer.parseInt(strArrtmp[2]);
                        DatePickerDialog pic=new DatePickerDialog(
                                DocterDetailActivity.this,
                                date, nam, thang, ngay);
                        pic.setTitle("Chọn ngày");
                        pic.show();
                    }
                });


                final EditText txtTime = dialogView.findViewById(R.id.etTime);
                txtTime.setHint("Chọn giờ");
                dft=new SimpleDateFormat("HH:mm",Locale.getDefault());
                String strTime=dft.format(myCalendar.getTime());
                txtTime.setText(strTime);

                final TimePickerDialog.OnTimeSetListener callback=new TimePickerDialog.OnTimeSetListener() {
                    public void onTimeSet(TimePicker view,int hourOfDay, int minute) {
                        String s= hourOfDay +":"+minute;
                        txtTime.setText(s);
                    }
                };
                txtTime.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        //View view = this.getCurrentFocus();
                        if (v != null) {
                            InputMethodManager imm = (InputMethodManager)getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        }
                        String s=txtTime.getText()+"";
                        String strArr[]=s.split(":");
                        int gio=Integer.parseInt(strArr[0]);
                        int phut=Integer.parseInt(strArr[1]);
                        TimePickerDialog time=new TimePickerDialog(
                                DocterDetailActivity.this,
                                callback, gio, phut, false);
                        time.setTitle("Chọn giờ");
                        time.show();
                    }
                });


                final AlertDialog alertDialog = dialogBuilder.create();
                Button btnok = dialogView.findViewById(R.id.button_positive);
                btnok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (input.getText().toString().length() > 0 && Utils.checkVNMobileNumber(input.getText().toString())) {
                            mySharedPreferences.setPHONE(input.getText().toString());
                            saveBooking(input.getText().toString(), txtTime.getText().toString() + " " + txtDate.getText().toString());
                        } else {
                            Toast.makeText(getApplicationContext(), "Bạn vui lòng nhập số điện thoại.", Toast.LENGTH_SHORT).show();
                        }
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
        doRequest();
    }
    public String getTinhtien() {
        int tam = 0;
        int vs = 0;

        if (rgTam.getCheckedRadioButtonId() != -1) {
            RadioButton rdoTam = findViewById(rgTam.getCheckedRadioButtonId());
            tam = Integer.parseInt((String) rdoTam.getTag());
        }
        if (rgVeSinh.getCheckedRadioButtonId() != -1) {
            RadioButton rdoVs = findViewById(rgVeSinh.getCheckedRadioButtonId());
            vs = Integer.parseInt((String) rdoVs.getTag());
        }
        int tongtien = (tam + vs) * 300000;
        String str = "Thành tiền : " + Utils.priceWithout(tongtien);

        return str;

    }


    public void saveBooking(String sdt, String text) {
        int p_id = mySharedPreferences.getP_ID();
        //int d_id = Mdocter.getId();

        ConnectionDetector cd = new ConnectionDetector(this);
        if (!cd.isConnectingToInternet()) {
            MessageBox.showToast(this,
                    getString(R.string.msg_no_internet_access));
            return;
        }
        try {

            int tam = 0;
            int vs = 0;
            if (rgTam.getCheckedRadioButtonId() != -1) {
                RadioButton rdoTam = findViewById(rgTam.getCheckedRadioButtonId());
                tam = Integer.parseInt((String) rdoTam.getTag());
            }
            if (rgVeSinh.getCheckedRadioButtonId() != -1) {
                RadioButton rdoVs = findViewById(rgVeSinh.getCheckedRadioButtonId());
                vs = Integer.parseInt((String) rdoVs.getTag());
            }


            ServiceRequest serviceRequest = new ServiceRequest(Config.CMD_P_BOOKING, ServiceRequest.Booking(p_id, docter_id, sdt, text, tam, vs, mySharedPreferences.getLongitude(), mySharedPreferences.getLatitude()));
            UDocterServices.invoke(this, serviceRequest).execute();
            isLoading = true;
            viewState(Config.ViewState.Loading);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void viewState(Config.ViewState state) {
        if (Config.ViewState.Loading.equals(state)) {
            //mDocFormView.setVisibility(View.GONE);
            mProgressView.setVisibility(View.VISIBLE);
            return;
        }
        // Loaded
        if (Config.ViewState.Loaded.equals(state)) {
            //mDocFormView.setVisibility(View.VISIBLE);
            mProgressView.setVisibility(View.GONE);
            return;
        }
    }

    private void doRequest() {
        ConnectionDetector cd = new ConnectionDetector(this);
        if (!cd.isConnectingToInternet()) {
            MessageBox.showToast(this,
                    getString(R.string.msg_no_internet_access));
            return;
        }
        try {
            ServiceRequest serviceRequest = new ServiceRequest(Config.CMD_GET_DOCTER_DETAIL, ServiceRequest.getDocterDetail(docter_id));
            UDocterServices.invoke(this, serviceRequest).execute();
            isLoading = true;
            viewState(Config.ViewState.Loading);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setView(DOCTER_INFO docter_info) {

        Mdocter = docter_info;
        collapsingToolbar.setTitle("");
        mTextView.setText(docter_info.getName());
        mTextViewDes.setText(docter_info.getInfo_p());
        if (docter_info.getAvartar() != null && docter_info.getAvartar().length() > 5)
            uilImageLoader.displayImage(Utils.getAvatarfacebook(docter_info.getAvartar()), ivAvatar_pic, options);
        else{
            String avatar = Config.URL_IMAGE + mySharedPreferences.getDEFAULT_AVATAR();
            uilImageLoader.displayImage(avatar, ivAvatar_pic, options);
        }

        //setupRecyclerView(rv);
    }

//    private void setupRecyclerView(RecyclerView recyclerView) {
//        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
//        recyclerView.setAdapter(new ServiceRecyclerViewAdapter(this, dt));
//    }

    @Override
    public void onResponseReceived(int cmd, String resp) {
        Log.i("resp", resp);
        viewState(Config.ViewState.Loaded);
        if (Utils.isNullOrEmpty(resp)) {
            //isLoading = false;
            //viewState(Config.ViewState.Loaded);
            return;
        }
        if (cmd == Config.CMD_P_BOOKING) {
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
                            if ((jObj.getInt("ERR_CODE") == 0)) {
                                onBackPressed();
                                Intent bookingIntent = new Intent(getApplicationContext(), BookingDetailActivity.class);
                                bookingIntent.putExtra(BookingDetailActivity.EXTRA_BOOKING_ID, jObj.getInt("USER_TYPE") + "");
                                getApplicationContext().startActivity(bookingIntent);

                            }


                        } else {
                            MessageBox.showToast(getApplicationContext(), getString(R.string.no_data));
                        }

                    } catch (JSONException e) {
                        viewState(Config.ViewState.Empty);
                        MessageBox.showToast(getApplicationContext(),
                                getString(R.string.msg_request_fail_retry));
                    }
                    isLoading = false;
                }

            });
            return;

        }
        if (cmd == Config.CMD_GET_DOCTER_DETAIL) {
            final String mResp = resp;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    try {
                        JSONObject jObj = new JSONObject(mResp);
                        if ((jObj != null) && (jObj.getInt("ERR_CODE") == 0)) {
                            JSONObject docInfo = jObj.getJSONObject(Config.KEY_DATA);

                            DOCTER_INFO docter_info = new DOCTER_INFO();
                            docter_info.setUser_id(docInfo.getInt("user_id"));
                            docter_info.setName(docInfo.getString("name"));
                            docter_info.setBod(docInfo.getString("dob"));
                            docter_info.setSex_id(docInfo.getInt("sex_id"));
                            docter_info.setInfo_p(docInfo.getString("info_p"));
                            docter_info.setHospital(docInfo.getString("hospital"));
                            docter_info.setYear(docInfo.getInt("year"));
                            docter_info.setAddress(docInfo.getString("address"));
                            docter_info.setLatitude(docInfo.getDouble("latitude"));
                            docter_info.setLongitude(docInfo.getDouble("longitude"));
                            docter_info.setAvartar(docInfo.getString("avartar"));

                            // get service
                            JSONArray jsArr = docInfo.getJSONArray("SERVICE");
                            for (int i = 0; i < jsArr.length(); i++) {
                                JSONObject service = jsArr.getJSONObject(i);
                                SERVICES_UD servicesUd = new SERVICES_UD();
                                servicesUd.setDocter_id(docter_id);
                                servicesUd.setService_id(service.getInt("id"));
                                servicesUd.setPrice(service.getInt("price"));
                                servicesUd.setName(service.getString("name"));

                                dt.add(servicesUd);

                            }

                            Log.i("DTSIZE", dt.size() + "");
                            setView(docter_info);

                        } else {
                            MessageBox.showToast(getApplicationContext(),
                                    getString(R.string.no_data));
                        }

                    } catch (JSONException e) {
                        //viewState(Config.ViewState.Empty);
                        MessageBox.showToast(getApplicationContext(),
                                getString(R.string.msg_request_fail_retry));
                    }
                    //isLoading = false;
                }

            });
            return;
        }

    }
}
