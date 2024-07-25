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
* File: src/ch/epfl/gsn/networking/zeromq/ZeroMQWrapperPush.java
*
* @author Julien Eberle
* @author Davide De Sclavis
* @author Manuel Buchauer
* @author Jan Beutel
*
*/
package ch.epfl.gsn.networking.zeromq;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.zeromq.ZContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;

import ch.epfl.gsn.Main;
import ch.epfl.gsn.beans.DataField;
import ch.epfl.gsn.beans.StreamElement;
import ch.epfl.gsn.delivery.DeliverySystem;

public class ZeroMQDeliverySync implements DeliverySystem {

	private ZContext context;
	private Socket sender;
	private boolean closed = true;
	private Kryo kryo = new Kryo();
	private String name;

	public static transient Logger logger = LoggerFactory.getLogger(ZeroMQDeliverySync.class);

	/**
	 * Constructs a ZeroMQDeliverySync object with the specified name and remote
	 * contact point.
	 *
	 * This constructor initializes a ZeroMQDeliverySync object, setting up the
	 * necessary ZeroMQ context, creating a REQ
	 * socket for communication, and connecting it to the specified remote contact
	 * point.
	 *
	 * @param name               The name associated with the ZeroMQDeliverySync
	 *                           object.
	 * @param remoteContactPoint The remote contact point to which the ZeroMQ socket
	 *                           connects.
	 */
	public ZeroMQDeliverySync(String name, String remoteContactPoint) {
		if (name.endsWith(":")) {
			name = name.substring(0, name.length() - 1);
		}
		this.name = name;
		context = Main.getZmqContext();
		sender = context.createSocket(ZMQ.REQ);
		sender.connect(remoteContactPoint);
		closed = false;
	}

	/**
	 * Writes the structure of the data to be sent over the network.
	 * This method registers the structure with the ZeroMQ proxy.
	 *
	 * @param fields an array of DataField objects representing the structure of the
	 *               data
	 * @throws IOException if an I/O error occurs while registering the structure
	 */
	@Override
	public void writeStructure(DataField[] fields) throws IOException {
		Main.getZmqProxy().registerStructure(name, fields);
	}

	/**
	 * Writes a StreamElement to the ZeroMQ socket for delivery.
	 * 
	 * @param se the StreamElement to be written
	 * @return true if the StreamElement was successfully written, false otherwise
	 */
	@Override
	public boolean writeStreamElement(StreamElement se) {
		try {
			ByteArrayOutputStream bais = new ByteArrayOutputStream();
			Output o = new Output(bais);
			kryo.writeObjectOrNull(o, se, StreamElement.class);
			o.close();
			byte[] b = bais.toByteArray();
			if (sender.send(b)) {
				byte[] rec = sender.recv();
				return rec != null && rec.length == 1 && rec[0] == 0;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return false;
	}

	@Override
	public boolean writeKeepAliveStreamElement() {
		return true;
	}

	/**
	 * Closes the ZeroMQDeliverySync instance.
	 * This method closes the sender and sets the 'closed' flag to true.
	 */
	@Override
	public void close() {
		sender.close();
		closed = true;
	}

	@Override
	public boolean isClosed() {
		return closed;
	}

}
