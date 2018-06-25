package com.example.hoshiko.myspotifi.login;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.XmlResourceParser;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hoshiko.myspotifi.R;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SignUp_Fragment extends Fragment implements View.OnClickListener {

    private static View view;
    private static EditText fullName, emailId, password, confirmPassword;
    private static TextView login;
    private static Button signUpButton;


    public SignUp_Fragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_sign_up_, container, false);
        initViews();
        setListeners();
        return view;
    }


    // Initialize all views
    private void initViews() {
        fullName =  view.findViewById(R.id.fullName);
        emailId =  view.findViewById(R.id.userEmailId);
        password = view.findViewById(R.id.password);
        confirmPassword =  view.findViewById(R.id.confirmPassword);
        signUpButton =  view.findViewById(R.id.signUpBtn);
        login =  view.findViewById(R.id.already_user);

        // Setting text selector over textviews
       XmlResourceParser xrp = getResources().getXml(R.drawable.text_selector);
        try {
            ColorStateList csl = ColorStateList.createFromXml(getResources(),
                    xrp);

            login.setTextColor(csl);

        } catch (Exception e) {
        }
    }

    // Set Listeners
    private void setListeners() {
        signUpButton.setOnClickListener(this);
        login.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.signUpBtn:

                // Call checkValidation method
                boolean canSignUp = checkValidation();
                if (canSignUp) {
                    doSignUp();
                    new LoginActivity().replaceLoginFragment();
                };
                break;

            case R.id.already_user:

                // Replace login fragment
                new LoginActivity().replaceLoginFragment();
                break;
        }
    }

    private void doSignUp() {
        Background background = new Background(this.getContext());
        String name = fullName.getText().toString();
        String email = emailId.getText().toString();
        String pass = password.getText().toString();
        background.execute(name, email, pass);

    }

    // Check Validation Method
    private boolean checkValidation() {

        // Get all edittext texts
        String getFullName = fullName.getText().toString();
        String getEmailId = emailId.getText().toString();
        String getPassword = password.getText().toString();
        String getConfirmPassword = confirmPassword.getText().toString();

        // Pattern match for email id
        Pattern p = Pattern.compile(Utils.regEx);
        Matcher m = p.matcher(getEmailId);

        // Check if all strings are null or not
        if (getFullName.equals("") || getFullName.length() == 0
                || getEmailId.equals("") || getEmailId.length() == 0
                || getPassword.equals("") || getPassword.length() == 0
                || getConfirmPassword.equals("")
                || getConfirmPassword.length() == 0) {
            new CustomToast().Show_Toast(getActivity(), view,
                    "Bắt buộc điền đủ.");
            return false;
        }

        // Check if email id valid or not
        else if (!m.find()) {
            new CustomToast().Show_Toast(getActivity(), view,
                    "Email Id của bạn không hợp lệ.");
            return false;
        }

        // Check if both password should be equal
        else if (!getConfirmPassword.equals(getPassword)) {
            new CustomToast().Show_Toast(getActivity(), view,
                    "Password không khớp.");
            return false;
        }

        return true;
    }
}


class Background extends AsyncTask<String, String, String> {

    private static final String TAG = "SIGNUP_FRAGMENT" ;
    private Context mContext;
    String data = "";

    public Background (Context context){
        mContext = context;
    }



    @Override
    protected String doInBackground(String... params) {
        String name = params[0];
        String email = params[1];
        String password = params[2];

        int temp;

        OutputStream outputStream = null;
        InputStream inputStream = null;
        HttpURLConnection urlConnection = null;

        try {
            URL url = new URL("http://yeulaptrinh.xyz/music_app/API/register.php") ;
            String urlParams = "name="+name+"&password="+password+"&email="+email;

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
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

        s = "Đăng ký tài khoản thành công!";
        if(data.equals("Unable to save data into mySQL")){
            s = "Thất bại: Email đã được đăng ký!";
        }
        Toast.makeText(mContext, s, Toast.LENGTH_LONG).show();

    }

}


