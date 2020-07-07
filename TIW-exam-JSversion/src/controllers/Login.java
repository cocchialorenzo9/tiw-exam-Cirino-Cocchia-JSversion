package controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.text.StringEscapeUtils;

import com.google.gson.Gson;

import beans.User;
import daos.UserDAO;
import utils.ConnectionHandler;

@WebServlet("/Login")
public class Login extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String usrn = null;
		String pwd = null;
		usrn = StringEscapeUtils.escapeJava(request.getParameter("logUsername"));
		System.out.println("Login:: usern: " + usrn);
		pwd = StringEscapeUtils.escapeJava(request.getParameter("logPassword"));
		if (usrn == null || pwd == null || usrn.isEmpty() || pwd.isEmpty() ) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Credentials must be not null or empty");
			return;
		}
		UserDAO userDao = new UserDAO(connection);
		User user = null;
		try {
			user = userDao.checkCredentials(usrn, pwd);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Internal server error, retry later");
			return;
		}

		if (user == null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().println("Incorrect credentials");
		} else {
			Gson userGson = new Gson();
			request.getSession().setAttribute("user", user);
			response.setStatus(HttpServletResponse.SC_OK);
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().println(userGson.toJson(user));
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