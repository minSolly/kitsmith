package solly.kitsmith.dsp.envelope;

public class DecayEnvelope {

    private final float attack;
    private final float decayRate;
    private final float curve;

    public DecayEnvelope(float attack, float decayRate) {
        this(attack, decayRate, 1f);
    }

    public DecayEnvelope(float attack, float decayRate, float curve) {
        this.attack = Math.max(attack, 0.0002f);
        this.decayRate = decayRate;
        this.curve = curve;
    }

    public float amplitudeAt(float time) {
        if (time < attack) {
            return time / attack;
        }
        float t = time - attack;
        return (float) Math.pow(Math.exp(-t * decayRate), curve);
    }
}
