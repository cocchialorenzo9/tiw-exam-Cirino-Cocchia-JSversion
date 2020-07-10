package daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import beans.CurrentAccount;

public class CurrentAccountDAO {
	private Connection con;

	public CurrentAccountDAO(Connection connection) {
		this.con = connection;
	}

	public CurrentAccount getCAById(int idcurrentAccount) {
		String query = "SELECT * FROM currentAccount WHERE idcurrentAccount = ?";
		try {
			PreparedStatement pstatement = con.prepareStatement(query);
			pstatement.setInt(1, idcurrentAccount);
			ResultSet result = pstatement.executeQuery();
			CurrentAccount newCA = new CurrentAccount();
			if(result.next()) {
				newCA.setIdcurrentAccount(result.getInt("idcurrentAccount"));
				newCA.setTotal(result.getFloat("total"));
				newCA.setCAcode(result.getString("CAcode"));
			} else {
				newCA = null;
			}
			return newCA;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public CurrentAccount getCAByCode(String CAcode) {
		String query = "SELECT * FROM currentAccount WHERE CAcode = ?";
		try {
			PreparedStatement pstatement = con.prepareStatement(query);
			pstatement.setString(1, CAcode);
			ResultSet result = pstatement.executeQuery();
			CurrentAccount newCA = new CurrentAccount();
			if(result.next()) {
				newCA.setIdcurrentAccount(result.getInt("idcurrentAccount"));
				newCA.setTotal(result.getFloat("total"));
				newCA.setCAcode(result.getString("CAcode"));
				newCA.setIduser(result.getInt("iduser"));
			} else {
				newCA = null;
			}
			return newCA;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public List<CurrentAccount> getCAsByUser(int iduser) throws SQLException {
		String query = "SELECT * FROM currentAccount WHERE iduser = ?";
		try {
			PreparedStatement pstatement = con.prepareStatement(query);
			pstatement.setInt(1, iduser);
			ResultSet result = pstatement.executeQuery();
			List<CurrentAccount> returningList = new ArrayList<>();
			while(result.next()) {
				CurrentAccount newCA = new CurrentAccount();
				newCA.setIdcurrentAccount(result.getInt("idcurrentAccount"));
				newCA.setCAcode(result.getString("CAcode"));
				newCA.setTotal(result.getFloat("total"));
				returningList.add(newCA);
			}
			return returningList;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException();
		}
	}
	
	public float getTotalByCode(String CA) throws SQLException {
		String query = "SELECT * FROM currentAccount WHERE CAcode = ? ";
		try {
			PreparedStatement pstatement = con.prepareStatement(query);
			pstatement.setString(1, CA);
			ResultSet result = pstatement.executeQuery();
			if(result.next()) {
				return result.getFloat("total");
			} else {
				return -1;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException();
		}
	}
	
	public boolean updateCheckByAmount(String CA, float amount) throws SQLException {
		String query = "UPDATE currentAccount SET total = ? WHERE CAcode = ? ";
		try {
			PreparedStatement pstatement = con.prepareStatement(query);
			pstatement.setFloat(1, amount);
			pstatement.setString(2, CA);
			int flag = pstatement.executeUpdate();
			if(flag == 0) {
				return false;
			} else {
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException();
		}
	}

}