import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

public class UIConfig {
    
    public static Dimension LEVEL_BTN_SIZE;
    public static Dimension ACTION_BTN_SIZE;
    public static Dimension EXIT_BTN_SIZE;

    public static int MENU_USER_Y;    
    public static int MENU_TITLE_Y;   
    
    public static int MENU_LEVEL_START_X; 
    public static int MENU_LEVEL_START_Y; 
    public static int MENU_LEVEL_SPACING; 
    
    public static int MENU_SETTINGS_X;
    public static int MENU_SETTINGS_Y;
    public static int MENU_EXIT_X;
    public static int MENU_EXIT_Y;

    public static int GAME_RETRY_X;
    public static int GAME_RETRY_Y;
    public static int GAME_MENU_X;
    public static int GAME_MENU_Y;

    public static int JUDGMENT_Y;
    public static int PAUSE_TEXT_Y;
    public static int PAUSE_RESUME_X;
    public static int PAUSE_RESUME_Y;
    public static int PAUSE_MENU_X;
    public static int PAUSE_MENU_Y;
    
    public static int MENU_MANUAL_X;
    public static int MENU_MANUAL_Y;

    public static Font TITLE_FONT;
    public static Font SUBTITLE_FONT;
    public static Font LEVEL_BTN_FONT;
    public static Font ACTION_BTN_FONT;

    public static final Color TITLE_COLOR = Color.CYAN;
    public static final Color TEXT_LIGHT = Color.LIGHT_GRAY;
    public static final Color TEXT_WHITE = Color.WHITE;
    public static final Color SETTINGS_BG = Color.BLACK;
    public static final Color EXIT_BG = new Color(50, 0, 0);
    public static final Color EXIT_BG_M = new Color(150, 110, 40, 0);

