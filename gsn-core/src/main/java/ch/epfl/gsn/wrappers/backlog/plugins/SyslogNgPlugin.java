package ch.epfl.gsn.wrappers.backlog.plugins;

import java.io.Serializable;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import ch.epfl.gsn.beans.DataField;


/**
 * This plugin listens for incoming SyslogNg messages.
 * 
 * @author Tonio Gsell
 */
public class SyslogNgPlugin extends AbstractPlugin {
	
	private static DataField[] dataField = {
			new DataField("TIMESTAMP", "BIGINT"),
			new DataField("GENERATION_TIME", "BIGINT"),
			new DataField("DEVICE_ID", "INTEGER"),

			new DataField("LOG_MESSAGE", "BINARY")};

	private final transient Logger logger = LoggerFactory.getLogger( SyslogNgPlugin.class );

	@Override
	public short getMessageType() {
		return ch.epfl.gsn.wrappers.backlog.BackLogMessage.SYSLOG_NG_MESSAGE_TYPE;
	}

	@Override
	public DataField[] getOutputFormat() {
		return dataField;
	}


	@Override
	public String getPluginName() {
        return "SyslogNgPlugin";
	}

	@Override
	public boolean messageReceived(int deviceId, long timestamp, Serializable[] data) {
		try {
			long generation_time;
			if (data[0] instanceof Double)
				generation_time = (long) ((Double)data[0]*1000);
			else
				generation_time = toLong(data[0])*1000;
			if( dataProcessed(System.currentTimeMillis(), new Serializable[]{timestamp, generation_time, deviceId, ((String)data[1]).getBytes("UTF-8")}) ) {
				ackMessage(timestamp, super.priority);
				return true;
			} else {
				logger.warn("The message with timestamp >" + timestamp + "< could not be stored in the database.");
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return false;
	}
}
