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
* File: src/ch/epfl/gsn/DataDistributer.java
*
* @author Ali Salehi
* @author Mehdi Riahi
* @author Timotee Maret
*
*/

package ch.epfl.gsn;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.LoggerFactory;

import ch.epfl.gsn.beans.StreamElement;
import ch.epfl.gsn.beans.VSensorConfig;
import ch.epfl.gsn.delivery.DefaultDistributionRequest;
import ch.epfl.gsn.delivery.DeliverySystem;
import ch.epfl.gsn.delivery.DistributionRequest;
import ch.epfl.gsn.networking.zeromq.ZeroMQDeliveryAsync;
import ch.epfl.gsn.networking.zeromq.ZeroMQDeliverySync;
import ch.epfl.gsn.storage.DataEnumerator;
import ch.epfl.gsn.storage.SQLValidator;
import ch.epfl.gsn.storage.StorageManager;

import org.slf4j.Logger;

public class DataDistributer implements VirtualSensorDataListener, VSensorStateChangeListener, Runnable {

    public static final int KEEP_ALIVE_PERIOD = 15 * 1000; // 15 sec.

    private static int keepAlivePeriod = -1;

    private javax.swing.Timer keepAliveTimer = null;

    private static transient Logger logger = LoggerFactory.getLogger(DataDistributer.class);

    private static HashMap<Class<? extends DeliverySystem>, DataDistributer> singletonMap = new HashMap<Class<? extends DeliverySystem>, DataDistributer>();
    private Thread thread;
    private HashMap<StorageManager, Connection> connections = new HashMap<StorageManager, Connection>();

