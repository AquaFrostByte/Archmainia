import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;

public class ImageButton extends JButton {
    private static final Color HOVER_TEXT = new Color(120, 220, 225);

    private static BufferedImage source;
    private static int capSrc = 1;
    private static boolean triedLoading = false;

    public static void reload(){
        triedLoading = false;
        source = null;
        capSrc = 1;
    }

    private static BufferedImage source() {
        if(!triedLoading) {
            triedLoading = true;
            File f = SkinManager.resolve("button.png");
            try {
                source = ImageIO.read(f);
                if (source != null) capSrc = detectCap(source);
            } catch (IOException e) {
                System.err.println("Could not load" + f.getPath() + ": " + e.getMessage());
            }
        }
        return source;
    }

    private static int detectCap(BufferedImage img) {
        int w = img.getWidth(), last = img.getHeight() - 1;
        int topMin = -1, topMax = -1, botMin = -1, botMax = -1;
        for (int x = 0; x < w; x++) {
            if (alpha(img, x, 0) > 128)    { if (topMin < 0) topMin = x; topMax = x; }
            if (alpha(img, x, last) > 128) { if (botMin < 0) botMin = x; botMax = x; }
        }

        if (topMin < 0 || botMin < 0) return 1;
        int slant = Math.max(Math.abs(botMin - topMin), Math.abs(botMax - topMax));
        return Math.max(1, Math.min(slant + 1, w / 2));
    }

    private static int alpha(BufferedImage img, int x, int y) {
        return (img.getRGB(x, y) >>> 24) & 0xFF;
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
                int y0 = 0, drawH = h;

                if (h >= sh * 2) {                                        // pixel art
                    int scale = Math.max(1, Math.min(Math.round(h / (float) sh), w / (2 * capSrc)));
                    drawH = sh * scale;
                    y0 = (h - drawH) / 2;
                    cap = capSrc * scale;
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                } else {                                                  // high-res art
                    cap = Math.min(w / 2, (int) Math.round(capSrc * h / (double) sh));
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                    g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                }

                g2.drawImage(src, cap, y0, w - cap, y0 + drawH, capSrc, 0, sw - capSrc, sh, null);  
                g2.drawImage(src, 0, y0, cap, y0 + drawH, 0, 0, capSrc, sh, null);                  
                g2.drawImage(src, w - cap, y0, w, y0 + drawH, sw - capSrc, 0, sw, sh, null);  
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