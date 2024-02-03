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
* File: src/ch/epfl/gsn/http/rest/LocalDeliveryWrapper.java
*
* @author Ali Salehi
* @author Mehdi Riahi
* @author Timotee Maret
*
*/

package ch.epfl.gsn.delivery;

import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.Date;

import javax.naming.OperationNotSupportedException;

import org.slf4j.LoggerFactory;

import ch.epfl.gsn.Main;
import ch.epfl.gsn.DataDistributer;
import ch.epfl.gsn.Mappings;
import ch.epfl.gsn.VirtualSensorInitializationFailedException;
import ch.epfl.gsn.beans.AddressBean;
import ch.epfl.gsn.beans.DataField;
import ch.epfl.gsn.beans.StreamElement;
import ch.epfl.gsn.beans.VSensorConfig;
import ch.epfl.gsn.storage.SQLUtils;
import ch.epfl.gsn.storage.SQLValidator;
import ch.epfl.gsn.utils.Helpers;
import ch.epfl.gsn.vsensor.AbstractVirtualSensor;
import ch.epfl.gsn.wrappers.AbstractWrapper;

import java.sql.Connection;
import java.sql.ResultSet;

import org.slf4j.Logger;
import org.joda.time.format.ISODateTimeFormat;

/**
 * The LocalDeliveryWrapper class represents a local delivery system.
 * It extends the AbstractWrapper class and implements the DeliverySystem
 * interface.
 * This class acts as a wrapper around the actual delivery system
 * implementation,
 * providing an abstraction layer for interacting with the delivery system.
 */
public class LocalDeliveryWrapper extends AbstractWrapper implements DeliverySystem {

	private final String CURRENT_TIME = ISODateTimeFormat.dateTime().print(System.currentTimeMillis());

	private static transient Logger logger = LoggerFactory.getLogger(LocalDeliveryWrapper.class);

	private VSensorConfig vSensorConfig;
	private DataField[] structure;

	private DefaultDistributionRequest distributionRequest;

	public VSensorConfig getVSensorConfig() {
		return vSensorConfig;
	}

	public String getWrapperName() {
		return "Local-wrapper";
	}

	/**
	 * Initializes the local delivery system by setting up the necessary parameters
	 * and configurations.
	 * It retrieves the required parameters from the AddressBean object and
	 * validates them.
	 * The method checks if the `query` and `name` parameters are specified, and if
	 * not, it logs an error and returns false.
	 * If the `query` parameter is not specified, a default query is constructed
	 * using the `vsName` parameter.
	 * If the `startTime` is set to "continue", the method retrieves the last
	 * visited timestamp from the database.
	 * If the `startTime` starts with a "-", it indicates a relative time offset
	 * from the current time.
	 * Otherwise, the `startTime` is assumed to be an ISO-formatted timestamp.
	 * The method validates the query using the `SQLValidator` class and retrieves
	 * the virtual sensor configuration.
	 * It then rewrites the query using the `SQLUtils` class, replacing the virtual
	 * sensor name with a lowercase version.
	 * Finally, the method creates a `DefaultDistributionRequest` object for further
	 * processing in the delivery system.
	 *
	 * @return true if the initialization is successful, false otherwise.
	 */
	public boolean initialize() {
		AddressBean params = getActiveAddressBean();
		String query = params.getPredicateValue("query");

		String vsName = params.getPredicateValue("name");
		String startTime = params.getPredicateValueWithDefault("start-time", CURRENT_TIME);

		if (query == null && vsName == null) {
			logger.error("For using local-wrapper, either >query< or >name< parameters should be specified");
			return false;
		}

		if (query == null) {
			query = "select * from " + vsName;
		}

		long lastVisited = -1;
		boolean continuous = false;
		Connection conn = null;
		ResultSet rs = null;
		if (startTime.equals("continue")) {
			continuous = true;
			try {
				conn = Main.getStorage(params.getVirtualSensorConfig()).getConnection();

				rs = conn.getMetaData().getTables(null, null, params.getVirtualSensorName(), new String[] { "TABLE" });
				if (rs.next()) {
					StringBuilder dbquery = new StringBuilder();
					dbquery.append("select max(timed) from ").append(params.getVirtualSensorName());
					Main.getStorage(params.getVirtualSensorConfig()).close(rs);

					rs = Main.getStorage(params.getVirtualSensorConfig()).executeQueryWithResultSet(dbquery, conn);
					if (rs.next()) {
						lastVisited = rs.getLong(1);
					}
				}
			} catch (SQLException e) {
				logger.error(e.getMessage(), e);
			} finally {
				Main.getStorage(params.getVirtualSensorConfig()).close(rs);
				Main.getStorage(params.getVirtualSensorConfig()).close(conn);
			}
		} else if (startTime.startsWith("-")) {
			try {
				lastVisited = System.currentTimeMillis() - Long.parseLong(startTime.substring(1));
			} catch (NumberFormatException e) {
				logger.error("Problem in parsing the start-time parameter, the provided value is: " + startTime);
				logger.error(e.getMessage(), e);
				return false;
			}
		} else {
			try {
				lastVisited = Helpers.convertTimeFromIsoToLong(startTime);
			} catch (Exception e) {
				logger.error("Problem in parsing the start-time parameter, the provided value is:" + startTime
						+ " while a valid input is:" + CURRENT_TIME);
				logger.error(e.getMessage(), e);
				return false;
			}
		}

		try {
			vsName = SQLValidator.getInstance().validateQuery(query);
			if (vsName == null) { // while the other instance is not loaded.
				return false;
			}

			vSensorConfig = Mappings.getConfig(vsName);

			if (startTime.equals("continue")) {
				try {
					conn = Main.getStorage(vSensorConfig).getConnection();

					rs = conn.getMetaData().getTables(null, null, vsName, new String[] { "TABLE" });
					if (rs.next()) {
						StringBuilder dbquery = new StringBuilder();
						dbquery.append("select max(timed) from ").append(vsName);
						Main.getStorage(vSensorConfig).close(rs);

						rs = Main.getStorage(vSensorConfig).executeQueryWithResultSet(dbquery, conn);
						if (rs.next()) {
							long t = rs.getLong(1);
							if (lastVisited > t) {
								lastVisited = t;
								logger.info("newest timed from " + vsName
										+ " is older than requested start time -> using timed as start time");
							}
						}
					}
				} catch (SQLException e) {
					logger.error(e.getMessage(), e);
				} finally {
					Main.getStorage(vSensorConfig).close(rs);
					Main.getStorage(vSensorConfig).close(conn);
				}
			}
			if (logger.isDebugEnabled()) {
				logger.debug("lastVisited=" + String.valueOf(lastVisited));
			}

			query = SQLUtils.newRewrite(query, vsName, vsName.toLowerCase()).toString();
			if(logger.isDebugEnabled()){
				logger.debug("Local wrapper request received for: " + vsName);
			}
			distributionRequest = DefaultDistributionRequest.create(this, vSensorConfig, query, lastVisited);
			// This call MUST be executed before adding this listener to the
			// data-distributer because distributer checks the isClose method before
			// flushing.
		} catch (Exception e) {
			logger.error("Problem in the query parameter of the local-wrapper.");
			logger.error(e.getMessage(), e);
			return false;
		}
		return true;
	}

