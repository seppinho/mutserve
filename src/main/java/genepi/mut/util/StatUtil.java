package genepi.mut.util;
public class StatUtil {

	public static double CIW_LOW(double het, double Covbase) {

		double p = het;
		double n = Covbase;
		double z = 1.96;
		double q = 1 - het;
		double num = p * q;
		double squarez = z * z;
		double squaren = n * n;
		double wilsonci_low = ((p + (squarez) / (2 * n) - z
				* (Math.sqrt(num / n + (squarez) / (4 * (squaren))))) / (1 + squarez / n));
		if (wilsonci_low < 0.0) {
			return 0.0;
		} else {
			return wilsonci_low;
		}
	}

	public static double CIW_UP(double het, double Covbase) {
		double p = het;
		double n = Covbase;
		double z = 1.96;
		double q = 1 - het;
		double num = p * q;
		double squarez = z * z;
		double squaren = n * n;
		double wilsonci_up = ((p + (squarez) / (2 * n) + z
				* (Math.sqrt(num / n + (squarez) / (4 * (squaren))))) / (1 + squarez / n));
		if (wilsonci_up > 1.0) {
			return 1.0;
		} else {
			return wilsonci_up;
		}
	}

	public static double CIAC_LOW(double cov, double Covbase) {

		double z = 1.96;
		double n = Covbase;
		double X = cov + (z * z) / 2;
		double N = n + (z * z);
		double P = X / N;
		double Q = 1 - P;
		double agresticoull_low = (P - (z * (Math.sqrt(P * Q / N))));
		if (agresticoull_low < 0.0) {
			return 0.0;
		} else {
			return agresticoull_low;
		}
	}

	public static double CIAC_UP(double cov, double Covbase) {
		double z = 1.96;
		double n = Covbase;
		double X = cov + (z * z) / (double) (2);
		double N = n + (z * z);
		double P = X / N;
		double Q = 1 - P;
		double agresticoull_up = (P + (z * (Math.sqrt(P * Q / N))));
		if (agresticoull_up > 1.0) {
			return 1.0;
		} else {
			return agresticoull_up;
		}
	}

	public static void main(String[] args) {

	}

}
