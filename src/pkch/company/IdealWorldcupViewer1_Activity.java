package pkch.company;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import pkch.company.domain.IdealWolrdcupQuestionElement;
import pkch.company.domain.IdealWorldcupElement;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class IdealWorldcupViewer1_Activity extends Activity {
	private int intent_max;		// 총 라운드
	private int intent_last_idx;		// 현재 index
	private ArrayList<IdealWorldcupElement> items;
	private IdealWolrdcupQuestionElement iqe;
	private IdealWorldcupElement item1;
	private IdealWorldcupElement item2;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view1);
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

		// intent_last_idx 와 intent_last_idx 가 일치할 경우
		// IdealWorldcupViewer2_Activity 로 이동함
		if (intent_last_idx == intent_max) {
			intent = new Intent(IdealWorldcupViewer1_Activity.this,	IdealWorldcupViewer2_Activity.class);
			//1.intent_max를 반으로 줄인다. 
			//2.intent_last_idx를 0으로 세팅한다. 
			//3.lists 배열에서 clicked 값이 true인 object 만 받아서 배열을 재 생성한다.
			//4.IdealWorldcupViewer2_Activity로 보낸다.
			ArrayList<IdealWorldcupElement> newLists = new ArrayList<IdealWorldcupElement>();
			for (int i = 0; i < items.size(); i++) {
				if (items.get(i).getClicked() == true) {
					items.get(i).setClicked(false);
					newLists.add(items.get(i));
				}
			}
			items = newLists;
			intent_last_idx = 0;
			finish();
			intent.putExtra("items", items);
			intent.putExtra("iqe", iqe);
			intent.putExtra("last_idx", intent_last_idx);
			startActivityForResult(intent, 0);
		} else {
			// 이 외의 경우에는 정상적으로 이동
			item1 = items.get((intent_last_idx++));
			item2 = items.get((intent_last_idx++));

			// 레이아웃에 설저오딘 Progress bar 를 invisible로 바꿈
			findViewById(R.id.progressBar1).setVisibility(View.INVISIBLE);
			
			// 레이어, 이미지를 화면 크기에 맞도록 확장
			imgExtension();
			
			// 텍스트 및 이미지를 매칭시킴
			panelMatching();
			
			// 이벤트 바인딩
			bindEvent();
		}
	}

	private void bindEvent() {
		findViewById(R.id.iv_1).setOnClickListener(clickListener);
		findViewById(R.id.iv_2).setOnClickListener(clickListener);
	}

	private View.OnClickListener clickListener = new View.OnClickListener() {
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.iv_1:
				items.get((intent_last_idx - 1)).setClicked(false);
				items.get((intent_last_idx - 2)).setClicked(true);
				break;
			case R.id.iv_2:
				items.get((intent_last_idx - 1)).setClicked(true);
				items.get((intent_last_idx - 2)).setClicked(false);
				break;
			}
			finish();
			Intent intent = new Intent(IdealWorldcupViewer1_Activity.this, IdealWorldcupViewer1_Activity.class);
			intent.putExtra("items", items);
			intent.putExtra("iqe", iqe);
			intent.putExtra("last_idx", intent_last_idx);
			startActivity(intent);
		}
	};

	private void panelMatching() {
		TextView tv = null;
		tv = (TextView) findViewById(R.id.tx_1);
		tv.setText(item1.getItemName());
		tv.setGravity(Gravity.CENTER);
		tv.setVisibility(View.VISIBLE);
		tv = (TextView) findViewById(R.id.tx_2);
		tv.setText(item2.getItemName());
		tv.setGravity(Gravity.CENTER);
		tv.setVisibility(View.VISIBLE);

		ImageView iv = null;
		iv = (ImageView) findViewById(R.id.iv_1);
		// ImageRepository.INSTANCE.setImageBitmap(item1.getImgurl(), iv);
		Bitmap bm = BitmapFactory.decodeFile(item1.getItemDir());
		iv.setImageBitmap(bm);
		iv.setVisibility(View.VISIBLE);

		iv = (ImageView) findViewById(R.id.iv_2);
		// ImageRepository.INSTANCE.setImageBitmap(item2.getImgurl(), iv);
		bm = BitmapFactory.decodeFile(item2.getItemDir());
		iv.setImageBitmap(bm);
		iv.setVisibility(View.VISIBLE);
	}

	Bitmap downloadBitmap(String url) {
		final HttpClient client = new DefaultHttpClient();
		final HttpGet getRequest = new HttpGet(url);

		HttpResponse response;
		try {
			response = client.execute(getRequest);
			final int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == HttpStatus.SC_OK) {
				final HttpEntity entity = response.getEntity();
				if (entity != null) {
					InputStream is = null;
					is = entity.getContent();
					return BitmapFactory.decodeStream(is);
				}
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void imgExtension() {
		int newWidth = getWindowManager().getDefaultDisplay().getWidth() / 2;
		int newHeight = getWindowManager().getDefaultDisplay().getHeight();
		RelativeLayout.LayoutParams params = null;
		//--------------------------- 아이템 및 아이템 이름를 세팅
		// layout 크리를 전체크기의 반으로 각각을 세팅한다
		RelativeLayout layout_1 = (RelativeLayout) findViewById(R.id.layout_1);
		RelativeLayout layout_2 = (RelativeLayout) findViewById(R.id.layout_2);
		params = new RelativeLayout.LayoutParams(newWidth, newHeight);
		layout_1.getLayoutParams().height = newHeight;
		layout_1.getLayoutParams().width = newWidth;
		layout_2.getLayoutParams().height = newHeight;
		layout_2.getLayoutParams().width = newWidth;
		
		// Image 전체 크기도 마찬가지로 각각 반으로 세팅한다
		ImageView iv_1 = (ImageView) findViewById(R.id.iv_1);
		ImageView iv_2 = (ImageView) findViewById(R.id.iv_2);
		params = new RelativeLayout.LayoutParams(newWidth, newHeight);
		iv_1.setLayoutParams(params);
		iv_2.setLayoutParams(params);
		
		//--------------------------- 질문을 하단에 보여줌
		TextView tv= (TextView)findViewById(R.id.qs_text);
		tv.setText(iqe.getQsTitle());
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
	}

	// 자식창에서 intent를 보냈을 경우
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Toast.makeText(this, "from activity2", Toast.LENGTH_SHORT).show();
	}
}
