package com.feng.Usb.UsbSerial;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;
import com.feng.RobotApplication;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * Created by fengscar on 2016/8/1.
 * 功能: 查找当前USB设备的驱动...并返回已成功连接的驱动
 */
public class UsbSerialConnector {
    private static final String TAG = "UsbSerialConnector";

    //region Singleton
    // 用来获取 USB设备列表
    private UsbManager mUsbManager;

    private UsbSerialConnector() {
        mUsbManager = (UsbManager) RobotApplication.getContext().getSystemService(Context.USB_SERVICE);
    }

    private static UsbSerialConnector sUsbSerialConnector;

    public static UsbSerialConnector getInstance() {
        if (sUsbSerialConnector == null) {
            synchronized (UsbSerialConnector.class) {
                if (sUsbSerialConnector == null) {
                    sUsbSerialConnector = new UsbSerialConnector();
                }
            }
        }
        return sUsbSerialConnector;
    }
    //endregion

    /**
     * 查找 参数列表中的 USB转串口设备的驱动.
     *
     * @return 如果没有找到, 返回null;如果有多个匹配的设备,返回第一个;
     */
    public UsbSerialDriver getCurrentUsbSerialDriver(UsbSerialDevice... targetDevices) throws SecurityException {
        try {
            FutureTask<UsbSerialDriver> futureTask = new FutureTask<>(new GetUsbTask(targetDevices));
            futureTask.run();
            while (!futureTask.isDone()) {
                Thread.sleep(500);
                Log.i(TAG, "getCurrentUsbSerialDriver: 正在获取驱动中...");
            }
            UsbSerialDriver driver = futureTask.get();
            if (driver == null) {
                Log.i(TAG, "getCurrentUsbSerialDriver: 获取驱动失败!");
                return null;
            }
            Log.i(TAG, "getCurrentUsbSerialDriver: 获取到驱动..." + driver.toString());
            return driver;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 开启一个异步任务,获取所有已连接的USB设备以及驱动,并进行驱动配置.
     *
     * @return 首个配置成功的设备驱动
     */
    class GetUsbTask implements Callable<UsbSerialDriver> {
        private UsbSerialDevice[] targetDevices;

        public GetUsbTask(UsbSerialDevice... usd) {
            targetDevices = usd;
        }

        @Override
        // 1: 遍历当前所有USB设备
        //      2. 查找目标设备
        //          3. 遍历目标设备的每个驱动
        //          3.1 如果驱动初始化成功..返回该驱动
        public UsbSerialDriver call() throws SecurityException {
            Log.i(TAG, "当前的USB设备有" + mUsbManager.getDeviceList().size() + "个");
            for (final UsbDevice curDevice : mUsbManager.getDeviceList().values()) {
                for (UsbSerialDevice targetDevice : targetDevices) {
                    if ((curDevice.getVendorId() == targetDevice.getVID() && curDevice.getProductId() == targetDevice.getPID())) {
                        Log.i(TAG, "找到目标USB设备: " + curDevice.getDeviceName());

                        final List<UsbSerialDriver> drivers = UsbSerialProber.probeSingleDevice(mUsbManager, curDevice);

                        if (!drivers.isEmpty()) {
                            for (UsbSerialDriver driver : drivers) {
                                if (initDriver(driver)) {
                                    Log.i(TAG, "(＾－＾) USB设备连接成功,驱动配置成功");
                                    return driver;
                                }
                            }
                        }
                        Log.i(TAG, curDevice.getDeviceName() + " 驱动配置失败...");
                    }
                }
            }
            return null;
        }
    }


    /**
     * 初始化(打开,设置属性:波特率等)目标驱动
     */
    private boolean initDriver(UsbSerialDriver driver) {
        Log.i(TAG, "initDriver...");
        if (driver == null) {
            return false;
        }
        try {
            driver.open();
            driver.setParameters(115200, 8, UsbSerialDriver.STOPBITS_1, UsbSerialDriver.PARITY_NONE);
        } catch (IOException e) {
            try {
                driver.close();
            } catch (IOException e2) {
                return false;
            }
            return false;
        }
        return true;
    }

}
