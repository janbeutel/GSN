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
* File: src/ch/epfl/gsn/http/rest/ModelDistributionRequest.java
*
* @author Julien Eberle
*
*/

package ch.epfl.gsn.delivery;

import java.io.IOException;
import java.sql.SQLException;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import ch.epfl.gsn.beans.DataField;
import ch.epfl.gsn.beans.StreamElement;
import ch.epfl.gsn.beans.VSensorConfig;
import ch.epfl.gsn.storage.SQLValidator;
import ch.epfl.gsn.utils.models.AbstractModel;

public class ModelDistributionRequest implements DistributionRequest {

	private static transient Logger logger = LoggerFactory.getLogger(DefaultDistributionRequest.class);

	private String query;

	private DeliverySystem deliverySystem;

	private VSensorConfig vSensorConfig;

	private AbstractModel modelClass;

	/**
	 * Constructor of ModelDistributionRequest class initializes a
	 * ModelDistributionRequest object with the provided parameters.
	 *
	 * @param deliverySystem The delivery system for distributing the model.
	 * @param sensorConfig   The sensor configuration associated with the
	 *                       distribution request.
	 * @param query          The query associated with the distribution request.
	 * @param model          The machine learning model to be distributed.
	 * @throws IOException  If an I/O error occurs during the initialization
	 *                      process.
	 * @throws SQLException If a SQL-related error occurs during the initialization
	 *                      process.
	 */
	private ModelDistributionRequest(DeliverySystem deliverySystem, VSensorConfig sensorConfig, String query,
			AbstractModel model) throws IOException, SQLException {
		this.deliverySystem = deliverySystem;
		vSensorConfig = sensorConfig;
		this.query = query;
		this.modelClass = model;
		DataField[] selectedColmnNames = SQLValidator.getInstance().extractSelectColumns(query,
				modelClass.getOutputFields());
		deliverySystem.writeStructure(selectedColmnNames);
	}

	/**
	 * Generates a string representation of the ModelDistributionRequest object.
	 *
	 * @return A string representation of the ModelDistributionRequest object,
	 *         including information about the delivery system, query, model class,
	 *         and virtual sensor name.
	 */
	public String toString() {
		return new StringBuilder("ModelDistributionRequest Request[[ Delivery System: ")
				.append(deliverySystem.getClass().getName())
				.append("],[Query:").append(query)
				.append("],[model:")
				.append(modelClass.getClass().getName())
				.append("],[VirtualSensorName:")
				.append(vSensorConfig.getName())
				.append("]]").toString();
	}

	public boolean deliverKeepAliveMessage() {
		return deliverySystem.writeKeepAliveStreamElement();
	}

	public boolean deliverStreamElement(StreamElement se) {
		return deliverySystem.writeStreamElement(se);
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
	 * Determines if the given Object equals the ModelDistributionRequest object.
	 *
	 * @param o The object to compare for equality.
	 * @return true if the two objects are equal, false otherwise.
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		ModelDistributionRequest that = (ModelDistributionRequest) o;

		if (deliverySystem == null ? that.deliverySystem != null : !deliverySystem.equals(that.deliverySystem)) {
			return false;
		}

		if (query == null ? that.query != null : !query.equals(that.query)) {
			return false;
		}
		if (vSensorConfig == null ? that.vSensorConfig != null : !vSensorConfig.equals(that.vSensorConfig)) {
			return false;
		}

		if (modelClass == null ? that.modelClass != null : modelClass.getClass() != that.modelClass.getClass() ) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = query == null ? 0 : query.hashCode();
		result = 31 * result + (deliverySystem == null ? 0 : deliverySystem.hashCode());
		result = 31 * result + (vSensorConfig == null ? 0 : vSensorConfig.hashCode());
		return result;
	}

	@Override
	public long getStartTime() {
		return 0;
	}

	@Override
	public long getLastVisitedPk() {
		return 0;
	}

	/**
	 * Creates a ModelDistributionRequest object with the provided parameters.
	 *
	 * @param delivery      The delivery system for distributing the model.
	 * @param vSensorConfig The sensor configuration associated with the
	 *                      distribution request.
	 * @param query         The query associated with the distribution request.
	 * @param modelClass    The machine learning model to be distributed.
	 * @return The created ModelDistributionRequest object.
	 * @throws IOException  If an I/O error occurs during the initialization
	 *                      process.
	 * @throws SQLException If a SQL-related error occurs during the initialization
	 *                      process.
	 */
	public static ModelDistributionRequest create(DeliverySystem delivery,
			VSensorConfig vSensorConfig, String query, AbstractModel modelClass) throws IOException, SQLException {
		return new ModelDistributionRequest(delivery, vSensorConfig, query, modelClass);
	}

	@Override
	public AbstractModel getModel() {
		return modelClass;
	}

}
