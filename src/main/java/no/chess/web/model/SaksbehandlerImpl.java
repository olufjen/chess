package no.chess.web.model;

import java.sql.Types;

/**
 * SaksbehandlerImpl
 * Representerer tabellen saksbehandler i databasen.
 * @since Januar 2018
 * Tilpasset endring/oppdatering av saksbehandlers passord
 * @author olj
 *
 */
public class SaksbehandlerImpl extends AbstractSaksbehandler implements Saksbehandler {

	private Object[] pwParams;
	private int[] pwtypes;
	public SaksbehandlerImpl() {
		super();
		types = new int[] {Types.VARCHAR,Types.VARCHAR,Types.VARCHAR,Types.VARCHAR};
		utypes = new int[]{Types.VARCHAR,Types.VARCHAR,Types.VARCHAR,Types.VARCHAR,Types.INTEGER};
		pwtypes = new int[]{Types.VARCHAR,Types.INTEGER};
	}
	public void setParams(){
		Long id = getSakbehandlerid();
		
		if (id == null){
			params = new Object[]{getBehandernavn(),getBehandlerepost(),getBehandlertlf(),getBehandlerpassord()};
		}else
			params = new Object[]{getBehandernavn(),getBehandlerepost(),getBehandlertlf(),getBehandlerpassord(),getSakbehandlerid()};
		
	}	
	public void savetoSaksbehandler(){
		setBehandlerepost(null);
		setBehandlerpassord(null);
		setDbChoice(null);
	}
	public Object[] getPwParams() {
		return pwParams;
	}
	public void setPwParams(Object[] pwParams) {
		this.pwParams = pwParams;
	}
	public int[] getPwtypes() {
		return pwtypes;
	}
	public void setPwtypes(int[] pwtypes) {
		this.pwtypes = pwtypes;
	}
	
}
