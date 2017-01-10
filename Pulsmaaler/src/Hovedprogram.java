import java.io.*;
import java.util.ArrayList;

public class Hovedprogram {
	public static int counter = 0;
	public static int sampleTime = 5;
	public static int sampleSize;
	public static ArrayList<Double> data;
	public static ArrayList<Double> data200;

	/**
	 * til den første konvertering. konverterer de første 1000 målinger og
	 * returnerer listen
	 */
	public static ArrayList<Double> førsteKonvertering(ArrayList<String> listen, ArrayList<Double> data) {
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
	 * fjerner de første x målinger, indsætter det samme antal nye målinger,
	 * konverterer og returnerer listen
	 */
	public static ArrayList<Double> konverter(ArrayList<String> listen, ArrayList<Double> data) {
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
	public static void sletFil() {
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
	public static boolean test() {
		String spm = "Køres med test-udskrifter?";
		String svar = javax.swing.JOptionPane.showInputDialog(spm, "ja");
		if (svar != null && svar.equals("ja"))
			return true;
		else
			return false;
	}

	/** spørgsmål om der skal bruges en testsensor */
	public static boolean testsensor() {
		String spm = "Køres med test-sensor?";
		String svar = javax.swing.JOptionPane.showInputDialog(spm, "ja");
		if (svar != null && svar.equals("ja"))
			return true;
		else
			return false;
	}

	/** gem til fil */
	public static void gemListeTilFil(ArrayList<Double> liste) {
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
			t.setup();
		}

		if (test)
			System.out.println("start");
		ArrayList<String> listen = new ArrayList<>();
		data = new ArrayList<>();

		// fjerner begyndelsesstøjs
		if (!testsensor)
			t.clear();
		if (test)
			System.out.println("Klar!");
		double prevPuls = 0;
		double puls = 0;
		sampleSize = 1000;

		// Modtager de første målinger
		listen = t.getValue(sampleSize);

		System.out.println("Vi er begyndt!");
		pulsB = new Pulsberegner(sampleSize, sampleTime);
		double t1 = System.currentTimeMillis();

		// Beregn puls af første målinger
		data = førsteKonvertering(listen, data);
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
			listen = t.getValue(sampleSize);
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
}
