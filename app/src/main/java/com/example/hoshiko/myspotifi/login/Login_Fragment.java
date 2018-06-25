package com.example.hoshiko.myspotifi.login;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.XmlResourceParser;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.InputType;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hoshiko.myspotifi.R;
import com.example.hoshiko.myspotifi.UserActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.jar.Attributes;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Login_Fragment extends Fragment implements View.OnClickListener {
    private static View view;

    private static EditText emailid, password;
    private static Button loginButton;
    private static TextView forgotPassword, signUp;
    private static CheckBox show_hide_password;
    private static LinearLayout loginLayout;
    private static Animation shakeAnimation;
    private static FragmentManager fragmentManager;


    public Login_Fragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_login_, container, false);
        initViews();
        setListeners();
        return view;
    }

    // Initiate Views
    private void initViews() {
        fragmentManager = Objects.requireNonNull(getActivity()).getSupportFragmentManager();

        emailid =  view.findViewById(R.id.login_emailid);
        password =  view.findViewById(R.id.login_password);
        loginButton =  view.findViewById(R.id.loginBtn);
        forgotPassword =  view.findViewById(R.id.forgot_password);
        signUp =  view.findViewById(R.id.createAccount);
        show_hide_password = view.findViewById(R.id.show_hide_password);
        loginLayout =  view.findViewById(R.id.login_layout);

        // Load ShakeAnimation
        shakeAnimation = AnimationUtils.loadAnimation(getActivity(),
                R.anim.shake);

        // Setting text selector over textviews
        @SuppressLint("ResourceType") XmlResourceParser xrp = getResources().getXml(R.drawable.text_selector);
        try {
            ColorStateList csl = ColorStateList.createFromXml(getResources(),
                    xrp);

            forgotPassword.setTextColor(csl);
            show_hide_password.setTextColor(csl);
            signUp.setTextColor(csl);
        } catch (Exception e) {
        }
    }

    // Set Listeners
    private void setListeners() {
        loginButton.setOnClickListener(this);
        forgotPassword.setOnClickListener(this);
        signUp.setOnClickListener(this);

        // Set check listener over checkbox for showing and hiding password
        show_hide_password
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton button,
                                                 boolean isChecked) {

                        // If it is checkec then show password else hide
                        // password
                        if (isChecked) {
                            show_hide_password.setText(R.string.hide_pwd);// change
                            password.setInputType(InputType.TYPE_CLASS_TEXT);
                            password.setTransformationMethod(HideReturnsTransformationMethod
                                    .getInstance());// show password
                        } else {
                            show_hide_password.setText(R.string.show_pwd);// change

                            password.setInputType(InputType.TYPE_CLASS_TEXT
                                    | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                            password.setTransformationMethod(PasswordTransformationMethod
                                    .getInstance());// hide password

                        }

                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.loginBtn:
                boolean canLogin = checkValidation();
                if(canLogin){
                    LoginAsyncTask loginAsyncTask = new LoginAsyncTask(this.getActivity());
                    String email = emailid.getText().toString();
                    String pass = password.getText().toString();
                    loginAsyncTask.execute(email, pass);
                }
                break;

            case R.id.forgot_password:

                // Replace forgot password fragment with animation
                fragmentManager
                        .beginTransaction()
                        .setCustomAnimations(R.anim.right_enter, R.anim.left_out)
                        .replace(R.id.frameContainer,
                                new ForgotPassword_Fragment(),
                                Utils.ForgotPassword_Fragment).commit();
                break;
            case R.id.createAccount:

                // Replace signup frgament with animation
                fragmentManager
                        .beginTransaction()
                        .setCustomAnimations(R.anim.right_enter, R.anim.left_out)
                        .replace(R.id.frameContainer, new SignUp_Fragment(),
                                Utils.SignUp_Fragment).commit();
                break;
        }

    }

    // Check Validation before login
    private boolean checkValidation() {
        // Get email id and password
        String getEmailId = emailid.getText().toString();
        String getPassword = password.getText().toString();

        // Check patter for email id
        Pattern p = Pattern.compile(Utils.regEx);

        Matcher m = p.matcher(getEmailId);

        // Check for both field is empty or not
        if (getEmailId.equals("") || getEmailId.length() == 0
                || getPassword.equals("") || getPassword.length() == 0) {
            loginLayout.startAnimation(shakeAnimation);
            new CustomToast().Show_Toast(getActivity(), view,
                    "Không được để trống các trường!");

            return false;
        }
        return true;
    }
}


class LoginAsyncTask extends AsyncTask<String, String, String> {

    private static final String TAG = "Login_FRAGMENT" ;
    private Context mContext;
    private Activity parentActivity;
    String data = "";


    public LoginAsyncTask (Activity activity){
        parentActivity = activity;
        mContext = parentActivity.getBaseContext();
    }


    @Override
    protected String doInBackground(String... params) {
        String email = params[0];
        String password = params[1];

        int temp;
        OutputStream outputStream =null;
        InputStream inputStream = null;
        HttpURLConnection urlConnection = null;

        try {
            URL url = new URL("http://yeulaptrinh.xyz/music_app/API/login.php") ;
            String urlParams = "email="+email+"&password="+password ;

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(true);

            // Start POST entered data into mySQL
            outputStream = urlConnection.getOutputStream();
            outputStream.write(urlParams.getBytes());
            outputStream.flush();
            outputStream.close();

            inputStream = urlConnection.getInputStream();
            while ((temp = inputStream.read()) != -1){
                data += (char) temp;
            }
            inputStream.close();
            Log.d(TAG, "DATA FROM MYSQL IS: " + data);
            return  data;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            return null;
        }
    }

    @Override
    protected void onPostExecute(String s) {

        String error = null;
        try {
            JSONObject root = new JSONObject(data);
            JSONObject user_data = root.getJSONObject("user_data");
            LoginActivity.ID = user_data.getInt("id");
            LoginActivity.NAME = user_data.getString("name");
            LoginActivity.PASSWORD = user_data.getString("password");
            LoginActivity.EMAIL = user_data.getString("email");
            Toast.makeText(mContext, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

            // Tạo SharedPreferences object
            SharedPreferences settings = parentActivity.getSharedPreferences(LoginActivity.PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("logged", "logged");
            editor.putInt("Id", LoginActivity.ID);
            editor.putString("Email", LoginActivity.EMAIL);
            editor.putString("Name", LoginActivity.NAME);
            editor.apply();

            // Truyền thông tin vào để hiển thị thông tin user
            Intent goHome = new Intent(parentActivity, UserActivity.class);
            goHome.putExtra("USER_ID", LoginActivity.ID);
            goHome.putExtra("NAME", LoginActivity.NAME);
            goHome.putExtra("EMAIL", LoginActivity.EMAIL );

            parentActivity.startActivity(goHome);

        } catch (JSONException e) {
            e.printStackTrace();
            error = "Exception: "+e.getMessage();
            Toast.makeText(mContext, "Không tồn tài tài khoản này!", Toast.LENGTH_SHORT).show();
            Log.d(TAG, error);
        }
    }

}



