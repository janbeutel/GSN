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
* File: src/ch/epfl/gsn/vsensor/BridgeVirtualSensor.java
*
* @author Ali Salehi
* @author Mehdi Riahi
* @author Sofiane Sarni
*
*/

package ch.epfl.gsn.vsensor;

import org.slf4j.LoggerFactory;

import ch.epfl.gsn.beans.StreamElement;
import ch.epfl.gsn.beans.VSensorConfig;

import org.slf4j.Logger;
import java.util.TreeMap;

/**
 * BridgeVirtualSensor extends AbstractVirtualSensor that provides
 * functionality for bridging data between input and output streams.
 */
public class BridgeVirtualSensor extends AbstractVirtualSensor {

    private static final transient Logger logger = LoggerFactory.getLogger(BridgeVirtualSensor.class);
    private boolean allow_nulls = true; // by default allow nulls

    /**
     * Initializes the BridgeVirtualSensor by getting the allow-nulls parameter
     * from the virtual sensor configuration and setting the allow_nulls field
     * accordingly.
     */
    public boolean initialize() {
        VSensorConfig vsensor = getVirtualSensorConfiguration();
        TreeMap<String, String> params = vsensor.getMainClassInitialParams();

        String allow_nulls_str = params.get("allow-nulls");
        if (allow_nulls_str != null) {
            allow_nulls = allow_nulls_str.equalsIgnoreCase("true");
        }
        return true;
    }

    /**
     * Called when data is available for processing.
     * If allow_nulls is true, the data is produced regardless of null fields.
     * If allow_nulls is false, the data is checked via areAllFieldsNull().
     * 
     * @param inputStreamName the name of the input stream
     * @param data            the StreamElement containing the data
     */
    public void dataAvailable(String inputStreamName, StreamElement data) {
        if (allow_nulls) {
            dataProduced(data);
        } else {
            if (areAllFieldsNull(data)) {
                if(logger.isDebugEnabled()){
                    logger.debug("Nulls received for timestamp (" + data.getTimeStamp() + "), discarded");
                }
            } else {
                dataProduced(data);
            }
        }
        if(logger.isDebugEnabled()){
            logger.debug("Data received under the name: " + inputStreamName);
        }
    }

    /**
     * Checks if fields of the given StreamElement are null.
     * 
     * @param data The StreamElement to check.
     * @return True if a field is null, false otherwise.
     */
    public boolean areAllFieldsNull(StreamElement data) {
        boolean allFieldsNull = false;
        for (int i = 0; i < data.getData().length; i++) {
            if (data.getData()[i] == null) {
                allFieldsNull = true;
                break;
            }
        }

        return allFieldsNull;
    }

    public void dispose() {

    }

}
