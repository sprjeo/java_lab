

public class Main {
    public static void main(String[] args) {
        int[][] matrix = {
                {5, 3, 8},
                {1, 9, 2},
                {4, 6, 7}
        };

        MaxNumberFinder finder = new MaxNumberFinder();
        String maxNumber = finder.findMaxNumber(matrix);

        System.out.println("Максимальное число: " + maxNumber);
    }
}

