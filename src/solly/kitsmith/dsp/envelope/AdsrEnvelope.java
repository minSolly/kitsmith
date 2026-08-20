package solly.kitsmith.dsp.envelope;

public class AdsrEnvelope {

    private final float attack;
    private final float decay;
    private final float sustain;
    private final float release;
    private final float releaseStart;

    public AdsrEnvelope(float attack, float decay, float sustain, float release, float releaseStart) {
        this.attack = Math.max(attack, 0.0005f);
        this.decay = Math.max(decay, 0.0005f);
        this.sustain = sustain;
        this.release = Math.max(release, 0.0005f);
        this.releaseStart = releaseStart;
    }

    public float amplitudeAt(float time) {
        if (time < attack) {
            return time / attack;
        }
        float afterAttack = time - attack;
        if (afterAttack < decay) {
            float progress = afterAttack / decay;
            return 1f - progress * (1f - sustain);
        }
        if (time < releaseStart) {
            return sustain;
        }
        float releaseTime = time - releaseStart;
        if (releaseTime >= release) return 0f;
        float progress = releaseTime / release;
        return sustain * (1f - progress) * (1f - progress);
    }
}
