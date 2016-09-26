package com.feng.Schedule;

import java.util.List;

/**
 * Created by fengscar on 2016/8/12.
 */
public class RobotComparer {
    public boolean compare2List(List<Integer> a, List<Integer> b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null || a.size() != b.size()) {
            return false;
        } else {
            for (int i = 0; i < a.size(); i++) {
                if (!a.get(i).equals(b.get(i))) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean compare2Int(int[] b1, int[] b2) {
        if (b1 == null && b2 == null) {
            return true;
        }
        if (b1 == null || b2 == null || b1.length != b2.length) {
            return false;
        } else {
            for (int i = 0; i < b1.length; i++) {
                if (b1[i] != b2[i]) {
                    return false;
                }
            }
        }
        return true;
    }

}
