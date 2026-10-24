import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class OsuConverter {
    private static final int LANES = 5;

    public static String convertOsuCore(File osuFile, File sourceDir) throws Exception {
        String title = osuFile.getName().replaceAll("(?i)\\.osu$", "")
                                        .replaceAll("[\\\\/:*?\"<>|]", "_");

        String audioFilename = "";
        double sliderMultiplier = 1.0;
        List<double[]> timing = new ArrayList<>();   
        List<String[]> hitObjects = new ArrayList<>();
        String section = "";

        try (BufferedReader reader = new BufferedReader(new FileReader(osuFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("//")) continue;

                if (line.startsWith("[") && line.endsWith("]")) {
                    section = line;
                    continue;
                }

                switch (section) {
                    case "[General]":
                        if (line.startsWith("AudioFilename:"))
                            audioFilename = line.substring(line.indexOf(':') + 1).trim();
                        break;
                    case "[Difficulty]":
                        if (line.startsWith("SliderMultiplier:"))
                            sliderMultiplier = Double.parseDouble(line.substring(line.indexOf(':') + 1).trim());
                        break;
                    case "[TimingPoints]": {
                        String[] p = line.split(",");
                        if (p.length >= 2) {
                            double beat = Double.parseDouble(p[1]);
                            boolean uninherited = p.length < 7 ? beat > 0 : p[6].trim().equals("1");
                            timing.add(new double[]{Double.parseDouble(p[0]), beat, uninherited ? 1 : 0});
                        }
                        break;
                    }
                    case "[HitObjects]":
                        hitObjects.add(line.split(","));
                        break;
                    default:
                        break;
                }
            }
        }

        if (audioFilename.isEmpty()) throw new IOException("No AudioFilename found in " + osuFile.getName());
        if (hitObjects.isEmpty()) throw new IOException("No hit objects found in " + osuFile.getName());

        timing.sort(Comparator.comparingDouble(a -> a[0])); 

        List<double[]> notes = new ArrayList<>();      
        for (String[] parts : hitObjects) {
            if (parts.length < 4) continue;

            int x = Integer.parseInt(parts[0].trim());
            double timeMs = Double.parseDouble(parts[2].trim());
            int type = Integer.parseInt(parts[3].trim());
            int lane = Math.min(LANES - 1, Math.max(0, (int) (x * LANES / 512.0)));
            double durationMs = 0;

            if ((type & 128) != 0 && parts.length >= 6) {
                double endMs = Double.parseDouble(parts[5].split(":")[0].trim());
                durationMs = endMs - timeMs;
            } else if ((type & 2) != 0 && parts.length >= 8) {
                int slides = Integer.parseInt(parts[6].trim());
                double length = Double.parseDouble(parts[7].trim());

                double beat = 500, sv = 1;
                for (double[] t : timing) {
                    if (t[0] > timeMs) break;
                    if (t[2] == 1) { beat = t[1]; sv = 1; }
                    else if (t[1] < 0) { sv = Math.max(0.1, Math.min(10, -100.0 / t[1])); }
                }
                durationMs = length * slides / (sliderMultiplier * 100.0 * sv) * beat;
            }
            notes.add(new double[]{timeMs / 1000.0, lane, Math.max(0, durationMs) / 1000.0});
        }
        notes.sort(Comparator.comparingDouble(a -> a[0]));

        File levelFolder = new File("Levels/" + title);
        levelFolder.mkdirs();

        File audioIn = new File(sourceDir, audioFilename);
        if (!audioIn.exists()) throw new FileNotFoundException("Audio file not found in map: " + audioFilename);
        convertToWav(audioIn, new File(levelFolder, "audio.wav"));

        try (PrintWriter w = new PrintWriter(new FileWriter(new File(levelFolder, "map.txt")))) {
            w.println("audio.wav");
            for (double[] n : notes) {
                w.println(String.format(Locale.US, "%.3f, %d, %.3f", n[0], (int) n[1], n[2]));
            }
        }
        return title;
    }

    private static void convertToWav(File in, File out) throws Exception {
        ProcessBuilder pb = new ProcessBuilder("ffmpeg", "-y", "-i", in.getAbsolutePath(),
                "-vn", "-ar", "44100", "-ac", "2", "-c:a", "pcm_s16le", out.getAbsolutePath());
        pb.redirectErrorStream(true);
        Process p;
        try {
            p = pb.start();
        } catch (IOException e) {
            throw new IOException("ffmpeg not found. Install ffmpeg and add it to your PATH "
                    + "(needed to turn the .mp3 into audio.wav).");
        }
        StringBuilder log = new StringBuilder();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String l;
            while ((l = r.readLine()) != null) log.append(l).append('\n'); // must drain or it can hang
        }
        if (p.waitFor() != 0 || !out.exists()) {
            throw new IOException("ffmpeg failed:\n" + log);
        }
    }
    
    public static List<File> extractOsz(File oszFile, File tempDir) throws IOException {
        tempDir.mkdirs();
        List<File> osuFiles = new ArrayList<>();
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(oszFile))) {
            ZipEntry e;
            while ((e = zis.getNextEntry()) != null) {
                File f = new File(tempDir, e.getName());
                if (!f.getCanonicalPath().startsWith(tempDir.getCanonicalPath()))
                    throw new IOException("Zip entry is outside of the target dir: " + e.getName());
                if (e.isDirectory()) { f.mkdirs(); continue; }

                f.getParentFile().mkdirs();
                try (FileOutputStream fos = new FileOutputStream(f)) {
                    byte[] buf = new byte[8192];
                    int len;
                    while ((len = zis.read(buf)) > 0) fos.write(buf, 0, len);
                }
                if (f.getName().toLowerCase().endsWith(".osu")) osuFiles.add(f);
            }
        }
        return osuFiles;
    }

    public static void convertOsuFile(File osuFile, File sourceDir) {
        try {
            String title = convertOsuCore(osuFile, sourceDir);
            JOptionPane.showMessageDialog(null, "Successfully converted: " + title + "\nLevel added to the menu!");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error converting level: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void handleOszFile(File oszFile) {
        File tempDir = new File("Levels/temp_osz");
        try {
            List<File> osuFiles = extractOsz(oszFile, tempDir);

            if (osuFiles.isEmpty()) {
                JOptionPane.showMessageDialog(null, "No .osu files found in the .osz archive.");
            } else if (osuFiles.size() == 1) {
                convertOsuFile(osuFiles.get(0), tempDir);
            } else {
                String[] options = new String[osuFiles.size()];
                for (int i = 0; i < options.length; i++) options[i] = osuFiles.get(i).getName();
                String selected = (String) JOptionPane.showInputDialog(null,
                        "Multiple difficulties found. Select one to convert:", "Select Difficulty",
                        JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                if (selected != null) {
                    for (File f : osuFiles) {
                        if (f.getName().equals(selected)) { convertOsuFile(f, tempDir); break; }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error extracting .osz: " + e.getMessage());
        } finally {
            deleteDirectory(tempDir);
        }
    }

    private static void deleteDirectory(File dir) {
        File[] all = dir.listFiles();
        if (all != null) for (File f : all) deleteDirectory(f);
        dir.delete();
    }

    public static void openConverterDialog() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select an .osu or .osz file");
        fileChooser.setFileFilter(new FileNameExtensionFilter("osu! Beatmaps (*.osu, *.osz)", "osu", "osz"));

        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            File selected = fileChooser.getSelectedFile();
            if (selected.getName().toLowerCase().endsWith(".osz")) handleOszFile(selected);
            else convertOsuFile(selected, selected.getParentFile());
        }
    }
}
