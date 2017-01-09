import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Hovedprogram {
	public static int counter = 0;
	public static int sampleTime = 5;
	public static int sampleSize;
	public static ArrayList<Double> data;
	/*
	public static returParameter førsteKonvertering(parameter){
		// Konverter den første måling (fx 1000 målinger)
		// Indfør tjek på om målingen faktisk er et tal 
		// Returner data-arraylisten
	}
	
	public static returParameter konverter(parameter){
		// Fjerner et antal målinger
		// Indsætter det samme antal nye målinger
		// konverter tal
		// tjek at målingen er et tal
		// Returner data-arraylisten
	}
	
	public static void opretFil(parameter){
		// tjek om fil eksisterer
		// hvis filen eksisterer, slet den
		// ellers opret filen
	}
	*/
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
		// TODO: Tjek om filen eksisterer - hvis den gør, slet
		try {
			FileWriter fil = new FileWriter("Rå data.txt", true);
			PrintWriter ud = new PrintWriter(fil);
			for (int i = 0; i < liste.size(); i++) {
				ud.println(i + ": " + liste.get(i));
			}

			ud.close();
			System.out.println("Skrevet til fil");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	/** gem til fil */
	public static void gemTilFil(double puls) {
		// TODO: Tjek om filen eksisterer - hvis den gør, slet
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

		listen = t.getValue(sampleSize);
		for (String p : listen) {
			data.add(Double.parseDouble(p));
		}
		System.out.println("Vi er begyndt!");
		pulsB = new Pulsberegner(sampleSize, sampleTime);
		System.out.println("Pulsen er inden start: " + puls);

		sampleSize = 200;
		for (;;) {
			for (int i = 0; i < sampleSize; i++) {
				data.remove(i);
			}

			listen = t.getValue(sampleSize);
			double t1 = System.currentTimeMillis();
			for (String p : listen) {
				// System.out.println(p);
				data.add(Double.parseDouble(p));
			}
			System.out.println("Data er nu " + data.size() + " lang");

			puls = pulsB.beregnPuls(data);
			gemListeTilFil(data);

			if (puls == 7 && prevPuls != 0)
				puls = prevPuls;
			if (puls > 0) {
				// if (puls > (1.1 * prevPuls) && prevPuls != 0)
				// puls = 1.1 * prevPuls;
				gemTilFil(puls);
			} else
				System.out.println("Dårlig måling - fortsætter");
			counter++;
			double t2 = System.currentTimeMillis();
			System.out.println("Det tog: " + (t2 - t1) + "ms");
			System.out.println("Pulsen var: " + puls + " bpm");
			prevPuls = puls;
		}
	}
}
