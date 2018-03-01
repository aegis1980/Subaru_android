package com.fitc.com.subaru;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by jonro on 24/02/2018.
 */

class TempHumidityManager {


    private static final String TAG = TempHumidityManager.class.getSimpleName();
    private static final boolean LOGGING = true;
    private static final char TEMPERATURE_MARKER = 't';
    private static final char HUMIDITY_MARKER = 'h';
    private static final int PARSE_ERROR = 999;


    private static TempHumidityManager sInstance;
    /**
     * Getter of singleton
     * @param c
     * @return
     */
    public static TempHumidityManager getInstance(Context c){
        if (sInstance == null) {
            sInstance = new TempHumidityManager(c);
        }
       // sInstance.loadPresetsFromPrefs();
        return sInstance;
    }


    private final Context mContext;


    private TempHumidityManager(Context context){
        mContext = context;

       // SharedPreferences sharedPref = mContext.getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE);
       // sharedPref.registerOnSharedPreferenceChangeListener(this);
    }

    public boolean process(char[] data) {
        if (data[0]!= TEMPERATURE_MARKER){
            return false;
        }
        boolean processingTemp =true;
        String t ="";
        String h = "";

        for (int i=1; i<data.length;i++){
            char c = data[i];
           // if (LOGGING) Log.i(TAG, ""+c);
            if (c == HUMIDITY_MARKER){
                processingTemp = false;
                continue;
            }

            if (Character.isDigit(c)) {
                if (processingTemp) {
                    t = t + c;
                } else {
                    h = h + c;
                }
            }

        }

        if (LOGGING) Log.i(TAG, " T:" + t + " H:"+ h);
        int temp = Constants.INT_PARSE_ERROR;
        int humidity = Constants.INT_PARSE_ERROR;
        try {
            temp = Integer.parseInt(t.trim());
        } catch (NumberFormatException e){}

        try {
            humidity = Integer.parseInt(h.trim());
        } catch (NumberFormatException e){}

        Intent intent = new Intent();
        intent.setAction(Constants.ACTION_TEMP_UPDATE);
        intent.putExtra(Constants.EXTRA_TEMP, temp);
        intent.putExtra(Constants.EXTRA_HUMIDITY, humidity);

        mContext.sendBroadcast(intent);
        return true;
    }
}
