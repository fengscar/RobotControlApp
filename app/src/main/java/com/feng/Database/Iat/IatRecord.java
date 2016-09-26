package com.feng.Database.Iat;

import android.support.annotation.NonNull;

/**
 * Created by fengscar on 2016/8/26.
 */
public class IatRecord {
    private int mID;
    private String mKey;
    private String mValue;

    public String getKey() {
        return mKey;
    }

    public void setKey(String key) {
        mKey = key;
    }

    public String getValue() {
        return mValue;
    }

    public void setValue(String value) {
        mValue = value;
    }

    public int getID() {
        return mID;
    }

    public void setID(int ID) {
        mID = ID;
    }

    public IatRecord(@NonNull String key, @NonNull String value) {
        mKey = key;
        mValue = value;
    }

    public IatRecord(@NonNull int ID, @NonNull String key, @NonNull String value) {
        mID = ID;
        mKey = key;
        mValue = value;
    }
}
