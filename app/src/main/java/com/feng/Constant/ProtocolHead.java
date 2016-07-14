package com.feng.Constant;

/**
 * Created by fengscar on 2016/4/22.
 */
public class ProtocolHead {
    private String mAction;
    private byte[] mData;

    public ProtocolHead(String action, byte[] data) {
        this.mAction = action;
        this.mData = data;
    }

    public String getAction() {
        return mAction;
    }

    public byte[] getData() {
        return mData;
    }
    public boolean isMatching(byte[] datas){
        if (datas==null || datas.length<5) {
            return false;
        }else {
            for(int i = 0;i<3;i++){
                if( this.mData[i]!=datas[i] ){
                    return false;
                }
            }
        }
        return true;
    }

}