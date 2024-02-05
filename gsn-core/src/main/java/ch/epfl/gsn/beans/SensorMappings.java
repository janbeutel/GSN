package ch.epfl.gsn.beans;

import java.util.ArrayList;
import java.util.Iterator;

public class SensorMappings {
	public Integer position;
	public ArrayList<SensorMap> mappings;

	/**
	 * default Constructor
	 */
	public SensorMappings() {
		//default constructor
	}

	public SensorMappings(Integer position, ArrayList<SensorMap> mappings) {
		this.position = position;
		this.mappings = mappings;
	}

	/**
	 * Adds a SensorMap to the list of mappings.
	 * If there is already a mapping with the same begin timestamp, it will be
	 * replaced.
	 *
	 * @param sensorMap the SensorMap to be added
	 */
	public void add(SensorMap sensorMap) {
		Iterator<SensorMap> iter = mappings.iterator();
		while (iter.hasNext()) {
			SensorMap map = iter.next();
			if (map.end == null && map.begin.compareTo(sensorMap.begin) == 0) {
				iter.remove();
			}
		}
		mappings.add(sensorMap);
	}
}
