import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class BracketChecker {
    private final Map<Character, Character> bracketPairs;
    private final Map<Character, Character> reverseBracketPairs;

    public BracketChecker(String configFilePath) throws IOException {
        this.bracketPairs = new HashMap<>();
        this.reverseBracketPairs = new HashMap<>();
        loadBracketConfig(configFilePath);
    }

    private void loadBracketConfig(String configFilePath) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(configFilePath)));
        JSONObject config = new JSONObject(content);
        JSONArray brackets = config.getJSONArray("bracket");

        for (int i = 0; i < brackets.length(); i++) {
            JSONObject pair = brackets.getJSONObject(i);
            char left = pair.getString("left").charAt(0);
            char right = pair.getString("right").charAt(0);
            bracketPairs.put(left, right);
            reverseBracketPairs.put(right, left);
        }
    }

    public void checkFile(String filePath) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(filePath)));
        Stack<BracketPosition> stack = new Stack<>();

        for (int i = 0; i < content.length(); i++) {
            char current = content.charAt(i);

            if (bracketPairs.containsKey(current)) {
                stack.push(new BracketPosition(current, i));
            } else if (reverseBracketPairs.containsKey(current)) {
                if (stack.isEmpty()) {
                    System.err.println("Ошибка: Неожиданная закрывающая скобка '" + current + "' в позиции " + i);
                    return;
                }

                BracketPosition lastOpened = stack.pop();
                char expectedClosing = bracketPairs.get(lastOpened.bracket);
                if (current != expectedClosing) {
                    System.err.println("Ошибка: Ожидалась закрывающая скобка '" + expectedClosing +
                            "', но найдена '" + current + "' в позиции " + i);
                    System.err.println("Соответствующая открывающая скобка '" + lastOpened.bracket +
                            "' была в позиции " + lastOpened.position);
                    return;
                }
            }
        }

        if (!stack.isEmpty()) {
            BracketPosition unclosed = stack.pop();
            System.err.println("Ошибка: Не закрыта скобка '" + unclosed.bracket +
                    "' в позиции " + unclosed.position);
        } else {
            System.out.println("Проверка завершена успешно: все скобки расставлены правильно.");
        }
    }

    private static class BracketPosition {
        char bracket;
        int position;

        BracketPosition(char bracket, int position) {
            this.bracket = bracket;
            this.position = position;
        }
    }
}