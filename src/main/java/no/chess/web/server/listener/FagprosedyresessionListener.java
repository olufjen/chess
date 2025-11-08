package no.chess.web.server.listener;

import jakarta.servlet.http.HttpSessionAttributeListener;
import jakarta.servlet.http.HttpSessionBindingEvent;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

public class FagprosedyresessionListener implements HttpSessionListener {




	@Override
	public void sessionCreated(HttpSessionEvent sEvent) {
		// TODO Auto-generated method stub
		System.out.println("Custom session initializes session id : "  + sEvent.getSession().getId());
		System.out.println("Custom session initializes from context : "+ sEvent.getSession().getServletContext().getContextPath());
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent sEvent) {
		// TODO Auto-generated method stub
		System.out.println("Custom session initializes session destroyed"+ sEvent.getSession().getId());
	}

}
