package ch.epfl.gsn.wrappers.backlog.plugins;

import java.io.IOException;
import java.io.Serializable;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import ch.epfl.gsn.beans.DataField;
import ch.epfl.gsn.beans.InputInfo;
import ch.epfl.gsn.wrappers.BackLogWrapper;
import ch.epfl.gsn.wrappers.backlog.plugins.dpp.ComponentId;

public class DPPFirmwarePlugin extends AbstractPlugin {

	public static final short FW_PKT_TYPE_DATA    = 1;     // firmware binary data with offset
	public static final short FW_PKT_TYPE_CHECK   = 2;     // request FW verification
	public static final short FW_PKT_TYPE_READY   = 3;     // response to a FW validation request
	public static final short FW_PKT_TYPE_DATAREQ = 4;     // request missing FW data packets
	public static final short FW_PKT_TYPE_UPDATE  = 5;     // initiate the FW update

	private static DataField[] dataField = {
			new DataField("TIMESTAMP", "BIGINT"),
			new DataField("GENERATION_TIME", "BIGINT"),
			new DataField("GENERATION_TIME_MICROSEC", "BIGINT"),
			new DataField("DEVICE_ID", "INTEGER"),

			new DataField("TARGET_ID", "INTEGER"),
			new DataField("COMPONENT_ID", "INTEGER"),
			new DataField("SEQNR", "INTEGER"),
			new DataField("VERSION", "INTEGER"),
			new DataField("MESSAGE_TYPE", "SMALLINT"),
			new DataField("MESSAGE", "VARCHAR(256)"),
			new DataField("FIRMWARE", "BINARY")};

	private final transient Logger logger = LoggerFactory.getLogger( DPPFirmwarePlugin.class );
	
	@Override
	public boolean initialize( BackLogWrapper backLogWrapper, String coreStationName, String deploymentName) {
		activeBackLogWrapper = backLogWrapper;
		String p = getActiveAddressBean().getPredicateValue("priority");
		if (p == null)
			priority = null;
		else
			priority = Integer.valueOf(p);
		
		registerListener();
		return true;
	}
	
