 package org.smslib.spring.boot;
//SendMessage.java - Sample application.
//短信发送测试程序
//This application shows you the basic procedure for sending messages.
//You will find how to send synchronous and asynchronous messages.
//
//For asynchronous dispatch, the example application sets a callback
//notification, to see what's happened with messages.


import org.smslib.AGateway;
import org.smslib.IOutboundMessageNotification;
import org.smslib.Library;
import org.smslib.OutboundMessage;
import org.smslib.Service;
import org.smslib.modem.SerialModemGateway;

public class SendMessage
{
	public void doIt() throws Exception
	{
		OutboundNotification outboundNotification = new OutboundNotification();
		System.out.println("Example: Send message from a serial gsm modem.");
		System.out.println(Library.getLibraryDescription());
		System.out.println("Version: " + Library.getLibraryVersion());
		/*
		modem.com1:网关ID（即短信猫端口编号）
		COM4:串口名称（在window中以COMXX表示端口名称，在linux,unix平台下以ttyS0-N或ttyUSB0-N表示端口名称），通过端口检测程序得到可用的端口
		115200：串口每秒发送数据的bit位数,必须设置正确才可以正常发送短信，可通过程序进行检测。常用的有115200、9600
		Huawei：短信猫生产厂商，不同的短信猫生产厂商smslib所封装的AT指令接口会不一致，必须设置正确.常见的有Huawei、wavecom等厂商
		最后一个参数表示设备的型号，可选
		*/
		SerialModemGateway gateway = new SerialModemGateway("modem.com1", "COM4", 115200, "Huawei", "");
		gateway.setInbound(true);	//设置true，表示该网关可以接收短信,根据需求修改
		gateway.setOutbound(true);//设置true，表示该网关可以发送短信,根据需求修改
		gateway.setSimPin("0000");//sim卡锁，一般默认为0000或1234
		// Explicit SMSC address set is required for some modems.
		// Below is for VODAFONE GREECE - be sure to set your own!
		gateway.setSmscNumber("+306942190000");//短信服务中心号码
		Service.getInstance().setOutboundMessageNotification(outboundNotification);	//发送短信成功后的回调函方法
		Service.getInstance().addGateway(gateway);	//将网关添加到短信猫服务中
		Service.getInstance().startService();	//启动服务，进入短信发送就绪状态
		System.out.println();
		//打印设备信息
		System.out.println("Modem Information:");
		System.out.println("  Manufacturer: " + gateway.getManufacturer());
		System.out.println("  Model: " + gateway.getModel());
		System.out.println("  Serial No: " + gateway.getSerialNo());
		System.out.println("  SIM IMSI: " + gateway.getImsi());
		System.out.println("  Signal Level: " + gateway.getSignalLevel() + " dBm");
		System.out.println("  Battery Level: " + gateway.getBatteryLevel() + "%");
		System.out.println();
		// Send a message synchronously.
		OutboundMessage msg = new OutboundMessage("306974000000", "Hello from SMSLib!");	//参数1：手机号码 参数2：短信内容
		Service.getInstance().sendMessage(msg);	//执行发送短信
		System.out.println(msg);
		// Or, send out a WAP SI message.
		//OutboundWapSIMessage wapMsg = new OutboundWapSIMessage("306974000000",  
//new URL("http://www.smslib.org/"), "Visit SMSLib now!");
		//Service.getInstance().sendMessage(wapMsg);
		//System.out.println(wapMsg);
		// You can also queue some asynchronous messages to see how the callbacks
		// are called...
		//msg = new OutboundMessage("309999999999", "Wrong number!");
		//srv.queueMessage(msg, gateway.getGatewayId());
		//msg = new OutboundMessage("308888888888", "Wrong number!");
		//srv.queueMessage(msg, gateway.getGatewayId());
		System.out.println("Now Sleeping - Hit <enter> to terminate.");
		System.in.read();
		Service.getInstance().stopService();
	}

	/*
	 短信发送成功后，调用该接口。并将发送短信的网关和短信内容对象传给process接口
	*/
	public class OutboundNotification implements IOutboundMessageNotification
	{
		public void process(AGateway gateway, OutboundMessage msg)
		{
			System.out.println("Outbound handler called from Gateway: " + gateway.getGatewayId());
			System.out.println(msg);
		}
	}

	public static void main(String args[])
	{
		SendMessage app = new SendMessage();
		try
		{
			app.doIt();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}