package daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import beans.User;

public class UserDAO {
	private Connection con;

	public UserDAO(Connection connection) {
		this.con = connection;
	}

	public User checkCredentials(String username, String pwd) throws SQLException {
		String query = "SELECT * FROM user WHERE username = ? AND password = ? ";
		try (PreparedStatement pstatement = con.prepareStatement(query);) {
			pstatement.setString(1, username);
			pstatement.setString(2, pwd);
			try (ResultSet result = pstatement.executeQuery();) {
				if (!result.isBeforeFirst()) // no results, credential check failed
					return null;
				else {
					result.next();
					User user = new User();
					user.setIduser(result.getInt("iduser"));
					user.setUsername(result.getString("username"));
					user.setUsercode(result.getString("usercode"));
					user.setEmail(result.getString("email"));
					return user;
				}
			}
		}
	}
	
	public boolean newUser(String username, String pwd, String usercode, String email) throws SQLException {
		String query = "INSERT INTO user (username, password, usercode, email) VALUES (?, ?, ?, ?) ";
		try (PreparedStatement pstatement = con.prepareStatement(query);) {
			pstatement.setString(1, username);
			pstatement.setString(2,  pwd);
			pstatement.setString(3,  pwd);
			pstatement.setString(4, email);
			int flag = 0;
			try {
				flag = pstatement.executeUpdate();
				if(flag == 0) {
					return false;
				} else {
					return true;
				}
			} catch (SQLException sqlException) {
				sqlException.printStackTrace();
				throw new SQLException();
			}			
		}
	}
	
	public boolean isYetExistent(String usrn) throws SQLException {
		String query = "SELECT * FROM user WHERE username = ? ";
		int counter = 0;
		PreparedStatement pstatement = con.prepareStatement(query);
		pstatement.setString(1,  usrn);
		ResultSet result = pstatement.executeQuery();
		while(result.next()) {
			counter++;
		}
		return counter != 0;
	}
	
	public User getUserByCode(String usercode) throws SQLException {
		String query = "SELECT * FROM user WHERE usercode = ? ";
		PreparedStatement pstatement = con.prepareStatement(query);
		pstatement.setString(1,  usercode);
		ResultSet result = pstatement.executeQuery();
		if(!result.isBeforeFirst())
			return null;
		else {
			result.next();
			User user = new User();
			user.setIduser(result.getInt("iduser"));
			user.setUsername(result.getString("username"));
			user.setUsercode(result.getString("usercode"));
			user.setEmail(result.getString("usercode"));
			return user;
		}
	}
	
	public User getUserById(int iduser) throws SQLException {
		String query = "SELECT * FROM user WHERE iduser = ? ";
		PreparedStatement pstatement = con.prepareStatement(query);
		pstatement.setInt(1, iduser);
		ResultSet result = pstatement.executeQuery();
		if(!result.isBeforeFirst()) {
			return null;
		} else {
			result.next();
			User user = new User();
			user.setIduser(result.getInt("iduser"));
			user.setUsername(result.getString("username"));
			user.setUsercode(result.getString("usercode"));
			user.setEmail(result.getString("usercode"));
			return user;
		}
	}
	
}
