package rxtx;


import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
//import java.io.OutputStream;
import java.util.Enumeration;
import java.util.TooManyListenersException;


public class Connection implements Runnable {
	String os ;
	CommPortIdentifier serialPortId;
	Enumeration enumComm;
	SerialPort serialPort;
	static OutputStream outputStream;
	InputStream inputStream;
	Boolean serialPortOpen = false;
	PrintStream ps;
	int baudrate = 38400;
	int dataBits = SerialPort.DATABITS_8;
	int stopBits = SerialPort.STOPBITS_1;
	int parity = SerialPort.PARITY_NONE;
	String portName = "/dev/ttyUSB0";
	 public static final int TIME_OUT = 2000;

	public Connection(String port, PrintStream ps)
	{
		this.portName = port;
		this.ps = ps;
		System.setOut(ps);
		System.setErr(ps);
		
	}

	public void run()
	{
		if (!openSerialPort(portName)){
			return;
		}
		openSerialPort(portName);
		while(true){
			
		}
	}

	boolean openSerialPort(String portName)
	{
		Boolean foundPort = false;
		if (serialPortOpen != false) {
			System.out.println("Serialport already open");
			return false;
		}
		System.out.println("Open serialport using " + portName);
		enumComm = CommPortIdentifier.getPortIdentifiers();
		while(enumComm.hasMoreElements()) {
			serialPortId = (CommPortIdentifier) enumComm.nextElement();
			if (portName.contentEquals(serialPortId.getName())) {
				foundPort = true;
				break;
			}
		}

		if (foundPort != true) {
			System.out.println("Serialport not found: " + portName);
			return false;
		}

		try {
			serialPort = (SerialPort) serialPortId.open("Open and send", TIME_OUT);
			serialPort.setRTS(false);
		} catch (PortInUseException e) {
			System.out.println("Port in use");
		}

		try {
			outputStream = serialPort.getOutputStream();
		} catch (IOException e) {
			System.out.println("Keinen Zugriff auf OutputStream");
		}

		try {
			inputStream = serialPort.getInputStream();
		} catch (IOException e) {
			System.out.println("Cannot access InputStream");
		}

		try {
			serialPort.addEventListener(new serialPortEventListener());
		} catch (TooManyListenersException e) {
			System.out.println("TooManyListenersException");
			
		}

		serialPort.notifyOnDataAvailable(true);
		serialPort.notifyOnOutputEmpty(true);
		try {
			serialPort.setSerialPortParams(baudrate, dataBits, stopBits, parity);
		} catch(UnsupportedCommOperationException e) {
			System.out.println("Couldn't set port parameters");
		}

		serialPortOpen = true;
		return true;
	}

	public void closeSerialPort()
	{
		if (serialPortOpen) {
			System.out.println("Closing Serialport");
			serialPort.close();
			serialPortOpen = false;
		} else {
			System.out.println("Serialport already closed");
		}
	}
	
	void serialPortDataAvailable() {
		try {
			byte[] data = new byte[150];
			int num;
			while(inputStream.available() > 0) {
				num = inputStream.read(data, 0, data.length);
				System.out.println(new String(data, 0, num));
			}
		} catch (IOException e) {
			System.out.println("Error while receiving data");
		}

	}
	
	public static synchronized void writeData(String data) {
		 //System.out.println("Sent: " + data);
		 try {
			 outputStream.write(data.getBytes());
		 } catch (Exception e) {
		 System.out.println("could not write to port");
		 }
		 }

	class serialPortEventListener implements SerialPortEventListener {
		public void serialEvent(SerialPortEvent event) {
			switch (event.getEventType()) {
			case SerialPortEvent.DATA_AVAILABLE:
				serialPortDataAvailable();
				break;
			case SerialPortEvent.BI:
				System.out.println("Event BI");
				break;
			case SerialPortEvent.CD:
				System.out.println("Event CD");
				break;
			case SerialPortEvent.CTS:
				System.out.println("Event CTS");
				break;
			case SerialPortEvent.DSR:
				System.out.println("Event DSR");
				break;
			case SerialPortEvent.FE:
				System.out.println("Event FE");
				break;
			case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
				break;
			case SerialPortEvent.PE:
				System.out.println("Event PE");
				break;
			case SerialPortEvent.RI:
				System.out.println("Event RI");
				break;
			default:
			}
		}
	}	
}