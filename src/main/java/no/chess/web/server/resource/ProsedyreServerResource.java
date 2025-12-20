package no.chess.web.server.resource;

import no.chess.web.control.SessionAdmin;
import no.chess.web.control.SessionAdminImpl;




/**
 * Master klasse for Server resource
 * 
 * @author oluf
 *
 */
public abstract class ProsedyreServerResource  {

	protected final SessionAdminImpl sessionAdmin = new SessionAdminImpl();

	
	
	protected String[]sessionParams;
	
	
	
	

	public SessionAdminImpl getSessionAdmin() {
		return sessionAdmin;
	}

	public String[] getSessionParams() {
		return sessionParams;
	}
	public void setSessionParams(String[] sessionParams) {
		this.sessionParams = sessionParams;
	}

	

}
