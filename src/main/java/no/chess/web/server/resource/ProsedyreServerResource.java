package no.chess.web.server.resource;


import org.restlet.resource.ServerResource;

import no.chess.web.control.SessionAdmin;


/**
 * Master klasse for Server resource
 * 
 * @author oluf
 *
 */
public class ProsedyreServerResource extends ServerResource {

	protected SessionAdmin sessionAdmin = null;

	
	
	protected String[]sessionParams;
	
	
	
	

	public SessionAdmin getSessionAdmin() {
		return sessionAdmin;
	}
	public void setSessionAdmin(SessionAdmin sessionAdmin) {
		this.sessionAdmin = sessionAdmin;
	}

	public String[] getSessionParams() {
		return sessionParams;
	}
	public void setSessionParams(String[] sessionParams) {
		this.sessionParams = sessionParams;
	}

	

}
