package pkch.company.pic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

import pkch.company.R;
import pkch.company.domain.IdealWorldcupElement;
import pkch.company.lib.HttpRequestor;
import pkch.company.lib.cacheManager2.CustomApplication;
import pkch.company.lib.sql.IdealWorldcupAppTables;
import pkch.company.lib.sql.IdealWorldcupSQLHandler;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class IdealWorldcupPicViewActivity extends Activity {
	private final String pkchActionUrl = "http://wwtrembling.cafe24.com/idealWorldcup/action.php";
	private ImageView iv;
	private IdealWorldcupElement item;
	private Intent intent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pic_view);
		init();
	}

	private void init() {
		Intent intent = this.getIntent();
		Serializable tmp = intent.getSerializableExtra("item");
		item = (IdealWorldcupElement) tmp;

		//이미지 적용
		iv= (ImageView) this.findViewById(R.id.pic_view_img);
		Bitmap bm =BitmapFactory.decodeFile(item.getItemDir());
		iv.setImageBitmap(bm);
		CustomApplication imgCache=new CustomApplication();
		imgCache.addImage(item.getItemDir(), bm);
		//이미지 페이드인 효과
		AlphaAnimation fade_in = new AlphaAnimation(0, 1);
		fade_in.setDuration(300);
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
		
		
		TextView tv = (TextView) this.findViewById(R.id.pic_view_l2_rl1_txt1);
		tv.setText(item.getItemName());
		this.findViewById(R.id.pic_view_img).setOnClickListener(clickListener);
		this.findViewById(R.id.pic_view_l2_rl1_btn_modify).setOnClickListener(clickListener);
		this.findViewById(R.id.pic_view_l2_rl1_btn_close).setOnClickListener(clickListener);
		this.findViewById(R.id.pic_view_l2_rl1_btn_remove).setOnClickListener(clickListener);
	}

	private View.OnClickListener clickListener = new View.OnClickListener() {
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.pic_view_img:
				IdealWorldcupPicViewActivity.this.finish();
				break;
			case R.id.pic_view_l2_rl1_btn_modify:
				//이미지 수정
				Intent intent= new Intent(IdealWorldcupPicViewActivity.this, IdealWorldcupPicModifyActivity.class);
				intent.putExtra("cmd", "modify");
				intent.putExtra("item_no", item.getItemNo());	// 이미지 번호
				intent.putExtra("item_name",item.getItemName());	// 이미지 이름
				intent.putExtra("item_path",item.getItemDir());	// 이미지 경로
				intent.putExtra("group_no",item.getItemGroupno());	// 이미지 소유자 고유번호
				intent.putExtra("group_uid",item.getItemGroupuid());	// 이미지 소유자
				startActivity(intent); // 수정
				finish();
				break;
			case R.id.pic_view_l2_rl1_btn_remove:
				//이미지 삭제
				//삭제여부 확인
				AlertDialog.Builder ab= new AlertDialog.Builder(IdealWorldcupPicViewActivity.this);
				ab.setMessage("이미지가 영구히 삭제 됩니다. 진행하시겠습니까?");
				ab.setTitle("Notice!");
				ab.setPositiveButton("YES", new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						PicViewRemoveTask task_remove= new PicViewRemoveTask();
						task_remove.execute((String[])null);
					}
				});
				ab.setNegativeButton("NO", null);
				ab.show();
				break;
			case R.id.pic_view_l2_rl1_btn_close :
				IdealWorldcupPicViewActivity.this.finish();
				break;
					
			}

		}
	};
	
	
	//삭제 버튼 클릭시
	class PicViewRemoveTask extends AsyncTask<String, Void, String>{
		private ProgressDialog dialog=null;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog= ProgressDialog.show(IdealWorldcupPicViewActivity.this, "Loading..", "삭제가 진행중입니다.",true);
		}
		@Override
		protected String doInBackground(String... params) {
			URL url;
			String str=null;
			//web 데이터 동기화
			try {
				url = new URL(pkchActionUrl);
				HttpRequestor request= new HttpRequestor(url,4);
				request.addParameter("cmd", "item_remove");
				request.addParameter("item_no", ""+item.getItemNo());	//이미지 소유자
				InputStream is= request.sendPost();
				BufferedReader br= new BufferedReader(new InputStreamReader(is));
				String line=null;
				StringBuffer sb= new StringBuffer();
				while((line=br.readLine())!=null){
					sb.append(line);
				}
				str=sb.toString();
				br.close();
				//성공시에 내부 데이터 갱신
				if(str.equals("ok")){
					//받은 데이타 DB에 저장
					IdealWorldcupSQLHandler sqlHandler=IdealWorldcupSQLHandler.getInstance(IdealWorldcupPicViewActivity.this);
					SQLiteDatabase db= sqlHandler.getWritableDatabase();
					String query=null;
					query="DELETE FROM "+IdealWorldcupAppTables.ClientItem.tableName+" WHERE item_no='"+item.getItemNo()+"' ";
					db.execSQL(query);
					query="DELETE FROM "+IdealWorldcupAppTables.GroupItem.tableName+" WHERE client_item_item_no='"+item.getItemNo()+"' ";
					db.execSQL(query);
					query="DELETE FROM "+IdealWorldcupAppTables.QuestionItem.tableName+" WHERE client_item_item_no='"+item.getItemNo()+"' ";
					db.execSQL(query);
					return "ok";
				}
				else{
					return "error";
				}
				
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}			
			return null;
		}
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			dialog.dismiss();
			String msg=null;
			if(result.equals("ok")){
				IdealWorldcupPicActivity.pic_status_flag="fromPicDeleteActivity";
				msg="삭제가 완료되었습니다.";
			}
			else if(result.equals("error")){
				msg="삭제가 실패되었습니다.";
			}
			Toast.makeText(IdealWorldcupPicViewActivity.this, msg, Toast.LENGTH_SHORT).show();
			finish();
		}
	}
}
