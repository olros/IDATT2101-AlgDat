package Java;

public class Exercise2 {
	// Complexity O(n)
	public static double pow1(double x, int n) {
		if (n == 0) return 1.0;
		else return x * pow1(x, n -1);
	}
	// Complexity O(log n)
	public static double pow2(double x, int n) {
		if (n == 0) return 1.0;
		else if (n % 2 == 1) return x * pow2(x * x, (n - 1) / 2);
		else return pow2(x * x, n / 2);
	}
	// Complexity O(1)
	public static double pow3(double x, int n) {
		return Math.pow(x, n);
	}
	public static void checkCorrect(double x, int n, double answer) {
		System.out.println(pow1(x, n) == answer);
		System.out.println(pow2(x, n) == answer);
		System.out.println(pow3(x, n) == answer);
	}
	public static void main(String[] args) {
		checkCorrect(3, 17, 129140163.0);
		checkCorrect(7, 4, 2401.0);
		double x = 2;
		int n = 500;
		int seconds = 5;
		int pow1Count = 0;
		long startTime = System.nanoTime();
		while(System.nanoTime() - startTime < 1000000000.0 * seconds) {
			pow1(x, n);
			pow1Count++;
		}
		System.out.println("Task 1: " + pow1Count / seconds + " times per second");
		int pow2Count = 0;
		startTime = System.nanoTime();
		while(System.nanoTime() - startTime < 1000000000.0 * seconds) {
			pow2(x, n);
			pow2Count++;
		}
		System.out.println("Task 2: " + pow2Count / seconds + " times per second");
		int pow3Count = 0;
		startTime = System.nanoTime();
		while(System.nanoTime() - startTime < 1000000000.0 * seconds) {
			pow3(x, n);
			pow3Count++;
		}
		System.out.println("Task 3: " + pow3Count / seconds + " times per second");
	}
}