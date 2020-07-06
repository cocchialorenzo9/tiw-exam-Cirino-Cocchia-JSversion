package daos;

import java.sql.Connection;

public class ContactDAO {
	private Connection con;

	public ContactDAO(Connection connection) {
		this.con = connection;
	}


}
