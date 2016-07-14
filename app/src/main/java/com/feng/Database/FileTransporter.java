package com.feng.Database;

import android.util.Log;
import com.feng.Utils.L;
import com.feng.RobotApplication;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by fengscar on 2016/5/30.
 */
public class FileTransporter {
    static final String LOG = FileTransporter.class.getSimpleName();
    static final String DATABASE_PATH = RobotApplication.getContext().getDatabasePath("mapDatabase").getAbsolutePath();

    /**
     * 加载本地文件,并转换为byte数组
     *
     * @return
     */
    public static byte[] loadFile(String fileName) throws FileNotFoundException {
        Log.d(LOG, "载入文件: " + fileName);
        File file = new File(fileName);
        if (file == null) {
            throw new FileNotFoundException();
        }

        FileInputStream fis = null;
        ByteArrayOutputStream baos = null;
        byte[] data = null;

        try {
            fis = new FileInputStream(file);

            baos = new ByteArrayOutputStream((int) file.length());

            byte[] buffer = new byte[1024];
            int len = -1;
            while ((len = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }

            data = baos.toByteArray();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                    fis = null;
                }

                baos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return data;
    }

    public static byte[] compress(byte[] data) {
        Log.d(LOG, "开始压缩文件,原始大小:" + data.length);

        GZIPOutputStream gzip = null;
        ByteArrayOutputStream baos = null;
        byte[] newData = null;

        try {
            baos = new ByteArrayOutputStream();
            gzip = new GZIPOutputStream(baos);

            gzip.write(data);
            gzip.finish();
            gzip.flush();

            newData = baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                gzip.close();
                baos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.d(LOG, "压缩文件成功,当前大小:" + newData.length);
        return newData;
    }

    /**
     * 解压文件...
     *
     * @param data
     * @return
     */
    public static byte[] decompress(byte[] data) {
        Log.d(LOG, "开始解压文件,原始大小:" + data.length);
        byte[] result = null;
        GZIPInputStream gzip = null;
        ByteArrayInputStream bais = null;
        ByteArrayOutputStream baos = null;
        try {
            bais = new ByteArrayInputStream(data);
            gzip = new GZIPInputStream(bais);
            baos = new ByteArrayOutputStream();


            byte[] buf = new byte[1024];
            int len;
            while ((len = gzip.read(buf)) > 0) {
                baos.write(buf, 0, len);
            }

            result = baos.toByteArray();
            baos.flush();
            baos.close();
            gzip.close();
            bais.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(LOG, "解压文件成功,当前大小:" + result.length);
        return result;
    }

    public static void createFile(String path, byte[] data) {
//        deleteFilesByDirectory(new File(path));
        Log.d(LOG, "开始生成文件: " + path);

        File file = new File(path);

        FileOutputStream fos = null;
        ByteArrayInputStream bis = null;

        try {
            // 如果存在,先删除在生成
            file.delete();

            fos = new FileOutputStream(file);
            bis = new ByteArrayInputStream(data);

            byte[] buffer = new byte[1024];
            int len = -1;
            while ((len = bis.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }

            Log.d(LOG, "生成文件成功");


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void deleteFilesByDirectory(File directory) {
        if ((directory != null) && directory.exists() &&
                directory.isDirectory()) {
            for (File item : directory.listFiles()) {
                item.delete();
            }
        }
        L.i(LOG, "删除旧数据库文件成功");
    }

}
