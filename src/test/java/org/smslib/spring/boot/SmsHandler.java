package org.smslib.spring.boot;

 
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smslib.AGateway;
import org.smslib.AGateway.Protocols;
import org.smslib.GatewayException;
import org.smslib.InboundMessage;
import org.smslib.Message.MessageEncodings;
import org.smslib.OutboundMessage;
import org.smslib.Service;
import org.smslib.modem.SerialModemGateway;
  
/** 
 * @author michael 
 *  
 */  
public class SmsHandler {  
	
    protected static Logger logger = LoggerFactory.getLogger(SmsHandler.class);  
    protected Service smsService;  
	  
    /** 
     *  
     */  
    public SmsHandler() {  
        smsService = Service.getInstance();  
        List<AGateway> agatewayList = new ArrayList<AGateway>();  
  
        String portName = "COM24";//"/dev/ttyUSB0";// COM24  
        SerialModemGateway gateway = new SerialModemGateway( "modem." + portName, portName, 9600, "wavecom", "PL2303");  
        gateway.setInbound(true);  
        gateway.setOutbound(true);  
        gateway.setProtocol(Protocols.PDU);  
        gateway.setSimPin("0000");  
        agatewayList.add(gateway);  
        try {  
            for (AGateway gatewayTmp : agatewayList) {  
                smsService.addGateway(gatewayTmp);  
            }  
        } catch (GatewayException ex) {  
            logger.error(ex.getMessage());  
        }  
    }  
  
    /** 
     *  
     */  
    public void start() {  
        logger.info("SMS service start.....");  
        try {  
            smsService.startService();  
        } catch (Exception ex) {  
            logger.error("SMS service start error:", ex);  
        }  
    }  
  
    /** 
     *  
     */  
    public void destroy() {  
        try {  
            smsService.stopService();  
        } catch (Exception ex) {  
            logger.error("SMS service stop error:", ex);  
        }  
        logger.info("SMS service stop");  
    }  
  
    /** 
     * send SMS 
     * @param msg 
     * @return Boolean 
     */  
    public Boolean sendSMS(OutboundMessage msg) {  
        try {  
            msg.setEncoding(MessageEncodings.ENCUCS2);  
            return smsService.sendMessage(msg);  
        } catch (Exception e) {  
            logger.error("send error:", e);  
        }  
        return false;  
    }  
  
    private boolean isStarted() {  
        if (smsService.getServiceStatus() == Service.ServiceStatus.STARTED) {  
            for (AGateway gateway : smsService.getGateways()) {  
                if (gateway.getStatus() == AGateway.GatewayStatuses.STARTED) {  
                    return true;  
                }  
            }  
        }  
        return false;  
    }  
  
    /** 
     * read SMS 
     * @return List 
     */  
    public List<InboundMessage> readSMS() {  
        List<InboundMessage> msgList = new LinkedList<InboundMessage>();  
        if (!isStarted()) {  
            return msgList;  
        }  
        try {  
            this.smsService.readMessages(msgList,  
                    InboundMessage.MessageClasses.ALL);  
            logger.info("read SMS size: " + msgList.size());  
        } catch (Exception e) {  
            logger.error("read error:", e);  
        }  
        return msgList;  
    }  
  
    /** 
     * @param args 
     */  
    public static void main(String[] args) {  
        OutboundMessage outMsg = new OutboundMessage("189xxxx****", "信息测试");  
        SmsHandler smsHandler = new SmsHandler();  
        smsHandler.start();  
        //发送短信  
        smsHandler.sendSMS(outMsg);  
        //读取短信  
        List<InboundMessage> readList = smsHandler.readSMS();  
        for (InboundMessage in : readList) {  
            System.out.println("发信人：" + in.getOriginator() + " 短信内容:"  
                    + in.getText());  
        }  
        smsHandler.destroy();  
        System.out.println("-----------");  
    } 
}
