package com.session.common;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import com.session.view.Member;

/**
 * This class is the MVC style "Model" which is responsible for holding session state information
 * 1 object per session of a client
 * 
 * @author karthik
 */
public class SessionState implements Serializable {
	private static final long serialVersionUID = -192594183352081471L;

	private final String sessionID;
	
	private Member primary;
	private List<Member> backups;
	
	private AtomicLong version;
	private String message;
	private long expiryTime;
	
	public SessionState() {
		// generate unique session id for each client first time
		this.sessionID = UUID.randomUUID().toString();
		
		this.primary = new Member(Constants.HOST_IP);
		
		this.version = new AtomicLong(0);
		
		// default message
		this.message = "Hello User";
				
		// expiry time set to 1 min from now
		updateExpiryTime();
	}
	
	public Member getPrimary() {
		return primary;
	}

	public void setPrimary(Member primary) {
		this.primary = primary;
	}

	public List<Member> getBackups() {
		return backups;
	}

	public void setBackups(List<Member> backups) {
		this.backups = backups;
	}

	/**
	 * This is the value of the session cookie that is returned to the client
	 * every time along with the response.
	 * The cookie value is restricted to a maximum size of 512 bytes using truncation if necessary  
	 * Note that message is not part of cookie value 
	 */
	public String getCookieValue() {
		List<String> values = new ArrayList<>();
		values.add(sessionID);
		values.add(version.toString());
		values.add(primary.getServerID());
		
		for(Member backup : backups)
			values.add(backup.getServerID());
		
		String cookieValue = join(values);
		cookieValue = cookieValue.substring(0, Math.min(cookieValue.length(), Constants.MAX_UDP_SIZE));
		
		return cookieValue;
	}
	
	String join(List<String> words) {
		StringBuilder sb = new StringBuilder();
		
		for(String word : words) {
			sb.append(word).append("_");
		}
		
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}
	
	public long getExpiryTime() {
		return expiryTime;
	}
	
	public String getExpiryTimeDisplay() {
		return new Date(expiryTime).toString();
	}
	
	public void updateExpiryTime() {
		this.expiryTime = System.currentTimeMillis() + Constants.SESSION_TIMEOUT_MILLIS;
	}
	
	public long getDiscardTime() {
		return expiryTime + + Constants.SESSION_TIMEOUT_EXTRA_MILLIS;
	}
	
	public String getDiscardTimeDisplay() {
		return new Date(getDiscardTime()).toString();
	}
	public String getSessionID() {
		return sessionID;
	}

	public AtomicLong getVersion() {
		return version;
	}

	public void incrementVersion() {
		version.incrementAndGet();
	}
	
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	
	@Override
	public String toString() {
		return "SessionState [sessionID=" + sessionID + ", primary=" + primary + ", backups=" + backups + ", version=" + version + ", message=" + message
				+ ", expiryTime=" + expiryTime + ", discardTime=" + getDiscardTime() + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sessionID == null) ? 0 : sessionID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SessionState other = (SessionState) obj;
		if (sessionID == null) {
			if (other.sessionID != null)
				return false;
		} else if (!sessionID.equals(other.sessionID))
			return false;
		return true;
	}
}
