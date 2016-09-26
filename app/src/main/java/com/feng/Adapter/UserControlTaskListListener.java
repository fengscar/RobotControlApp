package com.feng.Adapter;

import com.feng.Database.Map.Node;

/**
 * Created by fengscar on 2016/5/26.
 */
public interface UserControlTaskListListener {
    void onUserAddToExpander(int groupPosition);

    void onUserDelFromExpander(Node node);
}
