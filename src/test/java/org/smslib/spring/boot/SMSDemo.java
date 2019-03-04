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

import org.smslib.Message.MessageEncodings;
import org.smslib.OutboundMessage;
import org.smslib.Service;
import org.smslib.modem.SerialModemGateway;
 
public class SMSDemo {
 
    public static void main(String args[]) throws Exception {
 
        // ---------------创建串口设备，如果有多个，就创建多个--------------
        // 1、你自己连接网关的id
        // 2、com口名称，如COM1或/dev/ttyS1（根据实际情况修改）
        // 3、串口波特率，如9600（根据实际情况修改）
        // 4、开发商，如Apple
        // 5、型号，如iphone4s
        SerialModemGateway gateway = new SerialModemGateway("SMS" , "COM3",
                9600, "", "");
 
        gateway.setInbound( true); // 设置true，表示该网关可以接收短信
        gateway.setOutbound( true); // 设置true，表示该网关可以发送短信
 
        // -----------------创建发送短信的服务（它是单例的）----------------
        Service service = Service. getInstance();
 
        // ---------------------- 将设备加到服务中----------------------
        service.addGateway(gateway);
 
        // ------------------------- 启动服务 -------------------------
        service.startService();
 
        // ------------------------- 发送短信 -------------------------
        OutboundMessage msg = new OutboundMessage("187xxxxxxxx" , "Hello World");
        msg.setEncoding(MessageEncodings. ENCUCS2);
 
        service.sendMessage(msg);
 
        // ------------------------- 关闭服务 -------------------------
        service.stopService();
    }
 
}