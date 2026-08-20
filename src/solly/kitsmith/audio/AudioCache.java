package solly.kitsmith.audio;

import solly.kitsmith.KitSlot;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AudioCache {
    private static final AudioCache INSTANCE = new AudioCache();
    private final Map<KitSlot, float[]> cache = new ConcurrentHashMap<>();
    private final Map<KitSlot, float[]> peakCache = new ConcurrentHashMap<>();

    private AudioCache() {}

    public static AudioCache getInstance() {
        return INSTANCE;
    }

    public float[] getAudio(KitSlot slot) {
        return cache.computeIfAbsent(slot, s -> s.getAudio());
    }

    public float[] getPeaks(KitSlot slot, int barCount) {
        String key = slot.getId() + "_" + barCount;
        float[] peaks = peakCache.get(slot);
        if (peaks == null || peaks.length != barCount) {
            peaks = computePeaks(slot.getAudio(), barCount);
            peakCache.put(slot, peaks);
        }
        return peaks;
    }

    private float[] computePeaks(float[] audio, int barCount) {
        if (audio == null || audio.length == 0) {
            return new float[barCount];
        }
        int segment = Math.max(1, audio.length / barCount);
        float[] peaks = new float[barCount];
        float maxPeak = 0.0001f;

        for (int b = 0; b < barCount; b++) {
            int start = b * segment;
            int end = Math.min(audio.length, start + segment);
            float peak = 0f;
            for (int i = start; i < end; i++) {
                float a = Math.abs(audio[i]);
                if (a > peak) peak = a;
            }
            peaks[b] = peak;
            if (peak > maxPeak) maxPeak = peak;
        }

        for (int b = 0; b < barCount; b++) {
            peaks[b] = peaks[b] / maxPeak;
        }
        return peaks;
    }

    public void clear() {
        cache.clear();
        peakCache.clear();
    }

    public void invalidate(KitSlot slot) {
        cache.remove(slot);
        peakCache.remove(slot);
    }
}