package org.smslib.spring.boot;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

import org.smslib.helper.CommPortIdentifier;
import org.smslib.helper.SerialPort;

/**
 * @author michael
 * 
 */
public class TestCommPort {
	static CommPortIdentifier portId;
	static Enumeration portList;
	static int bauds[] = { 9600, 19200, 57600, 115200 };

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		portList = CommPortIdentifier.getPortIdentifiers();
		System.out.println("GSM Modem 串行端口连接测试开始...");
		String portName = "COM24";
		while (portList.hasMoreElements()) {
			portId = (CommPortIdentifier) portList.nextElement();
			if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL
					&& portName.equals(portId.getName())) {
				System.out.println("找到串口: " + portId.getName());
				for (int i = 0; i < bauds.length; i++) {
					System.out.print("  Trying at " + bauds[i] + "...");
					try {
						SerialPort serialPort;
						InputStream inStream;
						OutputStream outStream;
						int c;
						StringBuffer response = new StringBuffer();
						serialPort = (SerialPort) portId.open(
								"SMSLibCommTester", 2000);
						serialPort
								.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN);
						serialPort.setSerialPortParams(bauds[i],
								SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
								SerialPort.PARITY_NONE);
						inStream = serialPort.getInputStream();
						outStream = serialPort.getOutputStream();
						serialPort.enableReceiveTimeout(1000);
						c = inStream.read();
						while (c != -1) {
							c = inStream.read();
						}
						outStream.write('A');
						outStream.write('T');
						outStream.write('\r');
						try {
							Thread.sleep(1000);
						} catch (Exception e) {
						}
						c = inStream.read();
						while (c != -1) {
							response.append((char) c);
							c = inStream.read();
						}
						if (response.indexOf("OK") >= 0) {
							System.out.print("  正在检测设备:");
							try {
								outStream.write('A');
								outStream.write('T');
								outStream.write('+');
								outStream.write('C');
								outStream.write('G');
								outStream.write('M');
								outStream.write('M');
								outStream.write('\r');
								response = new StringBuffer();
								c = inStream.read();
								while (c != -1) {
									response.append((char) c);
									c = inStream.read();
								}
								System.out.println("  发现设备: "
										+ response.toString().replaceAll(
												"(\\s+OK\\s+)|[\n\r]", ""));
							} catch (Exception e) {
								System.out.println("  检测设备失败,获取设备信息异常："
										+ e.getMessage());
							}
						} else {
							System.out.println("  检测设备失败，沒有接收到响应结果!");
						}
						serialPort.close();
					} catch (Exception e) {
						System.out.println("  检测设备失败，发生异常：" + e.getMessage());
					}
				}
			}
		}
	}
}