package com.feng.Database.Map;

import com.feng.Usb.ArmProtocol;
import com.feng.Utils.Transfer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fengscar on 2016/4/22.
 */
public class ArmDataManager implements ArmProtocol {
    private Transfer mTransfer = new Transfer();

    /*
     获取要发送的 完整地图数据包 （ list中每个byte数组 都是一条数据）
      */
    public List<byte[]> getMapData() {
        List<byte[]> mapdata = new ArrayList<>();
        List<byte[]> armRouteByte = new ArmRoutes().getArmRouteBytes();
        List<byte[]> armNodesByte = new ArmNodes().getArmNodeBytes();
        /*
            路线的包
         */
        //一次性最多发送 30条路线
        // 0x01 代表路线，n 总包数 ,  i 当前包序号 , j 当前的路线数据
        //如果 没有路线信息，则总包为0
        byte routePackageCount = armRouteByte == null ? 0 : (byte) (armRouteByte.size() / 30 + 1);
        //

        for( int i=0;i<routePackageCount;i++) {
            byte[] onePackageData = new byte[]{0x01, routePackageCount, (byte)(i + 1)};
            for (int j = 0; j < armRouteByte.size()-i*30; j++) {
                onePackageData=mTransfer.add2Byte(onePackageData, armRouteByte.get(j+i*30));
            }
            mapdata.add(onePackageData);
        }
        /*
            节点的包
         */
        // 一个包最多有12个节点
        byte nodePackageCount = armNodesByte == null ? 0 : (byte) (armNodesByte.size() / 12 + 1);
        for( int i=0;i<nodePackageCount;i++) {
            byte[] onePackageData = new byte[]{0x02, routePackageCount, (byte)(i + 1)};
            for (int j = 0; j < armNodesByte.size()-i*12; j++) {
                onePackageData=mTransfer.add2Byte(onePackageData, armNodesByte.get(j+i*12));
            }
            mapdata.add(onePackageData);
        }
        return mapdata;
    }

}
