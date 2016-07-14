/**
 * @author 福建省和创伟业智能科技有限公司
 * @创建时间 2016-3-4 上午9:20:21
 */
package com.feng.CustomView;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.Button;
import com.feng.Utils.L;

public class IconButton extends Button {
    private final static String LOG = IconButton.class.getSimpleName();

    public static final int LEFT = 0;
    public static final int TOP = 1;
    public static final int RIGHT = 2;
    public static final int BOTTOM = 3;

    private Drawable mIcon;
    private int mPosition = -1;
    private int mIconWidth = -1;
    private int mIconHeight = -1;
    private int mTextSize;

    public void setIconSize(int iconWidth, int iconHeight) {
        mIconWidth = iconWidth;
        mIconHeight = iconHeight;
    }

    public void setIconPosition(int position) {
        if (position < 0 || position > 3) {
            throw new IllegalArgumentException();
        }
        mPosition = position;
    }

    public void setIconDrawableID(int drawableID) {
        try {
            this.setIconDrawable(getResources().getDrawable(drawableID));
        } catch (Resources.NotFoundException e) {
            L.e(LOG, e.toString());
        }
    }

    public void setIconDrawable(Drawable drawable) {
        // 如果是从 XML 中设置的, 则 mPosition为-1 , 获取当前的drawable的位置
        if (mPosition == -1) {
            Drawable[] drawables = getCompoundDrawables();
            for (int i = 0; i < drawables.length; i++) {
                if (drawables[i] != null) {
                    // 排序 为 L,T,R,B
                    mPosition = i;
                }
            }
            // 如果XML中也没有设置mPostion, 仍为-1
            if (mPosition == -1) {
                throw new Resources.NotFoundException("Unknown Icon position!");
            }
        }
        mIcon = drawable;

        // 如果有自定义图标大小
        if (mIconWidth != -1 && mIconHeight != -1) {
            mIcon.setBounds(0, 0, mIconWidth, mIconHeight);
        } else {
            mIcon.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        }

        switch (mPosition) {
            case LEFT:
                setCompoundDrawables(mIcon, null, null, null);
                break;
            case TOP:
                setCompoundDrawables(null, mIcon, null, null);
                break;
            case RIGHT:
                setCompoundDrawables(null, null, mIcon, null);
                break;
            case BOTTOM:
                setCompoundDrawables(null, null, null, mIcon);
                break;
        }

    }

    public IconButton(Context context) {
        super(context);
    }

    public IconButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public IconButton(Context context, int iconPosition, int drawableID) {
        super(context);
        setIconPosition(iconPosition);
        setIconDrawableID(drawableID);
    }

    public int getIconWidth() {
        return mIconWidth;
    }

    public int getIconHeight() {
        return mIconHeight;
    }

}
