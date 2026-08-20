package solly.kitsmith.export;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class AudioExporter {

    public static void exportWav(float[] audio, String path, int sampleRate) throws IOException {
        if (audio == null || audio.length == 0) {
            throw new IOException("Audio data is empty");
        }

        System.out.println("Exporting WAV: " + path + ", samples: " + audio.length);

        AudioInputStream stream = toStream(audio, sampleRate);
        try {
            File file = new File(path);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            AudioSystem.write(stream, AudioFileFormat.Type.WAVE, file);
            System.out.println("WAV written: " + file.length() + " bytes");
        } finally {
            stream.close();
        }
    }

    public static byte[] toWavBytes(float[] audio, int sampleRate) throws IOException {
        if (audio == null || audio.length == 0) {
            System.err.println("ERROR: Audio data is empty");
            throw new IOException("Audio data is empty");
        }

        System.out.println("Converting to WAV bytes, samples: " + audio.length);

        AudioInputStream stream = toStream(audio, sampleRate);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            AudioSystem.write(stream, AudioFileFormat.Type.WAVE, baos);
            byte[] result = baos.toByteArray();
            System.out.println("WAV bytes generated: " + result.length + " bytes");
            if (result.length == 0) {
                throw new IOException("Generated WAV is empty");
            }
            return result;
        } catch (Exception e) {
            System.err.println("Error in toWavBytes: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
            stream.close();
        }
    }

    private static AudioInputStream toStream(float[] audio, int sampleRate) {
        AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);
        byte[] bytes = floatToPcm(audio);
        return new AudioInputStream(new ByteArrayInputStream(bytes), format, audio.length);
    }

    private static byte[] floatToPcm(float[] audio) {
        byte[] bytes = new byte[audio.length * 2];
        for (int i = 0; i < audio.length; i++) {
            float sample = Math.max(-1f, Math.min(1f, audio[i]));
            short s = (short) (sample * 32767);
            bytes[i * 2] = (byte) (s & 0xFF);
            bytes[i * 2 + 1] = (byte) ((s >> 8) & 0xFF);
        }
        return bytes;
    }
}