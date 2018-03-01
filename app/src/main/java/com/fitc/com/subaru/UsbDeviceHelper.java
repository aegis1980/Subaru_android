package com.fitc.com.subaru;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.usb.UsbDevice;
import android.util.Log;

import com.hoho.android.usbserial.driver.UsbSerialPort;


/**
 * Created by jonro on 10/02/2018.
 */

public class UsbDeviceHelper {
    private static final String TAG = UsbDeviceHelper.class.getSimpleName();
    private static final boolean LOGGING = true;
    private static UsbDeviceHelper ourInstance;
    private final Context mContext;
    private String mDeviceName;

    public static UsbDeviceHelper getInstance(Context c) {
        if (ourInstance==null){
            ourInstance= new UsbDeviceHelper(c);
        }
        return ourInstance;
    }

    private UsbDeviceHelper(Context c) {
        mContext = c;

        getDeviceName();

    }

    public String getDeviceName() {
        SharedPreferences sp = mContext.getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE );
        mDeviceName = sp.getString(Constants.SERIAL_USB_DEVICE, null );
        return mDeviceName;

    }



    public void setDevice(UsbDevice device) {

        if (device!=null){
            mDeviceName = device.getDeviceName();
            SharedPreferences sp = mContext.getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE );
            sp.edit().putString(Constants.SERIAL_USB_DEVICE, mDeviceName ).commit();
        } else {
            if (LOGGING) Log.e(TAG, "Device is null");
        }

    }


}
