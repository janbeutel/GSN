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
* File: src/ch/epfl/gsn/utils/protocols/ProtocolManager.java
*
* @author Jerome Rousselot
* @author Ali Salehi
*
*/

package ch.epfl.gsn.utils.protocols;

import java.util.Collection;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.naming.OperationNotSupportedException;

import org.slf4j.LoggerFactory;

import ch.epfl.gsn.wrappers.AbstractWrapper;

import org.slf4j.Logger;

/**
 * This class implements a generic finite state machine
 * for HostControllerInterface Protocols.
 * For simple protocols that never wait for an answer
 * from the controller, simply create a ProtocolManager
 * instance with the appropriate Protocol object and
 * then call the method sendQuery.
 * 
 * Warning: other methods of this class may be refactored soon,
 * and more states could be added.
 * 
 * @see AbstractHCIProtocol
 */
public class ProtocolManager {
	private static final transient Logger logger = LoggerFactory.getLogger(ProtocolManager.class);
	private AbstractHCIProtocol protocol;
	private ProtocolStates currentState;
	private AbstractHCIQuery lastExecutedQuery = null;
	private Vector<Object> lastParams;
	private AbstractWrapper outputWrapper;

	private Timer timer;
	private TimerTask answerTimeout = new TimerTask() {

		public synchronized void run() {
			lastExecutedQuery = null;
			currentState = ProtocolStates.READY;
		}
	};

	public enum ProtocolStates {
		READY, WAITING
	}

	/**
	 * Constructs a new ProtocolManager with the specified protocol and output
	 * wrapper.
	 *
	 * @param protocol      the AbstractHCIProtocol that this ProtocolManager will
	 *                      manage
	 * @param outputWrapper the AbstractWrapper that this ProtocolManager will use
	 *                      for output operations
	 *                      The initial state of the ProtocolManager is set to
	 *                      READY.
	 */
	public ProtocolManager(AbstractHCIProtocol protocol, AbstractWrapper outputWrapper) {
		this.protocol = protocol;
		this.outputWrapper = outputWrapper;
		currentState = ProtocolStates.READY;
	}

	public synchronized ProtocolStates getCurrentState() {
		return currentState;
	}

	/**
	 * This method tries to execute a query named queryName with parameters params
	 * on the wrapper wrapper.
	 * If successful, it returns the raw command that has been sent.
	 */
	public synchronized byte[] sendQuery(String queryName, Vector<Object> params) {
		byte[] answer = null;
		if (currentState == ProtocolStates.READY) {
			AbstractHCIQuery query = protocol.getQuery(queryName);

			if (query == null) {
				logger.warn("Query " + queryName
						+ " found but no bytes produced to send to device. Implementation may be missing.");
			} else {
				if(logger.isDebugEnabled()){
					logger.debug("Retrieved query " + queryName + ", trying to build raw query.");
				}
				byte[] queryBytes = query.buildRawQuery(params);
				if (queryBytes != null) {
					try {
						if(logger.isDebugEnabled()){
							logger.debug("Built query, it looks like: " + new String(queryBytes));
						}
						outputWrapper.sendToWrapper(null, null, new Object[] { queryBytes });
						lastExecutedQuery = query;
						lastParams = params;
						answer = queryBytes;
						if(logger.isDebugEnabled()){
							logger.debug("Query succesfully sent!");
						}
						if (query.needsAnswer(params)) {
							if(logger.isDebugEnabled()){
								logger.debug("Now entering wait mode for answer.");
							}
							timer = new Timer();
							currentState = ProtocolStates.WAITING;
							timer.schedule(answerTimeout, new Date());
						}
					} catch (OperationNotSupportedException e) {
						if(logger.isDebugEnabled()){
							logger.debug("Query could not be sent ! See error message.");
						}
						logger.error(e.getMessage(), e);
						currentState = ProtocolStates.READY;
					}
				}
			}

		}
		return answer;
	}

	/**
	 * This tries to match incoming data to the pattern
	 * expected by the query. If the pattern describes
	 * several groups then all the different String
	 * matching these groups are returned.
	 * 
	 * @param rawData the raw data to process
	 * @return an array of objects representing the answer, or null if the current
	 *         state is not WAITING
	 */
	public synchronized Object[] getAnswer(byte[] rawData) {
		Object[] answer = null;
		if (currentState == ProtocolStates.WAITING) {
			answer = lastExecutedQuery.getAnswers(rawData);
		}
		return answer;
	}

	/**
	 * @return
	 */
	public String getProtocolName() {
		if (protocol != null) {
			return protocol.getName();
		}
		return null;
	}

	/**
	 * Returns the AbstractHCIQuery associated with the provided string identifier.
	 * If the protocol is null, it returns null.
	 *
	 * @param string the identifier of the query to be retrieved
	 * @return the AbstractHCIQuery associated with the string identifier if the
	 *         protocol is not null, null otherwise
	 */
	public AbstractHCIQuery getQuery(String string) {
		if (protocol != null) {
			return protocol.getQuery(string);
		}
		return null;
	}

	/**
	 * Returns a collection of AbstractHCIQuery objects associated with the current
	 * protocol.
	 * If the protocol is null, it returns null.
	 *
	 * @return a collection of AbstractHCIQuery objects if the protocol is not null,
	 *         null otherwise
	 */
	public Collection<AbstractHCIQuery> getQueries() {
		if (protocol != null) {
			return protocol.getQueries();
		}
		return null;
	}
}
