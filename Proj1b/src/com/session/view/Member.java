package com.session.view;

import java.io.Serializable;

/**
 * This class represents a server node in the network of servers which handle
 * client requests
 * 
 * @author karthik
 */
public class Member implements Serializable {
	private static final long serialVersionUID = 3518952288127436169L;

	private final String serverID;
	private Status status;
	private long lastSeenTimeInMillis;

	public enum Status {
		UP, DOWN
	}

	public Member(String serverID) {
		super();
		this.serverID = serverID;
		this.status = Status.UP;
		this.lastSeenTimeInMillis = System.currentTimeMillis();
	}

	public Member(String serverID, Status status, long lastSeenTimeInMillis) {
		super();
		this.serverID = serverID;
		this.status = status;
		this.lastSeenTimeInMillis = lastSeenTimeInMillis;
	}

	public void setLastSeenTimeInMillis(long lastSeenTimeInMillis) {
		this.lastSeenTimeInMillis = lastSeenTimeInMillis;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public long getLastSeenTimeInMillis() {
		return lastSeenTimeInMillis;
	}

	public void updateLastSeenTime() {
		this.lastSeenTimeInMillis = System.currentTimeMillis();
	}

	public String getServerID() {
		return serverID;
	}

	@Override
	public String toString() {
		return "Member [serverID=" + serverID + ", status=" + status + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((serverID == null) ? 0 : serverID.hashCode());
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
		Member other = (Member) obj;
		if (serverID == null) {
			if (other.serverID != null)
				return false;
		} else if (!serverID.equals(other.serverID))
			return false;
		return true;
	}
}
