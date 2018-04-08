package com.fitc.com.subaru;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;

public class MainActivity extends Activity implements SharedPreferences.OnSharedPreferenceChangeListener, View.OnLongClickListener {

    public static final int REQUEST_CODE_PICK_CAMERA_APP = 6;
    private Button mInfoBtn,mMenuBtn,mMapBtn,mAvBtn,mMediaBtn;
    private TextView mInfoTv,mMenuTv,mMapTv,mAvTv,mMediaTv;
    private int SELECTED_START_APP_COLOUR = Color.RED;


    /**
     * Store normal colors of TV so can revert back to them on {@link MainActivity#resetTvColors}
     */
    private ColorStateList mTvColorState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BackgroundUsbSerialService.start(this);


        /**
         * Sort out long presses. Regular presses in the rotation as 'onclick'
         */
        (mInfoBtn = findViewById(R.id.info_button)).setOnLongClickListener(this);
        (mMenuBtn = findViewById(R.id.menu_button)).setOnLongClickListener(this);
        (mMapBtn = findViewById(R.id.map_button)).setOnLongClickListener(this);
        (mAvBtn =  findViewById(R.id.av_button)).setOnLongClickListener(this);
        (mMediaBtn =  findViewById(R.id.media_button)).setOnLongClickListener(this);


        /**
         * Referecnes to text views which show default app for each button
         */
        mInfoTv = findViewById(R.id.tv_info);
        mMenuTv = findViewById(R.id.tv_menu);
        mMapTv = findViewById(R.id.tv_map);
        mAvTv =  findViewById(R.id.tv_av);
        mMediaTv =  findViewById(R.id.tv_media);

        mTvColorState = mInfoTv.getTextColors();


        SharedPreferences sharedPref = getSharedPreferences(Constants.SHARED_PREFS, MODE_PRIVATE);
        sharedPref.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences sharedPref = getSharedPreferences(Constants.SHARED_PREFS, MODE_PRIVATE);
        sharedPref.registerOnSharedPreferenceChangeListener(this);

