package filters;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


@WebFilter(
		urlPatterns = {"/GetAllContacts", "/GetAllTransfers", "/GetCurrentAccountsList", "/NewContact", "/NewTransfer"}
)
public class SessionFilter implements Filter {

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		String loginpath = req.getServletContext().getContextPath() + "/index.html";
		
		System.out.println("Filtering ...");

		HttpSession s = req.getSession();
		if (s.isNew() || s.getAttribute("user") == null) {
			System.out.println("This request has no valid session");
			res.sendRedirect(loginpath);
			return;
		}
		chain.doFilter(request, response);
	}


}
