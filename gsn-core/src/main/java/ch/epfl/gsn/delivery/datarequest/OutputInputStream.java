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
* File: src/ch/epfl/gsn/http/datarequest/OutputInputStream.java
*
* @author Timotee Maret
*
*/

package ch.epfl.gsn.delivery.datarequest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ArrayBlockingQueue;

public class OutputInputStream {

	private OISInputStream oisi = null;
	private OISOutputStream oiso = null;
	private boolean oisiClosed = false;
	private boolean oisoClosed = false;
	private ArrayBlockingQueue<Integer> circularBuffer = null;

	/**
	 * Constructs a new OutputInputStream object with the specified buffer size.
	 *
	 * @param bufferSize The size of the circular buffer used for storing elements.
	 */
	public OutputInputStream(int bufferSize) {
		circularBuffer = new ArrayBlockingQueue<Integer>(bufferSize);
	}

	/**
	 * Closes the input and output streams associated with the OutputInputStream
	 * object.
	 *
	 * @throws IOException If an error occurs while closing the streams.
	 */
	public void close() throws IOException {
		synchronized (this) {
			if (oisi != null && !oisiClosed) {
				oisi.close();
			}
			if (oiso != null && !oisoClosed) {
				oiso.close();
			}
			circularBuffer = null;
			// System.out.println("OutputInputStream >" + this + "< has been closed");
		}
	}

	/**
	 * Returns an InputStream object associated with the OutputInputStream object.
	 *
	 * @return The InputStream object associated with the OutputInputStream.
	 */
	public InputStream getInputStream() {
		if (oisi == null) {
			oisi = new OISInputStream();
		}
		return oisi;
	}

	/**
	 * Returns an OutputStream object associated with the OutputInputStream object.
	 *
	 * @return The OutputStream object associated with the OutputInputStream.
	 */
	public OutputStream getOutputStream() {
		if (oiso == null) {
			oiso = new OISOutputStream();
		}
		return oiso;
	}

	/**
	 * The OISOutputStream class extends the OutputStream class and is responsible
	 * for writing data to the OutputInputStream object.
	 */
	private class OISOutputStream extends OutputStream {
		/**
		 * Writes a byte of data to the output stream.
		 *
		 * @param b The byte of data to be written.
		 * @throws IOException If the output stream is closed.
		 */
		@Override
		public void write(int b) throws IOException {
			if (oisoClosed) {
				throw new IOException("Outputstream is closed");
			}

			try {
				circularBuffer.put(b);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		/**
		 * Closes the output stream.
		 *
		 * @throws IOException If an error occurs while closing the stream.
		 */
		@Override
		public void close() throws IOException {
			synchronized (OutputInputStream.this) {
				oisoClosed = true;
				// System.out.println("OISOutputStream >" + this + " has been closed<");
				if (oisiClosed) {
					OutputInputStream.this.close();
				}
			}
		}
	}

	/**
	 * The OISInputStream class extends the InputStream class and is responsible for
	 * reading data from the OutputInputStream object.
	 */
	private class OISInputStream extends InputStream {

		/**
		 * Reads the next byte of data from the input stream.
		 *
		 * @return The next byte of data, or -1 if the end of the stream is reached.
		 * @throws IOException If the input stream is closed or an error occurs while
		 *                     reading.
		 */
		@Override
		public int read() throws IOException {
			if (oisiClosed) {
				throw new IOException("InputStream has been closed");
			}
			int nextValue = -1;
			try {
				if (oisoClosed) {
					nextValue = available() > 0 ? circularBuffer.take() : -1;
				} else {
					nextValue = circularBuffer.take();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return nextValue;
		}

		/**
		 * Reads data from the input stream into a byte array.
		 *
		 * @param b The byte array to read the data into.
		 * @return The number of bytes read, or -1 if the end of the stream is reached.
		 * @throws IOException If the input stream is closed or an error occurs while
		 *                     reading.
		 */
		@Override
		public int read(byte[] b) throws IOException {
			if (oisiClosed) {
				throw new IOException("InputStream has been closed");
			}
			if (b == null || b.length == 0) {
				return 0;
			}
			int available = available();
			if (available == 0) {
				if (oisoClosed) {
					return -1;
				} else {
					try {
						b[0] = circularBuffer.take().byteValue(); // TODO
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					return 1;
				}
			} else {
				int dataLength = Math.min(available, b.length);
				for (int i = 0; i < dataLength; i++) {
					try {
						b[i] = circularBuffer.take().byteValue(); // TODO
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				return dataLength;
			}
		}

		/**
		 * Closes the input stream associated with the OutputInputStream object.
		 *
		 * @throws IOException If an I/O error occurs while closing the input stream.
		 */
		@Override
		public void close() throws IOException {
			synchronized (OutputInputStream.this) {
				oisiClosed = true;
				// System.out.println("OISInputStream >" + this + " has been closed<");
				OutputInputStream.this.close();
			}
		}

		/**
		 * Returns the number of bytes that can be read from the input stream without
		 * blocking.
		 *
		 * @return The number of bytes available for reading from the input stream.
		 */
		@Override
		public int available() {
			int available = 0;
			synchronized (circularBuffer) {
				available = circularBuffer.size();
			}
			return available;
		}
	}

	/**
	 * The main method creates an instance of OutputInputStream with a specified
	 * buffer size.
	 * It writes data to the output stream and reads it from the input stream.
	 */
	public static void main(String[] args) {
		final OutputInputStream ois = new OutputInputStream(4);
		new Thread(
				new Runnable() {
					public void run() {
						try {
							ois.getOutputStream().write("ABCDEFGHIJKLMNOPQRSTUVWXYZ".getBytes());
							ois.getOutputStream().close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}).start();
		InputStream is = ois.getInputStream();
		int nextValue = -1;
		try {
			while ((nextValue = is.read()) != -1) {
				System.out.println("read: " + nextValue);
			}
			ois.getInputStream().close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
