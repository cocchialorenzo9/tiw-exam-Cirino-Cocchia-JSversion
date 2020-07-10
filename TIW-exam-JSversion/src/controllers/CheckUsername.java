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


@WebServlet("/CheckUsername")
@MultipartConfig
public class CheckUsername extends HttpServlet {
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
		boolean flag = false;
		
		System.out.println("CheckUsername invoked");
		
		username = StringEscapeUtils.escapeJava(request.getParameter("username"));
		if(username == null || username.isEmpty()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("username can not be null or empty");
			return;
		}
		
		UserDAO userDao = new UserDAO(connection);
		try {
			System.out.println("Checking if " + username + " is a yet existent username");
			flag = userDao.isYetExistent(username);
			System.out.println("Username was yet existent? " + flag);
			if(!flag) {
				System.out.println("Sending code to username request " + HttpServletResponse.SC_OK);
				response.setStatus(HttpServletResponse.SC_OK);
			} else {
				System.out.println("Sending code to username request " + HttpServletResponse.SC_BAD_REQUEST);
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("This username was already taken");
			}
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("A server error made impossible to check username");
		}
	}

}
