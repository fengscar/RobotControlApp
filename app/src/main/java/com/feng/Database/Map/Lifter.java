package com.feng.Database.Map;

import java.util.List;

/**
 * Created by fengscar on 2016/8/24.
 */
public class Lifter {
    List<Tray> mTrays;

    // 提升机 托盘, 每个托盘有2个节点
    class Tray {
        private int trayID; //托盘ID
        private int frontNodeID; //托盘 前部ID
        private int backNodeID; //托盘 后部ID
    }
}
