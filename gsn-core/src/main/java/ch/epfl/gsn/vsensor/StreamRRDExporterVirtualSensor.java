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
* File: src/ch/epfl/gsn/vsensor/StreamRRDExporterVirtualSensor.java
*
* @author Ali Salehi
* @author Mehdi Riahi
*
*/

package ch.epfl.gsn.vsensor;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import java.util.Map.Entry;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import ch.epfl.gsn.beans.StreamElement;
import ch.epfl.gsn.beans.VSensorConfig;



public class StreamRRDExporterVirtualSensor extends AbstractVirtualSensor {
	public static final String PARAM_RRDFILE = "rrdfile";

	public static final String PARAM_FIELD = "field";

	private static final transient Logger logger = LoggerFactory.getLogger(StreamRRDExporterVirtualSensor.class);

	private String rrdfile = null;

	private Vector<String> fields = new Vector<String>();

	/**
	 * Initializes the virtual sensor by setting its configuration parameters from
	 * the Virtual
	 * Sensor Configuration and performing necessary checks and operations.
	 *
	 * @return True if the initialization is successful, false otherwise.
	 */
	public boolean initialize() {
		VSensorConfig vsensor = getVirtualSensorConfiguration();
		TreeMap<String, String> params = vsensor.getMainClassInitialParams();
		Set<Entry<String, String>> entrySet = params.entrySet();
		Iterator it = entrySet.iterator();
		while (it.hasNext()) {
			Entry entry = (Entry) it.next();
			String key = (String) entry.getKey();
			String value = (String) entry.getValue();
			if (key.equals(PARAM_RRDFILE)) {
				this.rrdfile = value;
			} else if (key.equals(PARAM_FIELD)) {
				this.fields.add(value);
			}
		}
		if (rrdfile == null) {
			if(logger.isDebugEnabled()){
				logger.debug("Initialization Parameter " + PARAM_RRDFILE + " is missing!");
			}
			return false;
		}
		if(logger.isDebugEnabled()){
			logger.debug("rrdfile=" + this.rrdfile);
		}
		if (ensureFileExistence(rrdfile)) {
			return true;
		} else {
			return createRRDFile();
		}
	}

	/**
	 * Creates an RRD (Round Robin Database) file using the rrdtool command-line
	 * tool.
	 *
	 * @return True if the RRD file creation is successful, false otherwise.
	 */
	private boolean createRRDFile() {
		String command = "rrdtool create " + rrdfile + " --step 300 ";
		for (int i = 0; i < this.fields.size(); i++) {
			command = command + "DS:field" + i + ":GAUGE:600:0:U ";
		}
		command = command + "RRA:AVERAGE:0.5:1:600 ";
		command = command + "RRA:AVERAGE:0.5:6:700 ";
		command = command + "RRA:AVERAGE:0.5:24:775 ";
		command = command + "RRA:AVERAGE:0.5:288:797 ";
		command = command + "RRA:MAX:0.5:1:600 ";
		command = command + "RRA:MAX:0.5:6:700 ";
		command = command + "RRA:MAX:0.5:24:775";
		command = command + "RRA:MAX:0.5:288:797";
		Runtime runtime = Runtime.getRuntime();
		try {
			if(logger.isDebugEnabled()){
				logger.debug("The used rrdtool create command is: " + command);
			}
			Process process = runtime.exec(command);
			if(logger.isDebugEnabled()){
				logger.debug("The exit value of the rrdtool create command is: " +
					process.exitValue());
			}
			return true;
		} catch (IOException e) {
			if(logger.isDebugEnabled()){
				logger.debug("An IOException has occured: " + e);
			}
			return false;
		}
	}

	/**
	 * Called when new data is available in the input stream.
	 * Ensures the existence of the RRD file and exports the values from the stream
	 * element.
	 *
	 * @param inputStreamName The name of the input stream.
	 * @param streamElement   The data element from the stream.
	 */
	public void dataAvailable(String inputStreamName, StreamElement streamElement) {
		ensureFileExistence();
		exportValues(streamElement);
	}

	/**
	 * Ensures the existence of the RRD file by checking if it exists.
	 *
	 * @return True if the RRD file exists, false otherwise.
	 */
	private boolean ensureFileExistence() {
		return ensureFileExistence(this.rrdfile);
	}

	/**
	 * Ensures the existence of the requested file by checking if it exists.
	 *
	 * @param filename The name of the file to check for existence.
	 * @return True if the file exists, false otherwise.
	 */
	private boolean ensureFileExistence(String filename) {
		File file = new File(rrdfile);
		if (file.exists()) {
			return true;
		} else {
			logger.error("rrdfile " + rrdfile + " does not exist!");
			return false;
		}
	}

	/**
	 * Exports values from a StreamElement to the proposed table name into
	 * the database selected by the currently open connection.
	 *
	 * @param streamElement The StreamElement object containing the data to be
	 *                      exported.
	 */
	private void exportValues(StreamElement streamElement) {
		if(logger.isDebugEnabled()){
			logger.debug("Trying to add new data items to the rrdfile:" + this.rrdfile);
		}
		String command = "rrdtool update " + rrdfile + " N";
		Serializable[] stream = streamElement.getData();
		String field;
		for (int i = 0; i < stream.length; i++) {
			field = stream[i].toString();
			// if the field is empty we have to add an U for unknown to the rrdfile
			if (field == null || field.equals("")) {
				field = "U";
			}
			command = command + ":" + field;
		}
		Runtime runtime = Runtime.getRuntime();
		try {
			if(logger.isDebugEnabled()){
				logger.debug("The used rrdtool update command is: " + command);
			}
			runtime.exec(command);
			if(logger.isDebugEnabled()){
				logger.debug("The processing did not generate an error!");
			}
		} catch (IOException e) {
			if(logger.isDebugEnabled()){
				logger.debug("An IOException has occured: " + e);
			}
		}
	}

	public void dispose() {
	}
}
