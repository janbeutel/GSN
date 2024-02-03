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
* File: src/ch/epfl/gsn/networking/zeromq/ZeroMQWrapperPush.java
*
* @author Julien Eberle
*
*/

package ch.epfl.gsn.networking.zeromq;

import java.io.ByteArrayInputStream;

import org.zeromq.ZContext;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.zeromq.ZMQ;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;

import ch.epfl.gsn.Main;
import ch.epfl.gsn.beans.AddressBean;
import ch.epfl.gsn.beans.DataField;
import ch.epfl.gsn.beans.StreamElement;
import ch.epfl.gsn.delivery.StreamElement4Rest;
import ch.epfl.gsn.wrappers.AbstractWrapper;

public class ZeroMQWrapperPush extends AbstractWrapper {

	private transient Logger logger = LoggerFactory.getLogger(this.getClass());
	private DataField[] structure;
	private Kryo kryo = new Kryo();
	private ZMQ.Socket receiver = null;
	private int lport = 0;
	private String laddress;

	/**
	 * Initializes the ZeroMQ wrapper by registering required classes with Kryo,
	 * configuring the output structure,
	 * and setting up a ZeroMQ REP socket to listen for incoming requests.
	 *
	 * This method registers the {@code StreamElement4Rest} and {@code DataField[]}
	 * classes with Kryo, retrieves the
	 * configured output structure from the active address bean, and validates the
	 * local address and port parameters. It then
	 * creates a ZeroMQ REP socket, binds it to the specified local address and
	 * port, and sets a receive timeout of 10 seconds.
	 * The method returns {@code true} upon successful initialization.
	 *
	 * @return {@code true} if the initialization is successful; {@code false}
	 *         otherwise.
	 */
	@Override
	public boolean initialize() {

		kryo.register(StreamElement4Rest.class);
		kryo.register(DataField[].class);

		AddressBean addressBean = getActiveAddressBean();

		structure = getActiveAddressBean().getOutputStructure();

		String _lport = addressBean.getPredicateValue("local_port");
		laddress = addressBean.getPredicateValue("local_address");

		if (laddress == null || laddress.trim().length() == 0) {
			throw new RuntimeException("The >local_address< parameter is missing from the ZeroMQ wrapper.");
		}

		if (_lport == null) {
			throw new RuntimeException("The >local_port< parameter is missing from the ZeroMQ wrapper.");
		} else {
			lport = Integer.parseInt(_lport);
			if (lport <= 0 || lport > 65535) {
				throw new RuntimeException("The >local_port< parameter must be a valid port number.");
			}
		}

		ZContext ctx = Main.getZmqContext();
		receiver = ctx.createSocket(ZMQ.REP);
		receiver.bind("tcp://*:" + lport);
		receiver.setReceiveTimeOut(10000);

		return true;
	}

	@Override
	public void dispose() {

	}

	@Override
	public String getWrapperName() {
		return "ZeroMQ wrapper Push";
	}

	/**
	 * Runs the main logic of the ZeroMQ wrapper in a continuous loop while the
	 * wrapper is active.
	 *
	 * This method continuously listens for incoming requests on the configured
	 * ZeroMQ REP socket. Upon receiving a request,
	 * it deserializes the data into a {@code StreamElement}, attempts to post the
	 * element, and sends a response indicating
	 * the success or failure of the operation. In case of a protocol error or
	 * exception, the socket is re-initialized, and
	 * the loop continues.
	 */
	@Override
	public void run() {

		while (isActive()) {
			try {
				byte[] rec = receiver.recv();
				if (rec != null) {
					ByteArrayInputStream bais = new ByteArrayInputStream(rec);
					StreamElement se = kryo.readObjectOrNull(new Input(bais), StreamElement.class);
					boolean success = postStreamElement(se);
					receiver.send(success ? new byte[] { (byte) 0 } : new byte[] { (byte) 1 });
				}
			} catch (IllegalStateException z) {
				logger.error("ZMQ wrapper error in zmq protocol (re-init socket): ", z);
				try {
					receiver.send(new byte[] { (byte) 1 });
				} catch (Exception e) {
					logger.error("ZMQ wrapper error in send: ", e);
				} finally {
					receiver.close();
					ZContext ctx = Main.getZmqContext();
					receiver = ctx.createSocket(ZMQ.REP);
					receiver.bind("tcp://*:" + lport);
					receiver.setReceiveTimeOut(10000);
				}
			} catch (Exception e) {
				logger.error("ZMQ wrapper error: ", e);
			}
		}
		receiver.close();
	}

	/**
	 * if the structure is not defined in the xml file, throws an exception.
	 */
	@Override
	public DataField[] getOutputFormat() throws RuntimeException {
		if (structure == null) {
			throw new RuntimeException("ZMQ Push wrapper has an undefined output structure.");
		}
		return structure;
	}

	@Override
	public boolean isTimeStampUnique() {
		return false;
	}

}
