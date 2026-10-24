import java.awt.event.KeyEvent;
import java.io.*;
import java.util.HashMap;

public class SaveSystem {
    private final String FILE_NAME = "save_data.txt";
    private HashMap<String, String> levelRecords = new HashMap<>();

    public String username = "Player1"; //Still have to do a real settings Option
    public int volume = 80;
    public int resWidth = 951;
    public int resHeight = 1247;
    public int judgmentY = 1050;
    public boolean upscroll = false;

    public int[] keyCodes = {KeyEvent.VK_D, KeyEvent.VK_F, KeyEvent.VK_G, KeyEvent.VK_H, KeyEvent.VK_J};

    private final String CONFIG_NAME = "config.txt";

    public SaveSystem() {
        loadScores();
        loadConfig();
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

    private void loadConfig() {
        File configFile = new File(CONFIG_NAME);
        
        if (!configFile.exists()) {
            System.out.println("No config found, creating default...");
            saveConfig();
            return; 
        }

        try (BufferedReader br = new BufferedReader(new FileReader(configFile))) {
            String line;
            
            line = br.readLine();
            if (line != null && !line.isEmpty()) username = line.trim();

            line = br.readLine();
            if (line != null && !line.isEmpty()) volume = Integer.parseInt(line.trim());

            line = br.readLine();
            if (line != null && !line.isEmpty()) resWidth = Integer.parseInt(line.trim());

            line = br.readLine();
            if (line != null && !line.isEmpty()) resHeight = Integer.parseInt(line.trim());

            line = br.readLine();
            if (line != null && !line.isEmpty()) judgmentY = Integer.parseInt(line.trim());

            line = br.readLine();
            if (line != null && !line.isEmpty()) upscroll = Boolean.parseBoolean(line.trim());

            line = br.readLine();
            if (line != null && !line.isEmpty()) {
                String[] parts = line.trim().split(",");
                if (parts.length == keyCodes.length) {
                    int[] parsed = new int[parts.length];
                    for (int i = 0; i < parts.length; i++) parsed[i] = Integer.parseInt(parts[i].trim());
                    keyCodes = parsed;
                }
            }



            System.out.println("Config loaded successfully!");

        } catch (Exception e) {
            System.err.println("ERROR READING CONFIG FILE");
        }
    }

    private String joinKeys() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < keyCodes.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(keyCodes[i]);
        }
        return sb.toString();
    }

    public void saveConfig() {
        try (PrintWriter out = new PrintWriter(new FileWriter(CONFIG_NAME))) {
            out.println(username);
            out.println(volume);
            out.println(resWidth);  
            out.println(resHeight); 
            out.println(judgmentY); 
            out.println(upscroll);
            out.println(joinKeys()); 
        } catch (Exception e) {
            System.err.println("Could not save config!");
        }
    }
}

