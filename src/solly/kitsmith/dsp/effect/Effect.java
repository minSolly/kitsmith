package solly.kitsmith.dsp.effect;

public interface Effect {
    float[] apply(float[] input, int sampleRate);

    String label();
}
