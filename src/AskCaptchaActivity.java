package ru.askor.blagosfera.activitys;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Network;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;

import org.xwalk.core.XWalkCookieManager;
import org.xwalk.core.XWalkPreferences;
import org.xwalk.core.XWalkResourceClient;
import org.xwalk.core.XWalkView;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import ru.askor.blagosfera.AskApp;
import ru.askor.blagosfera.AskAppConfig;
import ru.askor.blagosfera.AskCookies;
import ru.askor.blagosfera.AskNetwork;
import ru.askor.blagosfera.R;
import ru.askor.blagosfera.fragments.AskWebFragment;
import ru.askor.blagosfera.interfaces.AskCallback;
import ru.askor.blagosfera.interfaces.AskParamCallback;
import ru.askor.blagosfera.interfaces.AskRequestCallback;

public class AskCaptchaActivity extends AppCompatActivity implements Handler.Callback {
    private static final String captcha2 = "https://blagosfera.su/ng/captcha.html";
    public static final String P_USER = "user";
    public static final String P_PASSWORD = "password";
    public static final String P_REMEMBER_ME = "rememberMe";

    private static final int TOAST = 1;

    private XWalkView walkView;
    private ProgressDialog dialog;
    private static XWalkCookieManager cookieManager = null;
    private static Handler handler = null;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_captcha);

        handler = new Handler(this);

        Intent intent = getIntent();
        walkView = (XWalkView)findViewById(R.id.walkView);

        walkView.setResourceClient(new MyResourceClient(walkView));
        XWalkPreferences.setValue(XWalkPreferences.REMOTE_DEBUGGING, true);
        XWalkPreferences.setValue("enable-javascript", true);
        XWalkPreferences.setValue(XWalkPreferences.REMOTE_DEBUGGING, true);
        XWalkPreferences.setValue(XWalkPreferences.JAVASCRIPT_CAN_OPEN_WINDOW, true);

        cookieManager = new XWalkCookieManager();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptFileSchemeCookies(true);

        dialog = new ProgressDialog(this);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setMessage(getString(R.string.wait));
        dialog.show();

        walkView.addJavascriptInterface(new CaptchaX(AskAppConfig.getContext(),
                intent.getStringExtra(P_USER),
                intent.getStringExtra(P_PASSWORD),
                intent.getBooleanExtra(P_REMEMBER_ME, true), walkView),
        "CaptchaX");

        walkView.clearCache(true);
        walkView.getSettings().setInitialPageScale(300);
        walkView.load(captcha2, null);
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case TOAST: {
                Toast.makeText(this, (String)msg.obj, Toast.LENGTH_SHORT).show();
            } break;
        }
        return false;
    }

    private void exitAfterTime(final int milliseconds, final boolean silient) {
        final int[] counter = new int[1];
        counter[0] = 0;

        if (silient == false) {
            handler.obtainMessage(TOAST, getString(R.string.exit)).sendToTarget();
        }

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                counter[0] += 1000;
                if (counter[0] >= milliseconds) {
                    cancel();
                    moveTaskToBack(true);
                    finish();
                }
            }
        }, 0, 1000);
    }

    public class CaptchaX {
        private static final String TAG = "CaptchaX";
        private final Context context;
        private final XWalkView walkView;
        private AskNetwork network;

        private String u;
        private String p;
        private boolean r;

        public CaptchaX(Context context, final String username,
                       final String password, final boolean rememberMe, XWalkView xWalkWebView) {
            this.context = context;
            this.walkView = xWalkWebView;
            this.u = username;
            this.p = password;
            this.r = rememberMe;
            network = AskNetwork.getInstance();
        }

        @org.xwalk.core.JavascriptInterface
        public void captchaSuccess(final String token) {
            network.login(u, p, r, token, new AskParamCallback<String, Void>() {
                @Override
                public Void callbackFunc(String params) {
                    if (params.equalsIgnoreCase("\"OK\"")) {
                        FlurryAgent.setUserId(u);

                        Intent i = new Intent(context, AskMainActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(i);
                    } else {
                        handler.obtainMessage(TOAST,
                                getString(R.string.login_method_return_error) + params).sendToTarget();
                        exitAfterTime(5000, false);
                    }
                    return null;
                }
            });
        }

        @org.xwalk.core.JavascriptInterface
        public void captchaExpired() {
            handler.obtainMessage(TOAST,
                    getString(R.string.error) + " " + getString(R.string.time_out)).sendToTarget();
            exitAfterTime(5000, false);
        }
    }

    private class MyResourceClient extends XWalkResourceClient {
        MyResourceClient(XWalkView view) {
            super(view);
        }

        @Override
        public boolean shouldOverrideUrlLoading(XWalkView view, String stringUrl) {
            return false;
        }

        @Override
        public void onLoadFinished(XWalkView view, String url) {
            super.onLoadFinished(view, url);
            if (dialog != null) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
            if (url.equalsIgnoreCase(captcha2)) {
                walkView.scrollTo((int) (walkView.getWidth() * 0.80f), 0);
            }
        }
    }
}
