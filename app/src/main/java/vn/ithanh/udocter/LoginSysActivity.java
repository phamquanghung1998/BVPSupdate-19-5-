package vn.ithanh.udocter;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import vn.ithanh.udocter.service.IServiceCallback;
import vn.ithanh.udocter.service.ServiceRequest;
import vn.ithanh.udocter.service.UDocterServices;
import vn.ithanh.udocter.util.Config;
import vn.ithanh.udocter.util.ConnectionDetector;
import vn.ithanh.udocter.util.MessageBox;
import vn.ithanh.udocter.util.MySharedPreferences;
import vn.ithanh.udocter.util.Utils;

/**
 * Created by ithanh on 2/20/18.
 */

public class LoginSysActivity extends AppCompatActivity implements IServiceCallback {

    private Button btn_login;
    private Button btn_link_signup;

    private EditText et_login_email;
    private EditText ev_login_password;


    private boolean isLoading = false;
    ProgressDialog progress;

    MySharedPreferences mySharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        this.mySharedPreferences = new MySharedPreferences(this);
        et_login_email = findViewById(R.id.et_login_email);
        ev_login_password = findViewById(R.id.ev_login_password);

        btn_login = findViewById(R.id.btn_login);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Login(et_login_email.getText().toString(), ev_login_password.getText().toString());

            }
        });


        btn_link_signup = findViewById(R.id.btn_link_signup);
        btn_link_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mainIntent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(mainIntent);
            }
        });

    }

    public void openMain() {
        if (mySharedPreferences.getUSER_ID() > 0) {
            // La benh nha
            if (mySharedPreferences.getUSER_TYPE() == 2) {
                Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(mainIntent);
                return;
            }
            // La Bac Sy
            if (mySharedPreferences.getUSER_TYPE() == 1) {
                Intent docterIntent = new Intent(getApplicationContext(), DocterActivity.class);
                startActivity(docterIntent);
                return;
            }
        }
    }

    public void showDialog() {
        final CharSequence[] items = { "Bác sĩ", "Bệnh nhân" };
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginSysActivity.this);
        builder.setTitle("Bạn là ?");
        builder.setSingleChoiceItems(items, -1,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        UPDATE(mySharedPreferences.getUSER_ID(), item+1);
                        dialog.dismiss();
                    }
                });
        builder.show();
    }

    public void Login(String user, String pass){
        ConnectionDetector cd = new ConnectionDetector(this);
        if (!cd.isConnectingToInternet()) {
            MessageBox.showToast(this,
                    getString(R.string.msg_no_internet_access));
            return;
        }
        try {
            ServiceRequest serviceRequest = new ServiceRequest(Config.CMD_LOGIN, ServiceRequest.Login(user, pass));
            UDocterServices.invoke(this, serviceRequest).execute();
            isLoading = true;
            showProgress();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void UPDATE(int id, int t){
        ConnectionDetector cd = new ConnectionDetector(this);
        if (!cd.isConnectingToInternet()) {
            MessageBox.showToast(this,
                    getString(R.string.msg_no_internet_access));
            return;
        }
        try {
            ServiceRequest serviceRequest = new ServiceRequest(Config.CMD_LOGIN, ServiceRequest.UpUType(id, t));
            UDocterServices.invoke(this, serviceRequest).execute();
            isLoading = true;
            showProgress();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showProgress(){
        progress = ProgressDialog.show(LoginSysActivity.this, "", "Loading...", true);
    }

    @Override
    public void onResponseReceived(int cmd, String resp) {
        isLoading = false;
        progress.dismiss();
        if (Utils.isNullOrEmpty(resp)) {
            return;
        }
        if (cmd == Config.CMD_LOGIN)
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
                                JSONObject userInfo = jObj.getJSONObject(Config.KEY_DATA);
                                if (userInfo.getInt(Config.USER_UD.KEY_user_type) == 0){
                                    mySharedPreferences.setUSER_ID(userInfo.getInt(Config.USER_UD.KEY_ID));
                                    showDialog();
                                }
                                else {
                                    mySharedPreferences.setUSER_ID(userInfo.getInt(Config.USER_UD.KEY_ID));
                                    mySharedPreferences.setFULL_NAME(userInfo.getString(Config.USER_UD.KEY_full_name));
                                    mySharedPreferences.setEMAIL(userInfo.getString(Config.USER_UD.KEY_username));
                                    mySharedPreferences.setPHONE(userInfo.getString(Config.USER_UD.KEY_phone));
                                    mySharedPreferences.setUSER_TYPE(userInfo.getInt(Config.USER_UD.KEY_user_type));
                                    mySharedPreferences.setLongitude(userInfo.getLong("longitude"));
                                    mySharedPreferences.setLatitude(userInfo.getLong("latitude"));
                                    if (mySharedPreferences.getUSER_TYPE() == 1)
                                        mySharedPreferences.setD_ID(userInfo.getInt(Config.USER_UD.KEY_d_id));
                                    if (mySharedPreferences.getUSER_TYPE() == 2)
                                        mySharedPreferences.setP_ID(userInfo.getInt(Config.USER_UD.KEY_p_id));
                                    openMain();
                                }
                            }else{
                                MessageBox.showToast(getApplicationContext(), jObj.getString("Message"));
                            }
                        } else {
                            MessageBox.showToast(getApplicationContext(), getString(R.string.no_data));
                        }

                    } catch (JSONException e) {
                        //viewState(Config.ViewState.Empty);
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
