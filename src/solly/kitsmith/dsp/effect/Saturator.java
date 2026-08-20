package solly.kitsmith.dsp.effect;

public class Saturator implements Effect {

    private final float drive;
    private final float mix;

    public Saturator(float drive, float mix) {
        this.drive = drive;
        this.mix = mix;
    }

    @Override
    public float[] apply(float[] input, int sampleRate) {
        float[] output = new float[input.length];
        for (int i = 0; i < input.length; i++) {
            float x = input[i] * drive;
            float wet = softClip(x) / softClip(drive);
            output[i] = input[i] * (1f - mix) + wet * mix;
        }
        return output;
    }

    private float softClip(float x) {
        if (x > 1f) return 1f;
        if (x < -1f) return -1f;
        return x - (x * x * x) / 3f;
    }

    @Override
    public String label() {
        return "Saturator";
    }
}
