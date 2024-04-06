package ch.epfl.gsn.vsensor;

import java.awt.image.renderable.ParameterBlock;
import java.awt.image.RenderedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URLConnection;
import java.util.TreeMap;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageReadParam;
import javax.imageio.stream.ImageInputStream;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

import ch.epfl.gsn.beans.DataTypes;
import ch.epfl.gsn.beans.StreamElement;
import ch.epfl.gsn.beans.VSensorConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.media.jai.codec.SeekableStream;

public class JpegGetterVirtualSensor extends BridgeVirtualSensorPermasense {
	
	private static final String DEFAULT_NEF_EXTRACTION_TYPE = "thumbnail";
	
	private static final String DCRAW = "dcraw";

	private static final transient Logger logger = LoggerFactory.getLogger(JpegGetterVirtualSensor.class);

	private String storage_directory = null;
	private String nef_extraction_type = null;
	private String dcraw_flip = null;
	private Double rotation = null;	
	
	@Override
	public boolean initialize() {
		boolean ret = super.initialize();
		VSensorConfig vsensor = getVirtualSensorConfiguration();
		TreeMap<String,String> params = vsensor.getMainClassInitialParams();

		storage_directory = vsensor.getStorage().getStorageDirectory();
		if (storage_directory != null) {
			storage_directory = new File(storage_directory, deployment).getPath();
		}
		nef_extraction_type = params.get("nef_extraction_type");
		if (nef_extraction_type == null)
			nef_extraction_type = DEFAULT_NEF_EXTRACTION_TYPE;
		
		dcraw_flip = params.get("dcraw_flip");
		if (params.get("rotation")!=null)
			rotation = new Double(params.get("rotation"));
		return ret;
	}
	
	
	@Override
	public void dataAvailable(String inputStreamName, StreamElement data) {
		File file;
		if (storage_directory == null) {
			String filename = (String) data.getData("file");
			if (filename != null) {
				file = new File(filename);
				if (!file.exists()) {
					logger.error(file.getAbsolutePath() + " does not exist");
					return;
				}
			}
			else {
				logger.error("no file specified");
				return;
			}
		}
		else {
			file = new File(new File(storage_directory, Integer.toString((Integer)data.getData("device_id"))).getPath(), (String) data.getData("relative_file"));
		}
		file = file.getAbsoluteFile();

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		InputStream is = null;
		String mimeType = null;
		try {
			mimeType = URLConnection.guessContentTypeFromStream(new BufferedInputStream(new FileInputStream(file)));
		} catch (Exception e) {
			logger.error(file.getAbsolutePath() + ": " + e.getMessage(), e);
		}
		
		if (mimeType != null && (mimeType.equalsIgnoreCase("image/png") || mimeType.equalsIgnoreCase("image/jpeg") || mimeType.equalsIgnoreCase("image/pjpeg"))) {
			logger.debug("getting image: " + file.getAbsolutePath());
		    try {
		    	is = new FileInputStream(file);
		    } catch (IOException e) {
		    	logger.error(file.getAbsolutePath() + ": " + e.getMessage(), e);
		    }
		}
		else {
			logger.debug("exctracting image from: " + file.getAbsolutePath());
			Process p = null;
			StringBuffer cmd = new StringBuffer(DCRAW + " -c");
			if (nef_extraction_type.equalsIgnoreCase("thumbnail"))
				cmd.append(" -e");
			if (dcraw_flip != null)
				cmd.append(" -t "+dcraw_flip);
			cmd.append(" "+file.getAbsolutePath());
			logger.debug("exec "+cmd);
			try {
				p = Runtime.getRuntime().exec(cmd.toString());
				is = p.getInputStream();
			} catch (IOException e) {
				logger.error(file.getAbsolutePath() + ": " + e.getMessage(), e);
			}
		}
		
		if (is != null) {
			try (ImageInputStream s = ImageIO.createImageInputStream(is)) {
				// Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("TIFF"); // Could also use ImageIO.getImageReaders(s) to support any input format
				Iterator<ImageReader> readers = ImageIO.getImageReaders(s); 
				if (!readers.hasNext()) {
					return; // ...or throw new IllegalArgumentException or similar
				}
			ImageReader dec = readers.next();
			dec.setInput(s);
			// The above code doesn't seem to use param, but if needed that is also possible
			ImageReadParam param = null; // dec.getDefaultReadParam() or new TIFFImageReadParam()
			RenderedImage image = dec.readAsRenderedImage(0, param); // Could als use dec.read(0, param) in most cases 

			logger.debug("create rendered image");
			if (rotation!=null) {
				// rotate
				logger.debug("rotate " + file.getAbsolutePath() + " by "+ rotation);
				// 	rotation center
				float centerX = (float)image.getWidth() / 2;
				float centerY = (float)image.getHeight() / 2;
				logger.debug("centerx " + centerX + " centery "+ centerY);
				ParameterBlock pb = new ParameterBlock();
				pb.addSource(image);
				pb.add(centerX);
				pb.add(centerY);
				pb.add((float)(rotation / 180d * Math.PI));
				pb.add(new javax.media.jai.InterpolationBicubic(10));
				// create a new, rotated image
				image = JAI.create("rotate", pb);
				logger.debug("after create rotate");
			}
			try {
				// ImageIO.write(image.getRenderedImage(), "jpg", os);
				ImageIO.write(image, "jpg", os);
				logger.debug("after write jpg");
			} catch (IOException e) {
				logger.error(file.getAbsolutePath() + ": " + e.getMessage(), e);
				return;
			}
			data = new StreamElement(data, 
					new String[]{"jpeg_scaled"},
					new Byte[]{DataTypes.BINARY},
					new Serializable[]{os.toByteArray()});
			os.close();
			is.close();

			} catch (IOException e) {
				logger.error("something is wrong " + e.getMessage(), e);
			}
	
			super.dataAvailable(inputStreamName, data);
		}
		else
			logger.error("file " + file.getName() + " could not be forwarded as jpeg");
	}
}
