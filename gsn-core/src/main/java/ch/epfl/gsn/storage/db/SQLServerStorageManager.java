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
* File: src/ch/epfl/gsn/storage/db/SQLServerStorageManager.java
*
* @author Timotee Maret
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
import java.sql.Types;

public class SQLServerStorageManager extends StorageManager {

    private static final transient Logger logger = LoggerFactory.getLogger(SQLServerStorageManager.class);

    public SQLServerStorageManager() {
        super();
        this.isSqlServer = true;
    }

    @Override
    public String getJDBCPrefix() {
        return "jdbc:jtds:sqlserver:";
    }

    /**
     * Converts a GSN data field type to a local data field type.
     * 
     * @param gsnType the GSN data field type to be converted
     * @return the converted local data field type as a String
     */
    @Override
    public String convertGSNTypeToLocalType(DataField gsnType) {
        String convertedType = null;
        switch (gsnType.getDataTypeID()) {
            case DataTypes.CHAR:
            case DataTypes.VARCHAR:
                // Because the parameter for the varchar is not
                // optional.
                convertedType = gsnType.getType();
                break;
            case DataTypes.FLOAT:
                convertedType = "REAL";
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
            case Types.REAL:
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
        // if (isSqlServer()) return "DROP TABLE #NAME";
        // another: return "DROP INDEX #NAME";
        return "DROP INDEX #NAME ON #TABLE";
    }

    /**
     * Returns the SQL statement for dropping a view.
     *
     * @return the SQL statement for dropping a view
     */
    @Override
    public String getStatementDropView() {
        // if (isSqlServer()) return "DROP VIEW #NAME";
        return "DROP VIEW #NAME";
    }

    @Override
    public int getTableNotExistsErrNo() {
        return 208; // java.sql.SQLException: Invalid object name
    }

    /**
     * Adds a LIMIT and OFFSET clause to the given SQL query.
     *
     * @param query  the original SQL query
     * @param limit  the maximum number of rows to return
     * @param offset the number of rows to skip before starting to return rows
     * @return the modified SQL query with the LIMIT and OFFSET clause
     */
    @Override
    public String addLimit(String query, int limit, int offset) {
        // FIXME, INCORRECT !
        return query + " LIMIT " + limit + " OFFSET " + offset;
    }

    /**
     * Returns the statement difference time in milliseconds.
     *
     * @return the statement difference time in milliseconds as a string.
     */
    @Override
    public String getStatementDifferenceTimeInMillis() {
        return "select convert(bigint,datediff(second,'1/1/1970',current_timestamp))*1000 ";
    }

    /**
     * Generates a SQL statement to drop the specified table from the database.
     * 
     * @param tableName The name of the table to be dropped.
     * @param conn      The database connection used to determine the appropriate
     *                  SQL syntax.
     * @return A StringBuilder containing the SQL statement for dropping the
     *         specified table.
     * @throws SQLException If an SQL exception occurs while generating the
     *                      statement.
     */
    @Override
    public StringBuilder getStatementDropTable(CharSequence tableName, Connection conn) throws SQLException {
        StringBuilder sb = new StringBuilder("Drop table ");
        sb.append(tableName);
        return sb;
    }

    /**
     * Generates a SQL statement to create a new table in the database.
     *
     * @param tableName The name of the table to be created.
     * @param structure An array of DataField objects representing the structure of
     *                  the table.
     * @return A StringBuilder containing the SQL statement for creating the
     *         specified table.
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
     * Generates a SQL statement to remove useless data from a table based on a
     * count limit.
     *
     * @param virtualSensorName The name of the virtual sensor table from which data
     *                          is to be removed.
     * @param storageSize       The count limit indicating the maximum number of
     *                          records to keep in the table.
     * @return A StringBuilder containing the SQL statement for removing useless
     *         data based on a count limit.
     */
    @Override
    public StringBuilder getStatementUselessDataRemoval(String virtualSensorName, long storageSize) {
        return new StringBuilder()
                .append("delete from ")
                .append(virtualSensorName)
                .append(" where ")
                .append(virtualSensorName)
                .append(".timed < (select min(timed) from (select top ")
                .append(storageSize)
                .append(" * from ")
                .append(virtualSensorName)
                .append(" order by ")
                .append(virtualSensorName)
                .append(".timed DESC ) as x ) ");
    }

    /**
     * Generates a SQL statement to remove useless data from a table based on a
     * count limit.
     *
     * @param virtualSensorName The name of the virtual sensor table from which data
     *                          is to be removed.
     * @param storageSize       The count limit indicating the maximum number of
     *                          records to keep in the table.
     * @return A StringBuilder containing the SQL statement for removing useless
     *         data based on a count limit.
     */
    @Override
    public StringBuilder getStatementRemoveUselessDataCountBased(String virtualSensorName, long storageSize) {
        return new StringBuilder()
                .append("delete from ")
                .append(virtualSensorName)
                .append(" where ")
                .append(virtualSensorName)
                .append(".timed < (select min(timed) from (select top ")
                .append(storageSize)
                .append(" * from ")
                .append(virtualSensorName)
                .append(" order by ")
                .append(virtualSensorName)
                .append(".timed DESC ) as x ) ");

    }

}
