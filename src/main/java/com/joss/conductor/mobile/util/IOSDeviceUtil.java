package com.joss.conductor.mobile.util;

import org.libimobiledevice.ios.driver.binding.exceptions.SDKException;
import org.libimobiledevice.ios.driver.binding.services.DeviceCallBack;
import org.libimobiledevice.ios.driver.binding.services.DeviceService;
import org.libimobiledevice.ios.driver.binding.services.IOSDevice;
import org.libimobiledevice.ios.driver.binding.services.InformationService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created on 9/1/16.
 */
public class IOSDeviceUtil {

    private final CountDownLatch ideviceLatch = new CountDownLatch(1);
    private List<String> devices;

    public static IOSDeviceUtil getInstance() {
        return new IOSDeviceUtil();
    }

    public List<String> findDevices() {
        try {
            DeviceService.INSTANCE.startDetection(new DeviceCallBack() {
                @Override
                protected void onDeviceAdded(String udid) {
                    addDevice(udid);
                }

                @Override
                protected void onDeviceRemoved(String s) {

                }
            });
            ideviceLatch.await(1, TimeUnit.SECONDS);
        } catch (SDKException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ideviceLatch.countDown();
        return devices;
    }

    public static IOSDevice getIOSDevice(String udid) throws SDKException {
        return DeviceService.get(udid);
    }

    public String getDeviceName(String udid) {
        String deviceName = "";
        try {
            InformationService informationService = new InformationService(getIOSDevice(udid));
            deviceName = informationService.getDeviceName();
        } catch (SDKException e) {
            e.printStackTrace();
        }
        return deviceName;
    }

    public String getDeviceType(String udid) {
        String deviceType = "";
        try {
            InformationService informationService = new InformationService(getIOSDevice(udid));
            deviceType = informationService.getDeviceType();
        } catch (SDKException e) {
            e.printStackTrace();
        }
        return deviceType;
    }

    private void addDevice(String udid) {
        if (devices == null) {
            devices = new ArrayList<String>();
        }
        devices.add(udid);
    }

}
