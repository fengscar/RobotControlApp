package com.feng.Fragments;

import com.feng.Database.Map.Node;

/**
 * Created by fengscar on 2016/8/11.
 */
public class Target {
    private Node mNode;
    private String mRouteName;
    private boolean mIsSelect;

    public Target(Node node, String routeName, boolean isSelect) {
        mNode = node;
        mRouteName = routeName;
        mIsSelect = isSelect;
    }

    public Target(String routeName, Node node) {
        mRouteName = routeName;
        mNode = node;
    }

    public Node getNode() {
        return mNode;
    }

    public void setNode(Node node) {
        mNode = node;
    }

    public String getRouteName() {
        return mRouteName;
    }

    public void setRouteName(String routeName) {
        mRouteName = routeName;
    }

    public boolean isSelect() {
        return mIsSelect;
    }

    public void setSelect(boolean select) {
        mIsSelect = select;
    }
}
