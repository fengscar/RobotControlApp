/**
 * @author 福建省和创伟业智能科技有限公司
 * @创建时间 2016-3-25 上午11:50:39
 */
package com.feng.SpeechRecognize;


import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import com.feng.Constant.I_Parameters;
import com.feng.Database.Iat.IatDbHelper;
import com.feng.Database.Iat.IatRecord;
import com.feng.RobotApplication;
import com.feng.Utils.L;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;

import java.util.ArrayList;
import java.util.List;

public class IatService extends Service implements I_Parameters {
    private final static String LOG = IatService.class.getSimpleName();
    // 引擎类型
    private BroadcastReceiver receiver;

    private static final int START_RECOGNIZE = 888;

    public IBinder onBind(Intent intent) {
        return null;
    }

    private IatListener mIatListener;
    private TtsSpeaker mTtsSpeaker;

    Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            removeMessages(START_RECOGNIZE);
        }

        ;
    };

    private IatCallback icb = new IatCallback() {
        @Override
        public void iatReturn(String result) {
            L.i(LOG, "回调识别结果：" + result);
            String iatResult = getResponse(result);
            if (iatResult == null) {
                L.i(LOG, "识别结果无法解析,继续开始识别!");
                mIatListener.startListen();
            } else {
                L.i(LOG, "识别结果可以解析,开始语音合成");
                mTtsSpeaker.speak(iatResult, icb);
            }
        }

        public void ttsFinish() {
            L.i(LOG, "TTS结束！ 继续开始识别!");
            mIatListener.startListen();
        }
    };

    public int onStartCommand(Intent intent, int flags, int startId) {
        SpeechUtility.createUtility(RobotApplication.getContext(), SpeechConstant.APPID + "=56eb5dfb");

        mIatListener = IatListener.getInstance(icb);
        mTtsSpeaker = TtsSpeaker.getInstance();

        initReceiver();
        L.i(LOG, "(＾－＾)V 语音识别 服务开启");

        mIatListener.startListen();
        return super.onStartCommand(intent, flags, startId);
    }

    private void startTTS(String ttsStr) {
        mIatListener.stopListen();
        mTtsSpeaker.speak(ttsStr, icb);
    }

    private String getResponse(String iatStr) {
        List<IatRecord> iatRecordList = IatDbHelper.getInstance().getAllRecord();
        List<String> result = new ArrayList<>();

        for (IatRecord iatRecord : iatRecordList) {
            if (iatStr.contains(iatRecord.getKey())) {
                result.add(iatRecord.getValue());
            }
        }

        if (result.size() == 0) {
            return null;
        } else {
            int x = (int) (Math.random() * result.size());
            return result.get(x);
        }
    }

    private void initReceiver() {
        IntentFilter filter = new IntentFilter();
        /**  协议 部分*/
        filter.addAction(IAT_START_LISTENNING);
        filter.addAction(IAT_STOP_LISTENNING);
        filter.addAction(TTS_START_SPEAK);

        if (receiver == null) {
            receiver = new IATReceiver();
        }
        L.i(LOG, "注册 IatReceiver");
        registerReceiver(receiver, filter);
    }

    class IATReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case IAT_START_LISTENNING:
                    mIatListener.startListen();
                    break;
                case TTS_START_SPEAK:
                    L.e(LOG, "准备语音合成: --->" + intent.getStringExtra(I_Parameters.UNIFORM_TTS));
                    startTTS(intent.getStringExtra(I_Parameters.UNIFORM_TTS));
                    break;

                default:
                    break;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            L.e(LOG, "正在注销 IatReceiver...");
            unregisterReceiver(receiver);
        }
        // 退出时释放连接
        mIatListener.finishListen();
        L.e(LOG, "╮(╯_╰)╭语音识别 服务关闭");
    }
}

