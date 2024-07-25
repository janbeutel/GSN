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
* File: src/ch/epfl/gsn/http/rest/Field4RestConverter.java
*
* @author Ali Salehi
* @author Davide De Sclavis
* @author Manuel Buchauer
* @author Jan Beutel
*
*/

package ch.epfl.gsn.delivery;

import java.io.Serializable;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.core.util.Base64Encoder;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import ch.epfl.gsn.beans.DataTypes;

/**
 * This class is responsible for converting Field4Rest objects to XML format and
 * vice versa.
 * It implements the Converter interface.
 */
public class Field4RestConverter implements Converter {

	private static final Class<Field4Rest> clazz = Field4Rest.class;
	private static final Base64Encoder base64 = new Base64Encoder();

	/**
	 * Marshals the given object to XML format.
	 *
	 * @param in      The object to be marshalled.
	 * @param writer  The HierarchicalStreamWriter used for writing XML.
	 * @param context The MarshallingContext.
	 */
	public void marshal(Object in, HierarchicalStreamWriter writer, MarshallingContext context) {
		Field4Rest input = (Field4Rest) in;
		writer.addAttribute("name", input.getName());
		if (input.getValue() == null) {
			writer.addAttribute("is-null", "true");
		}

		String type = "not-detected";
		String value = null;

		switch (input.getType()) {
			case DataTypes.BIGINT:
			case DataTypes.SMALLINT:
			case DataTypes.INTEGER:
			case DataTypes.DOUBLE:
			case DataTypes.TINYINT:
			case DataTypes.FLOAT:
				type = "numeric";
				if (input.getValue() != null) {
					value = Double.toString(((Number) input.getValue()).doubleValue());
				}
				break;
			case DataTypes.CHAR:
			case DataTypes.VARCHAR:
				type = "string";
				if (input.getValue() != null) {
					value = (String) input.getValue();
				}
				break;
			default:
				type = "binary";
				if (input.getValue() != null) {
					value = base64.encode((byte[]) input.getValue());
				}
				break;
		}

		writer.addAttribute("type", type);
		if (value != null) {
			writer.setValue(value);
		}
	}

	/**
	 * Unmarshals the XML data to create an object of type Field4Rest.
	 *
	 * @param reader  The HierarchicalStreamReader used for reading XML.
	 * @param context The UnmarshallingContext.
	 * @return The unmarshalled Field4Rest object.
	 */
	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
		Field4Rest toReturn = null;
		String name = reader.getAttribute("name");
		String type = reader.getAttribute("type");
		Byte typeId;
		if (type.equalsIgnoreCase("numeric")) {
			typeId = DataTypes.DOUBLE;
		} else if (type.equalsIgnoreCase("string")) {
			typeId = DataTypes.VARCHAR;
		} else {
			typeId = DataTypes.BINARY;
		}

		Boolean isNull = false;
		if (reader.getAttribute("is-null") != null) {
			isNull = Boolean.valueOf(reader.getAttribute("is-null"));
		}

		Serializable value = null;
		if (!isNull) {
			if (type.equalsIgnoreCase("string")) {
				value = reader.getValue();
			} else if (type.equalsIgnoreCase("numeric")) {
				value = Double.parseDouble(reader.getValue());
			} else {
				value = (byte[]) base64.decode(reader.getValue());
			}
		}

		toReturn = new Field4Rest(name, typeId, value);
		return toReturn;
	}

	/**
	 * Checks if the given class can be converted by this converter.
	 *
	 * @param arg0 The class to be checked.
	 * @return true if the class can be converted, false otherwise.
	 */
	public boolean canConvert(Class arg0) {
		return clazz.equals(arg0);
	}
}
