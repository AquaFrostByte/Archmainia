import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;

public class ImageButton extends JButton {
    private static final  String IMAGE_PATH = "img/button.png";
    private static final  int CAP = 30;

    private static final Color HOVER_TEXT = new Color(120, 220, 225);

    private static BufferedImage source;
    private static boolean triedLoading = false;

    private static BufferedImage source() {
        if(!triedLoading) {
            triedLoading = true;
            try {
                source = ImageIO.read(new File(IMAGE_PATH));
            } catch (IOException e) {
                System.err.println("Could not load" + IMAGE_PATH + ": " + e.getMessage());
            }
        }
        return source;
    }


    public ImageButton(String text) {
        super(text);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setRolloverEnabled(true);
        setForeground(Color.WHITE);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override 
    protected void paintComponent(Graphics g) {
        BufferedImage src = source();
        int w = getWidth(), h = getHeight();

        Graphics2D g2 = (Graphics2D) g.create();

        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
            int cap = 10;
            if (src != null) {
                int sw = src.getWidth(), sh = src.getHeight();
                cap = Math.min(w /2, (int) Math.round(CAP * h / (double) sh));
                g2.drawImage(src, cap, 0, w - cap, h, CAP, 0, sw - CAP, sh, null);
                g2.drawImage(src, 0, 0, cap, h, 0, 0, CAP, sh, null);
                g2.drawImage(src, w - cap, 0, w, h, sw - CAP, 0, sw, sh, null); 
            } else {
                g2.setColor(getBackground());

                g2.fillRect(0, 0, w, h);
            }

            String text = getText();
            if (text != null && !text.isEmpty()) {
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();

                int avail = w - 2 * cap - 8;
                if (fm.stringWidth(text) > avail) {
                    String t = text;
                    while (t.length() > 1 && fm.stringWidth(t + "\u2026") > avail) { //I love fancy 3 dots :3
                        t = t.substring(0, t.length() - 1);
                    }
                    text = t + "\u2026";
                }

                int x = (w - fm.stringWidth(text)) / 2;
                int y = (h - fm.getHeight()) / 2 + fm.getAscent();
                if (getModel().isPressed()) {x++;  y++; }

                g2.setColor(!isEnabled() ? Color.GRAY
                        : getModel().isRollover() ? HOVER_TEXT
                        : getForeground());
                g2.drawString(text, x, y);
            }
        } finally {
            g2.dispose();
        }
    }
}