package ch.epfl.gsn.networking.zeromq;

import org.zeromq.ZMQ;
import org.zeromq.ZContext;

import java.io.ByteArrayInputStream;
import java.io.Serializable;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import ch.epfl.gsn.Main;
import ch.epfl.gsn.Mappings;
import ch.epfl.gsn.vsensor.AbstractVirtualSensor;
import scala.annotation.meta.companionObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



public class BacklogZeroMQ extends Thread implements Runnable {
    private static transient Logger logger = LoggerFactory.getLogger(BacklogZeroMQ.class);
    private Kryo kryo = new Kryo();
	private ZMQ.Socket receiver = null;
	private int lport = 0;
	private String laddress;


    public BacklogZeroMQ(final int port) {
        kryo.register(CommandFile.class);
        kryo.register(CommandFile[].class);
        kryo.register(UploadCommandData.class);
        ZContext ctx = Main.getZmqContext();
		receiver = ctx.createSocket(ZMQ.REP);
		receiver.bind("tcp://*:" + port);
		receiver.setReceiveTimeOut(10000);
        logger.error("createdbacklogzeromq");
        Thread backlogproxy = new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
                    try {
                        byte[] rec = receiver.recv();
                        if (rec != null) {
                            ByteArrayInputStream bais = new ByteArrayInputStream(rec);
                            UploadCommandData data = kryo.readObjectOrNull(new Input(bais), UploadCommandData.class);
                           
                            List<String> paramNames = new ArrayList<>(Arrays.asList(data.getParamNames()));
                            List<Serializable> paramValues = new ArrayList<>();

                            // Add elements from the String array to the Serializable list
                            for (String value : data.getParamValues()) {
                                paramValues.add(value);
                            }

                            String vsname= data.getVsname().toLowerCase();
                            String cmd = data.getCmd();
                            boolean success = false;

                            CommandFile[]files = data.getCommandFiles();
                            for(CommandFile file : files){
                                paramNames.add(file.getFileKey());
                                paramValues.add(file.getFileItem());
                            }

                            AbstractVirtualSensor vs= Mappings.getVSensorInstanceByVSName(vsname).borrowVS();
                            success = vs.dataFromWeb( cmd , paramNames.toArray(new String[0]), paramValues.toArray(new Serializable[0]));
					        receiver.send(success ? new byte[] { (byte) 0 } : new byte[] { (byte) 1 });
                        }
                    } catch (IllegalStateException z) {
                        logger.error("Backlog ZMQ error (re-init socket): ", z);
                        try {
                            receiver.send(new byte[] { (byte) 1 });
                        } catch (Exception e) {
                            logger.error("BacklogZMQ error in send: ", e);
                        } finally {
                            receiver.close();
                            ZContext ctx = Main.getZmqContext();
                            receiver = ctx.createSocket(ZMQ.REP);
                            receiver.bind("tcp://*:" + port);
                            receiver.setReceiveTimeOut(10000);
                        }
                    } catch (Exception e) {
                        logger.error("BacklogZMQ error ", e);
                    }
                }
			}
		});

        backlogproxy.start();
    }


}





