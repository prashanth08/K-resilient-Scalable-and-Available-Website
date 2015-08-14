<%@ page language="java" session="false" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.*, com.session.view.*, com.session.common.*" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Enter Servlet</title>
</head>
<body>
<h2>
${sessionState.message}
</h2>

<form action="EnterServlet">
	<input type="submit" name="clicked" value="Replace" />
	<input type="text" name="message" size="100"/>	<br /><br />
	<input type="submit" name="clicked" value="Refresh" /> <br /><br />
	<input type="submit" name="clicked" value="Logout" />
</form>

<br />
<hr />
<b>Session Cookie Value: </b>${sessionState.cookieValue}
<br />
<b>This Request was handled by: </b><%= Constants.HOST_IP %>
<br />
<b>Session Data was found on: </b>${selectedReadMember}
<br />
<b>New Primary: </b>${sessionState.primary}
<br />
<b>New Backups: </b>${sessionState.backups}
<br />
<b>Session Expiry Time: </b>${sessionState.expiryTimeDisplay}
<br />
<b>Session Discard Time: </b>${sessionState.discardTimeDisplay}
<br /><br />
<b>Membership View of server: </b>
<%
List<Member> members = (List<Member>) request.getAttribute("membershipView");
out.write("<table border=\"1\">");
out.write("<th>Server ID</th>");
out.write("<th>Status</th>");
out.write("<th>Last Seen At</th>");

for(Member member : members) {
	String color = member.getStatus().name().equals("UP") ? "#98FF98" : "#F9966B";
	out.write("<tr bgcolor="+color+">");
	out.write("<td>"+member.getServerID()+"</td>");
	out.write("<td>"+member.getStatus()+"</td>");
	out.write("<td>"+new Date(member.getLastSeenTimeInMillis())+"</td>");
	out.write("</tr>");
}
out.write("</table>");
%>
<hr />
</body>
</html>