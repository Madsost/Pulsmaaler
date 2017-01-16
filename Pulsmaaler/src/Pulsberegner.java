import java.util.ArrayList;

public class Pulsberegner {
	private double middelVal, standardAfv, puls, max;
	private int sampleSize;
	private int sampleTime, movingPoint;
	private ArrayList<Double> data;
	private ArrayList<Double> toppe;
	private ArrayList<Double> pulsmålinger = new ArrayList<>();

	/** konstruktør, der modtager en liste som parameter */
	public Pulsberegner(int sampleSize, int sampleTime) {
		this.sampleSize = sampleSize;
		this.sampleTime = sampleTime;
	}

	/**
	 * udregner pulsen baseret på bølgetoppe og returnerer pulsen som et
	 * flydende tal
	 */
	public double beregnPuls(ArrayList<Double> målinger) {
		data = målinger;
		sampleSize = data.size();
		middelVal = 0;
		standardAfv = 0;
		puls = 0;
		movingPoint = 20;
		/*
		 * for(double tal : data){ System.out.println(tal); }
		 */
		System.out.println("Udjævn");
		data = udjævn(); /* 1 */
		/*
		 * for(double tal : data){ System.out.println(tal); }
		 */

		beregnMiddelVal(); /* 2 */
		beregnAfvigelse(); /* 3 */
		// System.out.println("C");

		System.out.println("Middelværdi: " + middelVal + "\nAfvigelse: " + standardAfv);
		max = middelVal + 1.1 * standardAfv;
		System.out.println("Max er defineret til: " + max);

		findTop(); /* 4 */
		int peakCount = toppe.size();
		int j = peakCount - 1;
		double temp = 0;
		while (j > 0) { /* 5 */
			temp += (toppe.get(j) - toppe.get(j - 1));
			j--;
		}
		temp = temp * sampleTime;
		temp /= (peakCount - 1);
		puls = 60000 / temp;

		if (peakCount >= 3) /* 6 */
			return puls;
		else
			return 0;
	}

	/** udregner den laveste måling */
	public double beregnMin() {
		double min = 4.995;
		for (double tal : data) {
			min = (tal < min) ? tal : min;
		}
		return min;
	}

	/** udregner den højeste måling */
	public double beregnMax() {
		double max = 0;
		for (double tal : data) {
			max = (tal > max) ? tal : max;
		}
		return max;
	}

	/** finder antallet og positionen på bølgetoppe = pulsslag */
	public void findTop() {
		double peakCount = 0;
		boolean fundet = false;
		toppe = new ArrayList<>();
		int i = 20;
		while (!fundet && i < data.size()) { /* 4A */
			fundet = (data.get(i) > max);

			// Problem: Hvis pulsslaget registreres på den første plads
			boolean første = (i == 0);
			if (fundet && !første) { /* 4B */
				double a = (data.get(i) - data.get(i - 1));
				double y1 = data.get(i);
				double x1 = i;
				if (a > 0) { /* 4C */
					double b = y1 - a * x1;
					// Jeg tror det er en af disse to beregninger hvori det kan
					// gå galt.
					// Der må jo komme nogle minus-værdier et sted .. ?
					double x = (max - b) / a;
					toppe.add(x);
					i += 44;
					peakCount++;
				} else
					fundet = false;
			}
			i++;
			fundet = false;
		}
		System.out.println(peakCount);
	}

	/** gennemløber listen og udregner middelværdien af målingerne */
	public void beregnMiddelVal() {
		double sum = 0;
		for (double tal : data) { /* 2A */
			sum = sum + tal;
		}
		double resultat = sum / sampleSize;
		System.out.println(resultat);
		middelVal = resultat;
	}

	public double beregnMiddelVal(double[] inddata) {
		double sum = 0;
		for (double tal : inddata) { /* 2A */
			sum = sum + tal;
		}
		double resultat = sum / inddata.length;
		// System.out.println(resultat);
		return resultat;
	}

	/** gennemløber listen og udregner standardafvigelsen fra middelværdien */
	public void beregnAfvigelse() {
		double temp = 0;
		for (double tal : data) { /* 3A */
			temp += ((middelVal - tal) * (middelVal - tal));
		}
		double afv = Math.sqrt(temp / (sampleSize - 1));
		System.out.println(afv);
		standardAfv = afv;
	}

