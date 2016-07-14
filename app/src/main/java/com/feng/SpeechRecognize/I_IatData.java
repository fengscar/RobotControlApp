/**
 * @author 福建省和创伟业智能科技有限公司
 * @创建时间 2016-4-5 上午9:37:39
 */
package com.feng.SpeechRecognize;

public interface I_IatData {
    String IAT_DATABASE = "iatDatabase";

    // 识别表名称
    String IAT_TABLE = " iat_table ";
    String TTS_TABLE = " tts_table ";
    // 语音识别 数据库表对应的字段(一张识别表,一张匹配表)
    // 识别表: 根据输入的字符串,获取最匹配的key
    // 匹配表: 根据查询的key,获取随机的tts字符串.
    String KEYWORD = " keyword ";
    String IAT_WORD = " iatWord ";

    String TTS_WORD = " ttsWord ";

}

