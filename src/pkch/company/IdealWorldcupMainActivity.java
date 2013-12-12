package pkch.company;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import pkch.company.domain.IdealWolrdcupQuestionElement;
import pkch.company.domain.IdealWorldcupElement;
import pkch.company.help.MainHelpActivity;
import pkch.company.lib.IdealWorldcupDataSync;
import pkch.company.member.MemberAction;
import pkch.company.member.MemberLoginActivity;
import pkch.company.member.MemberLogoutActivity;
import pkch.company.member.MemberRegistActivity;
import pkch.company.pic.IdealWorldcupPicActivity;
import pkch.company.question.IdealWorldcupQuestionActivity;
import pkch.company.rank.IdealWorldCupRankActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class IdealWorldcupMainActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		bindEvent();
		init();
	}
	
	//초기화 함수
	private void init(){
		//기본적으로 wwtremlbing/sulung2 로그인 시킴
		new IdealWorldcupMainLoginInit().execute(null);
	}

	//터치 이벤트 묶기
	private void bindEvent() {
		findViewById(R.id.start_btn).setOnClickListener(clickListener);
		findViewById(R.id.rank_btn).setOnClickListener(clickListener);
		findViewById(R.id.pic_btn).setOnClickListener(clickListener);
		findViewById(R.id.qs_btn).setOnClickListener(clickListener);
		findViewById(R.id.finish_btn).setOnClickListener(clickListener);
	}
	
	//기본적 로그인을 Thread 형태로 시키도록 수행함 안그럴경우 network error
	private class IdealWorldcupMainLoginInit extends AsyncTask<Void, Void, Void>{
		@Override
		protected Void doInBackground(Void... params) {
			MemberAction ma= new MemberAction(IdealWorldcupMainActivity.this);
			HashMap<String, String> hm=ma.memberCheck("wwtrembling", "sulung2");
			return null;
		}
		
	}

	//터치 이벤트 클래스 생성
	View.OnClickListener clickListener = new View.OnClickListener() {
		private ArrayList<IdealWolrdcupQuestionElement> arr_q;
		private MemberAction ma;
		public void onClick(View v) {
			switch (v.getId()) {
			// 시작하기
			case R.id.start_btn:
				// ----------Spinning Dialog On
				ma= new MemberAction(IdealWorldcupMainActivity.this);	// 질문 데이타를 받아옴
				this.arr_q=ma.getQuestion(null);
				IdealWorldcupMainActivity.this.showQuestionDialog(this.arr_q, "vote");// 투표
				break;
			//랭킹확인
			case R.id.rank_btn:
				// ----------Spinning Dialog On
				ma= new MemberAction(IdealWorldcupMainActivity.this);	// 질문 데이타를 받아옴
				this.arr_q=ma.getQuestion(null);
				IdealWorldcupMainActivity.this.showQuestionDialog(this.arr_q, "rank");// 순위
				break;
			//사진 관리
			case R.id.pic_btn:
				Intent intent=new Intent(IdealWorldcupMainActivity.this, IdealWorldcupPicActivity.class);
				startActivity(intent);
				break;
			//질문 관리
			case R.id.qs_btn:
				Intent intent_qs= new Intent(IdealWorldcupMainActivity.this, IdealWorldcupQuestionActivity.class);
				startActivity(intent_qs);
				break;
			// 끝내기
			case R.id.finish_btn:
				IdealWorldcupMainActivity.this.endOfIdealWorldcup();
				break;
			}

		}
	};

	//질문을 보여주고 선택하도록 함
	private void showQuestionDialog(final ArrayList<IdealWolrdcupQuestionElement> arr_q, final String _type){
		final String[] categories= new String[arr_q.size()];
		for(int i=0;i<arr_q.size();i++){
			categories[i]=arr_q.get(i).getQsTitle();
		}
		new AlertDialog.Builder(this).setTitle("질문을 선택해 주세요.").setItems(categories, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if(_type.equals("vote")) {
					IdealWorldcupMainActivity.this.goWorldcupActivity(arr_q.get(which));
				}
				else if(_type.equals("rank")){
					IdealWorldcupMainActivity.this.goWorldcupRankActivity(arr_q.get(which));
				}
			}
		}).setNegativeButton("취소", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				
			}
		}).show();
	}
	
	//My월드컵
	private void goWorldcupActivity(IdealWolrdcupQuestionElement iqe){
		//해당 질문에 해당하는 전체 데이타 받아오기
		MemberAction ma= new MemberAction(this);
		ArrayList<IdealWorldcupElement> arr_i= null;
		arr_i= ma.getDataByQsno(iqe.getQsNo());
		if(iqe.getQsRound()>arr_i.size()){
			Toast.makeText(this, "질문에 해당하는 이미지가 부족합니다. 질문설정에서 이미지를 다시 설정해 주세요.", Toast.LENGTH_SHORT).show();
		}
		else{
			//random으로 섞은 후에 라운드 갯수만큼 잘라서 items 에 담는다.
			ArrayList<IdealWorldcupElement> items=new ArrayList<IdealWorldcupElement>();
			Collections.shuffle(arr_i);
			for(int i=0;i<iqe.getQsRound();i++){
				items.add(arr_i.get(i));
			}
			Intent intent = null;
			intent=new Intent(this, IdealWorldcupViewer1_Activity.class);	
			intent.putExtra("items", items);
			intent.putExtra("iqe", iqe);
			intent.putExtra("last_idx", 0); // 첫번째 index
			startActivity(intent);
		}
	}
	
	//순위 고고씽!
	private void goWorldcupRankActivity(IdealWolrdcupQuestionElement iqe){
		//질문값 넘기기
		Intent intent = null;
		intent=new Intent(this, IdealWorldCupRankActivity.class);
		intent.putExtra("iqe", iqe);
		startActivity(intent);
	}
	
	//--------------------------------------------------------------------옵션메뉴 시작
	private final int MENU_LOGIN = 0;
	private final int MENU_LOGOUT = 1;
	private final int MENU_REGIST = 2;
	private final int MENU_SYNC = 3;
	private final int MENU_HELP = 4;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, MENU_LOGIN, Menu.NONE, "로그인");
		menu.add(0, MENU_LOGOUT, Menu.NONE, "로그아웃");
		menu.add(0, MENU_REGIST, Menu.NONE, "등록하기");
		menu.add(0, MENU_SYNC, Menu.NONE, "데이터 동기화");
		menu.add(0, MENU_HELP, Menu.NONE, "도움말");
		return true;
	}

	//옵션메뉴를 선택했을 경우
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
			case MENU_LOGIN: {	// 로그인
				startActivityForResult(new Intent(this, MemberLoginActivity.class), 1001);
				break;
			}
			case MENU_LOGOUT: {	// 로그아웃
				startActivityForResult(new Intent(this, MemberLogoutActivity.class), 1002);
				break;
			}
			case MENU_REGIST: {	// 사용자 등록
				startActivityForResult(new Intent(this,MemberRegistActivity.class), 1003);
				break;
			}
			case MENU_SYNC: {	// 데이타 동기화
				IdealWorldcupDataSync task= new IdealWorldcupDataSync(this);
				task.execute();
				break;
			}
			case MENU_HELP: {	// 도움말
				startActivityForResult(new Intent(this, MainHelpActivity.class), 1001);
				break;
			}			
		}
		return true;
	}
	
	//옵션메뉴에서 다시 돌아왔을 경우
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		//Login return
		if(requestCode==1001 && resultCode==RESULT_OK){
			Toast.makeText(this, "로그인이 완료되었습니다.", Toast.LENGTH_SHORT).show();
		}
		else if(requestCode==1002 && resultCode==RESULT_OK){
			Toast.makeText(this, "로그아웃이 완료되었습니다.", Toast.LENGTH_SHORT).show();
		}
		else if(requestCode==1003 && resultCode==RESULT_OK){
			Toast.makeText(this, "사용자 등록이 완료되었습니다.", Toast.LENGTH_SHORT).show();
		}
	}

	// 백버튼을 눌렀을 경우
	public void onBackPressed() {
		endOfIdealWorldcup();
	};

	// End of this Activity
	protected void endOfIdealWorldcup() {
		finish(); // 후에 삭제할것
		/*
		AlertDialog.Builder ab = new AlertDialog.Builder(this);
		ab.setTitle("Notice");
		ab.setMessage("종료하시겠습니까?");
		ab.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});
		ab.setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {

			}
		});
		ab.show();
		*/
	}
}