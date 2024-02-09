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
* File: src/ch/epfl/gsn/storage/db/OracleStorageManager.java
*
* @author Timotee Maret
*
*/

package ch.epfl.gsn.storage.db;

import org.slf4j.LoggerFactory;

import ch.epfl.gsn.beans.DataField;
import ch.epfl.gsn.beans.DataTypes;
import ch.epfl.gsn.storage.SQLUtils;
import ch.epfl.gsn.storage.StorageManager;

import org.slf4j.Logger;

import java.sql.*;

public class OracleStorageManager extends StorageManager {

    private static final transient Logger logger = LoggerFactory.getLogger(OracleStorageManager.class);

    public OracleStorageManager() {
        super();
        this.isOracle = true;
    }

    @Override
    public String getJDBCPrefix() {
        return "jdbc:oracle:thin:";
    }

    /**
     * http://docs.oracle.com/cd/B19306_01/java.102/b14355/oraint.htm
     * http://docs.oracle.com/cd/E11882_01/java.112/e16548/apxref.htm#JJDBC28906
     * mapping of double and float should be using the oracle JDBC extension and
     * BINARY_DOUBLE/BINARY_FLOAT
     * 
     */
    @Override
    public String convertGSNTypeToLocalType(DataField gsnType) {
        String convertedType = null;
        switch (gsnType.getDataTypeID()) {
            case DataTypes.BIGINT:
            case DataTypes.SMALLINT:
            case DataTypes.INTEGER:
            case DataTypes.TINYINT:
                convertedType = "number(38,0)";
                break;
            case DataTypes.DOUBLE:
                convertedType = "number(38,16)";
                break;
            case DataTypes.FLOAT:
                convertedType = "number(38,8)";
                break;
            case DataTypes.CHAR:
            case DataTypes.VARCHAR:
                // Because the parameter for the varchar is not
                // optional.
                convertedType = gsnType.getType();
                convertedType = convertedType.toLowerCase().replace("varchar", "varchar2");
                break;
            case DataTypes.BINARY:
                convertedType = "LONG RAW";
                break;
            default:
                convertedType = DataTypes.TYPE_NAMES[gsnType.getDataTypeID()];
                break;
        }
        return convertedType;
    }

