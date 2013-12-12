package pkch.company;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import pkch.company.domain.IdealWolrdcupQuestionElement;
import pkch.company.domain.IdealWorldcupElement;
import pkch.company.domain.IdealWorldcupLoadingDataElement;
import pkch.company.lib.IdealWorldCupUrlHttp;
import pkch.company.lib.sql.IdealWorldcupAppTables;
import pkch.company.lib.sql.IdealWorldcupSQLHandler;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class IdealWorldcupViewer2_Activity extends Activity {;
	private final String pkchActionUrl = "http://wwtrembling.cafe24.com/idealWorldcup/action.php";
	private int intent_max;		// 총 라운드
	private int intent_last_idx;		// 현재 index
	private ArrayList<IdealWorldcupElement> items;
	private IdealWolrdcupQuestionElement iqe;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view2);
		init();
	}

	private void init() {
		//인텐트 전달값 받아오기
		Intent intent = getIntent();
		Serializable tmp =null; 
		tmp=intent.getSerializableExtra("items");
		items = (ArrayList<IdealWorldcupElement>) tmp;
		tmp=intent.getSerializableExtra("iqe");
		iqe = (IdealWolrdcupQuestionElement) tmp;
		intent_max = items.size();
		intent_last_idx = intent.getIntExtra("last_idx", 0);
		
		// 최종 결과값을 출력할 경우
		TextView tv = (TextView) findViewById(R.id.next_btn);
		if (this.intent_max == 1) {
			//hit 수정 task 시작
			ItemEditTask task= new ItemEditTask();
			task.execute(null);
			
			ImageView iv= (ImageView)findViewById(R.id.final_img);
			Bitmap bm= BitmapFactory.decodeFile(items.get(0).getItemDir());
			iv.setImageBitmap(bm);
			tv.setText("처음으로 이동하기");
			
		} else {
			if(intent_max==2){tv.setText("결승전!!");}
			else{tv.setText(intent_max + "강!");}
		}
		bindEvent();
	}

	private void bindEvent() {
		findViewById(R.id.next_btn).setOnClickListener(clickListener);
	}

	public View.OnClickListener clickListener = new View.OnClickListener() {
		public void onClick(View v) {
			Intent intent = null;
			switch (v.getId()) {
			case R.id.next_btn:
				// 최종 결과값을 출력할 경우
				if (intent_max == 1) {
					finish();
				}
				else{
					finish();
					// 기타 결과값을 출력할 경우, 부모창에 exit intent를 넘김
					intent = new Intent(IdealWorldcupViewer2_Activity.this,IdealWorldcupViewer1_Activity.class);
					intent.putExtra("items", items);
					intent.putExtra("iqe", iqe);
					intent.putExtra("last_idx", intent_last_idx);
					startActivity(intent);
				}
				break;
			}
		}
	};
	
	private class ItemEditTask extends AsyncTask<String, Void, String>{
		@Override
		protected String doInBackground(String... params) {
			//DB 저장
			IdealWorldcupSQLHandler sqlHandler=IdealWorldcupSQLHandler.getInstance(IdealWorldcupViewer2_Activity.this);
			SQLiteDatabase db= sqlHandler.getWritableDatabase();
			String query="UPDATE "+IdealWorldcupAppTables.QuestionItem.tableName
					+" SET hits=hits+1 WHERE client_question_qs_no='"+items.get(0).getItemNo()+"' and client_item_item_no='"+iqe.getQsNo()+"' ";
			db.execSQL(query);			
			
			//결과값을 서버 및 데이터에 저장함
			HashMap<String, String> hm= new HashMap<String, String>();
			hm.put("cmd", "item_hits_edit");
			hm.put("item_no", ""+items.get(0).getItemNo());
			hm.put("qs_no", ""+iqe.getQsNo());
			IdealWorldCupUrlHttp http =new IdealWorldCupUrlHttp(pkchActionUrl);
			hm=http.sendPost(hm);
			
			return null;
		}
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			Toast.makeText(IdealWorldcupViewer2_Activity.this, "Saved!", Toast.LENGTH_SHORT).show();
		}
		
	}
}
