package ch.epfl.gsn.vsensor;

import ch.epfl.gsn.beans.DataField;
import ch.epfl.gsn.beans.DataTypes;
import ch.epfl.gsn.beans.StreamElement;

/**
 * This class represents a counter virtual sensor that counts the number of data
 * elements received.
 */
public class CounterVS extends AbstractVirtualSensor {

	private long count = 0;

	@Override
	public boolean initialize() {
		return true;
	}

	@Override
	public void dispose() {
	}

	/**
	 * Increments the counter variable when new data is received.
	 * Produces a new StreamElement with the updated counter value.
	 */
	@Override
	public void dataAvailable(String inputStreamName, StreamElement streamElement) {
		count += 1;
		dataProduced(new StreamElement(new DataField[] { new DataField("Counter", DataTypes.BIGINT) },
				new Long[] { count }));

	}

}
