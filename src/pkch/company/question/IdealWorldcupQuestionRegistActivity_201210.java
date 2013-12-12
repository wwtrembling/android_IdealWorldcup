package pkch.company.question;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import pkch.company.R;
import pkch.company.domain.IdealWorldcupLoadingDataElement;
import pkch.company.lib.IdealWorldCupUrlHttp;
import pkch.company.lib.saxSupplier.IdealWorldcupXmlHandlerForLoadingData;
import pkch.company.lib.sql.IdealWorldcupAppTables;
import pkch.company.lib.sql.IdealWorldcupSQLHandler;
import pkch.company.member.MemberAction;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class IdealWorldcupQuestionRegistActivity_201210 extends Activity{
	private ArrayList<IdealWorldcupLoadingDataElement> arr_members=null;
	private AlertDialog.Builder alert=null;
	private MemberAction ma=null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.question_regist);
		init();
	}
	protected void init(){
		setTitle("질문을 입력해 주세요.");
		
		//질문자 선택하도록 Spinner 노출
		ma = new MemberAction(this);
		arr_members = ma.getAllmembers(true);		
		ArrayList<String> a = new ArrayList<String>();
		for (int i = 0; i < arr_members.size(); i++) {
			a.add(arr_members.get(i).get_group_uid());
		}
		ArrayAdapter<String> aa = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, a);
		aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		Spinner spin = (Spinner)findViewById(R.id.question_regist_spinner1);
		spin.setAdapter(aa);
		spin.setPrompt("선택해 주세요.");
		
		//라운드 선택
		ArrayList<String> a2= new ArrayList<String>();
		a2.add("4");
		a2.add("8");
		a2.add("16");
		a2.add("32");
		a2.add("64");
		Spinner spin2= (Spinner)findViewById(R.id.question_regist_spinner2);
		ArrayAdapter<String> aa2= new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, a2);
		aa2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spin2.setAdapter(aa2);
		spin2.setPrompt("선택해 주세요.");
		
		
		findViewById(R.id.question_regist_ok).setOnClickListener(qclick);
		findViewById(R.id.question_regist_no).setOnClickListener(qclick);
		
	}

	final View.OnClickListener qclick= new View.OnClickListener() {
		public void onClick(View v) {
			switch(v.getId()){
				case R.id.question_regist_ok:
					EditText txt= (EditText)findViewById(R.id.question_regist_qsname);
					String str=txt.getText().toString();
					if(str.equals("") && str.trim().equals("")){
						Toast.makeText(IdealWorldcupQuestionRegistActivity_201210.this,"질문을 입력해 주세요.",Toast.LENGTH_SHORT).show();
					}
					else{
						String  qs_title=str.trim();
						Spinner spin=(Spinner)findViewById(R.id.question_regist_spinner1);
						String group_uid=(String) spin.getItemAtPosition(spin.getSelectedItemPosition());
						Spinner spin2=(Spinner)findViewById(R.id.question_regist_spinner2);
						String qs_round=(String) spin2.getItemAtPosition(spin.getSelectedItemPosition());
						QuestionRegistTask task = new QuestionRegistTask( qs_title, qs_round, group_uid);
						task.execute();
					}
					break;
				case R.id.question_regist_no:
					finish();
					break;
			}
		}
	};

	

	//질문등록 task 수행
	class QuestionRegistTask extends AsyncTask<Void, String, String> {
		String qs_title = null;
		String qs_round=null;
		String group_uid=null;
		ProgressDialog dialog=null;
		
		//제목, 라운드, 소유자아이디
		public QuestionRegistTask(String qs_title, String qs_round, String group_uid) {
			this.qs_title = qs_title;
			this.qs_round=qs_round;
			this.group_uid=group_uid;
		}
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = ProgressDialog.show(IdealWorldcupQuestionRegistActivity_201210.this, "NOTICE", "질문을 저장 중입니다.", true);
		}

		@Override
		protected String doInBackground(Void... params) {
			// 사용자 아이디에 해당하는 no를 가지고 온다.
			HashMap<String,String> hm= ma.getUserInfo(group_uid);
			String group_no= hm.get("group_no");
			if(group_no==null){
				return "error";
			}
			else{
				// 웹을 통해서 저장함
				HashMap<String,String> hm2= new HashMap<String, String>();
				HashMap<String,String> hm_result = new HashMap<String, String>();
				hm2.put("cmd", "question_regist");
				hm2.put("group_no", group_no);
				hm2.put("qs_round", qs_round);
				hm2.put("qs_title", qs_title);
				IdealWorldCupUrlHttp http =new IdealWorldCupUrlHttp();
				hm_result=http.sendPost(hm2);
				String strxml= hm_result.get("result");
				if(strxml.equals("error")){
					return "error";
				}
				else{
					// 정상적으로 등록하였을 경우에 데이터를 DB에 저장
					try {
						XMLReader reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
						IdealWorldcupXmlHandlerForLoadingData handler= new IdealWorldcupXmlHandlerForLoadingData();
						reader.setContentHandler(handler);
						reader.parse(new InputSource(new ByteArrayInputStream(strxml.getBytes("utf-8") ) ) );
						HashMap<String, ArrayList<IdealWorldcupLoadingDataElement>> hhm= handler.getLists();
						String q=null;
						IdealWorldcupSQLHandler sqlHandler=IdealWorldcupSQLHandler.getInstance(IdealWorldcupQuestionRegistActivity_201210.this);
						SQLiteDatabase dbw= sqlHandler.getWritableDatabase();
						//그룹-질문 테이블 데이타 추가
						ArrayList<IdealWorldcupLoadingDataElement> hm_result1= hhm.get("group_question");
						q="INSERT INTO "+IdealWorldcupAppTables.GroupQuestion.tableName
								+" (client_group_group_no, client_question_qs_no) values('"+hm_result1.get(0).get_client_group_group_no()+"','"+hm_result1.get(0).get_client_question_qs_no()+"')";
						dbw.execSQL(q);
						//질문 테이블 데이타 추가
						ArrayList<IdealWorldcupLoadingDataElement> hm_result2= hhm.get("client_question");
						q="INSERT INTO "+IdealWorldcupAppTables.ClientQuestion.tableName
								+" (qs_no, qs_title, qs_round) " +
								"values('"+hm_result2.get(0).get_qs_no()+"','"+hm_result2.get(0).get_qs_title()+"','"+hm_result2.get(0).get_qs_round()+"')";
						dbw.execSQL(q);
					} catch (SAXException e) {
						e.printStackTrace();
					} catch (ParserConfigurationException e) {
						e.printStackTrace();
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				return "ok";
			}
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			//화면 갱신
			dialog.dismiss();
			if(result.equals("ok")){
				Toast.makeText(IdealWorldcupQuestionRegistActivity_201210.this, "성공적으로 저장하였습니다.", Toast.LENGTH_SHORT).show();
				Intent intent= getIntent();
				setResult(RESULT_OK, intent);
				finish();
			}
			else{
				Toast.makeText(IdealWorldcupQuestionRegistActivity_201210.this, "저장에 실패하였습니다.", Toast.LENGTH_SHORT).show();
			}
		}

	}
	

}
