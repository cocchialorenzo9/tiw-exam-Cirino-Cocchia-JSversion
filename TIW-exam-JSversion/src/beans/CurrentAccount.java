package beans;

public class CurrentAccount {
	private int idcurrentAccount;
	private String CAcode;
	private float check;
	private int iduser;
	public int getIdcurrentAccount() {
		return idcurrentAccount;
	}
	public void setIdcurrentAccount(int idcurrentAccount) {
		this.idcurrentAccount = idcurrentAccount;
	}
	public String getCAcode() {
		return CAcode;
	}
	public void setCAcode(String cAcode) {
		CAcode = cAcode;
	}
	public float getCheck() {
		return check;
	}
	public void setCheck(float check) {
		this.check = check;
	}
	public int getIduser() {
		return iduser;
	}
	public void setIduser(int iduser) {
		this.iduser = iduser;
	}
	
}
