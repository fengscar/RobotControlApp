package com.feng.Utils;

import android.renderscript.Int2;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by fengscar on 2016/7/5.
 */

public class WidgetController {
    private final static String LOG = WidgetController.class.getSimpleName();

    /**
     * 当控件的属性为 al_most( wrap_content) 时,手动measure并获取该控件的大小
     *
     * @return Int2(控件的测量宽度, 控件的测量高度)
     */
    public static Int2 getViewMeasure(View v) throws IllegalArgumentException {
        // @spec :  space requirements as imposed by the
        int specX = View.MeasureSpec.makeMeasureSpec((1 << 30) - 1, View.MeasureSpec.AT_MOST);
        int specY = View.MeasureSpec.makeMeasureSpec((1 << 30) - 1, View.MeasureSpec.AT_MOST);

        // 避免重复measure
        if (v.getMeasuredWidth() == 0 && v.getMeasuredHeight() == 0) {
            v.measure(specX, specY);
        }
        return new Int2(v.getMeasuredWidth(), v.getMeasuredHeight());
    }

    /**
     * 设置 控件在父容器中的位置 (参数坐标点为控件的左上角)
     *
     * @param v
     * @param x
     * @param y
     */
    public static void setViewLayout(View v, int x, int y) {
        FrameLayout.LayoutParams flp = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        flp.leftMargin = x;
        flp.topMargin = y;
        v.setLayoutParams(flp);
    }

    /**
     * 设置 控件在父容器中的位置 (参数坐标点为控件的中心)
     *
     * @param v
     * @param x
     * @param y
     */
    public static void setViewLayoutInCenter(View v, int x, int y) {
        try {
            int vWidth = getViewMeasure(v).x;
            int vHeight = getViewMeasure(v).y;
            setViewLayout(v, x - vWidth / 2, y - vHeight / 2);
        } catch (IllegalArgumentException e) {
            Log.e(LOG, e.toString());
        }
    }
}