	/** udjævner målingerne med en løbende middelværdi for at fjerne støj */
	public ArrayList<Double> udjævn() {
		ArrayList<Double> nyData = new ArrayList<>();
		double nums = movingPoint;
		for (int i = movingPoint; i < data.size(); i++) { /* 1A */
			double sum = 0;
			for (int j = 0; j < nums; j++) { /* 1B */
				sum += data.get(i - movingPoint + j);
			}
			double resultat = sum / nums;
			nyData.add(resultat);
		}
		return nyData;
	}
	
	public double[] udjævn(double[] data, int tal){
		double[] output = data; 
		for (int i = tal; i < data.length; i++) { 
			double sum = 0;
			for (int j = 0; j < tal; j++) { 
				sum += data[i - tal + j];
			}
			double resultat = sum / tal;
			output[i] = resultat;
		}
		return output;
	}

	public double beregnVer2(ArrayList<Double> målinger) {
		/*
		 * Konverter målingerne til et array af simpel type
		 * 
		 * Send målingerne igennem et filter (FIR / SMA)
		 * 
		 * Udregn middelværdien
		 * 
		 * Træk middelværdien fra (centrer målingerne omkring middelværdien)
		 * 
		 * Traverser datasættet sammenlign med datasættet forskudt.
		 * 
		 * Det forventes at være nok at gøre mellem 60 og 150 gange.
		 * 
		 * Find den forskydning med lavest score (afvigelse)
		 * 
		 * Hvis afvigelsen er over en værdi (som vi skal finde) må målingen
		 * siges at være for dårlig til at beregne en puls.
		 * 
		 * Antallet af forskydninger giver et gæt på puls / frekvensen
		 * 
		 * Gem målingerne i en liste og udregn gennemsnittet af dem Eventuelt
		 * vægtet, så de forrige målinger er vigtigere end den nye
		 * 
		 * Returner puls-gættet
		 * 
		 */

		double[] inddata = new double[målinger.size()];

		// Konverterer til array af simpel type
		for (int i = 0; i < målinger.size(); i++) {
			inddata[i] = målinger.get(i);
		}

		double[] nyeKoeff = new double[] { -0.005790971968579455, -0.0016914204666226684, -0.0018843635085074118,
				-0.002050473677114152, -0.002180054224353485, -0.0022625221529621563, -0.002287815734377963,
				-0.00224560580166865, -0.0021245808099482387, -0.0019171544295382601, -0.0016039920346966935,
				-0.0011976164334236798, -0.0006628530701148304, -0.000007731040172825967, 0.0007701150949619668,
				0.0016794573650217453, 0.0027257251321815643, 0.003907819255531189, 0.005221196387092289,
				0.006662345770641899, 0.008224102913472189, 0.009903557392250356, 0.01168519499965433,
				0.013560836820519416, 0.015515191900856568, 0.017529797925762394, 0.019586770170979755,
				0.021668637039765386, 0.023755329979769018, 0.02582415884284531, 0.027854216221964687,
				0.029823011590910564, 0.0317110655195153, 0.03349428476903138, 0.03515237502805645, 0.03666613485398455,
				0.03801764493611727, 0.0391900482289899, 0.04016979231125777, 0.04094555255457108, 0.04150649289256357,
				0.041845632962081984, 0.04195857280289159, 0.041845632962081984, 0.04150649289256357,
				0.04094555255457108, 0.04016979231125777, 0.0391900482289899, 0.03801764493611727, 0.03666613485398455,
				0.03515237502805645, 0.03349428476903138, 0.0317110655195153, 0.029823011590910564,
				0.027854216221964687, 0.02582415884284531, 0.023755329979769018, 0.021668637039765386,
				0.019586770170979755, 0.017529797925762394, 0.015515191900856568, 0.013560836820519416,
				0.01168519499965433, 0.009903557392250356, 0.008224102913472189, 0.006662345770641899,
				0.005221196387092289, 0.003907819255531189, 0.0027257251321815643, 0.0016794573650217453,
				0.0007701150949619668, -0.000007731040172825967, -0.0006628530701148304, -0.0011976164334236798,
				-0.0016039920346966935, -0.0019171544295382601, -0.0021245808099482387, -0.00224560580166865,
				-0.002287815734377963, -0.0022625221529621563, -0.002180054224353485, -0.002050473677114152,
				-0.0018843635085074118, -0.0016914204666226684, -0.005790971968579455, };

		FIR filter = new FIR(nyeKoeff);
		
		double middelval = beregnMiddelVal(inddata);

		for (double tal : inddata) {
			tal = tal - middelval;
		}

		for (int j = 0; j < inddata.length; j++) {
			inddata[j] = filter.getOutputSample(inddata[j]);
			inddata[j] = inddata[j] / filter.skalar;
		}

		//double[] nytsæt = beregnVer3(inddata);
		// inddata = nytsæt;

		double[] scores = new double[299];
		for (int j = 1; j < 300; j++) {
			double score = 0;
			int b = j;
			int a = 0;
			while (b < (inddata.length)) {
				score += (inddata[a] - inddata[b]) * (inddata[a] - inddata[b]);
				b++;
				a++;
			}
			scores[j-1] = score;
		}

		double min = Double.MAX_VALUE;
		int måling = 0;
		//ArrayList<Integer> måling = new ArrayList<>();
		for (int i = 60; i < scores.length; i++) {
			if (scores[i] < min) {
				min = scores[i];
				måling = (i);
			}
		}
		/*
		for (int i = 0; i < scores.length; i++) {
			System.out.println((i + 1) + ": " + scores[i]);
		}*/

		if (min > 2000){
			System.out.println("Min er: "+min);
			return 0; // dårlig måling
		}
		else {
			/*int sum = 0;
			for (int p : måling) {
				sum += p;
			}
			sum /= måling.size();*/
			int sum = måling;
			System.out.println("Min er: " +min);
			double pulsgæt = 60000 / (sum * sampleTime);
			System.out.println("Bedste gæt: " + pulsgæt);
			pulsmålinger.add(pulsgæt);
			
			// Skal denne værdi være lavere end 5?
			while (pulsmålinger.size() >= 5) {
				pulsmålinger.remove(0);
			}
			
			double jævnetPuls = 0;
			for (double puls : pulsmålinger) {
				jævnetPuls += puls;
			}
			
			jævnetPuls = jævnetPuls / pulsmålinger.size();
			return jævnetPuls;
		}
	}

