package com.feng.CustomView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import com.feng.Utils.L;

/**
 * Created by fengscar on 2016/7/7.
 */
public class CustomLinearLayout extends LinearLayout {
    final String LOG = CustomLinearLayout.class.getSimpleName();

    public CustomLinearLayout(Context context) {
        super(context);
    }

    public CustomLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        L.i(LOG, "onInterceptTouchEvent");
        return super.onInterceptTouchEvent(ev);
    }

}
