package pkch.company.lib.saxSupplier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;

import android.util.Log;

import pkch.company.domain.IdealWorldcupElement;

public class IdealWorldcupXmlFileHandler {
	String XmlUrl;
	String XmlFile;
	boolean flag;
	public IdealWorldcupXmlFileHandler(String url, String dir, boolean flag){
		XmlUrl=url;
		XmlFile=dir;
		this.flag=flag;
	}
	public HashMap<HashMap<Integer,String>, ArrayList<IdealWorldcupElement>> getLists(){
		// XML파일이 존재하는지 확인
		File fp = new File(XmlFile);
		//Log.e("pkch","LOCAL에 저장된 XML 존재 확인 시작!");
		if(flag==true) fp.delete();
		if (!fp.exists()) {
			//Log.e("pkch","URL 통해서 XML 다운로드 시작!");
			// 파일이 없을 경우 XML URL을 통해서 불러와서 파일로 저장
			try {
				URL url = new URL(XmlUrl);
				URLConnection conn = url.openConnection();
				conn.connect();
				InputStream is = conn.getInputStream();
				OutputStream os = new FileOutputStream(new File(XmlFile));
				int c = 0;
				while ((c = is.read()) != -1) {
					os.write(c);
				}
				os.flush();
				fp = new File(XmlFile);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			//Log.e("pkch","URL 통해서 XML 다운로드 완료!");
		}
		//Log.e("pkch","LOCAL에 저장된 XML 존재 확인 완료!");
		

		//Log.e("pkch","LOCAL에 저장된 XML 파일을 읽기 시작!");
		//LOCAL에 저장된 XML 파일을 읽어와서 객체화
		
		FileReader reader;
		String data = null;
		StringBuffer strxml = new StringBuffer();
		try {
			reader = new FileReader(XmlFile);
			BufferedReader br = new BufferedReader(reader);
			while ((data = br.readLine()) != null) {
				strxml.append(data);
				//Log.e("pkch",data);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//Log.e("pkch","LOCAL에 저장된 XML 파일을 읽기 완료!");
		
		// 해당 XML파싱
		IdealWorldcupXmlLoader supplier = new IdealWorldcupXmlLoader();
		//Log.e("pkch","Just finished XML Parsing");
		return supplier.getAllLists(strxml.toString());
	}
}
