package com.feng.MapModule;

import android.graphics.Path;
import android.graphics.RectF;
import android.util.Log;
import com.feng.Database.MapDatabaseHelper;
import com.feng.Database.Node;
import com.feng.Database.Route;
import com.feng.Utils.L;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by fengscar on 2016/7/6.
 */
public class PathManager {
    private final static String LOG = PathManager.class.getSimpleName();

    public static Map<Integer, Path> getWholeWorkspace(int workspaceID) {
        return getWholeWorkspace(workspaceID, 60);
    }

    public static Map<Integer, Path> getWholeWorkspace(int workspaceID, int radius) {
        MapDatabaseHelper mapDatabaseHelper = MapDatabaseHelper.getInstance();
        List<Route> allRoute = mapDatabaseHelper.getAllRoute(workspaceID);
        Map<Integer, Path> result = new HashMap<>();
        if (allRoute == null) {
            return result;
        }
        for (Route route : allRoute) {
            result.put(route.getId(), getRouteGraph(route.getId(), radius));
        }
        return result;
    }

    public static Path getRouteGraphPath(int routeID) {
        return getRouteGraph(routeID, 60);
    }

    /**
     * 根据数据库的 route->path->node->position ,生成画图上的一个path
     *
     * @param routeID 数据库中的路线ID
     * @param Radius  最大的半径( 默认为60像素)
     * @return
     */
    public static Path getRouteGraph(int routeID, int Radius) {
        MapDatabaseHelper mapDatabaseHelper = MapDatabaseHelper.getInstance();
        List<com.feng.Database.Path> pathList = mapDatabaseHelper.getWholeRoute(routeID);
        if (pathList == null) {
            return null;
        }
        // 将Node 与ID绑定, 并保存到map中.避免多次查找
        Map<Integer, Node> nodeMap = mapDatabaseHelper.getNodeMap(pathList);

        Path path = new Path();
        for (com.feng.Database.Path mapPath : pathList) {
            Node startNode = nodeMap.get(mapPath.getNodeID());
            Node endNode = nodeMap.get(mapPath.getEndNode());
            if (startNode == null || endNode == null) {
                L.e(LOG, "生成路径时出错: 无法获取节点信息");
                continue;
            }
            //直线
            if (mapPath.getYaw() == 0) {
                Path linePath = new Path();
                linePath.moveTo(startNode.getPositionX(), startNode.getPositionY());
                linePath.lineTo(endNode.getPositionX(), endNode.getPositionY());
                path.addPath(linePath);
            } else {
                // 带转弯的path
                path.addPath(
                        getArcPath(
                                startNode.getPositionX(), startNode.getPositionY(),
                                endNode.getPositionX(), endNode.getPositionY(),
                                Radius, mapPath.getYaw() < 0));
            }

        }
        return path;
    }

