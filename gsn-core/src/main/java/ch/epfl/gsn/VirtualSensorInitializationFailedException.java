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
* File: src/ch/epfl/gsn/VirtualSensorInitializationFailedException.java
*
* @author Ali Salehi
* @author Davide De Sclavis
* @author Manuel Buchauer
* @author Jan Beutel
*
*/

package ch.epfl.gsn;

public class VirtualSensorInitializationFailedException extends Exception {

   private static final long serialVersionUID = -6903638792983036844L;

   public VirtualSensorInitializationFailedException() {
      super();
   }

   public VirtualSensorInitializationFailedException(String message) {
      super(message);
   }

   public VirtualSensorInitializationFailedException(String message, Throwable cause) {
      super(message, cause);
   }

   public VirtualSensorInitializationFailedException(Throwable cause) {
      super(cause);
   }

}
