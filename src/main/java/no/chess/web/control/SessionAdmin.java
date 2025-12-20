package no.chess.web.control;

import javax.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Context;

/**
 * Dette en grensesnittdefinisjon for SessionAdmin
 * Klassen håndterer alle session objekter for Restlet resurser
 * @author olj
 *
 */
public interface SessionAdmin {
	
	public Object getSessionObject(@Context javax.servlet.http.HttpServletRequest request,String idKey);
	public void setSessionObject(@Context HttpServletRequest request,Object o,String idKey);
	public HttpSession getSession(@Context HttpServletRequest request,String idKey);
	public String[] getSessionParams();
	public void setSessionParams(String[] sessionParams);

}
