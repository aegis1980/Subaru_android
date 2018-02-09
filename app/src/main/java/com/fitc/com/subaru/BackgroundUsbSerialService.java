package com.fitc.com.subaru;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.util.HexDump;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;


public class BackgroundUsbSerialService extends Service implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = BackgroundUsbSerialService.class.getSimpleName() ;

    /*
    Messages to the Sevrice handler.
     */
    private static final int NEW_INCOMING_DATA = 10;

    /**
     * Driver instance, passed in statically via
     * {@link #start(Context, UsbSerialPort)}.
     *
     * <p/>
     * This is a devious hack; it'd be cleaner to re-create the driver using
     * arguments passed in with the {@link #startActivity(Intent)} intent. We
     * can get away with it because both activities will run in the same
     * process, and this is a simple demo.
     */
    private static UsbSerialPort sPort = null;

    private UsbManager mUsbManager;
    private SerialInputOutputManager mSerialIoManager;


    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;

    private HardButtonProcessor mHbp;


    public static void start(Context context, UsbSerialPort port) {
        sPort = port;
        Intent intent = new Intent(context, BackgroundUsbSerialService.class);
        context.startService(intent);

    }


    public BackgroundUsbSerialService() {
    }



    @Override
    public void onCreate() {

        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        mHbp = new HardButtonProcessor(this);

        SharedPreferences sp = getSharedPreferences(Constants.SHARED_PREFS,MODE_PRIVATE);
        sp.registerOnSharedPreferenceChangeListener(this);


        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                android.os.Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

        Log.d(TAG, "Resumed, port=" + sPort);
        if (sPort == null) {
            Log.d(TAG,"No serial device.");
        } else {
            final UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

            UsbDeviceConnection connection = usbManager.openDevice(sPort.getDriver().getDevice());
            if (connection == null) {
                Log.e(TAG,"Opening device failed");
                return START_STICKY;
            }

            try {
                sPort.open(connection);
                sPort.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);

            } catch (IOException e) {
                Log.e(TAG, "Error setting up device: " + e.getMessage(), e);
                try {
                    sPort.close();
                } catch (IOException e2) {
                    // Ignore.
                }
                sPort = null;
                return START_STICKY;
            }
            Log.d(TAG,"Serial device: " + sPort.getClass().getSimpleName());
        }
        onDeviceStateChange();


        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        mServiceHandler.sendMessage(msg);

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }




    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
    }


    //****************************************************************************************

    private void stopIoManager() {
        if (mSerialIoManager != null) {
            Log.i(TAG, "Stopping io manager ..");
            mSerialIoManager.stop();
            mSerialIoManager = null;
        }
    }

    private void startIoManager() {
        if (sPort != null) {
            Log.i(TAG, "Starting io manager ..");
            mSerialIoManager = new SerialInputOutputManager(sPort, mListener);
            mServiceHandler.post(mSerialIoManager);
            try {
                sPort.setDTR(true);
                sPort.setRTS(true);
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }

    private void onDeviceStateChange() {
        stopIoManager();
        startIoManager();
    }

    private void processIncomingData(byte[] data) {

        String msg = HexDump.dumpHexString(data);

        if (data.length==1){
            mHbp.processPress(msg.charAt(0));
        }

        Log.d(TAG,"Data read " + data.length + " bytes: " + msg);
    }



    private final SerialInputOutputManager.Listener mListener =
            new SerialInputOutputManager.Listener() {

                @Override
                public void onRunError(Exception e) {
                    Log.e(TAG, "Runner stopped.");
                }

                @Override
                public void onNewData(final byte[] data) {

                    // For each start request, send a message to start a job and deliver the
                    // start ID so we know which request we're stopping when we finish the job
                    Message msg = mServiceHandler.obtainMessage();
                    msg.arg1 = NEW_INCOMING_DATA;
                    msg.obj = data;
                    mServiceHandler.sendMessage(msg);
                }
            };

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key){
            case Constants.AV_BUTTON:
            case Constants.INFO_BUTTON:
            case Constants.MAP_BUTTON:
            case Constants.MENU_BUTTON:
            case Constants.MEDIA_BUTTON:
                String json = sharedPreferences.getString(key,null);
                if (json!=null) {
                    ResolveInfo ri = new Gson().fromJson(json, ResolveInfo.class);
                    assignActivitytoHardwareButton(key, ri);
                }
        }
    }

    private void assignActivitytoHardwareButton(String buttonKey,ResolveInfo ri)  {
        mHbp.loadPresetsFromPrefs();
    }


    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {

            switch (msg.arg1){
                case NEW_INCOMING_DATA:
                    byte[] data = (byte[]) msg.obj;
                    processIncomingData(data);
                    break;
            }

            //final List<UsbSerialDriver> drivers =
            //        UsbSerialProber.getDefaultProber().findAllDrivers(mUsbManager);
            // Normally we would do some work here, like download a file.
            // For our sample, we just sleep for 5 seconds.

            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            //stopSelf(msg.arg1);
        }
    }
}
