package ch.epfl.gsn.beans;

import java.util.ArrayList;
import java.util.Iterator;

public class PositionMappings {
	public Integer position;
	public ArrayList<PositionMap> mappings;

	public PositionMappings() {
	}

	public PositionMappings(Integer position, ArrayList<PositionMap> mappings) {
		this.position = position;
		this.mappings = mappings;
	}

	/**
	 * Adds a PositionMap to the list of mappings.
	 * If there is already a mapping with the same begin value and no end value, it
	 * is removed before adding the new mapping.
	 *
	 * @param positionMap the PositionMap to be added
	 */
	public void add(PositionMap positionMap) {
		Iterator<PositionMap> iter = mappings.iterator();
		while (iter.hasNext()) {
			PositionMap map = iter.next();
			if (map.end == null && map.begin.compareTo(positionMap.begin) == 0) {
				iter.remove();
			}
		}
		mappings.add(positionMap);
	}
}