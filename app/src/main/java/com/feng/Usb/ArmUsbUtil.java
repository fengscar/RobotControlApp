package com.feng.Usb;

import com.feng.Constant.ArmProtocol;
import com.feng.Database.ArmDataManager;
import com.feng.Database.MapDatabaseHelper;
import com.feng.Database.Node;
import com.feng.Utils.L;
import com.feng.Utils.Transfer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by fengscar on 2016/5/24.
 */
public class ArmUsbUtil extends ArmUsbManager implements ArmProtocol {

    private final static String LOG = ArmUsbUtil.class.getSimpleName();

    private Transfer mTransfer = new Transfer();

    private static ArmUsbUtil instance = null;

    private ArmUsbUtil() {
        this.connect();
    }

    public static ArmUsbUtil getInstance() {
        if (instance == null) {
            synchronized (ArmUsbUtil.class) {
                if (instance == null) {
                    instance = new ArmUsbUtil();
                }
            }
        }
        return instance;
    }


    /**
     * 根据输入参数的查询类型,通过USB向ARM发送指令
     *
     * @param queryTypeHead 查询的类型( 例如: {@link com.feng.Constant.ArmProtocol} 中的 QueryTarget
     */
    public void query(byte[] queryTypeHead) {
        send(mTransfer.packingByte(queryTypeHead, null));
    }

    /**
     * 设置机器人执行运动/停止运动
     *
     * @param setGo true: 设置运动 ;  false: 设置为停止
     */
    public void setMove(boolean setGo) {
        if (setGo) {
            send(mTransfer.packingByte(SetMoving, new byte[]{(byte) 0x01}));
        } else {
            send(mTransfer.packingByte(SetMoving, new byte[]{(byte) 0x00}));
        }
    }


    /**
     * 发送添加该任务的byte[]请求给ARM
     */
    public void addTarget(int[] nodeIDs) {
        byte[] protocolNodeData = new byte[nodeIDs.length * 2 + 1];
        protocolNodeData[0] = (byte) nodeIDs.length;
        for (int i = 0; i < nodeIDs.length; i++) {
            System.arraycopy(mTransfer.intTo2Byte(nodeIDs[i]), 0, protocolNodeData, 1 + i * 2, 2);
        }
        send(mTransfer.packingByte(AddTargets, protocolNodeData));
    }

    public void addTarget(int nodeID) {
        int[] nodeIDs = new int[]{nodeID};
        addTarget(nodeIDs);
    }

    public void addTarget(Node node) {
        addTarget(node.getId());
    }

    public void addTarget(List<Node> nodeList) {
        if (nodeList != null && nodeList.size() > 0) {
            int[] nodeIDs = new int[nodeList.size()];
            for (int i = 0; i < nodeList.size(); i++) {
                nodeIDs[i] = nodeList.get(i).getId();
            }
            addTarget(nodeIDs);
        }
    }

    /**
     * 执行操作的函数
     *
     * @param nodeIDs
     */
    public void delTarget(int[] nodeIDs) {
        byte[] protocolNodeData = new byte[nodeIDs.length * 2 + 1];
        protocolNodeData[0] = (byte) nodeIDs.length;
        for (int i = 0; i < nodeIDs.length; i++) {
            System.arraycopy(mTransfer.intTo2Byte(nodeIDs[i]), 0, protocolNodeData, 1 + i * 2, 2);
        }
        send(mTransfer.packingByte(DelTargets, protocolNodeData));
    }

    public void delTarget(int nodeID) {
        int[] nodeIDs = new int[]{nodeID};
        delTarget(nodeIDs);
    }

    public void delTarget(Node node) {
        delTarget(node.getId());
    }

    public void delTarget(List<Node> nodeList) {
        if (nodeList != null && nodeList.size() > 0) {
            int[] nodeIDs = new int[nodeList.size()];
            for (int i = 0; i < nodeList.size(); i++) {
                nodeIDs[i] = nodeList.get(i).getId();
            }
            delTarget(nodeIDs);
        }
    }


    /**
     * 将协议中的 增加/删除任务的请求 由byte[] 转换成 nodeList
     *
     * @param fromArmBytes
     * @return
     */
    public List<Node> getNodesFromByte(byte[] fromArmBytes) {
        MapDatabaseHelper mDatabaseHelper = MapDatabaseHelper.getInstance();
        List<Node> result = new ArrayList<>();
        byte[] setOrDelData = mTransfer.getData(fromArmBytes, 1);
        if (setOrDelData == null) {
            return Collections.EMPTY_LIST;
        }
        for (int i = 0; i < setOrDelData.length / 2; i++) {
            int nodeID = mTransfer.byteToInt(new byte[]{setOrDelData[i * 2], setOrDelData[i * 2 + 1]});
            Node node = mDatabaseHelper.getNodeByID(nodeID);
            if (node == null) {
                L.e(LOG, "获取节点信息时出错");
                continue;
            }
            result.add(node);
        }
        return result;
    }

    @Override
    public void disconnect() {
        super.disconnect();
        // 释放资源
        instance = null;
    }

    /**
     * 获取要发送的 完整地图数据包 （ list中每个byte数组 都是一条数据）
     */
    public void saveMap() {
        for (byte[] onePackage : new ArmDataManager().getMapData()) {
            send(onePackage);
        }
    }
}
