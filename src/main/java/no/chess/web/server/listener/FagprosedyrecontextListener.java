package no.chess.web.server.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class FagprosedyrecontextListener implements ServletContextListener {

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		// TODO Auto-generated method stub
		System.out.println("Custom listeners destroyed.");
	
	}

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		// TODO Auto-generated method stub
		System.out.println("Custom listeners initializes from context : "  + sce.getServletContext().getContextPath());
	
	}

}
