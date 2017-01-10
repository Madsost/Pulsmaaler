import java.util.ArrayList;

public class Pulsberegner {
	private double middelVal, standardAfv, puls, max;
	private int sampleSize;
	private int sampleTime, movingPoint;
	private ArrayList<Double> data;
	private ArrayList<Double> toppe;

	/** konstrukt�r, der modtager en liste som parameter */
	public Pulsberegner(int sampleSize, int sampleTime) {
		this.sampleSize = sampleSize;
		this.sampleTime = sampleTime;
	}

	/** udregner pulsen baseret p� b�lgetoppe og returnerer pulsen som et flydende tal */
	public double beregnPuls(ArrayList<Double> m�linger) {
		data = m�linger;
		sampleSize = data.size();
		middelVal = 0;
		standardAfv = 0;
		puls = 0;
		movingPoint = 80;
		/*
		for(double tal : data){
			System.out.println(tal);
		}*/
		System.out.println("Udj�vn");
		data = udj�vn();
		/*
		for(double tal : data){
			System.out.println(tal);
		}*/

		beregnMiddelVal();
		beregnAfvigelse();
		System.out.println("C");

		System.out.println("Middelv�rdi: " + middelVal + "\nAfvigelse: " + standardAfv);
		max = middelVal + 1.1 * standardAfv;
		System.out.println("Max er defineret til: " + max);

		findTop();
		int peakCount = toppe.size();
		int j = peakCount - 1;
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
		else
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
	public void findTop() {
		double peakCount = 0;
		boolean fundet = false;
		toppe = new ArrayList<>();
		int i = 0;
		while (!fundet && i < data.size()) {
			fundet = (data.get(i) > max);

			// Problem: Hvis pulsslaget registreres p� den f�rste plads
			boolean f�rste = (i == 0);
			if (fundet && !f�rste) {
				double a = (data.get(i) - data.get(i - 1)) / (i - (i - 1));
	           double y1 = data.get(i);
	           double x1 = i;
				if (a > 0) {
					double b = y1-a * x1;
					double x = (max-b) / a;
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

	/** genneml�ber listen og udregner middelv�rdien af m�lingerne */
	public void beregnMiddelVal() {
		double sum = 0;
		for (double tal : data) {
			sum = sum + tal;
		}
		double resultat = sum / sampleSize;
		System.out.println(resultat);
		middelVal = resultat;
	}

	/** genneml�ber listen og udregner standardafvigelsen fra middelv�rdien */
	public void beregnAfvigelse() {
		double temp = 0;
		for (double tal : data) {
			temp += ((middelVal - tal) * (middelVal - tal));
		}
		double afv = Math.sqrt(temp / (sampleSize - 1));
		System.out.println(afv);
		standardAfv = afv;
	}

	/** udj�vner m�lingerne med en l�bende middelv�rdi for at fjerne st�j */ 
	public ArrayList<Double> udj�vn(){
		ArrayList<Double> nyData = new ArrayList<>();
		double nums = movingPoint;
		for(int i = movingPoint; i<data.size(); i++){
			double sum = 0;
			for(int j = 0; j<nums; j++){
				sum += data.get(i-movingPoint+j);
			}
			double resultat = sum / nums;
			nyData.add(resultat);
		}
		return nyData;
	}

}
