package solly.kitsmith.dsp.envelope;

public class PitchDropEnvelope {

    private final float startFreq;
    private final float endFreq;
    private final float duration;
    private final float steepness;

    public PitchDropEnvelope(float startFreq, float endFreq, float duration, float steepness) {
        this.startFreq = startFreq;
        this.endFreq = endFreq;
        this.duration = Math.max(duration, 0.001f);
        this.steepness = steepness;
    }

    public float frequencyAt(float time) {
        if (time >= duration) return endFreq;
        float progress = time / duration;
        float shaped = (float) Math.exp(-progress * steepness);
        return endFreq + (startFreq - endFreq) * shaped;
    }
}
