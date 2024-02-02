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
* File: src/ch/epfl/gsn/utils/Formatter.java
*
* @author Sofiane Sarni
*
*/

package ch.epfl.gsn.utils;

import java.util.List;

/**
 * The Formatter class provides utility methods for formatting arrays and lists.
 */
public class Formatter {

    /**
     * Returns a formatted string representation of the specified array elements.
     *
     * @param a    the array of integers
     * @param from the starting index (inclusive)
     * @param to   the ending index (exclusive)
     * @return a formatted string representation of the array elements
     */
    public static String listArray(int[] a, int from, int to) {
        return listArray(a, from, to, false);
    }

    /**
     * Returns a string representation of the given integer array.
     *
     * @param a the integer array to be converted to a string
     * @return a string representation of the integer array
     */
    public static String listArray(int[] a) {
        return listArray(a, 0, a.length - 1);
    }

    /**
     * Returns a string representation of the specified integer array, starting from
     * index 0 and ending at the specified length.
     *
     * @param a   the array to be represented as a string
     * @param len the length of the array to be represented
     * @return a string representation of the specified array
     */
    public static String listArray(int[] a, int len) {
        return listArray(a, 0, len - 1);
    }

    /**
     * Returns a string representation of the specified integer array.
     *
     * @param a         the array of integers
     * @param len       the length of the array
     * @param hexFormat whether to format the integers as hexadecimal values
     * @return a formatted string representation of the array
     */
    public static String listArray(int[] a, int len, boolean hexFormat) {
        return listArray(a, 0, len - 1, hexFormat);
    }

    /**
     * Returns a string representation of the specified integer array.
     * 
     * @param a         the array of integers
     * @param from      the starting index of the array to format
     * @param to        the ending index of the array to format
     * @param hexFormat specifies whether the output should be in hexadecimal format
     * @return the formatted string representation of the array
     */
    public static String listArray(int[] a, int from, int to, boolean hexFormat) {
        StringBuilder hex_sb_2 = new StringBuilder();
        StringBuilder dec_sb_2 = new StringBuilder();
        for (int i = from; (i <= to && i < a.length); i++) {
            hex_sb_2.append(String.format("%02x", a[i] & 0xff)).append(" ");
            dec_sb_2.append(a[i] & 0xff).append(" ");
        }

        hex_sb_2.append("(").append(String.format("%2d", to - from + 1)).append(")");
        dec_sb_2.append("(").append(String.format("%2d", to - from + 1)).append(")");

        if (hexFormat) {
            return hex_sb_2.toString();
        } else {
            return dec_sb_2.toString();
        }

    }

    /**
     * Returns a formatted string representation of the specified byte array, from
     * the specified index 'from' to the specified index 'to'.
     * 
     * @param a    the byte array
     * @param from the starting index (inclusive)
     * @param to   the ending index (exclusive)
     * @return a formatted string representation of the byte array
     */
    public static String listArray(byte[] a, int from, int to) {
        return listArray(a, from, to, false);
    }

    /**
     * Returns a string representation of the specified byte array, starting from
     * index 0 and ending at the specified length.
     *
     * @param a   the byte array
     * @param len the length of the array to be represented as a string
     * @return a string representation of the specified byte array
     */
    public static String listArray(byte[] a, int len) {
        return listArray(a, 0, len - 1);
    }

    /**
     * Returns a formatted string representation of the given byte array.
     *
     * @param a         the byte array to be formatted
     * @param len       the length of the byte array to be formatted
     * @param hexFormat true if the byte array should be formatted in hexadecimal
     *                  format, false otherwise
     * @return a formatted string representation of the byte array
     */
    public static String listArray(byte[] a, int len, boolean hexFormat) {
        return listArray(a, 0, len - 1, hexFormat);
    }

