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
* File: src/ch/epfl/gsn/vsensor/SensorInternetVS.java
*
* @author Timotee Maret
* @author Ali Salehi
* @author Mehdi Riahi
*
*/

package ch.epfl.gsn.vsensor;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeMap;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import ch.epfl.gsn.beans.StreamElement;

public class SensorInternetVS extends AbstractVirtualSensor {

	private static final String SI_URL = "si-url";
	private URL siUrl = null;

	private static final String SI_USERNAME = "si-username";
	private String siUsername = null;

	private static final String SI_PASSWORD = "si-password";
	private String siPassword = null;

	private static final String SI_STREAM_MAPPING = "si-stream-mapping";
	private Integer[] siStreamMapping = null;

	private static final String REQUEST_AGENT = "GSN (Global Sensors Networks) Virtual Sensor";

	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

	private static transient Logger logger = LoggerFactory.getLogger(SensorInternetVS.class);

	/**
	 * Initializes the SensorInternetVS virtual sensor.
	 * 
	 * Parses the required configuration parameters from the virtual sensor
	 * configuration file and validates them:
	 * - siUrl: The URL to send the data to.
	 * - siUsername: The username for basic HTTP authentication.
	 * - siPassword: The password for basic HTTP authentication.
	 * - siStreamMapping: The mapping of input streams to fields in the request.
	 * 
	 * Also sets up the basic HTTP authentication using the provided credentials.
	 * 
	 * @return true if initialization succeeded, false otherwise.
	 */
	@Override
	public boolean initialize() {
		TreeMap<String, String> params = getVirtualSensorConfiguration().getMainClassInitialParams();
		String param = null;

		param = params.get(SI_URL);
		if (param == null) {
			logger.error(
					"The required parameter: >" + SI_URL + "<+ is missing from the virtual sensor configuration file.");
			return false;
		} else {
			try {
				siUrl = new URL(param);
			} catch (MalformedURLException e) {
				logger.error(e.getMessage(), e);
				return false;
			}
		}

		param = params.get(SI_USERNAME);
		if (param == null) {
			logger.error("The required parameter: >" + SI_USERNAME
					+ "<+ is missing from the virtual sensor configuration file.");
			return false;
		} else {
			siUsername = param;
		}

		param = params.get(SI_PASSWORD);
		if (param == null) {
			logger.error("The required parameter: >" + SI_PASSWORD
					+ "<+ is missing from the virtual sensor configuration file.");
			return false;
		} else {
			siPassword = param;
		}

		param = params.get(SI_STREAM_MAPPING);
		if (param == null) {
			logger.error("The required parameter: >" + SI_STREAM_MAPPING
					+ "<+ is missing from the virtual sensor configuration file.");
			return false;
		} else {
			siStreamMapping = initStreamMapping(param);
			if (siStreamMapping == null) {
				logger.error("Failed to parse the required parameter: >" + SI_STREAM_MAPPING + "< (" + param + ")");
				return false;
			}
		}

		// Enabling Basic authentication
		Authenticator.setDefault(new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(siUsername, siPassword.toCharArray());
			}
		});

		return true;
	}

	/**
	 * Sends the given StreamElement data to the configured remote server.
	 *
	 * This is called when new data is available from the attached input streams.
	 * 
	 * It will encode the StreamElement's fields and values into URL encoded POST
	 * parameters, and send a POST request to the configured remote URL.
	 *
	 * @param inputStreamName The name of the input stream of the data
	 * @param streamElement   The StreamElement containing the new data
	 */
	@Override
	public void dataAvailable(String inputStreamName, StreamElement streamElement) {
		try {

			// Init the HTTP Connection
			HttpURLConnection siConnection = (HttpURLConnection) siUrl.openConnection();
			siConnection.setRequestMethod("POST");
			siConnection.setDoOutput(true);
			siConnection.setRequestProperty("User-Agent", REQUEST_AGENT);
			siConnection.connect();

			// Build and send the parameters
			PrintWriter out = new PrintWriter(siConnection.getOutputStream());
			String postParams = buildParameters(streamElement.getFieldNames(), streamElement.getData(),
					streamElement.getTimeStamp());
			
			if(logger.isDebugEnabled()){
				logger.debug("POST parameters: " + postParams);
			}
			out.print(postParams);
			out.flush();
			out.close();

			if (siConnection.getResponseCode() == 200) {
				if(logger.isDebugEnabled()){
					logger.debug("data successfully sent");
				}
			} else {
				logger.error("Unable to send the data. Check you configuration file. "
						+ siConnection.getResponseMessage() + " Code (" + siConnection.getResponseCode() + ")");
			}
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}

	@Override
	public void dispose() {

	}

	/**
	 * Builds the parameter string to send to the Sensor Internet server based on
	 * the input data.
	 *
	 * @param fieldsNames the names of the data fields
	 * @param data        the array of data values
	 * @param timestamp   the timestamp for the data
	 * @return a string containing the parameters to send to the server, or null if
	 *         error
	 */
	private String buildParameters(String[] fieldsNames, Serializable[] data, long timestamp) {

		StringBuilder sb = new StringBuilder();
		//
		for (int i = 0; i < fieldsNames.length; i++) {
			if (i < siStreamMapping.length) {
				if (i != 0) {
					sb.append("&");
				}
				sb.append(createPostParameter("time[" + i + "]=", dateFormat.format(new Date(timestamp))));
				sb.append("&");
				sb.append(createPostParameter("data[" + i + "]=", data[i].toString()));
				sb.append("&");
				sb.append(createPostParameter("key[" + i + "]=", Integer.toString(siStreamMapping[i])));
			} else {
				logger.warn("The field >" + fieldsNames[i] + "< is not mapped in your configuration file.");
			}
		}
		return sb.toString();
	}

	/**
	 * Creates a URL encoded parameter string for an HTTP POST request.
	 * 
	 * @param paramName  The parameter name.
	 * @param paramValue The parameter value.
	 * @return The URL encoded parameter string, or null if encoding fails.
	 */
	private String createPostParameter(String paramName, String paramValue) {
		try {
			return paramName + URLEncoder.encode(paramValue, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			if(logger.isDebugEnabled()){
				logger.debug(e.getMessage(), e);
			}
		}
		return null;
	}

	/**
	 * Initializes the stream mapping array by parsing a comma-separated parameter
	 * string.
	 * 
	 * The stream mapping array maps the virtual sensor's output fields to keys that
	 * identify streams on the Sensor Internet server. This allows each output field
	 * to be
	 * sent as a separate stream.
	 *
	 * @param param A comma-separated string containing the stream mapping. Each
	 *              element is the
	 *              Sensor Internet stream key for the corresponding output field.
	 * @return An array mapping output fields to Sensor Internet stream keys, or
	 *         null if parsing fails.
	 */
	private Integer[] initStreamMapping(String param) {
		String[] mps = param.split(",");
		Integer[] mapping = new Integer[mps.length];
		try {
			for (int i = 0; i < mps.length; i++) {
				mapping[i] = Integer.parseInt(mps[i]);
			}
		} catch (java.lang.NumberFormatException e) {
			logger.error(e.getMessage());
			return null;
		}
		return mapping;
	}
}
