import java.util.ArrayList;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

/**
 * Sensor-klasse
 * 
 * Indhenter måling fra en serielt tilsluttet sensor Arduinoen måler hvert 5.
 * ms.
 * 
 * Indeholder en metode til at hente målinger, til at rense den første måling og
 * en opsætningsmetode. Returnerer en liste med sample_size størrelse med
 * målinger.
 */

public class Sensor {

	private SerialPort serialPort;
	private ArrayList<String> målinger;
	private String buffer = "";
	private boolean test = false;

	/**
	 * opsætter den serielle port og melder at data terminalen er klar til at
	 * modtage data
	 */
	public void setup() {
		try {
			String[] portNames = SerialPortList.getPortNames();
			String port = portNames[0];
			this.serialPort = new SerialPort(port);
			serialPort.openPort(); // Open serial port
			serialPort.setParams(19200, 8, 1, 0); // Set params.
			serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
			serialPort.setDTR(true);
		} catch (ArrayIndexOutOfBoundsException b) {
			System.out.println("Der var ikke tilsluttet nogen enheder");
		} catch (SerialPortException ex) {
			System.out.println("Serial Port Exception: " + ex);
		}
	}

	/**
	 * Kalder mål() og returnerer en liste med det antal målinger, som
	 * parameteren bestemmer
	 */
	public ArrayList<String> getValue(int sampleSize) {
		mål(sampleSize);
		return målinger;
	}

	/**
	 * Modtager målinger fra sensoren, skiller dem ad og tilføjer dem til en
	 * liste
	 */
	public void mål(int sampleSize) {
		int point = 0;
		// instantierer listen - således gemmes kun sample_size for hvert kald
		målinger = new ArrayList<>(sampleSize);
		do {
			if (test)
				System.out.println("Mål!");
			try {
				// hvis der er målinger på vej
				if (serialPort.getInputBufferBytesCount() > 0) {
					buffer += serialPort.readString();
					if (test)
						System.out.println("buffer: " + buffer);
					int pos = -1;
					while ((pos = buffer.indexOf("!")) > -1) {
						målinger.add(buffer.substring(0, pos));
						buffer = buffer.substring(pos + 1);
					}
				}
				// Venter på at input-bufferen bliver fyldt igen
				else
					try {
						Thread.sleep(75);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
			} catch (SerialPortException e1) {
				e1.printStackTrace();
			}
			// Udskriver et punktum for hvert 200 måling (ca. hvert sekund)
			if (test) {
				if (målinger.size() - point >= 200) {
					System.out.print(".");
					point = (målinger.size() / 200) * 200;
				}
				System.out.println(målinger.size());
			}
		} while (målinger.size() <= sampleSize);
		System.out.println();
	}

	/** Rydder den første måling, da de første målinger typisk er fejlagtige */
	public void clear() {
		try {
			// hvis der er målinger på vej
			if (serialPort.getInputBufferBytesCount() > 0) {
				buffer += serialPort.readString();
				int pos = -1;
				if ((pos = buffer.lastIndexOf("!")) > -1) {
					buffer = buffer.substring(pos + 1);
				}
			}
		} catch (SerialPortException e2) {
			e2.printStackTrace();
		}
	}
}
