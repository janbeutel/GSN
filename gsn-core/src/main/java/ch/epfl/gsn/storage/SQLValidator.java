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
* File: src/ch/epfl/gsn/storage/SQLValidator.java
*
* @author Ali Salehi
* @author Timotee Maret
* @author Mehdi Riahi
* @author Julien Eberle
*
*/

package ch.epfl.gsn.storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

import org.slf4j.LoggerFactory;

import ch.epfl.gsn.VSensorStateChangeListener;
import ch.epfl.gsn.beans.DataField;
import ch.epfl.gsn.beans.VSensorConfig;

import org.slf4j.Logger;
import org.h2.command.CommandInterface;
import org.h2.command.Parser;
import org.h2.command.Prepared;
import org.h2.command.dml.Select;
import org.h2.engine.ConnectionInfo;
import org.h2.engine.Session;

public class SQLValidator implements VSensorStateChangeListener {

	private static final transient Logger logger = LoggerFactory.getLogger(SQLValidator.class);

	private Session session = null;
	private Connection connection;
	private static SQLValidator validator;

	public synchronized static SQLValidator getInstance() throws SQLException {
		if (validator == null) {
			validator = new SQLValidator();
		}

		return validator;
	}

	private SQLValidator() throws SQLException {
		Properties properties = new Properties();
		properties.put("user", "sa");
		properties.put("password", "");
		String URL = "jdbc:h2:mem:test";
		ConnectionInfo connInfo = new ConnectionInfo(URL, properties);

		// org.h2.engine.SessionRemote f= new org.h2.engine.SessionRemote(connInfo);
		session = org.h2.engine.Engine.getInstance().createSession(connInfo);
		// SessionFactoryEmbedded factory = new SessionFactoryEmbedded();
		// session = (Session) factory.createSession(connInfo);
		this.connection = DriverManager.getConnection(URL, properties);

		// This is only a workaround for queries containing 'UNIX_TIMESTAMP()' with no
		// parameter.
		// It does not return the same value as UNIX_TIMESTAMP() in MySQL returns!
		executeDDL("CREATE ALIAS UNIX_TIMESTAMP FOR \"java.lang.System.currentTimeMillis()\"");
	}

	public void executeDDL(String ddl) throws SQLException {
		CommandInterface command = session.prepareCommand(ddl, 0);
		command.executeUpdate();
	}

	/**
	 * Conditions to check.
	 * 1. No inner select.(done)
	 * 2. Be a select (done)
	 * 3. Valid fields (done)
	 * 4. Valid SQL (done)
	 * 5. Single Select without joins.(done)
	 * 6. Only using one table. (done)
	 * 7. No aggregation, groupby or having. (done)
	 * 8. no order by (done)
	 * 9. no limit keyword (done)
	 */
	public static String removeQuotes(String in) {
		return in.replaceAll("\"([^\"]|.)*\"", "");
	}

	/**
	 * Removes single quotes from a given string.
	 *
	 * @param in the input string
	 * @return the input string with single quotes removed
	 */
	public static String removeSingleQuotes(String in) {
		return in.replaceAll("'([^']|.)*'", "");
	}

	/**
	 * Checks if a given SQL query is valid.
	 *
	 * @param query the SQL query to be validated
	 * @return true if the query is valid, false otherwise
	 */
	private static boolean isValid(String query) {
		String simplified = removeSingleQuotes(removeQuotes(query)).toLowerCase().trim();
		if (simplified.lastIndexOf("select") != simplified.indexOf("select")) {
			return false;
		}
		if (simplified.indexOf("order by") > 0 || simplified.indexOf("group by") > 0 || simplified.indexOf("having") > 0
				|| simplified.indexOf("limit") > 0 || simplified.indexOf(";") > 0) {
			return false;
		}
		return true;
	}

	/**
	 * Adds "order by TIMED desc limit 1" to the given query string.
	 * 
	 * @param query the original query string
	 * @return the modified query string with the added clause
	 */
	public static String addTopFirst(String query) {
		return query + " order by TIMED desc limit 1";
	}

	/**
	 * Returns null if the validation fails. Returns the table name used in the
	 * query if the validation succeeds.
	 * 
	 * @param query to validate.
	 * @return Null if the validation fails. The name of the table if the validation
	 *         succeeds.
	 */
	public String validateQuery(String query) {
		Select select = queryToSelect(query);
		if (select == null) {
			return null;
		}

		if ((select.getTables().size() != 1) || (select.getTopFilters().size() != 1)
				|| select.isQuickAggregateQuery()) {
			return null;
		}

		return select.getTables().iterator().next().getName();
	}

