package pkch.company.lib;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import pkch.company.IdealWorldcupViewer1_Activity;
import android.util.Log;

public class Url2File {
	public Url2File(){
	}
	
	public String getFileName(String url, String fpath, String fname){
		//디렉토리 확보
		File f = null;
		f = new File(fpath);
		if (!f.isDirectory()) {
			f.mkdirs();
		}
		
		URL imgurl = null;
		HttpURLConnection conn = null;
		InputStream is = null;
		OutputStream os = null;
		
		String fullName = fpath+fname;		
		f = new File(fullName);
		/*
		if(f.exists()==true){
			f.delete(); // 파일이 존재할 경우 삭제함
		}
		*/
		if(f.exists()==false){
			// 파일을 체크해서 없을 경우 받아와서 저장한다
			//Log.e("pkch",fullName+" not exists, from "+url);
			// url에서 읽어오기
			try {
				imgurl = new URL(url);
				conn = (HttpURLConnection) imgurl.openConnection();
				conn.connect();
				is = conn.getInputStream();
				os = new FileOutputStream(new File(fullName));
				int c = 0;
				while ((c = is.read()) != -1) {
					os.write(c);
				}
				is.close();
				os.close();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return fullName;
	}
}
