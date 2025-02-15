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
* File: src/ch/epfl/gsn/wrappers/JDBCWrapper.java
*
* @author Sofiane Sarni
* @author Davide De Sclavis
* @author Manuel Buchauer
* @author Jan Beutel
*
*/

package ch.epfl.gsn.wrappers;

import org.slf4j.LoggerFactory;

import ch.epfl.gsn.Main;
import ch.epfl.gsn.beans.AddressBean;
import ch.epfl.gsn.beans.DataField;
import ch.epfl.gsn.beans.DataTypes;
import ch.epfl.gsn.beans.StreamElement;
import ch.epfl.gsn.storage.DataEnumerator;
import ch.epfl.gsn.storage.StorageManager;
import ch.epfl.gsn.storage.StorageManagerFactory;

import org.slf4j.Logger;
import org.apache.commons.io.FileUtils;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.sql.SQLException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * This JDBC wrapper enables one to reply the existing stream from a a table in
 * a database.
 * parameters: table: table name, start-time: starting time to replay from
 */
public class JDBCWrapper extends AbstractWrapper {

    private static long DEFAULT_RATE = 1000; // 1 second in milliseconds
    private static long DEFAULT_BUFFER_SIZE = 100;

    private transient Logger logger = LoggerFactory.getLogger(this.getClass());
    private DataField[] outputFormat;
    private int threadCounter = 0;
    private String table_name;
    private long start_time;
    private long rate = DEFAULT_RATE;
    private long buffer_size = DEFAULT_BUFFER_SIZE;
    private long latest_timed;
    private String checkPointDir;

    private String driver;
    private String username;
    private String password;
    private String databaseURL;

    String checkPointFile;
    StorageManager sm = null;

    String[] dataFieldNames;
    Byte[] dataFieldTypes;
    int dataFieldsLength;

    boolean useDefaultStorageManager = true;

    public String getWrapperName() {
        return "JDBCWrapper";
    }

    public void dispose() {
        threadCounter--;
    }

    public DataField[] getOutputFormat() {
        return outputFormat;
    }

