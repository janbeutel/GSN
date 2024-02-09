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
* File: src/ch/epfl/gsn/utils/UnsignedByte.java
*
* @author Sofiane Sarni
*
*/

package ch.epfl.gsn.utils;

public class UnsignedByte {
    private byte byteValue;
    private int intValue;

    /**
     * Default Constructor of an unsigned byte value.
     */
    public UnsignedByte() {
        byteValue = 0;
        intValue = 0;
    }

    /**
     * Constructs an UnsignedByte object with the specified byte value.
     * 
     * @param b the byte value to be represented as an unsigned byte
     */
    public UnsignedByte(byte b) {
        byteValue = b;
        intValue = (int) b & 0xff;
    }

    /**
     * Constructs an UnsignedByte object from an integer value.
     * The integer value is converted to an unsigned byte value by masking it with
     * 0xff.
     * 
     * @param i the integer value to be converted to an unsigned byte
     */
    public UnsignedByte(int i) {
        int value = i;
        value = value & 0xff;
        byteValue = (byte) value;
        intValue = value;
    }

    /**
     * Sets the value of the unsigned byte.
     * 
     * @param b the byte value to set
     * @return the updated UnsignedByte object
     */
    public UnsignedByte setValue(byte b) {
        byteValue = b;
        intValue = (int) b & 0xff;
        return this;
    }

    /**
     * Sets the value of the unsigned byte.
     * 
     * @param i the value to set
     * @return the updated UnsignedByte object
     */
    public UnsignedByte setValue(int i) {
        i = i & 0xff;
        byteValue = (byte) i;
        intValue = i;
        return this;
    }

    public int getInt() {
        return intValue;
    }

    public byte getByte() {
        return byteValue;
    }

    /**
     * Returns a string representation of the UnsignedByte object.
     * The string representation consists of the byte value and the corresponding
     * integer value.
     *
     * @return a string representation of the UnsignedByte object.
     */
    public String toString() {
        return "(byte:" + getByte() + ", int:" + getInt() + ")";
    }

    /**
     * Converts an array of UnsignedByte objects to a byte array.
     *
     * @param uba the array of UnsignedByte objects to be converted
     * @return the resulting byte array
     */
    public static byte[] UnsignedByteArray2ByteArray(UnsignedByte[] uba) {
        int length = uba.length;
        byte[] ba = new byte[length];
        for (int i = 0; i < length; i++) {
            ba[i] = uba[i].getByte();
        }
        return ba;
    }

    /**
     * Converts a byte array to an array of UnsignedByte objects.
     *
     * @param ba the byte array to be converted
     * @return an array of UnsignedByte objects representing the values of the byte
     *         array
     */
    public static UnsignedByte[] ByteArray2UnsignedByteArray(byte[] ba) {
        int length = ba.length;
        UnsignedByte[] uba = new UnsignedByte[length];
        for (int i = 0; i < length; i++) {
            uba[i].setValue(ba[i]);
        }
        return uba;
    }
}