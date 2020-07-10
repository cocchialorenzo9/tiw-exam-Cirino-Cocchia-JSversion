package utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import beans.CurrentAccount;
import beans.User;
import daos.CurrentAccountDAO;

public class CoherenceSupervisor {
	
	public static boolean checkOwnsThisCurrentAccount(HttpServletRequest request, Connection connection, int idcurrentAccount) throws SQLException {
		User user = (User) request.getSession().getAttribute("user");
		CurrentAccountDAO caDao = new CurrentAccountDAO(connection);
		
		
		List<CurrentAccount> ownedByUser = caDao.getCAsByUser(user.getIduser());
		
		for(CurrentAccount ca: ownedByUser) {
			if(ca.getIdcurrentAccount() == idcurrentAccount) {
				return true;
			}
		}
		
		return false;
	}

}
