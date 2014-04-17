package jssc;


import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.InputStream;
//import java.io.OutputStream;
import java.util.Enumeration;
import java.util.TooManyListenersException;


public class Connection implements Runnable {

	public static void main(String[] args)
	{
		Runnable runnable = new Connection(args[0]);
		new Thread(runnable).start();
		System.out.println("main finished");
	}

	CommPortIdentifier serialPortId;
	Enumeration enumComm;
	SerialPort serialPort;
	//OutputStream outputStream;
	InputStream inputStream;
	Boolean serialPortOpen = false;

	int baudrate = 9600;
	int dataBits = SerialPort.DATABITS_8;
	int stopBits = SerialPort.STOPBITS_1;
	int parity = SerialPort.PARITY_NONE;
	String portName = "/dev/ttyUSB0";
	

	public Connection(String port)
	{
		this.portName = port;
	}

	public void run()
	{
		if (!openSerialPort(portName))
			return;

		while (true) {
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) {
			} finally {
				if (openSerialPort(portName)) {
					closeSerialPort();
				}
			}
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
			serialPort = (SerialPort) serialPortId.open("Öffnen und Senden", 500);
			serialPort.setRTS(false);
		} catch (PortInUseException e) {
			System.out.println("Port in use");
		}
/*
		try {
			outputStream = serialPort.getOutputStream();
		} catch (IOException e) {
			System.out.println("Keinen Zugriff auf OutputStream");
		}
*/
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

		try {
			serialPort.setSerialPortParams(baudrate, dataBits, stopBits, parity);
		} catch(UnsupportedCommOperationException e) {
			System.out.println("Couldn't set port parameters");
		}

		serialPortOpen = true;
		return true;
	}

	void closeSerialPort()
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
				System.out.println("Receiving: "+ new String(data, 0, num));
			}
		} catch (IOException e) {
			System.out.println("Error while receiving data");
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
				System.out.println("Event BUFFER_EMPTY");
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