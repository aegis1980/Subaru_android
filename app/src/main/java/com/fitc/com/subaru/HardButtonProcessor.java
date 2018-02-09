package com.fitc.com.subaru;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;

import com.google.gson.Gson;

import java.util.HashMap;

/**
 * Created by jon robinson on 6/02/2018.
 * Processes serial inputs from connected arduino corresponding to the 5 hardware buttons in the centre dash
 */

public class HardButtonProcessor {

    private static final char INFO = 'i';
    private static final char MENU = 'm';
    private static final char MAP = 'p';
    private static final char AV = 'a';
    private static final char MEDIA = 'e';

    HashMap<String, ResolveInfo> mButtonMap = new HashMap<>(5);


    private final Context mContext;

    public HardButtonProcessor(Context context){

        mContext = context;

        loadPresetsFromPrefs();

    }

    public void loadPresetsFromPrefs() {
        SharedPreferences sp = mContext.getSharedPreferences(Constants.SHARED_PREFS,Context.MODE_PRIVATE);

        Gson gson = new Gson();
        String json;

        json = sp.getString(Constants.INFO_BUTTON,null);
        if (json!=null){
            ResolveInfo ri = gson.fromJson(json, ResolveInfo.class);
            mButtonMap.put(Constants.INFO_BUTTON,ri);
        }

        json = sp.getString(Constants.MENU_BUTTON,null);
        if (json!=null){
            ResolveInfo ri = gson.fromJson(json, ResolveInfo.class);
            mButtonMap.put(Constants.MENU_BUTTON,ri);
        }

        json = sp.getString(Constants.MAP_BUTTON,null);
        if (json!=null){
            ResolveInfo ri = gson.fromJson(json, ResolveInfo.class);
            mButtonMap.put(Constants.MAP_BUTTON,ri);
        }

        json = sp.getString(Constants.AV_BUTTON,null);
        if (json!=null){
            ResolveInfo ri = gson.fromJson(json, ResolveInfo.class);
            mButtonMap.put(Constants.AV_BUTTON,ri);
        }

        json = sp.getString(Constants.MEDIA_BUTTON,null);
        if (json!=null){
            ResolveInfo ri = gson.fromJson(json, ResolveInfo.class);
            mButtonMap.put(Constants.MEDIA_BUTTON,ri);
        }
    }

    public void processPress(char c){
        
        switch (c){
            case INFO:
                openPresetApp(mButtonMap.get(Constants.INFO_BUTTON));
                break;
            case MENU:
                gotoLauncherHome();
                break;
            case MAP:
                openPresetApp(mButtonMap.get(Constants.MAP_BUTTON));
                break;
            case AV:
                openPresetApp(mButtonMap.get(Constants.AV_BUTTON));
                break;
            case MEDIA:
                openPresetApp(mButtonMap.get(Constants.MEDIA_BUTTON));
                break;
        }

    }

    private void openPresetApp(ResolveInfo resolveInfo) {
        Intent intent = new Intent();
        intent.setClassName(resolveInfo.activityInfo.applicationInfo.packageName,
                resolveInfo.activityInfo.name);
        mContext.startActivity(intent);
    }


    private void gotoLauncherHome() {
        Intent i = new Intent();
        i.setAction(Intent.ACTION_MAIN);
        i.addCategory(Intent.CATEGORY_HOME);
        mContext.startActivity(i);
    }
}
