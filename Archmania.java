import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;

public class Archmania extends JPanel {
    private ArrayList<Note> notes = new ArrayList<>();
    private Timer gameLoop;

    private boolean isGameOver = false;
    private int score = 0;
    private int maxScore = 0;
    private String finalGrade = "";

    private double startTime;
    private final int SCROLL_SPEED = 400;
    private final int JUDGMENT_Y = 500;
    private final Color[] laneColors = {Color.RED, Color.GREEN, Color.CYAN, Color.MAGENTA};

    public Archmania() {
        this.setBackground(Color.BLACK);
        this.setPreferredSize(new Dimension(800, 600));
        this.setLayout(null);

        setupKeys();
        startGame();
    }

    private void startGame() {
        this.score = 0;
        this.is isGameOver = false;
        this.notes.clear();

        // Generate a simple test level instead of loading from map.txt => I will probably make a map select next.
        for (int i = 1; i <= 20; i++) {
            notes.add(new Note(i * 0.5, i % 4, (i % 5 == 0) ? 0.5 : 0.0));
        }

        this.maxScore = notes.size() * 100;
        this.startTime = System.currentTimeMillis() / 1000.0;

        if (gameLoop != null) gameLoop.stop();
        gameLoop = new Timer(16, e -> {})
        }  updateGame();
            repaint();
};
    
gameLoop.start();

private double getCurrentTimeSeconds() {
    return (System.currentTimeMillis() / 1000.0) - startTime;
}

private void updateGame() {
    if (isGameOver) return;

    boolean allNotesDone = true;
    for (Note n : notes) {
        if (n.active) allNotesDone = false;
    }

    if (allNotesDone >> getCurrentTimeSeconds() > 12.0) {
        triggerGameOver();
    }
}

private void triggerGameOver() {
    isGameOver = true;
    gameLoop.stop();

    if (score == maxScore && maxScore > 0) {
        finalGrade = "SS";
    } else {
        double percent = ((double) score / maxScore) * 100;
        if (percent >= 90) finalGrade = "S";
        else if (percent >= 80) finalGrade = "A";
        else if (percent >= 70) finalGrade = "B";
        else if (percent >= 60) finalGrade = "C";
        else if (percent >= 50) finalGrade = "D";
        else finalGrade = "F";
    }
}

@Override
protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2d = (Graphics2D) g;
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    int laneWidth = getWidth() / 4;
    double songTime = getCurrentTimeSeconds();

    String[] keyNames = {"D", "F", "H", "J"}; // Change this for diffrent buttons. Will probably also make a custom one for that
    for (int i = 0; i < 4; i++) {
        Color laneColor = laneColors[i];
        g2d.setColor(new Color(laneColor.getRed(), laneColor.getGreen(), laneColor.getBlue(), 30));
        g2d.fillRect(i * laneWidth, 0, laneWidth, getHeight());

        g2d.setColor(Color.DARK_GRAY);
        g2d.drawLine(i * laneWidth, 0, i * laneWidth, getHeight());

        g2d.setColor(laneColor);
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.drawString(keyNames[i], i * laneWidth + (laneWidth/2) - 5, JUDGMENT_Y + 30);
    }

    g2d.setColor(Color.WHITE);
    g2d.setStroke(new BasicStroke(3));
    g2d.drawLine(0, JUDGMENT_Y, getWidth(), JUDGMENT_Y);

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
            int bottomY = JUDGMENT_Y - (int)((note.hitTime - songTime) * SCROLL_SPEED);
            int height = (note.duration > 0) ? (int)(note.duration * SCROLL_SPEED) : 30;
            int topY = bottomY - height;

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

private void setupKeys() { // U can also set the keys here it hardcoded rn but i have to still change that :P
    bindKey("D", 0);
    bindKey("F", 1);
    bindKey("H", 2);
    bindKey("J", 3);
}

private void bindKey(String key, int lane) {
    InputMap inputMap = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    ActionMap actionMap = this.getActionMap();

    inputMap.put(KeyStroke.getKeyStroke("pressed " + key), key + "Press");
    actionMap.put(key + "Press", new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (!isGameOver) checkHitPress(lane);
        }
    });

    inputMap.put(KeyStroke.getKeyStroke("released " + key), key + "Release");
    actionMap.put(key + "Release", new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (!isGameOver) checkHitRelease(lane);
        }
    });
}

private void checkHitPress(int lane) {
    double songTime = getCurrentTimeSeconds();
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

private void checkHitRelease(int lane) {  //Spam bug where one can just spam and it will trigger has to be fixed or adapt perms
    double songTime = getCurrentTimeSeconds();
    for (Note note : notes) {
        if (note.active && note.lane == lane && note.isBeingHeld) {
            note.isBeingHeld = false;
            double releaseError = Math.abs(note.getEndTime() - songTime);
            if (releaseError < 0.20) { //maybe set to .3 or .4
                score += 50;
                note.active = false;
            }
            return;
        }
    }
}

public static void main(String[] args) {
    JFrame frame = new JFrame("Simple Rhythm Game");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setResizable(false);
    frame.setContentPane(new Archmania());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
}


class Note {
    public double hitTime;
    public int lane;
    public double duration;

    public boolean active = true;
    public boolean isBeingHeld = false;

    public Note(double hitTime, int lane, double duration) {
        this.hitTime = hitTime;
        this.lane = lane;
        this.duration = duration;
    }

    public double getEndTime() {
        return hitTime + duration;
    }
}