	/**
	 * Responsible for sending data back to the source virtual sensor by invoking
	 * the `dataFromWeb` method on an instance of the virtual sensor.
	 *
	 * @param action      The action to be performed by the virtual sensor.
	 * @param paramNames  An array of parameter names to be passed to the action.
	 * @param paramValues An array of parameter values to be passed to the action.
	 * @return true if the data is successfully sent to the virtual sensor, false
	 *         otherwise.
	 * @throws OperationNotSupportedException If the operation is not supported by
	 *                                        the virtual sensor.
	 */
	public boolean sendToWrapper(String action, String[] paramNames, Serializable[] paramValues)
			throws OperationNotSupportedException {
		AbstractVirtualSensor vs;
		try {
			vs = Mappings.getVSensorInstanceByVSName(vSensorConfig.getName()).borrowVS();
		} catch (VirtualSensorInitializationFailedException e) {
			logger.warn("Sending data back to the source virtual sensor failed !: " + e.getMessage(), e);
			return false;
		}
		boolean toReturn = vs.dataFromWeb(action, paramNames, paramValues);
		Mappings.getVSensorInstanceByVSName(vSensorConfig.getName()).returnVS(vs);
		return toReturn;
	}

	/**
	 * Returns a string representation of the LocalDeliveryWrapper.
	 *
	 * @return A string representation of the LocalDeliveryWrapper, including the
	 *         query and start time.
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("LocalDistributionReq => [").append(distributionRequest.getQuery()).append(", Start-Time: ")
				.append(new Date(distributionRequest.getStartTime())).append("]");
		return sb.toString();
	}

	/**
	 * Responsible for running the local data distribution process.
	 * It retrieves an instance of the `DataDistributer` class specific to the
	 * `LocalDeliveryWrapper` class.
	 * The method adds the `distributionRequest` object as a listener to the
	 * `DataDistributer` instance.
	 * This allows the `LocalDeliveryWrapper` to receive and process new data for
	 * local distribution.
	 */
	public void run() {
		DataDistributer localDistributer = DataDistributer.getInstance(LocalDeliveryWrapper.class);
		localDistributer.addListener(this.distributionRequest);
	}

	public void writeStructure(DataField[] fields) throws IOException {
		this.structure = fields;

	}

	public DataField[] getOutputFormat() {
		return structure;
	}

	/**
	 * Responsible for closing the local delivery.
	 * It logs a warning message indicating the closure of the local delivery.
	 * The method then attempts to release any resources held by the local delivery
	 * by invoking the `releaseResources` method of the AbstractWrapper class.
	 * If an `SQLException` occurs during the resource release, an error message is
	 * logged.
	 */
	public void close() {
		logger.warn("Closing a local delivery.");
		try {
			releaseResources();
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}

	}

	public boolean isClosed() {
		return !isActive();
	}

	/**
	 * Responsible for writing a StreamElement object to the local delivery system.
	 *
	 * @param se The StreamElement object to be written.
	 * @return true indicating that the stream element was written successfully.
	 */
	public boolean writeStreamElement(StreamElement se) {
		boolean isSucced = postStreamElement(se);
		if(logger.isDebugEnabled()){
			logger.debug("wants to deliver stream element:" + se.toString() + "[" + isSucced + "]");
		}
		return true;
	}

	public boolean writeKeepAliveStreamElement() {
		return true;
	}

	public void dispose() {

	}

}
