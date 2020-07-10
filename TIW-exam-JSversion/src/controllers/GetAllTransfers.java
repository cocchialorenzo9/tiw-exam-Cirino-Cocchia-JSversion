package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.text.StringEscapeUtils;

import com.google.gson.Gson;

import beans.CurrentAccount;
import beans.Transfer;
import daos.CurrentAccountDAO;
import daos.TransferDAO;
import utils.ConnectionHandler;

/**
 * Servlet implementation class GetCurrentAccount
 */
@WebServlet("/GetAllTransfers")
public class GetAllTransfers extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		int idCA = -1;
		try {
			idCA = Integer.parseInt(StringEscapeUtils.escapeJava(request.getParameter("idCA")));
		} catch (NumberFormatException e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Not possible to recover CA transfer due to idCA unparsable to int");
			return;
		}
		
		TransferDAO transferDao = new TransferDAO(connection);
		CurrentAccountDAO caDao = new CurrentAccountDAO(connection);
		List<Transfer> allTransfers = new ArrayList<>();
		
		try {
			CurrentAccount ca = caDao.getCAById(idCA);
			allTransfers = transferDao.getTransfersByCACode(ca.getCAcode());
		} catch (SQLException e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Not possible to recover user contacts due to server problems");
			return;
		}
		
		Gson gson = new Gson();
		String json = gson.toJson(allTransfers);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(json);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
