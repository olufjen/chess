package no.chess.web.control;



import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Denne klassen administerer session objekter for Resurser 
 * @author olj
 * @since December 2025
 * Reworked for use of jakarta
 *
 */
public class SessionAdminImpl {
	private String[]sessionParams;
	
	public SessionAdminImpl() {
		super();
		  System.out.println("SessionAdmin felles started");
	}

	public String[] getSessionParams() {
		return sessionParams;
	}

	public void setSessionParams(String[] sessionParams) {
		this.sessionParams = sessionParams;
	}

	public Object getSessionObject(HttpServletRequest request,String idKey) {
		  HttpSession session = request.getSession(true);
          Object sessionObject = session.getAttribute(idKey);
		return sessionObject;
	}
	public void setSessionObject(HttpServletRequest request,Object o,String idKey) {
		  HttpSession session = request.getSession(true);
		  session.setAttribute(idKey, o);
	}
	public HttpSession getSession(HttpServletRequest request) {
		  HttpSession session = request.getSession(true);
		  return session;
	}


}
