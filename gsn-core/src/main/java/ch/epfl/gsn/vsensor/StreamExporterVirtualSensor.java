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
* File: src/ch/epfl/gsn/vsensor/StreamExporterVirtualSensor.java
*
* @author Jerome Rousselot
* @author Ali Salehi
* @author Mehdi Riahi
* @author Timotee Maret
* @author Ivo Dimitrov
*
*/

package ch.epfl.gsn.vsensor;

import org.slf4j.LoggerFactory;

import ch.epfl.gsn.Main;
import ch.epfl.gsn.beans.StreamElement;
import ch.epfl.gsn.beans.VSensorConfig;
import ch.epfl.gsn.utils.GSNRuntimeException;

import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.TreeMap;

/**
 * This class represents a StreamExporterVirtualSensor, which is a type of AbstractVirtualSensor.
 * This virtual sensor saves its input stream to any JDBC accessible source.
 */
public class StreamExporterVirtualSensor extends AbstractVirtualSensor {

	public static final String PARAM_USER = "user", PARAM_PASSWD = "password", PARAM_URL = "url", TABLE_NAME = "table",
			PARAM_DRIVER = "driver", PARAM_ENTRIES = "entries";

	public static final String[] OBLIGATORY_PARAMS = new String[] { PARAM_USER, PARAM_URL, PARAM_DRIVER };

	private static final transient Logger logger = LoggerFactory.getLogger(StreamExporterVirtualSensor.class);

	private Connection connection;

	private CharSequence table_name;

	private String password;

	private String user;

	private String url;

	private String entries; //
	private long startTime;
	private long estimatedTime;
	private int counter = 0;
	private int limit;

	/**
	 * Initializes the StreamExporterVirtualSensor by retrieving the necessary parameters from the virtual sensor configuration.
	 * It establishes a JDBC connection to the specified database and creates the table if it does not exist.
	 * 
	 * @return true if initialization is successful, false otherwise
	 */
	public boolean initialize() {
		VSensorConfig vsensor = getVirtualSensorConfiguration();
		TreeMap<String, String> params = vsensor.getMainClassInitialParams();

		for (String param : OBLIGATORY_PARAMS) {
			if (params.get(param) == null || params.get(param).trim().length() == 0) {
				logger.warn("Initialization Failed, The " + param + " initialization parameter is missing");
				return false;
			}
		}
		table_name = params.get(TABLE_NAME);
		user = params.get(PARAM_USER);
		password = params.get(PARAM_PASSWD);
		url = params.get(PARAM_URL);
		entries = params.get(PARAM_ENTRIES); //
		limit = Integer.parseInt(entries); //
		estimatedTime = 0;
		try {
			Class.forName(params.get(PARAM_DRIVER));
			connection = getConnection();
			if(logger.isDebugEnabled()){
				logger.debug("jdbc connection established.");
			}
			if (!Main.getStorage(table_name.toString()).tableExists(table_name,
					getVirtualSensorConfiguration().getOutputStructure(), connection)) {
				Main.getStorage(table_name.toString()).executeCreateTable(table_name,
						getVirtualSensorConfiguration().getOutputStructure(), false, connection);
			}
		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage(), e);
			logger.error("Initialization of the Stream Exporter VS failed !");
			return false;
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			logger.error("Initialization of the Stream Exporter VS failed !");
			return false;
		} catch (GSNRuntimeException e) {
			logger.error(e.getMessage(), e);
			logger.error("Initialization failed. There is a table called " + TABLE_NAME
					+ " Inside the database but the structure is not compatible with what GSN expects.");
			return false;
		}
		return true;
	}

	/**
	 * This method is called when data is available in the input stream.
	 * It inserts the stream element into the database table and keeps track of the insertion time.
	 * If the number of inserted elements reaches the limit, it logs the estimated time taken for the insertions.
	 * 
	 * @param inputStreamName the name of the input stream
	 * @param streamElement the stream element to be inserted
	 */
	public void dataAvailable(String inputStreamName, StreamElement streamElement) {
		StringBuilder query = Main.getStorage(table_name.toString()).getStatementInsert(table_name,
				getVirtualSensorConfiguration().getOutputStructure());

		try {
			counter++; //
			startTime = System.nanoTime();
			Main.getStorage(table_name.toString()).executeInsert(table_name,
					getVirtualSensorConfiguration().getOutputStructure(), streamElement, getConnection());
			estimatedTime += (System.nanoTime() - startTime);
			if (counter >= limit) {
				double seconds = (double) estimatedTime / 1000000000.0;
				logger.trace("The estimated time (sec) is = " + seconds);
			}
			if ((counter % 1000) == 0) {
				logger.trace("Up until the Entry = " + counter);
				double seconds = (double) estimatedTime / 1000000000.0;
				logger.trace("The estimated time (sec) is = " + seconds);
			}
		} catch (SQLException e) {
			logger.error("Insertion failed! (" + query + "): " + e.getMessage());
		} finally {
			dataProduced(streamElement);
		}

	}

	/**
	 * Retrieves the JDBC connection to the database.
	 * If the connection is closed or null, a new connection is established.
	 * 
	 * @return the JDBC connection
	 * @throws SQLException if an error occurs while establishing the connection
	 */
	public Connection getConnection() throws SQLException {
		if (this.connection == null || this.connection.isClosed()) {
			this.connection = DriverManager.getConnection(url, user, password);
		}
		return connection;
	}
	
	public void dispose() {
	}
}
