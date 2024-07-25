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
* File: src/ch/epfl/gsn/http/datarequest/StandardCriterion.java
*
* @author Ali Salehi
* @author Timotee Maret
* @author Davide De Sclavis
* @author Manuel Buchauer
* @author Jan Beutel
*
*/

package ch.epfl.gsn.delivery.datarequest;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;

import ch.epfl.gsn.Main;

public class StandardCriterion extends AbstractCriterion {

	private static Hashtable<String, String> allowedOp = null;
	private static Hashtable<String, String> allowedJoin = null;
	private static Hashtable<String, String> allowedNeg = null;

	private String critJoin = null;
	private String critNeg = null;
	private String critField = null;
	private String critVsname = null;
	private String critValue = null;
	private String critOperator = null;

	private static SimpleDateFormat sdf = new SimpleDateFormat(Main.getContainerConfig().getTimeFormat());

	static {
		allowedOp = new Hashtable<String, String>();
		allowedOp.put("le", "<");
		allowedOp.put("leq", "<=");
		allowedOp.put("ge", ">");
		allowedOp.put("geq", ">=");
		allowedOp.put("eq", " equal");
		allowedOp.put("like", "like");
		//
		allowedJoin = new Hashtable<String, String>();
		allowedJoin.put("or", "or");
		allowedJoin.put("and", "and");
		//
		allowedNeg = new Hashtable<String, String>();
		allowedNeg.put("", "");
		allowedNeg.put("not", "not");
	}

	public StandardCriterion() {
	}

	/**
	 * <p>
	 * Create a new Custom Criteria from a serialized Criteria description.
	 * The description must follow the syntax:<br />
	 * <code><critJoin>:<negation>:<vsname>:<field>:<operator>:<value></code>.
	 * 
	 * Note that if <vsname> is blank then the criteria applies to the field of all
	 * virtual sensors.
	 * </p>
	 * 
	 * @param inlinecrits
	 * @return
	 */
	public StandardCriterion(String inlinecrits) throws DataRequestException {

		String[] crits = inlinecrits.split(":");

		if (crits.length != 6) {
			throw new DataRequestException(GENERAL_ERROR_MSG + " >" + inlinecrits + "<.");
		}

		critJoin = getCriterion(crits[0], allowedJoin);
		critNeg = getCriterion(crits[1], allowedNeg);
		critVsname = crits[2];
		critField = crits[3];
		critOperator = getCriterion(crits[4], allowedOp);
		critValue = crits[5];
	}

	public String toString() {
		String hrtf = critField.compareToIgnoreCase("timed") == 0 ? sdf.format(new Date(Long.parseLong(critValue)))
				: critValue;
		return critJoin + " " + critNeg + " " + critVsname + " " + critField + " " + critOperator + " " + hrtf;
	}

	public String getCritJoin() {
		return this.critJoin;
	}

	public String getNegation() {
		return this.critNeg;
	}

	public String getVsname() {
		return this.critVsname;
	}

	public String getField() {
		return this.critField;
	}

	public String getValue() {
		return this.critValue;
	}

	public String getOperator() {
		return this.critOperator;
	}

	public void setCritJoin(String critJoin) {
		this.critJoin = critJoin;
	}

	public void setCritNeg(String critNeg) {
		this.critNeg = critNeg;
	}

	public void setCritField(String critField) {
		this.critField = critField;
	}

	public void setCritVsname(String critVsname) {
		this.critVsname = critVsname;
	}

	public void setCritValue(String critValue) {
		this.critValue = critValue;
	}

	public void setCritOperator(String critOperator) {
		this.critOperator = critOperator;
	}

	/**
	 * Compares this StandardCriterion object with another object for equality.
	 * Two StandardCriterion objects are considered equal if their critField,
	 * critJoin,
	 * critNeg, critOperator, and critVsname fields are equal.
	 *
	 * @param o The object to compare with this StandardCriterion.
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

		StandardCriterion that = (StandardCriterion) o;

		if (critField == null ? that.critField != null : !critField.equals(that.critField)) {
			return false;
		}
		if (critJoin == null ?  that.critJoin != null : !critJoin.equals(that.critJoin)) {
			return false;
		}
		if (critNeg == null ?  that.critNeg != null : !critNeg.equals(that.critNeg)) {
			return false;
		}
		if (critOperator == null ? that.critOperator != null :!critOperator.equals(that.critOperator)) {
			return false;
		}
		if (critVsname == null ?  that.critVsname != null : !critVsname.equals(that.critVsname)) {
			return false;
		}

		return true;
	}

	/**
	 * Generates a hash code value for this StandardCriterion object.
	 * The hash code is calculated based on the critJoin, critNeg, critField,
	 * critVsname, and critOperator fields of the object.
	 *
	 * @return The hash code value for this StandardCriterion object.
	 */
	@Override
	public int hashCode() {
		int result = critJoin == null ? 0 : critJoin.hashCode();
		result = 31 * result + (critNeg == null ? 0 : critNeg.hashCode());
		result = 31 * result + (critField == null ? 0 : critField.hashCode());
		result = 31 * result + (critVsname == null ? 0 : critVsname.hashCode());
		result = 31 * result + (critOperator == null ? 0 : critOperator.hashCode());
		return result;
	}
}
