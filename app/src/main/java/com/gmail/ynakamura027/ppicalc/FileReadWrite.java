package com.gmail.ynakamura027.ppicalc;

import java.io.*;
import java.util.ArrayList;

import android.util.Log;

public class FileReadWrite {

	/**
	* ファイル書き込み処理（String文字列⇒ファイル）
	* @param sFilepath　書き込みファイルパス
	* @param sOutdata　ファイル出力するデータ
	* @param sEnctype　文字エンコード
	*/
	public static void writeFile(String sFilepath, String sOutdata, String sEnctype){

		BufferedWriter bufferedWriterObj = null;
		try {
			//ファイル出力ストリームの作成
			bufferedWriterObj = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(sFilepath, true), sEnctype));
	
			bufferedWriterObj.write(sOutdata);
			bufferedWriterObj.flush();
	
		} catch (Exception e) {
			Log.d("CommonFile.writeFile", e.getMessage());
		} finally {
			try {
				if( bufferedWriterObj != null) bufferedWriterObj.close();
			} catch (IOException e2) {
				Log.d("CommonFile.writeFile", e2.getMessage());	
			}
		}
	}

	/**
	* ファイル読み込み処理（ファイル⇒String文字列）
	* @param sFilepath　書き込みファイルパス
	* @param sEnctype　文字エンコード
	* @return　読み込みだファイルデータ文字列
	*/
	public static String readFile(String sFilepath, String sEnctype){

		String sData = "";
		BufferedReader bufferedReaderObj = null;
	
		try {
			//入力ストリームの作成
			bufferedReaderObj = new BufferedReader(new InputStreamReader(new FileInputStream(sFilepath), sEnctype));
	
		String sLine;
		while ((sLine = bufferedReaderObj.readLine()) != null) {
			sData += sLine + "\n";
		}
	
		} catch (Exception e) {
		Log.d("CommonFile.readFile", e.getMessage());
		} finally{
			try {
				if (bufferedReaderObj!=null) bufferedReaderObj.close();
			} catch (IOException e2) {
				Log.d("CommonFile.readFile", e2.getMessage());
			}
		}
	
		return sData;
	}

	// ファイル削除の処理
	public static void removeFile(String sFilepath){
		File f =new File( sFilepath ); 
		try{
			if( f.exists() ){
				f.delete();
				Log.d("removeFile", "Delete file  ["+f.toString()+"]");
			}
		}catch (Exception e){
			Log.d("removeFile", e.getMessage());
		}
		
	}
}
