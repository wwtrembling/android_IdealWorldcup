package pkch.company.help;

import pkch.company.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainHelpActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_help);
		bindEvent();
	}
	
	private void bindEvent(){
		Button mh_btn1= (Button)findViewById(R.id.mh_btn1);
		mh_btn1.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
	}
}
