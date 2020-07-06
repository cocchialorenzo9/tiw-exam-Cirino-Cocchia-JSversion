package daos;

import java.sql.Connection;

public class CurrentAccountDAO {
	private Connection con;

	public CurrentAccountDAO(Connection connection) {
		this.con = connection;
	}

}
