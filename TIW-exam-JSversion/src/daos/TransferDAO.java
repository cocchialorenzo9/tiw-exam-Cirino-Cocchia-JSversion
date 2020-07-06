package daos;

import java.sql.Connection;

public class TransferDAO {
	private Connection con;

	public TransferDAO(Connection connection) {
		this.con = connection;
	}

}
