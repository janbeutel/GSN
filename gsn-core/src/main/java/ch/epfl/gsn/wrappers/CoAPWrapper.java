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
* File: src/ch/epfl/gsn/wrappers/CoAPWrapper.java
*
* @author iseitani
* @author Julien Eberle
* @author Davide De Sclavis
* @author Manuel Buchauer
* @author Jan Beutel
*
*/
package ch.epfl.gsn.wrappers;

import ch.epfl.gsn.beans.AddressBean;
import ch.epfl.gsn.beans.DataField;
import ch.epfl.gsn.networking.mqtt.MQTTWrapper;
import java.io.Serializable;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.OptionSet;
import org.eclipse.californium.core.CoapHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoAPWrapper extends AbstractWrapper implements CoapHandler {

	private final transient Logger logger = LoggerFactory.getLogger(MQTTWrapper.class);

	private String serverURI; // ="coap://[you_ipv6]:5683/.temp";
	private AddressBean addressBean;
	private CoapClient client;
	private long nextExpectedMessage;

	@Override
	public DataField[] getOutputFormat() {
		return new DataField[] {
				new DataField("raw_packet", "BINARY", "The packet contains raw data received in the CoAP payload.") };
	}

	/**
	 * Initializes the CoAPWrapper by retrieving the server URI from the active
	 * address bean.
	 * If the server URI is missing or empty, the initialization fails.
	 * 
	 * @return true if the initialization is successful, false otherwise.
	 */
	@Override
	public boolean initialize() {
		try {
			addressBean = getActiveAddressBean();
			serverURI = addressBean.getPredicateValue("uri");
			if (serverURI == null || serverURI.trim().length() == 0) {
				logger.error("The uri parameter is missing from the CoAP wrapper, initialization failed.");
				return false;
			}
		} catch (Exception e) {
			logger.error("Error in instanciating CoAP Client @ " + serverURI, e);
			return false;
		}
		return true;
	}

	@Override
	public void dispose() {

	}

	@Override
	public String getWrapperName() {
		return "CoAP Wrapper";
	}

	/**
	 * Runs the CoAP client and continuously observes the server for updates.
	 * The client sends an observe request to the server at regular intervals and
	 * waits for updates.
	 * If an update is received, the client's callback method will be invoked.
	 * This method runs in a loop until the client is deactivated.
	 */
	public void run() {

		client = new CoapClient(serverURI);

		while (isActive()) {
			if (System.currentTimeMillis() / 1000 > nextExpectedMessage) {
				client.observe(this);
			}
			try {
				Thread.sleep(60000);
			} catch (Exception ex) {
				if(logger.isDebugEnabled()){
					logger.debug(ex.getMessage(), ex);
				}
			}
		}
	}

	@Override
	public void onError() {
		logger.error("CoAP observation error...");
	}

	/**
	 * This method is called when a CoAP response is loaded.
	 * It updates the next expected message time and posts the response payload to
	 * the stream element.
	 * 
	 * @param response The CoAP response object.
	 */
	@Override
	public void onLoad(CoapResponse response) {
		OptionSet os = response.getOptions();
		nextExpectedMessage = os.getMaxAge() * 2 + (System.currentTimeMillis() / 1000);
		postStreamElement(new Serializable[] { response.getPayload() });
	}

}
