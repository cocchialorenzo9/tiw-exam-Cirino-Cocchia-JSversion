package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.text.StringEscapeUtils;

import com.google.gson.Gson;

import beans.CurrentAccount;
import daos.CurrentAccountDAO;
import utils.CoherenceSupervisor;

/**
 * Servlet implementation class GetCurrentAccount
 */
@WebServlet("/GetCurrentAccount")
public class GetCurrentAccount extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	//private ServletContext context = null;
	

	public void init() throws ServletException {
		try {
			ServletContext context = getServletContext();
			String driver = context.getInitParameter("dbDriver");
			String url = context.getInitParameter("dbUrl");
			String user = context.getInitParameter("dbUser");
			String password = context.getInitParameter("dbPassword");
			Class.forName(driver);
			connection = DriverManager.getConnection(url, user, password);

		} catch (ClassNotFoundException e) {
			throw new UnavailableException("Can't load database driver");
		} catch (SQLException e) {
			throw new UnavailableException("Couldn't get db connection");
		}
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		int idCA = -1;
		
		try {
			idCA = Integer.parseInt(StringEscapeUtils.escapeJava(request.getParameter("idCA")));
		} catch (NumberFormatException e) {
			response.setContentType("plain/text");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().println("Can't parse, your current account id, contact the administrator");
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		
		try {
			if(CoherenceSupervisor.checkOwnsThisCurrentAccount(request, connection, idCA)) {
				CurrentAccountDAO caDao = new CurrentAccountDAO(connection);
				CurrentAccount ca = caDao.getCAById(idCA);
				if(ca != null) {
					Gson gson = new Gson();
					String json = gson.toJson(ca);
					response.setContentType("application/json");
					response.setCharacterEncoding("UTF-8");
					response.getWriter().write(json);
				} else  {
					response.setContentType("plain/text");
					response.setCharacterEncoding("UTF-8");
					response.getWriter().println("It does not exist such current account id, can't complete the request");
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					return;
				}
			} else {
				response.setContentType("plain/text");
				response.setCharacterEncoding("UTF-8");
				response.getWriter().println("You don't own that current account, can't complete this request");
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				return;
			}
		} catch (SQLException e) {
			response.setContentType("plain/text");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().println("There was an error while connecting to the server, retry later");
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return;
		}
		
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
