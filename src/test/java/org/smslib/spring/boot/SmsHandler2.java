/*
 * Copyright (c) 2018, vindell (https://github.com/vindell).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.smslib.spring.boot;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.smslib.AGateway;
import org.smslib.AGateway.Protocols;
import org.smslib.GatewayException;
import org.smslib.InboundMessage;
import org.smslib.Message;
import org.smslib.Message.MessageEncodings;
import org.smslib.OutboundMessage;
import org.smslib.Service;
import org.smslib.modem.SerialModemGateway;

/**
 * lxt
 * 
 * @author
 */
public class SmsHandler2 {
	private static final Logger logger = LoggerFactory.getLogger(SmsHandler.class);
	public final static String MSG_FILE_PATH = "server.properties";
	private static final SmsHandler2 instance = new SmsHandler2();

	private SmsHandler2() {
	};

	public static SmsHandler2 getInstance() {
		return instance;
	}

	private String id;
	private String comPort;
	private String baudRate;
	private String manufacturer;
	private String model;
	private String simPin = "0000";
	private Service smsService;
	private List<AGateway> agatewayList = new ArrayList<AGateway>();

	/**
	 * 初始化端口配置信息
	 */
	public void init() {

		try {
			smsService = Service.getInstance();

			Properties properties = new Properties();
			InputStream instream = ClassLoader.getSystemResourceAsStream(MSG_FILE_PATH);
			properties.load(instream);
			id = properties.getProperty("send.gatewayId");
			comPort = properties.getProperty("send.comPort");
			baudRate = properties.getProperty("send.baudRate");
			manufacturer = properties.getProperty("modem.manufacturer");
			model = properties.getProperty("modem.model") == null ? "" : properties.getProperty("modem.model");
			simPin = properties.getProperty("modem.simPin");

			System.out.println("..............init services...params.............");
			System.out.println("id:" + id);
			System.out.println("comPort:" + comPort);
			System.out.println("baudRate:" + baudRate);
			System.out.println("manufacturer:" + manufacturer);
			System.out.println("model:" + model);
			// String[] ids=id.split(",");
			String[] comPorts = comPort.split(",");
			String[] baudRates = baudRate.split(",");
			// String[] manufacturers=manufacturer.split(",");
			// String[] models=model.split(",");
			for (int i = 0; i < comPorts.length; i++) {
				int baudrate = Integer.parseInt(baudRates[i]);
				SerialModemGateway gateway = new SerialModemGateway(id, comPorts[i], baudrate, manufacturer, "");

				gateway.setInbound(true);
				gateway.setOutbound(true);
				gateway.setProtocol(Protocols.PDU);
				gateway.setSimPin(simPin);
				agatewayList.add(gateway);
			}

			try {
				for (AGateway gatewayTmp : agatewayList) {
					smsService.addGateway(gatewayTmp);
				}
			} catch (GatewayException ex) {
				logger.error("addGateWay失败！" + ex.getMessage());
			}

		} catch (Exception e) {
			logger.error(" 初始化端口配置信息失败！" + e.getMessage());
		}
	}

	/**
	 * start service启动服务
	 */
	public void start() {
		logger.info("SMS service start.....");
		try {
			// smsService.getInstance().S.SERIAL_POLLING=true; //启用轮循模式
			smsService.startService();
			logger.info("SMS service start sucess");
		} catch (Exception ex) {
			logger.error("SMS service start error:", ex);
		}

	}

	public synchronized void sycInit() {
		if (isStarted()) {
			return;
		}
		init();
		start();
	}

	/**
	 * 注销服务
	 */
	public synchronized void destroy() {
		try {
			smsService.stopService();
			for (AGateway gateway : agatewayList) {
				System.out.println(".................gateway.getStatus():" + gateway.getStatus());
				smsService.removeGateway(gateway);
			}
			agatewayList.clear();
			System.out.println(".........agatewayList.size()" + agatewayList.size() + "............");
			logger.info("SMS service stop sucess");
		} catch (Exception ex) {
			logger.error("SMS service stop error:", ex);
		}
	}

	/**
	 * send SMS 发送短信
	 * 
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

	/**
	 * exict service is start检测服务是否启动
	 * 
	 * @return
	 */
	public synchronized boolean isStarted() {
		if (Service.getInstance().getServiceStatus() == Service.ServiceStatus.STARTED) {
			if (SmsHandler2.getInstance() == null)
				return false;
			for (AGateway gateway : smsService.getGateways()) {
				if (gateway.getStatus() == AGateway.GatewayStatuses.STARTED) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * read SMS 接收短信
	 * 
	 * @return List
	 */
	public List<InboundMessage> readSMS() {
		List<InboundMessage> msgList = new LinkedList<InboundMessage>();
		if (!isStarted()) {
			return msgList;
		}
		try {
			this.smsService.readMessages(msgList, InboundMessage.MessageClasses.ALL);
			logger.info("read SMS size: " + msgList.size());
		} catch (Exception e) {
			logger.error("read error:", e);
		}
		return msgList;
	}

	public synchronized boolean send(String mobile, String content) {
		try {
			// sycInit();
			OutboundMessage msg = new OutboundMessage(mobile, content);
			msg.setEncoding(MessageEncodings.ENCUCS2);
			boolean result = Service.getInstance().sendMessage(msg);
			System.out.println(msg);
			return result;
		} catch (Exception e) {
			logger.error("send error:", e);
		}
		return false;
	}

	public synchronized List<Message> read() {
		List<Message> resultList = new ArrayList<Message>();
		List<InboundMessage> msgList = new LinkedList<InboundMessage>();
		if (!isStarted()) {
			return resultList;
		}
		try {
			Service.getInstance().readMessages(msgList, InboundMessage.MessageClasses.ALL);
			logger.info("read SMS size: " + msgList.size());
			Message message = null;
			for (InboundMessage msg : msgList) {
				System.out.println(msg);
				message = new Message();
				if (msg.getDate() == null) {
					msg.setDate(new Date());
				}
				BeanUtils.copyProperties(message, msg);
				Service.getInstance().deleteMessage(msg);
				resultList.add(message);
			}
		} catch (Exception e) {
			logger.error("read error:", e);
		}
		return resultList;
	}

	/**
	 * 测试
	 * 
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException {
		for (int i = 0; i < 3; i++) {
			Logger.getRootLogger().setLevel(Level.INFO);
			// OutboundMessage outMsg = new OutboundMessage("+8615011172844" ,
			// "正在测试短信多条发送的稳定性。");
			SmsHandler.getInstance().sycInit();

			// 发送短信
			// System.out.println("result:" + smsHandler.sendSMS(outMsg));;
			// System.out.println("result:" + smsHandler.send("447711383249" ,
			// "正在测试短信多条发送的稳定性。"));;
			// 读取短信
			List<Message> readList = SmsHandler.getInstance().read();
			// List<InboundMessage> readList = smsHandler.readMSM();

			for (Message in : readList) {
				System.out.println("发信人：" + in.getOriginator() + " 短信内容:" + in.getText());
			}
			SmsHandler.getInstance().destroy();
		}

	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getComPort() {
		return comPort;
	}

	public void setComPort(String comPort) {
		this.comPort = comPort;
	}

	public String getBaudRate() {
		return baudRate;
	}

	public void setBaudRate(String baudRate) {
		this.baudRate = baudRate;
	}

	public String getManufacturer() {
		return manufacturer;
	}

	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getSimPin() {
		return simPin;
	}

	public void setSimPin(String simPin) {
		this.simPin = simPin;
	}

}