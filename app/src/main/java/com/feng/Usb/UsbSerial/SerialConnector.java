package com.feng.Usb.UsbSerial;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.support.annotation.NonNull;
import android.util.Log;
import com.feng.RobotApp;
import com.feng.Usb.UsbSerial.UsbSerialDevice;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * Created by fengscar on 2016/8/1.
 * 功能: 查找当前USB设备的驱动...并返回已成功连接的驱动
 */
public class SerialConnector {
    private static final String TAG = "SerialConnector";

    private SerialConnector() {
    }

    /**
     * 查找 参数列表中的 USB转串口设备的驱动.
     *
     * @return 如果没有找到, 返回null;如果有多个匹配的设备,返回第一个;
     */
    public static UsbSerialPort getUsbPort(UsbSerialDevice... targetDevices) throws SecurityException {
        try {
            FutureTask<List<UsbSerialPort>> futureTask = new FutureTask<>(new GetUsbPortsTask(targetDevices));
            futureTask.run();
            while (!futureTask.isDone()) {
                Thread.sleep(500);
                Log.i(TAG, "getUsbPort: 正在获取驱动中...");
            }
            List<UsbSerialPort> usbSerialPorts = futureTask.get();
            if (usbSerialPorts == null || usbSerialPorts.size() == 0) {
                Log.w(TAG, "getUsbPort: 获取驱动失败: 未找到UsbSerialPort");
                return null;
            }
            Log.i(TAG, "获取到驱动..." + usbSerialPorts.toString());
            return usbSerialPorts.get(0);
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
    static class GetUsbPortsTask implements Callable<List<UsbSerialPort>> {
        private UsbSerialDevice[] targetDevices;

        public GetUsbPortsTask(@NonNull UsbSerialDevice... usd) {
            targetDevices = usd;
        }

        @Override
        // 1: 遍历当前所有USB设备
        //      2. 查找目标设备
        //          3. 遍历目标设备的每个驱动
        //          3.1 如果驱动初始化成功..返回该驱动
        public List<UsbSerialPort> call() throws SecurityException {
            UsbManager mUsbManager = (UsbManager) RobotApp.getContext().getSystemService(Context.USB_SERVICE);
            final List<UsbSerialDriver> drivers = UsbSerialProber.getDefaultProber().findAllDrivers(mUsbManager);

            final List<UsbSerialPort> result = new ArrayList<>();
            for (final UsbSerialDriver driver : drivers) {
                final List<UsbSerialPort> ports = driver.getPorts();
                Log.d(TAG, String.format("%s: %s port%s", driver, ports.size(), ports.size() == 1 ? "" : "s"));
                UsbDevice curDevice = driver.getDevice();
                for (UsbSerialDevice targetDevice : targetDevices) {
                    if ((curDevice.getVendorId() == targetDevice.getVID() && curDevice.getProductId() == targetDevice.getPID())) {
                        Log.i(TAG, "找到目标USB设备: " + curDevice.getDeviceName());
                    }
                }
                result.addAll(driver.getPorts());
            }
            return result;
        }
    }
}
