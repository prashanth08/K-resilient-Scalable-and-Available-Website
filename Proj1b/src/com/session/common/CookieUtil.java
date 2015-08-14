package com.session.common;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author karthik
 * 
 */
public class CookieUtil {
	
	/**
	 * Extract the session cookie from the HTTP Request
	 * @param request
	 * @return
	 */
	public static Cookie extractSessionCookie(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();

		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(Constants.SESSION_COOKIE_NAME)) {
					return cookie;
				}
			}
		}

		return null;
	}
	
	/**
	 * Update the value and maxAge of the session cookie in the HTTP response
	 * @param request
	 * @param response
	 * @param newValue
	 * @param maxAge
	 */
	public static void updateCookie(HttpServletRequest request, HttpServletResponse response, String newValue, int maxAge) {
		Cookie[] cookies = request.getCookies();

		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(Constants.SESSION_COOKIE_NAME)) {
					cookie.setValue(newValue);
					cookie.setMaxAge(maxAge);
					response.addCookie(cookie);
				}
			}
		}
	}

	/**
	 * Exract Session ID from the cookie
	 * @param cookie
	 * @return
	 */
	public static String extractSessionID(Cookie cookie) {
		String value = cookie.getValue();

		if (value != null) {
			String[] tokens = value.split("_");
			return tokens[0];
		}

		return null;
	}
	
	/**
	 * Extract version from the cookie
	 * @param cookie
	 * @return
	 */
	public static long extractVersion(Cookie cookie) {
		String value = cookie.getValue();

		if (value != null) {
			String[] tokens = value.split("_");
			return Long.parseLong(tokens[1]);
		}

		return 0;
	}
	
	/**
	 * Extract primary server from the cookie
	 * @param cookie
	 * @return
	 */
	public static String extractPrimary(Cookie cookie) {
		String value = cookie.getValue();

		if (value != null) {
			String[] tokens = value.split("_");
			return tokens[2];
		}

		return null;
	}

	/**
	 * Extract the list of backup servers from the cookie
	 * @param cookie
	 * @return
	 */
	public static List<String> extractBackups(Cookie cookie) {
		String value = cookie.getValue();

		if (value != null) {
			String[] tokens = value.split("_");
			return Arrays.asList(Arrays.copyOfRange(tokens, 3, tokens.length));
		}

		return null;
	}
}
