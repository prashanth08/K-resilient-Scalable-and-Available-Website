package com.session.rpc;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Map;

import com.session.common.Constants;
import com.session.common.SessionState;
import com.session.common.SessionStore;
import com.session.view.Member;
import com.session.view.MembershipViewStore;

/**
 * The RPC Server runs as a daemon thread within the Tomcat container
 * and services requests from RPC Clients. Supports 3 RPC calls - 
 * SessionRead, SessionWrite and ExchangeView
 * 
 * @author karthik
 */
public class RPCServer implements Runnable {

	DatagramSocket serverSocket = null;
	DatagramPacket request = null, response = null;

	ByteArrayOutputStream byteOutputStream = null;
	ObjectOutputStream outputStream = null;

	ByteArrayInputStream byteInputStream = null;
	ObjectInputStream inputStream = null;

	public RPCServer() {
		try {
			serverSocket = new DatagramSocket(Constants.RPC_PORT);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void run() {
		byte[] buffer = null;
		while (true) {
			try {
				buffer = new byte[65535];

				request = new DatagramPacket(buffer, buffer.length);
				serverSocket.receive(request);

				buffer = request.getData();
				byteInputStream = new ByteArrayInputStream(buffer);
				inputStream = new ObjectInputStream(byteInputStream);

				NetworkObject<Object> networkObject = (NetworkObject<Object>) inputStream.readObject();
				
				String clientAddress = request.getAddress().getHostAddress();
				String requestType = networkObject.getRequestType();
				System.out.println("Received request: "+requestType+" from "+clientAddress);

				if (requestType.equals("WRITE")) {
					// update the membership view first
					MembershipViewStore.getInstance().update(clientAddress, Member.Status.UP);
					
					// store session state received from client to local cache
					SessionState sessionState = (SessionState) networkObject.getObject();
					SessionStore.getInstance().add(sessionState);

					byteOutputStream = new ByteArrayOutputStream();
					outputStream = new ObjectOutputStream(byteOutputStream);

					NetworkObject<String> reply = new NetworkObject<String>("RESPONSE", "OK");
					reply.setCallID(networkObject.callID);

					outputStream.writeObject(reply);
					outputStream.flush();

					buffer = byteOutputStream.toByteArray();

					response = new DatagramPacket(buffer, buffer.length, request.getAddress(), request.getPort());

					serverSocket.send(response);
				} 
				else if (requestType.equals("READ")) {
					// update the membership view first
					MembershipViewStore.getInstance().update(clientAddress, Member.Status.UP);
					
					// Read session state from local cache and return it to RPC client
					String sessionID = (String) networkObject.getObject();
					SessionState sessionState = SessionStore.getInstance().get(sessionID);
					
					byteOutputStream = new ByteArrayOutputStream();
					outputStream = new ObjectOutputStream(byteOutputStream);

					NetworkObject<SessionState> reply = new NetworkObject<SessionState>("RESPONSE", sessionState);
					reply.setCallID(networkObject.callID);

					outputStream.writeObject(reply);
					outputStream.flush();

					byte rbuffer[] = byteOutputStream.toByteArray();

					response = new DatagramPacket(rbuffer, rbuffer.length, request.getAddress(), request.getPort());

					serverSocket.send(response);
				} 
				else 
				{
					// update the membership view first
					MembershipViewStore.getInstance().update(clientAddress, Member.Status.UP);
					
					Map inboundViewTable = (Map) networkObject.getObject();
					
					MembershipViewStore.getInstance().mergeViews(inboundViewTable);
					
					Map<String, Member> outboundViewTable = MembershipViewStore.getInstance().getMembershipView();

					byteOutputStream = new ByteArrayOutputStream();
					outputStream = new ObjectOutputStream(byteOutputStream);

					NetworkObject<Map> reply = new NetworkObject<Map>("RESPONSE", outboundViewTable);
					reply.setCallID(networkObject.callID);

					outputStream.writeObject(reply);
					outputStream.flush();

					buffer = byteOutputStream.toByteArray();

					response = new DatagramPacket(buffer, buffer.length, request.getAddress(), request.getPort());

					serverSocket.send(response);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
