import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class SkinManager {
    public static final String SKINS_DIR = "Skins";
    public static final String DEFAULT_DIR = "img";
    public static final String DEFAULT_NAME = "Default";

    public static final String[] ASSETS = {
        "button.png", "background.png", "cgirl.png", "duke1.png", "duke2.png", "duke3.png"
    };

    private static String active = "";

    public static void setActive(String name) {
        active = (name == null || name.equals(DEFAULT_NAME)) ? "" : name;
        ImageButton.reload();
    }

    public static String getActive() {
        return active.isEmpty() ? DEFAULT_NAME : active;
    }

    public static File resolve(String assetName) {
        if (!active.isEmpty()) {
            File f = new File(new File(SKINS_DIR, active), assetName);
            if (f.isFile()) return f;
        }
        return new File(DEFAULT_DIR, assetName);
    }

    public static List<String> listSkins() {
        List<String> out = new ArrayList<>();

        out.add(DEFAULT_NAME);
        File[] dirs = new File(SKINS_DIR).listFiles(File::isDirectory);
        if (dirs != null) {
            Arrays.sort(dirs, Comparator.comparing(d -> d.getName().toLowerCase()));
            for (File d : dirs) out.add(d.getName());
        }
        return out;
    }

    public static String install(File amsFile) throws IOException {
        String name = amsFile.getName().replaceAll("(?i)\\.ams$", "")
                                       .replaceAll("[\\\\/:*?\"<>|]", "_").trim();
        if (name.isEmpty() || name.equalsIgnoreCase(DEFAULT_NAME)) name = "skin_" + System.currentTimeMillis();

        Map<String, byte[]> found = new HashMap<>();
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(amsFile))) {
            ZipEntry e;
            while ((e = zis.getNextEntry()) != null) {
                if (e.isDirectory()) continue;
                String base = new File(e.getName()).getName();   // drops any path -> no zip-slip
                String match = null;
                for (String a : ASSETS) if (a.equalsIgnoreCase(base)) { match = a; break; }
                if (match == null) continue;

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] buf = new byte[8192];
                int len;
                while ((len = zis.read(buf)) > 0) bos.write(buf, 0, len);
                found.put(match, bos.toByteArray());
            }
        }

        if (found.isEmpty()) {
            throw new IOException("No skin images found. Expected any of: " + String.join(", ", ASSETS));
        }

        File dir = new File(SKINS_DIR, name);
        deleteDirectory(dir);
        if (!dir.mkdirs()) throw new IOException("Could not create " + dir.getPath());
        for (Map.Entry<String, byte[]> en : found.entrySet()) {
            try (FileOutputStream fos = new FileOutputStream(new File(dir, en.getKey()))) {
                fos.write(en.getValue());
            }
        }
        return name;
    }

    private static void deleteDirectory(File dir) {
        File[] all = dir.listFiles();
        if (all != null) for (File f : all) deleteDirectory(f);
        dir.delete();
    } 
}
