package com.session.common;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Responsible for caching and managing Session State information 
 * @author karthik
 */
public class SessionStore {
	// Stores session information pertaining to each client keyed by a unique Session ID
	private final Map<String, SessionState> sessionMap = new ConcurrentHashMap<>();
	
	// Timed thread to periodically cleanup expired sessions from the sessionMap
	private final ScheduledExecutorService cleanupExecutorService = Executors.newSingleThreadScheduledExecutor();
	
	private static SessionStore INSTANCE = null;
	
	private SessionStore() {}
	
	public static SessionStore getInstance() {
		if(INSTANCE == null) {
			INSTANCE = new SessionStore();
		}
		
		return INSTANCE;
	}
	
	/**
	 * Set up a timed thread which will run periodically in the background.
	 * Responsible for removing expired session data from the session cache. We
	 * only track "live" sessions
	 */
	public void initialise() {
		int initialDelay = Constants.SESSION_TIMEOUT_SECS;
		int interval = Constants.SESSION_TIMEOUT_SECS;

		cleanupExecutorService.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				Iterator<Entry<String, SessionState>> it = sessionMap.entrySet().iterator();

				while (it.hasNext()) {
					Entry<String, SessionState> entry = it.next();
					if (System.currentTimeMillis() >= entry.getValue().getDiscardTime()) {
						System.out.println("Removing expired session with ID = " + entry.getKey());
						it.remove();
					}
				}

				System.out.println("Done removing expired sessions from cache");
			}
		}, initialDelay, interval, TimeUnit.SECONDS);
	}

	public void add(SessionState sessionState) {
		sessionMap.put(sessionState.getSessionID(), sessionState);
	}
	
	public SessionState get(String sessionID) {
		return sessionMap.get(sessionID);
	}
	
	public SessionState remove(String sessionID) {
		return sessionMap.remove(sessionID);
	}
	
	public void destroy() {
		cleanupExecutorService.shutdown();
	}
}
