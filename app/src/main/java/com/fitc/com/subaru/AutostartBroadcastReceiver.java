package com.fitc.com.subaru;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.HashMap;
import java.util.Iterator;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.hardware.usb.IUsbManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.IBinder;
import android.os.ServiceManager;

public class AutostartBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        SingleIncomingCharManager.getInstance(context).openDefaultOnBootApp();

        String action = intent.getAction();
        if (action != null && action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            usbPermissionForThisApp(context);
            usbPermissionsForCameraApp(context);
            usbPermissionsForEcuApp(context);
        }

        BackgroundUsbSerialService.start(context);


    }

    private void usbPermissionsForCameraApp(Context context) {

        try {
            ResolveInfo ri = SingleIncomingCharManager.getInstance(context).getCameraApp();
            if (ri != null) {
                ApplicationInfo ai = ri.activityInfo.applicationInfo;

                if (ai != null) {
                    UsbManager manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
                    IBinder b = ServiceManager.getService(Context.USB_SERVICE);
                    IUsbManager service = IUsbManager.Stub.asInterface(b);

                    HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
                    Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
                    while (deviceIterator.hasNext()) {
                        UsbDevice device = deviceIterator.next();
                        if (device.getVendorId() != 0x2341 &&
                                device.getVendorId() != 2341 &&
                                device.getVendorId() != 0x9025 &&
                                device.getVendorId() != 9025) {
                            service.grantDevicePermission(device, ai.uid);
                            service.setDevicePackage(device, ri.resolvePackageName, ai.uid);
                        }
                    }
                }
            }
        } catch (Exception e) {
            //trace( e.toString() );
        }


    }


    private void usbPermissionForThisApp(Context context) {

        try {

            String appPackageNamespace = context.getApplicationContext().getPackageName();
            PackageManager pm = context.getPackageManager();
            ApplicationInfo ai = pm.getApplicationInfo(appPackageNamespace, 0);
            if (ai != null) {
                UsbManager manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
                IBinder b = ServiceManager.getService(Context.USB_SERVICE);
                IUsbManager service = IUsbManager.Stub.asInterface(b);

                HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
                Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
                while (deviceIterator.hasNext()) {
                    UsbDevice device = deviceIterator.next();
                    if (device.getVendorId() == 0x2341 ||
                            device.getVendorId() == 2341 ||
                            device.getVendorId() == 0x9025 ||
                            device.getVendorId() == 9025) {
                        service.grantDevicePermission(device, ai.uid);
                        service.setDevicePackage(device, appPackageNamespace, ai.uid);
                    }
                }
            }
        } catch (Exception e) {
            //trace( e.toString() );
        }
    }

    private void usbPermissionsForEcuApp(Context context){
        try {
            ResolveInfo ri = SingleIncomingCharManager.getInstance(context).getEcuApp();
            if (ri != null) {
                ApplicationInfo ai = ri.activityInfo.applicationInfo;

                if (ai != null) {
                    UsbManager manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
                    IBinder b = ServiceManager.getService(Context.USB_SERVICE);
                    IUsbManager service = IUsbManager.Stub.asInterface(b);

                    HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
                    Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
                    while (deviceIterator.hasNext()) {
                        UsbDevice device = deviceIterator.next();
                        if (device.getVendorId() != 0x2341 &&
                                device.getVendorId() != 2341 &&
                                device.getVendorId() != 0x9025 &&
                                device.getVendorId() != 9025) {
                            service.grantDevicePermission(device, ai.uid);
                            service.setDevicePackage(device, ri.resolvePackageName, ai.uid);
                        }
                    }
                }
            }
        } catch (Exception e) {
            //trace( e.toString() );
        }
    }

}
