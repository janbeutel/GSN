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
* File: src/ch/epfl/gsn/utils/Helpers.java
*
* @author Timotee Maret
* @author Ali Salehi
* @author Sofiane Sarni
*
*/

package ch.epfl.gsn.utils;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * The Helpers class provides utility methods for time conversion and
 * formatting.
 */
public class Helpers {
	private static final long GPS_OFFSET = 315964800L;

	private static final long[] leaps = { 46828800L, 78364801L, 109900802L, 173059203L, 252028804L,
			315187205L, 346723206L, 393984007L, 425520008L, 457056009L, 504489610L,
			551750411L, 599184012L, 820108813L, 914803214L, 1025136015L, 1119744016L, 1167264017L };


	/**
	 * Formats the given timestamp into a human-readable time period string.
	 *
	 * @param timestamp the timestamp to format
	 * @return the formatted time period string
	 */
	public static String formatTimePeriod(long timestamp) {
		if (timestamp < 1000) {
			return timestamp + " ms";
		}
		if (timestamp < 60 * 1000) {
			return (timestamp / 1000) + " sec";
		}
		if (timestamp < 60 * 60 * 1000) {
			return (timestamp / (1000 * 60)) + " min";
		}
		if (timestamp < 24 * 60 * 60 * 1000) {
			return (timestamp / (1000 * 60 * 60)) + " h";
		}
		return (timestamp / (24 * 1000 * 60 * 60)) + " day";
	}

	/**
	 * Converts a time string in ISO format to a long value representing
	 * milliseconds since the epoch.
	 *
	 * @param time the time string in ISO format
	 * @return the long value representing milliseconds since the epoch
	 * @throws Exception if the time string is not in a valid ISO format
	 */
	public static long convertTimeFromIsoToLong(String time) throws Exception {
		DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
		return fmt.parseDateTime(time).getMillis();
	}

	/**
	 * Converts a time string in ISO format to a long value representing
	 * milliseconds.
	 *
	 * @param time   the time string to convert
	 * @param format the format of the time string
	 * @return the converted time in milliseconds
	 * @throws Exception if an error occurs during the conversion
	 */
	public static long convertTimeFromIsoToLong(String time, String format) throws Exception {
		DateTimeFormatter fmt = DateTimeFormat.forPattern(format);
		return fmt.parseDateTime(time).getMillis();
	}

	/**
	 * Converts a long timestamp to an ISO formatted string representation.
	 *
	 * @param timestamp the long timestamp to convert
	 * @return the ISO formatted string representation of the timestamp
	 */
	public static String convertTimeFromLongToIso(long timestamp) {
		DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
		DateTime dt = new DateTime(timestamp);
		return fmt.print(dt);
	}

	/**
	 * Converts a long timestamp to a string representation in the specified format.
	 *
	 * @param timestamp the long timestamp to convert
	 * @param format    the format of the string representation (e.g., "yyyy-MM-dd
	 *                  HH:mm:ss")
	 * @return the string representation of the timestamp in the specified format
	 */
	public static String convertTimeFromLongToIso(long timestamp, String format) {
		DateTimeFormatter fmt = DateTimeFormat.forPattern(format);
		DateTime dt = new DateTime(timestamp);
		return fmt.print(dt);
	}

	/*
	 * Modified version (by Tonio Gsell) of:
	 * 
	 * gpstimeutil.js: a javascript library which translates between GPS and unix
	 * time
	 * 
	 * Copyright (C) 2012 Jeffery Kline
	 * 
	 * This program is free software: you can redistribute it and/or modify
	 * it under the terms of the GNU General Public License as published by
	 * the Free Software Foundation, either version 3 of the License, or
	 * (at your option) any later version.
	 * 
	 * This program is distributed in the hope that it will be useful,
	 * but WITHOUT ANY WARRANTY; without even the implied warranty of
	 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
	 * GNU General Public License for more details.
	 * 
	 * You should have received a copy of the GNU General Public License
	 * along with this program. If not, see &lt;http://www.gnu.org/licenses/&gt;.
	 * 
	 */

	//
	// v0.0: Sat May 19 22:24:16 CDT 2012
	// initial release
	// v0.1: Sat May 19 22:24:31 CDT 2012
	// fix bug converting negative fractional gps times
	// fix bug converting negative fractional unix times
	// introduce global variable
	// v0.2: Sat May 19 23:08:05 CDT 2012
	// ensure that unix2gps/gps2unix treats all input/output as Number

	/*
	 * Javascript code is based on original at:
	 * http://www.andrews.edu/~tzs/timeconv/timealgorithm.html
	 * 
	 * The difference between the original and this version is that this
	 * version handles the leap seconds using linear interpolation, not a
	 * discontinuity. Linear interpolation guarantees a 1-1 correspondence
	 * between gps times and unix times.
	 * 
	 * By contrast, for example, the original implementation maps both gps
	 * times 46828800.5 and 46828800 map to unix time 362793599.5
	 */

	
	/**
	 * Converts GPS time to Unix time.
	 * 
	 * @param gpsSec  the GPS seconds
	 * @param gpsWeek the GPS week
	 * @return the Unix time
	 */
	public static double convertGPSTimeToUnixTime(double gpsSec, short gpsWeek) {
		double gpsTime = (double) (gpsWeek * 604800 + gpsSec);

		if (gpsTime < 0) {
			return gpsTime + GPS_OFFSET;
		}

		double fpart = gpsTime % 1;
		long ipart = (long) Math.floor(gpsTime);

		long leap = countleaps(ipart, false);
		double unixTime = (double) (ipart + GPS_OFFSET - leap);

		if (isleap(ipart + 1)) {
			unixTime = unixTime + fpart / 2;
		} else if (isleap(ipart)) {
			unixTime = unixTime + (fpart + 1) / 2;
		} else {
			unixTime = unixTime + fpart;
		}
		return unixTime;
	}

	/**
	 * Checks if the given GPS time is a leap second.
	 *
	 * @param gpsTime the GPS time to check
	 * @return true if the GPS time is a leap second, false otherwise
	 */
	private static boolean isleap(long gpsTime) {
		boolean isLeap = false;
		for (int i = 0; i < leaps.length; i++) {
			if (gpsTime == leaps[i]) {
				isLeap = true;
				break;
			}
		}
		return isLeap;
	}

	/**
	 * Counts the number of leap seconds that have occurred since the given GPS
	 * time.
	 *
	 * @param gpsTime     the GPS time
	 * @param accum_leaps flag indicating whether to accumulate leap seconds or not
	 * @return the number of leap seconds
	 */
	private static long countleaps(long gpsTime, boolean accum_leaps) {
		long nleaps = 0;

		if (accum_leaps) {
			for (int i = 0; i < leaps.length; i++) {
				if (gpsTime + i >= leaps[i]) {
					nleaps++;
				}
			}
		} else {
			for (int i = 0; i < leaps.length; i++) {
				if (gpsTime >= leaps[i]) {
					nleaps++;
				}
			}
		}

		return nleaps;
	}

}
