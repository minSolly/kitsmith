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
        AudioInputStream stream = toStream(audio, sampleRate);
        AudioSystem.write(stream, AudioFileFormat.Type.WAVE, new File(path));
        stream.close();
    }

    public static byte[] toWavBytes(float[] audio, int sampleRate) throws IOException {
        AudioInputStream stream = toStream(audio, sampleRate);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        AudioSystem.write(stream, AudioFileFormat.Type.WAVE, baos);
        stream.close();
        return baos.toByteArray();
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
