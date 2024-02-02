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
* File: src/ch/epfl/gsn/vsensor/ScheduledBridgeVirtualSensor.java
*
* @author bgpearn
*
*/

package ch.epfl.gsn.vsensor;

import java.sql.SQLException;
import java.util.Date;
import java.util.TimerTask;

import ch.epfl.gsn.ContainerImpl;

/**
 * ScheduledBridgeVirtualSensor extends AbstractScheduledVirtualSensor to
 * provide a bridge between physical sensors and virtual sensors. It allows data
 * from physical sensors to be passed to virtual sensors based on a scheduled
 * timer.
 */
public class ScheduledBridgeVirtualSensor extends AbstractScheduledVirtualSensor {

	/**
	 * Initializes the scheduled bridge virtual sensor by scheduling the timer task
	 * to run at the configured fixed rate.
	 *
	 * Super class initialize is called to get timer settings.
	 * A TimerTask is created and scheduled using the start time and clock rate.
	 * 
	 * @return true if initialization succeeded, false otherwise
	 */
	public boolean initialize() {
		super.initialize(); // get the timer settings
		TimerTask timerTask = new MyTimerTask();
		timer0.scheduleAtFixedRate(timerTask, new Date(startTime), clock_rate);
		return true;
	}

	/**
	 * TimerTask subclass that runs periodically to publish sensor data.
	 * Retrieves the latest dataItem, sets the timestamp, logs a message,
	 * and publishes the dataItem to the Container.
	 */
	class MyTimerTask extends TimerTask {

		public void run() {
			if (dataItem == null) {
				return;
			}
			dataItem.setTimeStamp(System.currentTimeMillis());
			logger.warn(getVirtualSensorConfiguration().getName() + " Timer Event ");
			try {
				ContainerImpl.getInstance().publishData(ScheduledBridgeVirtualSensor.this, dataItem);
			} catch (SQLException e) {
				if (e.getMessage().toLowerCase().contains("duplicate entry")) {
					logger.info(e.getMessage(), e);
				} else {
					logger.error(e.getMessage(), e);
				}
			}

		}
	}

	/**
	 * Cancels the timer that was scheduled to periodically run the timer task.
	 * This cleans up the timer resource when the virtual sensor is disposed.
	 */
	public void dispose() {
		timer0.cancel();

	}

}
