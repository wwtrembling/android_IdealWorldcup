package pkch.company.member;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import pkch.company.R;
import pkch.company.domain.IdealWorldcupLoadingDataElement;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/*회원 로그아웃 Activity*/
public class MemberLogoutActivity extends Activity {
	private final String pkchDataXmlUrl = "http://wwtrembling.cafe24.com/idealWorldcup/data.php";
	private final String pkchActionUrl = "http://wwtrembling.cafe24.com/idealWorldcup/action.php";
	private final String pkchDefaultUid = "wwtrembling";
	private ArrayList<IdealWorldcupLoadingDataElement> lists;
	private int max;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.member_logout_list);
		init();
	}

	protected void init() {
		Intent intent = getIntent();
		MemberAction ma= new MemberAction(this);
		lists=ma.getAllmembers(false);//default 아이디만 빼고 가지고 옴
		max = lists.size();
		ListView lv = (ListView) findViewById(R.id.member_login_list);
		MemberAdapter adapter = new MemberAdapter(this,R.layout.member_logout_row, lists);
		lv.setAdapter(adapter);
	}

	private class MemberAdapter extends BaseAdapter {
		private Context context;
		private ArrayList<IdealWorldcupLoadingDataElement> tmp_lists;
		private LayoutInflater inflater;
		private int layoutid;

		public MemberAdapter(Context context, int layoutid, ArrayList<IdealWorldcupLoadingDataElement> lists) {
			this.context = context;
			this.layoutid = layoutid;
			this.tmp_lists = lists;
			this.inflater = (LayoutInflater) this.context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		}

		public int getCount() {
			// TODO Auto-generated method stub
			return this.tmp_lists.size();
		}

		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return this.tmp_lists.get(position);
		}

		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return this.tmp_lists.get(position).get_group_no();
		}

		public View getView(int position, View v, ViewGroup parent) {
			if(v==null){
				v=inflater.inflate(layoutid, parent, false);
			}
			IdealWorldcupLoadingDataElement ide= tmp_lists.get(position);
			TextView tv= null;
			tv= (TextView)v.findViewById(R.id.member_logour_row_name);
			tv.setText(ide.get_group_uid());
			tv.setOnClickListener(clickListener);
			tv.setTag(ide.get_group_no()+"");
			return v;
		}
		
		View.OnClickListener clickListener = new View.OnClickListener() {
			public void onClick(View v) {
				final int group_no= Integer.parseInt(v.getTag().toString());
				AlertDialog.Builder ab = new AlertDialog.Builder(MemberLogoutActivity.this);
				ab.setTitle("Notice");
				ab.setMessage("로그아웃 하시겠습니까?(해당 정보가 모두 삭제 됩니다.)");
				ab.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						MemberAction ma= new MemberAction(MemberLogoutActivity.this);
						boolean status= ma.doLogout(group_no);
						if(status==true){
							Toast.makeText(MemberLogoutActivity.this, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
							finish();
						}
					}
				});
				ab.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						
					}
				});
				ab.show();
			}			
		};

	}
}
