import java.util.ArrayList;

public class Pulsberegner {
	private double middelVal, standardAfv, puls, max;
	private int sampleSize;
	private int sampleTime, movingPointStr; 
	private ArrayList<Double> data;
	private ArrayList<Double> toppe = new ArrayList<Double>();

	/** konstrukt�r, der modtager en liste som parameter */
	public Pulsberegner(int sampleSize,int sampleTime) {
		this.sampleSize = sampleSize;
		this.sampleTime = sampleTime;
	}

	/**
	 * udregner pulsen baseret p� b�lgetoppe og returnerer pulsen som et
	 * flydende tal
	 */
	public double beregnPuls(ArrayList<Double> m�linger) {
		data = m�linger;
		sampleSize = data.size();
		middelVal = 0;
		standardAfv = 0;
		puls = 0;
		movingPointStr = 80;
		System.out.println("A");
		
		data = udj�vn();
		System.out.println("B");
		
		middelVal = beregnMiddelVal();
		standardAfv = beregnAfvigelse();
		System.out.println("C");
		
		System.out.println("Middelv�rdi: " + middelVal + "\nAfvigelse: " + standardAfv);
		max = middelVal + 1.1 * standardAfv;
		System.out.println("Max er defineret til: " + max);
		
		toppe = findTop();
		int peakCount = toppe.size();
		int j = peakCount -1;
		double temp = 0;
		while (j > 0) {
			temp += (toppe.get(j) - toppe.get(j - 1));
			j--;
		}
		temp = temp * sampleTime;
		temp /= (peakCount - 1);
		puls = 60000 / temp;

		if (peakCount >= 3)
			return puls;
		/*if (peakCount < 3 && peakCount > 0) {
			System.out.println("Mellem 0 og 3 toppe");
			return puls;
		}*/ else
			return 0;
	}

	/** udregner den laveste m�ling */
	public double beregnMin() {
		double min = 4.995;
		for (double tal : data) {
			min = (tal < min) ? tal : min;
		}
		return min;
	}

	/** udregner den h�jeste m�ling */
	public double beregnMax() {
		double max = 0;
		for (double tal : data) {
			max = (tal > max) ? tal : max;
		}
		return max;
	}

	/** finder antallet og positionen p� b�lgetoppe = pulsslag */
	public ArrayList<Double> findTop() {
		double peakCount = 0;
		boolean fundet = false;
		ArrayList<Double> peaks = new ArrayList<>();
		int i = movingPointStr;
		while (!fundet && i < data.size()) {
			fundet = (data.get(i) > max);

			boolean f�rste = (i == 0);
			if (fundet && !f�rste) {
				double a = (data.get(i) - data.get(i - 1)) / (i - (i - 1));
				if (a > 0) {
					double x = max * (1 / a) + i;
					peaks.add(x);
					i += 34;
					peakCount++;
				} else
					fundet = false;
			}
			i++;
			fundet = false;
		}
		System.out.println(peakCount);
		return peaks;
	}

	/** genneml�ber listen og udregner middelv�rdien af m�lingerne */
	public double beregnMiddelVal() {
		double sum = 0;
		for (double tal : data) {
			sum = sum + tal;
		}
		double resultat = sum / sampleSize;
		System.out.println(resultat);
		return resultat;
	}

	/** genneml�ber listen og udregner standardafvigelsen fra middelv�rdien */
	public double beregnAfvigelse() {
		double temp = 0;
		for (double tal : data) {
			temp += ((middelVal - tal) * (middelVal - tal));
		}
		double afv = Math.sqrt(temp / (sampleSize - 1));
		System.out.println(afv);
		return afv;
	}
	
	public ArrayList<Double> udj�vn(){
		ArrayList<Double> nyData = new ArrayList<>();
		System.out.println("Udj�vn 1");
		for(int i = 80; i<data.size()-80; i++){
			//System.out.println("Udj�vn 2");
			double temp = 0;
			for(int j = 0; j<161; j++){
				temp += data.get(i-80+j);
			}
			temp /= 161;
			nyData.add(temp);
		}
		return nyData;
	}
	
}
