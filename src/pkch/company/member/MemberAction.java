package pkch.company.member;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

import pkch.company.domain.IdealWolrdcupQuestionElement;
import pkch.company.domain.IdealWorldcupElement;
import pkch.company.domain.IdealWorldcupLoadingDataElement;
import pkch.company.lib.IdealWorldCupUrlHttp;
import pkch.company.lib.sql.IdealWorldcupAppTables;
import pkch.company.lib.sql.IdealWorldcupSQLHandler;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class MemberAction{
	private final String pkchDefaultUid = "wwtrembling";
	private IdealWorldcupSQLHandler sqlHandler;
	private SharedPreferences sp;
	private SQLiteDatabase db;
	
	public MemberAction(Context context){
		sqlHandler=IdealWorldcupSQLHandler.getInstance(context);
	}
	
	//로그인 하기 : DB에 해당 값을 저장하고, sp에 group_uid, group_no를 저장한다
	public void memberLogin(String userid){
		//DB에 저장
		ContentValues val=new ContentValues();
		val.put("group_no", "0");
		val.put("group_uid", userid);
		db= sqlHandler.getWritableDatabase();
		
		long id=0;
		try{
			db.beginTransaction();
			id=db.insert(IdealWorldcupAppTables.ClientGroup.tableName, null, val);
			db.setTransactionSuccessful();
		}finally{
			db.endTransaction();
		}		
		db.close();
	}
	
	
	//사용자 로그인 check
	public HashMap<String, String> memberCheck(String group_uid, String pwd){
		HashMap<String, String> hm= new HashMap<String, String>();
		boolean status=false;
		db= sqlHandler.getReadableDatabase();
		String query="SELECT * FROM "+IdealWorldcupAppTables.ClientGroup.tableName+" where group_uid='"+group_uid+"'";
		Cursor cursor= db.rawQuery(query, null);		
		if(cursor!=null){
			if(cursor.getCount()>0){
				status=true;
			}
		}
		else{
			status=false;
		}
		cursor.close();
		if(status==true){//중복된 로그인
			hm.put("status", "duplogin");	// 이미 로그인을 한 상태임
		}
		else{
			hm=this.doLogin(group_uid, pwd);
			String login_flag=hm.get("result");
			if(login_flag.equals("0")){
				hm.put("status","nomatch");
			}
			else{
				//로그인 결과 저장
				db= sqlHandler.getWritableDatabase();
				query="INSERT INTO "+IdealWorldcupAppTables.ClientGroup.tableName
						+" (group_no, group_uid) values("+hm.get("result")+",'"+group_uid+"')";
				db.execSQL(query);
				hm.put("status", "logined");
			}
		}
		db.close();
		return hm;
	}
	
	//사용자 로그인 action
	public HashMap<String, String> doLogin(String group_uid, String pwd){
		HashMap<String, String> hm= new HashMap<String, String>();
		hm.put("cmd", "group_login");
		hm.put("group_uid", group_uid);
		hm.put("group_pw",pwd);
		IdealWorldCupUrlHttp http =new IdealWorldCupUrlHttp();
		hm=http.sendPost(hm);
		return hm;
	}
	
	//사용자 로그아웃 action
	public boolean doLogout(int group_no){
		boolean status=true;
		db= sqlHandler.getReadableDatabase();
		String query=null;
		query="DELETE FROM "+IdealWorldcupAppTables.ClientGroup.tableName+" WHERE group_no='"+group_no+"' ";
		db.execSQL(query);
		return status;
	}
	
	//전체 로그인된 사용자 list
	public ArrayList<IdealWorldcupLoadingDataElement> getAllmembers(boolean all_flag){ // true : 전체, false: default 만 뺴고 
		ArrayList<IdealWorldcupLoadingDataElement> al=new ArrayList<IdealWorldcupLoadingDataElement>();
		IdealWorldcupLoadingDataElement ide=null;
		db= sqlHandler.getReadableDatabase();
		String query=null;
		if(all_flag==true){
			query="SELECT * FROM "+IdealWorldcupAppTables.ClientGroup.tableName;
		}else{
			query="SELECT * FROM "+IdealWorldcupAppTables.ClientGroup.tableName+" where group_uid <>'"+pkchDefaultUid+"' ";
		}
		Cursor cursor= db.rawQuery(query, null);
		if(cursor!=null){
			cursor.moveToFirst();
			while(!cursor.isAfterLast()){
				ide= new IdealWorldcupLoadingDataElement();
				for(int i = 0 ; i < cursor.getColumnCount() ; i++ ){
					//Log.e("pkch",cursor.getColumnName(i)+" , "+cursor.getString(i) );
					if(cursor.getColumnName(i).equals("group_no")){
						ide.set_group_no(cursor.getInt(i));
					}
					else if(cursor.getColumnName(i).equals("group_uid")){
						ide.set_group_uid(cursor.getString(i));
					}					
				}
				al.add(ide);
				cursor.moveToNext();
			}
			cursor.close();
		}
		return al;
	}
	
	//사용자 정보 가지고 오기
	public HashMap<String,String> getUserInfo(String group_uid){
		HashMap<String,String> hm=new HashMap<String, String>();
		String q=null;
		q="select group_no from client_group where group_uid='"+group_uid+"' ";
		db= sqlHandler.getReadableDatabase();
		Cursor cursor= db.rawQuery(q, null);
		if(cursor!=null){
			cursor.moveToFirst();
			while(!cursor.isAfterLast()){
				for(int i = 0 ; i < cursor.getColumnCount() ; i++ ){
					hm.put(cursor.getColumnName(i), cursor.getString(i));
				}
				cursor.moveToNext();
			}			
		}
		return hm;
	}
	
	
	//질문 가지고 오기(사용자 유무에 따라서 다름)
	public ArrayList<IdealWolrdcupQuestionElement> getQuestion(String group_uid){
		ArrayList<IdealWolrdcupQuestionElement> arr_q= new ArrayList<IdealWolrdcupQuestionElement>();
		IdealWolrdcupQuestionElement iqe=null;
		String q=null;
		q="select * from client_group, group_question, client_question " +
				"where" +
				" client_group.[group_no]=group_question.[client_group_group_no] and " +
				" group_question.[client_question_qs_no]=client_question.[qs_no] ";
		if(group_uid !=null){
			q+=" and client_group.[group_uid]='"+group_uid+"' ";
		}
		db= sqlHandler.getReadableDatabase();
		Cursor cursor= db.rawQuery(q, null);
		if(cursor!=null){
			cursor.moveToFirst();
			while(!cursor.isAfterLast()){
				iqe= new IdealWolrdcupQuestionElement();
				for(int i = 0 ; i < cursor.getColumnCount() ; i++ ){
					//Log.e("pkch",cursor.getColumnName(i)+" : "+cursor.getString(i));
					if(cursor.getColumnName(i).equals("group_no")){iqe.setGroupNo(cursor.getInt(i));}
					else if(cursor.getColumnName(i).equals("group_uid")){iqe.setGroupUid(cursor.getString(i));}
					else if(cursor.getColumnName(i).equals("qs_no")){iqe.setQsNo(cursor.getInt(i));}
					else if(cursor.getColumnName(i).equals("qs_title")){iqe.setQsTitle(cursor.getString(i));}
					else if(cursor.getColumnName(i).equals("qs_round")){iqe.setQsRound(cursor.getInt(i));}
				}
				arr_q.add(iqe);
				iqe=null;
				cursor.moveToNext();
			}
			cursor.close();
		}
		return arr_q;
	}
	
	//현재 로그인된 전체 데이타 불러오기
	public ArrayList<IdealWorldcupElement> getDataByQsno(int qs_no){
		ArrayList<IdealWorldcupElement> arr_i= new ArrayList<IdealWorldcupElement>();
		IdealWorldcupElement ide= new IdealWorldcupElement();
		db= sqlHandler.getReadableDatabase();
		String query=null;		
		query="select * " +
				" from client_item as a, question_item as b " +
				" where a.item_no=b.client_item_item_no and b.client_question_qs_no='"+qs_no+"' ";
		//Log.e("pkch",query);
		Cursor cursor= db.rawQuery(query, null);
		if(cursor!=null){
			cursor.moveToFirst();
			while(!cursor.isAfterLast()){
				ide= new IdealWorldcupElement();
				for(int i = 0 ; i < cursor.getColumnCount() ; i++ ){
					//Log.e("pkch",cursor.getColumnName(i)+" : "+cursor.getString(i));
					if(cursor.getColumnName(i).equals("item_no")){
						ide.setItemNo(cursor.getInt(i));
					}
					else if(cursor.getColumnName(i).equals("item_name")){
						ide.setItemName(cursor.getString(i));
					}
					else if(cursor.getColumnName(i).equals("item_path")){
						ide.setItemDir(cursor.getString(i));
					}
					else if(cursor.getColumnName(i).equals("hits")){
						ide.setHits(cursor.getInt(i));
					}
				}
				ide.setClicked(false);
				arr_i.add(ide);
				ide=null;
				cursor.moveToNext();
			}
			cursor.close();
		}
		return arr_i;
	}


}
