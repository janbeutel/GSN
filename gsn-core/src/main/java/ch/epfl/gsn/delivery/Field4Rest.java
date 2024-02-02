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
* File: src/ch/epfl/gsn/http/rest/Field4Rest.java
*
* @author Ali Salehi
*
*/

package ch.epfl.gsn.delivery;

import java.io.Serializable;

/**
 * Represents a field for REST communication.
 */
public class Field4Rest {
	private String name;
	private Serializable value;
	private Byte type;

	/**
	 * Constructs a Field4Rest object with the specified name, type, and value.
	 *
	 * @param name  the name of the field
	 * @param type  the type of the field
	 * @param value the value of the field
	 */
	public Field4Rest(String name, Byte type, Serializable value) {
		this.name = name;
		this.type = type;
		this.value = value;
	}

	/**
	 * Returns the name of the field.
	 *
	 * @return the name of the field
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the value of the field.
	 *
	 * @return the value of the field
	 */
	public Serializable getValue() {
		return value;
	}

	/**
	 * Returns the type of the field.
	 *
	 * @return the type of the field
	 */
	public byte getType() {
		return type;
	}

	/**
	 * Returns a string representation of the Field4Rest object.
	 *
	 * @return a string representation of the object
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Field(name:").append(name).append(",").append("type:").append(type).append(",value:").append(value)
				.append(")");
		return sb.toString();
	}

}
