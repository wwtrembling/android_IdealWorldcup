package pkch.company.question;

import java.util.ArrayList;
import java.util.HashMap;

import pkch.company.R;
import pkch.company.domain.IdealWorldcupLoadingDataElement;
import pkch.company.member.MemberAction;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.Toast;

public class IdealWorldcupQuestionActivity extends Activity {
	private WebView wv;
	private final Handler handler= new Handler();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.question_web_view);

		// 인터넷이 연결이 되어 있는지 확인
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		boolean isWifiConn = ni.isConnected();
		ni = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		boolean isMobileConn = ni.isConnected();

		// 인터넷이 연결되어 있지 않을 경우
		if (isWifiConn == false && isMobileConn == false) {
			Toast.makeText(this, "인터넷에 연결되어 있지 않습니다.", Toast.LENGTH_SHORT)
					.show();
			finish();
		} else {
			// POST로 현재 회원정보 전송후 데이타값 확인
			init();
		}

	}

	private void init() {
		// 사용자 전체 데이터 받아오기
		MemberAction ma = new MemberAction(this);
		ArrayList<IdealWorldcupLoadingDataElement> logined_users = ma.getAllmembers(false);

		// 사용자 데이터 저장
		HashMap<String, String> hm = new HashMap<String, String>();
		hm.put("cmd", "question_user_auth");
		String group_no_str = "";
		for (int i = 0; i < logined_users.size(); i++) {
			group_no_str += logined_users.get(i).get_group_no() + ",";
		}

		wv = (WebView) findViewById(R.id.question_wv);
		wv.setWebChromeClient(new WebChromeClient(){
			//자바스크립트 alert
			public boolean onJsAlert(WebView view, String url, String msg, final android.webkit.JsResult result){
				new AlertDialog.Builder(IdealWorldcupQuestionActivity.this).setTitle("Notice").setMessage(msg).setPositiveButton("확인",
						new AlertDialog.OnClickListener(){	public void onClick(DialogInterface dialog, int which){result.confirm();}
					
				}).setCancelable(false).create().show();
				return true;
			}
			
			//자바스크립트 prompt
			public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, final JsPromptResult result){
				final AlertDialog.Builder builder= new AlertDialog.Builder(IdealWorldcupQuestionActivity.this).setTitle(message);
				final EditText et= new EditText(IdealWorldcupQuestionActivity.this);
				et.setSingleLine();
				et.setText(defaultValue);
				builder.setView(et);
				builder.setPositiveButton("확인", new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						result.confirm(et.getText().toString());
					}
				}).setNeutralButton("취소", new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						result.cancel();
					}
				}).setCancelable(false).create().show();
				return true;
			}
			
			//자바스크립트 confirm
			public boolean onJsConfirm(WebView view, String url, String message, final JsResult result){
				final AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
				builder.setTitle("Notice").setMessage(message)
				.setPositiveButton("확인", new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					result.confirm();
				}
				}).setNeutralButton("취소", new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						result.cancel();
					}
					});
					builder.setOnCancelListener(new OnCancelListener() {
					public void onCancel(DialogInterface dialog) {
						result.cancel();
					}
				}).setCancelable(false).create().show();
				return true;
			}
		});
		wv.getSettings().setJavaScriptEnabled(true); // javascript 사용 가능하게 한다
		wv.addJavascriptInterface(new WebViewBridge(), "idealAndroid"); //자사스크립트에서 안드로이드 함수를 호출할 수 있도록 함
		wv.loadUrl("http://wwtrembling.cafe24.com/idealWorldcup/question_list_wv.php?group_no_str="+ group_no_str);
		wv.setWebViewClient(new WebViewClient());
	}

	// 뒤로 가기
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK) && wv.canGoBack()) {
			wv.goBack();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private class WebViewClientClass extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
		}
	}
	
	//자바스크립트에서 ANDROID 호출하도록함
	private class WebViewBridge{
		public void goMain(){
			handler.post(new Runnable() {
				public void run() {
					finish();
				}
			});
		}
	}
}
