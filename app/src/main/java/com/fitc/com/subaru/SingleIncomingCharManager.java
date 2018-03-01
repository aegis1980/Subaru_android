package com.fitc.com.subaru;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.util.Log;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.List;


/**
 * Created by jon robinson on 6/02/2018.
 * Processes serial inputs from connected arduino corresponding to the 5 hardware buttons in the centre dash
 */

public class SingleIncomingCharManager implements SharedPreferences.OnSharedPreferenceChangeListener {


    private static final String TAG = SingleIncomingCharManager.class.getSimpleName();
    private static final boolean LOGGING = true;

    private static final char INFO = 'i';
    private static final char MENU = 'm';
    private static final char MAP = 'p';
    private static final char AV = 'a';
    private static final char MEDIA = 'e';

    private static final char REVERSE = 'r';
    private static final char FORWARDS = 'f';

    private static SingleIncomingCharManager sInstance;

    HashMap<String, ResolveInfo> mButtonMap = new HashMap<>(5);


    private final Context mContext;

    /**
     * if set, this is what is loaded up on boot, by {@link AutostartBroadcastReceiver}
     */
    private ResolveInfo mDefaultOnBoot;
    private ComponentName mActivityToRestore;
    private boolean mCameraActive = false;

    /**
     * Getter of singleton
     * @param c
     * @return
     */
    public static SingleIncomingCharManager getInstance(Context c){
        if (sInstance == null) {
            sInstance = new SingleIncomingCharManager(c);
        }
        sInstance.loadPresetsFromPrefs();
        return sInstance;
    }

    private SingleIncomingCharManager(Context context){
        mContext = context;

        SharedPreferences sharedPref = mContext.getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE);
        sharedPref.registerOnSharedPreferenceChangeListener(this);
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

        json = sp.getString(Constants.REVERSING_CAMERA,null);
        if (json!=null){
            ResolveInfo ri = gson.fromJson(json, ResolveInfo.class);
            mButtonMap.put(Constants.REVERSING_CAMERA,ri);
        }

        json = sp.getString(Constants.DEFAULT_ON_BOOT,null);
        mDefaultOnBoot = gson.fromJson(json, ResolveInfo.class);

    }

    public void processChar(char c){
        
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
            case REVERSE:
                if (!mCameraActive) {
                    ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
                    mActivityToRestore = am.getRunningTasks(1).get(0).topActivity;

                    openPresetApp(mButtonMap.get(Constants.REVERSING_CAMERA));
                    mCameraActive = true;
                }
                break;
            case FORWARDS:
                if (mActivityToRestore!=null) {
                    Intent intent = new Intent();
                    intent.setAction("android.intent.action.VIEW");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setComponent(mActivityToRestore);
                    mContext.startActivity(intent);
                } else {
                    openDefaultOnBootApp();
                }
                mCameraActive = false;
                //close camera app and revert back to previous
                break;
        }

    }

    private void openPresetApp(ResolveInfo resolveInfo) {
        if (resolveInfo!=null) {
            Intent intent = new Intent();
            intent.setAction("android.intent.action.VIEW");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setClassName(resolveInfo.activityInfo.applicationInfo.packageName,
                    resolveInfo.activityInfo.name);
            mContext.startActivity(intent);
        } else {
            if (LOGGING) Log.i(TAG, "No app assigned to this button");
        }
    }


    public ResolveInfo getCameraApp(){
        return mButtonMap.get(Constants.REVERSING_CAMERA);
    }

    public ResolveInfo getEcuApp() {
        return mButtonMap.get(Constants.INFO_BUTTON);
    }


    public void openDefaultOnBootApp() {
        if (mDefaultOnBoot!=null) openPresetApp(mDefaultOnBoot);
    }

    public String getAssignedActivityByName(String button){
        ResolveInfo ri = mButtonMap.get(button);

        String name = null;
        if (ri!=null){
            name = ri.activityInfo.name;
        }

        return name;
    }

    public ResolveInfo getAssignedActivity(String button){
        return mButtonMap.get(button);
    }


    private void gotoLauncherHome() {
        Intent i = new Intent();
        i.setAction(Intent.ACTION_MAIN);
        i.addCategory(Intent.CATEGORY_HOME);
        mContext.startActivity(i);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        loadPresetsFromPrefs();
    }


}
