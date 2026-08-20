package solly.kitsmith.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.ByteArrayInputStream;

public class AudioEngine {

    private static final AudioEngine INSTANCE = new AudioEngine();
    private Clip activeClip;

    private AudioEngine() {
    }

    public static AudioEngine getInstance() {
        return INSTANCE;
    }

    public synchronized void play(float[] audio, int sampleRate) {
        stop();
        if (audio == null || audio.length == 0) return;
        try {
            AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);
            byte[] bytes = floatToPcm(audio);
            AudioInputStream stream = new AudioInputStream(
                    new ByteArrayInputStream(bytes), format, audio.length);
            activeClip = AudioSystem.getClip();
            activeClip.open(stream);
            activeClip.start();
        } catch (Exception ignored) {
        }
    }

    public synchronized void stop() {
        if (activeClip != null) {
            try {
                if (activeClip.isRunning()) activeClip.stop();
                activeClip.close();
            } catch (Exception ignored) {
            }
            activeClip = null;
        }
    }

    private byte[] floatToPcm(float[] audio) {
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
