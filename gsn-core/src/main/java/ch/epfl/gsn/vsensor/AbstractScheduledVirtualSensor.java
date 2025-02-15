/**
* Global Sensor Networks (GSN) Source Code
* Copyright (c) 2006-2016, Ecole Polytechnique Federale de Lausanne (EPFL)
* Copyright (c) 2020-2023, University of Innsbruck
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
* File: src/ch/epfl/gsn/vsensor/AbstractScheduledVirtualSensor.java
*
* @author bgpearn
* @author Davide De Sclavis
* @author Manuel Buchauer
* @author Jan Beutel
*
*/

package ch.epfl.gsn.vsensor;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TreeMap;

import org.apache.commons.lang.time.DateUtils;
import org.slf4j.LoggerFactory;

import ch.epfl.gsn.beans.DataField;
import ch.epfl.gsn.beans.DataTypes;
import ch.epfl.gsn.beans.StreamElement;
import ch.epfl.gsn.utils.Helpers;

import org.slf4j.Logger;
import org.joda.time.format.ISODateTimeFormat;

import java.util.TimerTask;

/**
 * Extends AbstractVirtualSensor to allow scheduled output.
 * Parameters:
 * rate = interval between data output
 * start-time = scheduled time to start output (defaults to the next whole time
 * interval)
 * 
 * @author bgpearn
 *
 */

public abstract class AbstractScheduledVirtualSensor extends AbstractVirtualSensor {

	private static final String RATE_PARAM = "rate";
	private static final String START_PARAM = "start-time";
	protected int clock_rate; // Scheduled interval in milliseconds
	protected long startTime; // Schedule start time
	private final String CURRENT_TIME = ISODateTimeFormat.dateTime().print(System.currentTimeMillis());

	protected StreamElement dataItem; // Buffer for most recent stream element
	protected static final transient Logger logger = LoggerFactory.getLogger(AbstractScheduledVirtualSensor.class);
	protected Timer timer0;

	/**
	 * Called once while initializing an instance of the virtual sensor
	 * Gets schedule parameters from VSD, calculates timer parameters and
	 * instantiates timer instance.
	 * 
	 * @return True if the initialization is done successfully.
	 */
	public boolean initialize() {
		TreeMap<String, String> params = getVirtualSensorConfiguration()
				.getMainClassInitialParams();
		String rate_value = params.get(RATE_PARAM);

		if (rate_value == null) {
			logger.warn("Parameter \"" + RATE_PARAM
					+ "\" not provider in Virtual Sensor file");
			return false;
		}

		clock_rate = Integer.parseInt(rate_value);

		String start_value = params.get(START_PARAM);

		if (start_value != null) { // start value set in VSD
			try {
				startTime = Helpers.convertTimeFromIsoToLong(start_value);
			} catch (Exception e) {
				logger.error("Failed to parse the start-time parameter of the remote wrapper, a sample time could be:"
						+ (CURRENT_TIME));
				throw new RuntimeException(e);
			}
		}

		// If the scheduled start is not in the future
		// then start at the next whole time interval
		if (System.currentTimeMillis() >= startTime) {
			startTime = System.currentTimeMillis(); // current time
			// Calculate from midnight
			long midnight = DateUtils.truncate(new Date(), Calendar.DATE).getTime();
			long current_time = System.currentTimeMillis();
			// Start
			startTime = midnight + (((current_time - midnight) / clock_rate) + 1) * clock_rate;
		} // otherwise use the time retrieved from the VSD

		logger.warn(
				getVirtualSensorConfiguration().getName() + " scheduled to start at " + new Date(startTime).toString());

		// startTime is used in the virtual sensor class to start a timer
		timer0 = new Timer();
		// timer task is started in the sub class
		return true;
	}

	/**
	 * Handles new data received from the input stream.
	 * Validates the data against the output structure defined in the
	 * virtual sensor descriptor file.
	 * Logs any errors validating the data.
	 * Sets the dataItem property with the received data if valid.
	 */
	public void dataAvailable(String inputStreamName, StreamElement data) {
		try {
			// <TODO> if AbstractVirtualSensor.validateStreamElement() was protected then
			// we could re-use the validate and compatibleStructure methods but I am
			// not sure if that would be ok to do.
			// super.validateStreamElement(data, true);
			validateStreamElement(data, true);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return;
		}
		if(logger.isDebugEnabled()){
			logger.debug("Data received under the name: " + inputStreamName);
		}
		dataItem = data;

	}

