package com.session.rpc;
import java.io.Serializable;
import java.util.UUID;

/**
 * Used to encapsulate data sent across the network during RPC calls
 * @author karthik
 */
public class NetworkObject<T> implements Serializable {
	private static final long serialVersionUID = 2L;
	
	// SessionRead, SessionWrite or ExchangeView
	String requestType;
	
	// Used to match request with the correct response
	String callID;
	
	// Data sent as args/returns in the RPC
	T object;
	
	public NetworkObject(String requestType, T session){
		this.requestType = requestType;
		this.object = session;
		this.callID = UUID.randomUUID().toString();
	}

	public String getRequestType() {
		return requestType;
	}

	public T getObject() {
		return object;
	}
	
	public String getCallID() {
		return callID;
	}
	
	public void setCallID(String callID) {
		this.callID = callID;
	}
}
