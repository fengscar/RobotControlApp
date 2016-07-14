/**
 * @author 福建省和创伟业智能科技有限公司
 * @创建时间  2016-1-30 下午10:45:14
 */
package com.feng.Utils;

import android.os.Environment;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {
	private static String PATH_LOGCAT=getInnerSDCardPath();  

//	public String readSDFile(String fileName) throws IOException {
//
//		File file = new File(fileName);
//		FileInputStream fis = new FileInputStream(file);
//		int length = fis.available();
//		byte[] buffer = new byte[length];
//		fis.read(buffer);
//		String res = EncodingUtils.getString(buffer, "UTF-8");
//		fis.close();
//		return res;
//	}

	// 写文件
	public static void writeSDFile( String logFileName,String write_str)
			throws IOException {
		writeSDFile(getInnerSDCardPath(),logFileName,write_str);
	}

	public static void writeSDFile(String path,String logFileName,String write_str)	throws IOException {
		File file=new File(path+"/"+logFileName+".txt");
		if( !file.exists() ){
			file.createNewFile();
		}
		FileWriter fileWriter = new FileWriter(file, true);
		fileWriter.write("\n"+write_str);
		fileWriter.close();
	}

	/**
	 * 获取内置SD卡路径
	 * @return
	 */
	public static String getInnerSDCardPath() {  
		return Environment.getExternalStorageDirectory().getPath();  
	}
	/**
	 * 获取外置SD卡路径
	 * @return  应该就一条记录或空
	 */
	public List<String> getExtSDCardPath()
	{
		List<String> lResult = new ArrayList<String>();
		try {
			Runtime rt = Runtime.getRuntime();
			Process proc = rt.exec("mount");
			InputStream is = proc.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line;
			while ((line = br.readLine()) != null) {
				if (line.contains("extSdCard"))
				{
					String [] arr = line.split(" ");
					String path = arr[1];
					File file = new File(path);
					if (file.isDirectory())
					{
						lResult.add(path);
					}
				}
			}
			isr.close();
		} catch (Exception e) {
		}
		return lResult;
	}

}
