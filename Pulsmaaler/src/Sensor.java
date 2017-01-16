import java.util.ArrayList;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

/**
 * Klasse til kommunikation med Arduinoen og derigennem sensoren.
 * <p>
 * Indhenter målinger fra en serielt tilsluttet Arduino, der måler hvert 5.
 * ms.
 * <p>
 * Indeholder en metode til at hente målinger, til at rense den første måling og
 * en opsætningsmetode. Returnerer en liste med sample_size størrelse med
 * målinger.
 * @author Mads og Mikkel
 */

public class Sensor {

	private SerialPort serialPort;
	private ArrayList<String> målinger;
	private String buffer = "";
	private boolean test = false;

	/**
	 * opsætter den serielle port og melder at data terminalen er klar til at
	 * modtage data.<p>
	 * Anvender JSSC biblioteket. Kræver at der kun er tilsluttet én seriel
	 *  forbindelse til programmet, da det antages at der kun er en port i brug.<p>
	 * Returnerer intet.
	 */
	public void opsaet() {
		try {
			String[] portNames = SerialPortList.getPortNames();
			String port = portNames[0];
			this.serialPort = new SerialPort(port);
			serialPort.openPort(); // Open serial port
			serialPort.setParams(19200, 8, 1, 0); // Set params.
			serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
			serialPort.setDTR(true); // Arduinoen er ligeglad
		} catch (ArrayIndexOutOfBoundsException b) {
			System.out.println("Der var ikke tilsluttet nogen enheder");
		} catch (SerialPortException ex) {
			System.out.println("Serial Port Exception: " + ex);
		}
	}

	/**
	 * Kalder mål() med parameteren og returnerer en liste med det antal målinger fra Arduinoen som parameteren bestemmer.
	 *  
	 * @param sampleSize det ønskede antal målinger, som heltal.
	 * @return målinger: en ArrayList af strenge, hvor hver plads indeholder en måling.
	 */
	public ArrayList<String> hentMaalinger(int sampleSize) {
		maal(sampleSize);
		return målinger;
	}

	/**
	 * Ansvarlig for hentning af målinger. 
	 * Denne metode modtager en heltalsparameter, som bestemmer antallet af målinger der skal hentes.
	 * Metoden består af en løkke, som henter værdier fra den serielle port indtil parameter-værdien er nået.
	 * Hvis <code>InputBuffer</code> er tom, ventes der i 75 ms.
	 * <p> 
	 * Returnerer intet, men gemmer til listen <code>målinger</code>, som kan tilgås med <code>hentMålinger</code>
	 * 
	 * @param sampleSize det ønskede antal målinger, som heltal.
	 */
	public void maal(int sampleSize) {
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
			if (målinger.size() - point >= 200) {
					System.out.print(".");
					point = målinger.size();
				}
			if (test) {
				System.out.println(målinger.size());
			}
			
		// størrelsen kan godt være større end sampleSize!
		} while (målinger.size() <= sampleSize);
		System.out.println();
	}

	/** 
	 * Rydder den første måling, da de første målinger typisk er fejlagtige. <p>
	 * De første målinger i <code>InputBuffer</code> aflæses, opdeles i enkelte målinger og slettes.
	 */
	public void rens() {
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
