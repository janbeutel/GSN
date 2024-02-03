/**
* Global Sensor Networks (GSN) Source Code
* Copyright (c) 2006-2016, Ecole Polytechnique Federale de Lausanne (EPFL)
* 
* This file is part of GSN.
* 
* GSN is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* GSN is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with GSN.  If not, see <http://www.gnu.org/licenses/>.
* 
* File: src/ch/epfl/gsn/utils/ValidityTools.java
*
* @author Jerome Rousselot
* @author gsn_devs
* @author Ali Salehi
* @author Timotee Maret
*
*/

package ch.epfl.gsn.utils;

import java.io.File;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.LoggerFactory;

import org.slf4j.Logger;

public class ValidityTools {

	public static final int SMTP_PORT = 25;

	static Pattern hostAndPortPattern = Pattern.compile("(.+):(\\d+)$");

	public static final transient Logger logger = LoggerFactory.getLogger(ValidityTools.class);
	public static final ArrayList<InetAddress> NETWORK_LOCAL_INETADDRESSES = new ArrayList<InetAddress>();
	/**
	 * Checks to see if the specified address is accessible. 3sec is used as the
	 * default
	 * timeout period.
	 * 
	 * @param host The host to connect to.
	 * @param port the port to connect to (e.g., host:port).
	 */
	public static boolean isAccessibleSocket(String host, int port) throws UnknownHostException {
		return isAccessibleSocket(host, port, 3000);
	}

