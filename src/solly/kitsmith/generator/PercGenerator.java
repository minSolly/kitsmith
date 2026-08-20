
package solly.kitsmith.generator;

import solly.kitsmith.dsp.AudioConstants;
import solly.kitsmith.dsp.NoiseGenerator;
import solly.kitsmith.dsp.Oscillator;
import solly.kitsmith.dsp.envelope.DecayEnvelope;
import solly.kitsmith.dsp.envelope.PitchDropEnvelope;
import solly.kitsmith.dsp.filter.BiquadFilter;

import java.util.Random;

public class PercGenerator {

    public static float[] generate(Random random) {
        int variant = random.nextInt(3);
        switch (variant) {
            case 0:
                return tom(random);
            case 1:
                return metallic(random);
            default:
                return woodblock(random);
        }
    }

    private static float[] tom(Random random) {
        int sampleRate = AudioConstants.SAMPLE_RATE;
        float startFreq = 180f + random.nextFloat() * 220f;
        float endFreq = 90f + random.nextFloat() * 80f;
        float decayRate = 4f + random.nextFloat() * 5f;
        float noiseMix = 0.1f + random.nextFloat() * 0.15f;

        float duration = 0.25f + random.nextFloat() * 0.1f;
        int samples = (int) (sampleRate * duration);
        float[] audio = new float[samples];

        PitchDropEnvelope pitch = new PitchDropEnvelope(startFreq, endFreq, 0.1f, 4f);
        DecayEnvelope env = new DecayEnvelope(0.002f, decayRate);
        Oscillator osc = new Oscillator(Oscillator.Waveform.SINE);
        NoiseGenerator noise = new NoiseGenerator(random);

        for (int i = 0; i < samples; i++) {
            float t = (float) i / sampleRate;
            float a = env.amplitudeAt(t);
            float tone = osc.next(pitch.frequencyAt(t), sampleRate);
            audio[i] = (tone * (1f - noiseMix) + noise.white() * noiseMix) * a;
        }
        return audio;
    }

    private static float[] metallic(Random random) {
        int sampleRate = AudioConstants.SAMPLE_RATE;
        float carrier = 300f + random.nextFloat() * 400f;
        float modulator = 180f + random.nextFloat() * 500f;
        float modIndex = 2f + random.nextFloat() * 6f;
        float decayRate = 6f + random.nextFloat() * 8f;

        float duration = 0.2f + random.nextFloat() * 0.08f;
        int samples = (int) (sampleRate * duration);
        float[] audio = new float[samples];

        DecayEnvelope env = new DecayEnvelope(0.001f, decayRate);
        float carrierPhase = 0f;
        float modPhase = 0f;

        for (int i = 0; i < samples; i++) {
            float t = (float) i / sampleRate;
            float modSample = (float) Math.sin(2 * Math.PI * modPhase);
            float fm = carrier + modSample * modIndex * carrier;
            carrierPhase += fm / sampleRate;
            modPhase += modulator / sampleRate;
            audio[i] = (float) Math.sin(2 * Math.PI * carrierPhase) * env.amplitudeAt(t);
        }
        return audio;
    }

    private static float[] woodblock(Random random) {
        int sampleRate = AudioConstants.SAMPLE_RATE;
        float bandCenter = 900f + random.nextFloat() * 1400f;
        float bandQ = 3f + random.nextFloat() * 4f;
        float decayRate = 25f + random.nextFloat() * 25f;

        float duration = 0.1f + random.nextFloat() * 0.05f;
        int samples = (int) (sampleRate * duration);
        float[] audio = new float[samples];

        NoiseGenerator noise = new NoiseGenerator(random);
        BiquadFilter bandpass = new BiquadFilter(sampleRate);
        bandpass.set(BiquadFilter.Type.BANDPASS, bandCenter, bandQ);
        DecayEnvelope env = new DecayEnvelope(0.0005f, decayRate);

        for (int i = 0; i < samples; i++) {
            float t = (float) i / sampleRate;
            audio[i] = bandpass.process(noise.white()) * env.amplitudeAt(t);
        }
        return audio;
    }
}