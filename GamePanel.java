import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import javax.swing.*;

public class GamePanel extends JPanel {
    private ArrayList<Note> notes = new ArrayList<>();
    private AudioManager audioManager = new AudioManager();
    private Timer gameLoop;

    private JButton playAgainBtn;
    private JButton menuBtn;

    private boolean isGameOver = false;
    private boolean isPaused = false;

    private JButton resumeBtn;
    private JButton pauseMenuBtn;
    private double lastNoteTime = 0.0;
    private int score = 0;
    private int maxScore = 0;
    private String currentLevelFile;
    private String finalGrade = "";

    private final int SCROLL_SPEED = 400;
    private int JUDGMENT_Y = UIConfig.JUDGMENT_Y;
    private final Color[] laneColors = {Color.RED, Color.GREEN, Color.CYAN, Color.MAGENTA};

    public GamePanel(String levelFile) {
        this.setBackground(Color.BLACK);
        this.setLayout(null);

        setupKeys();
        setupButtons();
        startGame(levelFile);
    }

    private void setupButtons() {
        int btnWidth = UIConfig.ACTION_BTN_SIZE.width;
        int btnHeight = UIConfig.ACTION_BTN_SIZE.height;

        playAgainBtn = new JButton("Retry");
        playAgainBtn.setBounds(UIConfig.GAME_RETRY_X, UIConfig.GAME_RETRY_Y, btnWidth, btnHeight);
        playAgainBtn.setVisible(false);
        playAgainBtn.setFocusPainted(false);
        playAgainBtn.setFont(UIConfig.ACTION_BTN_FONT);
        playAgainBtn.setBackground(UIConfig.SETTINGS_BG);
        playAgainBtn.setForeground(UIConfig.TEXT_WHITE);
        playAgainBtn.addActionListener(e -> startGame(currentLevelFile));
        this.add(playAgainBtn);

        menuBtn = new JButton("Back to Menu");
        menuBtn.setBounds(UIConfig.GAME_MENU_X, UIConfig.GAME_MENU_Y, btnWidth, btnHeight);
        menuBtn.setVisible(false);
        menuBtn.setFocusPainted(false);
        menuBtn.setFont(UIConfig.ACTION_BTN_FONT);
        menuBtn.setBackground(UIConfig.EXIT_BG);
        menuBtn.setForeground(UIConfig.TEXT_WHITE);
        menuBtn.addActionListener(e -> {
            audioManager.stopAndClose();
            Main.showMenu();
        });
        this.add(menuBtn);

        resumeBtn = new JButton("Resume");
        resumeBtn.setBounds(UIConfig.PAUSE_RESUME_X, UIConfig.PAUSE_RESUME_Y, btnWidth, btnHeight);
        resumeBtn.setVisible(false);
        resumeBtn.setFocusPainted(false);
        resumeBtn.setContentAreaFilled(false);
        resumeBtn.setBorderPainted(false);
        resumeBtn.setFont(UIConfig.ACTION_BTN_FONT);
        resumeBtn.setBackground(UIConfig.SETTINGS_BG);
        resumeBtn.setForeground(UIConfig.TEXT_WHITE);
        resumeBtn.addActionListener(e -> togglePause());
        this.add(resumeBtn);

        pauseMenuBtn = new JButton("Quit to Menu");
        pauseMenuBtn.setBounds(UIConfig.PAUSE_MENU_X, UIConfig.PAUSE_MENU_Y, btnWidth, btnHeight);
        pauseMenuBtn.setVisible(false);
        pauseMenuBtn.setFocusPainted(false);
        pauseMenuBtn.setContentAreaFilled(false);
        pauseMenuBtn.setBorderPainted(false);
        pauseMenuBtn.setFont(UIConfig.ACTION_BTN_FONT);
        pauseMenuBtn.setBackground(UIConfig.EXIT_BG);
        pauseMenuBtn.setForeground(UIConfig.TEXT_WHITE);
        pauseMenuBtn.addActionListener(e -> {
            audioManager.stopAndClose();
            Main.showMenu();
        });
        this.add(pauseMenuBtn);
    }

        private void startGame(String levelFile) {
        this.currentLevelFile = levelFile;
        this.score = 0;
        this.maxScore = 0;
        this.isGameOver = false;
        this.lastNoteTime = 0.0;
        this.notes.clear();

        playAgainBtn.setVisible(false);
        menuBtn.setVisible(false);

        loadLevel(levelFile);

        this.maxScore = notes.size() * 100;

        if (gameLoop != null) gameLoop.stop();
        gameLoop = new Timer(16, e -> {
            updateGame();
            if (isPaused) {
                repaint();
                return;
            }
            repaint();
        });
        gameLoop.start();
        }

    private void loadLevel(String folderPath) {
        String mapFile = folderPath + "/map.txt";
        try (BufferedReader br = new BufferedReader(new FileReader(mapFile))) {
            String songFileName = br.readLine().trim();
            audioManager.setVolume(Main.saveSystem.volume);
            audioManager.loadAndPlay(folderPath + "/" + songFileName);

            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                double time = Double.parseDouble(parts[0].trim());
                int lane = Integer.parseInt(parts[1].trim());

                double duration = (parts.length > 2) ? Double.parseDouble(parts[2].trim()) : 0.0;
                notes.add(new Note(time, lane, duration));
                if (time + duration > lastNoteTime) lastNoteTime = time + duration;
            }
        } catch (Exception e) {
            System.err.println("Could not load level folder: " + folderPath);
        }
    }

    private void updateGame() {
        if (isGameOver) return;
        if (audioManager.getCurrentTimeSeconds() > lastNoteTime + 2.5) {
            triggerGameOver();
        }
    }

    private void triggerGameOver() {
        isGameOver = true;
        gameLoop.stop();
        audioManager.stopAndClose();

        if (score == maxScore && maxScore > 0) {
            finalGrade = "SS";
        } else {
            double percent = ((double) score / maxScore) * 100; //this sucks btw
            if (percent >= 90) finalGrade = "S";
            else if (percent >= 80) finalGrade = "A";
            else if (percent >= 70) finalGrade = "B";
            else if (percent >= 60) finalGrade = "C";
            else if (percent >= 50) finalGrade = "D";
            else finalGrade = "F";
        }

        String levelName = new File(currentLevelFile).getName();
        Main.saveSystem.saveScore(levelName, score, finalGrade);

        playAgainBtn.setVisible(true);
        menuBtn.setVisible(true);
    }
}

// Set up the Ui for the playing feeld TLDR
// add the controll stuff