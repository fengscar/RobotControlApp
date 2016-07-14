package com.feng.SpeechRecognize;

import android.os.Bundle;
import com.feng.Utils.L;
import com.feng.RobotApplication;
import com.iflytek.cloud.*;

/**
 * Created by fengscar on 2016/5/13.
 */
public class TtsSpeaker {
    private final static String LOG = TtsSpeaker.class.getSimpleName();

    // 语音合成对象
    private SpeechSynthesizer mTts;
    // 回调接口
    private  IatCallback iatCallback;

    private static TtsSpeaker sTtsSpeaker;
    public static TtsSpeaker getInstance() {
        L.i(LOG,"TtsSpeaker get Instance!");
        if (sTtsSpeaker == null) {
            sTtsSpeaker = new TtsSpeaker();
            sTtsSpeaker.init();
        }
        return sTtsSpeaker;
    }
    private void init() {
        mTts=SpeechSynthesizer.createSynthesizer(RobotApplication.getContext(), mInitTtsListenner);
    }

    public void speak(String str,IatCallback icb){
        iatCallback=icb;
        //TODO  如果正在说话,停止说话! 最好可以改成等待当前说话完成后 ,继续说话, 不过这样要加入时间判断,不然延迟可能很严重
        if(  mTts.isSpeaking() ) {
            mTts.stopSpeaking();
        }
        mTts.startSpeaking(str,mTTSListener);
    }

    private InitListener mInitTtsListenner=new InitListener() {
        public void onInit(int code) {
            if (code != ErrorCode.SUCCESS) {
                L.e(LOG,"语音合成 初始化失败，错误码：" + code);
            }else{
                L.i(LOG," 语音合成 初始化成功 ");
                mTts.setParameter(SpeechConstant.ENGINE_TYPE,SpeechConstant.TYPE_LOCAL);
            }
        }
    };

    private SynthesizerListener mTTSListener=new SynthesizerListener() {

        public void onSpeakResumed() {

        }

        @Override
        public void onSpeakProgress(int arg0, int arg1, int arg2) {

        }

        @Override
        public void onSpeakPaused() {

        }

        @Override
        public void onSpeakBegin() {

        }

        @Override
        public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {

        }

        @Override
        public void onBufferProgress(int arg0, int arg1, int arg2, String arg3) {

        }
        @Override
        public void onCompleted(SpeechError arg0) {
            L.i(LOG," 语音TTS 完成");
            iatCallback.ttsFinish();
        }
    };
}
