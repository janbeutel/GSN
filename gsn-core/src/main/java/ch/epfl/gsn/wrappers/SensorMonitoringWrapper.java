package ch.epfl.gsn.wrappers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ch.epfl.gsn.beans.AddressBean;
import ch.epfl.gsn.beans.DataField;
import ch.epfl.gsn.beans.DataTypes;
import ch.epfl.gsn.beans.StreamElement;
import ch.epfl.gsn.utils.ParamParser;

import ch.epfl.gsn.Main;
import ch.epfl.gsn.monitoring.*;
import java.util.Hashtable;
import java.util.Map;
import java.util.HashMap;
import java.io.Serializable;

public class SensorMonitoringWrapper extends AbstractWrapper {
    private static final int DEFAULT_SAMPLING_RATE = 10000;

    private int samplingRate = DEFAULT_SAMPLING_RATE;
    private final transient Logger logger = LoggerFactory.getLogger(SensorMonitoringWrapper.class);
    private static int threadCounter = 0;
    private transient DataField[] outputStructureCache = new DataField[] {
            new DataField("SENSOR_NAME", "varchar(50)", "Name of the monitored sensor"),
            new DataField("TOTAL_CPU_TIME_COUNTER", "bigint", "cpu time of monitored sensor"),
            new DataField("LAST_OUTPUT_TIME", "bigint", "last output time of monitored sensor"),
            new DataField("OUTPUT_PRODUCED_COUNTER", "bigint", "output counter of monitored sensor"),
            new DataField("LAST_INPUT_TIME", "bigint", "last output time of monitored sensor"),
            new DataField("INPUT_PRODUCED_COUNTER", "bigint", "input counter of monitored sensor")

    };
    private static final String[] FIELD_NAMES = new String[] { "SENSOR_NAME",
            "TOTAL_CPU_TIME_COUNTER",
            "LAST_OUTPUT_TIME",
            "OUTPUT_PRODUCED_COUNTER",
            "LAST_INPUT_TIME",
            "INPUT_PRODUCED_COUNTER"
    };

    /**
     * Initializes the SensorMonitoringWrapper.
     * This method retrieves the sampling rate from the active address bean and sets
     * it as the sampling rate for the wrapper.
     * If the sampling rate is not specified or is not a positive number, the
     * default sampling rate is used.
     * 
     * @return true if the initialization is successful, false otherwise.
     */
    public boolean initialize() {
        AddressBean addressBean = getActiveAddressBean();
        if (addressBean.getPredicateValue("sampling-rate") != null) {
            samplingRate = ParamParser.getInteger(addressBean.getPredicateValue("sampling-rate"),
                    DEFAULT_SAMPLING_RATE);
            if (samplingRate <= 0) {
                logger.warn(
                        "The specified >sampling-rate< parameter for the >SensorMonitoringWrapper< should be a positive number.\nGSN uses the default rate ("
                                + DEFAULT_SAMPLING_RATE + "ms ).");
                samplingRate = DEFAULT_SAMPLING_RATE;
            }
        }
        return true;
    }

    /**
     * Executes the main logic of the SensorMonitoringWrapper in a separate thread.
     * This method continuously monitors the sensor data and posts the collected
     * data to a stream.
     */
    public void run() {
        while (isActive()) {
            try {
                Thread.sleep(samplingRate);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }

            Map<String, Map<String, Long>> sensorDataMap = new HashMap<>();
            for (Monitorable m : Main.getInstance().getToMonitor()) {
                Hashtable<String, Object> h = m.getStatistics();

                long outputedTime = -1;
                long inputTime = -1;
                long totalcpuTime = -1;
                long inputproduced = -1;
                long outputproduced = -1;
                // Iterate through the entries of the Hashtable
                for (Map.Entry<String, Object> entry : h.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    String[] parts = key.split("\\.");
                    if (parts[0].equals("vs")) {
                        String virtualSensorName = parts[1];
                        sensorDataMap.putIfAbsent(virtualSensorName, new HashMap<>());
                        Map<String, Long> sensorInnerMap = sensorDataMap.get(virtualSensorName);
                        if (key.contains("outupt.lastOutputedTime")) {
                            sensorInnerMap.put("outputedTime", (long) value);
                        }
                        if (key.contains("input.lastInputTime")) {
                            sensorInnerMap.put("inputTime", (long) value);
                        }
                        if (key.contains("cputime.totalCpuTime.counter")) {
                            sensorInnerMap.put("totalcpuTime", (long) value);
                        }
                        if (key.contains("input.produced.counter")) {
                            sensorInnerMap.put("inputproduced", (long) value);
                        }
                        if (key.contains("output.produced.counter")) {
                            sensorInnerMap.put("outputproduced", (long) value);
                        }
                    }
                }
            }
            for (Map.Entry<String, Map<String, Long>> sensorEntry : sensorDataMap.entrySet()) {
                String virtualSensorName = sensorEntry.getKey();
                Map<String, Long> sensorInnerMap = sensorEntry.getValue();

                StreamElement streamElement = new StreamElement(
                        FIELD_NAMES,
                        new Byte[] { DataTypes.VARCHAR, DataTypes.BIGINT, DataTypes.BIGINT, DataTypes.BIGINT,
                                DataTypes.BIGINT, DataTypes.BIGINT },
                        new Serializable[] { virtualSensorName, sensorInnerMap.get("totalcpuTime"),
                                sensorInnerMap.get("outputedTime"),
                                sensorInnerMap.get("outputproduced"), sensorInnerMap.get("inputTime"),
                                sensorInnerMap.get("inputproduced") },
                        System.currentTimeMillis());

                postStreamElement(streamElement);
            }

        }
    }

    public void dispose() {
        threadCounter--;
    }

    /**
     * The output fields exported by this virtual sensor.
     * 
     * @return The strutcture of the output.
     */

    public final DataField[] getOutputFormat() {
        return outputStructureCache;
    }

    public String getWrapperName() {
        return "Sensor Monitoring";
    }
}