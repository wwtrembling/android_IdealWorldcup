package pkch.company.pic;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import pkch.company.IdealWorldcupMainActivity;
import pkch.company.R;
import pkch.company.domain.IdealWorldcupLoadingDataElement;
import pkch.company.lib.HttpRequestor;
import pkch.company.lib.Url2File;
import pkch.company.lib.saxSupplier.IdealWorldcupXmlHandlerForLoadingData;
import pkch.company.lib.sql.IdealWorldcupAppTables;
import pkch.company.lib.sql.IdealWorldcupSQLHandler;
import pkch.company.member.MemberAction;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

//이미지 등록/수정 클래스
public class IdealWorldcupPicModifyActivity extends Activity{
	private final String pkchActionUrl = "http://wwtrembling.cafe24.com/idealWorldcup/action.php";
	private final String pkchFileSaveDir = "/data/data/pkch.company/";
	private String cmd=null;
	private ImageView iv=null;
	private Intent intent=null;
	private static ProgressDialog dialog=null;
	private static int item_no=0;
	private static int group_no=0;
	private static String group_uid=null;
	private static String item_name=null;
	private static String item_path=null;
	private static ArrayList<IdealWorldcupLoadingDataElement> arr_members=null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pic_modify);
		init();
	}
	
	private void init(){
		intent= this.getIntent();
		Serializable tmp= intent.getSerializableExtra("cmd");
		cmd= (String)tmp;
		
		//이미지 등록할 경우
		if(cmd.equals("regist")){
			//현재 로그인 된 사용자의 이름을 세팅한다
			MemberAction ma= new MemberAction(this);
			arr_members= ma.getAllmembers(false);
			ArrayList<String> a=new ArrayList<String>();
			
			for(int i=0;i<arr_members.size();i++){
				a.add(arr_members.get(i).get_group_uid());
			}
			ArrayAdapter<String> aa= new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, a);
			aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			Spinner spin= (Spinner)findViewById(R.id.pic_modify_lo2_ro1_spinnerowner);
			spin.setAdapter(aa);
			spin.setPrompt("Choose");
			spin.setOnItemSelectedListener(null);

			//등록 이벤트 줌
			findViewById(R.id.pic_modify_image).setOnClickListener(clickListener);
			findViewById(R.id.pic_modify_lo2_ro1_btnreload).setOnClickListener(clickListener);
			findViewById(R.id.pic_modify_lo2_ro1_btnregist).setOnClickListener(clickListener);
			findViewById(R.id.pic_modify_lo2_ro1_btnclose).setOnClickListener(clickListener);
			
			//이미지를 가지고 온다
			Intent intent_sd=null;
			intent_sd = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
			startActivityForResult(intent_sd, 5001);	// 등록할 경우 리턴값은 5001
		}
		//이미지 수정할 경우
		else if(cmd.equals("modify")){
			item_no= intent.getIntExtra("item_no", 0);	// 이미지 번호
			item_name=intent.getStringExtra("item_name");	// 이미지 이름
			item_path=intent.getStringExtra("item_path");	// 이미지 경로
			group_no=intent.getIntExtra("group_no",0);	// 이미지 소유자 고유번호
			group_uid=intent.getStringExtra("group_uid");	// 이미지 소유자
			
			//이미지 불러오기 버튼 DISABLE
			this.findViewById(R.id.pic_modify_lo2_ro1_btnreload).setVisibility(View.INVISIBLE);
			//이미지 보여주기
			iv=(ImageView) this.findViewById(R.id.pic_modify_image);
			Bitmap bm = BitmapFactory.decodeFile(item_path);
			iv.setImageBitmap(bm);
			
			//이미지 이름 세팅
			EditText ed= (EditText) this.findViewById(R.id.pic_modify_lo2_ro1_editname);
			ed.setText(item_name);
			
			//이미지 소유자 세팅 > 소유자는 한명
			ArrayList<String> a=new ArrayList<String>();
			a.add(group_uid);
			ArrayAdapter<String> aa= new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, a);
			aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			Spinner spin= (Spinner)findViewById(R.id.pic_modify_lo2_ro1_spinnerowner);
			spin.setAdapter(aa);
			spin.setPrompt("Choose");
			spin.setOnItemSelectedListener(null);
			
			//REGIST  >> Modify 수정
			Button btn_modify=(Button)findViewById(R.id.pic_modify_lo2_ro1_btnregist);
			btn_modify.setText("MODIFY");
			
			//수정버튼에 이벤트 줌
			findViewById(R.id.pic_modify_lo2_ro1_btnregist).setOnClickListener(clickListener);
			findViewById(R.id.pic_modify_lo2_ro1_btnclose).setOnClickListener(clickListener);
			
		}
	}
	
	private View.OnClickListener clickListener= new View.OnClickListener() {
		public void onClick(View v) {			
			switch(v.getId()){
				case R.id.pic_modify_image:{	//이미지를 클릭했을 경우
					init();					
					break;
				}
				case R.id.pic_modify_lo2_ro1_btnreload:{	// 이미지 다시 불러오기
					init();
					break;
				}
				case R.id.pic_modify_lo2_ro1_btnregist :{	//등록/수정
					//이미지경로 및 이미지 이름이 제대로 입력되었는지 확인
					EditText et= (EditText)findViewById(R.id.pic_modify_lo2_ro1_editname);
					item_name= et.getText().toString();
					
					//선택된 아이디 가지고 오기
					Spinner spin= (Spinner)findViewById(R.id.pic_modify_lo2_ro1_spinnerowner);
					group_uid=spin.getSelectedItem().toString();
					
					//등록된 경우에만 확인 ##################################### 미구현됨
					if(cmd.equals("regist")){
						//해당 아이디로 등록된 이미지갯수가 100개가 넘지 않는지 확인
						
						
						
						
					}
					//HTTP 통신을 하여 파일을  서버로 전송한다.					
					if(item_name.equals("")==true){
						Toast.makeText(IdealWorldcupPicModifyActivity.this, "이미지 이름을 넣어주세요.", Toast.LENGTH_SHORT).show();
						et.requestFocus();
					}
					else{
						//HTTP 통신을 하여 파일을  서버로 전송한다.
						IdealWorldcupPicModifyUploadTask task= new IdealWorldcupPicModifyUploadTask();
						task.execute(null);
					}
					
					break;
				}
				case R.id.pic_modify_lo2_ro1_btnclose:{
					finish();
					break;
				}
			}
		}
	};
		
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		//이미지 등록시 처리
		if(requestCode==5001){
			if( resultCode==RESULT_OK){
				iv=(ImageView) this.findViewById(R.id.pic_modify_image);
				Uri uri= data.getData();
				item_path= getPath(uri);
				Bitmap bm = BitmapFactory.decodeFile(item_path);
				iv.setImageBitmap(bm);
				
				// 이미지 페이드인 효과
				AlphaAnimation fade_in = new AlphaAnimation(0, 1);
				fade_in.setDuration(700);
				fade_in.setAnimationListener(new AnimationListener() {
					public void onAnimationStart(Animation animation) {
						iv.setVisibility(View.INVISIBLE);
					}
	
					public void onAnimationRepeat(Animation animation) {
					}
	
					public void onAnimationEnd(Animation animation) {
						iv.setVisibility(View.VISIBLE);
					}
				});
				iv.startAnimation(fade_in);
			}
			else if(resultCode==RESULT_CANCELED){
				finish();
			}
		}
	}
	
	private String getPath(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(uri, projection, null, null, null);
		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}
	
	class IdealWorldcupPicModifyUploadTask extends AsyncTask<String, Void, String>{
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			//데이타 업로드 시작
			dialog= ProgressDialog.show(IdealWorldcupPicModifyActivity.this,"", "데이타를 변환 및 전송 중입니다.", true);
		}
		
		@Override
		protected String doInBackground(String... params) {
			//데이타 업로드
			URL url;
			String xmlstr=null;
			if(cmd.equals("regist")){
				try {
					url = new URL(pkchActionUrl);
					HttpRequestor request= new HttpRequestor(url,4);
					request.addParameter("cmd", "item_regist");
					request.addParameter("group_uid", group_uid);	//이미지 소유자
					//그룹 넘버 가지고 오기
					for(int i=0;i<arr_members.size();i++){
						if(arr_members.get(i).get_group_uid().equals(group_uid)){
							request.addParameter("group_no", ""+arr_members.get(i).get_group_no());	//이미지 소유자
							break;
						}
					}
					request.addParameter("item_name", item_name);
					request.addFile("item_path", new File(item_path));
					InputStream is= request.sendMultipartPost();
					BufferedReader br= new BufferedReader(new InputStreamReader(is));
					String line=null;
					StringBuffer sb= new StringBuffer();
					while((line=br.readLine())!=null){
						sb.append(line);
					}
					xmlstr=sb.toString();
					br.close();
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				//저장완료된 XML 데이타 db 저장 및 이미지 다운로드
				HashMap<String, ArrayList<IdealWorldcupLoadingDataElement>> hhm =null;
				XMLReader reader;
				try {
					reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
					//만들어진 핸들러 객체 생성, XML reader에 핸들러 객체를 적용시킴
					IdealWorldcupXmlHandlerForLoadingData handler= new IdealWorldcupXmlHandlerForLoadingData();
					reader.setContentHandler(handler);
					reader.parse(new InputSource(new ByteArrayInputStream(xmlstr.getBytes("utf-8") ) ) );
					hhm= handler.getLists();
					
					//파일받아와서 데이타에 저장 및 새로운 이미지 경로 수정
					//해당 디렉토리에 있는 파일을 삭제 해야 하는지??
					Url2File u2f= new Url2File();
					ArrayList<IdealWorldcupLoadingDataElement> arr_ide= hhm.get("client_item");
					IdealWorldcupLoadingDataElement ide=null;
					String item_path=null;
					String item_no=null;
					String item_path2=null;
					for(int i=0;i<arr_ide.size();i++){
						ide=arr_ide.get(i);
						item_path=ide.get_item_path();
						item_no=ide.get_item_no()+"";
						item_path2=pkchFileSaveDir+"img/"+item_no;
						u2f.getFileName(item_path, pkchFileSaveDir+"img/", item_no);
						ide.set_item_path(item_path2);
					}
					
					//배열에 해당되는 내용 db에 저장(모두 날리고 새롭게 저장함)
					IdealWorldcupSQLHandler sqlHandler=IdealWorldcupSQLHandler.getInstance(IdealWorldcupPicModifyActivity.this);
					SQLiteDatabase db= sqlHandler.getReadableDatabase();
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
							for(int i=0;i<arr_ide.size();i++){
								a= arr_ide.get(i).get_item_no()+"";
								b= arr_ide.get(i).get_item_name()+"";
								c= arr_ide.get(i).get_item_path()+"";
								query="INSERT INTO "+IdealWorldcupAppTables.ClientItem.tableName
										+" (item_no, item_name, item_path) values('"+a+"','"+b+"','"+c+"' )";
								db.execSQL(query);
							}
						}
						else if(key.equals("group_item")){
							for(int i=0;i<arr_ide.size();i++){
								a= arr_ide.get(i).get_client_group_group_no()+"";
								b= arr_ide.get(i).get_client_item_item_no()+"";
								query="INSERT INTO "+IdealWorldcupAppTables.GroupItem.tableName
										+" (client_group_group_no, client_item_item_no) values('"+a+"','"+b+"')";
								db.execSQL(query);
							}
						}
					}				
				} catch (SAXException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ParserConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}			
				return null;
			}
			else if(cmd.equals("modify")){
				try {
					//웹전송
					url = new URL(pkchActionUrl);
					HttpRequestor request= new HttpRequestor(url,4);
					request.addParameter("cmd", "item_modify_name");
					request.addParameter("item_no", item_no+"");
					request.addParameter("item_name", item_name);
					InputStream is= request.sendPost();
					BufferedReader br= new BufferedReader(new InputStreamReader(is));
					String line=null;
					StringBuffer sb= new StringBuffer();
					while((line=br.readLine())!=null){
						sb.append(line);
					}
					xmlstr=sb.toString();
					br.close();
					
					//받은 데이타 DB에 저장
					IdealWorldcupSQLHandler sqlHandler=IdealWorldcupSQLHandler.getInstance(IdealWorldcupPicModifyActivity.this);
					SQLiteDatabase db= sqlHandler.getWritableDatabase();
					String query="UPDATE "+IdealWorldcupAppTables.ClientItem.tableName
							+" SET item_name=\""+item_name.replace("\"","")+"\" where item_no="+item_no;
					db.execSQL(query);
					
					return xmlstr;
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			dialog.dismiss();
			if(cmd.equals("regist")){
				//사진 등록 완료 후 사진관리에 다시 들어감
				intent= getIntent();
				setResult(-1, intent);
				finish();
			}
			else if(cmd.equals("modify")){
				String msg=null;
				if(result.equals("ok")){
					msg="수정이 완료되었습니다.";
					IdealWorldcupPicActivity.pic_status_flag="fromPicModifyActivity";
				}
				else {
					msg="수정 작업에서 에러가 발생하였습니다.";
				}
				Toast.makeText(IdealWorldcupPicModifyActivity.this, msg, Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}
}
