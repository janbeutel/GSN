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
* File: src/ch/epfl/gsn/storage/DataEnumerator.java
*
* @author parobert
* @author Ali Salehi
* @author Mehdi Riahi
* @author Timotee Maret
* @author Sofiane Sarni
* @author Julien Eberle
* @author Milos Stojanovic
* @author Davide De Sclavis
* @author Manuel Buchauer
* @author Jan Beutel
*
*/

package ch.epfl.gsn.storage;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import org.slf4j.LoggerFactory;

import ch.epfl.gsn.beans.DataTypes;
import ch.epfl.gsn.beans.StreamElement;

import org.slf4j.Logger;

/**
 * FIXME : 1. Because a prepared statements relies on the connection being
 * there. If a connection times out the pool will restore it and then the
 * prepared statement is then stale. The only way you can tell this it to use
 * it, and any speed advantage is negated when you have to try it, catch the
 * error, regen the statement and go again. 2. You use a connection pool in
 * threaded enviroment. A prepared statement is a single instance that can only
 * be used in a single thread. So either you block all the threads waiting for
 * the single prepared statement (and then you have to wonder why you are using
 * threads) or you have multiple prepared statements for same thing.
 */
public class DataEnumerator implements DataEnumeratorIF {

	private transient Logger logger = LoggerFactory.getLogger(DataEnumerator.class);

	private ResultSet resultSet = null;

	private String[] dataFieldNames;

	private Byte[] dataFieldTypes;

	private boolean hasNext = false;

	boolean hasTimedFieldInResultSet = false;

	int indexOfTimedField = -1;

	int indexofPK = -1;

	boolean linkBinaryData = false;

	private StorageManager storageManager = null;

	private boolean manualCloseConnection;
	private boolean hadError = false;
	private StreamElement streamElement = null;

	/**
	 * Creats an empty data enumerator.
	 */
	public DataEnumerator() {
		hasNext = false;
	}

	public boolean IsNull() {
		return resultSet == null;
	}

	public DataEnumerator(StorageManager storageManager, PreparedStatement preparedStatement, boolean binaryLinked) {
		this(storageManager, preparedStatement, binaryLinked, false);
	}

