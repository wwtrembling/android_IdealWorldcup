package pkch.company.pic;

import java.util.ArrayList;

import pkch.company.R;
import pkch.company.domain.IdealWorldcupElement;
import pkch.company.lib.cacheManager2.CustomApplication;
import pkch.company.lib.sql.IdealWorldcupSQLHandler;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

//로그인된 이미지 정보 전체를 보여줌
public class IdealWorldcupPicActivity extends Activity {
	private static CustomApplication imgCache = null;
	private ArrayList<IdealWorldcupElement> item_arr = null;
	private Intent intent=null;
	public static String pic_status_flag="";	// 현재 상태를 확인함

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pic_list);
		init_pic();
	}

	private void init_pic() {
		imgCache = new CustomApplication();
		ShowAllPicsTask task = new ShowAllPicsTask();
		task.execute(null);
	}

	// 기본 데이터 받아오기
	private class ShowAllPicsTask extends AsyncTask<String, Void, String> {
		private ProgressDialog dialog = null;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = ProgressDialog.show(IdealWorldcupPicActivity.this,
					"Notice", "이미지 정보를 받아오고 있습니다.", true);
		}

		@Override
		protected String doInBackground(String... params) {
			item_arr = new ArrayList<IdealWorldcupElement>();
			IdealWorldcupSQLHandler sqlHandler = IdealWorldcupSQLHandler
					.getInstance(IdealWorldcupPicActivity.this);
			SQLiteDatabase db = sqlHandler.getReadableDatabase();
			String query = "select client_item.*, client_group.* "
					+ "FROM client_group, group_item, client_item "
					+ "where client_group.[group_no]=group_item.[client_group_group_no] and " +
					"group_item.[client_item_item_no]=client_item.[item_no] and client_group.[group_uid]!='wwtrembling' "+
					" order by client_item.item_no desc ";
			Cursor cursor = db.rawQuery(query, null);
			if (cursor != null) {
				cursor.moveToFirst();
				IdealWorldcupElement item = null;
				while (!cursor.isAfterLast()) {
					item = new IdealWorldcupElement();
					for (int i = 0; i < cursor.getColumnCount(); i++) {
						if (cursor.getColumnName(i).equals("item_no")) {
							item.setItemNo(cursor.getInt(i));
						} else if (cursor.getColumnName(i).equals("item_name")) {
							item.setItemName(cursor.getString(i));
						} else if (cursor.getColumnName(i).equals("item_path")) {
							item.setItemDir(cursor.getString(i));
						} else if (cursor.getColumnName(i).equals("group_no")) {
							item.setItemGroupno(cursor.getInt(i));
						} else if (cursor.getColumnName(i).equals("group_uid")) {
							item.setItemGroupuid(cursor.getString(i));
						}
					}
					item_arr.add(item);
					cursor.moveToNext();
				}
				cursor.close();
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			dialog.dismiss();
			GridView gridview = (GridView) findViewById(R.id.pic_gridview);
			gridview.setAdapter(new PicAdapter(IdealWorldcupPicActivity.this));
			gridview.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View v,
						int position, long id) {
					// Toast.makeText(IdealWorldcupPicActivity.this,
					// item_arr.get(position).getItemNo()+"",Toast.LENGTH_SHORT).show();
					Intent intent = new Intent(IdealWorldcupPicActivity.this,
							IdealWorldcupPicViewActivity.class);
					intent.putExtra("item", item_arr.get(position));
					startActivity(intent);
				}
			});
		}
	}

	// 이미지 Adapter Class
	private class PicAdapter extends BaseAdapter {
		private Context mcontext;

		public PicAdapter(Context context) {
			mcontext = context;
		}

		public int getCount() {
			return item_arr.size();
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

			String imgDir = item_arr.get(position).getItemDir();

			// 캐쉬 적용 시작!!
			Bitmap bm = null;
			bm = imgCache.getImage(imgDir);
			if (bm == null) {
				PicImageTask task = new PicImageTask(imgDir, iv);
				task.execute(null);
			} else {
				iv.setImageBitmap(bm);
			}

			return iv;
		}
	}

	// 이미지 task
	private class PicImageTask extends AsyncTask<String, Void, Bitmap> {
		private String imgDir;
		private ImageView iv;

		public PicImageTask(String imgDir, ImageView iv) {
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

	// --------------------------------------------------------------------옵션메뉴
	// 시작
	private final int PIC_REGIST = 0; // 이미지 등록
	private final int PIC_HELP = 1; // 이미지 도움말

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, PIC_REGIST, Menu.NONE, "Image Regist");
		menu.add(0, PIC_HELP, Menu.NONE, "Image Help");
		return true;
	}

	// 메뉴 버튼을 클릭했을 경우
	@Override
	public boolean onOptionsItemSelected(MenuItem menu) {
		super.onOptionsItemSelected(menu);
		switch(menu.getItemId()){
			case PIC_REGIST:{
				//이미지 등록하기
				intent= new Intent(IdealWorldcupPicActivity.this, IdealWorldcupPicModifyActivity.class);
				intent.putExtra("cmd", "regist");
				//Log.e("pkch","intent 출발 ! "+intent.toString());
				IdealWorldcupPicActivity.this.startActivityForResult(intent, 5001);
				break;
			}
			case PIC_HELP:{
				//도움말
				AlertDialog alert = null;
				alert=new AlertDialog.Builder(IdealWorldcupPicActivity.this).setTitle("도움말").setMessage("이미지를 등록/수정/삭제하실 수 있습니다.")
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					}).show();
				break;
			}
		}
		return true;
	}
	
	//결과값
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		//이미지 등록이 정상적으로 이루어 졌을 경우
		if(requestCode==5001 && resultCode==-1){
			init_pic();
			Toast.makeText(this, "이미지가 정상적으로 등록이 되었습니다.", Toast.LENGTH_SHORT).show();
		}
	}

	//다시 불러질 경우
	@Override
	protected void onResume() {
		super.onResume();
		if(pic_status_flag.equals("fromPicModifyActivity")){
			init_pic();
			pic_status_flag="";
		}
		else if(pic_status_flag.equals("fromPicDeleteActivity")){
			init_pic();
			pic_status_flag="";
		}
	}

}
