package solly.kitsmith.dsp;

public class Oscillator {

    public enum Waveform {
        SINE, SAW, SQUARE, TRIANGLE
    }

    private final Waveform waveform;
    private float phase;

    public Oscillator(Waveform waveform) {
        this.waveform = waveform;
        this.phase = 0f;
    }

    public Oscillator(Waveform waveform, float startPhase) {
        this.waveform = waveform;
        this.phase = startPhase;
    }

    public float next(float frequency, int sampleRate) {
        float value = sampleAt(phase, waveform);
        phase += frequency / sampleRate;
        if (phase >= 1f) phase -= 1f;
        if (phase < 0f) phase += 1f;
        return value;
    }

    public static float sampleAt(float phase, Waveform waveform) {
        float p = phase - (float) Math.floor(phase);
        switch (waveform) {
            case SAW:
                return 2f * (p - 0.5f);
            case SQUARE:
                return p < 0.5f ? 1f : -1f;
            case TRIANGLE:
                return 4f * Math.abs(p - 0.5f) - 1f;
            case SINE:
            default:
                return (float) Math.sin(2 * Math.PI * p);
        }
    }
}
