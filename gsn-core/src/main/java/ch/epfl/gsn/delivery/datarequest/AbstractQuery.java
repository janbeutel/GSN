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
* File: src/ch/epfl/gsn/http/datarequest/AbstractQuery.java
*
* @author Timotee Maret
* @author Milos Stojanovic
*
*/

package ch.epfl.gsn.delivery.datarequest;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.ArrayList;

/**
 * The AbstractQuery class represents a query object used for data retrieval.
 * It provides methods to construct and manipulate the query.
 */
public class AbstractQuery {

	// private StringBuilder standardQuery = null;
	private String[] fields;
	private LimitCriterion limitCriterion;
	private AggregationCriterion aggregation;
	private String vsName;
	private ArrayList<StandardCriterion> criteria;

	private static transient Logger logger = LoggerFactory.getLogger(AbstractQuery.class);

	/**
	 * Constructs a new AbstractQuery object with the specified parameters.
	 *
	 * @param limitCriterion the limit criterion for the query
	 * @param aggregation    the aggregation criterion for the query
	 * @param vsname         the name of the virtual schema
	 * @param fields         the fields to be included in the query result
	 * @param criteria       the standard criteria for the query
	 */
	public AbstractQuery(LimitCriterion limitCriterion, AggregationCriterion aggregation, String vsname,
			String[] fields, ArrayList<StandardCriterion> criteria) {
		// this.standardQuery = standardQuery;
		this.limitCriterion = limitCriterion;
		this.aggregation = aggregation;
		this.vsName = vsname;
		this.fields = fields;
		this.criteria = criteria;
	}

	/**
	 * Constructs and returns the standard query based on the specified criteria and
	 * fields.
	 *
	 * @return the standard query as a StringBuilder object
	 */
	public StringBuilder getStandardQuery() {
		// Standard Criteria
		StringBuilder partStandardCriteria = new StringBuilder();
		if (criteria != null) {
			StandardCriterion lastStandardCriterionLinkedToVs = null;
			StandardCriterion cc;
			for (int i = 0; i < criteria.size(); i++) {
				cc = criteria.get(i);
				if (cc.getVsname().compareTo("") == 0 || cc.getVsname().compareToIgnoreCase(vsName) == 0) {

					if (lastStandardCriterionLinkedToVs == null) {
						partStandardCriteria
								.append(cc.getNegation() + " " + cc.getField() + " " + cc.getOperator() + " ");
					} else {
						partStandardCriteria.append(lastStandardCriterionLinkedToVs.getCritJoin() + " "
						+ cc.getNegation() + " " + cc.getField() + " " + cc.getOperator() + " ");
					}

					lastStandardCriterionLinkedToVs = cc;

					if (cc.getOperator().compareToIgnoreCase("like") == 0) {
						partStandardCriteria.append("'%");
					}

					partStandardCriteria.append(cc.getValue());

					if (cc.getOperator().compareToIgnoreCase("like") == 0) {
						partStandardCriteria.append("%'");
					}
					partStandardCriteria.append(" ");
				}
			}
			if (lastStandardCriterionLinkedToVs != null) {
				partStandardCriteria.insert(0, "where ");
			}
		}

		StringBuilder partFields = new StringBuilder();
		for (int i = 0; i < fields.length; i++) {
			if (partFields.length() > 0) {
				partFields.append(", ");
			}
			if (aggregation != null) {
				if ("timed".equals(fields[i])) {
					partFields.append("max(");
				} else {
					partFields.append(aggregation.getGroupOperator() + "(");
				}
			}
			partFields.append(fields[i]);
			if (aggregation != null) {
				partFields.append(") as " + fields[i]);
			}

		}

		if (aggregation == null) {
			partFields.append(" ");
		} else {
			if (partFields.length() > 0) {
				partFields.append(", ");
			}
			partFields.append("floor(timed/" + aggregation.getTimeRange() + ") as aggregation_interval ");
		}

		// Build a final query
		StringBuilder sqlQuery = new StringBuilder();
		sqlQuery.append("select ");
		sqlQuery.append(partFields);
		sqlQuery.append("from ").append(vsName).append(" ");
		sqlQuery.append(partStandardCriteria);
		if (aggregation == null) {
			sqlQuery.append("order by timed desc ");
		} else {
			sqlQuery.append("group by aggregation_interval desc ");
		}
		if(logger.isDebugEnabled()){
			logger.debug("SQL Query built >" + sqlQuery.toString() + "<");
		}
		return sqlQuery;
	}

	/*
	 * public void setStandardQuery(StringBuilder standardQuery) {
	 * this.standardQuery = standardQuery;
	 * }
	 */

	/**
	 * Returns the limit criterion for the query.
	 *
	 * @return the limit criterion
	 */
	public LimitCriterion getLimitCriterion() {
		return limitCriterion;
	}

	/**
	 * Sets the limit criterion for the query.
	 *
	 * @param limitCriterion the limit criterion to be set
	 */
	public void setLimitCriterion(LimitCriterion limitCriterion) {
		this.limitCriterion = limitCriterion;
	}

	/**
	 * Returns the aggregation criterion for the query.
	 *
	 * @return the aggregation criterion
	 */
	public AggregationCriterion getAggregation() {
		return aggregation;
	}

	/**
	 * Sets the aggregation criterion for the query.
	 *
	 * @param aggregation the aggregation criterion to be set
	 */
	public void setAggregation(AggregationCriterion aggregation) {
		this.aggregation = aggregation;
	}

	/**
	 * Returns the name of the virtual schema.
	 *
	 * @return the name of the virtual schema
	 */
	public String getVsName() {
		return vsName;
	}

	/**
	 * Sets the name of the virtual schema.
	 *
	 * @param vsName the name of the virtual schema to be set
	 */
	public void setVsName(String vsName) {
		this.vsName = vsName;
	}

	/**
	 * Updates the specified standard criterion in the query.
	 *
	 * @param criterion the standard criterion to be updated
	 */
	public void updateCriterion(StandardCriterion criterion) {
		int index = criteria.indexOf(criterion);
		if (index == -1) {
			criteria.add(criterion);
		} else {
			criteria.set(index, criterion);
		}

	}

	/**
	 * Returns the list of standard criteria for the query.
	 *
	 * @return the list of standard criteria
	 */
	public ArrayList<StandardCriterion> getCriteria() {
		return criteria;
	}

	/**
	 * Sets the list of standard criteria for the query.
	 *
	 * @param criteria the list of standard criteria to be set
	 */
	public void setCriteria(ArrayList<StandardCriterion> criteria) {
		this.criteria = criteria;
	}

	/**
	 * Adds a new field to the query.
	 *
	 * @param fieldName the name of the field to be added
	 */
	public void addField(String fieldName) {
		String[] newFields = new String[fields.length + 1];
		System.arraycopy(fields, 0, newFields, 1, fields.length);
		fields = newFields;
		fields[0] = fieldName;
	}

	/**
	 * Returns the fields included in the query.
	 *
	 * @return the fields included in the query
	 */
	public String[] getFields() {
		return fields;
	}

	/**
	 * Sets the fields included in the query.
	 *
	 * @param fields the fields to be set
	 */
	public void setFields(String[] fields) {
		this.fields = fields;
	}
}
