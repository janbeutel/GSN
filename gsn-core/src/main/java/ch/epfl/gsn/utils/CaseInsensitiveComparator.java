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
* File: src/ch/epfl/gsn/utils/CaseInsensitiveComparator.java
*
* @author Mehdi Riahi
* @author Ali Salehi
* @author Timotee Maret
* @author Davide De Sclavis
* @author Manuel Buchauer
* @author Jan Beutel
*
*/

package ch.epfl.gsn.utils;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Note that this class will trim all the space characters surrounding the key
 * and value pairs hence you don't need to call trim when putting or getting a
 * value to/from the hashmap.
 */
public class CaseInsensitiveComparator implements Comparator<Object>, Serializable {

   private static final long serialVersionUID = 2540687777213332025L;

   /**
    * Compares two objects in a case-insensitive manner.
    * 
    * @param o1 the first object to be compared
    * @param o2 the second object to be compared
    * @return 0 if both objects are null, a negative value if o1 is null, a positive value if o2 is null,
    *         or the result of comparing the string representations of o1 and o2 in a case-insensitive manner
    */
   public int compare(Object o1, Object o2) {
      if (o1 == null && o2 == null) {
         return 0;
      }
      if (o1 == null) {
         return -1;
      }
      if (o2 == null) {
         return 1;
      }
      String input1 = o1.toString().trim();
      String input2 = o2.toString().trim();
      return input1.compareToIgnoreCase(input2);
   }
}
