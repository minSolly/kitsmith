package solly.kitsmith.export;

import solly.kitsmith.Kit;
import solly.kitsmith.KitSlot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipExporter {

    public static void exportKit(Kit kit, File zipFile, int sampleRate) throws IOException {
        System.out.println("Target file: " + zipFile.getAbsolutePath());

        String path = zipFile.getAbsolutePath();
        if (!path.toLowerCase().endsWith(".zip")) {
            zipFile = new File(path + ".zip");
            System.out.println("Added .zip extension: " + zipFile.getAbsolutePath());
        }

        File parentDir = zipFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            System.out.println("Creating parent directory: " + parentDir.getAbsolutePath());
            parentDir.mkdirs();
        }

        if (kit == null || kit.isEmpty()) {
            throw new IOException("Kit is empty or null");
        }

        System.out.println("Kit name: " + kit.getName());
        System.out.println("Total slots: " + kit.getAllSlots().size());

        
        try (FileOutputStream fos = new FileOutputStream(zipFile);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            int exportedCount = 0;

            for (KitSlot slot : kit.getAllSlots()) {
                try {
                    System.out.println("Processing slot: " + slot.getId());

                    float[] audio = slot.getAudio();
                    if (audio == null || audio.length == 0) {
                        System.err.println("ERROR: Empty audio for: " + slot.getId());
                        continue;
                    }

                    System.out.println("  Audio length: " + audio.length + " samples");

                    byte[] wavBytes = AudioExporter.toWavBytes(audio, sampleRate);
                    if (wavBytes == null || wavBytes.length == 0) {
                        System.err.println("ERROR: Empty WAV bytes for: " + slot.getId());
                        continue;
                    }

                    System.out.println("  WAV size: " + wavBytes.length + " bytes");

                    String fileName = slot.getId().replace(" ", "_") + ".wav";
                    System.out.println("  Adding to ZIP: " + fileName);

                    ZipEntry entry = new ZipEntry(fileName);
                    zos.putNextEntry(entry);
                    zos.write(wavBytes);
                    zos.closeEntry();
                    zos.flush();
                    exportedCount++;

                } catch (Exception e) {
                    System.err.println("ERROR exporting slot: " + slot.getId());
                    e.printStackTrace();
                }
            }

            if (exportedCount == 0) {
                throw new IOException("No sounds were exported successfully");
            }

            System.out.println("Exported " + exportedCount + " sounds");
            System.out.println("ZIP file size: " + zipFile.length() + " bytes");

        } catch (Exception e) {
            System.err.println("FATAL ERROR in ZipExporter:");
            e.printStackTrace();
            throw e;
        }
    }
}