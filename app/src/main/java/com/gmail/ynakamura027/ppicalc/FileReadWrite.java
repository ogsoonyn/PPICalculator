package com.gmail.ynakamura027.ppicalc;

import java.io.*;
import java.util.ArrayList;

import android.util.Log;

public class FileReadWrite {

	/**
	* �t�@�C���������ݏ����iString������˃t�@�C���j
	* @param sFilepath�@�������݃t�@�C���p�X
	* @param sOutdata�@�t�@�C���o�͂���f�[�^
	* @param sEnctype�@�����G���R�[�h
	*/
	public static void writeFile(String sFilepath, String sOutdata, String sEnctype){

		BufferedWriter bufferedWriterObj = null;
		try {
			//�t�@�C���o�̓X�g���[���̍쐬
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
	* �t�@�C���ǂݍ��ݏ����i�t�@�C����String������j
	* @param sFilepath�@�������݃t�@�C���p�X
	* @param sEnctype�@�����G���R�[�h
	* @return�@�ǂݍ��݂��t�@�C���f�[�^������
	*/
	public static String readFile(String sFilepath, String sEnctype){

		String sData = "";
		BufferedReader bufferedReaderObj = null;
	
		try {
			//���̓X�g���[���̍쐬
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

	// �t�@�C���폜�̏���
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
