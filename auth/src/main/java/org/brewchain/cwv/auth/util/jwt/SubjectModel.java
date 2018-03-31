package org.brewchain.cwv.auth.util.jwt;

/**
 * Token 主题模型
 * @author Moon
 *
 */
public class SubjectModel {

	private int uid;
	
	private String uty;
	
	public SubjectModel(int uid, String uty) {
		super();
		this.uid = uid;
		this.uty = uty;
	}

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public String getUty() {
		return uty;
	}

	public void setUty(String uty) {
		this.uty = uty;
	}
	
}
