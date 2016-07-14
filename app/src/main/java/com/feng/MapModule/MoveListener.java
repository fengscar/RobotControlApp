package com.feng.MapModule;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import com.feng.RobotApplication;

/**
 * Created by fengscar on 2016/7/4.
 */
public class MoveListener implements View.OnTouchListener {
    private static final String LOG = MoveListener.class.getSimpleName();

    public interface MoveCallback {
        void onMoveEnd(MotionEvent event);

        void onMoving(MotionEvent event);
    }

    private MoveCallback mMoveCallback;

    public MoveCallback getMoveCallback() {
        return mMoveCallback;
    }

    public void setMoveCallback(MoveCallback moveCallback) {
        mMoveCallback = moveCallback;
    }

    public MoveListener() {
        //默认为全屏
        DisplayMetrics dm = RobotApplication.getContext().getResources().getDisplayMetrics();
        mParentWidth = dm.widthPixels;
        mParentHeight = dm.heightPixels;
    }

    public MoveListener(int parentWidth, int parentHeight) {
        mParentWidth = parentWidth;
        mParentHeight = parentHeight;
    }

    // 父容器的长宽
    private int mParentWidth, mParentHeight;

    public int getParentHeight() {
        return mParentHeight;
    }

    public void setParentHeight(int parentHeight) {
        mParentHeight = parentHeight;
    }

    public int getParentWidth() {
        return mParentWidth;
    }

    public void setParentWidth(int parentWidth) {
        mParentWidth = parentWidth;
    }

    //内置的用来保存上个位置的坐标
    private int lastX, lastY;

    public int getLastX() {
        return lastX;
    }

    public void setLastX(int lastX) {
        this.lastX = lastX;
    }

    public int getLastY() {
        return lastY;
    }

    public void setLastY(int lastY) {
        this.lastY = lastY;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mParentHeight == 0 || mParentWidth == 0) {
            Log.e(LOG, "未设置长宽");
            v.setOnTouchListener(null);
            return true;
        }
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                lastX = (int) event.getRawX();
                lastY = (int) event.getRawY();
                break;
            /**
             * layout(l,t,r,b)
             * l  Left position, relative to parent
             t  Top position, relative to parent
             r  Right position, relative to parent
             b  Bottom position, relative to parent
             * */
            case MotionEvent.ACTION_MOVE:
                int dx = (int) event.getRawX() - lastX;
                int dy = (int) event.getRawY() - lastY;

                int left = v.getLeft() + dx;
                int top = v.getTop() + dy;
                int right = v.getRight() + dx;
                int bottom = v.getBottom() + dy;
                if (left < 0) {
                    left = 0;
                    right = left + v.getWidth();
                }
                if (right > mParentWidth) {
                    right = mParentWidth;
                    left = right - v.getWidth();
                }
                if (top < 0) {
                    top = 0;
                    bottom = top + v.getHeight();
                }
                if (bottom > mParentHeight) {
                    bottom = mParentHeight;
                    top = bottom - v.getHeight();
                }
                v.layout(left, top, right, bottom);
                lastX = (int) event.getRawX();
                lastY = (int) event.getRawY();

                    if (mMoveCallback != null) {
                    mMoveCallback.onMoving(event);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mMoveCallback != null) {
                    mMoveCallback.onMoveEnd(event);
                }
                //取消绑定...
                v.setOnTouchListener(null);
                break;
        }
        // 默认消费掉该touch事件,后续的将不再触发(比如view的 click...longClick)
        return true;
    }
}
