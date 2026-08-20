package solly.kitsmith.dsp.filter;

public class BiquadFilter {

    public enum Type {
        LOWPASS, HIGHPASS, BANDPASS, NOTCH
    }

    private final int sampleRate;
    private float a0, a1, a2, b0, b1, b2;
    private float x1, x2, y1, y2;

    public BiquadFilter(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    public void set(Type type, float cutoff, float q) {
        float freq = Math.max(20f, Math.min(cutoff, sampleRate * 0.49f));
        float w0 = (float) (2 * Math.PI * freq / sampleRate);
        float cosw0 = (float) Math.cos(w0);
        float sinw0 = (float) Math.sin(w0);
        float alpha = sinw0 / (2f * Math.max(q, 0.1f));

        float ib0, ib1, ib2, ia0, ia1, ia2;
        switch (type) {
            case HIGHPASS:
                ib0 = (1 + cosw0) / 2;
                ib1 = -(1 + cosw0);
                ib2 = (1 + cosw0) / 2;
                ia0 = 1 + alpha;
                ia1 = -2 * cosw0;
                ia2 = 1 - alpha;
                break;
            case BANDPASS:
                ib0 = alpha;
                ib1 = 0;
                ib2 = -alpha;
                ia0 = 1 + alpha;
                ia1 = -2 * cosw0;
                ia2 = 1 - alpha;
                break;
            case NOTCH:
                ib0 = 1;
                ib1 = -2 * cosw0;
                ib2 = 1;
                ia0 = 1 + alpha;
                ia1 = -2 * cosw0;
                ia2 = 1 - alpha;
                break;
            case LOWPASS:
            default:
                ib0 = (1 - cosw0) / 2;
                ib1 = 1 - cosw0;
                ib2 = (1 - cosw0) / 2;
                ia0 = 1 + alpha;
                ia1 = -2 * cosw0;
                ia2 = 1 - alpha;
                break;
        }
        this.b0 = ib0 / ia0;
        this.b1 = ib1 / ia0;
        this.b2 = ib2 / ia0;
        this.a1 = ia1 / ia0;
        this.a2 = ia2 / ia0;
    }

    public float process(float input) {
        float output = b0 * input + b1 * x1 + b2 * x2 - a1 * y1 - a2 * y2;
        x2 = x1;
        x1 = input;
        y2 = y1;
        y1 = output;
        return output;
    }
}
