package com.feng.Database.Iat;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;
import com.feng.RobotApplication;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fengscar on 2016/8/26.
 */
public class IatDbHelper extends SQLiteOpenHelper {
    private final static String TAG = IatDbHelper.class.getSimpleName();
    private final static int VERSION = 1;
    private final static String IAT_DATABASE = "iatDatabase";


    private final static String TABLE_IAT = "IatKeyValue";
    private final static String COLUMN_ID = "id";
    private final static String COLUMN_KEY = "iatKey";
    private final static String COLUMN_VALUE = "iatValue";


    //region 构造和获取实例
    private SQLiteDatabase mDatabase;

    private volatile static IatDbHelper sInstance;

    private IatDbHelper() {
        super(RobotApplication.getContext(), IAT_DATABASE, null, VERSION);
        if (mDatabase == null) {
            mDatabase = getWritableDatabase();
        }
    }

    public static IatDbHelper getInstance() {
        if (sInstance == null) {
            synchronized (IatDbHelper.class) {
                if (sInstance == null) {
                    sInstance = new IatDbHelper();
                }
            }
        }
        return sInstance;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(TAG, "首次使用语音识别,创建数据库: " + IAT_DATABASE + ".db");
        mDatabase = db;
        createTable();
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private void createTable() {
        String sql = TextUtils.concat("create table if not exists ", TABLE_IAT, "(",
                COLUMN_ID, " integer primary key autoincrement ,",
                COLUMN_KEY, " text not null ,",
                COLUMN_VALUE, " text not null", ")").toString();
        mDatabase.execSQL(sql);
    }

    public void addRecord(IatRecord ir) {
        String sql = "insert into " + TABLE_IAT + "(" + COLUMN_KEY + "," + COLUMN_VALUE + ") values(?,?)";
        mDatabase.execSQL(sql, new Object[]{ir.getKey(), ir.getValue()});
    }

    /**
     * 根据Record的ID值来删除数据
     *
     * @param ir
     */
    public void delRecord(IatRecord ir) {
        String sql = "delete from " + TABLE_IAT + " where " + COLUMN_ID + "=?";
        mDatabase.execSQL(sql, new Object[]{ir.getID()});
    }

    public void delAllRecord() {
        String sql = "delete  from " + TABLE_IAT;
        mDatabase.execSQL(sql, new Object[]{});
    }

    /**
     * ID固定,key和value改变
     */
    public void updateRecord(IatRecord ir) {
        String sql = "update " + TABLE_IAT + " set " +
                COLUMN_KEY + "=?," + COLUMN_VALUE + " =? where " + COLUMN_ID + " =?";
        mDatabase.execSQL(sql, new Object[]{ir.getKey(), ir.getValue(), ir.getID()});
    }

    // 根据Key查找value,可能有多个value( key值并不是unique)
    public List<IatRecord> findRecord(String key) {
        String sql = "select * from " + TABLE_IAT + " where " + COLUMN_KEY + "=?";
        Cursor cursor = mDatabase.rawQuery(sql, new String[]{key});
        List<IatRecord> recordList = new ArrayList<>();

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
            String value = cursor.getString(cursor.getColumnIndex(COLUMN_VALUE));
            recordList.add(new IatRecord(id, key, value));
        }
        cursor.close();
        return recordList;
    }

    public List<IatRecord> getAllRecord() {
        String sql = "select * from " + TABLE_IAT;
        Cursor cursor = mDatabase.rawQuery(sql, null);
        List<IatRecord> recordList = new ArrayList<>();

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
            String key = cursor.getString(cursor.getColumnIndex(COLUMN_KEY));
            String value = cursor.getString(cursor.getColumnIndex(COLUMN_VALUE));
            recordList.add(new IatRecord(id, key, value));
        }
        cursor.close();
        return recordList;
    }
}
