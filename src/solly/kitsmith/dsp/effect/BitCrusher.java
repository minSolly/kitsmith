package solly.kitsmith.dsp.effect;

public class BitCrusher implements Effect {

    private final int bitDepth;
    private final int downsampleFactor;
    private final float mix;

    public BitCrusher(int bitDepth, int downsampleFactor, float mix) {
        this.bitDepth = bitDepth;
        this.downsampleFactor = Math.max(1, downsampleFactor);
        this.mix = mix;
    }

    @Override
    public float[] apply(float[] input, int sampleRate) {
        float[] output = new float[input.length];
        float steps = (float) Math.pow(2, bitDepth);
        float held = 0f;
        for (int i = 0; i < input.length; i++) {
            if (i % downsampleFactor == 0) {
                held = Math.round(input[i] * steps) / steps;
            }
            output[i] = input[i] * (1f - mix) + held * mix;
        }
        return output;
    }

    @Override
    public String label() {
        return "BitCrusher";
    }
}
