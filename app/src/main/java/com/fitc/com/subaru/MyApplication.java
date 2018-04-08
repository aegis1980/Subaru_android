package com.fitc.com.subaru;

import android.app.Application;
import android.content.res.Configuration;

/**
 * Created by jonro on 10/02/2018.
 */

public class MyApplication extends Application {



    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        HardwareManager.getInstance(this);
        UsbDeviceHelper.getInstance(this);

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }


}
