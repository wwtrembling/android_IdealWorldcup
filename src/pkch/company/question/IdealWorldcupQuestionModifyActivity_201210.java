package pkch.company.question;

import java.util.ArrayList;

import pkch.company.R;
import pkch.company.domain.IdealWorldcupElement;
import pkch.company.lib.CheckNetwork;
import pkch.company.lib.cacheManager2.CustomApplication;
import pkch.company.member.MemberAction;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

public class IdealWorldcupQuestionModifyActivity_201210 extends Activity implements OnScrollListener, OnItemClickListener{
	private static CustomApplication imgCache = null;
	private int qs_no=0;
	private int qs_round=0;
	private String qs_title=null;
	private ArrayList<IdealWorldcupElement> arr_iew=null;
	private ProgressDialog dialog=null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.question_modify);
		imgCache=new CustomApplication(); // 이미지 캐쉬 클래스 초기화
		
		init();	// 초기화 수행
		bind_event();	// 이벤트 묶기
	}
	
	//초기화
	private void init(){		
		//받아온 데이터 불러오기
		Intent intent=getIntent();
		this.qs_no=intent.getIntExtra("qs_no", 0);
		this.qs_round=intent.getIntExtra("qs_round",0);
		this.qs_title=intent.getStringExtra("qs_title");

		//Log.e("pkch",qs_no+"");
		//Log.e("pkch",qs_round+"");
		//Log.e("pkch",qs_title+"");
		
		//질문세팅
		EditText et= (EditText)findViewById(R.id.question_modify_edt1);
		et.setText(qs_title);
		
		//질문 Round 세팅
		int[] round_arr={4,8,16,32,64};
		int selected_idx=0;
		ArrayList<Integer> a2= new ArrayList<Integer>();
		for(int i=0;i<round_arr.length;i++){
			a2.add(round_arr[i]);
			if(qs_round==round_arr[i]){
				selected_idx=i;
			}
		}
		Spinner spin= (Spinner)findViewById(R.id.question_modify_spinner1);
		ArrayAdapter<Integer> aa2= new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_item, a2);
		aa2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spin.setAdapter(aa2);
		spin.setSelection(selected_idx);
		spin.setPrompt("선택해 주세요.");
		
		//질문에 해당하는 이미지 세팅
		ShowQuestionImageTask task = new ShowQuestionImageTask();
		task.execute();
	}
	
	//하단 이미지 Task 로 보여주기
	class ShowQuestionImageTask extends AsyncTask<String, Void, String>{
		protected void onPreExecute() {
			super.onPreExecute();
			dialog= ProgressDialog.show(IdealWorldcupQuestionModifyActivity_201210.this, "Notice", "이미지 정보를 받아오고 있습니다.", true);
		}
		
		@Override
		protected String doInBackground(String... params) {
			MemberAction ma = new MemberAction(IdealWorldcupQuestionModifyActivity_201210.this);
			arr_iew=ma.getDataByQsno(qs_no);
			GridView gv= (GridView)findViewById(R.id.question_modify_gridview);
			gv.setAdapter(new QuestionPicAdapter(IdealWorldcupQuestionModifyActivity_201210.this));
			return null;
		}
		
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			dialog.dismiss();
		}
	}
	
	// 이미지 Adapter Class
	class QuestionPicAdapter extends BaseAdapter {
		private Context mcontext;

		public QuestionPicAdapter(Context context) {
			mcontext = context;
		}

		public int getCount() {
			return arr_iew.size();
		}

		public Object getItem(int position) {
			return null;
		}

		public long getItemId(int position) {
			return 0;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView iv;
			if (convertView == null) { // 재생성이 안되었다면 초기화 시켜줌
				iv = new ImageView(mcontext);
				iv.setLayoutParams(new GridView.LayoutParams(100, 100));
				iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
				iv.setPadding(4, 4, 4, 4);
			} else {
				iv = (ImageView) convertView;
			}
			String imgDir = arr_iew.get(position).getItemDir();
			// 캐쉬 적용 시작!!
			Bitmap bm = null;
			bm = imgCache.getImage(imgDir);
			if (bm == null) {
				QuestionPicImageTask task = new QuestionPicImageTask(imgDir, iv);
				task.execute();
			} else {
				iv.setImageBitmap(bm);
			}

			return iv;
		}
	}
	
	// 그리드 뷰에 이미지 세팅하는 TASK
	private class QuestionPicImageTask extends AsyncTask<String, Void, Bitmap> {
		private String imgDir;
		private ImageView iv;

		public QuestionPicImageTask(String imgDir, ImageView iv) {
			this.imgDir = imgDir;
			this.iv = iv;
		}

		@Override
		protected Bitmap doInBackground(String... params) {
			// 캐쉬 확인
			Bitmap bm = null;
			bm = BitmapFactory.decodeFile(imgDir);
			return bm;
		}

		@Override
		protected void onPostExecute(Bitmap bm) {
			super.onPostExecute(bm);
			iv.setImageBitmap(bm);
			imgCache.addImage(imgDir, bm);
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
	}
	
	
	//이벤트 붙이기
	private void bind_event(){
		findViewById(R.id.question_modify_btn1).setOnClickListener(clickListener);	// 수정버튼
		findViewById(R.id.question_modify_btn2).setOnClickListener(clickListener);	// 삭제버튼
	}
	
	View.OnClickListener clickListener=new View.OnClickListener() {
		public void onClick(View v) {
			switch(v.getId()){
				case R.id.question_modify_btn1:{		// 수정버튼
					CheckNetwork chk= new CheckNetwork();
					boolean n_chk=chk.checkNetwork(IdealWorldcupQuestionModifyActivity_201210.this);
					if(n_chk==false){Toast.makeText(IdealWorldcupQuestionModifyActivity_201210.this, "네트워크 연결을 확인해 주세요.", Toast.LENGTH_SHORT);
					}else{
						
					}
					break;
				}
				case R.id.question_modify_btn2:{		// 삭제버튼
					CheckNetwork chk= new CheckNetwork();
					boolean n_chk=chk.checkNetwork(IdealWorldcupQuestionModifyActivity_201210.this);
					if(n_chk==false){Toast.makeText(IdealWorldcupQuestionModifyActivity_201210.this, "네트워크 연결을 확인해 주세요.", Toast.LENGTH_SHORT);
					}else{
						
					}
					break;
				}
			}
		}
	};
	
	//스크롤 이벤트 작업
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		
	}

	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub
		
	}

	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub
		
	}

}
