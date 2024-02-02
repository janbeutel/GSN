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
* File: src/ch/epfl/gsn/storage/db/H2StorageManager.java
*
* @author Timotee Maret
* @author Mehdi Riahi
*
*/

package ch.epfl.gsn.storage.db;

import org.slf4j.LoggerFactory;

import ch.epfl.gsn.beans.DataField;
import ch.epfl.gsn.beans.DataTypes;
import ch.epfl.gsn.storage.StorageManager;

import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

public class H2StorageManager extends StorageManager {

    private static final transient Logger logger = LoggerFactory.getLogger(H2StorageManager.class);

    public H2StorageManager() {
        super();
        this.isH2 = true;
    }

    @Override
    public String getJDBCPrefix() {
        return "jdbc:h2:";
    }

    /**
     * Converts the GSN type to the corresponding local type in the H2 database.
     * 
     * @param gsnType The GSN type to be converted.
     * @return The converted local type in the H2 database.
     */
    @Override
    public String convertGSNTypeToLocalType(DataField gsnType) {
        String convertedType = null;
        switch (gsnType.getDataTypeID()) {
            case DataTypes.CHAR:
            case DataTypes.VARCHAR:
                // Because the parameter for the varchar is not
                // optional.
                if (gsnType.getType().trim().equalsIgnoreCase("string")) {
                    convertedType = "TEXT";
                } else {
                    convertedType = gsnType.getType();
                }

                break;
            case DataTypes.FLOAT:
                convertedType = "REAL"; // Warning! The type FLOAT in H2 is a synonym of DOUBLE !!
                break;
            default:
                convertedType = DataTypes.TYPE_NAMES[gsnType.getDataTypeID()];
                break;
        }
        return convertedType;
    }

    /**
     * Converts a local JDBC type to the corresponding GSN data type.
     * 
     * @param jdbcType  the local JDBC type to be converted
     * @param precision the precision of the JDBC type
     * @return the corresponding GSN data type
     */
    @Override
    public byte convertLocalTypeToGSN(int jdbcType, int precision) {
        switch (jdbcType) {
            case Types.BIGINT:
                return DataTypes.BIGINT;
            case Types.INTEGER:
                return DataTypes.INTEGER;
            case Types.SMALLINT:
                return DataTypes.SMALLINT;
            case Types.TINYINT:
                return DataTypes.TINYINT;
            case Types.VARCHAR:
                return DataTypes.VARCHAR;
            case Types.CHAR:
                return DataTypes.CHAR;
            case Types.DOUBLE:
            case Types.DECIMAL: // This is needed for doing aggregates in datadownload servlet.
                return DataTypes.DOUBLE;
            case Types.REAL: // Warning! The type FLOAT in H2 is a synonym of DOUBLE !!
                return DataTypes.FLOAT;
            case Types.BINARY:
            case Types.BLOB:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                return DataTypes.BINARY;
            default:
                logger.error("The type can't be converted to GSN form : " + jdbcType);
                break;
        }
        return -100;
    }

    /**
     * Returns the SQL statement for dropping an index.
     *
     * @return the SQL statement for dropping an index
     */
    @Override
    public String getStatementDropIndex() {
        return "DROP INDEX #NAME";
    }

    /**
     * Returns the SQL statement for dropping a view.
     *
     * @return the SQL statement for dropping a view
     */
    @Override
    public String getStatementDropView() {
        return "DROP VIEW #NAME IF EXISTS";
    }

    @Override
    public int getTableNotExistsErrNo() {
        return 42102;
    }

    /**
     * Adds a LIMIT and OFFSET clause to the given SQL query.
     *
     * @param query  the original SQL query
     * @param limit  the maximum number of rows to return
     * @param offset the number of rows to skip before starting to return rows
     * @return the modified SQL query with the LIMIT and OFFSET clauses added
     */
    @Override
    public String addLimit(String query, int limit, int offset) {
        return query + " LIMIT " + limit + " OFFSET " + offset;
    }

    /**
     * Initializes the database access and sets up necessary configurations.
     * This method disables referential integrity, creates an alias for the
     * current system time in milliseconds, and calls the superclass method
     * to perform additional initialization steps.
     *
     * @param con the database connection
     * @throws Exception if an error occurs during initialization
     */
    @Override
    public void initDatabaseAccess(Connection con) throws Exception {
        Statement stmt = con.createStatement();
        stmt.execute("SET REFERENTIAL_INTEGRITY FALSE");
        stmt.execute("CREATE ALIAS IF NOT EXISTS NOW_MILLIS FOR \"java.lang.System.currentTimeMillis\";");
        super.initDatabaseAccess(con);
    }

