package com.feng.MapModule;

import android.content.Context;
import android.graphics.*;
import android.renderscript.Int2;
import android.view.View;
import com.feng.Database.MapDatabaseHelper;
import com.feng.Database.Route;
import com.feng.Utils.L;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 自定义的VIEW  , 添加在 LAYOUT的最上层
 * 内容包括 :
 * 1. 可以使用的路线 　：　显示为灰色（　不包括 禁用的路线 )
 * 2. 当前所选的路线 	  :    显示动画( 从起点循环移动到
 *
 * @author 福建省和创伟业智能科技有限公司
 * @创建时间 2015-12-7 下午8:18:01
 * @功能
 */
public class PathView extends View {
    private final static String LOG = PathView.class.getSimpleName();
    private final CornerPathEffect cornerPe;

    // 路径移动效果的偏移量( DashPathEffect)
    private float phase = 0;

    private float mMainPathWidth = 20f;
    // 主线路的动态效果
    private DashPathEffect dashPathEffect;

    /**
     * 设置当前路线的宽度
     *
     * @param mainPathWidth 当前路线的宽度
     */
    public void setMainPathWidth(float mainPathWidth) {
        mMainPathWidth = mainPathWidth;
    }

    //各类路线的画笔: 当前路线, 其他路线, 禁用路线
    private Paint currentPaint, usablePaint, uselessPaint;
    // 转弯角的半径大小( 单位为像素) 默认60
    private int mRadius = 60;

    // path的偏移量以及 缩放比
    private int mOffsetX = 0, mOffsetY = 0;
    private float mScaleX = 1.0f, mScaleY = 1.0f;


    private Map<Integer, Path> allPaths;
    private int mCurrentRouteID = -1;
    private List<Integer> usablePathIdList;
    private List<Integer> uselessPathIdList;

    //region Getter/Setter
    public int getRadius() {
        return mRadius;
    }

    public void setRadius(int radius) {
        mRadius = radius;
    }

    public void setUselessPathIdList(List<Integer> uselessPathIdList) {
        this.uselessPathIdList = uselessPathIdList;
    }

    /**
     * 路线的切换可以使用该方法.如果路线数据变化.请重新初始化,使用 initData(routeID);
     *
     * @param currentRouteID
     */
    public void setCurrentRouteID(int currentRouteID) {
        this.mCurrentRouteID = currentRouteID;
    }

    public void setUsablePathIdList(List<Integer> usablePathIdList) {
        this.usablePathIdList = usablePathIdList;
    }

    public void setScale(float scaleX, float scaleY) {
        mScaleX = scaleX;
        mScaleY = scaleY;
    }

    public void setOffset(int offsetX, int offsetY) {
        mOffsetX = offsetX;
        mOffsetY = offsetY;
    }
    //endregion

    public PathView(Context cx) {
        super(cx);
        initPaint();
        allPaths = new HashMap<>();
        usablePathIdList = new ArrayList<>();
        uselessPathIdList = new ArrayList<>();
        cornerPe = new CornerPathEffect(10);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 根据当前缩放比和偏移量设置画布.. onDraw执行完后复原.
        canvas.save();
//        canvas.scale(mScaleX, mScaleY);
        canvas.translate(mOffsetX, mOffsetY);

        if (allPaths == null || allPaths.size() == 0) {
            return;
        }
        // 绘出可用的Path ( 包括当前路线)
        for (Integer integer : usablePathIdList) {
            canvas.drawPath(allPaths.get(integer), usablePaint);
        }
        // 绘出不可用的Path
        for (Integer integer : uselessPathIdList) {
            canvas.drawPath(allPaths.get(integer), uselessPaint);
        }
        // 汇出当前路线
        if (mCurrentRouteID <= 0) {
            return;
        }
        Path currentPath = allPaths.get(mCurrentRouteID);
        if (currentPath == null) {
            return;
        }
        dashPathEffect = new DashPathEffect(new float[]{15, 35}, phase);
        // 组合2种效果
        ComposePathEffect cpe = new ComposePathEffect(dashPathEffect, cornerPe);
        currentPaint.setPathEffect(cpe);
        canvas.drawPath(currentPath, currentPaint);

        // 给PATH添加动态效果
        if (phase < -1000) {
            phase = -1;
        }
        phase -= 3;

//        canvas.restore();
    }

    // 当一条路线上的路径发生改变时, 刷新该路径信息
    public void refreshRoute(int routeID) {
        Path path = PathManager.getRouteGraph(routeID, mRadius);
        if (path == null) {
            return;
        }
        allPaths.remove(routeID);
        allPaths.put(routeID, path);
    }

    private void drawOrderText(Canvas canvas, float k, Int2 startPoint,
                               Int2 endPoint, int orderID) {
        int offsetX = 0;
        int offsetY = 0;
        //在箭头处 标号TEXT
        if (Math.abs(k) < 0.8) {
            offsetX = endPoint.x > startPoint.x ? 50 : -50;
            // 比较 平缓时 在下方标号
        } else {
            // 比较 陡时 ,标号 上下移动
            offsetY = endPoint.y > startPoint.y ? 50 : -50;
        }
        canvas.drawText(String.valueOf(orderID), (endPoint.x + startPoint.x) / 2 - offsetX,
                (endPoint.y + startPoint.y) / 2 + offsetY, currentPaint);

    }

    private void initPaint() {

        currentPaint = new Paint();
        currentPaint.setAntiAlias(true);
        currentPaint.setStrokeWidth(mMainPathWidth);
        currentPaint.setStyle(Paint.Style.STROKE);//设置为空心
        currentPaint.setColor(Color.rgb(54, 194, 242));
        currentPaint.setStrokeCap(Paint.Cap.ROUND);


        usablePaint = new Paint();
        usablePaint.setAntiAlias(true);
        usablePaint.setStrokeWidth((float) (mMainPathWidth * 0.618));
        usablePaint.setStyle(Paint.Style.STROKE);//设置为空心
        usablePaint.setStrokeCap(Paint.Cap.ROUND);
        usablePaint.setPathEffect(cornerPe);
        //设置为 浅黑色
        usablePaint.setColor(Color.argb(200, 102, 102, 102));


        uselessPaint = new Paint();
        uselessPaint.setAntiAlias(true);
        uselessPaint.setStyle(Paint.Style.STROKE);//设置为空心
        uselessPaint.setStrokeWidth((float) (mMainPathWidth * 0.618 * .618));
        uselessPaint.setStrokeCap(Paint.Cap.ROUND);
        // 设置为浅灰色
        uselessPaint.setColor(Color.argb(100, 204, 204, 204));
        uselessPaint.setPathEffect(cornerPe);

    }

    /***
     * @param canvas
     * @param paint
     * @param startX 起始点
     * @param startY
     * @param endX   终点
     * @param endY
     * @param angle  旋转角度 左转为负, 右转为正
     */
    private void drawArc(Canvas canvas, Paint paint, float startX, float startY, float endX, float endY, float angle) {
        float rectHeight = Math.abs(startY - endY);
        float rectWidth = Math.abs(startX - endX);

        RectF mRectF = new RectF();
        float startAngle = 0;
        float minX = Math.min(startX, endX);
        float minY = Math.min(startY, endY);
        float maxX = Math.max(startX, endX);
        float maxY = Math.max(startY, endY);

        if ((endX > startX && endY > startY && angle > 0) || (endX < startX && endY < startY && angle < 0)) {
            // 原矩形 处在1 象限
            startAngle = 270;
            mRectF.set(minX - rectWidth, minY, maxX, maxY + rectHeight);
        } else if ((endX > startX && endY < startY && angle < 0) || (endX < startX && endY > startY && angle > 0)) {
            // 原矩形 处在 2 象限
            startAngle = 180;
            mRectF.set(minX, minY, maxX + rectWidth, maxY + rectHeight);

        } else if ((endX > startX && endY > startY && angle < 0) || (endX < startX && endY < startY && angle > 0)) {
            //第三象限
            startAngle = 90;
            mRectF.set(minX, minY - rectHeight, maxX + rectWidth, maxY);

        } else if ((endX > startX && endY < startY && angle > 0) || (endX < startX && endY > startY && angle < 0)) {
            //第四象限
            startAngle = 0;
            mRectF.set(minX - rectWidth, minY - rectHeight, maxX, maxY);
        }
        // 固定画 1/4个圆 ,所以 sweepAngle固定为 90度
        canvas.drawArc(mRectF, startAngle, 90, false, paint);
    }

    public void initData(int workspaceID) {
        initData(workspaceID, -1);
    }

    public void initData(int workspaceID, int currentRouteID) {
        try {
            allPaths = PathManager.getWholeWorkspace(workspaceID, mRadius);
            if (currentRouteID > 0) {
                this.mCurrentRouteID = currentRouteID;
            }
            List<Integer> usableRoutesID = new ArrayList<>();
            List<Integer> uselessRoutesID = new ArrayList<>();
            List<Route> routeList = MapDatabaseHelper.getInstance().getAllRoute(workspaceID);
            for (Route route : routeList) {
                if (route.isEnabled()) {
                    usableRoutesID.add(route.getId());
                } else {
                    uselessRoutesID.add(route.getId());
                }
            }
            this.setUsablePathIdList(usableRoutesID);
            this.setUselessPathIdList(uselessRoutesID);
        } catch (Exception e) {
            L.e(e.toString());
        }
    }
}





