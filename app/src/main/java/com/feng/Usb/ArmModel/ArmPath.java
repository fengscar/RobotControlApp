package com.feng.Usb.ArmModel;

import com.feng.Usb.ArmHead;

/**
 * Created by fengscar on 2016/7/19.
 */
public enum ArmPath implements ArmHead {
    QueryRouteVersion(1), Query(2), QueryCard(3), QueryTarget(4),

    UpdateCard(5), AddTargets(6), DelTargets(7), SetPriority(8),
    SendMap(9), UploadMap(10), ReplyUploadMap(11),

    SetStopPoint(12), CurrentPath(13);

    private final int index;

    ArmPath(int b) {
        index = b;
    }

    public byte[] getHead() {
        return new byte[]{ROBOT, PATH, (byte) this.index};
    }

    public ArmPath get(byte b) {
        for (ArmPath p : ArmPath.values()) {
            if (p.index == b) {
                return p;
            }
        }
        return null;
    }

    @Override
    public byte getModule() {
        return PATH;
    }

    @Override
    public byte getCommand() {
        return (byte) index;
    }
}
