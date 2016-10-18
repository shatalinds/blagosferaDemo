package ru.askor.blagosfera;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.ActivityCompat;

public class AskGpsService extends Service implements Handler.Callback {
    public static final int MESSAGE_LOCATION = 1;
    public static final int MESSAGE_REQUEST_LOCATION = 2;
    public static final int MESSAGE_REQUEST_LOCATION_SUCCESS = 3;
    public static final int MESSAGE_REQUEST_LOCATION_FAIL = 4;

    private static Handler handlerMain = null;
    private static Handler handler = null;

    private static LocationManager locationManager;
    private static Context context = null;

    public AskGpsService() {
    }

    public static void setContext(Context context) {
        AskGpsService.context = context;
    }

    public void onCreate() {
        super.onCreate();
    }

    public static Handler getHandler() {
        return AskGpsService.handler;
    }

    public static void setMainHandler(Handler handler) {
        AskGpsService.handlerMain = handler;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        int cmd = super.onStartCommand(intent, flags, startId);
        handler = new Handler(this);
        requestLocation();
        return cmd;
    }

    private static boolean requestSuccess = false;

    private void requestLocation() {
        if (locationManager == null) {
            if (context != null) {
                locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            }
        }
        if (locationManager != null) {
            if (checkPermission()) {
                requestSuccess = true;
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        0, 0, myLocationListener);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        0, 0, myLocationListener);

                if (handlerMain != null) {
                    handlerMain.obtainMessage(MESSAGE_REQUEST_LOCATION_SUCCESS).sendToTarget();
                }
            } else {
                requestSuccess = false;
                if (handlerMain != null) {
                    handlerMain.obtainMessage(MESSAGE_REQUEST_LOCATION_FAIL).sendToTarget();
                }
            }
        }
    }

    public void onDestroy() {
        if (checkPermission()) {
            locationManager.removeUpdates(myLocationListener);
        }

        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Bundle bundle = intent.getExtras();
        return null;
    }

    private LocationListener myLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (handlerMain != null) {
                handlerMain.obtainMessage(MESSAGE_LOCATION, location).sendToTarget();
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    private boolean checkPermission() {
        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_REQUEST_LOCATION: {
                if (handlerMain != null) {
                    if (requestSuccess) {
                        handlerMain.obtainMessage(MESSAGE_REQUEST_LOCATION_SUCCESS).sendToTarget();
                    } else {
                        requestLocation();
                        handlerMain.obtainMessage(MESSAGE_REQUEST_LOCATION_FAIL).sendToTarget();
                    }
                }
            } break;
        }
        return false;
    }
}
