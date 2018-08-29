package no.chess.web.control;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.restlet.Request;
import org.restlet.ext.servlet.ServletUtils;

/**
 * Denne klassen administerer session objekter for Restlet Resurser 
 * @author olj
 *
 */
public class SessionAdminImpl implements SessionAdmin {
	private String[]sessionParams;
	
	public SessionAdminImpl() {
		super();
		  System.out.println("SessionAdmin felles started");
	}

	@Override
	public Object getSessionObject(Request request,String idKey) {
	     HttpServletRequest req = ServletUtils.getRequest(request);
	     HttpSession session = req.getSession();
	     Object result = session.getAttribute(idKey);
		return result;
	}

	@Override
	public void setSessionObject(Request request, Object o, String idKey) {
		  HttpServletRequest req = ServletUtils.getRequest(request);
		  HttpSession session = req.getSession();
		  session.setAttribute(idKey, o);

	}

	@Override
	public HttpSession getSession(Request request, String idKey) {
		HttpServletRequest req = ServletUtils.getRequest(request);
		HttpSession session = req.getSession();
		return session;
	}

	public String[] getSessionParams() {
		return sessionParams;
	}

	public void setSessionParams(String[] sessionParams) {
		this.sessionParams = sessionParams;
	}
	

}
