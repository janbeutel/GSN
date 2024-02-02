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
* File: src/ch/epfl/gsn/utils/ParamParser.java
*
* @author Ali Salehi
*
*/

package ch.epfl.gsn.utils;

public class ParamParser {

   /**
    * Parses the given input string into an integer value.
    * If the input is null or cannot be parsed, the default value is returned.
    *
    * @param input        the input string to be parsed
    * @param defaultValue the default value to be returned if parsing fails
    * @return the parsed integer value or the default value if parsing fails
    */
   public static int getInteger(String input, int defaultValue) {
      if (input == null) {
         return defaultValue;
      }
      try {
         return Integer.parseInt(input);
      } catch (Exception e) {
         return defaultValue;
      }
   }

   /**
    * Parses the input object into an integer value.
    * If the input is null or cannot be parsed into an integer, the default value
    * is returned.
    *
    * @param input        the input object to be parsed
    * @param defaultValue the default value to be returned if parsing fails
    * @return the parsed integer value or the default value if parsing fails
    */
   public static int getInteger(Object input, int defaultValue) {
      if (input == null) {
         return defaultValue;
      }
      try {
         if (input instanceof String) {
            return getInteger((String) input, defaultValue);
         }
         if (input instanceof Number) {
            return ((Number) input).intValue();
         } else {
            return Integer.parseInt(input.toString());
         }
      } catch (Exception e) {
         return defaultValue;
      }
   }
}
