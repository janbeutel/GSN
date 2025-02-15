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
* File: src/ch/epfl/gsn/beans/AddressBean.java
*
* @author Mehdi Riahi
* @author gsn_devs
* @author Timotee Maret
* @author Ali Salehi
* @author Julien Eberle
* @author Davide De Sclavis
* @author Manuel Buchauer
* @author Jan Beutel
*
*/

package ch.epfl.gsn.beans;

import java.io.Serializable;

import org.apache.commons.collections.KeyValue;

public final class AddressBean implements Serializable {

	private static final long serialVersionUID = -8975180532136014200L;

	private static final KeyValue[] EMPTY_PREDICATES = new KeyValue[0];

	private String wrapper;

	private String partialOrderKey;

	private KeyValue[] predicates = EMPTY_PREDICATES;

	private DataField[] wrapperOutputStructure = new DataField[0];

	private double random = Math.random();
	private String inputStreamName;
	private String virtualSensorName;
	private VSensorConfig vsconfig;

	public AddressBean() {
		this.predicates = EMPTY_PREDICATES;
	}

	public AddressBean(final String wrapper, KeyValue... newPredicates) {
		this.wrapper = wrapper;
		if (newPredicates == null) {
			this.predicates = EMPTY_PREDICATES;
		} else {
			this.predicates = newPredicates;
		}

	}

	public AddressBean(final String wrapper) {
		this.wrapper = wrapper;
		this.predicates = EMPTY_PREDICATES;
	}

	public String getWrapper() {
		return this.wrapper;
	}

	public String getPartialOrderKey() {
		return this.partialOrderKey;
	}

	public void setPartialOrderKey(String key) {
		this.partialOrderKey = key;

	}

	public KeyValue[] getPredicates() {
		return this.predicates;
	}

	/**
	 * Retrieves the value associated with the specified key from the predicates
	 * list.
	 * 
	 * @param key the key to search for
	 * @return the value associated with the key
	 * @throws RuntimeException if the key is not found in the predicates list
	 */
	public String getPredicateValueWithException(String key) {
		String keyTrimmed = key.trim();
		for (KeyValue predicate : this.predicates) {
			if (predicate.getKey().toString().trim().equalsIgnoreCase(keyTrimmed)) {
				final String value = (String) predicate.getValue();
				if (value.trim().length() > 0) {
					return value;
				}

			}
		}
		throw new RuntimeException(
				"The required parameter: >" + key + "<+ is missing.from the virtual sensor configuration file.");
	}

	/**
	 * Note that the key for the value is case insensitive.
	 * 
	 * @param key
	 * @return
	 */

	public String getPredicateValue(String key) {
		key = key.trim();
		for (KeyValue predicate : this.predicates) {
			if (predicate.getKey().toString().trim().equalsIgnoreCase(key)) {
				return ((String) predicate.getValue());
			}
		}
		return null;
	}

	/**
	 * Gets a parameter name. If the parameter value exists and is not an empty
	 * string, returns the value otherwise returns the
	 * default value
	 * 
	 * @param key          The key to look for in the map.
	 * @param defaultValue Will be return if the key is not present or its an empty
	 *                     string.
	 * @return
	 */
	public String getPredicateValueWithDefault(String key, String defaultValue) {
		String value = getPredicateValue(key);
		if (value == null || value.trim().length() == 0) {
			return defaultValue;
		}
		return value;
	}

	/**
	 * Gets a parameter name. If the parameter value exists and is a valid integer,
	 * returns the value otherwise returns the
	 * default value
	 * 
	 * @param key          The key to look for in the map.
	 * @param defaultValue Will be return if the key is not present or its value is
	 *                     not a valid integer.
	 * @return
	 */
	public int getPredicateValueAsInt(String key, int defaultValue) {
		String value = getPredicateValue(key);
		if (value == null || value.trim().length() == 0) {
			return defaultValue;
		}

		try {
			return Integer.parseInt(value);
		} catch (Exception e) {
			return defaultValue;
		}
	}

	/**
	 * Retrieves the value of a predicate as an integer, throwing an exception if
	 * the value is missing or cannot be parsed as an integer.
	 * 
	 * @param key the key of the predicate
	 * @return the value of the predicate as an integer
	 * @throws RuntimeException if the value is missing or cannot be parsed as an
	 *                          integer
	 */
	public int getPredicateValueAsIntWithException(String key) {
		String value = getPredicateValue(key);
		if (value == null || value.trim().length() == 0) {
			throw new RuntimeException(
					"The required parameter: >" + key + "<+ is missing.from the virtual sensor configuration file.");
		}
		try {
			return Integer.parseInt(value);
		} catch (Exception e) {
			throw new RuntimeException("The required parameter: >" + key
					+ "<+ is bad formatted.from the virtual sensor configuration file.", e);
		}
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(random);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj == null) {
			return false;
		}

		if (getClass() != obj.getClass()) {
			return false;
		}

		AddressBean other = (AddressBean) obj;
		if (Double.doubleToLongBits(random) != Double
				.doubleToLongBits(other.random)) {
			return false;
		}

		return true;
	}

	public String toString() {
		final StringBuffer result = new StringBuffer("[").append(this.getWrapper());
		for (final KeyValue predicate : this.predicates) {
			result.append(predicate.getKey() + " = " + predicate.getValue() + ",");
		}
		result.append("]");
		return result.toString();
	}


	public DataField[] getOutputStructure() {
		return wrapperOutputStructure;
	}

	public void setVsconfig(DataField[] outputStructure) {
		this.wrapperOutputStructure = outputStructure;
	}

	public String getInputStreamName() {
		return inputStreamName;
	}

	public void setInputStreamName(String inputStreamName) {
		this.inputStreamName = inputStreamName;
	}

	public String getVirtualSensorName() {
		return virtualSensorName;
	}

	public void setVirtualSensorName(String virtualSensorName) {
		this.virtualSensorName = virtualSensorName;
	}

	public VSensorConfig getVirtualSensorConfig() {
		return vsconfig;
	}

	public void setVirtualSensorConfig(VSensorConfig vsconfig) {
		this.vsconfig = vsconfig;
	}

}
