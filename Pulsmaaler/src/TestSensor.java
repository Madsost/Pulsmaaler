import java.util.ArrayList;

/**
 * Simulering af en pulsmåler
 * 
 * Anvender en sinus og cosinus-funktion til at simulere en måling
 */

public class TestSensor extends Sensor {

	private boolean test = false;
	public double count = 0.01;
	private ArrayList<String> målinger;

	/**
	 * Pulsfunktion - indeholder den funktion, som simulerer en puls Modtager et
	 * tal, som bruges som variablen i funktionen.
	 */
	public double pulsfunktion(double x) {
		double funktion = -1 * Math.sin(8 * x) + 1.5 * Math.cos(4 * x) + 2;
		return funktion;
	}

	/**
	 * svarer til getValues i Sensor-klassen. Venter 5ms for at matche arduinoen
	 */
	@Override
	public ArrayList<String> getValue(int sample_size) {
		målinger = new ArrayList<>();
		int point = 0;
		for (int i = 0; i < sample_size; i++) {
			double resultat = pulsfunktion(count);
			if (test)
				System.out.println(resultat);
			// tæller 1/10 op for hvert kald
			count += 0.01;
			String temp = "" + resultat;
			målinger.add(temp);
			if (målinger.size() - point >= 200) {
				System.out.print(".");
				point = (målinger.size() / 200) * 200;
			}
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (test)
			System.out.println(count);
		return målinger;
	}
}
