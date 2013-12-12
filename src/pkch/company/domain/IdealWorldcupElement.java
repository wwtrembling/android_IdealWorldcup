package pkch.company.domain;

import java.io.Serializable;
import android.util.Log;

public class IdealWorldcupElement implements Serializable{
	private static final long serialVersionUID=-4943568370424077035L;
	
	private int imgno;
	private String imgname;
	private String imgdir;
	private int hits;
	private int rank;
	private boolean clicked;
	private int groupno; // 소유자 고유번호
	private String groupuid; // 소유자


	public String getItemGroupuid(){return this.groupuid;}
	public void setItemGroupuid(String groupuid){this.groupuid=groupuid;}
	public int getItemGroupno(){return this.groupno;}
	public void setItemGroupno(int groupno){this.groupno=groupno;}
	
	public int getItemNo(){return this.imgno;}
	public void setItemNo(int imgno){this.imgno=imgno;}
	public String getItemName(){return imgname;}
	public void setItemName(String name){this.imgname=name;}
	public String getItemDir(){return imgdir;}
	public void setItemDir(String imgdir){this.imgdir=imgdir;}
	public Integer getHits(){return hits;}	
	public void setHits(int hits){this.hits=hits;}
	public boolean getClicked(){return clicked;}
	public void setClicked(boolean clicked){this.clicked=clicked;}
	public Integer getRank(){return rank;}
	public void setRank(int rank){this.rank=rank;}
}
