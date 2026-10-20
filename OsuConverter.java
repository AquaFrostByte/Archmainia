import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class OsuConverter {

    public static void convertOsuFile(File osuFile, File sourceDir) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(osuFile));
            String line;
            String audioFilename = "";
            String title = osuFile.getName().replace(".osu", "");
            boolean inHitObjects = false;

            File levelFolder = new File("Levels/" + title);
            if (!levelFolder.exists()) {
                levelFolder.mkdirs();
            }

            File mapFile = new File(levelFolder, "map.txt");
            PrintWriter writer = new PrintWriter(new FileWriter(mapFile));

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                if (line.startsWith("AudioFilename:")) {
                    audioFilename = line.substring(line.indexOf(":") + 1).trim();
                    writer.println(audioFilename);
                    
                    File audioFile = new File(sourceDir, audioFilename);
                    if (audioFile.exists()) {
                        Files.copy(audioFile.toPath(), new File(levelFolder, audioFilename).toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                } else if (line.equals("[HitObjects]")) {
                    if (audioFilename.isEmpty()) {
                        writer.println("audio.mp3");
                    }
                    inHitObjects = true;
                } else if (inHitObjects && !line.isEmpty()) {
                    String[] parts = line.split(",");
                    if (parts.length >= 6) {
                        int x = Integer.parseInt(parts[0]);
                        double time = Double.parseDouble(parts[2]) / 1000.0;
                        int type = Integer.parseInt(parts[3]);
                        
                        int lane = Math.min(3, Math.max(0, (int)(x / 128.0)));
                        double duration = 0.0;
                        
                        if ((type & 128) != 0) { 
                            String[] extra = parts[5].split(":");
                            double endTime = Double.parseDouble(extra[0]) / 1000.0;
                            duration = endTime - time;
                        }
                        
                        if (duration > 0) {
                            writer.println(time + "," + lane + "," + duration); //TS aint working 
                        } else {
                            writer.println(time + "," + lane);
                        }
                    }
                }
            }
            writer.close();
            reader.close();
            
            JOptionPane.showMessageDialog(null, "Successfully converted: " + title + "\nLevel added to the menu!");
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error converting level: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public static void handleOszFile(File oszFile) {
        File tempDir = new File("Levels/temp_osz");
        if (!tempDir.exists()) tempDir.mkdirs();
        
        List<File> osuFiles = new ArrayList<>();
        
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(oszFile))) {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                File newFile = new File(tempDir, zipEntry.getName());
                if (!newFile.getCanonicalPath().startsWith(tempDir.getCanonicalPath())) {
                    throw new IOException("Zip entry is outside of the target dir: " + zipEntry.getName());
                }
                
                if (zipEntry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("Failed to create directory " + newFile);
                    }
                } else {
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + parent);
                    }
                    
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                    
                    if (newFile.getName().toLowerCase().endsWith(".osu")) {
                        osuFiles.add(newFile);
                    }
                }
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error extracting .osz: " + e.getMessage());
            deleteDirectory(tempDir);
            return;
        }
        
        if (osuFiles.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No .osu files found in the .osz archive.");
        } else if (osuFiles.size() == 1) {
            convertOsuFile(osuFiles.get(0), tempDir);
        } else {
            String[] options = new String[osuFiles.size()];
            for (int i = 0; i < osuFiles.size(); i++) {
                options[i] = osuFiles.get(i).getName();
            }
            String selected = (String) JOptionPane.showInputDialog(null, 
                    "Multiple difficulties found. Select one to convert:", 
                    "Select Difficulty", JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
            
            if (selected != null) {
                for (File f : osuFiles) {
                    if (f.getName().equals(selected)) {
                        convertOsuFile(f, tempDir);
                        break;
                    }
                }
            }
        }
        
        deleteDirectory(tempDir);
    }
    
    private static void deleteDirectory(File dir) {
        File[] allContents = dir.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        dir.delete();
    }

    public static void openConverterDialog() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select an .osu or .osz file (osu!mania 4K)");
        fileChooser.setFileFilter(new FileNameExtensionFilter("osu! Beatmaps (*.osu, *.osz)", "osu", "osz"));
        
        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (selectedFile.getName().toLowerCase().endsWith(".osz")) {
                handleOszFile(selectedFile);
            } else {
                convertOsuFile(selectedFile, selectedFile.getParentFile());
            }
        }
    }
}
