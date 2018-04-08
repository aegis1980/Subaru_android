package com.fitc.com.subaru;

import android.app.Notification;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class BackgroundUsbSerialService extends Service  {

    private static final String TAG = BackgroundUsbSerialService.class.getSimpleName() ;
    private static final boolean LOGGING = false;


    /**
    Id for running service in foreground
         */
    private static int FOREGROUND_ID=1338;
    private static final String CHANNEL_ID = "control_app";



    /*
    Messages to the Sevrice handler.
     */
    private static final int CONNECT_TO_USB = 11;
    private static final int INCOMING_DATA= 12;



    private static final char END_OF_MESSAGE = '#';

    /**
     * Driver instance, passed in statically via
     * {@link #start(Context)}.
     *
     * <p/>
     * This is a devious hack; it'd be cleaner to re-create the driver using
     * arguments passed in with the {@link #startActivity(Intent)} intent. We
     * can get away with it because both activities will run in the same
     * process, and this is a simple demo.
     */


    private UsbManager mUsbManager;
    private SerialInputOutputManager mSerialIoManager;


    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;

    private HardwareManager mHardwareMgr;
    private TempHumidityManager mTempPressureMgr;


    private UsbSerialPort mPort;
    private UsbDevice mDevice;
    private String mDeviceName;
    private PendingIntent mPermissionIntent;


    public static void start(Context context) {
        Intent intent = new Intent(context, BackgroundUsbSerialService.class);
        context.startService(intent);
       // context.startForegroundService(intent)

    }


    public BackgroundUsbSerialService() {
    }



    @Override
    public void onCreate() {

        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        mHardwareMgr = HardwareManager.getInstance(this);
        mTempPressureMgr = TempHumidityManager.getInstance(this);

        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(mUsbReceiver, filter);



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
        startForeground(FOREGROUND_ID,
                buildForegroundNotification());


        mDeviceName = UsbDeviceHelper.getInstance(this).getDeviceName();


        if (mDeviceName!=null){
            // This and try and resume a saved connection in the worker thread
            Message msg = mServiceHandler.obtainMessage();
            msg.arg1 = startId;
            msg.arg2 = CONNECT_TO_USB;
            mServiceHandler.sendMessage(msg);
        } else {
            Toast.makeText(this,"No registered devicee",Toast.LENGTH_LONG).show();
        }

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
        if (mPort!=null){
            try {
                mPort.close();
            } catch (IOException e) {
                if (LOGGING) Log.e(TAG, "No port to close in onDestroy()");
            }
        }
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
    }

    //****************************************************************************************


    /**
     * Build low priority notification for running this service as a foreground service.
     * @return
     */
    private Notification buildForegroundNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);



        NotificationCompat.Builder b=new NotificationCompat.Builder(this,CHANNEL_ID);

        b.setOngoing(true)
                .setContentTitle("Control App")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_car_white_24dp);

        return(b.build());
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

        if (mPort != null) {
            Log.i(TAG, "Starting io manager ..");
            mSerialIoManager = new SerialInputOutputManager(mPort, mListener);
            mServiceHandler.post(mSerialIoManager);
            try {
                mPort.setDTR(true);
                mPort.setRTS(true);
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }

    private void onDeviceStateChange() {
        stopIoManager();
        startIoManager();
    }

    private final LinkedList<Character> mSerialDataBuffer = new LinkedList<>();

    private void processIncomingData(byte[] data) {


        for (byte b:data){
            char c = (char) b;
            if (LOGGING) Log.d(TAG,""+c);
            mSerialDataBuffer.add(c);
        }


            ArrayList<Character> payload = new ArrayList<>();

            while (true){
               Character c =  mSerialDataBuffer.peek();
               if (c!=null){
                   if ((c==END_OF_MESSAGE)){
                        mSerialDataBuffer.pop();
                        break;
                   } else {
                        payload.add(mSerialDataBuffer.pop());
                   }
               } else {
                   break;
               }


            if (LOGGING) Log.d(TAG,"payload size:" + payload.size());

            if (payload.size()>0){

                char[] chars = new char[payload.size()];

                for (int i=0; i<payload.size(); i++){
                    chars [i] = payload.get(i);
                }

                if (chars.length==1){
                    mHardwareMgr.processChar(chars[0]);
                } else {
                    mTempPressureMgr.process(chars);
                }
            }



        }

    }

    public List<UsbSerialPort> usbSerialPortProbe(UsbManager usbManager){
        final List<UsbSerialDriver> drivers =
                UsbSerialProber.getDefaultProber().findAllDrivers(usbManager);

        final List<UsbSerialPort> result = new ArrayList<UsbSerialPort>();
        for (final UsbSerialDriver driver : drivers) {
            final List<UsbSerialPort> ports = driver.getPorts();
            if (LOGGING) Log.d(TAG, String.format("+ %s: %s port%s",
                    driver, Integer.valueOf(ports.size()), ports.size() == 1 ? "" : "s"));
            result.addAll(ports);
        }

        return result;
    }



    private final SerialInputOutputManager.Listener mListener =
            new SerialInputOutputManager.Listener() {

                @Override
                public void onRunError(Exception e) {
                    Log.e(TAG, "Runner stopped.");
                }

                @Override
                public void onNewData(final byte[] data) {


                    Message msg = mServiceHandler.obtainMessage();
                   // msg.arg1 = startId;
                    msg.arg2 = INCOMING_DATA;
                    msg.obj = data;
                    mServiceHandler.sendMessage(msg);

                }
            };




    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {


        private static final int CONNECTION_FAIL = -1;

        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {

            switch (msg.arg2){
                case CONNECT_TO_USB:
                    resumeUsbSerialConnection(msg.arg1);
                    break;
                case INCOMING_DATA:
                    byte[] data = (byte[]) msg.obj;
                    processIncomingData(data);

            }

            //final List<UsbSerialDriver> drivers =
            //        UsbSerialProber.getDefaultProber().findAllDrivers(mUsbManager);
            // Normally we would do some work here, like download a file.
            // For our sample, we just sleep for 5 seconds.

            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            //stopSelf(msg.arg1);
        }

        private void resumeUsbSerialConnection(int startId) {
            int ret = 0;

            List<UsbSerialPort> ports = usbSerialPortProbe(mUsbManager);

            for (UsbSerialPort p:ports){
                if (p.getDriver().getDevice().getDeviceName().equals(mDeviceName)){
                    mPort = p;
                    break;
                }
            }

            if (mPort == null) {
                if (LOGGING) Log.d(TAG,"No serial device.");
                ret = CONNECTION_FAIL;
            } else {

                mDevice = mPort.getDriver().getDevice();
                if (mUsbManager.hasPermission(mDevice)) {
                    UsbDeviceConnection connection = mUsbManager.openDevice(mPort.getDriver().getDevice());
                    if (connection == null) {
                        Log.e(TAG, "Opening device failed");
                        ret = CONNECTION_FAIL;
                    } else {
                        try {
                            mPort.open(connection);
                            mPort.setParameters(9600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);

                        } catch (IOException e) {
                            Log.e(TAG, "Error setting up device: " + e.getMessage(), e);
                            try {
                                mPort.close();
                            } catch (IOException e2) {
                                // Ignore.
                            }

                            ret = CONNECTION_FAIL;
                        }
                        Log.d(TAG, "Serial device: " + mPort.getClass().getSimpleName());
                    }
                    onDeviceStateChange();
               } else {
                //    mUsbManager.requestPermission(mDevice, mPermissionIntent);
               }
            }

            if (ret==CONNECTION_FAIL){
                // Stop the service using the startId, so that we don't stop
                // the service in the middle of handling another job
                stopSelf(startId);
            }
        }
    }


    private static final String ACTION_USB_PERMISSION =
            "com.android.example.USB_PERMISSION";

    private static final String EXTRA_USB_PERMISSION_STARTID =
            "com.android.example.STARTID";


    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            int startId = intent.getIntExtra(EXTRA_USB_PERMISSION_STARTID,-1);
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);


                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        // This and try and resume a saved connection in the worker thread...again
                        Message msg = mServiceHandler.obtainMessage();
                        msg.arg1 = startId;
                        msg.arg2 = CONNECT_TO_USB;
                        mServiceHandler.sendMessage(msg);
                    }
                    else {
                        Log.d(TAG, "permission denied for accessory " + device);
                    }
                }
            }
        }
    };
}
