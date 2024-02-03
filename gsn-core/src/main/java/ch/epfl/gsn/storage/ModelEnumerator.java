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
* File: src/ch/epfl/gsn/storage/ModelEnumerator.java
*
* @author Julien Eberle
*
*/

package ch.epfl.gsn.storage;

import java.io.Serializable;

import org.slf4j.LoggerFactory;

import ch.epfl.gsn.beans.DataField;
import ch.epfl.gsn.beans.StreamElement;
import ch.epfl.gsn.utils.models.AbstractModel;

import org.slf4j.Logger;

public class ModelEnumerator implements DataEnumeratorIF {

	private static final transient Logger logger = LoggerFactory.getLogger(ModelEnumerator.class);

	private StreamElement[] results;
	private int ptr;

	/**
	 * Constructs a ModelEnumerator object for iterating over query results from the
	 * specified model and query.
	 *
	 * @param query The SQL query containing conditions to filter model results.
	 * @param model The AbstractModel instance to query.
	 */
	public ModelEnumerator(String query, AbstractModel model) {
		String where = SQLUtils.extractWhereClause(query).toLowerCase();
		String[] ex = where.split(" and ");
		DataField[] df = new DataField[ex.length];
		Serializable[] sr = new Serializable[ex.length];
		int i = 0;
		for (String s : ex) {
			String[] v = s.split(" = ");
			df[i] = new DataField(v[0], "double"); // !!! _HARDCODED, only supports double
			sr[i] = Double.parseDouble(v[1]);
			i++;
		}
		ptr = -1;
		results = model.query(new StreamElement(df, sr));
	}

	/**
	 * Returns true if there are more elements to be enumerated.
	 * 
	 * @return true if there are more elements, false otherwise
	 */
	@Override
	public boolean hasMoreElements() {
		return results != null && ptr < results.length - 1;
	}

	/**
	 * Returns the next element in the stream.
	 *
	 * @return the next element in the stream, or null if there are no more elements
	 * @throws RuntimeException if an error occurs while retrieving the next element
	 */
	@Override
	public StreamElement nextElement() throws RuntimeException {
		if (hasMoreElements()) {
			ptr = ptr + 1;
			return results[ptr];
		} else {
			return null;
		}
	}

	@Override
	public void close() {

	}

}
