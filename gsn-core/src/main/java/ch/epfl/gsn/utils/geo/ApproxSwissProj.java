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
* File: src/ch/epfl/gsn/utils/geo/ApproxSwissProj.java
*
* @author Sofiane Sarni
* @author Davide De Sclavis
* @author Manuel Buchauer
* @author Jan Beutel
*
*/

/*
source: http://www.swisstopo.admin.ch/internet/swisstopo/en/home/products/software/products/skripts.html
WGS84<->CH1903 (05.1999)
U. Marti swisstopo / H. Dupraz EPFL

Supported reference frames

    WGS84 global geographic coordinates (degrees or degrees / minutes / seconds)
    Swiss national coordinates (CH1903/LV03)

Map projection

    Oblique, conformal cylindrical projection (Mercator projection)
    Bessel ellipsoid 1841
    The projection center is the fundamental point at the old observatory in Bern
    (Longitude 7  26 '22:50 "/ latitude 46  57' 08.66" -> coordinates 600'000 .000 East / North 200'000 .000)
    Approximation (accuracy on the 1-meter level)
 */

package ch.epfl.gsn.utils.geo;

public class ApproxSwissProj {

	/**
	 * Converts a height value from the Swiss military coordinate system to the
	 * WGS84 coordinate system.
	 *
	 * @param y The y-coordinate value in the Swiss military coordinate system.
	 * @param x The x-coordinate value in the Swiss military coordinate system.
	 * @param h The height value in the Swiss military coordinate system.
	 * @return The converted height value in the WGS84 coordinate system.
	 */
	private static double CHtoWGSheight(double y, double x, double h) {
		// Converts militar to civil and to unit = 1000km
		// Axiliary values (% Bern)
		double y_aux = (y - 600000) / 1000000;
		double x_aux = (x - 200000) / 1000000;

		// Process height
		double height = h;
		height = (height + 49.55) - (12.60 * y_aux) - (22.64 * x_aux);

		return height;
	}

	/**
	 * Converts latitude coordinates from the Swiss military coordinate system to
	 * the WGS84 coordinate system.
	 *
	 * @param y The y-coordinate value in the Swiss military coordinate system.
	 * @param x The x-coordinate value in the Swiss military coordinate system.
	 * @return The converted latitude value in the WGS84 coordinate system.
	 */
	private static double CHtoWGSlat(double y, double x) {
		// Converts militar to civil and to unit = 1000km
		// Axiliary values (% Bern)
		double y_aux = (y - 600000) / 1000000;
		double x_aux = (x - 200000) / 1000000;

		// Process lat
		double lat = (16.9023892 + (3.238272 * x_aux))
				- (0.270978 * Math.pow(y_aux, 2))
				- (0.002528 * Math.pow(x_aux, 2))
				- (0.0447 * Math.pow(y_aux, 2) * x_aux)
				- (0.0140 * Math.pow(x_aux, 3));

		// Unit 10000" to 1 " and converts seconds to degrees (dec)
		lat = (lat * 100) / 36;

		return lat;
	}

	/**
	 * Converts longitude coordinates from the Swiss military coordinate system to
	 * the WGS84 coordinate system.
	 *
	 * @param y The y-coordinate value in the Swiss military coordinate system.
	 * @param x The x-coordinate value in the Swiss military coordinate system.
	 * @return The converted longitude value in the WGS84 coordinate system.
	 */
	private static double CHtoWGSlng(double y, double x) {
		// Converts militar to civil and to unit = 1000km
		// Axiliary values (% Bern)
		double y_aux = (y - 600000) / 1000000;
		double x_aux = (x - 200000) / 1000000;

		// Process long
		double lng = (2.6779094 + (4.728982 * y_aux)
				+ (0.791484 * y_aux * x_aux) + (0.1306 * y_aux * Math.pow(
						x_aux, 2)))
				- (0.0436 * Math.pow(y_aux, 3));

		// Unit 10000" to 1 " and converts seconds to degrees (dec)
		lng = (lng * 100) / 36;

		return lng;
	}

	/**
	 * Convert decimal angle (degrees) to sexagesimal angle (degrees, minutes and
	 * seconds dd.mmss,ss)
	 *
	 * @param dec The decimal angle value to be converted.
	 * @return The sexagesimal angle representation of the input decimal angle
	 *         value.
	 */
	public static double DecToSexAngle(double dec) {
		int deg = (int) Math.floor(dec);
		int min = (int) Math.floor((dec - deg) * 60);
		double sec = (((dec - deg) * 60) - min) * 60;

		// Output: dd.mmss(,)ss
		return deg + ((double) min / 100) + (sec / 10000);
	}

	/**
	 * Convert LV03 to WGS84 Return a array of double that contain lat, long,
	 * and height
	 *
	 * @param east
	 * @param north
	 * @param height
	 * @return
	 */
	public static double[] LV03toWGS84(double east, double north, double height) {

		double d[] = new double[3];

		d[0] = CHtoWGSlat(east, north);
		d[1] = CHtoWGSlng(east, north);
		d[2] = CHtoWGSheight(east, north, height);
		return d;
	}

