package vn.ithanh.udocter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import vn.ithanh.udocter.model.USER_INFO;
import vn.ithanh.udocter.service.IServiceCallback;
import vn.ithanh.udocter.service.ServiceRequest;
import vn.ithanh.udocter.service.UDocterServices;
import vn.ithanh.udocter.util.Config;
import vn.ithanh.udocter.util.Config.ViewState;
import vn.ithanh.udocter.util.ConnectionDetector;
import vn.ithanh.udocter.util.MessageBox;
import vn.ithanh.udocter.util.MySharedPreferences;
import vn.ithanh.udocter.util.Utils;

/**
 * Created by iThanh on 12/3/2017.
 */

public class RegisterActivity extends AppCompatActivity implements IServiceCallback {

    private AutoCompleteTextView mEmailView;
    private EditText etName;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etAddress;
    private EditText etPhone;
    private RadioGroup rgGender;
    private RadioGroup rgUserType;

    //private View mSingUpFormView;
    private View mProgressView;

    private boolean isLoading = false;

    MySharedPreferences mySharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);

        this.mySharedPreferences = new MySharedPreferences(this);

        // get control
        etName = findViewById(R.id.signup_input_name);
        etEmail = findViewById(R.id.signup_input_email);
        etPassword = findViewById(R.id.signup_input_password);
        etAddress = findViewById(R.id.signup_input_address);
        etPhone = findViewById(R.id.signup_input_phone);
//
        rgGender = findViewById(R.id.gender_radio_group);
        rgUserType = findViewById(R.id.usertype_radio_group);

        //mSingUpFormView = findViewById(R.id.signup_form);
        mProgressView = findViewById(R.id.register_progress);


        //set default address
        etAddress.setText(mySharedPreferences.getDEFAULT_ADDRESS());

//        Button btn_link_login = (Button) findViewById(R.id.btn_link_login);
//        btn_link_login.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                openLogin();
//            }
//        });

        Button btn_signup = (Button) findViewById(R.id.btn_signup);
        btn_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
    }
    private void openLogin()
    {
        Intent signupIntent = new Intent(getApplicationContext(), LoginSysActivity.class);
        startActivity(signupIntent);
    }

    private void attemptLogin(){
        ConnectionDetector cd = new ConnectionDetector(this);
        if (!cd.isConnectingToInternet()) {
            MessageBox.showToast(this, getString(R.string.msg_no_internet_access));
            return;
        }
        if (isLoading)
            return;

        USER_INFO user_info = new USER_INFO();
        user_info.setFull_name(etName.getText().toString());
        user_info.setUsername(etEmail.getText().toString());
        user_info.setEmail(etEmail.getText().toString());
        user_info.setPassword(etPassword.getText().toString());
        user_info.setAddress(etAddress.getText().toString());
        user_info.setPhone(etPhone.getText().toString());

        RadioButton rbgender = findViewById(rgGender.getCheckedRadioButtonId());
        user_info.setGender(0);
        if (rbgender.getText().toString().equals(getString(R.string.male)))
        {
            user_info.setGender(1);
        }

        RadioButton rbUsertype = findViewById(rgUserType.getCheckedRadioButtonId());
        user_info.setUser_type(2);
        if (rbUsertype.getText().toString().equals(getString(R.string.hint_docter)))
        {
            user_info.setUser_type(1);
        }

        // Check
        View focusView = null;

        if (TextUtils.isEmpty(user_info.getFull_name())){
            MessageBox.showToast(getApplicationContext(), this.getString(R.string.name_notnull));
            return;
        }
        // Check for a valid email address.
        if (TextUtils.isEmpty(user_info.getEmail()) || (!user_info.getEmail().contains("@"))) {
            MessageBox.showToast(getApplicationContext(), this.getString(R.string.email_check));
            return;
        }
        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(user_info.getPassword()) || user_info.getPassword().length() < 4) {
            MessageBox.showToast(getApplicationContext(), this.getString(R.string.password_check));
            return;
        }
        try {
            ServiceRequest serviceRequest = new ServiceRequest(Config.CMD_REGISTER, ServiceRequest.Register(user_info));
            UDocterServices.invoke(this, serviceRequest).execute();
            isLoading = true;
            viewState(Config.ViewState.Loading);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
    private void viewState(ViewState state) {
        if (ViewState.Loading.equals(state)) {
            //mSingUpFormView.setVisibility(View.GONE);
            mProgressView.setVisibility(View.VISIBLE);
            return;
        }
        // Loaded
        if (ViewState.Loaded.equals(state)) {
            //mSingUpFormView.setVisibility(View.VISIBLE);
            mProgressView.setVisibility(View.GONE);
            return;
        }
    }

    @Override
    public void onResponseReceived(int cmd, String resp) {
        viewState(ViewState.Loaded);
        isLoading = false;
        if (Utils.isNullOrEmpty(resp)) {
            return;
        }
        if (cmd == Config.CMD_REGISTER)
        {
            final String mResp = resp;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    try {
                        JSONObject jObj = new JSONObject(mResp);
                        if ((jObj != null)) {

                            viewState(ViewState.Loaded);
                            String message = jObj.getString(Config.KEY_MESSAGE);
                            MessageBox.showToast(getApplicationContext(), message);
                            if ((jObj.getInt("ERR_CODE") == 0))
                                openLogin();
                        } else {
                            MessageBox.showToast(getApplicationContext(), getString(R.string.no_data));
                        }

                    } catch (JSONException e) {
                        viewState(ViewState.Empty);
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
