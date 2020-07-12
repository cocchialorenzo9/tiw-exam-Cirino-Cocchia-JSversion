package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.translate.UnicodeUnescaper;

import com.google.gson.Gson;

import beans.CurrentAccount;
import beans.User;
import daos.CurrentAccountDAO;
import daos.TransferDAO;
import daos.UserDAO;
import utils.CoherenceSupervisor;
import utils.ConnectionHandler;

/**
 * Servlet implementation class NewTransfer
 */
@WebServlet("/NewTransfer")
@MultipartConfig
public class NewTransfer extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
		
		float amount = 0;
		String reason = null;
		int idCApayer = -1;
		String userCodePayee = null;
		String CApayee = null;
		
		CurrentAccount payer = null;
		CurrentAccount payee = null;
		
		String errorMessage = "";
		
		CurrentAccountDAO caDao = new CurrentAccountDAO(connection);
		
		//input controls section
		try {
			
			amount = Float.parseFloat(StringEscapeUtils.escapeJava(request.getParameter("amount")));
			
			//System.out.println("NewTransfer:: unescaped string: " + request.getParameter("reason"));
			
			reason = StringEscapeUtils.escapeJava(request.getParameter("reason"));
			
			//System.out.println("NewTransfer::" + reason);
			
		    String utf8Reason = new UnicodeUnescaper().translate(reason);

			//System.out.println(utf8Reason);
			reason = utf8Reason;
			
			userCodePayee = StringEscapeUtils.escapeJava(request.getParameter("userCodePayee"));
			CApayee = StringEscapeUtils.escapeJava(request.getParameter("CApayee"));
			
			try {
				idCApayer = Integer.parseInt(StringEscapeUtils.escapeJava(request.getParameter("CApayer"))); //hidden value
			} catch (NumberFormatException e) {
				errorMessage = "It was impossible to parse payer's current account id, please contact the administrator";
				throw new IllegalArgumentException();
			}
			
			//System.out.println("NewTransfer:: Hidden value was: " + idCApayer);
			
			if(!CoherenceSupervisor.checkOwnsThisCurrentAccount(request, connection, idCApayer)) {
				errorMessage = "You don't own such current account, can't complete the request";
				throw new IllegalArgumentException();
			}
			
			payer = caDao.getCAById(idCApayer);

			if(payer == null) {
				errorMessage = "It was impossible to reach payer's current account information, please contact the administrator";
				throw new IllegalArgumentException();
			}
						
			if(reason == null || userCodePayee == null || CApayee == null) {
				errorMessage = "You can't pass null strings";
				//response.getWriter().println("You can't pass null strings");
				throw new IllegalArgumentException();
			} else if(amount <= 0) {
				errorMessage = "You can't do a transfer with an amount less than or equals to 0";
				//response.getWriter().println("You can't do a transfer with an amount less than or equals to 0");
				throw new IllegalArgumentException();
			} else if(reason.isEmpty() || userCodePayee.isEmpty() || CApayee.isEmpty()) {
				errorMessage = "You can't pass empty strings";
				//response.getWriter().println("You can't pass empty strings");
				throw new IllegalArgumentException();
			} else if(userCodePayee.length() != 4 || CApayee.length() != 4){
				errorMessage = "A code length is incorrect";
				//response.getWriter().println("Current Account length is incorrect");
				throw new IllegalArgumentException();
			} else if (payer.getCAcode().equals(CApayee)) {
				errorMessage = "Can't transfer an amount from an account to the same account";
				//response.getWriter().println("Can't transfer an amout from an account to the same account");
				throw new IllegalArgumentException();
			} else {
				try {
					Integer.parseInt(userCodePayee);
					Integer.parseInt(CApayee);
				} catch (NumberFormatException e) {
					errorMessage ="You submitted codes that did not contain only numbers";
					throw new IllegalArgumentException();
				}
				
				UserDAO uDao = new UserDAO(connection);
				List<CurrentAccount> caPayeeList = new ArrayList<>();
				User userPayee = null;
				
				try {
					userPayee = uDao.getUserByCode(userCodePayee);
				} catch (SQLException e) {
					e.printStackTrace();
					errorMessage = "Impossible to reach information about user with the submitted user code";
					throw new IllegalArgumentException();
				}
				
				if(userPayee == null) {
					errorMessage = "There was no user with that user code";
					throw new IllegalArgumentException();
				}
				
				try {
					caPayeeList = caDao.getCAsByUser(userPayee.getIduser());
				} catch (SQLException e) {
					errorMessage = "There was an error retreiving submitted user information";
					throw new IllegalArgumentException();
				}
				
				payee = caDao.getCAByCode(CApayee);
				
				if(payer == null || payee == null) {
					errorMessage = "You inserted an invalid current account code";
					//response.getWriter().println("You inserted an invalid code");
					throw new IllegalArgumentException();
				} else if(payer.getTotal() < amount) {
					errorMessage = "Payer can't afford that amount of money";
					//response.getWriter().println("Payer can't afford that amount of money");
					throw new IllegalArgumentException();
				} else if(!listContainsId(caPayeeList, payee)) {
					errorMessage = "There was no corrispondence between Current Account code and User code specified";
					throw new IllegalArgumentException();
				}
			}
		} catch (IllegalArgumentException | SQLException e) {
			if(errorMessage.contentEquals("")) {
				errorMessage = "You inserted an argument considered illegal";
			}
			response.setContentType("plain/text");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().println(errorMessage);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;

		}
		
		//transaction section
		//at this point I'm sure that input is coherent, now it is only to preserve DB integrity by Isolation (ACID)
		//and rollback in the cases in which there is some problem with DB (not due to input, but to connection)
		//https://dev.mysql.com/doc/refman/8.0/en/innodb-transaction-isolation-levels.html
		//MYSQL standard isolation is REPEATABLE_READ, it prevents dirty reads and non-repeatable reads, while allows
		//phantom reads
		//https://docs.oracle.com/javase/tutorial/jdbc/basics/transactions.html		
		
		TransferDAO transferDao = new TransferDAO(connection);
		Savepoint savepoint = null;
		
		try {
			connection.setAutoCommit(false);
			savepoint = connection.setSavepoint();
			
		} catch (SQLException e) {
			e.printStackTrace();
			response.getWriter().println("There was an error while connecting to the server, retry later");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;

		}
		
		try {
			
			float checkPayer = caDao.getTotalByCode(payer.getCAcode());
			float checkPayee = caDao.getTotalByCode(CApayee);
			boolean transPayer = caDao.updateCheckByAmount(payer.getCAcode(), checkPayer - amount);
			boolean transPayee = caDao.updateCheckByAmount(CApayee, checkPayee + amount);
			boolean transRecord = transferDao.newTransfer(amount, reason, payer.getCAcode(), CApayee);
			if(!transPayer || !transPayee || !transRecord) {
				throw new SQLException();
			} else {
				connection.commit();
				response.setStatus(HttpServletResponse.SC_OK);
				Gson caGson = new Gson();
				response.setContentType("application/json");
				response.setCharacterEncoding("UTF-8");
				response.getWriter().println(caGson.toJson(payee));
			}
			
		//exception handling
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				connection.rollback(savepoint);
				response.getWriter().println("There was an error during the transaction, doing rollback, retry later");
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			} catch (SQLException e2) {
				e2.printStackTrace();
				response.getWriter().println("There was an error during the transaction, rollback thrown an exception, there can be some problem in the database, please contact the administrator");
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		}
	}
	
	public void destroy() {
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
		}
	}
	
	private boolean listContainsId(List<CurrentAccount> list, CurrentAccount caBean) {
		for(CurrentAccount ca : list) {
			if(ca.getIdcurrentAccount() == caBean.getIdcurrentAccount()) {
				return true;
			}
		}
		return false;
	}

}