    /**
     * Returns the statement difference time in milliseconds.
     *
     * @return the statement difference time in milliseconds
     */
    @Override
    public String getStatementDifferenceTimeInMillis() {
        return "call NOW_MILLIS()";
    }

    /**
     * Generates a StringBuilder containing a SQL statement to drop a table if it
     * exists.
     * The statement is constructed based on the provided table name.
     *
     * @param tableName The name of the table to be dropped.
     * @param conn      The database connection used to execute the statement.
     * @return A StringBuilder containing the SQL statement to drop the specified
     *         table if it exists.
     * @throws SQLException If a database access error occurs or the SQL statement
     *                      is invalid.
     */
    @Override
    public StringBuilder getStatementDropTable(CharSequence tableName, Connection conn) throws SQLException {
        StringBuilder sb = new StringBuilder("Drop table if exists ");
        sb.append(tableName);
        return sb;
    }

    /**
     * Returns a StringBuilder object that represents the SQL statement for creating
     * a table in the database.
     *
     * @param tableName the name of the table
     * @param structure an array of DataField objects representing the structure of
     *                  the table
     * @return a StringBuilder object containing the SQL statement for creating the
     *         table
     */
    @Override
    public StringBuilder getStatementCreateTable(String tableName, DataField[] structure) {
        StringBuilder result = new StringBuilder("CREATE TABLE ").append(tableName);
        result.append(" (PK BIGINT NOT NULL IDENTITY, timed BIGINT NOT NULL, ");
        for (DataField field : structure) {
            if (field.getName().equalsIgnoreCase("pk") || field.getName().equalsIgnoreCase("timed")) {
                continue;
            }
            result.append(field.getName().toUpperCase()).append(' ');
            result.append(convertGSNTypeToLocalType(field));
            result.append(" ,");
        }
        result.delete(result.length() - 2, result.length());
        result.append(")");
        return result;
    }

    /**
     * Generates a StringBuilder containing a SQL statement to remove useless data
     * from a table.
     * The statement is constructed based on the provided virtual sensor name and
     * storage size.
     *
     * @param virtualSensorName The name of the virtual sensor table.
     * @param storageSize       The size of the storage to retain in the table.
     * @return A StringBuilder containing the SQL statement to remove useless data
     *         from the specified table.
     */
    @Override
    public StringBuilder getStatementUselessDataRemoval(String virtualSensorName, long storageSize) {
        return new StringBuilder()
                .append("delete from ")
                .append(virtualSensorName)
                .append(" where ")
                .append(virtualSensorName)
                .append(".timed not in ( select ")
                .append(virtualSensorName)
                .append(".timed from ")
                .append(virtualSensorName)
                .append(" order by ")
                .append(virtualSensorName)
                .append(".timed DESC  LIMIT  ")
                .append(storageSize)
                .append(" offset 0 )");
    }

    /**
     * Generates a StringBuilder containing a SQL statement to remove useless data
     * from a table based on a count threshold.
     * The statement is constructed using the provided virtual sensor name and
     * storage size.
     *
     * @param virtualSensorName The name of the virtual sensor table.
     * @param storageSize       The count-based storage size threshold for retaining
     *                          data in the table.
     * @return A StringBuilder containing the SQL statement to remove useless data
     *         based on the count from the specified table.
     */
    @Override
    public StringBuilder getStatementRemoveUselessDataCountBased(String virtualSensorName, long storageSize) {
        return new StringBuilder()
                .append("delete from ")
                .append(virtualSensorName)
                .append(" where ")
                .append(virtualSensorName)
                .append(".timed not in ( select ")
                .append(virtualSensorName)
                .append(".timed from ")
                .append(virtualSensorName)
                .append(" order by ")
                .append(virtualSensorName)
                .append(".timed DESC  LIMIT  ")
                .append(storageSize).append(" offset 0 )");

    }

    //

    /*
     * This method only works with HSQLDB database. If one doesn't close the
     * HSQLDB properly with high probability, the DB goes into instable or in
     * worst case becomes corrupted.
     */
    @Override
    public void shutdown() throws SQLException {
        getConnection().createStatement().execute("SHUTDOWN");
        logger.warn("Closing the database server (for HSqlDB) [done].");
        logger.warn("Closing the connection pool [done].");
        super.shutdown();
    }
}
