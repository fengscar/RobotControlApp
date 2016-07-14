package com.feng;

import android.app.Application;
import android.content.Context;
import com.feng.Database.MapDatabaseHelper;
import com.feng.Schedule.ScheduleClient;
import com.feng.Usb.ArmUsbUtil;
import com.feng.Utils.L;

/**
 * @author 福建省和创伟业智能科技有限公司
 * @创建时间 2015-11-17 下午4:52:20
 * @功能 作为工具类 -方便其他类获取Context
 */
public class RobotApplication extends Application {
    private final static String LOG = RobotApplication.class.getSimpleName();

    private static Context context;
    private static ArmUsbUtil sArmUsbUtil;
    private static ScheduleClient sScheduleClient;

    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();

        L.i(LOG, "App启动: 初始化Usb通信V1.0");
        sArmUsbUtil = ArmUsbUtil.getInstance();
        sScheduleClient = ScheduleClient.getInstance();

//		LogcatHelper.getInstance(context).start();
    }

    public static ArmUsbUtil getArmUsbUtil() {
        if (sArmUsbUtil == null) {
            L.i(LOG, "重新获取USB连接对象");
            sArmUsbUtil = ArmUsbUtil.getInstance();
        }
        return sArmUsbUtil;
    }

    public static ScheduleClient getScheduleClient() {
        if (sScheduleClient == null) {
            L.i(LOG, "重新连接到调度系统");
            sScheduleClient = ScheduleClient.getInstance();
        }
        return sScheduleClient;
    }

    @Override
    public void onTerminate() {
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
        sArmUsbUtil.disconnect();
        sArmUsbUtil = null;

        sScheduleClient.close();
        sScheduleClient = null;

        MapDatabaseHelper db = MapDatabaseHelper.getInstance();
        db.closeDatabase();
    }
}
