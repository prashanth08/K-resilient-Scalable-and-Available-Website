package com.session.rpc;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Map;

import com.session.common.Constants;
import com.session.common.SessionState;

public class RPCClient {
	private RPCClient() {}

	private static RPCClient INSTANCE;

	public static synchronized RPCClient getInstance() {
		if (INSTANCE == null)
			INSTANCE = new RPCClient();

		return INSTANCE;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map exchangeViews(String backupServer, Map view) {
		DatagramSocket clientSocket = null;
		DatagramPacket request = null, response = null;

		ByteArrayOutputStream byteOutputStream = null;
		ObjectOutputStream outputStream = null;

		ByteArrayInputStream byteInputStream = null;
		ObjectInputStream inputStream = null;

		byte[] buffer = null;
		NetworkObject<Map> result = null;

		try {
			clientSocket = new DatagramSocket();
			clientSocket.setSoTimeout(Constants.RPC_TIMEOUT_MILLIS);
			
			byteOutputStream = new ByteArrayOutputStream();
			outputStream = new ObjectOutputStream(byteOutputStream);

			NetworkObject<Map> networkObject = new NetworkObject<Map>("VIEW", view);
			outputStream.writeObject(networkObject);
			outputStream.flush();

			buffer = byteOutputStream.toByteArray();

			request = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(backupServer), Constants.RPC_PORT);

			clientSocket.send(request);

			byte[] rbuffer = new byte[65535];

			response = new DatagramPacket(rbuffer, rbuffer.length);
			clientSocket.receive(response);

			rbuffer = response.getData();

			byteInputStream = new ByteArrayInputStream(rbuffer);
			inputStream = new ObjectInputStream(byteInputStream);

			result = (NetworkObject<Map>) inputStream.readObject();
			if(!result.callID.equals(networkObject.callID))
				return null;

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			clientSocket.close();
		}

		return result == null ? null : result.getObject();
	}

	@SuppressWarnings("unchecked")
	public SessionState readSession(String backupServer, String sessionID) {
		DatagramSocket clientSocket = null;
		DatagramPacket request = null, response = null;

		ByteArrayOutputStream byteOutputStream = null;
		ObjectOutputStream outputStream = null;

		ByteArrayInputStream byteInputStream = null;
		ObjectInputStream inputStream = null;

		byte[] buffer = null;
		NetworkObject<SessionState> result = null;

		try {
			clientSocket = new DatagramSocket();
			clientSocket.setSoTimeout(Constants.RPC_TIMEOUT_MILLIS);
			
			byteOutputStream = new ByteArrayOutputStream();
			outputStream = new ObjectOutputStream(byteOutputStream);

			NetworkObject<String> networkObject = new NetworkObject<>("READ", sessionID);
			outputStream.writeObject(networkObject);
			outputStream.flush();

			buffer = byteOutputStream.toByteArray();

			request = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(backupServer), Constants.RPC_PORT);

			clientSocket.send(request);

			byte[] rbuffer = new byte[65535];

			response = new DatagramPacket(rbuffer, rbuffer.length);
			clientSocket.receive(response);

			rbuffer = response.getData();

			byteInputStream = new ByteArrayInputStream(rbuffer);
			inputStream = new ObjectInputStream(byteInputStream);

			result = (NetworkObject<SessionState>) inputStream.readObject();
			if(!result.callID.equals(networkObject.callID))
				return null;
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			clientSocket.close();
		}

		return result == null ? null : result.getObject();
	}

	@SuppressWarnings("unchecked")
	public boolean writeSession(String backupServer, SessionState SessionState) {
		DatagramSocket clientSocket = null;
		DatagramPacket request = null, response = null;

		ByteArrayOutputStream byteOutputStream = null;
		ObjectOutputStream outputStream = null;

		ByteArrayInputStream byteInputStream = null;
		ObjectInputStream inputStream = null;

		byte[] buffer = null;
		NetworkObject<String> result = null;

		try {
			clientSocket = new DatagramSocket();
			clientSocket.setSoTimeout(Constants.RPC_TIMEOUT_MILLIS);
			
			byteOutputStream = new ByteArrayOutputStream();
			outputStream = new ObjectOutputStream(byteOutputStream);

			NetworkObject<SessionState> networkObject = new NetworkObject<SessionState>("WRITE", SessionState);
			outputStream.writeObject(networkObject);
			outputStream.flush();

			buffer = byteOutputStream.toByteArray();

			request = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(backupServer), Constants.RPC_PORT);

			clientSocket.send(request);

			byte[] rbuffer = new byte[65535];

			response = new DatagramPacket(rbuffer, rbuffer.length);
			clientSocket.receive(response);

			rbuffer = response.getData();

			byteInputStream = new ByteArrayInputStream(rbuffer);
			inputStream = new ObjectInputStream(byteInputStream);

			result = (NetworkObject<String>) inputStream.readObject();
			if(!result.callID.equals(networkObject.callID))
				return false;

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			clientSocket.close();
		}
		
		return result == null ? false : result.getObject().equals("OK"); 
	}
}
