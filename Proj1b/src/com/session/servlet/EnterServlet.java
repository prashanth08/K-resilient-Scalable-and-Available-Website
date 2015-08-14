package com.session.servlet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.session.common.Constants;
import com.session.common.CookieUtil;
import com.session.common.SessionState;
import com.session.common.SessionStore;
import com.session.rpc.RPCClient;
import com.session.view.Member;
import com.session.view.MembershipViewStore;

/**
 * This servlet is the MVC style "Controller" which receives client requests,
 * performs actions and sends back responses
 */
@WebServlet("/EnterServlet")
public class EnterServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	/**
	 * Make RPC calls to read session data remotely from other servers in the membership view
	 * 
	 * @param request
	 * @param sessionID
	 * @param primary
	 * @param backups
	 * @return
	 */
	private SessionState sessionRead(HttpServletRequest request, String sessionID, String primary, List<String> backups) {
		String selectedMember = "Old Primary["+primary+"]";
		SessionState sessionState = RPCClient.getInstance().readSession(primary, sessionID);
		
		if(sessionState == null) {
			// primary must be down, update membership view
			MembershipViewStore.getInstance().update(primary, Member.Status.DOWN);
			
			for(String backup : backups) {
				// try hitting the backup
				sessionState = RPCClient.getInstance().readSession(backup, sessionID);
				
				if(sessionState != null) {
					selectedMember = "Old Backup["+backup+"]";
					MembershipViewStore.getInstance().update(backup, Member.Status.UP);
					break;
				}
				else {
					// backup must also be down, update membership view
					MembershipViewStore.getInstance().update(backup, Member.Status.DOWN);
				}
			}
		}
		else {
			MembershipViewStore.getInstance().update(primary, Member.Status.UP);
		}
		
		request.setAttribute("selectedReadMember", selectedMember);
		return sessionState;
	}
	
	/**
	 * Make RPC calls to write session data 'K' backup servers in the membership view 
	 * 
	 * @param sessionState
	 * @return
	 */
	private List<Member> sessionWrite(SessionState sessionState) {
		List<Member> allMembers = MembershipViewStore.getInstance().getAllActive();
		List<Member> backups = new ArrayList<>(Constants.K);
		int index = 1;
		
		if(allMembers != null) {
			for(Member member : allMembers) {
				String serverID = member.getServerID();
				boolean status = RPCClient.getInstance().writeSession(serverID, sessionState);
				
				if(status) {
					System.out.println("SessionWrite to Backup: "+member+" completed successfully");
					MembershipViewStore.getInstance().update(member.getServerID(), Member.Status.UP);
					backups.add(member);
					
					if(index == Constants.K)
						return backups;
				}
				else {
					MembershipViewStore.getInstance().update(member.getServerID(), Member.Status.DOWN);
				}
				
				index++;
			}
		}
		
		// no backup found; return null
		if(backups.isEmpty())
			backups.add(Constants.NULL_MEMBER);
		
		return backups;
	}
	
	/**
	 * Create new server-side session state on primary and K backups 
	 * Create and return cookie back to client
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	private SessionState createNewSession(HttpServletRequest request, HttpServletResponse response) {
		// create and track session on server-side
		SessionState sessionState = new SessionState();
		
		// write to 'K' backups
		List<Member> backups = sessionWrite(sessionState);
		sessionState.setBackups(backups);
		
		SessionStore.getInstance().add(sessionState);

		// send back cookie to client in the response
		Cookie cookie = new Cookie(Constants.SESSION_COOKIE_NAME, sessionState.getCookieValue());
		cookie.setMaxAge(Constants.SESSION_TIMEOUT_SECS);
		response.addCookie(cookie);
		
		// used for debugging
		request.setAttribute("selectedReadMember", "New Primary["+Constants.HOST_IP+"]");
		
		return sessionState;
	}
	
	/**
	 * Lookup the required SessionState on primary or backups
	 * @param request
	 * @param sessionID
	 * @param primary
	 * @param backups
	 * @return
	 */
	private SessionState getSessionState(HttpServletRequest request, String sessionID, String primary, List<String> backups) {
		SessionState sessionState = null;
		String thisHost = Constants.HOST_IP;

		if (thisHost.equals(primary)) {
			sessionState = SessionStore.getInstance().get(sessionID);
			request.setAttribute("selectedReadMember", "Old Primary["+thisHost+"]");
		}
		else if(backups.contains(thisHost)) {
			sessionState = SessionStore.getInstance().get(sessionID);
			request.setAttribute("selectedReadMember", "Old Backup["+thisHost+"]");
		}
		else {
			sessionState = sessionRead(request, sessionID, primary, backups);
			if(sessionState != null)
				SessionStore.getInstance().add(sessionState);
		}
		
		return sessionState;
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Cookie cookie = CookieUtil.extractSessionCookie(request);
		SessionState sessionState = null;
		
		if (cookie == null) {
			sessionState = createNewSession(request, response);
		} 
		else {
			String sessionID = CookieUtil.extractSessionID(cookie);
			String primary = CookieUtil.extractPrimary(cookie);
			List<String> backups = CookieUtil.extractBackups(cookie);
			
			String clicked = request.getParameter("clicked");
			
			// logout - remove state from session table and cookie
			if(clicked != null && clicked.equals("Logout")) {
				if (Constants.HOST_IP.equals(primary) || backups.contains(Constants.HOST_IP)) {
					sessionState = SessionStore.getInstance().remove(sessionID);
				}
				
				CookieUtil.updateCookie(request, response, null, 0);
				response.getWriter().write("Logged Out");
				return;
			}
			
			sessionState = getSessionState(request, sessionID, primary, backups);
			
			// primary and all backups are down; display error to user 
			if(sessionState == null) {
				response.getWriter().write("Session timed out.");
				CookieUtil.updateCookie(request, response, null, 0);
				return;
			}
			
			sessionState.incrementVersion();
			sessionState.updateExpiryTime();
			// set new primary
			sessionState.setPrimary(Constants.SELF);
			
			String message = request.getParameter("message");
			if(clicked != null && clicked.equals("Replace") && message != null)
				sessionState.setMessage(message);
			
			// write session state to 'K' backups
			List<Member> newBackups = sessionWrite(sessionState);
			sessionState.setBackups(newBackups);
		}
		
		// we need to update the cookie value & max age
		CookieUtil.updateCookie(request, response, sessionState.getCookieValue(), Constants.SESSION_TIMEOUT_SECS);
		
		// these details will be displayed on the view for debugging purposes
		request.setAttribute("sessionState", sessionState);
		request.setAttribute("membershipView", MembershipViewStore.getInstance().getAll());

		RequestDispatcher dispatcher = request.getRequestDispatcher("Enter.jsp");
		dispatcher.forward(request, response);
	}
}