	public double beregnVer3(ArrayList<Double> målinger) {
		/*
		for (int j = 0; j < nydata.length - 1; j++) {
			nydata[j] = nydata[j + 1] - nydata[j];
		}*/
		
		double[] inddata = new double[målinger.size()];

		// Konverterer til array af simpel type
		for (int i = 0; i < målinger.size(); i++) {
			inddata[i] = målinger.get(i);
		}
		
		double middelval = beregnMiddelVal(inddata);

		for (double tal : inddata) {
			tal = tal - middelval;
		}
		
		// SMA med 30 målinger 
		inddata = udjævn(inddata,20);

		double[] scores = new double[300];
		int k = 1;
		for (int j = 0; j < 300; j++) {
			double score = 0;
			int b = k;
			while (b < (inddata.length - j)) {
				score += (inddata[j] - inddata[b]) * (inddata[j] - inddata[b]);
				b++;
			}
			scores[j] = score;
			k++;
		}

		double min = Double.MAX_VALUE;
		int måling = 0;
		//ArrayList<Integer> måling = new ArrayList<>();
		for (int i = 60; i < scores.length; i++) {
			if (scores[i] < min && min > 50) {
				min = scores[i];
				måling = (i);
			}
		}
		
		for (int i = 0; i < scores.length; i++) {
			System.out.println((i + 1) + ": " + scores[i]);
		}

		if (min > 800)
			return 0; // dårlig måling
		else {
			/*int sum = 0;
			for (int p : måling) {
				sum += p;
			}
			sum /= måling.size();*/
			int sum = måling;
			double pulsgæt = 60000 / (sum * sampleTime);
			System.out.println("Bedste gæt: " + pulsgæt);
			pulsmålinger.add(pulsgæt);
			
			while (pulsmålinger.size() >= 5) {
				pulsmålinger.remove(0);
			}
			
			double jævnetPuls = 0;
			for (double puls : pulsmålinger) {
				jævnetPuls += puls;
			}
			
			jævnetPuls = jævnetPuls / pulsmålinger.size();
			// double jævnetPuls = 0;
			return jævnetPuls;
		}
	}
}
