import java.io.IOException;

public class Main {
    public static void main(String[] args) {

        String configFile = "C:\\Users\\User\\source\\java_lab\\javalab1\\src\\config.json";
        String fileToCheck = "C:\\Users\\User\\source\\java_lab\\javalab1\\src\\input.txt";

        try {
            BracketChecker checker = new BracketChecker(configFile);
            checker.checkFile(fileToCheck);
        } catch (IOException e) {
            System.err.println("Ошибка при чтении файла: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }
}