	/**
	 * Validates the given StreamElement against the output structure
	 * defined in the virtual sensor descriptor.
	 * 
	 * Checks if the data types are compatible and the number of
	 * fields match the defined output structure.
	 * 
	 * Throws a RuntimeException if validation fails.
	 * 
	 * @param streamElement The StreamElement to validate
	 * @param adjust        If true, only checks data type compatibility,
	 *                      does not check number of fields.
	 */
	private void validateStreamElement(StreamElement streamElement, boolean adjust) {
		if (!compatibleStructure(streamElement, getVirtualSensorConfiguration().getOutputStructure(), adjust)) {
			StringBuilder exceptionMessage = new StringBuilder().append("The streamElement produced by :")
					.append(getVirtualSensorConfiguration().getName()).append(
							" Virtual Sensor is not compatible with the defined streamElement.\n");
			exceptionMessage.append("The expected stream element structure (specified in ")
					.append(getVirtualSensorConfiguration().getFileName()).append(" is [");
			for (DataField df : getVirtualSensorConfiguration().getOutputStructure()) {
				exceptionMessage.append(df.getName()).append(" (").append(DataTypes.TYPE_NAMES[df.getDataTypeID()])
						.append(") , ");
			}
			exceptionMessage.append(
					"] but the actual stream element received from the " + getVirtualSensorConfiguration().getName())
					.append(" has the [");
			for (int i = 0; i < streamElement.getFieldNames().length; i++) {
				exceptionMessage.append(streamElement.getFieldNames()[i]).append("(")
						.append(DataTypes.TYPE_NAMES[streamElement.getFieldTypes()[i]]).append("),");
			}
			exceptionMessage.append(" ] thus the stream element dropped !!!");
			throw new RuntimeException(exceptionMessage.toString());
		}
	}

	/**
	 * First checks compatibility of the data type of each output data item in the
	 * stream element with the
	 * defined output in the VSD file. (this check is done regardless of the value
	 * for adjust flag).
	 * <p>
	 * If the adjust flag is set to true, the method checks the newly generated
	 * stream element
	 * and returns true if and only if the number of data items is equal to the
	 * number of output
	 * data structure defined for this virtual sensor.
	 * If the adjust=true, then this test is not performed.
	 * 
	 * @param se
	 * @param outputStructure
	 * @param adjust          default is false.
	 * @return
	 */
	private static boolean compatibleStructure(StreamElement se, DataField[] outputStructure, boolean adjust) {
		if (!adjust && outputStructure.length != se.getFieldNames().length) {
			logger.warn(
					"Validation problem, the number of field doesn't match the number of output data strcture of the virtual sensor");
			return false;
		}
		int i = -1;
		for (DataField field : outputStructure) {
			Serializable value = se.getData(field.getName());
			i++;
			if (value == null) {
				continue;
			}
			if (((field.getDataTypeID() == DataTypes.BIGINT ||
					field.getDataTypeID() == DataTypes.DOUBLE ||
					field.getDataTypeID() == DataTypes.FLOAT ||
					field.getDataTypeID() == DataTypes.INTEGER ||
					field.getDataTypeID() == DataTypes.SMALLINT ||
					field.getDataTypeID() == DataTypes.TINYINT) && !(value instanceof Number))
					||
					((field.getDataTypeID() == DataTypes.VARCHAR || field.getDataTypeID() == DataTypes.CHAR)
							&& !(value instanceof String))
					||
					((field.getDataTypeID() == DataTypes.BINARY) && !(value instanceof byte[]))) {
				logger.warn("Validation problem for output field >" + field.getName() + ", The field type declared as >"
						+ field.getType() + "< while in VSD it is defined as >"
						+ DataTypes.TYPE_NAMES[outputStructure[i].getDataTypeID()]);
				return false;
			}
		}
		return true;
	}

	public abstract class MyTimerTask extends TimerTask {};

	public abstract void dispose();

}
