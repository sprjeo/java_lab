import java.util.Arrays;

public class MaxNumberFinder {

    private static final int SIZE = 3;
    private static final int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}}; // вверх, вниз, влево, вправо
    private String maxNumber = "";

    public String findMaxNumber(int[][] matrix) {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                boolean[][] visited = new boolean[SIZE][SIZE];
                dfs(matrix, i, j, "", visited);
            }
        }
        return maxNumber;
    }

    private void dfs(int[][] matrix, int x, int y, String currentNumber, boolean[][] visited) {
        visited[x][y] = true;
        currentNumber += matrix[x][y];

        if (currentNumber.compareTo(maxNumber) > 0) {
            maxNumber = currentNumber;
        }

        for (int[] direction : directions) {
            int newX = x + direction[0];
            int newY = y + direction[1];

            if (isValid(newX, newY, visited)) {
                dfs(matrix, newX, newY, currentNumber, visited);
            }
        }

        visited[x][y] = false;
    }

    private boolean isValid(int x, int y, boolean[][] visited) {
        return x >= 0 && x < SIZE && y >= 0 && y < SIZE && !visited[x][y];
    }
}
