import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.net.URI;
import javax.imageio.ImageIO;
import javax.swing.*;

public class MenuPanel extends JPanel {

    private BufferedImage backgroundImage;
    private List<FloatingElement> floaters;
    private Timer animationTimer;
    private BufferedImage frameBuffer;

    private static BufferedImage scaleImage(BufferedImage src, int targetW, int targetH) {
        if (src == null || targetW <= 0 || targetH <= 0) return src;
        BufferedImage current = src;
        int w = src.getWidth(), h = src.getHeight();
        while (w > targetW || h > targetH) {
            w = Math.max(targetW, w / 2);
            h = Math.max(targetH, h / 2);
            BufferedImage tmp = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = tmp.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.drawImage(current, 0, 0, w, h, null);
            g2.dispose();
            current = tmp;
        }
        if (w != targetW || h != targetH) {
            BufferedImage tmp = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = tmp.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.drawImage(current, 0, 0, targetW, targetH, null);
            g2.dispose();
            current = tmp;
        }
        return current;
    }

    private static BufferedImage rotate(BufferedImage src, double degrees) {
            if (src == null || degrees == 0) return src;
            int w = src.getWidth(), h = src.getHeight();
            int d = (int) Math.ceil(Math.hypot(w, h)) + 2;
            BufferedImage out = new BufferedImage(d, d, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = out.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.rotate(Math.toRadians(degrees), d / 2.0, d / 2.0);
            g2.drawImage(src, (d - w) / 2, (d - h) / 2, null);
            g2.dispose();
            return out;
    }



    private class FloatingElement {
        BufferedImage image;
        int x;
        double baseY, currentY;
        int direction = 1;
        double speed = 0.4;
        int hoverLimit;

        public FloatingElement(String imagePath, double centerX, double centerY, double designWidth, double tiltDegrees) {
            double f = UIConfig.scaleFactor;
            int w = (int) Math.round(designWidth * f);
            this.hoverLimit = (int) Math.round(30 * f);
            try {
                BufferedImage src = ImageIO.read(new File(imagePath));
                int h = (int) Math.round(w * (double) src.getHeight() / src.getWidth());

                image = rotate(scaleImage(src, w, h), tiltDegrees);

                this.x = (int) Math.round(centerX * f - image.getWidth() / 2.0);
                this.baseY = centerY * f - image.getHeight() / 2.0;
                this.currentY = this.baseY;
            } catch (IOException e) {
                System.err.println("Could not load " + imagePath);
            }
        }

        public void update() {
            currentY += (speed * direction);
            if (currentY >= baseY + hoverLimit) {
                direction = -1; 
            } else if (currentY <= baseY - hoverLimit) {
                direction = 1; 
            }
        }
        
        public void draw(Graphics g) {
            if (image != null) {
                g.drawImage(image, x, (int) Math.round(currentY), null);
            }
        }
    }

    @Override
    public void removeNotify() {
        if (animationTimer != null) animationTimer.stop();
        super.removeNotify();
    }

    public MenuPanel() {

        try {
            backgroundImage = scaleImage(ImageIO.read(new File("img/background.png")),
                    Main.saveSystem.resWidth, Main.saveSystem.resHeight);
        } catch (IOException e) {
            System.err.println("Could not load background: " + e.getMessage());
        }

        floaters = new ArrayList<>();

        floaters.add(new FloatingElement("img/cgirl.png", 842, 2196, 1689,   0));
        floaters.add(new FloatingElement("img/duke1.png", 440,  711,  479, -24));
        floaters.add(new FloatingElement("img/duke2.png", 1598, 661,  575,   8));
        floaters.add(new FloatingElement("img/duke3.png", 1680, 1134, 466,   0));

        floaters.get(1).direction = -1;
        floaters.get(3).direction = -1;

        animationTimer = new Timer(16, e -> {
            for (FloatingElement floater : floaters) {
                floater.update();
            }
            repaint();
        });
        animationTimer.start();

        this.setLayout(null);

        this.setDoubleBuffered(false);

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
                    String grade = Main.saveSystem.getGrade(levelName);
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

        JButton settingsBtn = new JButton("Settings");
            settingsBtn.setFont(UIConfig.ACTION_BTN_FONT);
            settingsBtn.setFocusPainted(false);
            settingsBtn.setBackground(UIConfig.SETTINGS_BG);
            settingsBtn.setForeground(UIConfig.TEXT_WHITE);
            settingsBtn.setBounds(UIConfig.MENU_SETTINGS_X, UIConfig.MENU_SETTINGS_Y, UIConfig.ACTION_BTN_SIZE.width, UIConfig.ACTION_BTN_SIZE.height);
            settingsBtn.addActionListener(e -> openSettingsMenu());
            this.add(settingsBtn);
        }

    @Override
    public boolean isOpaque() {
        return true;
    }

    @Override
    public void paint(Graphics g) {
        int w = getWidth(), h = getHeight();
        if (w <= 0 || h <= 0) return;

        if (frameBuffer == null || frameBuffer.getWidth() != w || frameBuffer.getHeight() != h) {
            frameBuffer = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        }

        Graphics2D b = frameBuffer.createGraphics();
        b.setComposite(AlphaComposite.Clear);
        b.fillRect(0, 0, w, h);
        b.setComposite(AlphaComposite.SrcOver);

        if (backgroundImage != null) {
            b.drawImage(backgroundImage, 0, 0, null);
        }
        for (FloatingElement floater : floaters) {
            floater.draw(b);
        }
        paintChildren(b);
        b.dispose();

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setComposite(AlphaComposite.Src);
        g2.drawImage(frameBuffer, 0, 0, null);
        g2.dispose();

        Toolkit.getDefaultToolkit().sync();
    }

    private void openSettingsMenu() {
        JDialog settingsDialog = new JDialog(Main.frame, "Settings", true);
        settingsDialog.setSize(350, 350);
        settingsDialog.setLocationRelativeTo(this);
        settingsDialog.getContentPane().setBackground(new Color(40, 40, 40));
        settingsDialog.setLayout(new BoxLayout(settingsDialog.getContentPane(), BoxLayout.Y_AXIS));

        JLabel nameLabel = new JLabel("Username:");
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField nameField = new JTextField(Main.saveSystem.username, 15);
        nameField.setMaximumSize(new Dimension(200, 30));
        nameField.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel volLabel = new JLabel("Volume:");
        volLabel.setForeground(Color.WHITE);
        volLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JSlider volSlider = new JSlider(0, 100, Main.saveSystem.volume);
        volSlider.setBackground(new Color(40, 40, 40));
        volSlider.setForeground(Color.WHITE);
        volSlider.setMajorTickSpacing(25);
        volSlider.setPaintTicks(true);
        volSlider.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel resLabel = new JLabel("Resolution:");
        resLabel.setForeground(Color.WHITE);
        resLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        String[] resOptions = {
            "951 x 1247 (Original)",
            "760 x 998",
            "634 x 831",
            "475 x 623"
        };
        JComboBox<String> resDropdown = new JComboBox<>(resOptions);
        resDropdown.setMaximumSize(new Dimension(200, 30));
        resDropdown.setAlignmentX(Component.CENTER_ALIGNMENT);

        if (Main.saveSystem.resWidth == 760) resDropdown.setSelectedIndex(1);
        else if (Main.saveSystem.resWidth == 634) resDropdown.setSelectedIndex(2);
        else if (Main.saveSystem.resWidth == 475) resDropdown.setSelectedIndex(3);
        else resDropdown.setSelectedIndex(0);

        JButton saveBtn = new JButton("Save & Apply Settings");
        saveBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        saveBtn.addActionListener(e -> {
            Main.saveSystem.username = nameField.getText();
            Main.saveSystem.volume = volSlider.getValue();

            int selectedRes = resDropdown.getSelectedIndex();
            switch(selectedRes) {
                case 0: Main.saveSystem.resWidth = 951; Main.saveSystem.resHeight = 1247; break;
                case 1: Main.saveSystem.resWidth = 760; Main.saveSystem.resHeight = 998; break;
                case 2: Main.saveSystem.resWidth = 634; Main.saveSystem.resHeight = 831; break;
                case 3: Main.saveSystem.resWidth = 475; Main.saveSystem.resHeight = 623; break;
            }

            UIConfig.updateForResolution(Main.saveSystem.resWidth, Main.saveSystem.resHeight);

            Main.frame.setSize(Main.saveSystem.resWidth, Main.saveSystem.resHeight);
            Main.frame.setLocationRelativeTo(null);

            Main.saveSystem.saveConfig();
            settingsDialog.dispose();
            Main.showMenu();
        });

        settingsDialog.add(Box.createVerticalStrut(15));
        settingsDialog.add(nameLabel);
        settingsDialog.add(nameField);
        settingsDialog.add(Box.createVerticalStrut(15));
        settingsDialog.add(volLabel);
        settingsDialog.add(volSlider);
        settingsDialog.add(Box.createVerticalStrut(15));
        settingsDialog.add(resLabel);
        settingsDialog.add(resDropdown);
        settingsDialog.add(Box.createVerticalStrut(20));
        settingsDialog.add(saveBtn);

        settingsDialog.setVisible(true);
}
}
