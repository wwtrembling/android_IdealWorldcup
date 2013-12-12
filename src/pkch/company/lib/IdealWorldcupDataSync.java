package pkch.company.lib;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import pkch.company.domain.IdealWorldcupLoadingDataElement;
import pkch.company.lib.saxSupplier.IdealWorldcupXmlHandlerForLoadingData;
import pkch.company.lib.sql.IdealWorldcupAppTables;
import pkch.company.lib.sql.IdealWorldcupSQLHandler;
import pkch.company.member.MemberAction;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

//데이터 동기화 클래스
public class IdealWorldcupDataSync extends AsyncTask<Void, Void, Void>{
	private final String pkchActionUrl = "http://wwtrembling.cafe24.com/idealWorldcup/action.php";
	private final String pkchFileSaveDir = "/data/data/pkch.company/";
	private ArrayList<IdealWorldcupLoadingDataElement> arr_ide;
	HashMap<String, ArrayList<IdealWorldcupLoadingDataElement>> hhm;
	private ProgressDialog dialog;
	private Context context;
	private String resultXml=null;
	private DataSyncDownloadThread downThread=null;
	static final int PROGRESS_DIALOG=0;
	static int item_total_size=0;	// 전체 사이즈

	public IdealWorldcupDataSync(Context context){
		this.context=context;
	}
	
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		this.dialog = ProgressDialog.show(context, "Data Loading..","로그인된 사용자의 정보를 받아오고 있습니다.", true);
	}

	@Override
	protected Void doInBackground(Void... params) {
		//현재로그인 되어 있는 사용자 아이디를 받아옴
		MemberAction ma= new MemberAction(this.context);
		ArrayList<IdealWorldcupLoadingDataElement> al= ma.getAllmembers(true);
		HashMap<String, String> hm=new HashMap<String, String>();
		if(al.size()>0){
			//사용자 아이디에 해당하는 데이타를 웹을 통해 받아가지고 옴
			String group_nos="";
			for(int i=0;i<al.size();i++){
				if(al.get(i).get_group_uid()!=null){
					group_nos+=al.get(i).get_group_no()+"|";
				}
			}
			hm.clear();
			hm.put("cmd", "data_load");
			hm.put("group_nos", group_nos);
			IdealWorldCupUrlHttp http =new IdealWorldCupUrlHttp(pkchActionUrl);
			hm=http.sendPost(hm);
			resultXml=hm.get("result");
			hm.clear();
			al.clear();
		}
		return null;
	}	

	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		dialog.dismiss();
		
		//웹을 통해서 받아오도록 수정함
		//XML 파싱후 배열로 저장
		
		XMLReader reader;
		try {
			reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
			//만들어진 핸들러 객체 생성, XML reader에 핸들러 객체를 적용시킴
			IdealWorldcupXmlHandlerForLoadingData handler= new IdealWorldcupXmlHandlerForLoadingData();
			reader.setContentHandler(handler);
			reader.parse(new InputSource(new ByteArrayInputStream(resultXml.getBytes("utf-8") ) ) );
			hhm= handler.getLists();
			arr_ide=hhm.get("client_item");	// 데이타를 다시 저장하기 위해서 해당 데이터만 뺴온다.
			item_total_size=0;
			//전체 용량 구하기
			for(int i=0;i<arr_ide.size();i++){
				item_total_size+= arr_ide.get(i).get_item_size();
			}
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(arr_ide.size()>0){
			dialog= new ProgressDialog(context);
			dialog.setProgressStyle(dialog.STYLE_HORIZONTAL);
			dialog.setTitle("Data Loading..");
			dialog.setMessage("데이타를 받아오는 중입니다.");
			dialog.setProgress(0);
			dialog.setMax(item_total_size);
			dialog.show();
			downThread = new DataSyncDownloadThread(handler);
			downThread.start();
		}
		else{
			DataSyncDBInsert task= new DataSyncDBInsert();
			task.execute(null);
		}
	}
	//데이타를 웹을 통해서 받아오게함
	private class DataSyncDownloadThread extends Thread{
		Handler mHandler;
		final static int STATE_DONE=0;
		final static int STATE_RUNNING=1;
		int mState;
		int total;
		int idx=0;
		
		Url2File u2f=null;
		IdealWorldcupLoadingDataElement ide=null;
		int item_no=0;
		String item_path=null;
		String item_path2=null;
		int item_size=0;
		
		public DataSyncDownloadThread(Handler h ) {
			this.u2f= new Url2File();
			mHandler=h;
		}
		public void run(){
			mState= STATE_RUNNING;
			total=0;
			while(mState==STATE_RUNNING){
				if(idx<arr_ide.size()){
					ide=arr_ide.get(idx);
					item_path=ide.get_item_path();
					item_no=ide.get_item_no();
					item_size=ide.get_item_size();	// 이미지 크기 추가
					
					if(item_size>0){
						item_path2=pkchFileSaveDir+"img/"+item_no;
						u2f.getFileName(item_path, pkchFileSaveDir+"img/", item_no+"");
						ide.set_item_path(item_path2);
					}
					
					total+=item_size;
					Message msg= mHandler.obtainMessage();
					Bundle b= new Bundle();
					b.putInt("total",total);
					msg.setData(b);
					mHandler.sendMessage(msg);
				}
				idx++;
			}
		}
		
		public void setState(int state){
			mState=state;
		}
	}
	
	//핸들러를 정의하여 스레드가 메시지를 보낼경우 프로그레스를 업데이트 함
	final Handler handler = new Handler(){
		public void handleMessage(Message msg){
			int total=msg.getData().getInt("total");
			dialog.setProgress(total);
			if(total>=item_total_size){
				dialog.dismiss();
				downThread.setState(downThread.STATE_DONE);
				hhm.remove("client_item");
				hhm.put("client_item", arr_ide);	// 아이템 데이터만 다시 저장한다.
				
				DataSyncDBInsert task= new DataSyncDBInsert();
				task.execute(null);
			}
		}
	};

	
	//db에 데이타를 삽입함
	private class DataSyncDBInsert extends AsyncTask<Void, String, Void>{
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = ProgressDialog.show(context, "Data Loading..","사용자 정보를 저장 중입니다.", true);
		}
		@Override
		protected Void doInBackground(Void... params) {
			//배열에 해당되는 내용 db에 저장(모두 날리고 새롭게 저장함)
			IdealWorldcupSQLHandler sqlHandler=IdealWorldcupSQLHandler.getInstance(context);
			SQLiteDatabase dbw= sqlHandler.getWritableDatabase();
			String query=null;
			Iterator<String> iter= hhm.keySet().iterator();
			String key=null;
			String a=null;
			String b=null;
			String c=null;
			String d=null;
			while(iter.hasNext()){
				key= iter.next();
				arr_ide= hhm.get(key);
				if(key.equals("client_item")){
					//client_item에 있는 행 데이타를 모두 삭제함
					query="DELETE FROM "+IdealWorldcupAppTables.ClientItem.tableName;
					dbw.execSQL(query);
					for(int i=0;i<arr_ide.size();i++){
						a= arr_ide.get(i).get_item_no()+"";
						b= arr_ide.get(i).get_item_name()+"";
						c= arr_ide.get(i).get_item_path()+"";
						query="INSERT INTO "+IdealWorldcupAppTables.ClientItem.tableName
								+" (item_no, item_name, item_path) values(\""+a+"\",\""+b+"\",\""+c+"\" )";
						dbw.execSQL(query);
					}
				}
				else if(key.equals("client_question")){
					query="DELETE FROM "+IdealWorldcupAppTables.ClientQuestion.tableName;
					dbw.execSQL(query);
					for(int i=0;i<arr_ide.size();i++){
						a= arr_ide.get(i).get_qs_no()+"";
						b= arr_ide.get(i).get_qs_title();
						c= arr_ide.get(i).get_qs_round()+"";
						query="INSERT INTO "+IdealWorldcupAppTables.ClientQuestion.tableName
								+" (qs_no, qs_title, qs_round) values(\""+a+"\",\""+b+"\",\""+c+"\")";
						//Log.e("pkch",query);
						dbw.execSQL(query);
					}
				}
				else if(key.equals("group_item")){
					query="DELETE FROM "+IdealWorldcupAppTables.GroupItem.tableName;
					dbw.execSQL(query);
					for(int i=0;i<arr_ide.size();i++){
						a= arr_ide.get(i).get_client_group_group_no()+"";
						b= arr_ide.get(i).get_client_item_item_no()+"";
						query="INSERT INTO "+IdealWorldcupAppTables.GroupItem.tableName
								+" (client_group_group_no, client_item_item_no) values(\""+a+"\",\""+b+"\")";
						dbw.execSQL(query);
					}
				}
				else if(key.equals("group_question")){
					query="DELETE FROM "+IdealWorldcupAppTables.GroupQuestion.tableName;
					dbw.execSQL(query);
					for(int i=0;i<arr_ide.size();i++){
						a= arr_ide.get(i).get_client_group_group_no()+"";
						b= arr_ide.get(i).get_client_question_qs_no()+"";
						query="INSERT INTO "+IdealWorldcupAppTables.GroupQuestion.tableName
								+" (client_group_group_no, client_question_qs_no) values(\""+a+"\",\""+b+"\")";
						dbw.execSQL(query);
					}
				}
				else if(key.equals("question_item")){
					query="DELETE FROM "+IdealWorldcupAppTables.QuestionItem.tableName;
					dbw.execSQL(query);
					for(int i=0;i<arr_ide.size();i++){
						a= arr_ide.get(i).get_client_question_qs_no()+"";
						b= arr_ide.get(i).get_client_item_item_no()+"";
						c= arr_ide.get(i).get_hits()+"";
						query="INSERT INTO "+IdealWorldcupAppTables.QuestionItem.tableName
								+" (client_question_qs_no, client_item_item_no, hits) values(\""+a+"\",\""+b+"\",\""+c+"\")";
						dbw.execSQL(query);
					}
				}
			}
			return null;
		}
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			dialog.dismiss();
			Toast.makeText(context, "데이타 동기화 작업을 마무리 하였습니다.", Toast.LENGTH_SHORT).show();
		}
	}
}

