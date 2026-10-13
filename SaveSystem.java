import java.io.*;
import java.util.HashMap;

public class SaveSystem {
    private final String FILE_NAME = "save_data.txt";
    private HashMap<String, String> levelRecords = new HashMap<>();

    public String username = "Player1"; //Still have to do a real settings OptionSSS    public int volume = 80;
    public int resWidth = 951;
    public int resHeight = 1247;
    public int judgmentY = 1050;

    public SaveSystem() {
        loadScores();
    }

    public void loadScores() {
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    levelRecords.put(parts[0], parts[1] + "," + parts[2]);
                }
            }
        } catch (Exception e) {}
    }

    public void saveScore(String level, int score, String grade) {
        int oldScore = getHighScore(level);
        if (score > oldScore) {
            levelRecords.put(level, score + "," + grade);
            saveScoresToFile();
        }
    }

    private void saveScoresToFile() {
        try (PrintWriter out = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (String level : levelRecords.keySet()) {
                out.println(level + "," + levelRecords.get(level));
            }
        } catch (Exception e) {}
    }

    public int getHighScore(String level) {
        if (!levelRecords.containsKey(level)) return 0;
        return Integer.parseInt(levelRecords.get(level).split(",")[0]);
    }
    public String getGrade(String level) {
        if (!levelRecords.containsKey(level)) return "Unplayed";
        return levelRecords.get(level).split(",")[1];
    }
}
