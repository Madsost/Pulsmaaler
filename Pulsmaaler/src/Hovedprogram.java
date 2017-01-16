import java.io.*;
import java.util.ArrayList;

/**
 * Kontrolklasse, der har til ansvar at styre slagets gang.
 * <p>
 * Klassen har metoder til at slette sessions-filen, gemme til den, konvertere
 * målinger fra Sensor-klassen til tal, samt afgøre om en test er i gang.
 * 
 * @author Mads og Mikkel
 */
public class Hovedprogram {
	public int counter = 0;
	public int sampleTime = 5;
	public int sampleSize;
	public ArrayList<Double> data;
	public ArrayList<Double> data200;

	/**
	 * Denne metode håndterer den første konvertering i en kørsel. Den modtager
	 * <code>listen</code> fra <code>hentMålinger</code> og <code>data</code>,
	 * som er det aktuelle datasæt. Metoden forsøger at konvertere hver streng i
	 * listen til et tal, hvis det fejler springes målingen over.
	 * <p>
	 * 
	 * @param listen
	 *            en liste af målinger fra Arduinoen som strenge.
	 * @param data
	 *            den liste som de konverterede målinger skal gemmes i.
	 * @return en liste med målingerne som tal.
	 */
	public ArrayList<Double> foersteKonvertering(ArrayList<String> listen, ArrayList<Double> data) {
		// Konverter den første måling (fx 1000 målinger)
		// Indfør tjek på om målingen faktisk er et tal
		// Returner data-arraylisten
		for (String p : listen) {
			p = p.trim();
			try {
				Double.parseDouble(p);
			} catch (NumberFormatException e) {
				continue;
			}
			data.add(Double.parseDouble(p));
		}
		System.out.println("Data er nu " + data.size() + " lang");
		return data;
	}

	/**
	 * Metoden rydder <code>data</code>-listen for de første sampleSize målinger
	 * og tilføjer de nye målinger i enden af listen. fjerner de første x
	 * målinger, indsætter det samme antal nye målinger, konverterer og
	 * returnerer listen
	 */
	public ArrayList<Double> konverter(ArrayList<String> listen, ArrayList<Double> data) {
		// Fjerner et antal målinger
		// Indsætter det samme antal nye målinger
		// konverter tal
		// tjek at målingen er et tal
		// Returner data-arraylisten
		data.subList(0, sampleSize).clear();
		for (String p : listen) {
			p = p.trim();
			try {
				Double.parseDouble(p);
			} catch (NumberFormatException e) {
				continue;
			}
			data.add(Double.parseDouble(p));
		}
		return data;
	}

	/**
	 * sletter filen for at sikre, at der kun gemmes for den nuværende session
	 */
	public void sletFil() {
		// tjek om fil eksisterer
		// hvis filen eksisterer, slet den
		// ellers opret filen
		File f = new File("Rå data.txt");
		File f1 = new File("Maalinger.txt");
		if (f.exists())
			f.delete();
		if (f1.exists())
			f1.delete();
	}

	/** spørgsmål om test er i gang */
	public boolean test() {
		String spm = "Køres med test-udskrifter?";
		String svar = javax.swing.JOptionPane.showInputDialog(spm, "ja");
		if (svar != null && svar.equals("ja"))
			return true;
		else
			return false;
	}

	/** spørgsmål om der skal bruges en testsensor */
	public boolean testsensor() {
		String spm = "Køres med test-sensor?";
		String svar = javax.swing.JOptionPane.showInputDialog(spm, "ja");
		if (svar != null && svar.equals("ja"))
			return true;
		else
			return false;
	}

