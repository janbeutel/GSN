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
* File: src/ch/epfl/gsn/utils/EmptyEnumerate.java
*
* @author Ali Salehi
*
*/

package ch.epfl.gsn.utils;

import java.util.Enumeration;
import java.util.NoSuchElementException;

public class EmptyEnumerate<T> implements Enumeration {

   /**
    * Returns whether there are more elements to iterate over.
    *
    * @return true if there are more elements, false otherwise
    */
   public boolean hasMoreElements() {
      return false;
   }

   /**
      * Returns the next element in the enumeration.
      *
      * @return the next element in the enumeration
      * @throws NoSuchElementException if there are no more elements in the enumeration
      */
   public Object nextElement() throws NoSuchElementException {
      return new NoSuchElementException("This is an Empty Enumerator");
   }

}
