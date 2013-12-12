package pkch.company.member;

import java.util.HashMap;
import pkch.company.R;
import pkch.company.lib.IdealWorldCupUrlHttp;
import pkch.company.lib.IdealWorldcupDataSync;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

//로그인 액티비티 클래스
public class MemberLoginActivity extends Activity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.member_login);
		bindEvent();
	}
	
	private void bindEvent(){
		findViewById(R.id.mlogin_btn).setOnClickListener(clickListener);
		findViewById(R.id.mcancle_btn).setOnClickListener(clickListener);
	}
	
	View.OnClickListener clickListener= new View.OnClickListener() {
		public void onClick(View v) {
			switch(v.getId()){
			case R.id.mlogin_btn:
				//SharedPregerence 에 저장되어 있는 아이디 인지 확인
				LoginMemberActionTask task= new LoginMemberActionTask();
				task.execute(null);
				break;
			case R.id.mcancle_btn:
				finish();
				break;
			}
		}
	};
		
	//로그인 체크 클래스
	private class LoginMemberActionTask extends AsyncTask<String, Void, HashMap<String, String>>{
		private Intent intent;
		private ProgressDialog dialog;
		private String http_result;
		private HashMap<String, String> hm;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = ProgressDialog.show(MemberLoginActivity.this, "회원관리", "로그인 중입니다.", true);
		}
		
		@Override
		protected HashMap<String, String> doInBackground(String... params) {
			//폼에 정상적으로 입력이 되었는지 확인
			EditText tmp= null;
			tmp= (EditText)findViewById(R.id.groupuid_edt);
			String group_uid=null;
			
			group_uid = tmp.getText().toString().trim();
			if (group_uid.equals("") || group_uid == null) {
				hm.put("status", "nouid");
				return hm;
			}
			tmp = (EditText) findViewById(R.id.grouppwd_edt);
			String pwd = tmp.getText().toString().trim();
			if (pwd.equals("") || pwd==null) {
				hm.put("status", "nopwd");
				return hm;
			}
			
			//로그인이 이미 수행되어 있는지 확인
			MemberAction ma= new MemberAction(MemberLoginActivity.this);
			hm= ma.memberCheck(group_uid, pwd);
			return hm;
		}
		
		protected void onPostExecute(HashMap<String, String> hm) {
			String status=hm.get("status");
			if(status.equals("nouid")==true){
				Toast.makeText(MemberLoginActivity.this, "아이디를 입력해 주시기 바랍니다.", Toast.LENGTH_SHORT).show();
				MemberLoginActivity.this.findViewById(R.id.groupuid_edt).requestFocus();
			}
			else if(status.equals("nopwd")==true){
				Toast.makeText(MemberLoginActivity.this, "비밀번호를 입력해 주시기 바랍니다.", Toast.LENGTH_SHORT).show();
				MemberLoginActivity.this.findViewById(R.id.grouppwd_edt).requestFocus();
			}
			else if(status.equals("duplogin")==true){
				Toast.makeText(MemberLoginActivity.this, "이미 로그인된 아이디 입니다.", Toast.LENGTH_SHORT).show();
				MemberLoginActivity.this.finish();
			}
			else if(status.equals("notmatch")==true){
				Toast.makeText(MemberLoginActivity.this, "아이디/비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
			}
			else if(status.equals("logined")==true){
				//동기화 할 것인지 선택하도록 함
				Toast.makeText(MemberLoginActivity.this, "로그인 되었습니다.", Toast.LENGTH_SHORT).show();
				AlertDialog.Builder ab = new AlertDialog.Builder(MemberLoginActivity.this);
				ab.setTitle("Notice");
				ab.setMessage("해당 아이디 정보를 서버에서 받아오시겠습니까?");
				ab.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						IdealWorldcupDataSync ids= new IdealWorldcupDataSync(MemberLoginActivity.this);
						ids.execute(null);
					}
				});
				ab.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						MemberLoginActivity.this.finish();
					}
				});
				ab.show();
			}
			else {
				Toast.makeText(MemberLoginActivity.this, "알 수 없는 원인으로 실행이 중단되었습니다.", Toast.LENGTH_SHORT).show();
			}	
			dialog.dismiss();
		}
	}
}
