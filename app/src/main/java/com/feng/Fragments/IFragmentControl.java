package com.feng.Fragments;

import com.feng.Adapter.UserControlFragmentListener;
import com.feng.Database.Map.Node;

import java.util.List;

/**
 * Created by fengscar on 2016/5/26.
 */

public interface IFragmentControl {
    String BundleWorkspaceID = "WorkspaceID";
    String BundleTaskListIDS = "TaskIDS";

    int getWorkspaceID();

    void setCallback(UserControlFragmentListener ucfc);

    void addTask(Node node);

    void addTask(List<Node> node);

    void delTask(Node node);

    void delTask(List<Node> node);

    void clearTask();
}
