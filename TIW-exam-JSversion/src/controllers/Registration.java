package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.text.StringEscapeUtils;

import daos.UserDAO;
import utils.ConnectionHandler;


@WebServlet("/Registration")
@MultipartConfig
public class Registration extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String username = null;
		String password = null;
		String repPassword = null;
		String usercode = null;
		String email = null;
		
		System.out.println("Registration invoked");
		
		username = StringEscapeUtils.escapeJava(request.getParameter("username"));
		password = StringEscapeUtils.escapeJava(request.getParameter("password"));
		repPassword = StringEscapeUtils.escapeJava(request.getParameter("repPassword"));
		usercode = StringEscapeUtils.escapeJava(request.getParameter("usercode"));
		email = StringEscapeUtils.escapeJava(request.getParameter("email"));
		
		if(username == null || password == null || repPassword == null || usercode == null || email == null || 
				username.isEmpty() || password.isEmpty() || repPassword.isEmpty() || usercode.isEmpty() || email.isEmpty()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Registration credentials must be not null or empty");
			return;
		}
		
		if(!password.equals(repPassword)) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("The two passwords are not matching");
			return;
		}
		
		UserDAO userDao = new UserDAO(connection);
		try {
			boolean flag = userDao.newUser(username, password, usercode, email);
			if(flag) {
				response.setStatus(HttpServletResponse.SC_OK);
				return;
			} /*else {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.getWriter().println("Username yet existed");
				return;
			}*/
		} catch (SQLException e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("User registration not successful: internal server error, retry");
			return;
		}
	}
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
