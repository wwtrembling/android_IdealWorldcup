package pkch.company.question;

import java.util.ArrayList;

import pkch.company.R;
import pkch.company.domain.IdealWolrdcupQuestionElement;
import pkch.company.domain.IdealWorldcupLoadingDataElement;
import pkch.company.member.MemberAction;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

public class IdealWorldcupQuestionActivity_201210 extends Activity {
	private ArrayList<IdealWolrdcupQuestionElement> q_arr = null;
	private ArrayList<IdealWorldcupLoadingDataElement> arr_members=null;
	private ProgressDialog dialog = null;
	private Spinner spin = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.question_list);
		init();
		
		/*
		//삭제할것
		Intent intent= new Intent(this, IdealWorldcupQuestionModifyActivity.class);
		intent.putExtra("qs_no", 8); // 해당 질문만 넘겨줌
		intent.putExtra("qs_title", "질문은 질문일뿐 따라하지 말자!");
		intent.putExtra("qs_round", 8);
		startActivity(intent);
		 */
	}
	
	private void init(){
		// 현재 로그인 된 사용자의 이름을 세팅한다
		MemberAction ma = new MemberAction(this);
		arr_members = ma.getAllmembers(true);
		ArrayList<String> a = new ArrayList<String>();
		a.add("전체보기");
		for (int i = 0; i < arr_members.size(); i++) {
			a.add(arr_members.get(i).get_group_uid());
		}
		ArrayAdapter<String> aa = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, a);
		aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spin = (Spinner) findViewById(R.id.question_list_spinner);
		spin.setAdapter(aa);
		spin.setPrompt("선택해 주세요.");
		spin.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View v,	int position, long id) {
				init_load();
			}
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});
	}

	// 질문정보 받아와서 출력함
	private void init_load() {
		dialog = ProgressDialog.show(this, "Loading", "데이타를 불러오고 있습니다.");
		MemberAction ma = new MemberAction(this);
		String a = (String) spin.getItemAtPosition(spin.getSelectedItemPosition());
		q_arr = new ArrayList<IdealWolrdcupQuestionElement>();
		if (a.equals("전체보기")) {
			q_arr = ma.getQuestion(null);
		} else {
			q_arr = ma.getQuestion(a);
		}
		
		ListView lv = (ListView) findViewById(R.id.question_listview);
		QuestionAdapter adapter = new QuestionAdapter(R.layout.question_row);
		lv.setAdapter(adapter);
		dialog.dismiss();
	}

	private class QuestionAdapter extends BaseAdapter {
		private Context context;
		private LayoutInflater inflater;
		private int layoutId;
		private IdealWolrdcupQuestionElement iqe;

		public QuestionAdapter(int layoutId) {
			this.context = IdealWorldcupQuestionActivity_201210.this;
			this.layoutId = layoutId;
			this.inflater = (LayoutInflater) this.context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		}

		public int getCount() {
			return q_arr.size();
		}

		public Object getItem(int position) {
			return q_arr.get(position);
		}

		public long getItemId(int position) {
			return q_arr.get(position).getQsNo();
		}

		public View getView(final int position, View v, ViewGroup parent) {
			if (v == null) {
				v = inflater.inflate(layoutId, parent, false);
			}
			iqe = q_arr.get(position);
			TextView txt1 = (TextView) v.findViewById(R.id.question_row_txt1);
			txt1.setText(iqe.getGroupUid());
			TextView txt2 = (TextView) v.findViewById(R.id.question_row_txt2);
			txt2.setText(iqe.getQsTitle());
			txt2.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					iqe=q_arr.get(position);
					Intent intent = new Intent(context,IdealWorldcupQuestionModifyActivity_201210.class);
					intent.putExtra("qs_no", iqe.getQsNo()); // 해당 질문만 넘겨줌
					intent.putExtra("qs_title", iqe.getQsTitle());
					intent.putExtra("qs_round", iqe.getQsRound());
					startActivityForResult(intent, 6002);
				}
			});
			return v;
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
	}
	// -------------------------------------------------- 옵션 메뉴 시작
	private final int Q_OPTION_REGIST = 20;
	private final int Q_OPTION_HELP = 21;

	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		menu.add(0, Q_OPTION_REGIST, Menu.NONE, "질문 등록하기");
		menu.add(0, Q_OPTION_HELP, Menu.NONE, "도움말");
		return true;
	};

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case Q_OPTION_REGIST: // 질문 등록
			Intent intent= new Intent(this, IdealWorldcupQuestionRegistActivity_201210.class);
			startActivityForResult(intent, 6001);
			break;
		case Q_OPTION_HELP: // 도움말
			AlertDialog alert = null;
			alert=new AlertDialog.Builder(this).setTitle("도움말").setMessage("질문을 등록/수정이 가능하며 해당 질문에 이미지 등록이 가능합니다.")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).show();
			break;
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode==6001 && resultCode==RESULT_OK){	//질문 저장 성공시
			init_load();
		}
		if(requestCode==6002 && resultCode==RESULT_OK){	//질문 수정 성공시
			init_load();
		}
	}
}




