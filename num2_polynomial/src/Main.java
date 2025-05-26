import java.util.HashMap;



public class Main {
    public static void main(String[] args) {

        // Создаем полиномы
        HashMap<Integer, Double> coeffs1 = new HashMap<>();
        coeffs1.put(2, 1.0); // x^2
        coeffs1.put(1, -2.0); // -2x
        coeffs1.put(0, 1.0); // 1
        Polynomial p1 = new Polynomial(coeffs1); // x^2 - 2x + 1

        HashMap<Integer, Double> coeffs2 = new HashMap<>();
        coeffs2.put(1, 1.0); // x
        coeffs2.put(0, -1.0); // -1
        Polynomial p2 = new Polynomial(coeffs2); // x - 1


        // Операции с полиномами
        System.out.println("p1: " + p1);
        System.out.println("p2: " + p2);
        System.out.println("p1 + p2: " + p1.add(p2));
        System.out.println("p1 - p2: " + p1.subtract(p2));
        System.out.println("p1 * p2: " + p1.multiply(p2));
        System.out.println("p1 * 2.5: " + p1.multiply(2.5));

        Polynomial[] divisionResult = p1.divide(p2);
        System.out.println("p1 / p2: " + divisionResult[0]);
        System.out.println("p1 % p2: " + divisionResult[1]);

        // Сравнение
        System.out.println("p1 compareTo p2: " + p1.compareTo(p2));

        // Клонирование
        Polynomial p3 = p1.clone();
        System.out.println("Clone of p1: " + p3);
        System.out.println("p1 equals clone: " + p1.equals(p3));
    }
}