    /**
     * Initializes the JDBCWrapper with the configuration obtained from the active
     * AddressBean.
     * This method retrieves database connection parameters, including the table
     * name, JDBC URL,
     * username, password, and driver. It then establishes a connection to the
     * database using
     * the specified parameters.
     *
     * @return {@code true} if initialization is successful, {@code false}
     *         otherwise. If false, warnings
     *         are logged indicating missing or malformed parameters.
     */
    public boolean initialize() {
        AddressBean addressBean = getActiveAddressBean();

        table_name = addressBean.getPredicateValue("table-name");

        databaseURL = addressBean.getPredicateValue("jdbc-url");
        username = addressBean.getPredicateValue("username");
        password = addressBean.getPredicateValue("password");
        driver = addressBean.getPredicateValue("driver");

        if ((databaseURL != null) && (username != null) && (password != null) && (driver != null)) {
            useDefaultStorageManager = false;
            sm = StorageManagerFactory.getInstance(driver, username, password, databaseURL,
                    Main.DEFAULT_MAX_DB_CONNECTIONS);
            logger.warn("Using specified storage manager: " + databaseURL);
        } else {
            sm = Main.getDefaultStorage();
            logger.warn("Using default storage manager");
        }

        if (table_name == null) {
            logger.warn("The > table-name < parameter is missing from the wrapper for VS "
                    + this.getActiveAddressBean().getVirtualSensorName());
            return false;
        }

        //////////////////
        boolean usePreviousCheckPoint = true;
        String time = addressBean.getPredicateValue("start-time");
        if (time == null) {
            logger.warn("The > start-time < parameter is missing from the wrapper for VS "
                    + this.getActiveAddressBean().getVirtualSensorName());
            return false;
        }

        if (time.equalsIgnoreCase("continue")) {
            latest_timed = getLatestProcessed();
            usePreviousCheckPoint = false;
            logger.warn("Mode: continue => " + latest_timed);
        } else if (isISOFormat(time)) {

            try {
                DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
                start_time = fmt.parseDateTime(time).getMillis();
                latest_timed = start_time;
                logger.warn("Mode: ISO => " + latest_timed);
            } catch (IllegalArgumentException e) {
                logger.warn("The > start-time < parameter is malformed (looks like ISO8601) for VS "
                        + this.getActiveAddressBean().getVirtualSensorName());
                return false;
            }
        } else if (isLong(time)) {
            try {
                latest_timed = Long.parseLong(time);
                logger.warn("Mode: epoch => " + latest_timed);
            } catch (NumberFormatException e) {
                logger.warn("The > start-time < parameter is malformed (looks like epoch) for VS "
                        + this.getActiveAddressBean().getVirtualSensorName());
                return false;
            }
        } else {
            logger.warn(
                    "Incorrectly formatted > start-time < accepted values are: 'continue' (from latest element in destination table), iso-date (e.g. 2009-11-02T00:00:00.000+00:00), or epoch (e.g. 1257946505000)");
            return false;
        }

        //////////////////

        checkPointDir = addressBean.getPredicateValueWithDefault("check-point-directory", "jdbc-check-points");
        checkPointFile = checkPointDir + "/" + table_name + "-" + this.getActiveAddressBean().getVirtualSensorName();
        new File(checkPointDir).mkdirs();

        if (usePreviousCheckPoint) {
            logger.warn("trying to read latest timestamp from chekpoint file ... " + checkPointFile);
            try {
                if (getLatestTimeStampFromCheckPoint() == 0) {
                    logger.warn("wrong value for latest ts (" + getLatestTimeStampFromCheckPoint() + "), ignored");
                } else {
                    latest_timed = getLatestTimeStampFromCheckPoint();
                    logger.warn("latest ts => " + latest_timed);
                }
            } catch (IOException e) {
                logger.warn("Checkpoints couldn't be used due to IO exception.");
                logger.warn(e.getMessage(), e);
            }
        }

        //////////////////

        Connection connection = null;
        try {
            logger.info("Initializing the structure of JDBCWrapper with : " + table_name);
            connection = sm.getConnection();

            outputFormat = sm.tableToStructureByString(table_name, connection);
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
            return false;
        } finally {
            sm.close(connection);
        }

        dataFieldsLength = outputFormat.length;
        dataFieldNames = new String[dataFieldsLength];
        dataFieldTypes = new Byte[dataFieldsLength];

        for (int i = 0; i < outputFormat.length; i++) {
            dataFieldNames[i] = outputFormat[i].getName();
            dataFieldTypes[i] = outputFormat[i].getDataTypeID();
        }

        return true;
    }

    /**
     * Retrieves the latest timestamp from the checkpoint file.
     * 
     * @return The latest timestamp from the checkpoint file.
     * @throws IOException If an I/O error occurs while reading the file.
     */
    public long getLatestTimeStampFromCheckPoint() throws IOException {
        String val = FileUtils.readFileToString(new File(checkPointFile), "UTF-8");
        long lastItem = 0;
        if (val != null && val.trim().length() > 0) {
            lastItem = Long.parseLong(val.trim());
        }
        return lastItem;
    }

    /**
     * Executes the main logic of the JDBCWrapper in a separate thread.
     * Retrieves data from the database using a specified query and processes it.
     * The retrieved data is converted into StreamElements and sent to the
     * postStreamElement method.
     * The method also updates the checkpoint file with the latest timestamp.
     */
    public void run() {

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }

        Connection conn = null;
        ResultSet resultSet = null;

