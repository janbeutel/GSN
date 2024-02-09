package ch.epfl.gsn.wrappers.backlog;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class AsyncCoreStationClient extends Thread {
	/**
	 * Timeout in seconds to pass until trying to reconnect
	 * to the CoreStation in case of a connection loss.
	 */
	public static final int RECONNECT_TIMEOUT_SEC = 30;

	protected static final int PACKET_SIZE = ch.epfl.gsn.wrappers.backlog.BackLogMessage.MAX_PAYLOAD_SIZE + 4;

	protected final transient Logger logger = LoggerFactory.getLogger(AsyncCoreStationClient.class);

	private static AsyncCoreStationClient singletonObject = null;

	protected Selector selector;
	protected List<ChangeRequest> changeRequests = new LinkedList<ChangeRequest>();

	// Maps a SocketChannel to a list of ByteBuffer instances
	private Map<SocketChannel, PriorityData> pendingData = new HashMap<SocketChannel, PriorityData>();

	protected Map<SocketChannel, CoreStationListener> socketToListenerList = new Hashtable<SocketChannel, CoreStationListener>();
	protected Map<CoreStationListener, SocketChannel> listenerToSocketList = new Hashtable<CoreStationListener, SocketChannel>();
	private static Map<String, Map<Integer, CoreStationListener>> deploymentToIdListenerMapList = Collections
			.synchronizedMap(new HashMap<String, Map<Integer, CoreStationListener>>());

	private boolean dispose = false;

	private AsyncCoreStationClient() throws IOException {
		this.selector = Selector.open();

		setName("AsyncCoreStationClient-Thread");
	}

	@SuppressWarnings("unused")
	public synchronized static AsyncCoreStationClient getSingletonObject() throws Exception {
		if (RECONNECT_TIMEOUT_SEC <= 0) {
			throw new Exception("RECONNECT_TIMEOUT_SEC must be a positive integer");
		}
		if (singletonObject == null) {
			singletonObject = new AsyncCoreStationClient();
		}
		return singletonObject;
	}

	/**
	 * Executes the main logic of the thread.
	 * This method continuously processes change requests and handles selected keys
	 * from the selector.
	 * It reads, writes, and finishes connections based on the operations specified
	 * in the change requests.
	 * The thread stops when the dispose flag is set to true.
	 */
	public void run() {
		if (logger.isDebugEnabled()) {
			logger.debug("thread started");
		}
		SelectionKey key;

		while (!dispose) {
			try {
				synchronized (changeRequests) {
					Iterator<ChangeRequest> changes = changeRequests.iterator();
					while (changes.hasNext()) {
						ChangeRequest change = changes.next();
						switch (change.type) {
							case ChangeRequest.TYPE_CHANGEOPS:
								key = change.socket.keyFor(selector);
								if (key == null || !key.isValid()) {
									continue;
								}
								if (!change.socket.isConnectionPending()) {
									key.interestOps(change.ops);
									key.attach(change);
								}
								break;
							case ChangeRequest.TYPE_REGISTER:
								if (logger.isDebugEnabled()) {
									logger.debug("Selector:register");
								}
								change.socket.register(selector, change.ops, change);
								break;
							case ChangeRequest.TYPE_RECONNECT:
								try {
									if (logger.isDebugEnabled()) {
										logger.debug("Selector:reconnect");
									}

									if (change.socket.keyFor(selector) != null
											&& change.socket.keyFor(selector).isValid()) {
										closeConnection(change.socket.keyFor(selector), change.socket);
									}

									CoreStationListener listener;

									synchronized (listenerToSocketList) {
										listener = socketToListenerList.get(change.socket);
										socketToListenerList.remove(change.socket);
										if (listener != null) {
											listenerToSocketList.remove(listener);
											timeReconnect(listener);
										}
									}
								} catch (Exception e) {
									logger.error(e.getMessage(), e);
								}
								break;
						}
					}
					changeRequests.clear();
				}

				if (selector.select() == 0) {
					continue;
				}

				Set<SelectionKey> readyKeys = selector.selectedKeys();
				Iterator<SelectionKey> iterator = readyKeys.iterator();
				while (iterator.hasNext()) {
					key = iterator.next();
					iterator.remove();
					if (!key.isValid()) {
						logger.warn("Selector:invalid");
						continue;
					}
					if (key.channel() instanceof SocketChannel) {
						try {
							if (key.isReadable()) {
								this.read(key);
							} else if (key.isWritable()) {
								this.write(key);
							} else if (key.isConnectable()) {
								if (logger.isDebugEnabled()) {
									logger.debug("Selector:connect");
								}
								this.finishConnection(key);
							}
						} catch (IOException e) {
							logger.error(e.getMessage(), e);
						}
					} else {
						if (logger.isDebugEnabled()) {
							logger.debug("no handler for " + key.channel().getClass().getName());
						}
					}
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}

		logger.info("thread stoped");
	}

	/**
	 * Reads data from the given socket channel and processes it.
	 * If the remote entity closes the connection, it will attempt to reconnect if
	 * necessary.
	 *
	 * @param key The selection key associated with the socket channel.
	 * @throws IOException If an I/O error occurs while reading from the socket
	 *                     channel.
	 */
	private void read(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();

		try {
			int numRead;
			byte[] data;
			synchronized (this.pendingData) {
				PriorityData pData = this.pendingData.get(socketChannel);
				// Clear out our read buffer so it's ready for new data
				pData.readBuffer.clear();

				numRead = socketChannel.read(pData.readBuffer);
				if (numRead == -1) {
					if (logger.isDebugEnabled()) {
						logger.debug("connection closed");
					}
					// Remote entity shut the socket down cleanly. Do the
					// same from our end and cancel the channel.
					if (!dispose && socketToListenerList.containsKey(socketChannel)) {
						reconnect(socketToListenerList.get(socketChannel));
					}
					return;
				}
				data = pData.readBuffer.array();
			}

			// Hand the data over to our listener thread
			socketToListenerList.get(socketChannel).processData(data, numRead);
		} catch (IOException e) {
			if (logger.isDebugEnabled()) {
				logger.debug("connection closed: " + e.getMessage());
			}
			// The remote forcibly closed the connection
			if (!dispose && socketToListenerList.containsKey(socketChannel)) {
				reconnect(socketToListenerList.get(socketChannel));
			}
		}
	}

	/**
	 * Closes the connection associated with the given SelectionKey and
	 * SocketChannel.
	 * If the SelectionKey is not null, it is cancelled.
	 * If the SocketChannel is not null, it is closed and the corresponding pending
	 * data is removed.
	 * Finally, the connectionLost() method is called on the associated
	 * CoreStationListener.
	 *
	 * @param key The SelectionKey associated with the connection.
	 * @param sc  The SocketChannel associated with the connection.
	 */
	private void closeConnection(SelectionKey key, SocketChannel sc) {
		if (key != null) {
			key.cancel();
		}
		if (sc != null) {
			synchronized (this.pendingData) {
				this.pendingData.remove(sc);
			}
			try {
				sc.close();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
			CoreStationListener listener;
			listener = socketToListenerList.get(sc);
			listener.connectionLost();
		}
	}

	/**
	 * Writes data from the pendingData queue to the specified socket channel.
	 * If there is no more data to write, switches the selection key to OP_READ.
	 *
	 * @param key The selection key associated with the socket channel.
	 */
	private void write(SelectionKey key) {
		SocketChannel socketChannel = (SocketChannel) key.channel();
		try {
			synchronized (this.pendingData) {
				PriorityData pData = this.pendingData.get(socketChannel);

				while (pData.writeBuffer.hasRemaining()) {
					if (socketChannel.write(pData.writeBuffer) == 0) {
						return;
					}
				}

				// Write until there's not more data ...
				while (!pData.queue.isEmpty()) {
					pData.writeBuffer.clear();
					try {
						pData.writeBuffer.put(pData.queue.poll().getData());
					} catch (BufferOverflowException e) {
						logger.error(e.getMessage(), e);
					} finally {
						pData.queue.remove(0);
					}

					pData.writeBuffer.flip();
					while (pData.writeBuffer.hasRemaining()) {
						if (socketChannel.write(pData.writeBuffer) == 0) {
							return;
						}
					}
				}

				if (pData.queue.isEmpty()) {
					// We wrote away all data, so we're no longer interested
					// in writing on this socket. Switch back to waiting for
					// data.
					key.interestOps(SelectionKey.OP_READ);
				}
			}
		} catch (IOException e) {
			if (logger.isDebugEnabled()) {
				logger.debug("connection closed: " + e.getMessage());
			}
			// The remote forcibly closed the connection
			if (!dispose && socketToListenerList.containsKey(socketChannel)) {
				reconnect(socketToListenerList.get(socketChannel));
			}
		}
	}

	/**
	 * Finishes the connection for the given SelectionKey.
	 * If the connection operation failed, it will raise an IOException.
	 * If the connection is successful, it notifies the corresponding
	 * CoreStationListener
	 * and registers an interest in reading on the channel.
	 *
	 * @param key The SelectionKey representing the connection.
	 */
	private void finishConnection(SelectionKey key) {
		SocketChannel socketChannel = (SocketChannel) key.channel();

		// Finish the connection. If the connection operation failed
		// this will raise an IOException.
		try {
			socketChannel.finishConnect();
		} catch (IOException e) {
			if (!dispose && socketToListenerList.containsKey(socketChannel)) {
				if(logger.isDebugEnabled()){
					logger.debug("could not connect to " + socketToListenerList.get(socketChannel).getCoreStationName()
						+ ": " + e.getMessage());
				}
				reconnect(socketToListenerList.get(socketChannel));
			}
			return;
		}

		try {
			CoreStationListener listener;
			listener = socketToListenerList.get(socketChannel);
			listener.connectionEstablished();
			if(logger.isDebugEnabled()){
				logger.debug("connection established to core station: " + listener.getCoreStationName());
			}

			// Register an interest in reading on this channel
			key.interestOps(SelectionKey.OP_READ);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * Registers a CoreStationListener to receive events from the core station.
	 * 
	 * @param listener the CoreStationListener to register
	 * @throws IOException if an I/O error occurs
	 */
	public void registerListener(CoreStationListener listener) throws IOException {
		if (logger.isDebugEnabled()) {
			logger.debug("register core station: " + listener.getCoreStationName());
		}
		try {
			if (!this.isAlive()) {
				this.start();
			}
		} catch (IllegalThreadStateException e) {
			if(logger.isDebugEnabled()){
				logger.debug("thread already running");
			}
		}

		SocketChannel socketChannel = SocketChannel.open();
		socketChannel.configureBlocking(false);
		if(logger.isDebugEnabled()){
			logger.debug("trying to connect to core station: " + listener.getCoreStationName());
		}
		try {
			socketChannel.connect(new InetSocketAddress(listener.getInetAddress(), listener.getPort()));
		} catch (UnknownHostException e) {
			logger.warn("unknown host (" + e.getMessage() + ") trying to resolve it again in " + RECONNECT_TIMEOUT_SEC
					+ " seconds");
			timeReconnect(listener);
			return;
		}

		synchronized (listenerToSocketList) {
			socketToListenerList.put(socketChannel, listener);
			listenerToSocketList.put(listener, socketChannel);
		}
		synchronized (changeRequests) {
			changeRequests.add(new ChangeRequest(socketChannel, ChangeRequest.TYPE_REGISTER, SelectionKey.OP_CONNECT));
		}
		selector.wakeup();
	}

	/**
	 * Deregisters a CoreStationListener from the AsyncCoreStationClient.
	 * This method closes the associated SocketChannel and removes the listener from
	 * the internal maps.
	 * If there are no more listeners registered, the client is disposed.
	 *
	 * @param listener The CoreStationListener to be deregistered.
	 */
	public void deregisterListener(CoreStationListener listener) {
		synchronized (listenerToSocketList) {
			SocketChannel sc = listenerToSocketList.get(listener);
			if (sc == null) {
				logger.error("this listener is not available in the listener map");
				return;
			}
			try {
				sc.close();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}

			try {
				sc.keyFor(selector).cancel();

				socketToListenerList.remove(sc);
				listenerToSocketList.remove(listener);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}

		if (socketToListenerList.isEmpty()) {
			dispose();
		}

	}

	/**
	 * Adds a device ID for a specific deployment to the listener map.
	 * 
	 * @param deployment the deployment name
	 * @param id         the device ID to add
	 * @param listener   the CoreStationListener associated with the device ID
	 */
	public void addDeviceId(String deployment, Integer id, CoreStationListener listener) {
		if (logger.isDebugEnabled()) {
			logger.debug("adding DeviceId " + id + " for " + deployment + " deployment");

		}
		try {
			if (!deploymentToIdListenerMapList.containsKey(deployment)) {
				deploymentToIdListenerMapList.put(deployment, new HashMap<Integer, CoreStationListener>());
			}

			deploymentToIdListenerMapList.get(deployment).put(id, listener);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * Removes the specified device ID for the given deployment.
	 * If the deployment or device ID is null, an error message is logged.
	 * If there is no core station listener for the specified deployment, an error
	 * message is logged.
	 * If the device ID is successfully removed and there are no more listeners for
	 * the deployment, the deployment is removed from the map.
	 *
	 * @param deployment the deployment name
	 * @param id         the device ID to remove
	 */
	public void removeDeviceId(String deployment, Integer id) {
		if (logger.isDebugEnabled()) {
			logger.debug("removing DeviceId: " + id + " for " + deployment + " deployment");
		}
		try {
			if (deployment == null) {
				logger.error("deployment is null");
			} else {
				Map<Integer, CoreStationListener> list = deploymentToIdListenerMapList.get(deployment);
				if (list == null) {
					logger.error("there is no core station listener for deployment " + deployment);
					return;
				}
				if (id == null) {
					logger.error("id is null for " + deployment + " deployment");
				} else {
					list.remove(id);
				}

				if (list.isEmpty()) {
					deploymentToIdListenerMapList.remove(deployment);
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * Disposes the object and performs necessary cleanup operations.
	 * Sets the singletonObject to null, marks the dispose flag as true,
	 * wakes up the selector, and waits for the thread to join.
	 * 
	 * @throws InterruptedException if the thread is interrupted while waiting to
	 *                              join
	 */
	private void dispose() {
		singletonObject = null;
		dispose = true;
		selector.wakeup();
		try {
			this.join();
		} catch (InterruptedException e) {
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * Stuffs the given message with a stuffing byte.
	 * 
	 * @param message the message to be stuffed
	 * @return the stuffed message
	 */
	private byte[] pktStuffing(byte[] message) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			for (int i = 0; i < message.length; i++) {
				baos.write(message[i]);
				if (message[i] == BackLogMessageMultiplexer.STUFFING_BYTE) {
					baos.write(message[i]);
				}
			}
			return baos.toByteArray();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	/**
	 * Sends data to a core station.
	 * 
	 * @param deployment the deployment name
	 * @param id         the device ID (null if not applicable)
	 * @param listener   the core station listener (null if not applicable)
	 * @param priority   the priority of the data
	 * @param data       the data to be sent
	 * @return an array of Serializable objects containing the success status and
	 *         the response (if any)
	 * @throws IOException if there is an error sending the data
	 */
	public Serializable[] send(String deployment, Integer id, CoreStationListener listener, int priority, byte[] data)
			throws IOException {

		CoreStationListener coreStationListener = listener;
		Serializable[] ret = new Serializable[] { false, null };
		if (deployment == null) {
			logger.error("deployment should not be null...");
			return ret;
		}
		Map<Integer, CoreStationListener> corestationMap = null;
		corestationMap = deploymentToIdListenerMapList.get(deployment);
		if (corestationMap == null) {
			throw new IOException("The " + deployment + " deployment is not connected or does not exist");
		}

		if (id == null) {
			ret = send(coreStationListener, priority, data, true);
		} else {
			if (id == 65535) {
				Iterator<Integer> iter = corestationMap.keySet().iterator();
				while (iter.hasNext()) {
					Serializable[] tmp = send(corestationMap.get(iter.next()), priority, data, true);
					if ((Boolean) tmp[0]) {
						ret = tmp;
					}
				}
			} else {
				coreStationListener = corestationMap.get(id);
				if (coreStationListener == null) {
					throw new IOException("The DeviceId " + id + " is not connected or does not exist for the "
							+ deployment + " deployment");
				}

				ret = send(coreStationListener, priority, data, true);
			}
		}
		return ret;
	}

	/**
	 * Sends data to the CoreStationListener over a socket channel.
	 * 
	 * @param listener the CoreStationListener to send the data to
	 * @param priority the priority of the data
	 * @param data     the byte array of data to send
	 * @param stuff    a flag indicating whether the data should be stuffed
	 * @return an array containing a boolean indicating the success of the send
	 *         operation and the size of the sent data
	 * @throws IOException if an I/O error occurs during the send operation
	 */
	private Serializable[] send(CoreStationListener listener, int priority, byte[] data, boolean stuff)
			throws IOException {
		if (data.length > PACKET_SIZE - 4) {
			throw new IOException("packet size limited to " + (PACKET_SIZE - 4) + " bytes");
		}

		SocketChannel socketChannel;
		socketChannel = listenerToSocketList.get(listener);

		if (socketChannel != null && socketChannel.isConnected()) {
			Long size = null;
			synchronized (this.changeRequests) {
				// Indicate we want the interest ops set changed
				this.changeRequests.add(new ChangeRequest(socketChannel, ChangeRequest.TYPE_CHANGEOPS,
						SelectionKey.OP_WRITE | SelectionKey.OP_READ));

				// And queue the data we want written
				synchronized (this.pendingData) {
					PriorityData pData = this.pendingData.get(socketChannel);
					if (pData == null) {
						pData = new PriorityData();
						this.pendingData.put(socketChannel, pData);
					}
					if (stuff) {
						ByteBuffer out = ByteBuffer.allocate(data.length + 4);
						out.order(ByteOrder.LITTLE_ENDIAN);
						out.putInt(data.length);
						out.put(data);
						out.position(0);
						byte[] arr = new byte[data.length + 4];
						out.get(arr);
						byte[] tmp = pktStuffing(arr);
						size = new Long(tmp.length);
						pData.queue.offer(new PriorityDataElement(priority, tmp));
					} else {
						size = new Long(data.length);
						pData.queue.offer(new PriorityDataElement(priority, data));
					}
				}
			}

			// Finally, wake up our selecting thread so it can make the required changes
			this.selector.wakeup();
			return new Serializable[] { true, size };
		} else {
			logger.warn(listener.getCoreStationName() + " is not connected");
			return new Serializable[] { false, null };
		}
	}

	public Serializable[] sendHelloMsg(CoreStationListener listener) throws IOException {
		if (logger.isDebugEnabled()) {
			logger.debug("send hello message");
		}
		byte[] data = { BackLogMessageMultiplexer.STUFFING_BYTE, BackLogMessageMultiplexer.HELLO_BYTE };

		return send(listener, 1, data, false);
	}

	/**
	 * Reconnects the listener to the core station.
	 * If a socket channel exists for the listener, a reconnect request is added to
	 * the change requests list.
	 * The selector is then woken up to process the change requests.
	 * If no socket channel is found for the listener, a warning is logged.
	 *
	 * @param listener The CoreStationListener to reconnect.
	 */
	public void reconnect(CoreStationListener listener) {
		SocketChannel sc = listenerToSocketList.get(listener);
		if (sc == null) {
			logger.warn("no socket for listener (" + listener.getCoreStationName() + ") in list");
		} else {
			try {
				synchronized (changeRequests) {
					if (logger.isDebugEnabled()) {
						logger.debug("add reconnect request");
					}
					// Indicate we want the interest ops set changed
					changeRequests.add(new ChangeRequest(sc, ChangeRequest.TYPE_RECONNECT, -1));
				}
				selector.wakeup();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}

	}

	/**
	 * Attempts to reconnect to the specified CoreStation after a certain timeout.
	 * 
	 * @param listener the CoreStationListener to reconnect to
	 */
	private void timeReconnect(CoreStationListener listener) {
		if (logger.isDebugEnabled()) {
			logger.debug("trying to reconnect to " + listener.getCoreStationName() + " CoreStation in "
					+ RECONNECT_TIMEOUT_SEC + " seconds");
		}
		Timer timer = new Timer("ReconnectTimer-" + listener.getCoreStationName());
		timer.schedule(new ReconnectTimerTask(this, listener), RECONNECT_TIMEOUT_SEC * 1000);
	}
}

class ChangeRequest {
	public static final int TYPE_REGISTER = 1;
	public static final int TYPE_CHANGEOPS = 2;
	public static final int TYPE_RECONNECT = 3;

	public SocketChannel socket;
	public int type;
	public int ops;

	public ChangeRequest(SocketChannel socket, int type, int ops) {
		this.socket = socket;
		this.type = type;
		this.ops = ops;
	}
}

class PriorityData {
	protected PriorityQueue<PriorityDataElement> queue;
	protected ByteBuffer writeBuffer;
	protected ByteBuffer readBuffer;

	public PriorityData() {
		queue = new PriorityQueue<PriorityDataElement>();
		writeBuffer = ByteBuffer.allocate(AsyncCoreStationClient.PACKET_SIZE * 2);
		readBuffer = ByteBuffer.allocate(AsyncCoreStationClient.PACKET_SIZE * 2);
		writeBuffer.flip();
	}
}

class PriorityDataElement implements Comparable<PriorityDataElement> {
	private int priority;
	private byte[] data;

	public PriorityDataElement(int priority, byte[] data) {
		this.priority = priority;
		this.data = data;
	}

	public byte[] getData() {
		return data;
	}

	@Override
	public int compareTo(PriorityDataElement obj) {
		if (obj == null) {
			throw new NullPointerException();
		}

		if (priority < obj.priority) {
			return -1;
		} else if (priority > obj.priority) {
			return 1;
		} else {
			return 0;
		}
	}
}

class ReconnectTimerTask extends TimerTask {
	private AsyncCoreStationClient parent;
	private CoreStationListener listener;

	public ReconnectTimerTask(AsyncCoreStationClient parent, CoreStationListener listener) {
		super();
		this.parent = parent;
		this.listener = listener;
	}

	public void run() {
		try {
			parent.registerListener(listener);
		} catch (IOException e) {
			parent.logger.error(e.getMessage(), e);
		}
	}
}