	/**
	 * Constructs a DataEnumerator object for iterating over query results from the
	 * specified PreparedStatement.
	 *
	 * This constructor initializes a DataEnumerator object for iterating over the
	 * results of the provided
	 * PreparedStatement. It associates the DataEnumerator with the given
	 * StorageManager and sets the manualCloseConnection
	 * flag accordingly.
	 * 
	 * @param storageManager    The StorageManager instance associated with the
	 *                          DataEnumerator.
	 * @param preparedStatement The PreparedStatement containing the query results
	 *                          to iterate over.
	 * @param binaryLinked      A boolean indicating whether binary data is linked.
	 * @param manualClose       A boolean indicating whether the connection should
	 *                          be manually closed.
	 */
	public DataEnumerator(StorageManager storageManager, PreparedStatement preparedStatement, boolean binaryLinked,
			boolean manualClose) {
		this.storageManager = storageManager;
		this.manualCloseConnection = manualClose;
		if (preparedStatement == null) {
			if(logger.isDebugEnabled()){
				logger.debug(new StringBuilder().append("resultSetToStreamElements").append(" is supplied with null input.")
					.toString());
			}
			hasNext = false;
			return;
		}

		// try {
		// preparedStatement.setFetchSize(50);
		// } catch (SQLException e1) {
		// logger.warn(e1.getMessage(),e1);
		// return;
		// }

		this.linkBinaryData = binaryLinked;
		Vector<String> fieldNames = new Vector<String>();
		Vector<Byte> fieldTypes = new Vector<Byte>();
		try {
			this.resultSet = preparedStatement.executeQuery();
			hasNext = resultSet.next();
			// Initializing the fieldNames and fieldTypes.
			// Also setting the values for <code> hasTimedFieldInResultSet</code>
			// if the timed field is present in the result set.
			String tableName = null;
			int problematicColumn = -1;
			for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
				if (i == 1) {
					tableName = resultSet.getMetaData().getTableName(1);
				}

				String colName = resultSet.getMetaData().getColumnLabel(i);
				int colTypeInJDBCFormat = resultSet.getMetaData().getColumnType(i);
				int colScale = resultSet.getMetaData().getScale(i);
				if (colName.equalsIgnoreCase("PK")) {
					indexofPK = i;
				} else if (colName.equalsIgnoreCase("timed")) {
					indexOfTimedField = i;
				} else {
					fieldNames.add(colName);
					byte gsnType = storageManager.convertLocalTypeToGSN(colTypeInJDBCFormat, colScale);
					if (gsnType == -100) {
						logger.error("The type can't be converted to GSN form - error description: ");
						logger.warn("Table name: " + tableName);
						logger.warn("Column name: " + colName);
						logger.warn("Column type name: " + resultSet.getMetaData().getColumnTypeName(i));
						logger.warn("Query result: " + preparedStatement.toString());
						problematicColumn = i;
					}
					fieldTypes.add(gsnType);
				}
			}
			if (problematicColumn != -1) {
				while (true) {
					logger.warn("Values of the column: " + resultSet.getObject(problematicColumn));
					if (resultSet.isLast()) {
						break;
					}
					resultSet.next();
				}
			}
			dataFieldNames = fieldNames.toArray(new String[] {});
			dataFieldTypes = fieldTypes.toArray(new Byte[] {});
			if (indexofPK == -1 && linkBinaryData) {
				throw new RuntimeException("The specified query can't be used with binaryLinked paramter set to true.");
			}
		} catch (Exception e) {
			logger.error("Trying to create DataEnumerator with:\n" + preparedStatement.toString());
			logger.error(e.getMessage(), e);
			hasNext = false;
		} finally {
			if (!hasNext) {
				close();
			}
		}
	}

	

	public boolean hasMoreElements() {
		return hasNext;
	}

	/**
	 * Returns the next stream element or > IndexOutOfBoundsException("The
	 * resultset doesn't have anymore elements or closed.")<
	 */
	public StreamElement nextElement() throws RuntimeException {
		if (!hasNext) {
			throw new IndexOutOfBoundsException("The resultset doesn't have anymore elements or closed.");
		}
		long timestamp = -1;
		long pkValue = -1;
		try {
			if (indexofPK != -1) {
				pkValue = resultSet.getLong(indexofPK);
			}
			Serializable[] output = new Serializable[dataFieldNames.length];
			for (int actualColIndex = 1,
					innerIndex = 0; actualColIndex <= resultSet.getMetaData().getColumnCount(); actualColIndex++) {
				if (actualColIndex == indexOfTimedField) {
					timestamp = resultSet.getLong(actualColIndex);
					continue;
				} else if (actualColIndex == indexofPK) {
					continue;
				} else {
					switch (dataFieldTypes[innerIndex]) {
						case DataTypes.VARCHAR:
						case DataTypes.CHAR:
							output[innerIndex] = resultSet.getString(actualColIndex);
							break;
						case DataTypes.INTEGER:
							output[innerIndex] = resultSet.getInt(actualColIndex);
							break;
						case DataTypes.TINYINT:
							output[innerIndex] = resultSet.getByte(actualColIndex);
							break;
						case DataTypes.SMALLINT:
							output[innerIndex] = resultSet.getShort(actualColIndex);
							break;
						case DataTypes.DOUBLE:
							output[innerIndex] = resultSet.getDouble(actualColIndex);
							break;
						case DataTypes.FLOAT:
							output[innerIndex] = resultSet.getFloat(actualColIndex);
							break;
						case DataTypes.BIGINT:
							output[innerIndex] = resultSet.getLong(actualColIndex);
							break;
						case DataTypes.BINARY:
							if (linkBinaryData) {
								output[innerIndex] = "field?vs=" + resultSet.getMetaData().getTableName(actualColIndex)
										+ "&amp;field=" + resultSet.getMetaData().getColumnLabel(actualColIndex)
										+ "&amp;pk=" + pkValue;
								resultSet.getBytes(actualColIndex);
							} else {
								output[innerIndex] = resultSet.getBytes(actualColIndex);
							}
							break;
						default:
							break;
					}
					if (resultSet.wasNull()) {
						output[innerIndex] = null;
					}

					innerIndex++;
				}
			}
			streamElement = new StreamElement(dataFieldNames, dataFieldTypes, output,
					indexOfTimedField == -1 ? System.currentTimeMillis() : timestamp);
			if (indexofPK != -1) {
				streamElement.setInternalPrimayKey(pkValue);
			}
			hasNext = resultSet.next();
			if (!hasNext) {
				close();
			}

		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
			close();
		}
		return streamElement; // BUG -> if a SQLException occurs, the last stream element will be returned.
	}

	/**
	 * Closes the DataEnumerator and releases any associated resources.
	 * If a ResultSet is present, it closes the ResultSet, Statement, and
	 * Connection.
	 * If manualCloseConnection is set to true, only the ResultSet is closed.
	 */
	public void close() {
		this.hasNext = false;
		if (resultSet == null) {
			return;
		}

		try {
			if (!manualCloseConnection && resultSet.getStatement() != null) {
				java.sql.Statement s = resultSet.getStatement();
				java.sql.Connection c = s.getConnection();
				storageManager.close(resultSet);
				storageManager.closeStatement(s);
				storageManager.close(c);
				resultSet = null;
			} else {
				try {
					resultSet.close();
				} catch (SQLException e) {
					if(logger.isDebugEnabled()){
						logger.debug(e.getMessage(), e);
					}
				}
			}

		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}
	}

	public boolean hadError() {
		return hadError;
	}

}
