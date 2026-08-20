package solly.kitsmith.dsp.effect;

public class Delay implements Effect {

    private final float delayMs;
    private final float feedback;
    private final float mix;

    public Delay(float delayMs, float feedback, float mix) {
        this.delayMs = delayMs;
        this.feedback = feedback;
        this.mix = mix;
    }

    @Override
    public float[] apply(float[] input, int sampleRate) {
        int delaySamples = Math.max(1, (int) (delayMs * sampleRate / 1000f));
        int tailSamples = delaySamples * 6;
        float[] output = new float[input.length + tailSamples];
        float[] line = new float[delaySamples];
        int writeIndex = 0;

        for (int i = 0; i < output.length; i++) {
            float dry = i < input.length ? input[i] : 0f;
            float delayed = line[writeIndex];
            float toStore = dry + delayed * feedback;
            line[writeIndex] = toStore;
            writeIndex = (writeIndex + 1) % delaySamples;
            output[i] = dry * (1f - mix) + delayed * mix;
        }
        return output;
    }

    @Override
    public String label() {
        return "Delay";
    }
}
