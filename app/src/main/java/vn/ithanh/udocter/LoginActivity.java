package vn.ithanh.udocter;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.util.ArrayList;
import java.util.Arrays;

import vn.ithanh.udocter.model.DOCTER_INFO;
import vn.ithanh.udocter.service.IServiceCallback;
import vn.ithanh.udocter.service.ServiceRequest;
import vn.ithanh.udocter.service.UDocterServices;
import vn.ithanh.udocter.util.Config;
import vn.ithanh.udocter.util.ConnectionDetector;
import vn.ithanh.udocter.util.CryptLib;
import vn.ithanh.udocter.util.MessageBox;
import vn.ithanh.udocter.util.MySharedPreferences;
import vn.ithanh.udocter.util.Utils;

/**
 * Created by iThanh on 12/3/2017.
 */

public class LoginActivity extends AppCompatActivity implements IServiceCallback {

    //private AutoCompleteTextView mEmailView;

    private boolean isLoading = false;
    ProgressDialog progress;

    private LoginButton loginButton;
    public static CallbackManager callbackManager;
    MySharedPreferences mySharedPreferences;

    int RC_SIGN_IN = 001;

    private GoogleSignInClient mGoogleSignInClient;

    private Button btnloginActivity;

    public void printHashKey(Context pContext) {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            for (android.content.pm.Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String hashKey = new String(Base64.encode(md.digest(), 0));
                Log.i("TAG", "printHashKey() Hash Key: " + hashKey);
            }
        } catch (NoSuchAlgorithmException e) {
            Log.e("TAG", "printHashKey()", e);
        } catch (Exception e) {
            Log.e("TAG", "printHashKey()", e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity_new);

        printHashKey(this);

        btnloginActivity = findViewById(R.id.btnloginActivity);

        btnloginActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mainIntent = new Intent(getApplicationContext(), LoginSysActivity.class);
                startActivity(mainIntent);
            }
        });

        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(this));
        this.mySharedPreferences = new MySharedPreferences(this);
        openMain();
        callbackManager = CallbackManager.Factory.create();

        // Login Google
//        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestEmail()
//                .build();
//
//        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
//
//        SignInButton signInButton = findViewById(R.id.sign_in_button);
//        signInButton.setSize(SignInButton.SIZE_STANDARD);
//
//        findViewById(R.id.sign_in_button).setOnClickListener(this);


        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList("public_profile", "email"));
        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                String accessToken = loginResult.getAccessToken().getToken();
                Log.i("accessToken", accessToken);

                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {

                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        Log.i("JSONObject", object.toString());

                        try {
                            String id = object.getString("id");
                            String first_name = object.getString("first_name");
                            String email = id+"@facebook.com";
                            if (object.has("email"))
                                email = object.getString("email");
                            //Log.i("FBLogin", "FBLogin: ");
                            FBLogin(id, first_name, email);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        // Get facebook data from login
                    }
                });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,first_name,email");
                request.setParameters(parameters);
                request.executeAsync();
            }
            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
            }
        });

        // get setting
        GET_SETTING();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
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
    public void GET_SETTING(){
        ConnectionDetector cd = new ConnectionDetector(this);
        if (!cd.isConnectingToInternet()) {
            MessageBox.showToast(this, getString(R.string.msg_no_internet_access));
            return;
        }
        try {
            ServiceRequest serviceRequest = new ServiceRequest(Config.CMD_GET_SETTING, ServiceRequest.getSetting());
            UDocterServices.invoke(this, serviceRequest).execute();
            isLoading = true;
            showProgress();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void FBLogin(String id, String first_name, String email){
        ConnectionDetector cd = new ConnectionDetector(this);
        if (!cd.isConnectingToInternet()) {
            MessageBox.showToast(this,
                    getString(R.string.msg_no_internet_access));
            return;
        }
        try {
            mySharedPreferences.setFBID(id);
            ServiceRequest serviceRequest = new ServiceRequest(Config.CMD_FB_LOGIN, ServiceRequest.FBLogin(id, first_name, email));
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
            ServiceRequest serviceRequest = new ServiceRequest(Config.CMD_FB_LOGIN, ServiceRequest.UpUType(id, t));
            UDocterServices.invoke(this, serviceRequest).execute();
            isLoading = true;
            showProgress();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showProgress(){
        progress = ProgressDialog.show(LoginActivity.this, "", "Loading...", true);
    }

    @Override
    public void onResponseReceived(int cmd, String resp) {
        //viewState(Config.ViewState.Loaded);
        isLoading = false;
        progress.dismiss();
        if (Utils.isNullOrEmpty(resp)) {
            return;
        }
        if (cmd == Config.CMD_FB_LOGIN)
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
        if (cmd == Config.CMD_GET_SETTING)
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
                                    if (docInfo.getString("SETTING_KEY").equals(Config.SETTING.ADDRESS_DOCTER_DEFAULT))
                                        mySharedPreferences.setDEFAULT_ADDRESS(docInfo.getString("VALUES_KEY"));
                                    if (docInfo.getString("SETTING_KEY").equals(Config.SETTING.AVATAR_DOCTER_DEFAULT))
                                        mySharedPreferences.setDEFAULT_AVATAR(docInfo.getString("VALUES_KEY"));
                                }

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
    public void showDialog() {
        final CharSequence[] items = { "Bác sĩ", "Bệnh nhân" };
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
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

}
