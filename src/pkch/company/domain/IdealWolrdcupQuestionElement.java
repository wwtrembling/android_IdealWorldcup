package pkch.company.domain;

import java.io.Serializable;

public class IdealWolrdcupQuestionElement implements Serializable{
	private static final long serialVersionUID = -4943568370424077015L;
	private int qs_no;
	private int qs_round;
	private String qs_title;
	private String group_uid;
	private int group_no;
	
	public void setQsNo(int a){this.qs_no=a;}
	public int getQsNo(){return this.qs_no;}
	
	public void setQsRound(int a){this.qs_round=a;}
	public int getQsRound(){return this.qs_round;}
	
	public void setQsTitle(String a){this.qs_title=a;}
	public String getQsTitle(){return this.qs_title;}
	
	public void setGroupUid(String a){this.group_uid=a;}
	public String getGroupUid(){return this.group_uid;}
	
	public void setGroupNo(int a){this.group_no=a;}
	public int getGroupNo(){return this.group_no;}
}
