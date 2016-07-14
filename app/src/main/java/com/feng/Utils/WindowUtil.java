package com.feng.Utils;

import android.content.Context;
import android.graphics.Point;
import android.view.WindowManager;
import com.feng.RobotApplication;

/**
 * Created by fengscar on 2016/7/13.
 */
public class WindowUtil {
    public static Point getScreenSize() {
        WindowManager wm = (WindowManager) RobotApplication.getContext().getSystemService(Context.WINDOW_SERVICE);
        Point size = new Point();
        wm.getDefaultDisplay().getSize(size);
        return size;
    }
}
