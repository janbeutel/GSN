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
* File: src/ch/epfl/gsn/utils/models/AbstractModel.java
*
* @author Julien Eberle
* @author Davide De Sclavis
* @author Manuel Buchauer
* @author Jan Beutel
*
*/

package ch.epfl.gsn.utils.models;

import ch.epfl.gsn.beans.DataField;
import ch.epfl.gsn.beans.StreamElement;
import ch.epfl.gsn.vsensor.ModellingVirtualSensor;

/**
 * This class is the base class for all models that need to be linked to a
 * virtual sensor for getting updated in real-time.
 * A reference to the VS allows for accessing the other models if needed.
 * 
 * @author jeberle
 *
 */
public abstract class AbstractModel {

	protected DataField[] outputfield;

	protected ModellingVirtualSensor vs;

	public DataField[] getOutputFields() {
		return outputfield;
	}

	/**
	 * Sets the output fields of the model.
	 *
	 * @param outputStructure the array of DataField objects representing the output
	 *                        structure
	 */
	public void setOutputFields(DataField[] outputStructure) {
		outputfield = outputStructure;

	}

	public abstract StreamElement[] pushData(StreamElement streamElement, String origin);

	public abstract StreamElement[] query(StreamElement params);

	public abstract void setParam(String k, String string);

	public boolean initialize() {
		return true;
	}

	/**
	 * Sets the virtual sensor for the model.
	 * 
	 * @param v the virtual sensor to be set
	 */
	public void setVirtualSensor(ModellingVirtualSensor v) {
		vs = v;
	}

	/**
	 * Returns the ModellingVirtualSensor associated with this AbstractModel.
	 *
	 * @return the ModellingVirtualSensor associated with this AbstractModel
	 */
	public ModellingVirtualSensor getVirtualSensor() {

		return vs;
	}

}
