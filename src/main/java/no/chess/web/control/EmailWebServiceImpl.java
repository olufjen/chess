package no.chess.web.control;

import no.naks.rammeverk.mailer.Mailer;

public class EmailWebServiceImpl implements EmailWebService {

		private Mailer mailer ;
	   //Authentication Sender email address
		private String mailAuthenticatorUser;
		//Authentication sender email password 
		private String mailAuthenticatorPwd;
		private String mailTo ="qadeeralvi@gmail.com";
		private String mailFrom ;
		private String host ;
	    private String port ;
	    private String subject;
	    private String emailText;
	    
	    
		public String getSubject() {
			return subject;
		}
		public void setSubject(String subject) {
			this.subject = subject;
		}
		public String getEmailText() {
			return emailText;
		}
		public void setEmailText(String emailText) {
			this.emailText = emailText;
		}
		public String getMailAuthenticatorUser() {
			return mailAuthenticatorUser;
		}
		public void setMailAuthenticatorUser(String mailAuthenticatorUser) {
			this.mailAuthenticatorUser = mailAuthenticatorUser;
		}
		public String getMailAuthenticatorPwd() {
			return mailAuthenticatorPwd;
		}
		public void setMailAuthenticatorPwd(String mailAuthenticatorPwd) {
			this.mailAuthenticatorPwd = mailAuthenticatorPwd;
		}
		public String getMailTo() {
			return mailTo;
		}
		public void setMailTo(String mailTo) {
			this.mailTo = mailTo;
		}
		public String getMailFrom() {
			return mailFrom;
		}
		public void setMailFrom(String mailFrom) {
			this.mailFrom = mailFrom;
		}
		public String getHost() {
			return host;
		}
		public void setHost(String host) {
			this.host = host;
		}
		public String getPort() {
			return port;
		}
		public void setPort(String port) {
			this.port = port;
		}
		public Mailer getMailer() {
			return mailer;
		}
		public void setMailer(Mailer mailer) {
			this.mailer = mailer;
		}
	    
	    public void sendEmail(String mailInfo){
	    	mailer.setMailAuthenticatorUser(mailAuthenticatorUser);
	    	mailer.setMailAuthenticatorPwd(mailAuthenticatorPwd);
	    	mailer.setHost(host);
	    	mailer.setPort(port);
	    	if(mailFrom == null || mailFrom.isEmpty() )
	    		mailFrom = mailAuthenticatorUser;
	    	mailer.setMailFrom(mailFrom);
	    	mailer.setMailTo(mailTo);
	    	mailer.setMailSubject(subject);
	    	mailer.setMailText(emailText+mailInfo);
	    	mailer.sendEmail();
	    }
	    
}