    public static void updateForResolution(int width, int height) { //for some reason i cant look the ratio of thw window so there are 4 presets i calculated with the style exel sheet u can also edit stuff there it should al be linkened together.
        if (width >= 951) {
            LEVEL_BTN_SIZE = new Dimension(420, 50);
            ACTION_BTN_SIZE = new Dimension(300, 45);
            EXIT_BTN_SIZE = new Dimension(60, 60);
            
            MENU_USER_Y = 50;
            MENU_TITLE_Y = 80;
            MENU_LEVEL_START_X = 306; MENU_LEVEL_START_Y = 200; MENU_LEVEL_SPACING = 65;
            MENU_SETTINGS_X = 366;    MENU_SETTINGS_Y = 920;
            MENU_MANUAL_X   = 366; MENU_MANUAL_Y   = 980;
            MENU_EXIT_X = 739;        MENU_EXIT_Y = 13;

            JUDGMENT_Y = 1050; 

            PAUSE_TEXT_Y = 450;
            PAUSE_RESUME_X = 325; PAUSE_RESUME_Y = 550;
            PAUSE_MENU_X = 325;   PAUSE_MENU_Y = 620;

            // GAME POSITIONS
            GAME_RETRY_X = 325; GAME_RETRY_Y = 550;
            GAME_MENU_X = 325;  GAME_MENU_Y = 620;

            TITLE_FONT = new Font("Arial", Font.BOLD, 40);
            SUBTITLE_FONT = new Font("Arial", Font.ITALIC, 16);
            LEVEL_BTN_FONT = new Font("Arial", Font.BOLD, 18);
            ACTION_BTN_FONT = new Font("Arial", Font.BOLD, 16);
            
        } else if (width >= 760) {
            LEVEL_BTN_SIZE = new Dimension(336, 40);
            ACTION_BTN_SIZE = new Dimension(240, 35);
            EXIT_BTN_SIZE = new Dimension(48, 48);
            
            MENU_USER_Y = 40;
            MENU_TITLE_Y = 70;
            MENU_LEVEL_START_X = 245; MENU_LEVEL_START_Y = 160; MENU_LEVEL_SPACING = 55;
            MENU_SETTINGS_X = 293;    MENU_SETTINGS_Y = 736;
            MENU_MANUAL_X   = 293; MENU_MANUAL_Y   = 800;
            MENU_EXIT_X = 591;        MENU_EXIT_Y = 10;

            GAME_RETRY_X = 261; GAME_RETRY_Y = 450;
            GAME_MENU_X = 261;  GAME_MENU_Y = 500;

            JUDGMENT_Y = 850; 

            PAUSE_TEXT_Y = 350;
            PAUSE_RESUME_X = 261; PAUSE_RESUME_Y = 450;
            PAUSE_MENU_X = 261;   PAUSE_MENU_Y = 500;

            TITLE_FONT = new Font("Arial", Font.BOLD, 32);
            SUBTITLE_FONT = new Font("Arial", Font.ITALIC, 14);
            LEVEL_BTN_FONT = new Font("Arial", Font.BOLD, 14);
            ACTION_BTN_FONT = new Font("Arial", Font.BOLD, 14);
            
        } else if (width >= 634) {
            LEVEL_BTN_SIZE = new Dimension(280, 35);
            ACTION_BTN_SIZE = new Dimension(200, 30);
            EXIT_BTN_SIZE = new Dimension(40, 40);
            
            MENU_USER_Y = 30;
            MENU_TITLE_Y = 50;
            MENU_LEVEL_START_X = 204; MENU_LEVEL_START_Y = 130; MENU_LEVEL_SPACING = 45;
            MENU_SETTINGS_X = 244;    MENU_SETTINGS_Y = 613;
            MENU_MANUAL_X   = 244; MENU_MANUAL_Y   = 653;
            MENU_EXIT_X = 492;        MENU_EXIT_Y = 8;

            GAME_RETRY_X = 205; GAME_RETRY_Y = 350;
            GAME_MENU_X = 205;  GAME_MENU_Y = 390;

            JUDGMENT_Y = 680;

            PAUSE_TEXT_Y = 270;
            PAUSE_RESUME_X = 205; PAUSE_RESUME_Y = 350;
            PAUSE_MENU_X = 205;   PAUSE_MENU_Y = 390;

            TITLE_FONT = new Font("Arial", Font.BOLD, 26);
            SUBTITLE_FONT = new Font("Arial", Font.ITALIC, 12);
            LEVEL_BTN_FONT = new Font("Arial", Font.BOLD, 12);
            ACTION_BTN_FONT = new Font("Arial", Font.BOLD, 12);
            
        } else {
            LEVEL_BTN_SIZE = new Dimension(210, 25);
            ACTION_BTN_SIZE = new Dimension(150, 22);
            EXIT_BTN_SIZE = new Dimension(30, 30);
            
            MENU_USER_Y = 20;
            MENU_TITLE_Y = 40;
            MENU_LEVEL_START_X = 153; MENU_LEVEL_START_Y = 100; MENU_LEVEL_SPACING = 40;
            MENU_SETTINGS_X = 183;    MENU_SETTINGS_Y = 460;
            MENU_MANUAL_X   = 183; MENU_MANUAL_Y   = 490;
            MENU_EXIT_X = 369;        MENU_EXIT_Y = 6;

            GAME_RETRY_X = 148; GAME_RETRY_Y = 250;
            GAME_MENU_X = 148;  GAME_MENU_Y = 285;

            JUDGMENT_Y = 510;

            PAUSE_TEXT_Y = 180;
            PAUSE_RESUME_X = 148; PAUSE_RESUME_Y = 250;
            PAUSE_MENU_X = 148;   PAUSE_MENU_Y = 285;

            TITLE_FONT = new Font("Arial", Font.BOLD, 20);
            SUBTITLE_FONT = new Font("Arial", Font.ITALIC, 10);
            LEVEL_BTN_FONT = new Font("Arial", Font.BOLD, 10);
            ACTION_BTN_FONT = new Font("Arial", Font.BOLD, 10);
        }
    }
}