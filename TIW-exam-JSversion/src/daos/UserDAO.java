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
		String query = "SELECT * FROM user WHERE usercode = ? AND password =?";
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
					user.setEmail(result.getString("email"));
					return user;
				}
			}
		}
	}
	
	public boolean newUser(String username, String pwd, String email) throws SQLException {
		String query = "INSERT INTO user (username, password, email) VALUES (?, ?, ?) ";
		try (PreparedStatement pstatement = con.prepareStatement(query);) {
			pstatement.setString(1, username);
			pstatement.setString(2,  pwd);
			pstatement.setString(3, email);
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
}
