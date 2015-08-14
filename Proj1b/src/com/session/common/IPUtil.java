package com.session.common;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.UnknownHostException;

public class IPUtil {

	public static String getHostIP() {
		try {
			return Inet4Address.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String getEc2HostIP() {
		String ipv4Address = null;

		try {
			Runtime rt = Runtime.getRuntime();
			Process proc = rt.exec("/opt/aws/bin/ec2-metadata --public-ipv4");
			InputStream stdin = proc.getInputStream();
			InputStreamReader isr = new InputStreamReader(stdin);
			BufferedReader br = new BufferedReader(isr);
			String line = br.readLine();

			if (line != null)
				ipv4Address = line.substring(13);

			proc.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("Retrieved EC2 Host IP Address: " + ipv4Address);
		return ipv4Address;
	}
}
