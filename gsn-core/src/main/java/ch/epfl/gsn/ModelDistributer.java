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
* File: src/ch/epfl/gsn/ModelDistributer.java
*
* @author Julien Eberle
*
*/

package ch.epfl.gsn;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.LoggerFactory;

import ch.epfl.gsn.beans.StreamElement;
import ch.epfl.gsn.beans.VSensorConfig;
import ch.epfl.gsn.delivery.DeliverySystem;
import ch.epfl.gsn.delivery.DistributionRequest;
import ch.epfl.gsn.storage.DataEnumeratorIF;
import ch.epfl.gsn.storage.ModelEnumerator;

import org.slf4j.Logger;

public class ModelDistributer implements VirtualSensorDataListener, VSensorStateChangeListener, Runnable {

    public static final int KEEP_ALIVE_PERIOD = 15 * 1000; // 15 sec.

    private static int keepAlivePeriod = -1;

    private javax.swing.Timer keepAliveTimer = null;

    private static transient Logger logger = LoggerFactory.getLogger(ModelDistributer.class);

    private static HashMap<Class<? extends DeliverySystem>, ModelDistributer> singletonMap = new HashMap<Class<? extends DeliverySystem>, ModelDistributer>();
    private Thread thread;

    /**
     * Constructs an instance of ModelDistributer, initializing the associated
     * thread and keep-alive timer.
     *
     * This constructor creates a new ModelDistributer instance, starting a
     * dedicated thread to handle distribution
     * tasks. It also initializes a keep-alive timer responsible for periodically
     * sending keep-alive messages to registered
     * listeners. The keep-alive messages help maintain the connection with
     * listeners. If any exceptions occur during the
     * construction or initialization process, a RuntimeException is thrown.
     */
    private ModelDistributer() {
        try {
            thread = new Thread(this);
            thread.start();
            // Start the keep alive Timer -- Note that the implementation is backed by one
            // single thread for all the Delivery instances.
            keepAliveTimer = new javax.swing.Timer(getKeepAlivePeriod(), new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    // write the keep alive message to the stream
                    synchronized (listeners) {
                        ArrayList<DistributionRequest> clisteners = (ArrayList<DistributionRequest>) listeners.clone();
                        for (DistributionRequest listener : clisteners) {
                            if (!listener.deliverKeepAliveMessage()) {
                                if (logger.isDebugEnabled()) {
                                    logger.debug("remove the listener.");
                                }
                                removeListener(listener);
                            }
                        }
                    }
                }
            });
            keepAliveTimer.start();
            // } catch (SQLException e) {
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves the singleton instance of ModelDistributer associated with the
     * specified DeliverySystem class.
     *
     * This method returns the existing singleton instance of ModelDistributer if
     * available in the singletonMap,
     * associated with the provided DeliverySystem class. If no instance is found, a
     * new ModelDistributer instance is
     * created, associated with the specified DeliverySystem class, and stored in
     * the singletonMap for future access.
     * This method ensures that only one ModelDistributer instance is associated
     * with each unique DeliverySystem class.
     *
     * @param c The class object representing the DeliverySystem class for which to
     *          retrieve the ModelDistributer instance.
     * @return The singleton instance of ModelDistributer associated with the
     *         specified DeliverySystem class.
     */
    public static ModelDistributer getInstance(Class<? extends DeliverySystem> c) {
        ModelDistributer toReturn = singletonMap.get(c);
        if (toReturn == null) {
            singletonMap.put(c, (toReturn = new ModelDistributer()));
        }

        return toReturn;
    }

    /**
     * Returns the keep-alive period for remote connections.
     * If the keep-alive period is not set, it will be retrieved from the system
     * property "remoteKeepAlivePeriod".
     * If the system property is not set, the default keep-alive period will be
     * used.
     *
     * @return the keep-alive period in milliseconds
     */
    public static int getKeepAlivePeriod() {
        if (keepAlivePeriod == -1) {
            keepAlivePeriod = System.getProperty("remoteKeepAlivePeriod") == null ? KEEP_ALIVE_PERIOD
                    : Integer.parseInt(System.getProperty("remoteKeepAlivePeriod"));
        }

        return keepAlivePeriod;
    }

    private ArrayList<DistributionRequest> listeners = new ArrayList<DistributionRequest>();

    private LinkedBlockingQueue<DistributionRequest> locker = new LinkedBlockingQueue<DistributionRequest>();

