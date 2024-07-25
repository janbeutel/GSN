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
* File: src/ch/epfl/gsn/http/rest/DeliverySystem.java
*
* @author Ali Salehi
* @author Timotee Maret
* @author Davide De Sclavis
* @author Manuel Buchauer
* @author Jan Beutel
*
*/

package ch.epfl.gsn.delivery;

import java.io.IOException;

import ch.epfl.gsn.beans.DataField;
import ch.epfl.gsn.beans.StreamElement;

/**
 * The DeliverySystem interface represents a delivery system in a Global Sensor
 * Networks (GSN) application.
 */
public interface DeliverySystem {

	public abstract void writeStructure(DataField[] fields) throws IOException;

	public abstract boolean writeStreamElement(StreamElement se);

	public abstract boolean writeKeepAliveStreamElement();

	public abstract void close();

	public abstract boolean isClosed();

}