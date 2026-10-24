import java.io.File;
import javax.sound.sampled.*;

public class AudioManager {
    private Clip clip;
    private int currentVolume = 80; 
    private long pausePosition = 0; 

    public void loadAndPlay(String filePath) {
        stopAndClose(); 
        try {
            File audioFile = new File(filePath);
            AudioInputStream stream = AudioSystem.getAudioInputStream(audioFile);
            clip = AudioSystem.getClip();
            clip.open(stream);
            
            applyVolume(); 
            
            pausePosition = 0;
            clip.start();
        } catch (Exception e) {
            System.err.println("Could not load audio file: " + filePath);
        }
    }

    public void pause() {
        if (clip != null && clip.isRunning()) {
            pausePosition = clip.getMicrosecondPosition(); 
            clip.stop();
        }
    }

    public void resume() {
        if (clip != null && !clip.isRunning()) {
            clip.setMicrosecondPosition(pausePosition);
            clip.start();
        }
    }

    public double getCurrentTimeSeconds() {
        if (clip == null) return 0.0;
        return clip.getMicrosecondPosition() / 1_000_000.0;
    }

    public void stopAndClose() {
        if (clip != null && clip.isOpen()) {
            clip.stop();
            clip.close();
        }
    }

    public void setVolume(int percent) {
        this.currentVolume = percent;
        applyVolume();
    }

    private void applyVolume() {
        if (clip != null && clip.isOpen()) {
            try {
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                
                if (currentVolume <= 0) {
                    gainControl.setValue(gainControl.getMinimum()); 
                } else {
                    float db = (float) (20f * Math.log10(currentVolume / 100f));
                    gainControl.setValue(db);
                }
            } catch (IllegalArgumentException e) {
                System.err.println("Volume control not supported on this OSS line.");
            }
        }
    }
}
