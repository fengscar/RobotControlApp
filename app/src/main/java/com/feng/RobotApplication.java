package com.feng;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import com.feng.Database.Map.MapDatabaseHelper;
import com.feng.Schedule.ScheduleClient;
import com.feng.Usb.ArmHandler.BaseHandler;
import com.feng.Usb.ArmUsbManager;
import com.feng.Utils.L;

/**
 * @author 福建省和创伟业智能科技有限公司
 * @创建时间 2015-11-17 下午4:52:20
 * @功能 作为工具类 -方便其他类获取Context
 */
public class RobotApplication extends Application {
    private final static String TAG = RobotApplication.class.getSimpleName();

    private static Context context;
    private static ArmUsbManager sArmUsbManager;
    private static ScheduleClient sScheduleClient;

    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();

        Log.i(TAG, "App启动: 初始化Usb通信V8.09");
        sArmUsbManager = ArmUsbManager.getInstance();
        sArmUsbManager.connect();

        sScheduleClient = ScheduleClient.getInstance();

//		LogcatHelper.getInstance(context).start();
    }

    //region 当APP退出时,可能还缓存在内存中,再次打开不会调用onCreate(),所以在MainActivity中调用以下两个方法来 重新获取连接
    public static ArmUsbManager getArmUsbManager() {
        if (sArmUsbManager == null) {
            L.i(TAG, "重新连接到ARM");
            sArmUsbManager = ArmUsbManager.getInstance();
            if (!sArmUsbManager.isConnect()) {
                sArmUsbManager.connect();
            }
        }
        return sArmUsbManager;
    }

    public static ScheduleClient getScheduleClient() {
        if (sScheduleClient == null) {
            L.i(TAG, "重新连接到调度系统");
            sScheduleClient = ScheduleClient.getInstance();
        }
        return sScheduleClient;
    }
    //endregion

    @Override
    public void onTerminate() {
        Log.i(TAG, "onTerminate...");
        super.onTerminate();
    }

    public static Context getContext() {
        return context;
    }

    /**
     * 当系统退出时,由于onTerminate不一定会被调用,
     * 所以在mainActivity退出时,调用该函数来释放资源,关闭网络连接.
     */
    public void quit() {
        if (sArmUsbManager != null) {
            sArmUsbManager.disconnect();
            sArmUsbManager = null;
        }

        if (sScheduleClient != null) {
            sScheduleClient.close();
            sScheduleClient = null;
        }

        BaseHandler.releaseUsbManager();

        MapDatabaseHelper.getInstance().closeDatabase();
    }
}
