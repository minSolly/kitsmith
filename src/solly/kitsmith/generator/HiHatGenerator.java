
package solly.kitsmith.generator;

import solly.kitsmith.dsp.AudioConstants;
import solly.kitsmith.dsp.NoiseGenerator;
import solly.kitsmith.dsp.Oscillator;
import solly.kitsmith.dsp.envelope.DecayEnvelope;
import solly.kitsmith.dsp.filter.BiquadFilter;

import java.util.Random;

public class HiHatGenerator {

    private static final float[] METAL_RATIOS = {1f, 1.34f, 1.79f, 2.31f, 2.92f, 3.61f};

    public static float[] generate(Random random, boolean open) {
        int sampleRate = AudioConstants.SAMPLE_RATE;
        float fundamental = 280f + random.nextFloat() * 120f;
        float highpassCutoff = 6500f + random.nextFloat() * 3500f;
        float decayRate = open ? (3f + random.nextFloat() * 4f) : (18f + random.nextFloat() * 20f);
        float noiseMix = 0.35f + random.nextFloat() * 0.25f;

        float duration = open ? (0.35f + random.nextFloat() * 0.15f) : (0.08f + random.nextFloat() * 0.05f);
        int samples = (int) (sampleRate * duration);
        float[] audio = new float[samples];

        Oscillator[] oscillators = new Oscillator[METAL_RATIOS.length];
        for (int i = 0; i < oscillators.length; i++) {
            oscillators[i] = new Oscillator(Oscillator.Waveform.SQUARE);
        }
        DecayEnvelope env = new DecayEnvelope(0.001f, decayRate);
        NoiseGenerator noise = new NoiseGenerator(random);
        BiquadFilter highpass = new BiquadFilter(sampleRate);
        highpass.set(BiquadFilter.Type.HIGHPASS, highpassCutoff, 0.8f);
        BiquadFilter bandpass = new BiquadFilter(sampleRate);
        bandpass.set(BiquadFilter.Type.BANDPASS, highpassCutoff * 1.3f, 0.5f);

        for (int i = 0; i < samples; i++) {
            float t = (float) i / sampleRate;
            float a = env.amplitudeAt(t);

            float metallic = 0f;
            for (int v = 0; v < oscillators.length; v++) {
                metallic += oscillators[v].next(fundamental * METAL_RATIOS[v], sampleRate);
            }
            metallic /= oscillators.length;
            metallic = bandpass.process(metallic);

            float noiseSample = highpass.process(noise.white());

            audio[i] = (metallic * (1f - noiseMix) + noiseSample * noiseMix) * a;
        }
        return audio;
    }
}