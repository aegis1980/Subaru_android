package com.fitc.com.subaru;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.SyncStateContract;
import android.support.v4.content.ContextCompat;

/**
 * Created by jonro on 9/02/2018.
 */

public class Constants {
    public static final String SERIAL_USB_DEVICE = "com.fitc.SERIAL_USB_DEVICE";
    public static final String EXTRA_RESULT_DATA1 = "com.fitc.EXTRA_RESULT_DATA1";
    public static final String EXTRA_RESULT_DATA2 = "com.fitc.EXTRA_RESULT_DATA2";
    public static final String REVERSING_CAMERA = "reversing_camera" ;
    public static final String ACTION_TEMP_UPDATE = "com.fitc.ACTION_TEMP_UPDATE";
    public static final String EXTRA_TEMP = "temp";
    public static final String EXTRA_HUMIDITY = "humidity";
    public static final int INT_PARSE_ERROR = 99;


    private Constants(){}

    public static final String SHARED_PREFS = "shared_prefs";

    public static final String INFO_BUTTON = "info_button";
    public static final String MENU_BUTTON = "menu_button";
    public static final String MAP_BUTTON = "map_button";
    public static final String AV_BUTTON = "av_button";
    public static final String MEDIA_BUTTON = "media_button";
    public static final String DEFAULT_ON_BOOT = "default_on_boot";

    public static final int SERIAL_SETUP_REQUEST_CODE =10;

    public static void triggerOnSharedPreferenceChangeListener(Context c){
        SharedPreferences sp = c.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
        sp.edit().putFloat("r", (float) Math.random()).commit();
    }


}