	/**
	 * Checks if a socket connection can be established to the specified host and
	 * port within the given timeout.
	 *
	 * @param host          the host to connect to
	 * @param port          the port to connect to
	 * @param timeOutInMSec the timeout in milliseconds
	 * @return true if the socket connection is accessible, false otherwise
	 * @throws UnknownHostException if the host is unknown
	 * @throws RuntimeException     if the specified parameters are not valid
	 */
	public static boolean isAccessibleSocket(String host, int port, int timeOutInMSec)
			throws UnknownHostException, RuntimeException {
		Socket socket = null;
		boolean toReturn = false;
		try {
			if (port <= 0 || port > 65535 || host == null || host.trim().length() == 0) {
				logger.info("Bad parameters for validator tool" + "(port = " + port + ", host=" + (host == null));
				throw new RuntimeException("The specified parameters are not valid");
			}
			socket = new Socket();
			InetSocketAddress inetSocketAddress = new InetSocketAddress(host, port);
			socket.connect(inetSocketAddress, timeOutInMSec);
			toReturn = true;
		} catch (ConnectException e) {
			if(logger.isDebugEnabled()){
				logger.debug(e.getMessage(), e);
			}
		} catch (Exception e) {
			logger.info(e.getMessage(), e);
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (Exception e) {
					if(logger.isDebugEnabled()){
						logger.debug(e.getMessage(), e);
					}
				}
			}
		}
		return toReturn;
	}

	/**
	 * Checks the accessibility of directories.
	 * 
	 * @param args The names of the directories to check.
	 */
	public static void checkAccessibilityOfDirs(String... args) {
		for (String name : args) {
			File f = new File(name);
			if (f.canRead() && f.canWrite() && f.isDirectory()) {
				continue;
			} else {
				logger.error("The required directory : " + f.getAbsolutePath() + " is not accessible.");
				System.exit(1);
			}
		}
	}

	/**
	 * Checks the accessibility of files.
	 *
	 * @param args The names of the files to check.
	 */
	public static void checkAccessibilityOfFiles(String... args) {
		for (String name : args) {
			File f = new File(name);
			if (f.canRead() && f.canWrite() && f.isFile()) {
				continue;
			} else {
				logger.error("The required file : " + f.getAbsolutePath() + " is not accessible.");
				System.exit(1);
			}
		}
	}

	/**
	 * Checks if the database is accessible by establishing a connection using the
	 * provided driver class, URL, username, and password.
	 * 
	 * @param driverClass the fully qualified name of the JDBC driver class
	 * @param url         the URL of the database
	 * @param user        the username for the database connection
	 * @param password    the password for the database connection
	 * @throws ClassNotFoundException if the driver class cannot be found
	 * @throws SQLException           if there is an error while establishing the
	 *                                database connection
	 */
	public static void isDBAccessible(String driverClass, String url, String user, String password)
			throws ClassNotFoundException, SQLException {
		Class.forName(driverClass);
		Connection con = DriverManager.getConnection(url, user, password);
		con.close();
	}

	/*
	 * Returns the hostname part of a host:port String. This method is ipv6
	 * compatible. @param hostandport A string containing a host and a port
	 * number, separated by a ":" @return host A string with the host name part
	 * (either name or ip address) of the input string.
	 */
	public static String getHostName(String hostandport) {
		String hostname = "";
		try {
			Matcher m = hostAndPortPattern.matcher(hostandport);
			m.matches();
			hostname = m.group(1).toLowerCase().trim();
		} catch (Exception e) {
		}
		return hostname;
	}

	/*
	 * Returns the port number of a host:port String. This method is ipv6
	 * compatible.
	 */
	public static int getPortNumber(String hostandport) {
		int port = -1;
		try {
			Matcher m = hostAndPortPattern.matcher(hostandport);
			m.matches();
			port = Integer.parseInt(m.group(2).toLowerCase().trim());
		} catch (Exception e) {
		}
		return port;
	}

	/**
	 * Checks if the given host is the localhost.
	 *
	 * @param host the host to check
	 * @return true if the host is the localhost, false otherwise
	 */
	public static boolean isLocalhost(String host) {
		// this allows us to be ipv6 compatible (we simply remove the port)
		try {
			InetAddress hostAddress = InetAddress.getByName(host);
			if (hostAddress == null) {
				return false;
			}
			for (InetAddress address : NETWORK_LOCAL_INETADDRESSES) {
				if (address.equals(hostAddress)) {
					return true;
				}
			}
			return hostAddress.isLoopbackAddress();
		} catch (UnknownHostException e) {
			if(logger.isDebugEnabled()){
				logger.debug(e.getMessage());
			}
			return false;
		}
	}

	// public static final ArrayList < String > NETWORK_LOCAL_ADDRESS = new
	// ArrayList < String >( );

	
	static {
		try {
			Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
			for (NetworkInterface netint : Collections.list(nets)) {
				Enumeration<InetAddress> address = netint.getInetAddresses();
				for (InetAddress addr : Collections.list(address)) {
					if (!addr.isMulticastAddress()) {
						// NETWORK_LOCAL_ADDRESS.add( addr.getCanonicalHostName( ) );
						NETWORK_LOCAL_INETADDRESSES.add(addr);
					}
				}
				// NETWORK_LOCAL_ADDRESS.add("localhost");
				// NETWORK_LOCAL_ADDRESS.add("127.0.0.1");

			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * Returns true if the provided string can be converted
	 * correctly to an integer. Otherwise (null or non-numerical
	 * string) returns false
	 * 
	 * @param value
	 * @return
	 */
	public static boolean isInt(String value) {
		try {
			Integer.parseInt(value.trim());
			return true;
		} catch (Exception exception) {
			return false;
		}
	}

	/**
	 * Checks if the given string is a valid Java variable.
	 *
	 * @param string the string to be checked
	 * @return true if the string is a valid Java variable, false otherwise
	 */
	public static boolean isValidJavaVariable(CharSequence string) {
		if (string == null) {
			return false;
		}
		StringBuilder sb = new StringBuilder(string);
		if (sb.length() == 0) {
			return false;
		}
		if (!Character.isJavaIdentifierStart(sb.charAt(0)) && string.charAt(0) != '\"') {
			return false;
		}
		for (int i = 1; i < sb.length(); i++) {
			if (!Character.isJavaIdentifierPart(sb.charAt(i))
					&& (i == sb.length() - 1 && string.charAt(sb.length() - 1) != '\"')) {
				return false;
			}
		}

		return true;
	}

}
