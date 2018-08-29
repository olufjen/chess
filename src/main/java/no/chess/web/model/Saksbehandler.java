package no.chess.web.model;

import java.util.Map;

public interface Saksbehandler {

	public Long getSakbehandlerid();
	public void setSakbehandlerid(Long sakbehandlerid);
	public String getBehandernavn();
	public void setBehandernavn(String behandernavn);
	public String getBehandlerepost();
	public void setBehandlerepost(String behandlerepost);
	public String getBehandlertlf();
	public void setBehandlertlf(String behandlertlf);
	public String getBehandlerpassord();
	public void setBehandlerpassord(String behandlerpassord);
	public void setParams();
	public Object[] getParams();
	public int[] getTypes();
	public int[] getUtypes();
	public Map<String, String> getBehandlerFields();
	public void setBehandlerFields(Map<String, String> behandlerFields);
	public void savetoSaksbehandler();
	public Object[] getPwParams();
	public void setPwParams(Object[] pwParams);
	public int[] getPwtypes();
	public void setPwtypes(int[] pwtypes);
	
	public String getDbChoice();
	public void setDbChoice(String dbChoice);
	
}