	@Override
	public boolean messageReceived(int deviceId, long timestamp, Serializable[] data) {
		try {
            Long generation_time = toLong(data[0]);
			Integer device_id = toInteger(data[1]);
			Integer target_id = toInteger(data[2]);
			Integer component_id = toInteger(data[3]);
			Integer seqnr = toInteger(data[4]);
			Integer ver = toInteger(data[5]);
			Short type = toShort(data[6]);
            String message = (String)data[7];
            //TODO: to binary
            Serializable ihex = data[8];
			if( dataProcessed(System.currentTimeMillis(), new Serializable[] {timestamp, (long)(generation_time/1000.0), generation_time, device_id, target_id, component_id, seqnr, ver, type, message, ihex}) ) {
				ackMessage(timestamp, super.priority);
				return true;
			} else {
				logger.warn("The DPP firmware message with timestamp >" + timestamp + "< could not be stored in the database.");
			}
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		return false;
	}

	@Override
	public InputInfo sendToPlugin(String action, String[] paramNames, Object[] paramValues) {
		long time = System.currentTimeMillis();
		

		Integer component_id = null;
		Integer target_id = null;
		Integer version = null;
		
		if( action.compareToIgnoreCase("firmware_data") == 0 ) {
			FileItem ihex_file = null;
			byte [] ihex_binary;
			for (int i = 0 ; i < paramNames.length ; i++) {
				if( paramNames[i].compareToIgnoreCase("intel_hex_file") == 0 )
					ihex_file = (FileItem)paramValues[i];
			}
			if (ihex_file == null) {
				logger.warn("ihex_file is missing: could not upload DPP firmware data");
				return new InputInfo(getActiveAddressBean().toString(), "ihex_file is missing: could not upload DPP firmware data", false);
			}
			
			ihex_binary = ihex_file.get();
			String[] filename_split = FilenameUtils.getBaseName(ihex_file.getName()).split("_");
			String component_name = filename_split[0];
					
			//TODO: implement with lookup table
			if (component_name.equalsIgnoreCase("cc430")) { component_id = ComponentId.DPP_COMPONENT_ID_CC430; }
			else if (component_name.equalsIgnoreCase("wgps2")) { component_id = ComponentId.DPP_COMPONENT_ID_WGPS2; }
			else if (component_name.equalsIgnoreCase("geophone")) { component_id = ComponentId.DPP_COMPONENT_ID_GEOPHONE; }
			else if (component_name.equalsIgnoreCase("devboard")) { component_id = ComponentId.DPP_COMPONENT_ID_DEVBOARD; }
			else if (component_name.equalsIgnoreCase("sx1262")) { component_id = ComponentId.DPP_COMPONENT_ID_SX1262; }
			else if (component_name.equalsIgnoreCase("geo3x")) { component_id = ComponentId.DPP_COMPONENT_ID_GEO3X; }
			else if (component_name.equalsIgnoreCase("geomini")) { component_id = ComponentId.DPP_COMPONENT_ID_GEOMINI; }
			else if (component_name.equalsIgnoreCase("baseboard")) { component_id = ComponentId.DPP_COMPONENT_ID_BASEBOARD; }
			else {
				logger.warn("ihex_file filename has to start with component_id (cc430, geophone, wgps, devboard, sx1262, geo3x, geomini or baseboard) followed by an underline ([COMPONENTNAME]_....hex)");
				return new InputInfo(getActiveAddressBean().toString(), "ihex_file filename has to start with component_id (cc430, geophone, wgps, devboard, sx1262, geo3x, geomini or baseboard) followed by an underline ([COMPONENTNAME]_....hex)", false);
			}
			
			// if CC430 or sx1262: target_id has to be provided
			if (component_id == ComponentId.DPP_COMPONENT_ID_CC430 || component_id == ComponentId.DPP_COMPONENT_ID_SX1262) {
				if (filename_split.length < 5) {
					logger.warn("ihex_file filename has to contain at least four underlines ([COMPONENTNAME]_[PROGRAMNAME]_v[VERSION]_[YYYYMMDD]_id[DEVICEID].hex)");
					return new InputInfo(getActiveAddressBean().toString(), "ihex_file filename has to contain at least four underlines ([COMPONENTNAME]_[PROGRAMNAME]_v[VERSION]_[YYYYMMDD]_id[DEVICEID].hex)", false);
				}
				try { target_id = Integer.parseInt(filename_split[4].substring(2)); }
				catch (Exception e) {
					logger.warn("ihex_file filename has to contain the device_id (int) after the fourth underline ([COMPONENTNAME]_[PROGRAMNAME]_v[VERSION]_[YYYYMMDD]_id[DEVICEID].hex)");
					return new InputInfo(getActiveAddressBean().toString(), "ihex_file filename has to contain the device_id (int) after the fourth underline ([COMPONENTNAME]_[PROGRAMNAME]_v[VERSION]_[YYYYMMDD]_id[DEVICEID].hex)", false);
				}
			}
			else {
				if (filename_split.length < 4) {
					logger.warn("ihex_file filename has to contain at least three underlines ([COMPONENTNAME]_[PROGRAMNAME]_v[VERSION]_[YYYYMMDD].hex)");
					return new InputInfo(getActiveAddressBean().toString(), "ihex_file filename has to contain at least three underlines ([COMPONENTNAME]_[PROGRAMNAME]_v[VERSION]_[YYYYMMDD].hex)", false);
				}
			}

			try { version = Integer.parseInt(filename_split[2].substring(1)); }
			catch (Exception e) {
				if (component_id == ComponentId.DPP_COMPONENT_ID_CC430 || component_id == ComponentId.DPP_COMPONENT_ID_SX1262) {
					logger.warn("ihex_file filename has to contain the version number (int) after the second underline ([COMPONENTNAME]_[PROGRAMNAME]_v[VERSION]_[YYYYMMDD]_id[DEVICEID].hex)");
					return new InputInfo(getActiveAddressBean().toString(), "ihex_file filename has to contain the version number (int) after the second underline ([COMPONENTNAME]_[PROGRAMNAME]_v[VERSION]_[YYYYMMDD]_id[DEVICEID].hex)", false);
				}
				else {
					logger.warn("ihex_file filename has to contain the version number (int) after the second underline ([COMPONENTNAME]_[PROGRAMNAME]_v[VERSION]_[YYYYMMDD].hex)");
					return new InputInfo(getActiveAddressBean().toString(), "ihex_file filename has to contain the version number (int) after the second underline ([COMPONENTNAME]_[PROGRAMNAME]_v[VERSION]_[YYYYMMDD].hex)", false);
				}
			}
			
			try {
				if (!sendRemote(System.currentTimeMillis(), new Serializable [] {FW_PKT_TYPE_DATA, component_id, version, target_id, ihex_binary}, super.priority)) {
					dataProcessed(time, new Serializable[] {time, time, time*1000L, getDeviceID(), null, component_id, null, version, FW_PKT_TYPE_DATA, "no connection to the CoreStation: could not upload DPP firmware data -> try again later", ihex_binary});
					logger.warn("no connection to the CoreStation: could not upload DPP firmware data -> try again later");
					return new InputInfo(getActiveAddressBean().toString(), "no connection to the CoreStation: could not upload DPP firmware data -> try again later", false);
				}
				else
					return new InputInfo(getActiveAddressBean().toString(), "DPP firmware uploaded", true);
			} catch (IOException e) {
				dataProcessed(time, new Serializable[] {time, time, time*1000L, getDeviceID(), null, component_id, null, version, FW_PKT_TYPE_DATA, e.getMessage() + ": could not upload DPP firmware data -> try again later", ihex_binary});
				logger.warn(e.getMessage() + ": could not upload DPP firmware data -> try again later");
				return new InputInfo(getActiveAddressBean().toString(), e.getMessage() + ": could not upload DPP firmware data -> try again later", false);
			}
		}
		else if(action.compareToIgnoreCase("firmware_update") == 0) {
			for (int i = 0 ; i < paramNames.length ; i++) {
				if( paramNames[i].compareToIgnoreCase("target_id") == 0 )
					target_id = new Integer((String)paramValues[i]);
				else if( paramNames[i].compareToIgnoreCase("component_id") == 0 )
					component_id = new Integer((String)paramValues[i]);
			}
			if (target_id == null) {
				logger.warn("target_id is missing: could not upload DPP firmware update command");
				return new InputInfo(getActiveAddressBean().toString(), "target_id is missing: could not upload DPP firmware update command", false);
			}
			if (component_id == null) {
				logger.warn("component_id is missing: could not upload DPP firmware update command");
				return new InputInfo(getActiveAddressBean().toString(), "component_id is missing: could not upload DPP firmware update command", false);
			}
			
			try {
				if (!sendRemote(System.currentTimeMillis(), new Serializable [] {FW_PKT_TYPE_UPDATE, component_id, version, target_id}, super.priority)) {
					dataProcessed(time, new Serializable[] {time, time, time*1000L, getDeviceID(), target_id, component_id, null, 0, FW_PKT_TYPE_UPDATE, "no connection to the CoreStation: could not upload DPP firmware update command -> try again later", null});
					logger.warn("no connection to the CoreStation: could not upload DPP firmware update command -> try again later");
					return new InputInfo(getActiveAddressBean().toString(), "no connection to the CoreStation: could not upload DPP firmware update command -> try again later", false);
				}
				else
					return new InputInfo(getActiveAddressBean().toString(), "DPP firmware uploaded", true);
			} catch (IOException e) {
				dataProcessed(time, new Serializable[] {time, time, time*1000L, getDeviceID(), target_id, component_id, null, 0, FW_PKT_TYPE_UPDATE, e.getMessage() + ": could not upload DPP firmware update command -> try again later", null});
				logger.warn(e.getMessage() + ": could not upload DPP firmware update command -> try again later");
				return new InputInfo(getActiveAddressBean().toString(), e.getMessage() + ": could not upload DPP firmware update command -> try again later", false);
			}
		}
		else {
			logger.warn("action >" + action + "< not supported");
			return new InputInfo(getActiveAddressBean().toString(), "action >" + action + "< not supported", false);
		}
	}

	@Override
	public short getMessageType() {
		return ch.epfl.gsn.wrappers.backlog.BackLogMessage.DPP_FIRMWARE_TYPE;
	}

	@Override
	public DataField[] getOutputFormat() {
		return dataField;
	}

	@Override
	public String getPluginName() {
        return "DPPFirmwarePlugin";
	}

}
