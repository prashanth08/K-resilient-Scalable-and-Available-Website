package com.session.common;
import com.session.view.Member;

/**
 * @author karthik
 * 
 */
public class Constants {
	// Define key of the cookie which stores session information
	public static final String SESSION_COOKIE_NAME = "CS5300PROJ1SESSION";
	public static final int MAX_UDP_SIZE = 512;
	
	// Server-side session state constants
	public static final int SESSION_TIMEOUT_SECS  = 60 * 60;
	public static final int SESSION_TIMEOUT_MILLIS = SESSION_TIMEOUT_SECS * 1000;
	public static final int SESSION_TIMEOUT_EXTRA_MILLIS = 180 * 1000;  
	
	// Gossip constants
	public static final int GOSSIP_INTERVAL_MINS = 1;
	public static final int MAX_RANDOM_GOSSIP_DELAY_MILLIS = 60 * 1000; 
	
	// Simple DB
	public static final String SIMPLE_DB_DOMAIN = "GroupMembershipViewStore";
	public static final String ACCESS_KEY = System.getProperty("AWS_ACCESS_KEY_ID");
	public static final String SECRET_KEY = System.getProperty("AWS_SECRET_KEY");
	
	public static final String HOST_IP = IPUtil.getEc2HostIP();
	public static final Member NULL_MEMBER = new Member("0.0.0.0");
	public static final Member SELF = new Member(HOST_IP);
	
	public static final int RPC_PORT = 5305;
	public static final int RPC_TIMEOUT_MILLIS = 5 * 1000;
	
	// 'k' in k-resilient
	public static final int K = getK();

	private static int getK() {
		String param1 = System.getProperty("PARAM1");
		
		if(param1 != null)
			return Integer.parseInt(param1);
		
		return 1;
	}
}
