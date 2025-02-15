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
* File: src/ch/epfl/gsn/http/datarequest/AggregationCriterion.java
*
* @author Timotee Maret
* @author Ali Salehi
* @author Milos Stojanovic
* @author Davide De Sclavis
* @author Manuel Buchauer
* @author Jan Beutel
*
*/

package ch.epfl.gsn.delivery.datarequest;

import java.util.Hashtable;

import ch.epfl.gsn.utils.Helpers;

/**
 * Represents an aggregation criterion used for data analysis or filtering.
 * This class extends the {@code AbstractCriterion} class.
 */
public class AggregationCriterion extends AbstractCriterion {

	private static Hashtable<String, String> allowedGroupOperator = null;

	static {
		allowedGroupOperator = new Hashtable<String, String>();
		allowedGroupOperator.put("max", "max");
		allowedGroupOperator.put("min", "min");
		allowedGroupOperator.put("avg", "avg");
		allowedGroupOperator.put("sum", "sum");
	}

	private String critTimeRange = null;
	private String critGroupOperator = null;

	/**
	 * <p>
	 * Create a new Aggregation Criteria from a serialized Aggregation description.
	 * The description must follow the syntax:<br />
	 * <code><timerange>:<groupoperator></code>
	 * </p>
	 * 
	 * @param inlinecrits
	 * @throws DataRequestException
	 */
	public AggregationCriterion(String inlinecrits) throws DataRequestException {

		String[] crits = inlinecrits.split(":");

		if (crits.length != 2) {
			throw new DataRequestException(GENERAL_ERROR_MSG + " >" + inlinecrits + "<.");
		}

		critTimeRange = crits[0];
		critGroupOperator = getCriterion(crits[1], allowedGroupOperator);
	}

	/**
	 * Returns a string representation of the AggregationCriterion object.
	 *
	 * @return A string representation of the AggregationCriterion object.
	 */
	public String toString() {
		return "Select: " + critGroupOperator.toUpperCase() + ", group by: timed/" + critTimeRange + " ("
				+ Helpers.formatTimePeriod(Long.parseLong(critTimeRange)) + ")";
	}

	public String getTimeRange() {
		return critTimeRange;
	}

	public String getGroupOperator() {
		return critGroupOperator;
	}
}