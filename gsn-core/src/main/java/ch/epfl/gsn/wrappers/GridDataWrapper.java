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
* File: src/ch/epfl/gsn/wrappers/GridDataWrapper.java
*
* @author Sofiane Sarni
* @author Davide De Sclavis
* @author Manuel Buchauer
* @author Jan Beutel
*
*/

package ch.epfl.gsn.wrappers;

import org.slf4j.LoggerFactory;

import ch.epfl.gsn.beans.AddressBean;
import ch.epfl.gsn.beans.DataField;
import ch.epfl.gsn.beans.StreamElement;

import org.slf4j.Logger;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GridDataWrapper extends AbstractWrapper {
    private static final transient Logger logger = LoggerFactory.getLogger(GridDataWrapper.class);
    private static int threadCounter;

    private String directory;
    private String fileExtension;
    private String timeFormat;
    private String fileMask;
    private long latestProcessedTimestamp;

    private static final String PARAM_DIRECTORY = "directory";
    private static final String PARAM_FILE_MASK = "file-mask";
    private static final String PARAM_TIME_FORMAT = "time-format";
    private static final String PARAM_EXTENSION = "extension";
    private static final String PARAM_RATE = "rate";

    private static final String[] ESRI_Format = { "ncols",
            "nrows",
            "xllcorner",
            "yllcorner",
            "cellsize",
            "NODATA_value" };

    private String header[] = new String[6];

    private int ncols;
    private int nrows;
    private double xllcorner;
    private double yllcorner;
    private double cellsize;
    private double NODATA_value;
    private Double[][] rawData;

    private long rate;

    /**
     * Initializes the GridDataWrapper by retrieving the necessary configuration
     * parameters from the AddressBean.
     * 
     * @return true if initialization is successful, false otherwise.
     */
    public boolean initialize() {

        AddressBean addressBean = getActiveAddressBean();

        fileExtension = addressBean.getPredicateValue(PARAM_EXTENSION);
        if (fileExtension == null) {
            logger.warn("The > " + PARAM_EXTENSION + " < parameter is missing from the wrapper for VS "
                    + this.getActiveAddressBean().getVirtualSensorName());
            return false;
        }

        timeFormat = addressBean.getPredicateValue(PARAM_TIME_FORMAT);
        if (timeFormat == null) {
            logger.warn("The > " + PARAM_TIME_FORMAT + " < parameter is missing from the wrapper for VS "
                    + this.getActiveAddressBean().getVirtualSensorName());
            return false;
        }

        fileMask = addressBean.getPredicateValue(PARAM_FILE_MASK);
        if (fileMask == null) {
            logger.warn("The > " + PARAM_FILE_MASK + " < parameter is missing from the wrapper for VS "
                    + this.getActiveAddressBean().getVirtualSensorName());
            return false;
        }

        directory = addressBean.getPredicateValue(PARAM_DIRECTORY);
        if (directory == null) {
            logger.warn("The > " + PARAM_DIRECTORY + " < parameter is missing from the wrapper for VS "
                    + this.getActiveAddressBean().getVirtualSensorName());
            return false;
        }

        String rateStr = addressBean.getPredicateValue(PARAM_RATE);
        if (rateStr == null) {
            logger.warn("The > " + PARAM_RATE + " < parameter is missing from the wrapper in VS "
                    + this.getActiveAddressBean().getVirtualSensorName());
            return false;
        } else {
            try {
                rate = Integer.parseInt(rateStr);
            } catch (NumberFormatException e) {
                logger.warn("The > " + PARAM_RATE + " < parameter is invalid for wrapper in VS "
                        + this.getActiveAddressBean().getVirtualSensorName());
                return false;
            }
        }

        latestProcessedTimestamp = -1;

        return true;
    }

    public DataField[] getOutputFormat() {
        return new DataField[] {
                new DataField("ncols", "int", "number of columns"),
                new DataField("nrows", "int", "number of rows"),
                new DataField("xllcorner", "double", "xll corner"),
                new DataField("yllcorner", "double", "yll corner"),
                new DataField("cellsize", "double", "cell size"),
                new DataField("nodata_value", "double", "no data value"),
                new DataField("grid", "binary:image/raw", "raw raster data") };
    }

    /**
     * Runs the GridDataWrapper thread.
     * This method sleeps for 2000 milliseconds and then enters a loop that checks
     * for new files in the specified directory
     * at a specified rate. The loop continues until the thread is no longer active.
     */
    public void run() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }

        while (isActive()) {
            try {

                listOfNewFiles(directory, fileMask);
                Thread.sleep(rate);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Parses a file and initializes the corresponding fields.
     *
     * @param fileName The name of the file to be parsed.
     * @return {@code true} if the file is successfully parsed and data is
     *         initialized, {@code false} otherwise.
     */
    public boolean parseFile(String fileName) {
        boolean success = true;
        String line;
        BufferedReader reader = null;
        List<String> lines = new ArrayList<String>();
        try {

            reader = new BufferedReader(new FileReader(fileName));

            line = reader.readLine();
            while (line != null) {
                lines.add(line);
                line = reader.readLine();
            }

            // System.out.println(lines);

        } catch (FileNotFoundException e) {
            success = false;
            logger.warn("File not found: " + fileName);
            logger.warn(e.getMessage());
        } catch (IOException e) {
            success = false;
            logger.warn("IO exception on opening of file: " + fileName);
            logger.warn(e.getMessage());
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                logger.warn("IO exception on closing of file: " + fileName);
                logger.warn(e.getMessage());
            }
        }

        if ((success) && (lines != null)) {

            // trim white spaces, replace tabs and multiple spaces with a single space
            for (int i = 0; i < lines.size(); i++) {
                lines.set(i, lines.get(i).trim().replaceAll("[ \t]+", " "));
                // System.out.println(lines.get(i));
            }

            if(logger.isDebugEnabled()){
                logger.debug("size " + lines.size());
            }

            try {
                for (int i = 0; i < 6; i++) {
                    String[] split = lines.get(i).split(" ");

                    header[i] = split[1];
                    if(logger.isDebugEnabled()){
                        logger.debug(split[0] + " <=> " + ESRI_Format[i]);
                    }
                    if (!split[0].equals(ESRI_Format[i])) {
                        if(logger.isDebugEnabled()){
                            logger.debug("=> inCorrect");
                        }
                        success = false;
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                success = false;
                logger.warn("Badly formatted file: " + fileName);
                logger.warn(e.getMessage());
            }

            if (success) {
                ncols = Integer.parseInt(header[0]);
                nrows = Integer.parseInt(header[1]);
                xllcorner = Double.parseDouble(header[2]);
                yllcorner = Double.parseDouble(header[3]);
                cellsize = Double.parseDouble(header[4]);
                NODATA_value = Double.parseDouble(header[5]);
                
                if(logger.isDebugEnabled()){
                    logger.debug("ncols " + ncols);
                    logger.debug("nrows " + nrows);
                    logger.debug("xllcorner " + xllcorner);
                    logger.debug("yllcorner " + yllcorner);
                    logger.debug("cellsize " + cellsize);
                    logger.debug("NODATA_value " + NODATA_value);
                }
            }

            // parse raw data
            if (success) {
                List raw = new ArrayList<Double>();

                for (int i = 6; i < lines.size(); i++) {
                    String[] aLine = lines.get(i).split(" ");
                    for (int j = 0; j < aLine.length; j++) {

                        try {
                            Double d = Double.parseDouble(aLine[j]);
                            if (d == null) {
                                raw.add(NODATA_value);
                            } else {
                                raw.add(d);
                            }
                        } catch (java.lang.NumberFormatException e) {
                            logger.warn(j + ": \"" + aLine[j] + "\"");
                            logger.warn(e.getMessage());
                        }
                        // System.out.println(i + "," + j + " : " + d);
                    }

                }
                
                if(logger.isDebugEnabled()){
                    logger.debug("Size of list => " + raw.size() + " ? " + ncols * nrows);
                    logger.debug(raw.toString());
                }

                if (raw.size() == nrows * ncols) {
                    rawData = new Double[nrows][ncols];
                    for (int i = 0; i < nrows; i++) {
                        for (int j = 0; j < ncols; j++) {
                            rawData[i][j] = (Double) raw.get(i * ncols + j);
                            // System.out.println(i + "," + j + " : " + rawData[i][j]);
                        }
                    }

                    if(logger.isDebugEnabled()){
                        logger.debug("rawData.length " + rawData.length);
                        logger.debug("rawData[0].length " + rawData[0].length);
                    }
                } else {
                    success = false;
                }
            }

        }

        return success;
    }

    public void dispose() {
        threadCounter--;
    }

    /**
     * Returns a vector of new files in the specified directory that match the given
     * regular expression file mask.
     *
     * @param dir           The directory path to search for new files.
     * @param regexFileMask The regular expression file mask to match against file
     *                      names.
     * @return A vector of new files that match the given regular expression file
     *         mask.
     */
    private Vector<String> listOfNewFiles(String dir, String regexFileMask) {

        File f = new File(dir);
        String[] files = f.list();

        Arrays.sort(files);

        Vector<String> v = new Vector<String>();
        if(logger.isDebugEnabled()){
            logger.debug("*** found " + files.length + " files ***");
        }
        for (int i = 0; i < files.length; i++) {
            String file = files[i];
            Pattern pattern = Pattern.compile(regexFileMask);
            Matcher matcher = pattern.matcher(file);
            if(logger.isDebugEnabled()){
                logger.debug("(" + i + ") Testing... " + file);
            }
            if (matcher.find()) {
                String date = getTimeStampFromFileName(file, regexFileMask);
                long epoch = strTime2Long(date, timeFormat);
                logger.warn("Matching => " + file + " date = " + date + " epoch = " + epoch);
                if (epoch > latestProcessedTimestamp) {
                    logger.warn("New file => " + epoch);
                    latestProcessedTimestamp = epoch;
                    v.add(file);
                    postData(dir + "/" + file, epoch);
                }
            }
        }

        return v;
    }

    /*
     * Posting data to database
     */
    private boolean postData(String filePath, long timed) {

        parseFile(filePath);

        boolean success = true;

        Serializable[] stream = new Serializable[7];

        try {

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(rawData);
            oos.flush();
            oos.close();
            bos.close();

            logger.trace("size => " + bos.toByteArray().length);

            stream[0] = new Integer(ncols);
            stream[1] = new Integer(nrows);
            stream[2] = new Double(xllcorner);
            stream[3] = new Double(yllcorner);
            stream[4] = new Double(cellsize);
            stream[5] = new Double(NODATA_value);
            stream[6] = bos.toByteArray();

            if(logger.isDebugEnabled()){
                logger.debug("size => " + bos.toByteArray().length);
            }

            // testDeserialize(bos.toByteArray());

        } catch (IOException e) {
            logger.warn(e.getMessage(), e);
            success = false;
        }

        StreamElement se = new StreamElement(getOutputFormat(), stream, timed);

        if (success) {
            success = postStreamElement(se);
        }

        return success;
    }

    /*
     * Test deserialization
     */
    public static void testDeserialize(byte[] bytes) {

        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ObjectInputStream in = null;

            in = new ObjectInputStream(bis);

            Double deserial[][] = new Double[0][];

            deserial = (Double[][]) in.readObject();
            in.close();

            if(logger.isDebugEnabled()){
                logger.debug("deserial.length" + deserial.length);
                logger.debug("deserial[0].length" + deserial[0].length);
            }

            for (int i = 0; i < deserial.length; i++) {
                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < deserial[0].length; j++) {
                    sb.append(deserial[i][j]).append(" ");
                }
                logger.trace(sb.toString());
            }

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public String getWrapperName() {
        return "GridDataWrapper";
    }

    /**
     * Converts a string representation of time to a long value based on the
     * specified time format.
     *
     * @param s          the string representation of time
     * @param timeFormat the format of the time string
     * @return the long value representing the time, or -1 if the conversion fails
     */
    private long strTime2Long(String s, String timeFormat) {

        long l = -1;
        try {
            DateTimeFormatter fmt = DateTimeFormat.forPattern(timeFormat);
            l = fmt.parseDateTime(s).getMillis();
        } catch (java.lang.IllegalArgumentException e) {
            logger.warn(e.getMessage(), e);
        }
        return l;
    }

    /**
     * Extracts the timestamp from a given file name using a regular expression
     * mask.
     *
     * @param fileName  the name of the file
     * @param regexMask the regular expression mask to match against the file name
     * @return the extracted timestamp as a string, or null if no match is found
     */
    private String getTimeStampFromFileName(String fileName, String regexMask) {

        Pattern pattern = Pattern.compile(regexMask);
        Matcher matcher = pattern.matcher(fileName);
        if (matcher.find()) {
            if(logger.isDebugEnabled()){
                logger.debug("Date => " + matcher.group(1));
            }
            return matcher.group(1);
        } else {
            if(logger.isDebugEnabled()){
                logger.debug("Date => null");
            }
            return null;
        }
    }
}
