package daos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import beans.Contact;

public class ContactDAO {
	private Connection con;

	public ContactDAO(Connection connection) {
		this.con = connection;
	}

	public boolean newUsersContact(int iduser, String CAcodeContact) throws SQLException{
		String query = "INSERT INTO contact (CAcode, iduser) VALUES (?, ?)";
		try {
			PreparedStatement pstatement = con.prepareStatement(query);
			pstatement.setString(1, CAcodeContact);
			pstatement.setInt(2,  iduser);
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
	
	public List<Contact> getAllUsersContacts(int iduser) throws SQLException {
		String query = "SELECT * FROM contact WHERE iduser = ?";
		try {
			PreparedStatement pstatement = con.prepareStatement(query);
			pstatement.setInt(1,  iduser);
			ResultSet result = pstatement.executeQuery();
			List<Contact> allContacts = new ArrayList<>();
			
			while(result.next()) {
				Contact contact = new Contact();
				contact.setIdContact(result.getInt("idcontact"));
				contact.setCAcode(result.getString("CAcode"));
				contact.setIduser(result.getInt("iduser"));
				allContacts.add(contact);
			}
			
			return allContacts;
			
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException();
		}
	}
}
