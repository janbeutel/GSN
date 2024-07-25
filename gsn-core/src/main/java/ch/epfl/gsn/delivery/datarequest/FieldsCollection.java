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
* File: src/ch/epfl/gsn/http/datarequest/FieldsCollection.java
*
* @author Timotee Maret
* @author Davide De Sclavis
* @author Manuel Buchauer
* @author Jan Beutel
*
*/

package ch.epfl.gsn.delivery.datarequest;

/**
 * This class stores a list of Fields for a Virtual Sensor. It adds by default
 * the <code>timed</code> field if missing and keep track if the
 * <code>timed</code> was needed or not.
 */
public class FieldsCollection {

	private boolean wantTimed;
	private String[] fields;

	/**
	 * Constructs a FieldsCollection object with the specified fields.
	 *
	 * @param _fields the array of fields
	 */
	public FieldsCollection(String[] _fields) {

		wantTimed = false;
		for (int j = 0; j < _fields.length; j++) {
			if (_fields[j].compareToIgnoreCase("timed") == 0) {
				wantTimed = true;
			}

		}
		String[] tmp = _fields;
		if (!wantTimed) {
			tmp = new String[_fields.length + 1];
			System.arraycopy(_fields, 0, tmp, 0, _fields.length);
			tmp[tmp.length - 1] = "timed";
		}
		this.fields = tmp;
	}

	/**
	 * Checks if the "timed" field is requested.
	 *
	 * @return true if the "timed" field is requested, false otherwise
	 */
	public boolean isWantTimed() {
		return wantTimed;
	}

	/**
	 * Retrieves the fields.
	 *
	 * @return an array of fields
	 */
	public String[] getFields() {
		return fields;
	}
}