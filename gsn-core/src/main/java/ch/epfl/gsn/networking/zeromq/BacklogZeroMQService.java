/**
* 
* @author Davide De Sclavis
* @author Manuel Buchauer
* @author Jan Beutel
*
*/

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
import ch.epfl.gsn.VirtualSensorInitializationFailedException;
import ch.epfl.gsn.vsensor.AbstractVirtualSensor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BacklogZeroMQService  {
    private static transient Logger logger = LoggerFactory.getLogger(BacklogZeroMQService.class);
    private Kryo kryo = new Kryo();
	private ZMQ.Socket receiver = null;
	private int port = 0;

    private Thread workerThread;
    private volatile boolean running = true;


    public BacklogZeroMQService(final int port) {
        this.port = port;
        kryo.register(CommandFile.class);
        kryo.register(CommandFile[].class);
        kryo.register(UploadCommandData.class);

        workerThread = new Thread(new Runnable() {

			@Override
			public void run() {
				while (running) {
                    try {
                        handleRequest();
                    } catch (IllegalStateException z) {
                        logger.error("Backlog ZMQ error (re-init socket): ", z);
                        try {
                            receiver.send(new byte[] { (byte) 1 });
                        } catch (Exception e) {
                            logger.error("BacklogZMQ error in send: ", e);
                        } finally {
                            cleanUp();
                            initSocket();
                        }
                    } catch (Exception e) {
                        logger.error("BacklogZMQ error ", e);
                    }
                }
			}
		});
    }

    public void start() {
        if (running) return;

        running = true;
        initSocket();
        workerThread.start();
    }

    public void close() {
        running = false;
        try {
            workerThread.interrupt();
        } catch (Exception e) {
            logger.error("BacklogZMQ error in interrupt: ", e);
        }
        cleanUp();
    }

    private void handleRequest() throws VirtualSensorInitializationFailedException {
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
    }
    
    private void initSocket() {
        ZContext ctx = Main.getZmqContext();
        receiver = ctx.createSocket(ZMQ.REP);
        receiver.bind("tcp://*:" + port);
        receiver.setReceiveTimeOut(10000);
    }

    private void cleanUp() {
        if (receiver != null) {
            receiver.close();
        }
    }



}





