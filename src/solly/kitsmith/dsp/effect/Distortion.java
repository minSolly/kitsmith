package solly.kitsmith.dsp.effect;

public class Distortion implements Effect {

    private final float drive;
    private final float mix;

    public Distortion(float drive, float mix) {
        this.drive = drive;
        this.mix = mix;
    }

    @Override
    public float[] apply(float[] input, int sampleRate) {
        float[] output = new float[input.length];
        float normalizer = (float) Math.tanh(drive);
        for (int i = 0; i < input.length; i++) {
            float wet = (float) Math.tanh(input[i] * drive) / Math.max(normalizer, 0.0001f);
            output[i] = input[i] * (1f - mix) + wet * mix;
        }
        return output;
    }

    @Override
    public String label() {
        return "Distortion";
    }
}
