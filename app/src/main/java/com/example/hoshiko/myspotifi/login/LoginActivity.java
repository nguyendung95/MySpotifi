package com.example.hoshiko.myspotifi.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.example.hoshiko.myspotifi.R;
import com.example.hoshiko.myspotifi.UserActivity;

/**
 * Màn hình đăng nhập dành cho User đã từng thoát acc or lần đầu bung app
 * User sử dụng email & pass để đăng nhập
 * Nếu chưa có acc sẽ chuyển đến Signup Fragment
 */
public class LoginActivity extends AppCompatActivity {


    public static final String PREFS_NAME = "LoginPrefs";
    public static String NAME, PASSWORD, EMAIL;
    public static int  ID;

    private static FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        // Kiểm tra nếu trước đây đã từng đăng nhập
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        if (settings.getString("logged", "").equals("logged")) {

            // Lấy dữ liệu mà người dùng  cuối cùng đã đăng nhập vào
            ID = settings.getInt("Id", 0);
            NAME = settings.getString("Name", "");
            EMAIL = settings.getString("Email", "");

            // Dẫn thẳng vào home activity & hiển thị thông tin người dùng
            Intent goHome = new Intent(LoginActivity.this, UserActivity.class);
            goHome.putExtra("USER_ID", ID);
            goHome.putExtra("NAME", NAME);
            goHome.putExtra("EMAIL", EMAIL );
            startActivity(goHome);
        }

        // get fragmentAcitivy
        fragmentManager = getSupportFragmentManager();

        // If savedinstnacestate is null then replace login fragment
        if (savedInstanceState == null) {
            fragmentManager
                    .beginTransaction()
                    .replace(R.id.frameContainer, new Login_Fragment(),
                            Utils.Login_Fragment).commit();
        }

        // On close icon click finish activity
        findViewById(R.id.close_activity).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        finish();

                    }
                });

    }

    // Replace Login Fragment with animation
    protected void replaceLoginFragment() {
        fragmentManager
                .beginTransaction()
                .setCustomAnimations(R.anim.left_enter, R.anim.right_out)
                .replace(R.id.frameContainer, new Login_Fragment(),
                        Utils.Login_Fragment).commit();
    }

    @Override
    public void onBackPressed() {

        // Find the tag of signup and forgot password fragment
        Fragment SignUp_Fragment = fragmentManager
                .findFragmentByTag(Utils.SignUp_Fragment);
        Fragment ForgotPassword_Fragment = fragmentManager
                .findFragmentByTag(Utils.ForgotPassword_Fragment);

        // Check if both are null or not
        // If both are not null then replace login fragment else do backpressed
        // task

        if (SignUp_Fragment != null)
            replaceLoginFragment();
        else if (ForgotPassword_Fragment != null)
            replaceLoginFragment();
        else
            super.onBackPressed();
    }
}
