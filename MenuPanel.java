import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import javax.imageio.ImageIO;
import javax.swing.*;

public class MenuPanel extends JPanel {
    private BufferedImage backgroundImage;

    public MenuPanel() {
        try {
            backgroundImage = ImageIO.read(new File("background.png"));
        } catch (IOException e) {
            System.err.println("Could not load background image: " + e.getMessage());
        }

        this.setLayout(null);
        this.setOpaque(false);

        JLabel userLabel = new JLabel("Welcome, " + Main.saveSystem.username, SwingConstants.CENTER);
        userLabel.setFont(UIConfig.SUBTITLE_FONT);
        userLabel.setForeground(UIConfig.TEXT_LIGHT);
        userLabel.setBounds(0, UIConfig.MENU_USER_Y, Main.saveSystem.resWidth, 30);
        this.add(userLabel);

        JLabel title = new JLabel("SELECT A LEVEL", SwingConstants.CENTER);
        title.setFont(UIConfig.TITLE_FONT);
        title.setForeground(UIConfig.TITLE_COLOR);
        title.setBounds(0, UIConfig.MENU_TITLE_Y, Main.saveSystem.resWidth, 50);
        this.add(title);

        File levelsDir = new File("Levels");
        if (!levelsDir.exists()) levelsDir.mkdir();

        File[] folders = levelsDir.listFiles();
        int levelCount = 0;
        int currentLevelY = UIConfig.MENU_LEVEL_START_Y;

        if (folders != null) {
            for (File folder : folders) {
                if (folder.isDirectory()) {
                    levelCount++;
                    String levelName = folder.getName();
                    int score = Main.saveSystem.getHighScore(levelName);
                    String grade = Main.saveSystem.getGrade(levelNa);
                    JButton btn = new JButton(levelName + "  |  Best: " + score + " [" + grade + "]");
                    
                    btn.setFont(UIConfig.LEVEL_BTN_FONT);
                    btn.setFocusPainted(false);
                    btn.setBounds(UIConfig.MENU_LEVEL_START_X, currentLevelY, UIConfig.LEVEL_BTN_SIZE.width, UIConfig.LEVEL_BTN_SIZE.height);
                    btn.addActionListener(e -> Main.playGame(folder.getPath()));
                    this.add(btn);
                    currentLevelY += UIConfig.MENU_LEVEL_SPACING;
                }
            }
        }

        if (levelCount == 0) {
            JLabel warningLabel = new JLabel("No levels found!", SwingConstants.CENTER);
            warningLabel.setFont(new Font("Arial", Font.BOLD, 22));
            warningLabel.setForeground(Color.RED);
            warningLabel.setBounds(0, UIConfig.MENU_LEVEL_START_Y, Main.saveSystem.resWidth, 50);
            this.add(warningLabel);
        }

        JButton manualBtn = new JButton("📖 Game Manual");
        manualBtn.setFont(UIConfig.ACTION_BTN_FONT);
        manualBtn.setFocusPainted(false);
        manualBtn.setContentAreaFilled(false);
        manualBtn.setBorderPainted(false);
        manualBtn.setBackground(UIConfig.SETTINGS_BG);
        manualBtn.setForeground(UIConfig.TEXT_WHITE);
        manualBtn.setBounds(UIConfig.MENU_MANUAL_X, UIConfig.MENU_MANUAL_Y, UIConfig.ACTION_BTN_SIZE.width, UIConfig.ACTION_BTN_SIZE.height);

        manualBtn.addActionListener(e -> {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                try {
                    Desktop.getDesktop().browse(new URI("https://archmania.lain.onl/manual"));
                } catch (Exception ex) {
                    System.err.println("Could not open the link!");
                }
            }
        });
        this.add(manualBtn);

        JButton exitBtn = new JButton("") {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };

        exitBtn.setContentAreaFilled(false);
        exitBtn.setFont(UIConfig.ACTION_BTN_FONT);
        exitBtn.setFocusPainted(false);
        exitBtn.setBackground(UIConfig.EXIT_BG_M);
        exitBtn.setForeground(UIConfig.EXIT_BG_M);
        exitBtn.setBorderPainted(false);
        exitBtn.setBounds(UIConfig.MENU_EXIT_X, UIConfig.MENU_EXIT_Y, UIConfig.EXIT_BTN_SIZE.width, UIConfig.EXIT_BTN_SIZE.height);
        exitBtn.addActionListener(e -> System.exit(0));

        this.add(exitBtn);
    
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}
