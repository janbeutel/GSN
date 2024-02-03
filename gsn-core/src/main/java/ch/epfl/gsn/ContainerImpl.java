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
* File: src/ch/epfl/gsn/ContainerImpl.java
*
* @author Jerome Rousselot
* @author gsn_devs
* @author Ali Salehi
* @author Timotee Maret
*
*/

package ch.epfl.gsn;

import java.sql.SQLException;
import java.util.ArrayList;
import ch.epfl.gsn.beans.StreamElement;
import ch.epfl.gsn.storage.StorageManager;
import ch.epfl.gsn.vsensor.AbstractVirtualSensor;

public class ContainerImpl {

	/**
	 * The <code> waitingVirtualSensors</code> contains the virtual sensors that
	 * recently produced data. This variable is useful for batch processing timed
	 * couple virtual sensor produce data.
	 *
	 *
	 * In the <code>registeredQueries</code> the key is the local virtual
	 * sensor name.
	 */

	private static ContainerImpl singleton;
	private static final Object psLock = new Object();
	private ArrayList<VirtualSensorDataListener> dataListeners = new ArrayList<VirtualSensorDataListener>();

	private ContainerImpl() {
	}

	/**
	 * Returns the singleton instance of the ContainerImpl class.
	 *
	 * <p>
	 * This method implements the Singleton Design Pattern, which ensures that only
	 * one instance of the ContainerImpl class is created.
	 * If the singleton instance is null, a new instance is created and returned.
	 * If the singleton instance already exists, the existing instance is returned.
	 * </p>
	 *
	 * @return The singleton instance of the ContainerImpl class.
	 */
	public static ContainerImpl getInstance() {
		if (singleton == null) {
			singleton = new ContainerImpl();
		}
		return singleton;
	}

	/**
	 * Publishes data from a virtual sensor to a storage manager and notifies
	 * registered listeners.
	 *
	 * This method retrieves the name of the virtual sensor from its configuration.
	 * It then retrieves a StorageManager instance associated with the virtual
	 * sensor's name.
	 * The method then executes an insert operation on the StorageManager, passing
	 * the sensor's name, its output structure, and the data.
	 *
	 * After the data has been stored, the method iterates over all registered
	 * VirtualSensorDataListener objects and calls their consume method,
	 * passing the data and the virtual sensor's configuration. This operation
	 * notifies the listener about the new data, allowing it to process it as
	 * needed.
	 *
	 * @param sensor The virtual sensor from which data is to be published.
	 * @param data   The data to be published.
	 * @throws SQLException If any SQL-related errors occur during the execution of
	 *                      the storage operation.
	 */
	public void publishData(AbstractVirtualSensor sensor, StreamElement data) throws SQLException {
		String name = sensor.getVirtualSensorConfiguration().getName().toLowerCase();
		StorageManager storageMan = Main.getStorage(sensor.getVirtualSensorConfiguration().getName());
		synchronized (psLock) {
			storageMan.executeInsert(name, sensor.getVirtualSensorConfiguration().getOutputStructure(), data);
		}

		for (VirtualSensorDataListener listener : dataListeners) {
			listener.consume(data, sensor.getVirtualSensorConfiguration());
		}
	}

	/**
	 * Adds a VirtualSensorDataListener to the list of data listeners if it is not
	 * already present.
	 *
	 *
	 * @param listener The VirtualSensorDataListener to be added to the
	 *                 dataListeners list.
	 */
	public synchronized void addVSensorDataListener(VirtualSensorDataListener listener) {
		if (!dataListeners.contains(listener)) {
			dataListeners.add(listener);
		}
	}

	/**
	 * Removes a VirtualSensorDataListener from the list of data listeners.
	 *
	 * @param listener The VirtualSensorDataListener to be removed from the
	 *                 dataListeners list.
	 */
	public synchronized void removeVSensorDataListener(VirtualSensorDataListener listener) {
		dataListeners.remove(listener);
	}

}