    /**
     * Formats a byte array into a string representation.
     *
     * @param a         the byte array to be formatted
     * @param from      the starting index of the array to be formatted
     * @param to        the ending index of the array to be formatted
     * @param hexFormat specifies whether the string should be formatted in
     *                  hexadecimal or decimal format
     * @return the formatted string representation of the byte array
     */
    public static String listArray(byte[] a, int from, int to, boolean hexFormat) {
        StringBuilder hex_sb_2 = new StringBuilder();
        StringBuilder dec_sb_2 = new StringBuilder();
        for (int i = from; (i <= to && i < a.length); i++) {
            hex_sb_2.append(String.format("%02x", a[i] & 0xff)).append(" ");
            dec_sb_2.append(a[i] & 0xff).append(" ");
        }

        hex_sb_2.append("(").append(String.format("%2d", to - from + 1)).append(")");
        dec_sb_2.append("(").append(String.format("%2d", to - from + 1)).append(")");

        if (hexFormat) {
            return hex_sb_2.toString();
        } else {
            return dec_sb_2.toString();
        }
    }

    /**
     * Returns a formatted string representation of the specified array of
     * UnsignedByte objects,
     * from the specified index 'from' to the specified index 'to'.
     *
     * @param a    the array of UnsignedByte objects
     * @param from the starting index (inclusive)
     * @param to   the ending index (exclusive)
     * @return a formatted string representation of the specified array
     */
    public static String listArray(UnsignedByte[] a, int from, int to) {
        return listArray(a, from, to, false);
    }

    /**
     * Returns a formatted string representation of an array of UnsignedByte
     * objects.
     *
     * @param a   the array of UnsignedByte objects
     * @param len the length of the array
     * @return a formatted string representation of the array
     */
    public static String listArray(UnsignedByte[] a, int len) {
        return listArray(a, 0, len - 1);
    }

    /**
     * Returns a formatted string representation of an array of UnsignedByte
     * objects.
     *
     * @param a         the array of UnsignedByte objects
     * @param len       the length of the array
     * @param hexFormat true if the string representation should be in hexadecimal
     *                  format, false otherwise
     * @return a formatted string representation of the array
     */
    public static String listArray(UnsignedByte[] a, int len, boolean hexFormat) {
        return listArray(a, 0, len - 1, hexFormat);
    }

    public static String listArray(UnsignedByte[] a, int from, int to, boolean hexFormat) {
        StringBuilder hex_sb_2 = new StringBuilder();
        StringBuilder dec_sb_2 = new StringBuilder();
        for (int i = from; (i <= to && i < a.length); i++) {
            hex_sb_2.append(String.format("%02x", a[i].getByte())).append(" ");
            dec_sb_2.append(a[i].getInt()).append(" ");
        }

        hex_sb_2.append("(").append(String.format("%2d", to - from + 1)).append(")");
        dec_sb_2.append("(").append(String.format("%2d", to - from + 1)).append(")");

        if (hexFormat) {
            return hex_sb_2.toString();
        } else {
            return dec_sb_2.toString();
        }
    }

    /**
     * Returns a string representation of the given list of UnsignedByte objects.
     * 
     * @param a the list of UnsignedByte objects
     * @return a string representation of the list
     */
    public static String listUnsignedByteList(List<UnsignedByte> a) {
        return listUnsignedByteList(a, false);
    }

    /**
     * Formats a list of UnsignedByte objects into a string representation.
     * 
     * @param a         the list of UnsignedByte objects to be formatted
     * @param hexFormat specifies whether the output should be in hexadecimal format
     *                  or decimal format
     * @return the formatted string representation of the list of UnsignedByte
     *         objects
     */
    public static String listUnsignedByteList(List<UnsignedByte> a, boolean hexFormat) {
        StringBuilder hex_sb_2 = new StringBuilder();
        StringBuilder dec_sb_2 = new StringBuilder();
        for (int i = 0; i < a.size(); i++) {
            hex_sb_2.append(String.format("%02x", a.get(i).getByte())).append(" ");
            dec_sb_2.append(a.get(i).getInt()).append(" ");
        }

        hex_sb_2.append("(").append(String.format("%2d", a.size())).append(")");
        dec_sb_2.append("(").append(String.format("%2d", a.size())).append(")");

        if (hexFormat) {
            return hex_sb_2.toString();
        } else {
            return dec_sb_2.toString();
        }
    }

}
