package com.session.servlet;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import com.session.common.SessionStore;
import com.session.rpc.RPCServer;
import com.session.view.MembershipViewStore;

/**
 * Application Lifecycle Listener implementation class
 * EnterServletContextListener
 * 
 * Used to initialiase the application on startup and perform
 * cleanup before server terminates
 */
@WebListener
public class EnterServletContextListener implements ServletContextListener {

	/**
	 * @see ServletContextListener#contextInitialized(ServletContextEvent)
	 */
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		SessionStore.getInstance().initialise();
		MembershipViewStore.getInstance().initialise();
		
		// Start RPCServer as a daemon thread
		RPCServer server = new RPCServer();
		Thread serverThread = new Thread(server);
		serverThread.start();
		
		System.out.println("Done initialising. Server is UP");
	}

	/**
	 * @see ServletContextListener#contextDestroyed(ServletContextEvent)
	 */
	public void contextDestroyed(ServletContextEvent arg0) {
		SessionStore.getInstance().destroy();
		MembershipViewStore.getInstance().destroy();
		
		System.out.println("Server is DOWN");
	}
}
