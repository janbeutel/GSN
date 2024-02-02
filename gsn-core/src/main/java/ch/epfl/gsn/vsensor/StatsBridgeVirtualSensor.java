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
* File: src/ch/epfl/gsn/vsensor/StatsBridgeVirtualSensor.java
*
* @author Sofiane Sarni
*
*/

package ch.epfl.gsn.vsensor;

import org.slf4j.LoggerFactory;

import ch.epfl.gsn.beans.StreamElement;

import org.slf4j.Logger;

import java.util.TreeMap;

/**
 * Extends AbstractVirtualSensor to provide a bridge virtual sensor that logs
 * statistics about the data received from the wrapped virtual sensor.
 */
public class StatsBridgeVirtualSensor extends AbstractVirtualSensor {

    private static final String PARAM_LOGGING_INTERVAL = "logging-interval";
    private boolean logging_timestamps = false;
    private long logging_interval;
    private long logging_counter = 0;
    private String vsname;

    private static final transient Logger logger = LoggerFactory.getLogger(BridgeVirtualSensor.class);

    /**
     * Initializes the StatsBridgeVirtualSensor.
     * 
     * Parses the logging interval parameter from the virtual sensor configuration.
     * Enables timestamp logging if the parameter is valid.
     * Stores the virtual sensor name.
     * 
     * @return True if initialization succeeded.
     */
    public boolean initialize() {

        TreeMap<String, String> params = getVirtualSensorConfiguration().getMainClassInitialParams();

        String logging_interval_str = params.get(PARAM_LOGGING_INTERVAL);
        if (logging_interval_str != null) {
            logging_timestamps = true;
            try {
                logging_interval = Integer.parseInt(logging_interval_str.trim());
            } catch (NumberFormatException e) {
                logger.warn("Parameter \"" + PARAM_LOGGING_INTERVAL + "\" incorrect in Virtual Sensor file");
                logging_timestamps = false;
            }
        }

        vsname = getVirtualSensorConfiguration().getName();

        return true;
    }

    /**
     * Logs statistics about the data received from the wrapped
     * virtual sensor.
     * 
     * This method is called whenever new data is available from the
     * virtual sensor. It logs a message at the configured
     * interval containing the virtual sensor name, logging counter,
     * and timestamp.
     *
     * @param inputStreamName Name of the input stream for the received data
     * @param data            The StreamElement data received from the wrapped
     *                        sensor
     */

    public void dataAvailable(String inputStreamName, StreamElement data) {

        if (logging_counter % logging_interval == 0) {
            logger.warn(vsname + " , " + logging_counter + " , " + System.currentTimeMillis());
        }

        logging_counter++;

        dataProduced(data);
        if(logger.isDebugEnabled()){
            logger.debug("Data received under the name: " + inputStreamName);
        }
    }

    public void dispose() {

    }

}