	/**
	 * Extracts the select columns from the given SQL query based on the provided
	 * VSensorConfig.
	 *
	 * @param query         the SQL query
	 * @param vSensorConfig the VSensorConfig containing the output structure
	 * @return an array of DataField objects representing the select columns
	 */
	public DataField[] extractSelectColumns(String query, VSensorConfig vSensorConfig) {
		Select select = queryToSelect(query);
		if (select == null) {
			return new DataField[0];
		}

		return getFields(select, vSensorConfig.getOutputStructure());
	}

	/**
	 * Extracts the select columns from the given SQL query based on the provided
	 * data fields.
	 * 
	 * @param query      the SQL query
	 * @param datafields the array of data fields
	 * @return an array of data fields representing the select columns
	 */
	public DataField[] extractSelectColumns(String query, DataField[] datafields) {
		Select select = queryToSelect(query);
		if (select == null) {
			return new DataField[0];
		}

		return getFields(select, datafields);
	}

	public Connection getSampleConnection() {
		return connection;
	}

	public boolean vsLoading(VSensorConfig config) {
		return false;
	}

	public boolean vsUnLoading(VSensorConfig config) {
		return false;
	}

	/**
	 * Retrieves the data fields from the given Select object based on the provided
	 * array of fields.
	 * 
	 * @param select The Select object from which to retrieve the data fields.
	 * @param fields The array of DataField objects to match against the column
	 *               names in the Select object.
	 * @return An array of DataField objects representing the matched fields,
	 *         excluding "timed" and "pk" columns.
	 */
	private DataField[] getFields(Select select, DataField[] fields) {
		ArrayList<DataField> toReturn = new ArrayList<DataField>();
		try {
			for (int i = 0; i < select.getColumnCount(); i++) {
				String name = select.queryMeta().getColumnName(i);
				if (name.equalsIgnoreCase("timed") || name.equalsIgnoreCase("pk")) {
					continue;
				}
				String gsnType = null;
				for (int j = 0; j < fields.length; j++) {
					if (fields[j].getName().equalsIgnoreCase(name)) {
						gsnType = fields[j].getType();
						toReturn.add(new DataField(name, gsnType));
						break;
					}
				}
			}
			return toReturn.toArray(new DataField[] {});
		} catch (Exception e) {
			if(logger.isDebugEnabled()){
				logger.debug(e.getMessage(), e);
			}
			return new DataField[0];
		}

	}

	/**
	 * Converts a SQL query string into a Select object.
	 * 
	 * @param query the SQL query string to convert
	 * @return the Select object representing the query, or null if the query is
	 *         invalid
	 */
	private Select queryToSelect(String query) {
		Select select = null;
		if (!isValid(query)) {
			return null;
		}

		Parser parser = new Parser(session);
		Prepared somePrepared;
		// try {
		somePrepared = parser.prepare(query);
		if (somePrepared instanceof Select && somePrepared.isQuery()) {
			select = (Select) somePrepared;
		}

		/*
		 * } catch (SQLException e) {
		 * logger.debug(e.getMessage(),e);
		 * }
		 */
		return select;
	}

	/**
	 * Adds a primary key field to the given SQL query if it does not already have
	 * one.
	 * 
	 * @param query the SQL query to modify
	 * @return the modified SQL query with a primary key field added if necessary
	 */
	public static String addPkField(String query) {
		if(logger.isDebugEnabled()){
			logger.debug("< QUERY IN: " + query);
		}
		try {
			SQLValidator sv = getInstance();
			Select select = sv.queryToSelect(query);
			if (select == null) {
				return query;
			}
			boolean hasPk = false;
			boolean hasWildCard = false;
			for (int i = 0; i < select.getColumnCount(); i++) {
				String name = select.queryMeta().getColumnName(i);
				if (name.equalsIgnoreCase("*")) {
					hasWildCard = true;
					break;
				}
				if (name.equalsIgnoreCase("pk")) {
					hasPk = true;
					break;
				}
			}
			//
			if (!hasPk && !hasWildCard) {
				int is = query.toUpperCase().indexOf("SELECT");
				query = new StringBuilder(query.substring(is, is + 6))
						.append(" pk, ")
						.append(query.substring(is + 7)).toString();
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		if(logger.isDebugEnabled()){
			logger.debug("> QUERY OUT: " + query);
		}
		return query;
	}

	/**
	 * Releases the connection to the database.
	 * If the connection is not null and not closed, it will be closed.
	 *
	 * @throws Exception if an error occurs while closing the connection.
	 */
	public void release() throws Exception {
		if (connection != null && !connection.isClosed()) {
			connection.close();
		}

	}

}
