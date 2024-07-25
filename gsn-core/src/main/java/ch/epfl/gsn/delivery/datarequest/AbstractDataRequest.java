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
* File: src/ch/epfl/gsn/http/datarequest/AbstractDataRequest.java
*
* @author Ali Salehi
* @author Timotee Maret
* @author Davide De Sclavis
* @author Manuel Buchauer
* @author Jan Beutel
*
*/

package ch.epfl.gsn.delivery.datarequest;

import java.io.OutputStream;
import java.util.Map;

/**
 * <p>
 * This class provides a generic and fine grained way to select data for a set
 * of Virtual Sensors.
 * For each of the specified Virtual Sensors it creates a SQL query that can be
 * directly executed to
 * access the data.
 * </p>
 * <p>
 * For each Virtual Sensor, the Fields can be selected. Moreover, The three
 * following types of filters
 * can be added to the queries. Notice that these filters are the same for all
 * the generated queries.
 * </p>
 * 
 * <ul>
 * <li><strong>MAX NUMBER OF RESULTS</strong> This option limits the number of
 * returned values to a maximal value.</li>
 * <li><strong>STANDARD CRITERIA</strong> Almost the SQL tests can be added to
 * the SQL queries.</li>
 * <li><strong>AGGREGATION CRITERION</strong> A SQL grouping function can be
 * added to the queries.</li>
 * </ul>
 * 
 * <h3>Examples</h3>
 * <ul>
 * <li><strong>Minimal Parameters:</strong>
 * <code>?vsname=ss_mem_vs:heap_memory_usage</code><br />
 * This request return a SQL query that select all the
 * <code>heap_memory_usage</code> values from the <code>ss_me_vs</code> Virtual
 * Sensor</li>
 * <li>
 * <strong>Typical Parameters:</strong>
 * <code>?vsname=tramm_meadows_vs:toppvwc_1:toppvwc_3&vsname=ss_mem_vs:heap_memory_usage&nb=0:5&critfield=and:::timed:ge:1201600800000&critfield=and:::timed:le:1211678800000&groupby=10000000:min</code><br
 * />
 * This request returns two SQL queries, one for <code>tramm_meadows_vs</code>
 * and one for <code>ss_mem_vs</code> Virtual Sensor. The number of elements
 * returned is limited to 5, are associated to a timestamp between
 * <code>1201600800000</code> and <code>1211678800000</code>.
 * The elements returned are the minimals values grouped by the timed field
 * divided by <code>10000000</code>.
 * </li>
 * </ul>
 * 
 * <ul>
 * <li>Notice that by the <code>timed</code> Field is associated to all the
 * Virtual Sensors elements returned.</li>
 * <li>Notice that this class doesn't check if the Virtual Sensors and Fields
 * names are corrects.</li>
 * </ul>
 */
public abstract class AbstractDataRequest {

	protected QueriesBuilder qbuilder = null;

	protected Map<String, String[]> requestParameters = null;

	/**
	 * Constructs a new AbstractDataRequest object with the specified request
	 * parameters.
	 *
	 * @param requestParameters the request parameters to be used for processing the
	 *                          data request
	 * @throws DataRequestException if an error occurs during the construction of
	 *                              the data request
	 */
	public AbstractDataRequest(Map<String, String[]> requestParameters) throws DataRequestException {
		this.requestParameters = requestParameters;
		qbuilder = new QueriesBuilder(requestParameters);
	}

	/**
	 * Gets the QueriesBuilder object associated with this data request.
	 *
	 * @return the QueriesBuilder object
	 */
	public QueriesBuilder getQueryBuilder() {
		return qbuilder;
	}

	/**
	 * Processes the data request.
	 *
	 * @throws DataRequestException if an error occurs during the processing of the
	 *                              data request
	 */
	public abstract void process() throws DataRequestException;

	/**
	 * Outputs the result of the data request to the specified output stream.
	 *
	 * @param os the output stream to write the result to
	 */
	public abstract void outputResult(OutputStream os);
}
