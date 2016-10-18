package ru.askor.blagosfera.activitys;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.lib.recaptcha.ReCaptcha;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import org.xwalk.core.XWalkPreferences;
import org.xwalk.core.XWalkView;

import java.util.ArrayList;
import java.util.HashMap;

import ru.askor.blagosfera.AskApp;
import ru.askor.blagosfera.AskAppConfig;
import ru.askor.blagosfera.AskLogHelper;
import ru.askor.blagosfera.AskNetwork;
import ru.askor.blagosfera.R;
import ru.askor.blagosfera.interfaces.AskCallback;
import ru.askor.blagosfera.interfaces.AskRequestCallback;

public class AskStartActivity extends AppCompatActivity implements View.OnFocusChangeListener {
    private static final String TAG = "AskStartActivity";

    private Button btnEnter;

    private EditText etUser;
    private EditText etPassword;
    private CheckBox cbRememberMe;
    private ScrollView scrollView;

    private AskNetwork network;
    private PermissionListener permissionlistener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        AskAppConfig.setActivity(this);
        AskAppConfig.setContext(getBaseContext());

        AskAppConfig.initPreferences();
        AskLogHelper.appendToLog(TAG, "Start AskMainActivity");

        network = AskNetwork.getInstance();
        scrollView = (ScrollView)findViewById(R.id.scrollView);

        final String user = AskAppConfig.getUser();
        final String password = AskAppConfig.getPassword();
        final boolean rememberMe = AskAppConfig.getRememberMe();

        etUser = (EditText)findViewById(R.id.etUser);
        etUser.setOnFocusChangeListener(this);
        etUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrollDown();
            }
        });

        if (user != null) {
            etUser.setText(user);
        }

        etPassword = (EditText)findViewById(R.id.etPassword);
        etPassword.setOnFocusChangeListener(this);
        etPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrollDown();
            }
        });

        if (password != null) {
            etPassword.setText(password);
        }

        cbRememberMe = (CheckBox)findViewById(R.id.cbRememberMe);
        cbRememberMe.setChecked(rememberMe);

        btnEnter = (Button)findViewById(R.id.btnEnter);

        //final Context context = getBaseContext();
        btnEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String user = etUser.getText().toString();
                final String password = etPassword.getText().toString();

                if (user.length() == 0) {
                    AskAppConfig.alertDialog(getString(R.string.info), getString(R.string.user_not_enter),
                            0, null, 0, getString(R.string.ok), new AskCallback() {
                        @Override
                        public void callbackFunc() {
                            etUser.setFocusable(true);
                        }
                    }, null, null, null, null);
                    return;
                }

                if (password.length() == 0) {
                    AskAppConfig.alertDialog(getString(R.string.info), getString(R.string.password_not_enter),
                            0, null, 0, getString(R.string.ok), new AskCallback() {
                        @Override
                        public void callbackFunc() {
                            etPassword.setFocusable(true);
                        }
                    }, null, null, null, null);
                    return;
                }

                AskAppConfig.setUser(user);
                AskAppConfig.setPassword(password);
                AskAppConfig.setRememberMe(cbRememberMe.isChecked());

                Intent i = new Intent(AskStartActivity.this, AskCaptchaActivity.class);
                i.putExtra(AskCaptchaActivity.P_USER, user);
                i.putExtra(AskCaptchaActivity.P_PASSWORD, password);
                i.putExtra(AskCaptchaActivity.P_REMEMBER_ME, true);
                startActivity(i);
            }
        });

        permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                final String message = String.format(getString(R.string.access_denied), deniedPermissions.toString());
                Toast.makeText(AskStartActivity.this, message, Toast.LENGTH_LONG).show();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        moveTaskToBack(true);
                        finish();
                    }
                }, 3000);
            }
        };

        new TedPermission(this)
                .setPermissionListener(permissionlistener)
                .setPermissions(
                        android.Manifest.permission.INTERNET,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.BLUETOOTH,
                        android.Manifest.permission.BLUETOOTH_ADMIN,
                        android.Manifest.permission.ACCESS_WIFI_STATE,
                        android.Manifest.permission.CHANGE_WIFI_STATE,
                        android.Manifest.permission.RECORD_AUDIO,
                        android.Manifest.permission.WAKE_LOCK,
                        //android.Manifest.permission.WRITE_SETTINGS,
                        android.Manifest.permission.MODIFY_AUDIO_SETTINGS,
                        android.Manifest.permission.READ_CONTACTS,
                        android.Manifest.permission.VIBRATE,
                        android.Manifest.permission.ACCESS_NETWORK_STATE,
                        android.Manifest.permission.READ_PHONE_STATE,
                        //android.Manifest.permission.READ_LOGS,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE)
                .check();

        if (rememberMe) {
            Intent i = new Intent(AskStartActivity.this, AskMainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        etPassword.setFocusableInTouchMode(true);
        etUser.setFocusableInTouchMode(true);
        AskAppConfig.hideKeyboard(this);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            scrollDown();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FlurryAgent.onStartSession(this, AskApp.FLURRY_API_KEY);
    }

    @Override
    protected void onStop() {
        super.onStop();
        FlurryAgent.onEndSession(this);
    }

    private void scrollDown() {
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                final int y = scrollView.getBottom();
                if (y > 0 && scrollView.getScrollY() == 0) {
                    ObjectAnimator objectAnimator = ObjectAnimator.ofInt(scrollView, "scrollY", 0, y).setDuration(500);
                    objectAnimator.start();
                    scrollView.scrollTo(0, y);
                }
            }
        });
    }
}
