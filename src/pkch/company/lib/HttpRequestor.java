package pkch.company.lib;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

import android.util.Log;

public class HttpRequestor {
	public static final String CRLF="\r\n";	// HTTP 에서 줄을 구분할떄 사용하는 상수를 선언함
	private URL targetUrl;
	private ArrayList list;	// 파라미터 목록을 저장하기 위함: 파라미터이름1,파라미터값1,파라미터이름2,파리미터값2.. 이런식으로 나감
	
	public HttpRequestor(URL target, int initialCapaicity){
		this.targetUrl=target;	// 전송URL 설정
		this.list= new ArrayList(initialCapaicity);	// 최대 용량 설정
	}
	
	//텍스트 파라미터 설정
	public void addParameter(String parameterName, String parameterValue){
		if(parameterValue==null) throw new IllegalArgumentException("no Parameter valuew!");
		list.add(parameterName);
		list.add(parameterValue);
	}
	
	//파일 파라미터 설정
	public void addFile(String parameterName, File parameterValue){
		if(parameterValue==null){
			list.add(parameterName);
			list.add(new NullFile());
		}else{
			list.add(parameterName);
			list.add(parameterValue);
		}
	}
	
	//GET/POST 방식으로 파라미터 값을 인코딩해서 보냄, application/x-www-form-urlencoded 인코딩 방식을 사용
	private static String encodeString(ArrayList parameters){
		StringBuffer sb= new StringBuffer(255);	// 자주 변경되는 문자열의 경우에는 StringBuffer를 사용하는 것이 메모리 사용에 효율적임
		Object[] obj= new Object[parameters.size()];
		parameters.toArray(obj);
		
		for(int i=0;i<obj.length;i+=2){
			if(obj[i+1] instanceof File || obj[i+1] instanceof NullFile) continue;
			sb.append(URLEncoder.encode((String)obj[i]));
			sb.append("=");
			sb.append(URLEncoder.encode((String)obj[i+1]));
			if(i+2<obj.length) sb.append("&");
		}
		
		return sb.toString();
	}
	
	//GET 형태로 URL에 파라미터 전송 후에 응답을 INPUT sTREAM 으로 리턴
	public InputStream sendGet() throws IOException{
		String paramString=null;
		if(list.size()>0){paramString="?"+encodeString(list);}
		else {paramString="";}
		URL url= new URL(targetUrl.toExternalForm()+paramString);
		URLConnection conn= url.openConnection();
		return conn.getInputStream();
	}
	
	//POST 형태로 url에 파라미터 전송 후에 응답을 Input Stream으로 리턴
	public InputStream sendPost() throws IOException{
		String paramString=null;
		if(list.size()>0){paramString=encodeString(list);}
		else{paramString="";}
		HttpURLConnection conn= (HttpURLConnection)targetUrl.openConnection();
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setUseCaches(false);
		DataOutputStream out= null;
		try{
			out= new DataOutputStream(conn.getOutputStream());
			out.writeBytes(paramString);
			out.flush();
		}finally{
			if(out!=null) out.close(); // try 실행 후에 exception 관계 없이 반드시 실행
		}
		
		return conn.getInputStream();
	}
	
	//Multipart/form-data 전송후에 응답하도록 함
	public InputStream sendMultipartPost() throws IOException {
	    HttpURLConnection conn = (HttpURLConnection)targetUrl.openConnection();
	    // Delimeter 생성
	    String delimeter = makeDelimeter();        
	    byte[] newLineBytes = CRLF.getBytes();
	    byte[] delimeterBytes = delimeter.getBytes();
	    byte[] dispositionBytes = "Content-Disposition: form-data; name=".getBytes();
	    byte[] quotationBytes = "\"".getBytes();
	    byte[] contentTypeBytes = "Content-Type: application/octet-stream".getBytes();
	    byte[] fileNameBytes = "; filename=".getBytes();
	    byte[] twoDashBytes = "--".getBytes();
	    
	    conn.setRequestMethod("POST");
	    conn.setRequestProperty("Content-Type",
	                            "multipart/form-data; boundary="+delimeter);
	    conn.setDoInput(true);
	    conn.setDoOutput(true);
	    conn.setUseCaches(false);
	    
	    BufferedOutputStream out = null;
	    try {
	        out = new BufferedOutputStream(conn.getOutputStream());
	        
	        Object[] obj = new Object[list.size()];
	        list.toArray(obj);
	        
	        for (int i = 0 ; i < obj.length ; i += 2) {
	            // Delimeter 전송
	            out.write(twoDashBytes);
	            out.write(delimeterBytes);
	            out.write(newLineBytes);
	            // 파라미터 이름 출력
	            out.write(dispositionBytes);
	            out.write(quotationBytes);
	            out.write( ((String)obj[i]).getBytes() );
	            out.write(quotationBytes);
	            if ( obj[i+1] instanceof String) {
	                // String 이라면
	                out.write(newLineBytes);
	                out.write(newLineBytes);
	                // 값 출력
	                out.write( ((String)obj[i+1]).getBytes() );
	                out.write(newLineBytes);
	            } else {
	                // 파라미터의 값이 File 이나 NullFile인 경우
	                if ( obj[i+1] instanceof File) {
	                    File file = (File)obj[i+1];
	                    // File이 존재하는 지 검사한다.
	                    out.write(fileNameBytes);
	                    out.write(quotationBytes);
	                    out.write(file.getAbsolutePath().getBytes() );
	                    out.write(quotationBytes);
	                } else {
	                    // NullFile 인 경우
	                    out.write(fileNameBytes);
	                    out.write(quotationBytes);
	                    out.write(quotationBytes);
	                }
	                out.write(newLineBytes);
	                out.write(contentTypeBytes);
	                out.write(newLineBytes);
	                out.write(newLineBytes);
	                // File 데이터를 전송한다.
	                if (obj[i+1] instanceof File) {
	                    File file = (File)obj[i+1];
	                    // file에 있는 내용을 전송한다.
	                    BufferedInputStream is = null;
	                    try {
	                        is = new BufferedInputStream(
	                                 new FileInputStream(file));
	                        byte[] fileBuffer = new byte[1024 * 8]; // 8k
	                        int len = -1;
	                        while ( (len = is.read(fileBuffer)) != -1) {
	                            out.write(fileBuffer, 0, len);
	                        }
	                    } finally {
	                        if (is != null) try { is.close(); } catch(IOException ex) {}
	                    }
	                }
	                out.write(newLineBytes);
	            } // 파일 데이터의 전송 블럭 끝
	            if ( i + 2 == obj.length ) {
	                // 마지막 Delimeter 전송
	                out.write(twoDashBytes);
	                out.write(delimeterBytes);
	                out.write(twoDashBytes);
	                out.write(newLineBytes);
	            }
	        } // for 루프의 끝	        
	        out.flush();
	    } finally {
	        if (out != null) out.close();
	    }
	    return conn.getInputStream();
	}
	
	private static String makeDelimeter() {
        return "---------------------------pkch19830101";
    }
	
	private class NullFile{
		NullFile(){}
		public String toString(){return "";}
	}
	
}
