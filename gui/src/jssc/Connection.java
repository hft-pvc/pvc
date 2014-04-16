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

//import OeffnenUndSenden.serialPortEventListener;



public class Connection implements Runnable {

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		Runnable runnable = new Connection(args[0]);
		new Thread(runnable).start();
		System.out.println("main finished");
	}
	
	/**
	 * 
	 */

	CommPortIdentifier serialPortId;
	Enumeration enumComm;
	SerialPort serialPort;
	//OutputStream outputStream;
	InputStream inputStream;
	Boolean serialPortGeoeffnet = false;

	int baudrate = 9600;
	int dataBits = SerialPort.DATABITS_8;
	int stopBits = SerialPort.STOPBITS_1;
	int parity = SerialPort.PARITY_NONE;
	String portName = "/dev/ttyUSB0";
	
	int secondsRuntime = 60;

	public Connection(String port)
	{
		this.portName = port;
		System.out.println("Konstruktor: EinfachSenden");
	}
	
    public void run()
    {
        Integer secondsRemaining = secondsRuntime;
        if (oeffneSerialPort(portName) != true)
        	return;
        
		while (secondsRemaining > 0) {
			System.out.println("Sekunden verbleiben: " + secondsRemaining.toString() );
			secondsRemaining--;
			try {
				Thread.sleep(1000);
			} catch(InterruptedException e) { }
		}
		schliesseSerialPort();
    	
    }
    
	boolean oeffneSerialPort(String portName)
	{
		Boolean foundPort = false;
		if (serialPortGeoeffnet != false) {
			System.out.println("Serialport bereits geöffnet");
			return false;
		}
		System.out.println("Öffne Serialport");
		enumComm = CommPortIdentifier.getPortIdentifiers();
		while(enumComm.hasMoreElements()) {
			serialPortId = (CommPortIdentifier) enumComm.nextElement();
			if (portName.contentEquals(serialPortId.getName())) {
				foundPort = true;
				break;
			}
		}
		if (foundPort != true) {
			System.out.println("Serialport nicht gefunden: " + portName);
			return false;
		}
		try {
			serialPort = (SerialPort) serialPortId.open("Öffnen und Senden", 500);
			serialPort.setRTS(false);
		} catch (PortInUseException e) {
			System.out.println("Port belegt");
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
			System.out.println("Keinen Zugriff auf InputStream");
		}
		try {
			serialPort.addEventListener(new serialPortEventListener());
		} catch (TooManyListenersException e) {
			System.out.println("TooManyListenersException für Serialport");
		}
		serialPort.notifyOnDataAvailable(true);
		try {
			serialPort.setSerialPortParams(baudrate, dataBits, stopBits, parity);
		} catch(UnsupportedCommOperationException e) {
			System.out.println("Konnte Schnittstellen-Paramter nicht setzen");
		}
		
		serialPortGeoeffnet = true;
		return true;
	}

	void schliesseSerialPort()
	{
		if ( serialPortGeoeffnet == true) {
			System.out.println("Schließe Serialport");
			serialPort.close();
			serialPortGeoeffnet = false;
		} else {
			System.out.println("Serialport bereits geschlossen");
		}
	}
	
	void serialPortDatenVerfuegbar() {
		try {
			byte[] data = new byte[150];
			int num;
			while(inputStream.available() > 0) {
				num = inputStream.read(data, 0, data.length);
				System.out.println("Empfange: "+ new String(data, 0, num));
			}
		} catch (IOException e) {
			System.out.println("Fehler beim Lesen empfangener Daten");
		}
	}

	class serialPortEventListener implements SerialPortEventListener {
		public void serialEvent(SerialPortEvent event) {
			System.out.println("serialPortEventlistener");
			switch (event.getEventType()) {
			case SerialPortEvent.DATA_AVAILABLE:
				serialPortDatenVerfuegbar();
				break;
			case SerialPortEvent.BI:
			case SerialPortEvent.CD:
			case SerialPortEvent.CTS:
			case SerialPortEvent.DSR:
			case SerialPortEvent.FE:
			case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
			case SerialPortEvent.PE:
			case SerialPortEvent.RI:
			default:
			}
		}
	}	
}