    private ConcurrentHashMap<DistributionRequest, DataEnumeratorIF> candidateListeners = new ConcurrentHashMap<DistributionRequest, DataEnumeratorIF>();

    private ConcurrentHashMap<DistributionRequest, Boolean> candidatesForNextRound = new ConcurrentHashMap<DistributionRequest, Boolean>();

    /**
     * Adds a DistributionRequest listener to the ModelDistributer.
     * 
     * @param listener The DistributionRequest listener to be added.
     */
    public void addListener(DistributionRequest listener) {
        synchronized (listeners) {
            if (listeners.contains(listener)) {
                logger.info(
                    "Adding a listener to ModelDistributer failed, duplicated listener! " + listener.toString());

            } else {
                logger.info("Adding a listener to ModelDistributer:" + listener.toString());

                listeners.add(listener);
                addListenerToCandidates(listener);
                
            }
        }
    }

    /**
     * Adds the specified DistributionRequest listener to the candidateListeners
     * map.
     *
     * @param listener The DistributionRequest listener to be added to the
     *                 candidateListeners map.
     */
    private void addListenerToCandidates(DistributionRequest listener) {
        /**
         * Locker variable should be modified EXACTLY like candidateListeners variable.
         */
        if(logger.isDebugEnabled()){
            logger.debug("Adding the listener: " + listener.toString() + " to the candidates.");
        }
        DataEnumeratorIF dataEnum = makeDataEnum(listener);
        if (dataEnum.hasMoreElements()) {
            candidateListeners.put(listener, dataEnum);
            locker.add(listener);
        }
    }

    /**
     * Removes the specified listener from the candidate list.
     * If the listener is found in the candidatesForNextRound list, it is removed
     * from the list and added to the candidateListeners map.
     * If the listener is not found in the candidatesForNextRound list, it is
     * removed from the locker and candidateListeners map.
     * 
     * @param listener the DistributionRequest listener to be removed
     */
    private void removeListenerFromCandidates(DistributionRequest listener) {
        /**
         * Locker variable should be modified EXACTLY like candidateListeners variable.
         */
        if(logger.isDebugEnabled()){
            logger.debug("Updating the candidate list [" + listener.toString() + " (removed)].");
        }
        if (candidatesForNextRound.contains(listener)) {
            candidateListeners.put(listener, makeDataEnum(listener));
            candidatesForNextRound.remove(listener);
        } else {
            locker.remove(listener);
            candidateListeners.remove(listener);
        }
    }

    /**
     * This method only flushes one single stream element from the provided data
     * enumerator.
     * Returns false if the flushing the stream element fails. This method also
     * cleans the prepared statements by removing the listener completely.
     *
     * @param dataEnum
     * @param listener
     * @return
     */
    private boolean flushStreamElement(DataEnumeratorIF dataEnum, DistributionRequest listener) {
        if (listener.isClosed()) {
            if(logger.isDebugEnabled()){
                logger.debug("Flushing an stream element failed, isClosed=true [Listener: " + listener.toString() + "]");
            }    
            return false;
        }

        if (!dataEnum.hasMoreElements()) {
            if(logger.isDebugEnabled()){
                logger.debug("Nothing to flush to [Listener: " + listener.toString() + "]");
            }
            return true;
        }

        StreamElement se = dataEnum.nextElement();
        // boolean success = true;
        boolean success = listener.deliverStreamElement(se);
        if (!success) {
            logger.warn("FLushing an stream element failed, delivery failure [Listener: " + listener.toString() + "]");
            return false;
        }
        if(logger.isDebugEnabled()){
            logger.debug("Flushing an stream element succeed [Listener: " + listener.toString() + "]");
        }
        return true;
    }

    /**
     * Removes a DistributionRequest listener from the ModelDistributer.
     * 
     * @param listener the DistributionRequest listener to be removed
     */
    public void removeListener(DistributionRequest listener) {
        synchronized (listeners) {
            if (listeners.remove(listener)) {
                candidatesForNextRound.remove(listener);
                removeListenerFromCandidates(listener);
                listener.close();
                if(logger.isDebugEnabled()){
                    logger.debug("Removing listener completely from Distributer [Listener: " + listener.toString() + "]");
                }
            }
        }
    }