        Constants.triggerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences sharedPref = getSharedPreferences(Constants.SHARED_PREFS, MODE_PRIVATE);
        sharedPref.unregisterOnSharedPreferenceChangeListener(this);
    }

    public void setupSerialConnection(View serialButtonView){
        Intent i = new Intent(this,DeviceListActivity.class);
        startActivityForResult(i,Constants.SERIAL_SETUP_REQUEST_CODE);
    }

    /**
     * Method bound to buttons in rotation/ UI
     * Starts app picket activity so user can select which app accosictaed with each hardware button
     * Result goes through {@code onActivityResult}
     *
     * @param buttonView
     */
    public void setHardwareButton(View buttonView){

        Intent i = new Intent(this,AppSelectionActivity.class);

        int rc = Integer.parseInt((String) buttonView.getTag());

        if (rc== REQUEST_CODE_PICK_CAMERA_APP){
            // Request code for camera.
            i.putExtra("intent-filter","android.hardware.usb.action.USB_DEVICE_ATTACHED");
        }

        startActivityForResult(i,rc);
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

            // Make sure the request was successful
            if (resultCode == RESULT_OK) {

                SharedPreferences sharedPref = getSharedPreferences(Constants.SHARED_PREFS, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();

                if (requestCode==Constants.SERIAL_SETUP_REQUEST_CODE){

                    UsbDevice device = data.getParcelableExtra(Constants.EXTRA_RESULT_DATA1);

                    UsbDeviceHelper.getInstance(this).setDevice(device);

                    BackgroundUsbSerialService.start(this);

                } else {

                    ResolveInfo ri = data.getParcelableExtra(Constants.EXTRA_RESULT_DATA1);
                    String json = ri == null ? null : new Gson().toJson(ri);

                    // Check which request we're responding to
                    switch (requestCode) {
                        case 1:
                            editor.putString(Constants.INFO_BUTTON, json).commit();
                            break;
                        case 2:
                            editor.putString(Constants.MENU_BUTTON, json).commit();
                            break;
                        case 3:
                            editor.putString(Constants.MAP_BUTTON, json).commit();
                            break;
                        case 4:
                            editor.putString(Constants.AV_BUTTON, json).commit();
                            break;
                        case 5:
                            editor.putString(Constants.MEDIA_BUTTON, json).commit();
                            break;
                        case 6:
                            editor.putString(Constants.REVERSING_CAMERA, json).commit();
                            break;
                        //tell service the pref have changed.

                    }
                }
            }
        }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sp, String s) {

        HardwareManager hbh = HardwareManager.getInstance(this);
        UsbDeviceHelper usm = UsbDeviceHelper.getInstance(this);
        String na = getString(R.string.not_assigned);


        //----------------------------------


        String appname;
;
        appname = hbh.getAssignedActivityByName(Constants.INFO_BUTTON);
        mInfoTv.setText(appname!=null ? appname : na);

        //tv = (TextView) findViewById(R.id.tv_menu);
        //appname = hbh.getAssignedActivityByName(Constants.MENU_BUTTON);
        //tv.setText(appname!=null ? appname : "not assigned");

        appname = hbh.getAssignedActivityByName(Constants.MAP_BUTTON);
        mMapTv.setText(appname!=null ? appname : na);

        appname = hbh.getAssignedActivityByName(Constants.AV_BUTTON);
        mAvTv.setText(appname!=null ? appname : na);

        appname = hbh.getAssignedActivityByName(Constants.MEDIA_BUTTON);
        mMediaTv.setText(appname!=null ? appname : na);

        //----------------------------------

        TextView tv = findViewById(R.id.tv_device_info);
        String device = usm.getDeviceName();
        tv.setText(device!=null ? device : na);

        //------------------------------------

        tv = findViewById(R.id.tv_camera_app_info);
        appname = hbh.getAssignedActivityByName(Constants.REVERSING_CAMERA);
        tv.setText(appname!=null ? appname : na);

    }

    /**
     * Long clickking one of the UI buttons makes this the default start app.
     * @param v
     * @return
     */
    @Override
    public boolean onLongClick(View v) {

        String button = null;
        switch (v.getId()) {
            case R.id.info_button:
                button = Constants.INFO_BUTTON;
                break;
            case R.id.menu_button:
                button = Constants.MENU_BUTTON;
                break;
            case R.id.map_button:
                button = Constants.MAP_BUTTON;
                break;
            case R.id.av_button:
                button = Constants.AV_BUTTON;
                break;
            case R.id.media_button:
                button = Constants.MEDIA_BUTTON;
                break;
        }

            if (button!=null){
                resetTvColors();
                if (button.equals(Constants.MENU_BUTTON)){
                    mMenuTv.setTextColor(SELECTED_START_APP_COLOUR);
                    // this is the default anyway - do not need to change anything.
                } else {
                   ResolveInfo ri = HardwareManager.getInstance(this).getAssignedActivity(button);
                    if (ri!=null){
                        switch (button){
                            case Constants.INFO_BUTTON:
                                mInfoTv.setTextColor(SELECTED_START_APP_COLOUR);
                                break;
                            case Constants.MAP_BUTTON:
                                mMapTv.setTextColor(SELECTED_START_APP_COLOUR);
                                break;
                            case Constants.AV_BUTTON:
                                mAvTv.setTextColor(SELECTED_START_APP_COLOUR);
                                break;
                            case Constants.MEDIA_BUTTON:
                                mMediaTv.setTextColor(SELECTED_START_APP_COLOUR);
                                break;
                        }
                        String json = ri == null ? null : new Gson().toJson(ri);

                        SharedPreferences sharedPref = getSharedPreferences(Constants.SHARED_PREFS, MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(Constants.DEFAULT_ON_BOOT, json).commit();
                    }
                }

        }
        return false;
    }

    private void resetTvColors(){
        mInfoTv.setTextColor(mTvColorState);
        mMenuTv.setTextColor(mTvColorState);
        mMapTv.setTextColor(mTvColorState);
        mAvTv.setTextColor(mTvColorState);
        mMediaTv.setTextColor(mTvColorState);
    }
}

