package com.feng.SpeechRecognize;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.feng.Utils.L;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fengscar on 2016/6/2.
 */
public class IatDatabaseHelper extends SQLiteOpenHelper implements I_IatData {
    private final String LOG = IatDatabaseHelper.class.getSimpleName();

    private static int VERSION = 1;
    private static SQLiteDatabase db;
    private static IatDatabaseHelper mInstance;

    private IatDatabaseHelper(Context context) {
        super(context, IAT_DATABASE, null, VERSION);
    }

    public synchronized static IatDatabaseHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new IatDatabaseHelper(context);
        }
        return mInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
    }

    private void createTables(SQLiteDatabase db) {

        // keyword不设置primarykey,使用默认的序号,因为keyword会有重复
        String createIatTable = "create table if not exists " + IAT_TABLE + "(" +
                KEYWORD + "text not null ," +
                IAT_WORD + "text not null)";
        String createTtsTable = "create table if not exists " + TTS_TABLE + "(" +
                KEYWORD + "text not null ," +
                TTS_WORD + "text not null)";
        try {
            db.execSQL(createIatTable);
            db.execSQL(createTtsTable);
            L.i(LOG, "语音识别数据库初始化成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        L.i("| 数据库 |  更新版本: 从" + oldVersion + "升级到" + newVersion);
        reCreateTable(db);
    }

    private void reCreateTable(SQLiteDatabase db) {
        dropAllTable(db);
        createTables(db);
    }

    private void dropAllTable(SQLiteDatabase db) {
        db.execSQL("drop table if exists " + IAT_TABLE);
        db.execSQL("drop table if exists " + TTS_TABLE);
        L.i(LOG, "| 数据库 |  删除旧表");
    }

    public void addIatWord(String keyword, String iatWord) {
        db = getWritableDatabase();
        String sql = "insert into " + IAT_TABLE + "(" + KEYWORD + "," + IAT_WORD + ") values(?,?)";
        db.execSQL(sql, new Object[]{keyword, iatWord});
    }

    public void addIatWord(String keyword, List<String> iatWords) {
        db = getWritableDatabase();
        String sql = "insert into " + IAT_TABLE + "(" + KEYWORD + "," + IAT_WORD + ") values(?,?)";
        for (String str : iatWords) {
            db.execSQL(sql, new String[]{keyword, str});
        }
    }

    public void delIatWord(String keyword) {
        db = getWritableDatabase();
        String sql = "delete from " + IAT_TABLE + " where " + keyword + "=? ";
        db.execSQL(sql, new Object[]{keyword});
    }


    public void addTtsWord(String keyword, String ttsWord) {
        db = getWritableDatabase();
        String sql = "insert into " + TTS_TABLE + "(" + KEYWORD + "," + TTS_TABLE + ") values(?,?)";
        db.execSQL(sql, new Object[]{keyword, ttsWord});
    }

    public void addTtsWord(String keyword, List<String> ttsWord) {
        db = getWritableDatabase();
        String sql = "insert into " + TTS_TABLE + "(" + KEYWORD + "," + TTS_TABLE + ") values(?,?)";
        for (String str : ttsWord) {
            db.execSQL(sql, new String[]{keyword, str});
        }
    }

    public void delTtsWord(String keyword) {
        db = getWritableDatabase();
        String sql = "delete from " + TTS_TABLE + " where " + keyword + "=? ";
        db.execSQL(sql, new Object[]{keyword});
    }


    public List<String> speechRecognize(String iat) {
        // 1. 查找 IAT表中的 keyword ( 2个结果union,一个是识别词比库短的,另一个是识别词比库长的 )
        db = getWritableDatabase();
        String sql =
                "select " + KEYWORD + " from " + IAT_TABLE + "where ? like" + "'%'||" + IAT_WORD + "||'%'" +
                        " union " +
                        "select " + KEYWORD + " from " + IAT_TABLE + " where " + IAT_WORD + " like '%?%'";
        Cursor cur = db.rawQuery(sql, new String[]{iat, iat});
        List<String> keywordList = new ArrayList<>();
        while (cur.moveToNext()) {
            String str = cur.getString(cur.getColumnIndex(KEYWORD));
            keywordList.add(str);
        }
        cur.close();
        // 2. 根据 keyWord 从 TTS表中获取 输出
        if (keywordList.size() == 0) {
            return null;
        } else {
            StringBuffer param = new StringBuffer();
            String[] objs = new String[keywordList.size()];
            for (int i = 0; i < keywordList.size(); i++) {
                param.append("?,");
                objs[i] = keywordList.get(i);
            }
            param.deleteCharAt(param.length() - 1);

            String sql2 = "select " + TTS_WORD + "from " + TTS_TABLE + " where " + KEYWORD + " in(" + param + ")";

            Cursor cur2 = db.rawQuery(sql2, objs);
            List<String> iatResult=new ArrayList<>();
            while( cur.moveToNext()){
                iatResult.add(cur.getString(cur.getColumnIndex(KEYWORD)));
            }
            if( iatResult.size()==0){
                return null;
            }
            cur.close();
            return iatResult;
        }
    }
    public List<String> getIatWord(String keyword){
        db=getWritableDatabase();
        String sql="select * from "+IAT_TABLE+" where "+KEYWORD+"=?";
        Cursor cur=db.rawQuery(sql,new String[]{keyword});
        List<String> result=new ArrayList<>();
        while(cur.moveToNext()){
            result.add(cur.getString(cur.getColumnIndex(IAT_WORD)));
        }
        return result;
    }
    public List<String> getTtsWord(String keyword){
        db=getWritableDatabase();
        String sql="select * from "+TTS_TABLE+" where "+KEYWORD+"=?";
        Cursor cur=db.rawQuery(sql,new String[]{keyword});
        List<String> result=new ArrayList<>();
        while(cur.moveToNext()){
            result.add(cur.getString(cur.getColumnIndex(TTS_WORD)));
        }
        return result;
    }
}
