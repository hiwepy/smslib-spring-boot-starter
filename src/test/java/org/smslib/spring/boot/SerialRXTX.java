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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.HashSet;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

/*
 * http://www.cnblogs.com/sowhat4999/p/4575696.html
 */
public class SerialRXTX {
    /**
     * This code snippet shows how to retrieve the available comms ports on your
     * computer. A CommPort is available if it is not being used by another
     * application.
     */
    public static void listAvailablePorts() {
        HashSet<CommPortIdentifier> portSet = getAvailableSerialPorts();
        String[] serialPort = new String[portSet.size()];
        int i = 0;
        for (CommPortIdentifier comm : portSet) {
            serialPort[i] = comm.getName();
            System.out.println(serialPort[i]);
            i++;
        }
    }

    public static String getPortTypeName(int portType) {
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

    /**
     * @return A HashSet containing the CommPortIdentifier for all serial ports
     *         that are not currently being used.
     */
    public static HashSet<CommPortIdentifier> getAvailableSerialPorts() {
        HashSet<CommPortIdentifier> h = new HashSet<CommPortIdentifier>();
        @SuppressWarnings("rawtypes")
        Enumeration thePorts = CommPortIdentifier.getPortIdentifiers();// 可以找到系统的所有的串口，每个串口对应一个CommPortldentifier
        while (thePorts.hasMoreElements()) {
            CommPortIdentifier com = (CommPortIdentifier) thePorts
                    .nextElement();
            switch (com.getPortType()) {
            case CommPortIdentifier.PORT_SERIAL:// type of the port is serial
                try {
                    CommPort thePort = com.open("CommUtil", 50);// open the serialPort
                    thePort.close();
                    h.add(com);
                } catch (PortInUseException e) {
                    System.out.println("Port, " + com.getName()
                            + ", is in use.");
                } catch (Exception e) {
                    System.err.println("Failed to open port " + com.getName());
                    e.printStackTrace();
                }
            }
        }
        return h;
    }

    public static SerialPort connect(String portName) throws Exception {
        SerialPort serialPort = null;
        CommPortIdentifier portIdentifier = CommPortIdentifier
                .getPortIdentifier(portName);// initializes of port operation
        if (portIdentifier.isCurrentlyOwned()) {
            System.out.println("Error: Port is currently in use");
        } else {
            CommPort commPort = portIdentifier.open(portName, 2000);// the delay
                                                                    // time of
                                                                    // opening
                                                                    // port
            if (commPort instanceof SerialPort) {
                serialPort = (SerialPort) commPort;
                serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8,
                        SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);// serial
                                                                        // communication
                                                                        // parameters
                                                                        // setting
                InputStream inputStream = serialPort.getInputStream();
                // OutputStream outputStream = serialPort.getOutputStream();
                // (new Thread(new SerialWriter(outputStream))).start();
                serialPort.addEventListener(new SerialReader(inputStream));
                serialPort.notifyOnDataAvailable(true);
            }
        }
        return serialPort;

    }

    /**
     * not necessary to send command in new thread, but the serialPort only has
     * one instance
     * 
     * @param serialPort
     * @param string
     */
    public static void sendMessage(SerialPort serialPort, String string) {
        try {
            OutputStream outputStream = serialPort.getOutputStream();
            (new Thread(new SerialWriter(outputStream, string))).start();// send
                                                                            // command
                                                                            // in
                                                                            // the
                                                                            // new
                                                                            // thread
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Handles the input coming from the serial port. A new line character is
     * treated as the end of a block in this example.
     */
    public static class SerialReader implements SerialPortEventListener {
        private InputStream in;

        public SerialReader(InputStream in) {
            this.in = in;
        }

        public void serialEvent(SerialPortEvent arg0) {
            byte[] buffer = new byte[1024];
            try {
                Thread.sleep(500);// the thread need to sleep for completed
                                    // receive the data
                if (in.available() > 0) {
                    in.read(buffer);
                }
                System.out.println(new String(buffer));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    /** */
    public static class SerialWriter implements Runnable {
        OutputStream out;
        String commandString;

        public SerialWriter(OutputStream out, String commandString) {
            this.out = out;
            this.commandString = commandString;
        }

        public void run() {
            while (true) {
                try {
                    Thread.sleep(3000);// an interval of 3 seconds to sending
                                        // data
                    out.write(commandString.getBytes());
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (InterruptedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
    	
    	System.out.println(System.getProperty("java.library.path"));;
    	
        listAvailablePorts();
        try {
            sendMessage(connect("/dev/ttyUSB0"), "P");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    /*
     * https://www.cnblogs.com/minteliu/p/5829061.html
     */
    private static void addLibraryDir(String libraryPath) throws Exception {
        Field userPathsField = ClassLoader.class.getDeclaredField("usr_paths");
        userPathsField.setAccessible(true);
        String[] paths = (String[]) userPathsField.get(null);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < paths.length; i++) {
            if (libraryPath.equals(paths[i])) {
                continue;
            }
            sb.append(paths[i]).append(';');
        }
        sb.append(libraryPath);
        System.setProperty("java.library.path", sb.toString());
        final Field sysPathsField = ClassLoader.class.getDeclaredField("sys_paths");
        sysPathsField.setAccessible(true);
        sysPathsField.set(null, null);
    }
}