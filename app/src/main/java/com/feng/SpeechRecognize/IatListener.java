package com.feng.SpeechRecognize;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.feng.Utils.L;
import com.feng.RobotApplication;
import com.iflytek.cloud.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by fengscar on 2016/5/13.
 */
public class IatListener {

    //这个handler防止出错后重启的太快
    private final int RESTART_LISTEN = 123;
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == RESTART_LISTEN) {
                sIat.startListening(mRecognizerListener);
            }
        }
    };

    private final static String LOG = IatListener.class.getSimpleName();
    // 语音听写对象
    private static SpeechRecognizer sIat;

    private static IatListener sIatListener;

    public static IatListener getInstance(IatCallback icb) {
        iatCallback = icb;
        L.i(LOG, "IatListener get Instance!");
        if (sIatListener == null) {
            sIatListener = new IatListener();
            sIatListener.init();
        } else {
            // 退出时记得释放 sIatListener
            L.e(LOG, "sIat 不为空");
        }
        return sIatListener;
    }

    // 存放识别结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<>();
    private SharedPreferences mSharedPreferences;

    // 引擎类型
    private String mEngineType = SpeechConstant.TYPE_LOCAL;
    // 回调接口
    private static IatCallback iatCallback;

    private void init() {
        mSharedPreferences = RobotApplication.getContext().getSharedPreferences("Iat", Activity.MODE_PRIVATE);
        sIat = SpeechRecognizer.createRecognizer(RobotApplication.getContext(), mInitListener);
    }

    public void startListen() {
        // 如果正在 识别 ...不继续识别，否则 抛出20017错误
        if (sIat.isListening()) {
            sIat.cancel();
            Log.e(LOG, "IatListener is already listening,Stop and Restart listen ");
        }
        sIat.startListening(mRecognizerListener);
    }

    public void stopListen() {
        if (sIat.isListening()) {
            Log.w(LOG, "正在识别语音，强行取消");
            sIat.cancel();
        }
        //取消 handler传递的message
        mHandler.removeMessages(RESTART_LISTEN);
    }

    public void finishListen() {
        stopListen();
        sIat.destroy();
        sIatListener = null;
        mHandler = null;
    }


    private InitListener mInitListener = new InitListener() {
        public void onInit(int code) {
            L.d(LOG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                L.e(LOG, "语音识别 初始化失败，错误码：" + code);
            } else {
                L.i(LOG, " 语音识别 初始化成功 ");
                sIatListener.setParam();
                L.i(LOG, " 语音识别 设置参数成功");
                sIatListener.startListen();
                L.i(LOG, " 语音识别 开始识别");
            }
        }
    };

    private RecognizerListener mRecognizerListener = new RecognizerListener() {

        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            L.i(LOG, "开始识别!");
        }

        @Override
        public void onError(SpeechError error) {
            // 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
            // 如果使用本地功能（语记）需要提示用户开启语记的录音权限。
            L.e(LOG, "识别中错误:" + error.getPlainDescription(true) + " 1秒后 继续开始识别!");
            if (mHandler == null) {
                return;
            }
            mHandler.removeMessages(RESTART_LISTEN);
            mHandler.sendEmptyMessageDelayed(RESTART_LISTEN, 1000);
        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            L.i(LOG, "结束识别!");
        }

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            // 打印结果 并且 开始语音合成
            printResult(results);
        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
//            L.e(LOG, "当前音量: " + volume);
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
        }
    };

    public void setParam() {
        // 清空参数
        sIat.setParameter(SpeechConstant.PARAMS, null);

        // 设置听写引擎
        sIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        // 设置返回结果格式
        sIat.setParameter(SpeechConstant.RESULT_TYPE, "json");

        String lag = mSharedPreferences.getString("iat_language_preference", "mandarin");
        if (lag.equals("en_us")) {
            // 设置语言
            sIat.setParameter(SpeechConstant.LANGUAGE, "en_us");
        } else {
            // 设置语言
            sIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
            // 设置语言区域
            sIat.setParameter(SpeechConstant.ACCENT, lag);
        }
        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        sIat.setParameter(SpeechConstant.VAD_BOS, mSharedPreferences.getString("iat_vadbos_preference", "5000"));

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        sIat.setParameter(SpeechConstant.VAD_EOS, mSharedPreferences.getString("iat_vadeos_preference", "1000"));

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        sIat.setParameter(SpeechConstant.ASR_PTT, mSharedPreferences.getString("iat_punc_preference", "0"));
        // 设置识别 门限值
        sIat.setParameter(SpeechConstant.ASR_THRESHOLD, "15");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        sIat.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        sIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/iat.wav");
        L.i(LOG, sIat.getParameter(SpeechConstant.ASR_AUDIO_PATH));
    }

    private void printResult(RecognizerResult results) {
        String text = JsonParser.parseIatResult(results.getResultString());

        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mIatResults.put(sn, text);

        StringBuffer resultBuffer = new StringBuffer();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }
        L.i(LOG, "当前的识别结果: " + resultBuffer.toString());

        // 回调结果给 service
        iatCallback.iatReturn(resultBuffer.toString());
    }
}
