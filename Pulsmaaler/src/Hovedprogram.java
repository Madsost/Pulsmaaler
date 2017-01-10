import java.io.*;
import java.util.ArrayList;

public class Hovedprogram {
	public static int counter = 0;
	public static int sampleTime = 5;
	public static int sampleSize;
	public static ArrayList<Double> data;
	public static ArrayList<Double> data200;

	/**
	 * til den f�rste konvertering. konverterer de f�rste 1000 m�linger og
	 * returnerer listen
	 */
	public static ArrayList<Double> f�rsteKonvertering(ArrayList<String> listen, ArrayList<Double> data) {
		// Konverter den f�rste m�ling (fx 1000 m�linger)
		// Indf�r tjek p� om m�lingen faktisk er et tal
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
	 * fjerner de f�rste x m�linger, inds�tter det samme antal nye m�linger,
	 * konverterer og returnerer listen
	 */
	public static ArrayList<Double> konverter(ArrayList<String> listen, ArrayList<Double> data) {
		// Fjerner et antal m�linger
		// Inds�tter det samme antal nye m�linger
		// konverter tal
		// tjek at m�lingen er et tal
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
	 * sletter filen for at sikre, at der kun gemmes for den nuv�rende session
	 */
	public static void sletFil() {
		// tjek om fil eksisterer
		// hvis filen eksisterer, slet den
		// ellers opret filen
		File f = new File("R� data.txt");
		File f1 = new File("Maalinger.txt");
		if (f.exists())
			f.delete();
		if (f1.exists())
			f1.delete();
	}

	/** sp�rgsm�l om test er i gang */
	public static boolean test() {
		String spm = "K�res med test-udskrifter?";
		String svar = javax.swing.JOptionPane.showInputDialog(spm, "ja");
		if (svar != null && svar.equals("ja"))
			return true;
		else
			return false;
	}

	/** sp�rgsm�l om der skal bruges en testsensor */
	public static boolean testsensor() {
		String spm = "K�res med test-sensor?";
		String svar = javax.swing.JOptionPane.showInputDialog(spm, "ja");
		if (svar != null && svar.equals("ja"))
			return true;
		else
			return false;
	}

	/** gem til fil */
	public static void gemListeTilFil(ArrayList<Double> liste) {
		try {
			FileWriter fil = new FileWriter("R� data.txt", true);
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
	public static void gemTilFil(double puls) {
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

	public static void main(String[] args) {
		sletFil();
		// Tjekker om programmet skal k�res med
		// testudskrifter
		boolean test = test();
		// tjekker om programmet skal k�red med testsensor
		boolean testsensor = testsensor();

		Sensor t;
		Pulsberegner pulsB;

		if (testsensor) // ops�tter testprogram
		{
			t = new TestSensor();
		} else // hovedprogram starter her
		{
			t = new Sensor();
			t.setup();
		}

		if (test)
			System.out.println("start");
		ArrayList<String> listen = new ArrayList<>();
		data = new ArrayList<>();

		// fjerner begyndelsesst�js
		if (!testsensor)
			t.clear();
		if (test)
			System.out.println("Klar!");
		double prevPuls = 0;
		double puls = 0;
		sampleSize = 1000;

		// Modtager de f�rste m�linger
		listen = t.getValue(sampleSize);

		System.out.println("Vi er begyndt!");
		pulsB = new Pulsberegner(sampleSize, sampleTime);
		double t1 = System.currentTimeMillis();

		// Beregn puls af f�rste m�linger
		data = f�rsteKonvertering(listen, data);
		puls = pulsB.beregnPuls(data);
		System.out.println("Pulsen er inden start: " + puls);
		gemListeTilFil(data);

		if (puls > 0) {
			gemTilFil(puls);
		} else
			System.out.println("D�rlig m�ling - forts�tter");

		counter++; // bruges til at skrive tid i filen
		double t2 = System.currentTimeMillis();
		System.out.println("Det tog: " + (t2 - t1) + "ms");
		System.out.println("Pulsen var: " + puls + " bpm");

		// Programmet er klart - begynder uendelig l�kke
		for (;;) {
			t1 = System.currentTimeMillis();
			sampleSize = 600;

			// henter m�linger, fjerner de f�rste sampleSize m�linger
			listen = t.getValue(sampleSize);
			data = konverter(listen, data);

			// tilf�jer de nye m�linger til filen
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
				System.out.println("D�rlig m�ling - forts�tter");
			counter++;
			t2 = System.currentTimeMillis();
			System.out.println("Det tog: " + (t2 - t1) + "ms");
			System.out.println("Pulsen var: " + puls + " bpm");
		}
	}
}
