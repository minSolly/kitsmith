package solly.kitsmith.dsp;

public final class AudioBuffer {

    private AudioBuffer() {
    }

    public static void normalize(float[] buffer, float targetPeak) {
        float peak = 0f;
        for (float v : buffer) {
            float a = Math.abs(v);
            if (a > peak) peak = a;
        }
        if (peak < 1e-6f) return;
        float gain = targetPeak / peak;
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] *= gain;
        }
    }

    public static void clip(float[] buffer, float limit) {
        for (int i = 0; i < buffer.length; i++) {
            if (buffer[i] > limit) buffer[i] = limit;
            if (buffer[i] < -limit) buffer[i] = -limit;
        }
    }

    public static void gain(float[] buffer, float amount) {
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] *= amount;
        }
    }

    public static void mixInto(float[] target, float[] source, float amount, int offset) {
        for (int i = 0; i < source.length; i++) {
            int idx = i + offset;
            if (idx >= 0 && idx < target.length) {
                target[idx] += source[i] * amount;
            }
        }
    }

    public static void fadeOutTail(float[] buffer, int tailSamples) {
        int start = Math.max(0, buffer.length - tailSamples);
        int len = buffer.length - start;
        for (int i = 0; i < len; i++) {
            float t = (float) i / len;
            buffer[start + i] *= (1f - t);
        }
    }

    public static float peak(float[] buffer) {
        float peak = 0f;
        for (float v : buffer) {
            float a = Math.abs(v);
            if (a > peak) peak = a;
        }
        return peak;
    }
}
