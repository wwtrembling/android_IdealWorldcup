package pkch.company.domain;

import java.io.Serializable;

public class IdealWorldcupLoadingDataElement implements Serializable {
	private static final long serialVersionUID = -4943568370424077037L;
	private int client_group_group_no;
	private int client_item_item_no;
	private int client_question_qs_no;
	private int group_no;
	private String group_uid;
	private String group_pwd;
	private int item_no;
	private String item_name;
	private String item_path;
	private int item_size;
	private int hits;
	private int qs_no;
	private String qs_title;
	private int qs_round;
	
	
	
	public int get_client_group_group_no(){	return client_group_group_no;}
	public void set_client_group_group_no(int a){	client_group_group_no=a;}
	
	public int get_client_item_item_no(){	return client_item_item_no;}
	public void set_client_item_item_no(int a){	client_item_item_no=a;}
	
	public int get_client_question_qs_no(){	return client_question_qs_no;}
	public void set_client_question_qs_no(int a){	client_question_qs_no=a;}
	
	public int get_group_no(){	return group_no;}
	public void set_group_no(int a){	group_no=a;}
	
	public String get_group_uid(){return group_uid;}
	public void set_group_uid(String a){	group_uid=a;}
	
	public String get_group_pwd(){	return group_pwd;}
	public void set_group_pwd(String a){	group_pwd=a;}
	
	public int get_item_no(){	return item_no;}
	public void set_item_no(int a){	item_no=a;}

	public String get_item_name(){	return item_name;}
	public void set_item_name(String a){	item_name=a;}

	public int get_item_size(){	return item_size;}
	public void set_item_size(int a){	item_size=a;}
	
	
	public String get_item_path(){	return item_path;}
	public void set_item_path(String a){	item_path=a;}

	public int get_hits(){	return hits;}
	public void set_hits(int a){	hits=a;}
	
	public int get_qs_no(){	return qs_no;}
	public void set_qs_no(int a){	qs_no=a;}

	public String get_qs_title(){	return qs_title;}
	public void set_qs_title(String a){	qs_title=a;}
	
	public int get_qs_round(){	return qs_round;}
	public void set_qs_round(int a){	qs_round=a;}
	
	
}