	/** gem til fil */
	public void gemListeTilFil(ArrayList<Double> liste) {
		try {
			FileWriter fil = new FileWriter("Rå data.txt", true);
			PrintWriter ud = new PrintWriter(fil);
			for (int i = 0; i < liste.size(); i++) {
				// ud.println(i + ": " + liste.get(i));
				ud.println(liste.get(i));
			}
			ud.close();
			System.out.println("Skrevet til fil");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	/** gem til fil */
	public void gemTilFil(double puls) {
		try {
			FileWriter fil = new FileWriter("Maalinger.txt", true);
			PrintWriter ud = new PrintWriter(fil);
			int temp1 = (int) puls;
			int temp2 = (int) (puls * 100) % 100;
			String skriver = "Tid " + (counter * sampleSize / 200) + "s: " + temp1 + "." + temp2 + " bpm";
			ud.println(skriver);
			ud.close();
			System.out.println("Skrevet til fil");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	public void koer() {
		sletFil();
		// Tjekker om programmet skal køres med
		// testudskrifter
		boolean test = test();
		// tjekker om programmet skal køred med testsensor
		boolean testsensor = testsensor();

		Sensor t;
		Pulsberegner pulsB;

		if (testsensor) // opsætter testprogram
		{
			t = new TestSensor();
		} else // hovedprogram starter her
		{
			t = new Sensor();
			t.opsaet();
		}

		if (test)
			System.out.println("start");
		ArrayList<String> listen = new ArrayList<>();
		data = new ArrayList<>();

		// fjerner begyndelsesstøjs
		if (!testsensor)
			t.rens();
		if (test)
			System.out.println("Klar!");
		double puls = 0;
		sampleSize = 1000;

		// Modtager de første målinger
		listen = t.hentMaalinger(sampleSize);

		System.out.println("Vi er begyndt!");
		pulsB = new Pulsberegner(sampleSize, sampleTime);
		double t1 = System.currentTimeMillis();

		// Beregn puls af første målinger
		data = foersteKonvertering(listen, data);
		puls = pulsB.beregnPuls(data);
		System.out.println("Pulsen er inden start: " + puls);
		gemListeTilFil(data);

		if (puls > 0) {
			gemTilFil(puls);
		} else
			System.out.println("Dårlig måling - fortsætter");

		counter++; // bruges til at skrive tid i filen
		double t2 = System.currentTimeMillis();
		System.out.println("Det tog: " + (t2 - t1) + "ms");
		System.out.println("Pulsen var: " + puls + " bpm");

		// Programmet er klart - begynder uendelig løkke
		for (;;) {
			t1 = System.currentTimeMillis();
			sampleSize = 600;

			// henter målinger, fjerner de første sampleSize målinger
			listen = t.hentMaalinger(sampleSize);
			data = konverter(listen, data);

			// tilføjer de nye målinger til filen
			data200 = new ArrayList<>();
			for (int i = (data.size() - sampleSize); i < data.size(); i++) {
				data200.add(data.get(i));
			}

			System.out.println("Data er nu " + data.size() + " lang");

			puls = pulsB.beregnPuls(data);
			gemListeTilFil(data200);

			if (puls > 0) {
				gemTilFil(puls);
			} else
				System.out.println("Dårlig måling - fortsætter");
			counter++;
			t2 = System.currentTimeMillis();
			System.out.println("Det tog: " + (t2 - t1) + "ms");
			System.out.println("Pulsen var: " + puls + " bpm");
		}
	}

	public void koerVer2() {
		// Slet fil
		sletFil();
		sampleSize = 1000;
		sampleTime = 5;

		// Initialiser de nødvendige objekter
		Sensor s = new Sensor();
		Pulsberegner p = new Pulsberegner(sampleSize, sampleTime);

		// Tjekker om programmet skal køres med
		// tjekker om programmet skal køred med testsensor
		boolean testsensor = testsensor();
		if (testsensor)
			s = new TestSensor();
		if (!testsensor) {
			// Opret forbindelse til sensor
			// Setup sensor
			System.out.println("Opsætter sensoren...");
			s.opsaet();
			System.out.println("Renser første måling...");
			s.rens();
		}
		// Enten: vent i x sekunder eller kald hentMålinger i x sekunder og smid
		// målingerne væk
		System.out.println("Vent:");
		s.hentMaalinger(sampleSize);
		System.out.println("Begynder løkke!");
		for (int i = 0; i < 24; i++) {
			ArrayList<String> input = new ArrayList<>();

			System.out.println("Henter målinger...");
			input = s.hentMaalinger(sampleSize);

			data = new ArrayList<>();

			// Konverter målingerne til double (skal det måske gøres allerede i
			// sensoren?)
			System.out.println("Konverterer målingerne...");
			for (String ord : input) {
				ord = ord.trim();
				try {
					Double.parseDouble(ord);
				} catch (NumberFormatException e) {
					continue;
				}
				data.add(Double.parseDouble(ord));
			}

			// System.out.println("Trimmer størrelsen ned til 1000 målinger
			// ...");
			data.subList(sampleSize, data.size()).clear();

			// Gem målinger
			System.out.println("Gemmer datasæt til fil ...");
			gemListeTilFil(data);
			System.out.println("Beregner puls...");
			double puls = p.beregnVer2(data);
			if (puls == 0)
				System.out.println("Dårlig måling!");
			else {
				gemTilFil(puls);
				System.out.println("Pulsen beregnes til: " + puls);
			}
			counter++;
		}
	}

	public static void main(String[] args) {
		Hovedprogram monitor = new Hovedprogram();
		monitor.koerVer2();
	}
}
