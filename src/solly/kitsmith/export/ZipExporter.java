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
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            for (KitSlot slot : kit.getAllSlots()) {
                byte[] wavBytes = AudioExporter.toWavBytes(slot.getAudio(), sampleRate);
                String fileName = slot.getId().replace(" ", "_") + ".wav";
                System.out.println("WAV size: " + wavBytes.length);
                zos.putNextEntry(new ZipEntry(fileName));
                zos.write(wavBytes);
                zos.closeEntry();
            }
        }
    }
}