    /**
     * Converts the specified JDBC type and precision to the corresponding GSN data
     * type.
     *
     * @param jdbcType  The JDBC type to convert.
     * @param precision The precision associated with the JDBC type.
     * @return The GSN data type corresponding to the JDBC type and precision.
     */
    @Override
    public byte convertLocalTypeToGSN(int jdbcType, int precision) {
        switch (jdbcType) {
            case Types.NUMERIC:
                if (precision == 0) {
                    return DataTypes.BIGINT;
                } else if (precision > 8) {
                    return DataTypes.DOUBLE;
                } else {
                    return DataTypes.FLOAT;
                }

            case Types.VARCHAR:
                return DataTypes.VARCHAR;
            case Types.CHAR:
                return DataTypes.CHAR;
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
        return "DROP INDEX #NAME ON #TABLE";
    }

    /**
     * Returns the SQL statement for dropping a view.
     *
     * @return the SQL statement for dropping a view
     */
    @Override
    public String getStatementDropView() {
        return "DROP VIEW #NAME";
    }

    @Override
    public int getTableNotExistsErrNo() {
        return 208; // java.sql.SQLException: Invalid object name
    }

    /**
     * Adds a LIMIT clause to the specified SQL query with optional OFFSET for
     * pagination.
     *
     * @param query  The SQL query to which the LIMIT clause should be added.
     * @param limit  The maximum number of rows to retrieve.
     * @param offset The number of rows to skip before starting to return rows.
     * @return The modified SQL query with the added LIMIT and OFFSET clauses.
     */
    @Override
    public String addLimit(String query, int limit, int offset) {
        String toAppend = "";
        if (offset == 0) {
            toAppend = " ROWNUM <= " + limit;
        } else {
            toAppend = " ROWNUM BETWEEN " + offset + " AND " + (limit + offset) + " ";
        }

        int indexOfWhere = SQLUtils.getWhereIndex(query);
        int indexOfGroupBy = SQLUtils.getGroupByIndex(query);
        int indexOfOrder = SQLUtils.getOrderByIndex(query);

        StringBuilder toReturn = new StringBuilder(query);
        if (indexOfGroupBy < 0 && indexOfWhere < 0 && indexOfOrder < 0) {
            return query + " WHERE " + toAppend;
        }

        if (indexOfWhere < 0 && indexOfOrder > 0) {
            return toReturn.insert(indexOfOrder, " WHERE " + toAppend).toString();
        }

        if (indexOfWhere < 0 && indexOfGroupBy > 0) {
            return toReturn.insert(indexOfGroupBy, " WHERE " + toAppend).toString();
        }

        if (indexOfWhere > 0) {
            StringBuilder tmp = toReturn.insert(indexOfWhere + " WHERE ".length(), toAppend + " AND (");
            int endIndex = tmp.length();
            if (indexOfGroupBy > 0) {
                endIndex = SQLUtils.getGroupByIndex(tmp);
            } else if (indexOfOrder > 0) {
                endIndex = SQLUtils.getOrderByIndex(tmp);
            }

            tmp.insert(endIndex, ")");
            return tmp.toString();
        }
        return query + " LIMIT " + limit + " OFFSET " + offset;
    }

    @Override
    public String getStatementDifferenceTimeInMillis() {
        return "";
    }

    /**
     * Generates a SQL statement to drop the specified table.
     *
     * @param tableName The name of the table to be dropped.
     * @param conn      The database connection to execute the statement.
     * @return A StringBuilder containing the SQL statement to drop the table.
     * @throws SQLException If a database access error occurs.
     */
    @Override
    public StringBuilder getStatementDropTable(CharSequence tableName, Connection conn) throws SQLException {
        StringBuilder sb = new StringBuilder("Drop table ");
        sb.append(tableName);
        return sb;
    }

    /**
     * Generates a SQL statement to create a table with the specified name and
     * structure.
     *
     * @param tableName The name of the table to be created.
     * @param structure The structure of the table represented by an array of
     *                  DataField objects.
     * @return A StringBuilder containing the SQL statement to create the table.
     */
    @Override
    public StringBuilder getStatementCreateTable(String tableName, DataField[] structure) {
        StringBuilder result = new StringBuilder("CREATE TABLE ").append(tableName);
        result.append(" (PK number(38) PRIMARY KEY, timed number(38) NOT NULL, ");
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
     * Generates a SQL statement to remove useless data from a virtual sensor table
     * based on the specified storage size.
     *
     * @param virtualSensorName The name of the virtual sensor table from which data
     *                          will be removed.
     * @param storageSize       The storage size, indicating the number of rows to
     *                          retain.
     * @return A StringBuilder containing the SQL statement to remove useless data
     *         from the virtual sensor table.
     */
    @Override
    public StringBuilder getStatementUselessDataRemoval(String virtualSensorName, long storageSize) {
        return new StringBuilder()
                .append("delete from ")
                .append(virtualSensorName)
                .append(" where timed <= ( SELECT * FROM ( SELECT timed FROM ")
                .append(virtualSensorName)
                .append(" group by timed ORDER BY timed DESC) where rownum = ")
                .append(storageSize + 1)
                .append(" )");
    }

    /**
     * Executes the drop table operation along with additional cleanup tasks like
     * dropping associated sequence and trigger.
     *
     * @param tableName  The name of the table to be dropped.
     * @param connection The database connection.
     */
    @Override
    public void executeDropTable(CharSequence tableName, Connection connection) {
        try {
            super.executeDropTable(tableName, connection);
            executeCommand("drop sequence " + tableNamePostFixAppender(tableName, "_SEQ"), connection);
            executeCommand("drop trigger " + tableNamePostFixAppender(tableName, "_TRIG"), connection);
        } catch (Exception e) {
            logger.info(e.getMessage(), e);
        }
    }

    /**
     * Executes the provided SQL command on the given database connection.
     *
     * @param sql        The SQL command to be executed.
     * @param connection The database connection on which the command will be
     *                   executed.
     */
    @Override
    public void executeCommand(String sql, Connection connection) {
        Statement stmt = null;
        try {
            stmt = connection.createStatement();
            stmt.execute(sql);
        } catch (SQLException error) {
            if ((sql.toLowerCase().contains("drop trigger") && error.getMessage().contains("does not exist")) ||
                    (sql.toLowerCase().contains("create sequence")
                            && error.getMessage().contains("name is already used"))) {
                // ignore it for oracle
                ;
            } else {
                logger.error(error.getMessage() + " FOR: " + sql, error);
            }

        } finally {
            try {
                if (stmt != null && !stmt.isClosed()) {
                    stmt.close();
                }

            } catch (SQLException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Executes the SQL commands to create a table, sequence, trigger, and index for
     * the specified table structure.
     *
     * @param tableName  The name of the table to be created.
     * @param structure  The array of DataField objects representing the table
     *                   structure.
     * @param unique     A boolean indicating whether to create a unique index on
     *                   the 'timed' column.
     * @param connection The database connection on which the commands will be
     *                   executed.
     * @throws SQLException If a database access error occurs or if the SQL commands
     *                      fail to execute.
     */
    @Override
    public void executeCreateTable(CharSequence tableName, DataField[] structure, boolean unique, Connection connection)
            throws SQLException {
        StringBuilder sql = getStatementCreateTable(tableName, structure, connection);
        if(logger.isDebugEnabled()){
            logger.debug(new StringBuilder().append("The create table statement is : ").append(sql).toString());
        }
        PreparedStatement prepareStatement = connection.prepareStatement(sql.toString());
        prepareStatement.execute();
        prepareStatement.close();

        // need to make a sequence and trigger.
        String oracleSeq = "create sequence " + tableNamePostFixAppender(tableName, "_SEQ");
        String oracleTrigger = "create or replace trigger " + tableNamePostFixAppender(tableName, "_TRIG")
                + " before insert on " + tableName + " for each row begin select "
                + tableNamePostFixAppender(tableName, "_SEQ") + ".nextval into :NEW.pk from dual; end;";
        
        if(logger.isDebugEnabled()){
            logger.debug(oracleSeq);
            logger.debug(oracleTrigger);
        }
        executeCommand(oracleSeq, connection);
        executeCommand(oracleTrigger, connection);

        sql = getStatementCreateIndexOnTimed(tableName, unique);
        if(logger.isDebugEnabled()){
            logger.debug(new StringBuilder().append("The create index statement is : ").append(sql).toString());
        }
        prepareStatement = connection.prepareStatement(sql.toString());
        prepareStatement.execute();

    }

    /**
     * Generates a SQL DELETE statement to remove useless data from the specified
     * virtual sensor based on the count.
     *
     * @param virtualSensorName The name of the virtual sensor table.
     * @param storageSize       The count of entries to retain in the virtual sensor
     *                          table.
     * @return A StringBuilder containing the generated SQL DELETE statement.
     */
    @Override
    public StringBuilder getStatementRemoveUselessDataCountBased(String virtualSensorName, long storageSize) {
        return new StringBuilder()
                .append("delete from ")
                .append(virtualSensorName)
                .append(" where timed <= ( SELECT * FROM ( SELECT timed FROM ")
                .append(virtualSensorName)
                .append(" group by timed ORDER BY timed DESC) where rownum = ")
                .append(storageSize + 1)
                .append(" )");
    }

    // private

    /**
     * This method is used ONLY for ORACLE DB.
     * ADDS the postfix at the end of the tableName. If the table name ends with "
     * then
     * updates it properly.
     * 
     * @param table_name
     * @return
     */
    @Override
    public String tableNamePostFixAppender(CharSequence table_name, String postFix) {
        String tableName = table_name.toString();
        if (tableName.endsWith("\"")) {
            return (tableName.substring(0, tableName.length() - 2)) + postFix + "\"";
        } else {
            return tableName + postFix;
        }

    }

    /**
     * Generates a StringBuilder containing the formatted table name based on
     * certain conditions.
     *
     * If the first character of the table name is an underscore ('_'), the method
     * wraps the table name with double
     * quotes. Otherwise, it returns the original table name.
     *
     * @param tableName The original table name to be formatted.
     * @return A StringBuilder containing the formatted table name.
     */
    @Override
    public StringBuilder tableNameGeneratorInString(CharSequence tableName) {
        if (tableName.charAt(0) == '_') {
            return new StringBuilder("\"").append(tableName).append("\"");
        }
        return new StringBuilder(tableName);
    }
}
