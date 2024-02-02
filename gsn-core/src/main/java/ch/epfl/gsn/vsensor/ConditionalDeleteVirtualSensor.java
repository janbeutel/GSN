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
* @author Tonio Gsell
* @author Jan Beutel
*/

package ch.epfl.gsn.vsensor;

import java.util.ArrayList;
import java.util.TreeMap;

import ch.epfl.gsn.Main;
import ch.epfl.gsn.beans.DataField;
import ch.epfl.gsn.beans.DataTypes;
import ch.epfl.gsn.beans.StreamElement;
import ch.epfl.gsn.beans.VSensorConfig;

import java.sql.PreparedStatement;
import java.sql.SQLException;

//import org.apache.log4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 * This class extends BridgeVirtualSensorPermasense to provide conditional
 * deletion of data based on evaluating fields against values.
 */
public class ConditionalDeleteVirtualSensor extends BridgeVirtualSensorPermasense {

	private final static transient Logger logger = LoggerFactory.getLogger(ConditionalDeleteVirtualSensor.class);

	private PreparedStatement preparedDeleteStatement;
	private ArrayList<String> fieldList;
	String conditional = "";

	/**
	 * Initializes the virtual sensor by parsing parameters from the virtual sensor
	 * configuration, constructing the delete query, and preparing the delete statement.
	 * 
	 * Retrieves the field names and operations from the parameter map. Validates
	 * that the fields exist in the output structure. 
	 * Constructs the conditional delete query by joining the field names,
	 * operations, and join clauses. Prepares the delete statement.
	 */
	@Override
	public boolean initialize() {
		boolean ret = super.initialize();
		VSensorConfig vsensor = getVirtualSensorConfiguration();
		TreeMap<String, String> params = vsensor.getMainClassInitialParams();
		fieldList = new ArrayList<String>();

		int index = 1;
		while (true) {
			String field = params.get("field" + index);
			if (field == null) {
				if (index == 1) {
					logger.error("no field1 parameter available");
				}
				break;
			}
			boolean fieldExists = false;
			for (DataField fields : vsensor.getOutputStructure()) {
				if (field.equalsIgnoreCase(fields.getName())) {
					fieldExists = true;
					break;
				}
			}
			if (!fieldExists) {
				logger.error("field" + index + " " + field + " does not exist in the virtual sensors output structure");
				return false;
			}
			String operation = params.get("operation" + index);
			if (operation == null) {
				logger.error("no operation" + index + " parameter for field" + index + " available");
				return false;
			}
			if (index > 1) {
				String join = params.get("join" + index);
				if (join == null) {
					logger.error("no join" + index + " parameter for field" + index + " and operation" + index
							+ " available");
					return false;
				}
				conditional += " " + join + " " + field + " " + operation + " ?";
			} else {
				conditional = "DELETE FROM " + vsensor.getName() + " WHERE " + field + " " + operation + " ?";
			}
			fieldList.add(field);
			index++;
		}
		try {
			preparedDeleteStatement = Main.getStorage(vsensor).getConnection().prepareStatement(conditional);
		} catch (SQLException e) {
			logger.error(e.getMessage());
		}

		if (logger.isDebugEnabled()) {
			logger.debug("prepared delete statement: " + conditional);
		}
		return ret;
	}

	/**
	 * Handles incoming data from the input stream.
	 * Checks if the data contains the fields needed for the delete query.
	 * If so, sets the prepared statement parameters and executes the delete.
	 * Retries the delete up to 3 times if there are issues executing it.
	 * 
	 * @param inputStreamName Name of the input stream
	 * @param data            StreamElement containing the input data
	 */
	@Override
	public void dataAvailable(String inputStreamName, StreamElement data) {
		long time = System.currentTimeMillis();

		boolean retry = true;
		int cnt = 0;

		while (retry && cnt++ < 3) {
			try {
				for (int i = 0; i < fieldList.size(); i++) {
					if (data.getData(fieldList.get(i)) == null) {
						logger.warn("field " + fieldList.get(i)
								+ " does not exist in stream element or is NULL -> delete query can not be applied");
						super.dataAvailable(inputStreamName, data);
						return;
					} else {
						switch (data.getType(fieldList.get(i))) {
							case DataTypes.BIGINT:
								preparedDeleteStatement.setLong(i + 1, (Long) data.getData(fieldList.get(i)));
								break;
							case DataTypes.INTEGER:
							case DataTypes.SMALLINT:
							case DataTypes.TINYINT:
								preparedDeleteStatement.setInt(i + 1, (Integer) data.getData(fieldList.get(i)));
								break;
							case DataTypes.DOUBLE:
								preparedDeleteStatement.setDouble(i + 1, (Double) data.getData(fieldList.get(i)));
								break;
							case DataTypes.VARCHAR:
								preparedDeleteStatement.setString(i + 1, (String) data.getData(fieldList.get(i)));
								break;
							case DataTypes.CHAR:
								preparedDeleteStatement.setByte(i + 1, (Byte) data.getData(fieldList.get(i)));
								break;
							default:
								logger.error("unknown data type");
						}
					}
				}
			} catch (SQLException e) {
				logger.error(e.getMessage());
			}

			try {
				preparedDeleteStatement.execute();
				retry = false;
			} catch (SQLException e) {
				try {
					preparedDeleteStatement = Main.getStorage(getVirtualSensorConfiguration()).getConnection()
							.prepareStatement(conditional);
				} catch (SQLException e1) {
					logger.error(e.getMessage());
				}
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("delete execution time: " + (System.currentTimeMillis() - time) + "ms");
		}

		super.dataAvailable(inputStreamName, data);
	}
}
