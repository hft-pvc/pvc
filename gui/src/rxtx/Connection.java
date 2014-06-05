package rxtx;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.TooManyListenersException;

import swing.Draw.Move;

public class Connection {

	String os;
	boolean draw = false;
	private boolean drawNeverCalledBefore = true;
	
	private Enumeration enumComm;
	private CommPortIdentifier serialPortId;
	private SerialPort serialPort;
	private static OutputStream outputStream;
	private InputStream inputStream;
	private Boolean serialPortOpen = false;

	private int baudrate = 38400;
	private int dataBits = SerialPort.DATABITS_8;
	private int stopBits = SerialPort.STOPBITS_1;
	private int parity = SerialPort.PARITY_NONE;
	private String portName = "/dev/ttyUSB0";
	
	public static final int TIME_OUT = 2000;

	private Move curMove = Move.IDLE;

	private static Connection INSTANCE = new Connection();

	public static Connection getInstance() {
		return INSTANCE;
	}

	private Connection() {
	}

	public void init(String port, PrintStream ps) {
		this.portName = port;
		if (!openSerialPort(portName)) {
			return;
		}

		System.setOut(ps);
		System.setErr(ps);
	}

	boolean openSerialPort(String portName) {
		Boolean foundPort = false;
		if (serialPortOpen != false) {
			System.out.println("Serialport already open");
			return false;
		}
		System.out.println("Open serialport using " + portName);
		enumComm = CommPortIdentifier.getPortIdentifiers();
		while (enumComm.hasMoreElements()) {
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
			serialPort = (SerialPort) serialPortId.open("Open and send",
					TIME_OUT);
			serialPort.setRTS(false);

			serialPort.disableReceiveTimeout();
			try {
				serialPort.enableReceiveThreshold(1);
			} catch (UnsupportedCommOperationException e) {
				e.printStackTrace();
			}
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
			serialPort
					.setSerialPortParams(baudrate, dataBits, stopBits, parity);
		} catch (UnsupportedCommOperationException e) {
			System.out.println("Couldn't set port parameters");
		}

		serialPortOpen = true;
		return true;
	}

	public void closeSerialPort() {
		if (serialPortOpen) {
			System.out.println("Closing Serialport");
			serialPort.close();
			serialPortOpen = false;
		} else {
			System.out.println("Serialport already closed");
		}
	}

	void serialPortDataAvailable() {

		String str = new String();
		BufferedReader br = new BufferedReader(new InputStreamReader(
				inputStream));

		try {
			str = br.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println(str);
		switch (new Integer(str)) {
		case 0:
			this.curMove = Move.LEFT;
			System.out.println(" to turn left");
			break;
		case 1:
			this.curMove = Move.RIGHT;
			System.out.println(" take a right!");
			break;
		case 2:
			this.curMove = Move.FWD;
			System.out.println("I drive FWD");
			break;
		case 3:
			this.curMove = Move.BWD;
			System.out.println("I drive BWD");
			break;
		case 4:
			this.curMove = Move.IDLE;
			break;
		case 5:
			setDraw(false);
			this.curMove = Move.STOP;
			System.out.println("I stop");
			break;
		case 6:
			setDraw(true);
			this.curMove = Move.FWD;
			break;
		case 7:
			setDraw(false);
//			this.curMove = Move.STOP;
			break;
		default:
			break;
		}

	}

	public static synchronized void writeData(String data) {
		try {
			outputStream.write(data.getBytes());
		} catch (Exception e) {
			System.out.println("could not write to port");
		}
	}

	public Move getCurMove() {
		return curMove;
	}

	public void setCurMove(Move curMove) {
		this.curMove = curMove;
	}

	public void setDraw(boolean draw) {
		this.draw = draw;
	}

	public boolean getDraw() {
		return draw;
	}

	public boolean getDrawNeverCalledBefore() {
		return drawNeverCalledBefore;
	}

	public void setDrawNeverCalledBefore(boolean drawNeverCalledBefore) {
		this.drawNeverCalledBefore = drawNeverCalledBefore;
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