    /**
     * Private constructor for the DataDistributer class.
     *
     * This constructor initializes a new Thread and starts it.
     * It also starts a keep-alive timer that periodically delivers a keep-alive
     * message to all registered listeners.
     * If a listener fails to receive the keep-alive message, it is removed from the
     * list of listeners.
     *
     * @throws RuntimeException if any exception occurs during the initialization of
     *                          the thread or the keep-alive timer.
     */
    private DataDistributer() {
        try {
            thread = new Thread(this);
            thread.start();
            thread.setName("DataDistributer");
            // Start the keep alive Timer -- Note that the implementation is backed by one
            // single thread for all the RestDelivery instances.
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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns an instance of DataDistributer associated with the given class.
     *
     * This method checks if an instance of DataDistributer is already associated
     * with the given class in the singletonMap.
     * If not, it creates a new instance of DataDistributer, associates it with the
     * class in the singletonMap, and returns it.
     * If an instance is already associated, it simply returns that instance.
     * This ensures that there is only one instance of DataDistributer per class
     * extending DeliverySystem.
     *
     * @param c The class extending DeliverySystem for which the DataDistributer
     *          instance is to be returned.
     * @return The instance of DataDistributer associated with the given class.
     */
    public static DataDistributer getInstance(Class<? extends DeliverySystem> c) {
        DataDistributer toReturn = singletonMap.get(c);
        if (toReturn == null) {
            singletonMap.put(c, (toReturn = new DataDistributer()));
        }

        return toReturn;
    }

    /**
     * Retrieves the keep-alive period for the remote connection.
     *
     * @return The keep-alive period for the remote connection.
     */
    public static int getKeepAlivePeriod() {
        if (keepAlivePeriod == -1) {
            keepAlivePeriod = System.getProperty("remoteKeepAlivePeriod") == null ? KEEP_ALIVE_PERIOD
                    : Integer.parseInt(System.getProperty("remoteKeepAlivePeriod"));
        }
        return keepAlivePeriod;
    }

    private HashMap<DistributionRequest, PreparedStatement> preparedStatements = new HashMap<DistributionRequest, PreparedStatement>();

    private ArrayList<DistributionRequest> listeners = new ArrayList<DistributionRequest>();

    private ConcurrentHashMap<DistributionRequest, DataEnumerator> candidateListeners = new ConcurrentHashMap<DistributionRequest, DataEnumerator>();

    private LinkedBlockingQueue<DistributionRequest> locker = new LinkedBlockingQueue<DistributionRequest>();

    private ConcurrentHashMap<DistributionRequest, Boolean> candidatesForNextRound = new ConcurrentHashMap<DistributionRequest, Boolean>();

    /**
     * Adds a new listener to the Distributer.
     *
     * This method is synchronized on the listeners list to prevent concurrent
     * modification.
     * If the listener is not already in the list, it is added and a new
     * PreparedStatement is prepared for it.
     * The PreparedStatement is also stored for future use.
     * If the listener is already in the list, an informational message is logged
     * and no action is taken
     *
     * @param listener The DistributionRequest to be added as a listener.
     *                 This object encapsulates the details of the request,
     *                 including the SQL query to be executed.
     * @throws RuntimeException If an error occurs while preparing the SQL
     *                          statement.
     */
    public void addListener(DistributionRequest listener) {
        synchronized (listeners) {
            if (listeners.contains(listener)) {
                logger.info("Adding a listener to Distributer failed, duplicated listener! " + listener.toString());

            } else {
                logger.info("Adding a listener to Distributer:" + listener.toString());
                boolean needsAnd = SQLValidator.removeSingleQuotes(SQLValidator.removeQuotes(listener.getQuery()))
                        .indexOf(" where ") > 0;
                String query = SQLValidator.addPkField(listener.getQuery());
                if (needsAnd) {
                    query += " AND ";
                } else {
                    query += " WHERE ";
                }

                query += " timed > ? and pk > ? order by pk asc "; // both have to be parameters to force the optimizer
                                                                   // of Postgres < 9.2 to not scan on timed index
                PreparedStatement prepareStatement = null;
                try {
                    prepareStatement = getPersistantConnection(listener.getVSensorConfig()).prepareStatement(query); // prepareStatement
                                                                                                                     // =
                                                                                                                     // StorageManager.getInstance().getConnection().prepareStatement(query);
                    prepareStatement.setMaxRows(1000); // Limit the number of rows loaded in memory.
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                preparedStatements.put(listener, prepareStatement);
                listeners.add(listener);
                addListenerToCandidates(listener);
            }
        }
    }

    /**
     * Adds a new listener to the candidate listeners list.
     *
     *
     * @param listener The DistributionRequest to be added as a listener.
     *                 This object encapsulates the details of the request,
     *                 including the SQL query to be executed.
     */
    private void addListenerToCandidates(DistributionRequest listener) {
        /**
         * Locker variable should be modified EXACTLY like candidateListeners variable.
         */
        if(logger.isDebugEnabled()){
            logger.debug("Adding the listener: " + listener.toString() + " to the candidates.");
        }
        DataEnumerator dataEnum = makeDataEnum(listener);
        if (dataEnum.hasMoreElements()) {
            candidateListeners.put(listener, dataEnum);
            locker.add(listener);
        }
    }

    /**
     * Removes a listener from the candidate listeners list.
     *
     *
     * @param listener The DistributionRequest to be removed.
     *                 This object encapsulates the details of the request,
     *                 including the SQL query to be executed.
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
    private boolean flushStreamElement(DataEnumerator dataEnum, DistributionRequest listener) {
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
        boolean success = listener.deliverStreamElement(se);
        if (!success) {
            if(logger.isDebugEnabled()){
                logger.debug("FLushing an stream element failed, delivery failure [Listener: " + listener.toString() + "]");
            }
            return false;
        }
        if(logger.isDebugEnabled()){
            logger.debug("Flushing an stream element succeed [Listener: " + listener.toString() + "]");
        }
        return true;
    }

    /**
     * Removes a listener from the distribution system.
     *
     *
     * @param listener The DistributionRequest to be removed.
     *                 This object encapsulates the details of the request,
     *                 including the SQL query to be executed.
     * @throws SQLException If an SQL error occurs while closing the
     *                      PreparedStatement associated with the listener.
     */
    public void removeListener(DistributionRequest listener) {
        synchronized (listeners) {
            if (listeners.remove(listener)) {
                try {
                    candidatesForNextRound.remove(listener);
                    removeListenerFromCandidates(listener);
                    preparedStatements.get(listener).close();
                    listener.close();
                    logger.info(
                            "Removing listener completely from Distributer [Listener: " + listener.toString() + "]");
                } catch (SQLException e) {
                    logger.error(e.getMessage(), e);
                } finally {
                    preparedStatements.remove(listener);
                }
            }
        }
    }

    /**
     * Consumes a StreamElement and distributes it to the appropriate listeners.
     *
     * This method iterates over all registered listeners and checks if the provided
     * VSensorConfig matches the one
     * associated with the listener.
     * If a match is found, a debug log message is generated and the listener is
     * added to the candidates for the next round of distribution.
     * If the listener is already a candidate, its status is updated to TRUE in the
     * candidatesForNextRound map.
     *
     * @param se     The StreamElement to be consumed. This object encapsulates the
     *               data to be distributed.
     * @param config The VSensorConfig associated with the StreamElement. This
     *               object contains configuration details for the virtual sensor
     *               that produced the StreamElement.
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

            for (Entry<DistributionRequest, DataEnumerator> item : candidateListeners.entrySet()) {
                boolean success = flushStreamElement(item.getValue(), item.getKey());
                if (success == false) {
                    removeListener(item.getKey());
                } else {
                    if (!item.getValue().hasMoreElements()) {
                        removeListenerFromCandidates(item.getKey());
                        // As we are limiting the number of elements returned by the JDBC driver
                        // we consume the eventual remaining items.
                        consume(null, item.getKey().getVSensorConfig());
                    }
                }
            }
        }
    }

    /**
     * This method is used to load a virtual sensor configuration and add it as a
     * listener.
     * If ZeroMQ is enabled and the current instance is of type ZeroMQDeliverySync,
     * a new ZeroMQDeliveryAsync is created and added as a listener.
     *
     * @param config The virtual sensor configuration to be loaded.
     * @return Always returns true.
     *
     * @throws IOException  If an I/O error occurs when creating the
     *                      ZeroMQDeliveryAsync.
     * @throws SQLException If a database access error occurs when creating the
     *                      DefaultDistributionRequest.
     */
    public boolean vsLoading(VSensorConfig config) {
        synchronized (listeners) {
            if (Main.getContainerConfig().isZMQEnabled() && getInstance(ZeroMQDeliverySync.class) == this) {
                try {
                    DeliverySystem delivery = new ZeroMQDeliveryAsync(config.getName());
                    addListener(DefaultDistributionRequest.create(delivery, config, "select * from " + config.getName(),
                            System.currentTimeMillis()));
                } catch (IOException e1) {
                    logger.error(e1.getMessage(), e1);
                } catch (SQLException e1) {
                    logger.error(e1.getMessage(), e1);
                }
            }
        }
        return true;
    }

    /**
     * Unloads a VSensorConfig from the DataDistributer.
     * Removes all DistributionRequest listeners associated with the given
     * VSensorConfig.
     * 
     * @param config the VSensorConfig to be unloaded
     * @return true if the unloading was successful, false otherwise
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
     * Creates a DataEnumerator object for iterating over data records.
     *
     * @param listener The DistributionRequest object containing the necessary
     *                 information for data distribution.
     * @return A DataEnumerator object for iterating over data records.
     */
    private DataEnumerator makeDataEnum(DistributionRequest listener) {

        PreparedStatement prepareStatement = preparedStatements.get(listener);
        try {
            // last time can be also used, but must change > to >= in the query for
            // non-unique timestamps
            // and it works only with totally ordered streams
            prepareStatement.setLong(1, listener.getStartTime());
            prepareStatement.setLong(2, listener.getLastVisitedPk());
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
            return new DataEnumerator();
        }

        DataEnumerator dataEnum = new DataEnumerator(Main.getStorage(listener.getVSensorConfig().getName()),
                prepareStatement, false, true);
        return dataEnum;
    }

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
     * Checks if the DataDistributer contains a specific DeliverySystem.
     *
     * @param delivery the DeliverySystem to check for
     * @return true if the DataDistributer contains the specified DeliverySystem,
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

    /**
     * Retrieves a persistent connection for the given VSensorConfig.
     *
     * @param config the VSensorConfig for which to retrieve the connection
     * @return a persistent Connection object
     * @throws Exception if an error occurs while retrieving the connection
     */
    public Connection getPersistantConnection(VSensorConfig config) throws Exception {
        StorageManager sm = Main.getStorage(config);
        Connection c = connections.get(sm);
        if (c == null) {
            c = sm.getConnection();
            c.setReadOnly(true);
            connections.put(sm, c);
        }
        return c;
    }

}
