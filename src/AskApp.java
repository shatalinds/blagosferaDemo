package ru.askor.blagosfera;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.flurry.android.FlurryAgent;
import com.flurry.android.FlurryAgentListener;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;

import ru.askor.blagosfera.activitys.AskMainActivity;

@ReportsCrashes(
        formUri = "https://blagosfera.cloudant.com/acra-blagosfera/_design/acra-storage/_update/report",
        reportType = HttpSender.Type.JSON,
        httpMethod = HttpSender.Method.POST,
        formUriBasicAuthLogin = "bednesevedlimingloweresc",
        formUriBasicAuthPassword = "0dc2660bb4c540c7dc714c9386c7e749da1486a6",
        //formKey = "", // This is required for backward compatibility but not used
        customReportContent = {
                ReportField.APP_VERSION_CODE,
                ReportField.APP_VERSION_NAME,
                ReportField.ANDROID_VERSION,
                ReportField.PACKAGE_NAME,
                ReportField.REPORT_ID,
                ReportField.BUILD,
                ReportField.STACK_TRACE
        },
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.toast_crash
)

public class AskApp extends Application {
    public final static String FLURRY_API_KEY = "???????????";
    public static Intent service = null;

    private static FlurryAgentListener flurryAgentListener = new FlurryAgentListener() {
        @Override
        public void onSessionStarted() {}
    };

    public void reportResolutionAndMemoryOnce() {
        FlurryAgent.setLogEnabled(true);
        FlurryAgent.setLogEvents(true);

        Configuration config = getBaseContext().getResources().getConfiguration();
        if ( config.screenWidthDp > config.screenHeightDp ) {
            FlurryAgent.logEvent(config.screenWidthDp + "x" + config.screenHeightDp);
        } else {
            FlurryAgent.logEvent(config.screenHeightDp + "x" + config.screenWidthDp);
        }

        FlurryAgent.logEvent("RAM: " + Runtime.getRuntime().maxMemory() /1024 / 1024 + "mb");
    }

    public static void startMyService(Context context) {
        if (service == null) {
            service = new Intent(context, AskConnectService.class);
        }
        if (!AskAppConfig.isServiceRunning(context, AskConnectService.class)) {
            AskAppConfig.setDeviceConnect(false);
            context.startService(service);
        }
    }

    public static void stopMyService(Context context) {
        if (AskAppConfig.isServiceRunning(context, AskConnectService.class) == true) {
            context.stopService(new Intent(context, AskConnectService.class));
            AskAppConfig.setDeviceConnect(false);
            service = null;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ACRA.init(this);
        startMyService(this);

        new FlurryAgent.Builder()
                .withListener(flurryAgentListener)
                .withLogEnabled(true)
                .withLogLevel(Log.INFO)
                .withContinueSessionMillis(5000L)
                .withCaptureUncaughtExceptions(true)
                .withPulseEnabled(true)
                .build(this, FLURRY_API_KEY);

        reportResolutionAndMemoryOnce();
    }
}