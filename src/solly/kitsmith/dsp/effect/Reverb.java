package solly.kitsmith.dsp.effect;

public class Reverb implements Effect {

    private static final int[] COMB_TUNING_MS = {29, 37, 41, 43};
    private static final int[] ALLPASS_TUNING_MS = {5, 1};

    private final float roomSize;
    private final float damping;
    private final float mix;
    private final int tailMs;

    public Reverb(float roomSize, float damping, float mix, int tailMs) {
        this.roomSize = roomSize;
        this.damping = damping;
        this.mix = mix;
        this.tailMs = tailMs;
    }

    @Override
    public float[] apply(float[] input, int sampleRate) {
        int tailSamples = sampleRate * tailMs / 1000;
        int totalLength = input.length + tailSamples;
        float[] extended = new float[totalLength];
        System.arraycopy(input, 0, extended, 0, input.length);

        float[] combSum = new float[totalLength];
        for (int tuning : COMB_TUNING_MS) {
            combSum = addBuffers(combSum, combFilter(extended, sampleRate, tuning));
        }
        for (int i = 0; i < combSum.length; i++) {
            combSum[i] /= COMB_TUNING_MS.length;
        }

        float[] wet = combSum;
        for (int tuning : ALLPASS_TUNING_MS) {
            wet = allpassFilter(wet, sampleRate, tuning);
        }

        float[] output = new float[totalLength];
        for (int i = 0; i < totalLength; i++) {
            float dry = i < input.length ? input[i] : 0f;
            output[i] = dry * (1f - mix) + wet[i] * mix;
        }
        return output;
    }

    private float[] combFilter(float[] input, int sampleRate, int delayMs) {
        int delaySamples = Math.max(1, delayMs * sampleRate / 1000);
        float[] buffer = new float[delaySamples];
        float[] output = new float[input.length];
        int index = 0;
        float feedback = 0.6f + roomSize * 0.35f;
        float filterStore = 0f;

        for (int i = 0; i < input.length; i++) {
            float bufOut = buffer[index];
            filterStore = bufOut * (1f - damping) + filterStore * damping;
            output[i] = bufOut;
            buffer[index] = input[i] + filterStore * feedback;
            index = (index + 1) % delaySamples;
        }
        return output;
    }

    private float[] allpassFilter(float[] input, int sampleRate, int delayMs) {
        int delaySamples = Math.max(1, delayMs * sampleRate / 1000);
        float[] buffer = new float[delaySamples];
        float[] output = new float[input.length];
        int index = 0;
        float g = 0.5f;

        for (int i = 0; i < input.length; i++) {
            float bufOut = buffer[index];
            float x = input[i];
            float y = -g * x + bufOut;
            buffer[index] = x + g * bufOut;
            index = (index + 1) % delaySamples;
            output[i] = y;
        }
        return output;
    }

    private float[] addBuffers(float[] a, float[] b) {
        float[] out = new float[a.length];
        for (int i = 0; i < a.length; i++) {
            out[i] = a[i] + b[i];
        }
        return out;
    }

    @Override
    public String label() {
        return "Reverb";
    }
}
