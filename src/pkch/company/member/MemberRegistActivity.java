package pkch.company.member;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pkch.company.R;
import pkch.company.lib.IdealWorldCupUrlHttp;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MemberRegistActivity extends Activity {
	private final String pkchDataXmlUrl = "http://wwtrembling.cafe24.com/idealWorldcup/data.php";
	private final String pkchActionUrl = "http://wwtrembling.cafe24.com/idealWorldcup/action.php";
	private String group_uid = null;
	private String group_pwd = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.member_regist);
		bindEvent();
	}

	private void bindEvent() {
		findViewById(R.id.mregist_btn).setOnClickListener(clickListener);
		findViewById(R.id.mcancle_btn).setOnClickListener(clickListener);
	}

	View.OnClickListener clickListener = new View.OnClickListener() {
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.mregist_btn:
				boolean subchk = true;
				// 아이디값 확인
				EditText tmp = null;
				tmp = (EditText) findViewById(R.id.groupuid_edt);
				group_uid = tmp.getText().toString();
				
				//아이디 검사
				if (group_uid == null) {
					Toast.makeText(MemberRegistActivity.this, "아이디를 입력해 주시기 바랍니다.", Toast.LENGTH_SHORT).show();
					tmp.requestFocus();
					subchk = false;
				}
				
				//아이디 검사(0와 문자로만 이루어짐)
				Pattern p= Pattern.compile("([a-zA-Z0-9])*");
				Matcher m=null;
				boolean b=false;
				m= p.matcher(group_uid);
				b=m.matches();
				if(b==false){
					Toast.makeText(MemberRegistActivity.this, "아이디는 영문 대소문자와 숫자만 입력하실 수 있습니다.", Toast.LENGTH_SHORT).show();
				}
				
				//패스워드 검사
				tmp = (EditText) findViewById(R.id.grouppwd2_edt);
				String pwd1 = tmp.getText().toString();
				tmp = (EditText) findViewById(R.id.grouppwd1_edt);
				String pwd2 = tmp.getText().toString();


				m= p.matcher(pwd1);
				b=m.matches();
				if(b==false){
					Toast.makeText(MemberRegistActivity.this, "패스워드는 영문 대소문자와 숫자만 입력하실 수 있습니다.", Toast.LENGTH_SHORT).show();
				}
				
				// 비밀번호1,2 확인
				if (pwd1.equals("") || pwd2.equals("")) {
					Toast.makeText(MemberRegistActivity.this,
							"비밀번호를 입력해 주시기 바랍니다.", Toast.LENGTH_SHORT).show();
					tmp.requestFocus();
					subchk = false;
				} else if (pwd1.equals(pwd2) == false) {
					Toast.makeText(MemberRegistActivity.this,
							"비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
					tmp.requestFocus();
					subchk = false;
				}
				
				group_pwd=pwd1;
				if (subchk == true) {
					RegistMemberActionTask task = new RegistMemberActionTask();
					task.execute();
				}
				break;
			case R.id.mcancle_btn:
				finish();
				break;
			}
		}
	};

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
	}

	// URL을 통해서 사용자 정보를 저장한다
	private class RegistMemberActionTask extends
			AsyncTask<String, Void, String> {
		private ProgressDialog dialog;
		

		@Override
		protected void onPreExecute() {
			dialog = ProgressDialog.show(MemberRegistActivity.this, "회원관리", "등록중입니다.", true);
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... params) {
			//URL 정보를 POST로 넘김
			//값을 받아와서 처리 returnCode > ok:정상적 입력, dup_id:중복입력, error:원인을 알 수 없는 실패
			IdealWorldCupUrlHttp http= new IdealWorldCupUrlHttp(pkchActionUrl);
			HashMap<String, String> hmap= new HashMap<String, String>();
			hmap.put("cmd", "group_add");
			hmap.put("group_uid", group_uid);
			hmap.put("group_pw", group_pwd);
			hmap=http.sendPost(hmap);
			String result=hmap.get("result");
			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			String txt=null;
			if(result.equals("error")){
				txt="정상적으로 저장되지 않았습니다.";
				Toast.makeText(MemberRegistActivity.this, txt, Toast.LENGTH_SHORT).show();
			}
			else if(result.equals("dup_id")){
				txt="중복된 아이디 입니다.";
				Toast.makeText(MemberRegistActivity.this, txt, Toast.LENGTH_SHORT).show();
			}
			else if(result.equals("ok")){
				txt="회원가입이 정상적으로 완료되었습니다. 로그인을 해주시기 바랍니다.";
				Toast.makeText(MemberRegistActivity.this, txt, Toast.LENGTH_SHORT).show();
				MemberRegistActivity.this.finish();
			}else{
				Toast.makeText(MemberRegistActivity.this,result, Toast.LENGTH_SHORT).show();
			}
			dialog.dismiss();
		}
	}
}
