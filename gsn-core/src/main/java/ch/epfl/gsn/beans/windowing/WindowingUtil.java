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
* File: src/ch/epfl/gsn/beans/windowing/WindowingUtil.java
*
* @author gsn_devs
* @author Timotee Maret
* @author Davide De Sclavis
* @author Manuel Buchauer
* @author Jan Beutel
*
*/

package ch.epfl.gsn.beans.windowing;

public class WindowingUtil {

    /**
     * Calculates the greatest common divisor (GCD) of two numbers.
     *
     * @param a the first number
     * @param b the second number
     * @return the GCD of the two numbers
     */
    public static long GCD(long a, long b) {
        if (a == 0 || b == 0) {
            return 0;
        }
        return GCDHelper(a, b);
    }

    /**
     * Calculates the greatest common divisor (GCD) of two numbers using the
     * Euclidean algorithm.
     *
     * @param a the first number
     * @param b the second number
     * @return the GCD of the two numbers
     */
    private static long GCDHelper(long a, long b) {
        if (b == 0) {
            return a;
        }
        return GCDHelper(b, a % b);
    }
}
