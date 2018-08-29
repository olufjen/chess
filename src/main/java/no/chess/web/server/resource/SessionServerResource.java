package no.chess.web.server.resource;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.restlet.Request;
import org.restlet.data.Reference;

/*import no.naks.biovigilans.model.Annenkomplikasjon;
import no.naks.biovigilans.model.Blodprodukt;
import no.naks.biovigilans.model.Forebyggendetiltak;
import no.naks.biovigilans.model.Giverkomplikasjon;
import no.naks.biovigilans.model.Komplikasjonsklassifikasjon;
import no.naks.biovigilans.model.Pasient;
import no.naks.biovigilans.model.Pasientkomplikasjon;
import no.naks.biovigilans.model.Produktegenskap;
import no.naks.biovigilans.model.Sykdom;
import no.naks.biovigilans.model.Symptomer;
import no.naks.biovigilans.model.Tiltak;
import no.naks.biovigilans.model.Transfusjon;
import no.naks.biovigilans.model.Utredning;
import no.naks.biovigilans.model.Vigilansmelding;*/
import no.chess.web.control.EmailWebService;
import no.chess.web.model.LoginModel;


/**
 * SessionServerResource
 * Denne klassen inneholder alle Webmodel objekter for en session.
 * Den er felles superklasse for alle ResourceHtml klassene
 * @author olj
 *
 */
public class SessionServerResource extends ProsedyreServerResource {

/*
 * Session objekter 
 */



    protected EmailWebService emailWebService;
    protected String displayKey = "display";

/*
 * Login objekter
 */
    protected LoginModel login = null;
    protected String loginKey = "login";
    
	public EmailWebService getEmailWebService() {
		return emailWebService;
	}

	public void setEmailWebService(EmailWebService emailWebService) {
		this.emailWebService = emailWebService;
	}



	public LoginModel getLogin() {
		return login;
	}

	public void setLogin(LoginModel login) {
		this.login = login;
	}

	public String getLoginKey() {
		return loginKey;
	}

	public void setLoginKey(String loginKey) {
		this.loginKey = loginKey;
	}


	public String getDisplayKey() {
		return displayKey;
	}

	public void setDisplayKey(String displayKey) {
		this.displayKey = displayKey;
	}

	/**
	 * invalidateSessionobjects
	 * Denne rutinen fjerner alle session objekter
	 */
	public void invalidateSessionobjects(){

	}




}