        while (isActive()) {
            try {
                conn = sm.getConnection();
                StringBuilder query = new StringBuilder("select * from ").append(table_name)
                        .append(" where timed > " + latest_timed + " limit 0," + buffer_size);

                resultSet = sm.executeQueryWithResultSet(query, conn);

                // logger.debug(query);

                while (resultSet.next()) {
                    Serializable[] output = new Serializable[this.getOutputFormat().length];

                    long timed = resultSet.getLong(2);

                    // logger.warn("pk => "+ pk);
                    // logger.warn("timed => "+ timed);

                    for (int i = 0; i < dataFieldsLength; i++) {

                        switch (dataFieldTypes[i]) {
                            case DataTypes.VARCHAR:
                            case DataTypes.CHAR:
                                output[i] = resultSet.getString(i + 3);
                                break;
                            case DataTypes.INTEGER:
                                output[i] = resultSet.getInt(i + 3);
                                break;
                            case DataTypes.TINYINT:
                                output[i] = resultSet.getByte(i + 3);
                                break;
                            case DataTypes.SMALLINT:
                                output[i] = resultSet.getShort(i + 3);
                                break;
                            case DataTypes.DOUBLE:
                                output[i] = resultSet.getDouble(i + 3);
                                break;
                            case DataTypes.FLOAT:
                                output[i] = resultSet.getFloat(i + 3);
                                break;
                            case DataTypes.BIGINT:
                                output[i] = resultSet.getLong(i + 3);
                                break;
                            case DataTypes.BINARY:
                                output[i] = resultSet.getBytes(i + 3);
                                break;
                            default:
                                break;
                        }
                        // logger.warn(i+" (type: "+dataFieldTypes[i]+" ) => "+output[i]);
                    }

                    StreamElement se = new StreamElement(dataFieldNames, dataFieldTypes, output, timed);
                    latest_timed = se.getTimeStamp();

                    // logger.warn(" Latest => " + latest_timed);

                    this.postStreamElement(se);

                    updateCheckPointFile(latest_timed);

                    // logger.warn(se);
                }

            } catch (java.io.IOException e) {
                logger.error(e.getMessage(), e);
            } catch (SQLException e) {
                logger.error(e.getMessage(), e);
            } finally {
                sm.close(resultSet);
                sm.close(conn);
            }

            try {
                Thread.sleep(rate);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public void updateCheckPointFile(long timestamp) throws IOException {
        FileUtils.writeStringToFile(new File(checkPointFile), Long.toString(timestamp), "UTF-8");
    }

    /**
     * Retrieves the latest processed timestamp from the database.
     * 
     * @return The latest processed timestamp, or -1 if an error occurs.
     */
    public long getLatestProcessed() {
        DataEnumerator data;
        long latest = -1;
        StringBuilder query = new StringBuilder("select max(timed) from ")
                .append(this.getActiveAddressBean().getVirtualSensorName());
        try {
            data = sm.executeQuery(query, false);
            logger.warn("Running query " + query);

            while (data.hasMoreElements()) {
                StreamElement se = data.nextElement();
                if (se.getData("max(timed)") != null) {
                    latest = (Long) se.getData("max(timed)");
                }
                logger.warn(" MAX ts = " + latest);
                logger.warn(se.toString());

            }
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        } catch (NullPointerException e) {
            logger.error(e.getMessage(), e);
        }
        return latest;
    }

    /**
     * Checks if the given time string is in ISO format.
     *
     * @param time the time string to be checked
     * @return true if the time string is in ISO format, false otherwise
     */
    public boolean isISOFormat(String time) {
        // Example: 2009-11-02T00:00:00.000+00:00
        String regexMask = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{3}[+-]\\d{2}:\\d{2}$";
        Pattern pattern = Pattern.compile(regexMask);
        Matcher matcher = pattern.matcher(time);
        if(logger.isDebugEnabled()){
            logger.debug("Testing... " + time + " <==> " + regexMask);
        }
        if (matcher.find()) {
            if(logger.isDebugEnabled()){
                logger.debug(">>>>>    ISO FORMAT");
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Checks if a given string represents a long number.
     *
     * @param time the string to be checked
     * @return true if the string represents a long number, false otherwise
     */
    public boolean isLong(String time) {

        String regexMask = "^\\d+$";
        Pattern pattern = Pattern.compile(regexMask);
        Matcher matcher = pattern.matcher(time);
        if(logger.isDebugEnabled()){
            logger.debug("Testing... " + time + " <==> " + regexMask);
        }
        if (matcher.find()) {
            if(logger.isDebugEnabled()){
                logger.debug(">>>>>    LONG number");
            }
            return true;
        } else {
            return false;
        }
    }
}
