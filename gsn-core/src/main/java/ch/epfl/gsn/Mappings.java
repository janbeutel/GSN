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
* File: src/ch/epfl/gsn/Mappings.java
*
* @author Ali Salehi
* @author Mehdi Riahi
* @author Timotee Maret
*
*/

package ch.epfl.gsn;

import java.util.Iterator;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.LoggerFactory;

import ch.epfl.gsn.beans.DataField;
import ch.epfl.gsn.beans.VSensorConfig;

import org.slf4j.Logger;

public final class Mappings {

   private static final ConcurrentHashMap<String, VSensorConfig> vsNameTOVSConfig = new ConcurrentHashMap<String, VSensorConfig>();

   private static final ConcurrentHashMap<String, VirtualSensor> fileNameToVSInstance = new ConcurrentHashMap<String, VirtualSensor>();

   private static final ConcurrentHashMap<String, TreeMap<String, Boolean>> vsNamesToOutputStructureFields = new ConcurrentHashMap<String, TreeMap<String, Boolean>>();

   private static final transient Logger logger = LoggerFactory.getLogger(Mappings.class);

   /**
    * Adds a virtual sensor instance to the sensor pool.
    * 
    * @param sensorPool The virtual sensor instance to be added.
    * @return true if the virtual sensor instance was added successfully, false
    *         otherwise.
    */
   public static boolean addVSensorInstance(VirtualSensor sensorPool) {
      try {
         logger.info("Testing the pool for :" + sensorPool.getConfig().getName());
         sensorPool.returnVS(sensorPool.borrowVS());
      } catch (Exception e) {
         sensorPool.closePool();
         logger.error("GSN can't load the virtual sensor specified at " + sensorPool.getConfig().getFileName()
               + " because the initialization of the virtual sensor failed. " + e.getMessage(), e);
         return false;
      }
      TreeMap<String, Boolean> vsNameToOutputStructureFields = new TreeMap<String, Boolean>();
      vsNamesToOutputStructureFields.put(sensorPool.getConfig().getName(), vsNameToOutputStructureFields);
      for (DataField fields : sensorPool.getConfig().getOutputStructure()) {
         vsNameToOutputStructureFields.put(fields.getName(), Boolean.TRUE);
      }
      vsNameToOutputStructureFields.put("timed", Boolean.TRUE);
      vsNameTOVSConfig.put(sensorPool.getConfig().getName(), sensorPool.getConfig());
      fileNameToVSInstance.put(sensorPool.getConfig().getFileName(), sensorPool);
      return true;
   }

   /**
    * Retrieves an instance of VirtualSensor based on the given file name.
    *
    * @param fileName the name of the file associated with the VirtualSensor
    *                 instance
    * @return the VirtualSensor instance associated with the given file name, or
    *         null if not found
    */
   public static VirtualSensor getVSensorInstanceByFileName(String fileName) {
      return fileNameToVSInstance.get(fileName);
   }

   /**
    * Retrieves the mapping of output structure fields for a given virtual sensor
    * name.
    *
    * @param vsName the name of the virtual sensor
    * @return a TreeMap containing the mapping of output structure fields
    */
   public static final TreeMap<String, Boolean> getVsNamesToOutputStructureFieldsMapping(String vsName) {
      return vsNamesToOutputStructureFields.get(vsName);
   }

   /**
    * Retrieves the configuration for a virtual sensor based on its name.
    *
    * @param vSensorName the name of the virtual sensor
    * @return the configuration for the virtual sensor, or null if the name is null
    *         or not found
    */
   public static VSensorConfig getVSensorConfig(String vSensorName) {
      if (vSensorName == null) {
         return null;
      }
      return vsNameTOVSConfig.get(vSensorName);
   }

   /**
    * Removes the specified filename from the mappings.
    * If the filename exists in the mappings, it will be removed along with its
    * associated VSensorConfig.
    *
    * @param fileName the name of the file to be removed from the mappings
    */
   public static void removeFilename(String fileName) {
      if (fileNameToVSInstance.containsKey(fileName)) {
         VSensorConfig config = fileNameToVSInstance.get(fileName).getConfig();
         vsNameTOVSConfig.remove(config.getName());
         fileNameToVSInstance.remove(fileName);
      }
   }

   /**
    * Returns the last modified time of the specified file.
    *
    * @param configFileName the name of the configuration file
    * @return the last modified time of the file as a Long value
    */
   public static Long getLastModifiedTime(String configFileName) {
      return Long.valueOf(fileNameToVSInstance.get(configFileName).getLastModified());
   }

   /**
    * Returns an array of all known file names.
    *
    * @return an array of strings representing the file names
    */
   public static String[] getAllKnownFileName() {
      return fileNameToVSInstance.keySet().toArray(new String[0]);
   }

   /**
    * Retrieves the configuration object for a given file name.
    *
    * @param fileName the name of the file
    * @return the configuration object associated with the file name, or null if
    *         the file name is null
    */
   public static VSensorConfig getConfigurationObject(String fileName) {
      if (fileName == null) {
         return null;
      }
      return fileNameToVSInstance.get(fileName).getConfig();
   }

   /**
    * Returns an iterator over all the VSensorConfigs in the Mappings.
    *
    * @return an iterator over all the VSensorConfigs
    */
   public static Iterator<VSensorConfig> getAllVSensorConfigs() {
      return vsNameTOVSConfig.values().iterator();
   }

   /**
    * Retrieves an instance of VirtualSensor based on the given virtual sensor
    * name.
    * 
    * @param vsensorName the name of the virtual sensor
    * @return an instance of VirtualSensor if found, otherwise null
    */
   public static VirtualSensor getVSensorInstanceByVSName(String vsensorName) {
      if (vsensorName == null) {
         return null;
      }
      VSensorConfig vSensorConfig = vsNameTOVSConfig.get(vsensorName);
      if (vSensorConfig == null) {
         return null;
      }
      return getVSensorInstanceByFileName(vSensorConfig.getFileName());
   }

   /**
    * Case insensitive matching.
    * 
    * @param vsName
    * @return
    */
   public static VSensorConfig getConfig(String vsName) {
      Iterator<VSensorConfig> configs = Mappings.getAllVSensorConfigs();
      while (configs.hasNext()) {
         VSensorConfig config = configs.next();
         if (config.getName().equalsIgnoreCase(vsName)) {
            return config;
         }
      }
      return null;
   }
}
