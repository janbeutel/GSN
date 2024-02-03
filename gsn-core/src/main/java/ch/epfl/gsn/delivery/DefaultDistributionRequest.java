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
* File: src/ch/epfl/gsn/http/rest/DefaultDistributionRequest.java
*
* @author Ali Salehi
* @author Mehdi Riahi
* @author Timotee Maret
* @author Julien Eberle
*
*/

package ch.epfl.gsn.delivery;

import java.io.IOException;
import java.sql.SQLException;

import org.slf4j.LoggerFactory;

import ch.epfl.gsn.beans.DataField;
import ch.epfl.gsn.beans.StreamElement;
import ch.epfl.gsn.beans.VSensorConfig;
import ch.epfl.gsn.storage.SQLValidator;
import ch.epfl.gsn.utils.models.AbstractModel;

import org.slf4j.Logger;

/**
 * The DefaultDistributionRequest class represents a distribution request for a
 * delivery system in a Global Sensor Networks (GSN) application.
 * It implements the {@code DistributionRequest} interface and provides methods
 * for handling the distribution of data from a sensor to the specified delivery
 * system.
 */
public class DefaultDistributionRequest implements DistributionRequest {

	private static transient Logger logger = LoggerFactory.getLogger(DefaultDistributionRequest.class);

	private long startTime;

	private long lastVisitedPk = -1;

	private String query;

	private DeliverySystem deliverySystem;

	private VSensorConfig vSensorConfig;

	/**
	 * Constructor of a new DefaultDistributionRequest object with the provided
	 * parameters.
	 * The constructor initializes the instance variables of the object and performs
	 * additional operations such as extracting selected column names from the query
	 * and writing the structure of the selected columns to the delivery system.
	 *
	 * @param deliverySystem The delivery system to write the structure of the
	 *                       selected columns.
	 * @param sensorConfig   The VSensorConfig object containing sensor
	 *                       configuration.
	 * @param query          The query string.
	 * @param startTime      The start time for the distribution request.
	 * @throws IOException  If an I/O error occurs during the writing of the
	 *                      structure.
	 * @throws SQLException If a SQL error occurs during the extraction of selected
	 *                      column names.
	 */
	private DefaultDistributionRequest(DeliverySystem deliverySystem, VSensorConfig sensorConfig, String query,
			long startTime) throws IOException, SQLException {
		this.deliverySystem = deliverySystem;
		vSensorConfig = sensorConfig;
		this.query = query;
		this.startTime = startTime;
		DataField[] selectedColmnNames = SQLValidator.getInstance().extractSelectColumns(query, vSensorConfig);
		deliverySystem.writeStructure(selectedColmnNames);
	}

	/**
	 * Creates a new instance of the DefaultDistributionRequest class with the
	 * provided parameters.
	 * Calls the Constructor and therefore encapsulates the object creation logic
	 * and returns the newly created object.
	 *
	 * @param deliverySystem The delivery system to write the structure of the
	 *                       selected columns.
	 * @param sensorConfig   The VSensorConfig object containing sensor
	 *                       configuration.
	 * @param query          The query string.
	 * @param startTime      The start time for the distribution request.
	 * @return A new instance of the DefaultDistributionRequest class.
	 * @throws IOException  If an I/O error occurs during the writing of the
	 *                      structure.
	 * @throws SQLException If a SQL error occurs during the extraction of selected
	 *                      column names.
	 */
	public static DefaultDistributionRequest create(DeliverySystem deliverySystem, VSensorConfig sensorConfig,
			String query, long startTime) throws IOException, SQLException {
		DefaultDistributionRequest toReturn = new DefaultDistributionRequest(deliverySystem, sensorConfig, query,
				startTime);
		return toReturn;
	}

	/**
	 * Returns a string representation of the DefaultDistributionRequest object.
	 * The method constructs a string by concatenating various information such as
	 * the delivery system class name, query string, start time, and virtual sensor
	 * name.
	 *
	 * @return A string representation of the DefaultDistributionRequest object.
	 */
	public String toString() {
		return new StringBuilder("DefaultDistributionRequest Request[[ Delivery System: ")
				.append(deliverySystem.getClass().getName())
				.append("],[Query:").append(query)
				.append("],[startTime:")
				.append(startTime)
				.append("],[VirtualSensorName:")
				.append(vSensorConfig.getName())
				.append("]]").toString();
	}

	public boolean deliverKeepAliveMessage() {
		return deliverySystem.writeKeepAliveStreamElement();
	}

	public boolean deliverStreamElement(StreamElement se) {
		boolean success = deliverySystem.writeStreamElement(se);
		// boolean success = true;
		if (success) {
			// startTime=se.getTimeStamp();
			lastVisitedPk = se.getInternalPrimayKey();
		}
		return success;
	}

	public long getStartTime() {
		return startTime;
	}

	public long getLastVisitedPk() {
		return lastVisitedPk;
	}

	public String getQuery() {
		return query;
	}

	public VSensorConfig getVSensorConfig() {
		return vSensorConfig;
	}

	public void close() {
		deliverySystem.close();
	}

	public boolean isClosed() {
		return deliverySystem.isClosed();
	}

	public DeliverySystem getDeliverySystem() {
		return deliverySystem;
	}

	/**
	 * Compares the current DefaultDistributionRequest object with the provided
	 * object for equality.
	 * The method returns true if the provided object is also a
	 * DefaultDistributionRequest object
	 * and all the instance variables of both objects are equal. Otherwise, it
	 * returns false.
	 *
	 * @param o The object to compare with the current DefaultDistributionRequest
	 *          object.
	 * @return true if the objects are equal, false otherwise.
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		DefaultDistributionRequest that = (DefaultDistributionRequest) o;

		if (deliverySystem == null ? that.deliverySystem != null : !deliverySystem.equals(that.deliverySystem)) {
			return false;
		}
		if (query == null ? that.query != null : !query.equals(that.query) ) {
			return false;
		}
		if (vSensorConfig == null ? that.vSensorConfig != null : !vSensorConfig.equals(that.vSensorConfig)) {
			return false;
		}

		return true;
	}

	/**
	 * Generates a hash code value for this DefaultDistributionRequest object.
	 * The hash code is calculated based on the deliverysystem, query, and virtual
	 * sensor name.
	 *
	 * @return The hash code value for this DefaultDistributionRequest object.
	 */
	@Override
	public int hashCode() {
		int result = query == null ? 0 :query.hashCode();
		result = 31 * result + (deliverySystem == null ? 0:deliverySystem.hashCode());
		result = 31 * result + (vSensorConfig == null ?  0:vSensorConfig.hashCode());
		return result;
	}

	@Override
	public AbstractModel getModel() {
		return null;
	}
}
