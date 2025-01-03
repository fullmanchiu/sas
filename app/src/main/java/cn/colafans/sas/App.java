package cn.colafans.sas;

import android.app.Application;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;

public class App extends Application {
    private static App mInstance;

    public static App getInstance() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        startFloatingService();
    }

    public void startFloatingService() {
        if (!Settings.canDrawOverlays(mInstance)) {
            Intent permissionintent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            permissionintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mInstance.startActivity(permissionintent);
        } else {
            Intent serviceIntent = new Intent(mInstance, FloatingWindowService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mInstance.startForegroundService(serviceIntent);
            } else {
                mInstance.startService(serviceIntent);
            }
        }
    }

}
