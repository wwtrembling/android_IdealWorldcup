package pkch.company.lib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

import android.util.Log;

//POST 방식을 이용하여 주로 db 에 값을 추가/수정/삭제 할 경우에 이용함
public class IdealWorldCupUrlHttp {
	private URL url;
	private String url_path="http://wwtrembling.cafe24.com/idealWorldcup/action.php";
	HttpURLConnection httpURLCon;

	public IdealWorldCupUrlHttp(String url_path) {
		this.url_path = url_path;
	}
	public IdealWorldCupUrlHttp(){
		
	}

	public String getUrl() {
		return url_path;
	}

	public HashMap<String, String> sendPost(HashMap<String, String> hmap) {
		//인터넷이 연결 확인
		
		String key = null;
		StringBuffer sb = new StringBuffer();
		HashMap<String, String> hm = new HashMap<String, String>();
		Iterator<String> iter = hmap.keySet().iterator();
		while (iter.hasNext()) {
			key = (String) iter.next();
			sb.append(key).append("=").append(hmap.get(key)).append("&");
		}

		PrintWriter pw = null;
		BufferedReader bf = null;
		StringBuilder buff = null;
		String line = "";
		String result = "";
		try {
			url = new URL(url_path);
			httpURLCon = (HttpURLConnection) url.openConnection();
			httpURLCon.setDefaultUseCaches(false);
			httpURLCon.setDoInput(true);
			httpURLCon.setDoOutput(true);
			httpURLCon.setRequestMethod("POST");
			httpURLCon.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
			pw = new PrintWriter(new OutputStreamWriter(httpURLCon.getOutputStream(), "UTF-8"));
			pw.write(sb.toString());
			pw.flush();
			bf = new BufferedReader(new InputStreamReader(httpURLCon.getInputStream(), "UTF-8"));
			buff = new StringBuilder();
			while ((line = bf.readLine()) != null) {
				result += line;
				buff.append(line);
			}
			hm.put("result", result);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return hm;
	}
}
