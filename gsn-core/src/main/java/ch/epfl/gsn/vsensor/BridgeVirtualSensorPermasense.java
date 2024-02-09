package ch.epfl.gsn.vsensor;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.TreeMap;
import java.util.Vector;

import javax.imageio.ImageIO;

import ch.epfl.gsn.beans.DataTypes;
import ch.epfl.gsn.beans.StreamElement;
import ch.epfl.gsn.beans.VSensorConfig;
import ch.epfl.gsn.processor.ScriptletProcessor;
import ch.epfl.gsn.utils.ParamParser;
import ch.epfl.gsn.utils.Helpers;
import ch.epfl.gsn.wrappers.DataMappingWrapper;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

/**
 *
 * @author Tonio Gsell
 * @author Mustafa Yuecel
 */

/**
 * BridgeVirtualSensorPermasense extends ScriptletProcessor to provide
 * functionality for processing Permasense sensor data.
 */
public class BridgeVirtualSensorPermasense extends ScriptletProcessor {
	private static final int DEFAULT_WIDTH = 610;
	private final static transient Logger logger = LoggerFactory.getLogger(BridgeVirtualSensorPermasense.class);

	protected String deployment;
	private int width;
	private Vector<String> jpeg_scaled;
	private String rotate_image;
	private boolean position_mapping = false;
	private boolean sensortype_mapping = false;
	private boolean sensorvalue_conversion = false;
	private boolean gps_time_conversion = false;
	private boolean processScriptlet;

	/**
	 * Initializes the BridgeVirtualSensorPermasense by parsing
	 * the virtual sensor configuration file
	 * 
	 * Sets the deployment name, image width, rotate image flag,
	 * list of images to scale, and enables position mapping, sensor type mapping,
	 * sensor value conversion, and GPS time conversion based on configuration.
	 * 
	 * Calls the parent initialize() method.
	 * 
	 * @return true if successful initialization
	 */
	@Override
	public boolean initialize() {
		String s;
		int i;
		VSensorConfig vsensor = getVirtualSensorConfiguration();
		TreeMap<String, String> params = vsensor.getMainClassInitialParams();

		deployment = vsensor.getName().split("_")[0].toLowerCase();

		width = ParamParser.getInteger(params.get("width"), DEFAULT_WIDTH);

		rotate_image = params.get("rotate_image");

		String[] jpeg = null;
		s = params.get("jpeg_scaled");
		if (s != null) {
			jpeg = s.split(",");
		}

		if (jpeg == null) {
			jpeg = new String[] {};
		}
		jpeg_scaled = new Vector<String>(jpeg.length);
		for (i = 0; i < jpeg.length; i++) {
			jpeg_scaled.addElement(jpeg[i].trim().toLowerCase());
		}

		if (params.get("position_mapping") != null) {
			position_mapping = true;
		}
		if (params.get("sensortype_mapping") != null) {
			sensortype_mapping = true;
		}
		if (params.get("sensorvalue_conversion") != null) {
			sensorvalue_conversion = true;
		}
		if (params.get("gps_time_conversion") != null) {
			gps_time_conversion = true;
		}
		processScriptlet = super.initialize();
		return true;
	}

	/**
	 * Processes incoming data from the input stream.
	 * Performs position mapping, sensor type mapping, sensor value conversion
	 * and GPS time conversion based on configuration.
	 * Scales images in the data to the configured width.
	 * 
	 * @param inputStreamName Name of the input stream
	 * @param data            The input data
	 */
	@Override
	public void dataAvailable(String inputStreamName, StreamElement data) {
		String s;
		StreamElement se = data;
		if (position_mapping && se.getData("device_id") != null && se.getData("generation_time") != null) {
			Integer position = DataMappingWrapper.getPosition(((Integer) se.getData("device_id")).intValue(),
					((Long) se.getData("generation_time")).longValue(),
					deployment, getVirtualSensorConfiguration().getName(), inputStreamName);
			se = new StreamElement(se,
					new String[] { "position" },
					new Byte[] { DataTypes.INTEGER },
					new Serializable[] { position });

		}
		if (sensortype_mapping &&
				se.getData("position") != null && se.getData("generation_time") != null) {
			Serializable[] sensortype = DataMappingWrapper.getSensorType(
					((Integer) se.getData("position")).intValue(),
					((Long) se.getData("generation_time")).longValue(),
					deployment, getVirtualSensorConfiguration().getName(), inputStreamName);
			se = new StreamElement(se,
					new String[] { "sensortype", "sensortype_serialid" },
					new Byte[] { DataTypes.VARCHAR, DataTypes.BIGINT },
					sensortype);
		}
		if (sensorvalue_conversion && se.getData("position") != null && se.getData("generation_time") != null) {
			se = DataMappingWrapper.getConvertedValues(se, deployment, getVirtualSensorConfiguration().getName(),
					inputStreamName);
		}
		if (gps_time_conversion && se.getData("gps_time") != null && se.getData("gps_week") != null) {
			se = new StreamElement(se,
					new String[] { "gps_unixtime" },
					new Byte[] { DataTypes.BIGINT },
					new Serializable[] { (long) (Helpers.convertGPSTimeToUnixTime(
							(double) ((Integer) se.getData("gps_time") / 1000.0), (Short) se.getData("gps_week"))
							* 1000.0) });
		}

		for (Enumeration<String> elem = jpeg_scaled.elements(); elem.hasMoreElements();) {
			// scale image to given width
			s = elem.nextElement();
			if (se.getData(s) != null) {
				try {
					BufferedImage image;
					try {
						image = ImageIO.read(new ByteArrayInputStream((byte[]) se.getData(s)));
					} catch (IOException e) {
						logger.error("Could not read image: skip image!", e);
						return;
					}

					// use Graphics2D for scaling -> make usage of GPU
					double factor = (float) width / image.getWidth();
					BufferedImage scaled = new BufferedImage(width, (int) (image.getHeight() * factor),
							BufferedImage.TYPE_INT_RGB);
					Graphics2D g = scaled.createGraphics();
					if (rotate_image != null) {
						g.rotate(Math.toRadians(Integer.parseInt(rotate_image)), scaled.getWidth() / 2d,
								scaled.getHeight() / 2d);
					}
					g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
					AffineTransform at = AffineTransform.getScaleInstance(factor, factor);
					g.drawRenderedImage(image, at);
					g.dispose();

					ByteArrayOutputStream os = new ByteArrayOutputStream();
					ImageIO.write(scaled, "jpeg", os);

					se.setData(s, os.toByteArray());
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		}

		if (processScriptlet) {
			super.dataAvailable(inputStreamName, se);
		} else {
			dataProduced(se);
		}
	}

	@Override
	public synchronized void dispose() {
		if (processScriptlet) {
			super.dispose();
		}
	}
}
