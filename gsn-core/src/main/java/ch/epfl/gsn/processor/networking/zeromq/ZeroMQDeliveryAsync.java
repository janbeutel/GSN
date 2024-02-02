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
import ch.epfl.gsn.beans.VSensorConfig;
import ch.epfl.gsn.delivery.DeliverySystem;

public class ZeroMQDeliveryAsync implements DeliverySystem {

	private ZContext context;
	private Socket publisher;
	private boolean closed = true;
	private Kryo kryo = new Kryo();
	private String name;

	public static transient Logger logger = LoggerFactory.getLogger(ZeroMQDeliveryAsync.class);

	/**
	 * Constructs a ZeroMQDeliveryAsync object with the specified name.
	 *
	 * This constructor initializes a ZeroMQDeliveryAsync object, setting up the
	 * necessary ZeroMQ context, creating a
	 * PUB socket for communication, and binding it to the "inproc://stream/"
	 * address with the specified name. Additionally,
	 * it connects to the specified name using the ZeroMQ proxy.
	 *
	 * @param name The name associated with the ZeroMQDeliveryAsync object.
	 */
	public ZeroMQDeliveryAsync(String name) {
		if (name.endsWith(":")) {
			name = name.substring(0, name.length() - 1);
		}
		this.name = name;
		context = Main.getZmqContext();
		// Socket to talk to clients
		publisher = context.createSocket(ZMQ.PUB);
		publisher.setLinger(5000);
		publisher.setSndHWM(0); // no limit
		publisher.bind("inproc://stream/" + name);
		Main.getZmqProxy().connectTo(name);
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
	 * Writes a StreamElement to the ZeroMQ publisher socket.
	 * 
	 * @param se the StreamElement to be written
	 * @return true if the StreamElement was successfully written, false otherwise
	 */
	@Override
	public boolean writeStreamElement(StreamElement se) {
		try {
			ByteArrayOutputStream bais = new ByteArrayOutputStream();
			bais.write((name + ": ").getBytes());
			Output o = new Output(bais);
			kryo.writeObjectOrNull(o, se, StreamElement.class);
			o.close();
			byte[] b = bais.toByteArray();
			return publisher.send(b);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		return false;
	}

	@Override
	public boolean writeKeepAliveStreamElement() {
		return true;
	}

	/**
	 * Closes the ZeroMQDeliveryAsync instance.
	 * This method closes the publisher and sets the 'closed' flag to true.
	 */
	@Override
	public void close() {
		publisher.close();
		closed = true;
	}

	@Override
	public boolean isClosed() {
		return closed;
	}

}
