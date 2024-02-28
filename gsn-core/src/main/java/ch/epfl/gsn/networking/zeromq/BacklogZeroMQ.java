package ch.epfl.gsn.networking.zeromq;

import org.zeromq.ZMQ;
import org.zeromq.ZContext;

import java.io.ByteArrayInputStream;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import ch.epfl.gsn.Main;
import ch.epfl.gsn.Mappings;
import ch.epfl.gsn.vsensor.AbstractVirtualSensor;
import scala.annotation.meta.companionObject;



public class BacklogZeroMQ extends Thread implements Runnable {
    private static transient Logger logger = LoggerFactory.getLogger(BacklogZeroMQ.class);
    private Kryo kryo = new Kryo();
	private ZMQ.Socket receiver = null;
	private int lport = 0;
	private String laddress;


    public BacklogZeroMQ(final int port) {
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
                            String[] paramNames= data.getParamNames();
                            String[] paramValues= data.getParamValues();
                            String vsname= data.getVsname().toLowerCase();
                            String cmd = "";
                            boolean success = false;
                            AbstractVirtualSensor vs= Mappings.getVSensorInstanceByVSName(vsname).borrowVS();
                            for(int i=0;i<paramNames.length;i++) {
                                if(paramNames[i].equals("cmd")) {
                                    cmd = paramValues[i];
                                }
                            }
                            success = vs.dataFromWeb( cmd , paramNames, paramValues);
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





