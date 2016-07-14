package com.feng.Fragments;

import com.feng.Database.Node;

/**
 * Created by fengscar on 2016/5/26.
 */
public interface UserControlFragmentCallback {
    /**
     * 用户点击了 添加任务
     *
     * @param node
     */
    void onUserAdd(Node node);

    void onUserDel(Node node);
}
