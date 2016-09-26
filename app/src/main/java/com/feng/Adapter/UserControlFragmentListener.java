package com.feng.Adapter;

import com.feng.Database.Map.Node;

/**
 * Created by fengscar on 2016/5/26.
 */
public interface UserControlFragmentListener {
    /**
     * 用户点击了 添加任务
     *
     * @param node
     */
    void onUserAdd(Node node);

    void onUserDel(Node node);
}
