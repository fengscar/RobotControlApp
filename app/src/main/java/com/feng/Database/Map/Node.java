package com.feng.Database.Map;

import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import com.feng.Constant.I_MapData;
import com.feng.RSS.R;
import com.feng.Utils.T;

import java.util.List;

/**
 * @author 福建省和创伟业智能科技有限公司
 * @创建时间 2015-11-19 下午5:27:24
 * @功能 保存节点对象, 包含到下个节点的路径信息
 */
public class Node implements I_MapData {

    private int positionX; // 相对地图的像素位置
    private int positionY;

    private int id; // 节点全局编号
    private String name;
    private String RFID;
    private String type; // 节点的类型

    private int workspaceID;

    //region Constructor
    public Node() {
    }

    public Node(int iD) {
        super();
        this.id = iD;
    }

    public Node(int positionX, int positionY, int id, String name, String rFID,
                String type, int workspaceID) {
        super();
        this.positionX = positionX;
        this.positionY = positionY;
        this.id = id;
        this.name = name;
        this.RFID = rFID;
        this.type = type;
        this.workspaceID = workspaceID;
    }

    public Node(int positionX, int positionY) {
        super();
        this.positionX = positionX;
        this.positionY = positionY;
    }

    public Node(int positionX, int positionY, String name, String rFID,
                String type, int workspaceID) {
        super();
        this.positionX = positionX;
        this.positionY = positionY;
        this.name = name;
        this.RFID = rFID;
        this.type = type;
        this.workspaceID = workspaceID;
    }
    //endregion

    //region Getter/Setter
    public int getPositionX() {
        return positionX;
    }

    public void setPositionX(int positionX) {
        this.positionX = positionX;
    }

    public int getPositionY() {
        return positionY;
    }

    public void setPositionY(int positionY) {
        this.positionY = positionY;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRFID() {
        return RFID;
    }

    public void setRFID(String rFID) {
        RFID = rFID;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getWorkspaceID() {
        return workspaceID;
    }

    public void setWorkspaceID(int workspaceID) {
        this.workspaceID = workspaceID;
    }

    //endregion

    //region hashCode,equals,toString
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Node other = (Node) obj;
        if (id != other.id)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Node [positionX=" + positionX + ", positionY=" + positionY
                + ", id=" + id + ", name=" + name + ", RFID=" + RFID
                + ", type=" + type + ", workspaceID=" + workspaceID + "]";
    }
    //endregion

    /**
     * 从 编辑节点的dialog的view中获取属性生成 node对象( 并不是数据库操作)
     * // 传入的Node 有Position属性
     */
    public static Node loadNode(Node node, List<View> viewList) {
        for (View v : viewList) {
            switch (v.getId()) {
                case R.id.tvDialogNodeID:
                    node.setId(Integer.parseInt(((TextView) v).getText().toString()));
                    break;
                case R.id.etDialogNodeName:
                    node.setName(((EditText) v).getText().toString());
                    break;

                case R.id.etDialogNodeRFID:
                    String RFID = ((EditText) v).getText().toString();
                    if (RFID.length() != 4) {
                        T.show("请输入4位的RFID");
                        return null;
                    }
                    node.setRFID(RFID);
                    break;

                case R.id.spDialogNodeType:
                    node.setType(((Spinner) v).getSelectedItem().toString());
                    break;

            }
        }
        return node;
    }

}