    /**
     * 根据 起始点+ 终点 , 生成2点之间的弧线
     * 弧线为 参数半径的90度圆弧..
     * 分两种情况
     * 1.如果两点之间距离比圆弧两端距离大. 则用 line补足
     * 2.如果比圆弧更小,则修改圆弧的大小( 半径为交小的那个点到两点水平/垂直延长线的交点)
     *
     * @param startX   起始点x
     * @param startY   起始点y
     * @param endX     终点x
     * @param endY     终点y
     * @param turnLeft 是否左转, true为左转,false为右转
     * @param radius   圆弧半径
     * @return 生成的path..可以与之间的path add,然后通过canvas画出来
     */
    public static Path getArcPath(int startX, int startY, int endX, int endY, int radius, boolean turnLeft) {
        Log.d(LOG, "起点: [" + startX + "," + startY + "],终点: [" + endX + "," + endY + "],半径:" + radius + (turnLeft ? "--左转" : "--右转"));
        //终点减去起点 x,y坐标之间的差 ( 带正负)
        int subX = endX - startX;
        int subY = endY - startY;
        if (subX == 0 || subY == 0) {
            Log.e(LOG, "两点水平或者垂直...无法画弧,已经替换成直线");
            Path pHorizontal = new Path();
            pHorizontal.moveTo(startX, startY);
            pHorizontal.lineTo(endX, endY);
            return pHorizontal;
        }
        // 最终要返回的Path
        Path path = new Path();
        // 判断两点之间的距离( 水平方向 / 垂直方向)
        int distanceH = Math.abs(subX);
        int distanceV = Math.abs(subY);
        int lineX, lineY;
        // 获取真实的圆弧半径
        if (distanceH < radius || distanceV < radius) { //第2种情况, 需要裁减并补全
            radius = Math.min(distanceH, distanceV);
        }
        // 需要补全的 line的长度 ( lineX: x方向line的长度 , lineY同理)
        lineX = distanceH - radius;
        lineY = distanceV - radius;
        if (turnLeft) {
            // 添加x方向延长线
            if (lineX > 0) {
                Path pathX = new Path();
                // 如果 x,y坐标差同号,说明两点连线在2/4象限,则x方向连线和终点连接
                if ((subX >> 31 == subY >> 31)) {
                    pathX.moveTo(subX < 0 ? endX + lineX : endX - lineX, endY);
                    pathX.lineTo(endX, endY);
                } else {
                    pathX.moveTo(startX, startY);
                    pathX.lineTo(subX > 0 ? startX + lineX : startX - lineX, startY);
                }
                path.addPath(pathX);
            }
            // 画 弧线, 获取两点xy坐标轴上的延长线焦点.做为起始点..
            RectF arcRect = new RectF();
            if ((subX >> 31 == subY >> 31)) {
                // 第四象限
                if (subX > 0) {
                    arcRect.set(startX, endY - radius * 2, startX + radius * 2, endY);
                    path.addArc(arcRect, 180, -90);
                } else {
                    // 第二象限
                    arcRect.set(startX - radius * 2, endY, startX, endY + radius * 2);
                    path.addArc(arcRect, 0, -90);
                }
            } else {
                // 第1象限
                if (subX > 0) {
                    arcRect.set(endX - radius * 2, startY - radius * 2, endX, startY);
                    path.addArc(arcRect, 90, -90);
                } else {
                    // 第3象限
                    arcRect.set(endX, startY, endX + radius * 2, startY + radius * 2);
                    path.addArc(arcRect, 270, -90);
                }
            }
            // 添加Y方向延长线
            if (lineY > 0) {
                Path pathY = new Path();
                if ((subX >> 31 == subY >> 31)) {
                    pathY.moveTo(startX, startY);
                    pathY.lineTo(startX, subY < 0 ? startY - lineY : startY + lineY);
                } else {
                    pathY.moveTo(endX, subY > 0 ? endY - lineY : endY + lineY);
                    pathY.lineTo(endX, endY);
                }
                path.addPath(pathY);
            }
        } else {
            // 添加x方向延长线
            if (lineX > 0) {
                Path pathX = new Path();
                // 如果 x,y坐标差同号,说明两点连线在2/4象限,则x方向连线和终点连接
                if ((subX >> 31 == subY >> 31)) {
                    pathX.moveTo(startX, startY);
                    pathX.lineTo(subX < 0 ? startX - lineX : startX + lineX, startY);
                } else {
                    pathX.moveTo(subX > 0 ? endX - lineX : endX + lineX, endY);
                    pathX.lineTo(endX, endY);
                }
                path.addPath(pathX);
            }
            // 画 弧线, 获取两点xy坐标轴上的延长线焦点.做为起始点..
            RectF arcRect = new RectF();
            if ((subX >> 31 == subY >> 31)) {
                // 第四象限
                if (subX > 0) {
                    arcRect.set(endX - radius * 2, startY, endX, startY + radius * 2);
                    path.addArc(arcRect, 270, 90);
                } else {
                    // 第二象限
                    arcRect.set(endX, startY - radius * 2, endX + radius * 2, startY);
                    path.addArc(arcRect, 90, 90);
                }
            } else {
                // 第1象限
                if (subX > 0) {
                    arcRect.set(startX, endY, startX + radius * 2, endY + radius * 2);
                    path.addArc(arcRect, 180, 90);
                } else {
                    // 第3象限
                    arcRect.set(startX - radius * 2, endY - radius * 2, startX, endY);
                    path.addArc(arcRect, 0, 90);
                }
            }
            // 添加Y方向延长线
            if (lineY > 0) {
                Path pathY = new Path();
                if ((subX >> 31 == subY >> 31)) {
                    pathY.moveTo(endX, subY > 0 ? endY - lineY : endY + lineY);
                    pathY.lineTo(endX, endY);
                } else {
                    pathY.moveTo(startX, startY);
                    pathY.lineTo(startX, subY < 0 ? startY - lineY : startY + lineY);

                }
                path.addPath(pathY);
            }
        }
        return path;
    }
}
