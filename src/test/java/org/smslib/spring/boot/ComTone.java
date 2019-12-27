/*
 * Copyright (c) 2018, hiwepy (https://github.com/hiwepy).
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
import java.io.OutputStream;
import java.util.Enumeration;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

/**
 * CommTest 检测端口
 */
public class ComTone {
	
	/** 常见端口波特率 */
	public static int bauds[] = { 9600, 19200, 57600, 115200 };

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {

		Enumeration<CommPortIdentifier> portList = CommPortIdentifier.getPortIdentifiers();

		long st = System.currentTimeMillis();

		System.out.println("短信设备端口连接测试...");

		while (portList.hasMoreElements()) {

			CommPortIdentifier portId = (CommPortIdentifier) portList.nextElement();

			if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				System.out.println("找到串口：" + portId.getName() + "\t类型：" + getPortTypeName(portId.getPortType()));

				for (int i = 0; i < bauds.length; i++) {
					System.out.print("  Trying at " + bauds[i] + "...");
					try {
						SerialPort serialPort;
						InputStream inStream;
						OutputStream outStream;
						int c;
						String response;
						serialPort = (SerialPort) portId.open("SMSLibCommTester", 1000);
						serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN);
						serialPort.setSerialPortParams(bauds[i], SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
								SerialPort.PARITY_NONE);
						inStream = serialPort.getInputStream();
						outStream = serialPort.getOutputStream();
						serialPort.enableReceiveTimeout(1000);
						c = inStream.read();
						while (c != -1)
							c = inStream.read();
						outStream.write('A');
						outStream.write('T');
						outStream.write('\r');
						try {
							Thread.sleep(1000);
						} catch (Exception e) {
						}
						response = "";
						c = inStream.read();
						while (c != -1) {
							response += (char) c;
							c = inStream.read();
						}
						if (response.indexOf("OK") >= 0) {
							try {
								System.out.print("  获取设备信息...");
								outStream.write('A');
								outStream.write('T');
								outStream.write('+');
								outStream.write('C');
								outStream.write('G');
								outStream.write('M');
								outStream.write('M');
								outStream.write('\r');
								response = "";
								c = inStream.read();
								while (c != -1) {
									response += (char) c;
									c = inStream.read();
								}
								System.out.println("  发现设备: " + response.replaceAll("\\s+OK\\s+", "")
										.replaceAll("\n", "").replaceAll("\r", ""));
							} catch (Exception e) {
								System.out.println("  没有发现设备!");
							}
						} else
							System.out.println("  没有发现设备!");
						serialPort.close();
					} catch (Exception e) {
						System.out.println("  没有发现设备!");
					}
				}
				System.out.println("找到串口: " + portId.getName());
			}
		}
		long et = System.currentTimeMillis();
		long time = et - st;
		System.out.println("系统的试运行时间:" + time + "毫秒");
	}

	private static String getPortTypeName(int portType) {
		switch (portType) {
		case CommPortIdentifier.PORT_I2C:
			return "I2C";
		case CommPortIdentifier.PORT_PARALLEL:
			return "Parallel";
		case CommPortIdentifier.PORT_RAW:
			return "Raw";
		case CommPortIdentifier.PORT_RS485:
			return "RS485";
		case CommPortIdentifier.PORT_SERIAL:
			return "Serial";
		default:
			return "unknown type";
		}
	}
}