	/**
	 * Convert sexagesimal angle (degrees, minutes and seconds dd.mmss,ss) to
	 * seconds.
	 *
	 * @param dms The sexagesimal angle value to be converted.
	 * @return The equivalent angle value in seconds.
	 */
	public static double SexAngleToSeconds(double dms) {
		double deg = 0, 
			   min = 0, 
			   sec = 0;
		deg = Math.floor(dms);
		min = Math.floor((dms - deg) * 100);
		sec = (((dms - deg) * 100) - min) * 100;

		// Result in degrees sex (dd.mmss)
		return sec + (min * 60) + (deg * 3600);
	}

	/**
	 * Convert sexagesimal angle (degrees, minutes and seconds "dd.mmss") to decimal
	 * angle (degrees)
	 *
	 * @param dms The sexagesimal angle value to be converted.
	 * @return The equivalent decimal angle value.
	 */
	public static double SexToDecAngle(double dms) {
		// Extract DMS
		// Input: dd.mmss(,)ss
		double deg = 0, min = 0, sec = 0;
		deg = Math.floor(dms);
		min = Math.floor((dms - deg) * 100);
		sec = (((dms - deg) * 100) - min) * 100;

		// Result in degrees dec (dd.dddd)
		return deg + (min / 60) + (sec / 3600);
	}

	/**
	 * Convert WGS84 to LV03 Return an array of double that contaign east,
	 * north, and height
	 *
	 * @param latitude
	 * @param longitude
	 * @param ellHeight
	 * @return
	 */
	public static double[] WGS84toLV03(double latitude, double longitude,
			double ellHeight) {
		// , ref double east, ref double north, ref double height
		double d[] = new double[3];

		d[0] = WGStoCHy(latitude, longitude);
		d[1] = WGStoCHx(latitude, longitude);
		d[2] = WGStoCHh(latitude, longitude, ellHeight);
		return d;
	}

	/**
	 * Convert WGS lat/long (deg dec) and height to CH h
	 *
	 * @param lat The latitude value in WGS84 coordinate system.
	 * @param lng The longitude value in WGS84 coordinate system.
	 * @param h   The ellipsoidal height value in WGS84 coordinate system.
	 * @return The converted height value in LV03 coordinate system.
	 */
	private static double WGStoCHh(double lat, double lng, double h) {
		// Converts degrees dec to sex
		double latitude = DecToSexAngle(lat);
		double longitude = DecToSexAngle(lng);

		// Converts degrees to seconds (sex)
		latitude = SexAngleToSeconds(latitude);
		longitude = SexAngleToSeconds(longitude);

		// Axiliary values (% Bern)
		double lat_aux = (latitude - 169028.66) / 10000;
		double lng_aux = (longitude - 26782.5) / 10000;

		// Process h
		double height = h;
		height = (height - 49.55) + (2.73 * lng_aux) + (6.94 * lat_aux);

		return height;
	}

	/**
	 * Convert WGS lat/long (deg dec) to CH x
	 *
	 * @param lat The latitude value in WGS84 coordinate system.
	 * @param lng The longitude value in WGS84 coordinate system.
	 * @return The converted x-coordinate value in LV03 coordinate system.
	 */
	private static double WGStoCHx(double lat, double lng) {
		// Converts degrees dec to sex
		lat = DecToSexAngle(lat);
		lng = DecToSexAngle(lng);

		// Converts degrees to seconds (sex)
		lat = SexAngleToSeconds(lat);
		lng = SexAngleToSeconds(lng);

		// Axiliary values (% Bern)
		double lat_aux = (lat - 169028.66) / 10000;
		double lng_aux = (lng - 26782.5) / 10000;

		// Process X
		double x = ((200147.07 + (308807.95 * lat_aux)
				+ (3745.25 * Math.pow(lng_aux, 2)) + (76.63 * Math.pow(lat_aux,
						2)))
				- (194.56 * Math.pow(lng_aux, 2) * lat_aux))
				+ (119.79 * Math.pow(lat_aux, 3));

		return x;
	}

	/**
	 * Convert WGS lat/long (deg dec) to CH y
	 *
	 * @param lat The latitude value in WGS84 coordinate system.
	 * @param lng The longitude value in WGS84 coordinate system.
	 * @return The converted y-coordinate value in LV03 coordinate system.
	 */
	private static double WGStoCHy(double lat, double lng) {
		// Converts degrees dec to sex
		lat = DecToSexAngle(lat);
		lng = DecToSexAngle(lng);

		// Converts degrees to seconds (sex)
		lat = SexAngleToSeconds(lat);
		lng = SexAngleToSeconds(lng);

		// Axiliary values (% Bern)
		double lat_aux = (lat - 169028.66) / 10000;
		double lng_aux = (lng - 26782.5) / 10000;

		// Process Y
		double y = (600072.37 + (211455.93 * lng_aux))
				- (10938.51 * lng_aux * lat_aux)
				- (0.36 * lng_aux * Math.pow(lat_aux, 2))
				- (44.54 * Math.pow(lng_aux, 3));

		return y;
	}

	private ApproxSwissProj() {
		// Only static
	}

}
