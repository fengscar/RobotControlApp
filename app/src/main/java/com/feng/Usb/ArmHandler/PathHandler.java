package com.feng.Usb.ArmHandler;

import com.feng.Database.ArmDataManager;
import com.feng.Database.MapDatabaseHelper;
import com.feng.Database.Node;
import com.feng.Usb.ArmHead;
import com.feng.Usb.ArmModel.ArmPath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by fengscar on 2016/8/1.
 */
public class PathHandler extends BaseHandler {
    private static final String TAG = "PathHandler";

    //region Singleton
    public static PathHandler getInstance() {
        // 这里必须是全局路径 否则无法找到
        return (PathHandler) instance(PathHandler.class.getName());
    }
    //endregion

    //region Members

    private int mRouteVersion;
    private Node mCurrentQueryCard; //当前查询到的卡号
    private int[] mTasks; //当前任务
    private int[] mPaths; // 当前路线


    //endregion

    //region Getter

    public int getRouteVersion() {
        return mRouteVersion;
    }

    public Node getCurrentQueryCard() {
        return mCurrentQueryCard;
    }

    public int[] getTasks() {
        return mTasks;
    }

    public int[] getPaths() {
        return mPaths;
    }

    //endregion

    //region 主动发送命令的方法


    public void queryRouteVersion() {
        send(ArmPath.QueryRouteVersion, null);
    }

    public void queryRouteMaxPoint() {
        send(ArmPath.Query, null);
    }

    public void queryCard(int routeID, int nodeID) {
        send(ArmPath.QueryCard, mTransfer.add2Byte(new byte[]{(byte) routeID}, mTransfer.intTo2Byte(nodeID)));
    }

    public void queryTarget() {
        mArmUsbManager.send(ArmPath.QueryTarget, null);
    }

    public void updateCard() {
        send(ArmPath.UpdateCard, null);
    }

    //region 发送添加任务的请求给ARM
    public void addTargets(int[] nodeIDs) {
        byte[] protocolNodeData = new byte[nodeIDs.length * 2 + 1];
        protocolNodeData[0] = (byte) nodeIDs.length;
        for (int i = 0; i < nodeIDs.length; i++) {
            System.arraycopy(mTransfer.intTo2Byte(nodeIDs[i]), 0, protocolNodeData, 1 + i * 2, 2);
        }
        mArmUsbManager.send(ArmPath.AddTargets, protocolNodeData);
    }

    public void addTargets(int nodeID) {
        int[] nodeIDs = new int[]{nodeID};
        addTargets(nodeIDs);
    }

    public void addTargets(List<Node> nodeList) {
        if (nodeList != null && nodeList.size() > 0) {
            int[] nodeIDs = new int[nodeList.size()];
            for (int i = 0; i < nodeList.size(); i++) {
                nodeIDs[i] = nodeList.get(i).getId();
            }
            addTargets(nodeIDs);
        }
    }
    //endregion

    //region 执行删除操作的函数
    public void delTargets(int[] nodeIDs) {
        byte[] protocolNodeData = new byte[nodeIDs.length * 2 + 1];
        protocolNodeData[0] = (byte) nodeIDs.length;
        for (int i = 0; i < nodeIDs.length; i++) {
            System.arraycopy(mTransfer.intTo2Byte(nodeIDs[i]), 0, protocolNodeData, 1 + i * 2, 2);
        }
        mArmUsbManager.send(ArmPath.DelTargets, protocolNodeData);
    }

    public void delTargets(int nodeID) {
        int[] nodeIDs = new int[]{nodeID};
        delTargets(nodeIDs);
    }

    public void delTargets(List<Node> nodeList) {
        if (nodeList != null && nodeList.size() > 0) {
            int[] nodeIDs = new int[nodeList.size()];
            for (int i = 0; i < nodeList.size(); i++) {
                nodeIDs[i] = nodeList.get(i).getId();
            }
            delTargets(nodeIDs);
        }
    }
    //endregion

    public boolean setPriority(int routeCount, int[] routeOrder) {
        // TODO
        return false;
    }

    // TODO 下发路径信息,让上传路径信息,回复上传路径
    public void saveMap() {
        for (byte[] onePackage : new ArmDataManager().getMapData()) {
            sendFullData(onePackage);
        }
    }

    public boolean setStopPoint(int stopNodeID) {
        return send(ArmPath.SetStopPoint, (byte) stopNodeID).getSendState();
    }
//endregion


    @Override
    public void onReceive(byte[] fromUsbData) {
        byte[] body = mTransfer.getBody(fromUsbData);
        if (body == null) {
            return;
        }
        switch (fromUsbData[ArmHead.COMMAND]) {
            case 0x01:
                mRouteVersion = body[0];
                break;
            // TODO 暂时没用到的
            case 0x02:
            case 0x03:
                break;
            case 0x04:
                mTasks = queryTaskResult(fromUsbData);
                break;
            case 0x0d:
                mPaths = queryTaskResult(fromUsbData);
                reply(fromUsbData);
                break;
        }
    }

    public int[] queryTaskResult(byte[] fromUsbData) {
        //无任务时
        if (fromUsbData == null) {
            return null;
        }
        int[] result = new int[fromUsbData[0]];
        byte[] setOrDelData = mTransfer.getBody(fromUsbData, 1);
        if (setOrDelData == null) {
            return null;
        }
        for (int i = 0; i < setOrDelData.length / 2; i++) {
            int nodeID = mTransfer.twoByteToInt(new byte[]{setOrDelData[i * 2], setOrDelData[i * 2 + 1]});
            result[i] = nodeID;
        }
        return result;
    }


    public List<Node> getNodesFromByte(byte[] fromArmBytes) {
        MapDatabaseHelper mDatabaseHelper = MapDatabaseHelper.getInstance();
        List<Node> result = new ArrayList<>();
        byte[] setOrDelData = mTransfer.getBody(fromArmBytes, 1);
        if (setOrDelData == null) {
            return Collections.EMPTY_LIST;
        }
        for (int i = 0; i < setOrDelData.length / 2; i++) {
            int nodeID = mTransfer.twoByteToInt(new byte[]{setOrDelData[i * 2], setOrDelData[i * 2 + 1]});
            Node node = mDatabaseHelper.getNodeByID(nodeID);
            if (node == null) {
                continue;
            }
            result.add(node);
        }
        return result;
    }

}
