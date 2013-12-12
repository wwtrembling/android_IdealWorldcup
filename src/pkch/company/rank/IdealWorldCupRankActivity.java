package pkch.company.rank;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import pkch.company.R;
import pkch.company.domain.IdealWolrdcupQuestionElement;
import pkch.company.domain.IdealWorldcupElement;
import pkch.company.domain.IdealWorldcupLoadingDataElement;
import pkch.company.lib.IdealWorldCupUrlHttp;
import pkch.company.lib.Url2File;
import pkch.company.lib.cacheManager.ImageRepository;
import pkch.company.lib.cacheManager2.CustomApplication;
import pkch.company.lib.cacheManager2.ImageCache;
import pkch.company.lib.saxSupplier.IdealWorldcupXmlHandlerForLoadingData;
import pkch.company.lib.sql.IdealWorldcupAppTables;
import pkch.company.lib.sql.IdealWorldcupSQLHandler;
import pkch.company.member.MemberLoginActivity;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class IdealWorldCupRankActivity extends Activity {
	private final String pkchActionUrl = "http://wwtrembling.cafe24.com/idealWorldcup/action.php";
	private static ProgressDialog dialog;
	private static CustomApplication imgCache=null;	// 이미지 캐슁
	private ArrayList<IdealWorldcupElement> items;
	private IdealWolrdcupQuestionElement iqe;
	private IdealWorldcupSQLHandler sqlHandler;
	private SQLiteDatabase db;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rank_list);
		init_rank();
	}
	
	// 시작!!!
	private void init_rank(){ 
		imgCache= new CustomApplication();
		items=new ArrayList<IdealWorldcupElement>();
		sqlHandler=IdealWorldcupSQLHandler.getInstance(this);
		
		//인텐트 전달값 받아오기
		Intent intent = getIntent();
		Serializable tmp =null;
		tmp=intent.getSerializableExtra("iqe");
		iqe = (IdealWolrdcupQuestionElement) tmp;
		dialog = ProgressDialog.show(this, "Notice", "설문조사 결과를 받아오고 있습니다.", true);
		
		//인터넷이 연결이 되어 있는지 확인
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		boolean isWifiAvail = ni.isAvailable();
		boolean isWifiConn = ni.isConnected();
		ni = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		boolean isMobileAvail = ni.isAvailable();
		boolean isMobileConn = ni.isConnected();
		
		//showRankAdapter();
		
		
		//인터넷이 연결되어 있지 않을 경우	            		  
		if(isWifiConn==false && isMobileConn==false)
		{
			showRankAdapter();
			//Log.e("pkch","local");
			dialog.dismiss();
		}
		else{
			//Log.e("pkch","wifi");
			RanktaskgetHttpPostTask task= new RanktaskgetHttpPostTask();
			task.execute(null);
		}
		
	}
	
	private class RanktaskgetHttpPostTask extends AsyncTask<String, Void, String> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}
		
		@Override
		protected String doInBackground(String... params) {
			//설문조사 결과값 가지고 오기
			HashMap<String, String> hm= new HashMap<String, String>();
			hm.put("cmd", "data_load_item_hits");
			hm.put("qs_no", ""+iqe.getQsNo());
			IdealWorldCupUrlHttp http =new IdealWorldCupUrlHttp(pkchActionUrl);
			hm=http.sendPost(hm);
			String xmlstr= hm.get("result");
			hm.clear();
			
			//XML 파싱후 배열로 저장
			HashMap<String, ArrayList<IdealWorldcupLoadingDataElement>> hhm =null;
			XMLReader reader;
			try {
				ArrayList<IdealWorldcupLoadingDataElement> arr_ide=null;
				reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
				//만들어진 핸들러 객체 생성, XML reader에 핸들러 객체를 적용시킴
				IdealWorldcupXmlHandlerForLoadingData handler= new IdealWorldcupXmlHandlerForLoadingData();
				reader.setContentHandler(handler);
				reader.parse(new InputSource(new ByteArrayInputStream(xmlstr.getBytes("utf-8") ) ) );
				hhm= handler.getLists();
				//배열에 해당되는 내용 db에 저장(모두 날리고 새롭게 저장함)
				IdealWorldcupSQLHandler sqlHandler=IdealWorldcupSQLHandler.getInstance(IdealWorldCupRankActivity.this);
				SQLiteDatabase db= sqlHandler.getWritableDatabase();
				String query=null;
				Iterator<String> iter= hhm.keySet().iterator();
				String key=null;
				int a=0;
				int b=0;
				int c=0;
				while(iter.hasNext()){
					key= iter.next();
					arr_ide= hhm.get(key);
					if(key.equals("question_item")){
						//client_item에 있는 행 데이타를 모두 삭제함 >> update 로 수정
						//query="DELETE FROM "+IdealWorldcupAppTables.QuestionItem.tableName+" where client_question_qs_no="+iqe.getQsNo();
						//Log.e("pkch",query);
						//db.execSQL(query);
						for(int i=0;i<arr_ide.size();i++){
							a= arr_ide.get(i).get_qs_no();
							b= arr_ide.get(i).get_item_no();
							c= arr_ide.get(i).get_hits();
							/*
							query="INSERT INTO "+IdealWorldcupAppTables.QuestionItem.tableName
									+" (client_question_qs_no, client_item_item_no, hits) values("+a+","+b+","+c+")";
							*/
							query ="UPDATE "+IdealWorldcupAppTables.QuestionItem.tableName+" set hits="+c+" where client_question_qs_no="+a+" and client_item_item_no="+b;
							//Log.e("pkch",query);
							db.execSQL(query);
						}
					}
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
			return null;
		}
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			showRankAdapter();
		}
		
	}
	
	
	//rank 보여주기
	private void showRankAdapter(){
		dialog.dismiss();
		db= sqlHandler.getReadableDatabase();
		//rank 데이타를 Hits 가 높은 순으로 보여주되 hits가 0 이상일 경우만 뽑아온다
		String query="SELECT client_item.[item_no], client_item.[item_name], client_item.[item_path], question_item.[hits] " +
				"FROM client_item, question_item " +
				"where client_item.[item_no]=question_item.[client_item_item_no] and question_item.[client_question_qs_no]='"+iqe.getQsNo()+"' and question_item.[hits]>0 " +
						"order by question_item.[hits] desc limit 0,50 ";
		//Log.e("pkch",query);
		Cursor cursor= db.rawQuery(query, null);
		IdealWorldcupElement item= null;
		if(cursor!=null){
			cursor.moveToFirst();
			int cur_hits=0;
			int prev_hits=0;
			int rank=1;
			//Log.e("pkch", cursor.getColumnCount()+"");
			while(!cursor.isAfterLast()){
				item=new IdealWorldcupElement();
				for(int i = 0 ; i < cursor.getColumnCount() ; i++ ){
					//Log.e("pkch",cursor.getColumnName(i)+" , "+cursor.getString(i));
					if(cursor.getColumnName(i).equals("hits")){item.setHits(cursor.getInt(i));cur_hits=cursor.getInt(i);}
					else if(cursor.getColumnName(i).equals("item_no")){item.setItemNo(cursor.getInt(i));}
					else if(cursor.getColumnName(i).equals("item_name")){item.setItemName(cursor.getString(i));}
					else if(cursor.getColumnName(i).equals("item_path")){item.setItemDir(cursor.getString(i));}
				}
				//hits 가 같을 경우에는 rank를 매기지 않는다.
				if(cur_hits==prev_hits){	item.setRank(0);}
				else{item.setRank(rank);}
				prev_hits=cur_hits;
				rank++;
				items.add(item);
				cursor.moveToNext();
			}
			cursor.close();
		}
		ListView lv= (ListView)findViewById(R.id.rank_list);
		RankAdapter adapter= new RankAdapter(R.layout.rank_row);
		lv.setAdapter(adapter);
	}

	private class RankAdapter extends BaseAdapter{
		private Context context;
		private LayoutInflater inflater;
		private int layoutId;
		
		public RankAdapter(int layoutId){
			this.context=IdealWorldCupRankActivity.this;
			this.layoutId=layoutId;
			this.inflater= (LayoutInflater)this.context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		}
		public int getCount() {
			return items.size();
		}

		public Object getItem(int position) {
			return items.get(position);
		}

		public long getItemId(int position) {
			return items.get(position).getItemNo();
		}

		public View getView(int position, View v, ViewGroup parent) {
			if(v==null){
				v=inflater.inflate(layoutId, parent, false);
			}
			IdealWorldcupElement item= items.get(position);
			TextView tv= null;
			tv=(TextView)v.findViewById(R.id.rank_name);
			tv.setText(item.getItemName());
			tv=(TextView)v.findViewById(R.id.rank_hits);
			tv.setText(""+item.getHits());
			if(item.getRank()>0){
				tv=(TextView)v.findViewById(R.id.rank_row_rankno);
				tv.setText(item.getRank()+" 위");
			}
			ImageView iv= (ImageView)v.findViewById(R.id.rank_row_img);
			//이미지 캐슁 시작!!
			Bitmap bm= null;
			bm= imgCache.getImage(items.get(position).getItemDir());
			if(bm==null){
				RankImageTask task= new RankImageTask(item.getItemDir(), item.getItemNo(), iv);
				task.execute(null);
			}else{
				iv.setImageBitmap(bm);
			}
			return v;
		}
	}
	
	private class RankImageTask extends AsyncTask<String, Void, Bitmap>{
		private String imgDir;
		private ImageView iv;
		private int itemNo;
		public RankImageTask(String imgDir, Integer itemNo, ImageView iv){
			this.imgDir=imgDir;
			this.iv=iv;
			this.itemNo=itemNo;			
		}
		
		@Override
		protected Bitmap doInBackground(String... params) {
			Bitmap bm = BitmapFactory.decodeFile(imgDir);
			return bm;
		}
		@Override
		protected void onPostExecute(Bitmap bm) {
			super.onPostExecute(bm);
			this.iv.setImageBitmap(bm);	
			imgCache.addImage(imgDir, bm);
		}
	}
	
}
