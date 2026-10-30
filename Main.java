import java.awt.Color;
import javax.swing.JFrame;

public class Main {
    public static JFrame frame;
    public static SaveSystem saveSystem;

    public static void main(String[] args) {

        saveSystem = new SaveSystem();
        SkinManager.setActive(saveSystem.skin);

        frame = new JFrame("Arch Rhythm Hero"); //if u want to make any kind of mots for Archmaina or as i like to call it in prod Arch Rythem Hero do so here :3
        frame.setSize(saveSystem.resWidth, saveSystem.resHeight);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setResizable(false);

        frame.setLocationRelativeTo(null);

        UIConfig.updateForResolution(saveSystem.resWidth, saveSystem.resHeight);

        showMenu();

        frame.setUndecorated(true);
        frame.setBackground(new Color(0, 0, 0, 0));
        frame.getContentPane().setBackground(new Color(0, 0, 0, 0));

        frame.getRootPane().setDoubleBuffered(false);


        frame.setVisible(true);
    }

    public static void showMenu() {
        frame.setContentPane(new MenuPanel());
        frame.revalidate();
        frame.repaint();
    }

    public static void playGame(String levelFile) {
        frame.setContentPane(new GamePanel(levelFile));
        frame.revalidate();
        frame.repaint();
        frame.getContentPane().requestFocusInWindow();
    }
}