    /**
     * Consumes a StreamElement and distributes it to the appropriate listeners
     * based on the provided VSensorConfig.
     * 
     * @param se     the StreamElement to be consumed
     * @param config the VSensorConfig used to determine the appropriate listeners
     */
    public void consume(StreamElement se, VSensorConfig config) {
        synchronized (listeners) {
            for (DistributionRequest listener : listeners) {
                if (listener.getVSensorConfig() == config) {
                    if(logger.isDebugEnabled()){
                        logger.debug("sending stream element " + (se == null ? "second-chance-se" : se.toString())
                            + " produced by " + config.getName() + " to listener =>" + listener.toString());
                    }
                    if (candidateListeners.containsKey(listener)) {
                        candidatesForNextRound.put(listener, Boolean.TRUE);
                    } else {
                        addListenerToCandidates(listener);
                    }
                }
            }
        }
    }

    /**
     * The main execution loop of the ModelDistributer thread.
     *
     * This method represents the main execution loop of the ModelDistributer
     * thread. It continuously checks the
     * locker's status, waiting for requests or data items. If the locker is empty,
     * it logs a debug message indicating
     * that it is waiting for requests or data items, along with the total number of
     * listeners. When the locker is not
     * empty, it releases the lock and attempts to find interest listeners by
     * iterating through the candidateListeners
     * map.
     */
    public void run() {
        while (true) {
            try {
                if (locker.isEmpty()) {
                    if(logger.isDebugEnabled()){
                        logger.debug("Waiting(locked) for requests or data items, Number of total listeners: "
                            + listeners.size());
                    }
                    locker.put(locker.take());
                    if(logger.isDebugEnabled()){
                        logger.debug("Lock released, trying to find interest listeners (total listeners:" + listeners.size()
                            + ")");
                    }
                }
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }

            for (Entry<DistributionRequest, DataEnumeratorIF> item : candidateListeners.entrySet()) {
                boolean success = flushStreamElement(item.getValue(), item.getKey());
                if (success == false) {
                    removeListener(item.getKey());
                } else {
                    if (!item.getValue().hasMoreElements()) {
                        removeListenerFromCandidates(item.getKey());
                        // As we are limiting the number of elements returned by the JDBC driver
                        // we consume the eventual remaining items.
                        // consume(null, item.getKey().getVSensorConfig()); //do not activate for models
                        // !!!!!
                    }
                }
            }
        }
    }

    public boolean vsLoading(VSensorConfig config) {
        return true;
    }

    /**
     * Unloads a VSensorConfig from the ModelDistributer by removing all listeners
     * associated with it.
     * 
     * @param config the VSensorConfig to be unloaded
     * @return true if the unloading is successful, false otherwise
     */
    public boolean vsUnLoading(VSensorConfig config) {
        synchronized (listeners) {
            if(logger.isDebugEnabled()){
                logger.debug("Distributer unloading: " + listeners.size());
            }
            ArrayList<DistributionRequest> toRemove = new ArrayList<DistributionRequest>();
            for (DistributionRequest listener : listeners) {
                if (listener.getVSensorConfig() == config) {
                    toRemove.add(listener);
                }
            }
            for (DistributionRequest listener : toRemove) {
                try {
                    removeListener(listener);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        return true;
    }

    /**
     * Creates a DataEnumeratorIF object based on the provided DistributionRequest
     * listener.
     *
     * @param listener The DistributionRequest listener containing the query and
     *                 model information.
     * @return A DataEnumeratorIF object.
     */
    private DataEnumeratorIF makeDataEnum(DistributionRequest listener) {
        ModelEnumerator mEnum = new ModelEnumerator(listener.getQuery(), listener.getModel());
        return mEnum;
    }

    /**
     * Releases the resources held by the ModelDistributer.
     * This method removes all listeners and stops the keep alive timer.
     */
    public void release() {
        synchronized (listeners) {
            while (!listeners.isEmpty()) {
                removeListener(listeners.get(0));
            }
        }
        if (keepAliveTimer != null) {
            keepAliveTimer.stop();
        }

    }

    /**
     * Checks if the ModelDistributer contains a specific DeliverySystem.
     *
     * @param delivery the DeliverySystem to check for
     * @return true if the ModelDistributer contains the specified DeliverySystem,
     *         false otherwise
     */
    public boolean contains(DeliverySystem delivery) {
        synchronized (listeners) {
            for (DistributionRequest listener : listeners) {
                if (listener.getDeliverySystem().equals(delivery)) {
                    return true;
                }
            }
            return false;
        }

    }

}
