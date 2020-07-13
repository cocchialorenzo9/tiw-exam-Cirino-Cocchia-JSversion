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
import javax.servlet.http.HttpSession;

import org.apache.commons.text.StringEscapeUtils;

import beans.User;
import daos.ContactDAO;
import daos.CurrentAccountDAO;
import daos.UserDAO;
import utils.ConnectionHandler;

@WebServlet("/NewContact")
@MultipartConfig
public class NewContact extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String CAcode = null;
		HttpSession session = request.getSession();
		User user = (User) session.getAttribute("user");
		
		CAcode = StringEscapeUtils.escapeJava(request.getParameter("CAcode"));
		
		//System.out.println("Adding " + CAcode + " as a contact to the session user");
		
		if(CAcode == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("CAcode parameter is not valid, contact not addable");
			return;
		}
		
		UserDAO uDao = new UserDAO(connection);
		CurrentAccountDAO caDao = new CurrentAccountDAO(connection);
		ContactDAO contactDao = new ContactDAO(connection);
		
		int id_ownerCAContact = caDao.getCAByCode(CAcode).getIduser();
		String usercode_ownerCAContact;
		
		//System.out.println("His owner is " + id_ownerCAContact);
		
		try {
			usercode_ownerCAContact = uDao.getUserById(id_ownerCAContact).getUsercode();
		} catch (SQLException e1) {
			e1.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Usercode parameter is not retrievable, contact not addable");
			return;
		}
		
		int iduser = user.getIduser();
		
		try {
			if(!contactDao.isUsersContactPresentYet(iduser, usercode_ownerCAContact, CAcode)) {
				contactDao.newUsersContact(iduser, usercode_ownerCAContact, CAcode);
			} else {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("This contact was yet in the list");
				return;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Can't add contact due to internal server reasons, retry later");
			return;
		}
		
		response.setStatus(HttpServletResponse.SC_OK);
		//Gson caGson = new Gson();
		//response.setContentType("application/json");
		//response.setCharacterEncoding("UTF-8");
		//response.getWriter().println(caGson.toJson(contact));
	}
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
