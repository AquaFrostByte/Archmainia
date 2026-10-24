import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
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
    private final boolean upscroll = Main.saveSystem.upscroll;
    private final int LANES = 5;
    private final Color[] laneColors = {Color.RED, Color.GREEN, Color.YELLOW, Color.CYAN, Color.MAGENTA};

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

        playAgainBtn = new ImageButton("Retry");
        playAgainBtn.setBounds(UIConfig.GAME_RETRY_X, UIConfig.GAME_RETRY_Y, btnWidth, btnHeight);
        playAgainBtn.setVisible(false);
        playAgainBtn.setFocusPainted(false);
        playAgainBtn.setFont(UIConfig.ACTION_BTN_FONT);
        playAgainBtn.setBackground(UIConfig.SETTINGS_BG);
        playAgainBtn.setForeground(UIConfig.TEXT_WHITE);
        playAgainBtn.addActionListener(e -> startGame(currentLevelFile));
        this.add(playAgainBtn);

        menuBtn = new ImageButton("Back to Menu");
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

        resumeBtn = new ImageButton("Resume");
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

        pauseMenuBtn = new ImageButton("Quit to Menu");
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

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int laneWidth = getWidth() / LANES;
        double songTime = audioManager.getCurrentTimeSeconds();

        int judgY = judgmentY();
        for (int i = 0; i < LANES; i++) { //mabey i will up it to 5 lines instad of 4
            Color laneColor = laneColors[i];
            g2d.setColor(new Color(laneColor.getRed(), laneColor.getGreen(), laneColor.getBlue(), 30));
            g2d.fillRect(i * laneWidth, 0, laneWidth, getHeight());

            g2d.setColor(Color.DARK_GRAY);
            g2d.drawLine(i * laneWidth, 0, i * laneWidth, getHeight());

            g2d.setColor(laneColor);
            g2d.setFont(new Font("Arial", Font.BOLD, 20));
            String keyName = KeyEvent.getKeyText(Main.saveSystem.keyCodes[i]);
            int textW = g2d.getFontMetrics().stringWidth(keyName);
            int labelY = upscroll ? judgY - 12 : judgY + 30;   // labels sit outside the note area
            g2d.drawString(keyName, i * laneWidth + (laneWidth - textW) / 2, labelY);
        }

        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawLine(0, judgY, getWidth(), judgY);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.drawString("Score: " + score, 10, 30);

        if (isGameOver) {
            g2d.setFont(new Font("Arial", Font.BOLD, 40));
            g2d.setColor(Color.YELLOW);
            g2d.drawString("GRADE: " + finalGrade, 140, 200);
            return;
        }

        for (Note note : notes) {
            if (note.active) {
                int offset = (int)((note.hitTime - songTime) * SCROLL_SPEED);
                int height = (note.duration > 0) ? (int)(note.duration * SCROLL_SPEED) : 30;
                int topY, bottomY;
                if (upscroll) {
                    topY = judgY + offset;
                    bottomY = topY + height;
                } else {
                    bottomY = judgY - offset;
                    topY = bottomY - height;
                }

                if (bottomY > -50 && topY < getHeight() + 50) {
                    if (note.isBeingHeld) {
                        g2d.setColor(Color.WHITE);
                    } else {
                        g2d.setColor(laneColors[note.lane]);
                    }
                    g2d.fillRect(note.lane * laneWidth + 10, topY, laneWidth - 20, height);
                }

                if (!note.isBeingHeld && songTime > note.getEndTime() + 0.2) {
                    note.active = false;
                }
            }
        }
    }

    private int judgmentY() {
        return upscroll ? getHeight() - JUDGMENT_Y : JUDGMENT_Y;
    }

    @Override 
    protected void paintChildren(Graphics g) {
        if (isPaused) {
            g.setColor(new Color(0, 0, 0, 170));
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        super.paintChildren(g);
    }

    private void setupKeys() {
        for (int lane = 0; lane < LANES; lane++) {
            bindKey(Main.saveSystem.keyCodes[lane], lane);
        }
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "pause");
        this.getActionMap().put("pause", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isGameOver) {
                    togglePause();
                }
            }
        });
    }

    private void togglePause() {
        isPaused = !isPaused;
        if (isPaused) {
            audioManager.pause();
            resumeBtn.setVisible(true);
            pauseMenuBtn.setVisible(true);
        } else {
            audioManager.resume();
            resumeBtn.setVisible(false);
            pauseMenuBtn.setVisible(false);
            this.requestFocusInWindow();
        }
    }

    private void bindKey(int keyCode, int lane) {
        InputMap inputMap = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = this.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(keyCode, 0, false), "lane" + lane + "Press");
        actionMap.put("lane" + lane + "Press", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isGameOver) checkHitPress(lane);
            }
        });

        inputMap.put(KeyStroke.getKeyStroke(keyCode, 0, true), "lane" + lane + "Release");
        actionMap.put("lane" + lane + "Release", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isGameOver) checkHitRelease(lane);
            }
        });
    }

    private void checkHitPress(int lane) {
        double songTime = audioManager.getCurrentTimeSeconds();
        for (Note note : notes) {
            if (note.active && note.lane == lane && !note.isBeingHeld) {
                double error = Math.abs(note.hitTime - songTime);
                if (error < 0.15) {
                    score += 50;
                    if (note.duration == 0) {
                        note.active = false;
                        score += 50;
                    } else {
                        note.isBeingHeld = true;
                    }
                    return;
                }
            }
        }
    }

    private void checkHitRelease(int lane) {
        double songTime = audioManager.getCurrentTimeSeconds();
        for (Note note : notes) {
            if (note.active && note.lane == lane && note.isBeingHeld) {
                note.isBeingHeld = false;
                double releaseError = Math.abs(note.getEndTime() - songTime);
                if (releaseError < 0.20) {
                    score += 50;
                    note.active = false;
                }
                return;
            }
        }
    }
}
