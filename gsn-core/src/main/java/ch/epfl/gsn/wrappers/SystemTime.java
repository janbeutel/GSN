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
* File: src/ch/epfl/gsn/wrappers/SystemTime.java
*
* @author gsn_devs
* @author Mehdi Riahi
* @author Ali Salehi
* @author Davide De Sclavis
* @author Manuel Buchauer
* @author Jan Beutel
*
*/

package ch.epfl.gsn.wrappers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.Timer;

import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.buffer.SynchronizedBuffer;
import org.apache.commons.collections.buffer.UnboundedFifoBuffer;
import org.apache.commons.lang.math.RandomUtils;
import org.slf4j.LoggerFactory;

import ch.epfl.gsn.beans.AddressBean;
import ch.epfl.gsn.beans.DataField;
import ch.epfl.gsn.beans.StreamElement;

import org.slf4j.Logger;

/**
 * This wrapper presents the system current clock.
 */

public class SystemTime extends AbstractWrapper implements ActionListener {

  public static final String CLOCK_PERIOD_KEY = "clock-period";

  public static final String MAX_DELAY_KEY = "max-delay";

  private static final Serializable[] EMPTY_DATA_PART = new Serializable[] {};

  private static final Byte[] EMPTY_FIELD_TYPES = new Byte[] {};

  private static final int DEFAULT_CLOCK_PERIODS = 1 * 1000;

  private static final int DEFAULT_MAX_DELAY = -1;// 5 seconds;

  private String[] EMPTY_FIELD_LIST = new String[] {};

  private DataField[] collection = new DataField[] {};

  private static int threadCounter = 0;

  private transient Logger logger = LoggerFactory.getLogger(this.getClass());

  private Timer timer;

  private boolean delayPostingElements = false;

  private int maximumDelay = DEFAULT_MAX_DELAY;

  private Buffer streamElementBuffer;

  private Object objectLock = new Object();

  public boolean initialize() {
    AddressBean addressBean = getActiveAddressBean();
    // TODO: negative values?
    timer = new Timer(addressBean.getPredicateValueAsInt(CLOCK_PERIOD_KEY, DEFAULT_CLOCK_PERIODS), this);
    maximumDelay = addressBean.getPredicateValueAsInt(MAX_DELAY_KEY, DEFAULT_MAX_DELAY);
    if (maximumDelay > 0) {
      streamElementBuffer = SynchronizedBuffer.decorate(new UnboundedFifoBuffer());
      delayPostingElements = true;
      if (timer.getDelay() < maximumDelay) {
        logger.warn(
            "Maximum delay is greater than element production interval. Running for a long time may lead to an OutOfMemoryException");
      }
    }
    return true;
  }

  /**
   * Runs the process of posting stream elements with optional delay.
   * If delayPostingElements is true, the method will continuously check for
   * stream elements in the buffer,
   * and if available, it will post them with a random delay.
   * The method will keep running until isActive() returns false.
   */
  public void run() {
    timer.start();
    if (delayPostingElements) {
      if(logger.isDebugEnabled()){
        logger.debug("Starting <" + getWrapperName() + "> with delayed elements.");
      }
      while (isActive()) {
        synchronized (objectLock) {
          while (streamElementBuffer.isEmpty()) {
            try {
              objectLock.wait();
            } catch (InterruptedException e) {
              logger.error(e.getMessage(), e);
            }
          }
        }
        try {
          int nextInt = RandomUtils.nextInt(maximumDelay);
          Thread.sleep(nextInt);
          // System.out.println("next delay : " + nextInt + " --> buffer size : " +
          // streamElementBuffer.size());
        } catch (InterruptedException e) {
          logger.error(e.getMessage(), e);
        }

        if (!streamElementBuffer.isEmpty()) {
          StreamElement nextStreamElement = (StreamElement) streamElementBuffer.remove();
          postStreamElement(nextStreamElement);
        }
      }
    }
  }

  public DataField[] getOutputFormat() {
    return collection;
  }

  /**
   * Performs the action associated with this event.
   * Creates a StreamElement object with empty field list, empty field types,
   * empty data part, and the timestamp from the action event.
   * If delayPostingElements is true, adds the stream element to the buffer and
   * notifies all threads waiting on the object lock.
   * Otherwise, directly posts the stream element.
   *
   * @param actionEvent the action event associated with this action
   */
  public void actionPerformed(ActionEvent actionEvent) {
    StreamElement streamElement = new StreamElement(EMPTY_FIELD_LIST, EMPTY_FIELD_TYPES, EMPTY_DATA_PART,
        actionEvent.getWhen());
    if (delayPostingElements) {
      streamElementBuffer.add(streamElement);
      synchronized (objectLock) {
        objectLock.notifyAll();
      }
    } else {
      postStreamElement(streamElement);
    }
  }

  public void dispose() {
    timer.stop();
    threadCounter--;
  }

  public String getWrapperName() {
    return "System Time";
  }

  public int getTimerClockPeriod() {
    return timer.getDelay();
  }

}
