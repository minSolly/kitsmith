
package solly.kitsmith.generator;

import solly.kitsmith.dsp.AudioConstants;
import solly.kitsmith.dsp.NoiseGenerator;
import solly.kitsmith.dsp.Oscillator;
import solly.kitsmith.dsp.envelope.AdsrEnvelope;
import solly.kitsmith.dsp.envelope.PitchDropEnvelope;
import solly.kitsmith.dsp.filter.BiquadFilter;

import java.util.Random;

public class SubBassGenerator {

    public static float[] generate(Random random) {
        int sampleRate = AudioConstants.SAMPLE_RATE;
        float rootFreq = 30f + random.nextFloat() * 25f;
        float glideStartFreq = rootFreq * (1.6f + random.nextFloat() * 1.8f);
        float glideDuration = 0.05f + random.nextFloat() * 0.09f;
        float glideSteepness = 5f + random.nextFloat() * 6f;
        float attack = 0.003f + random.nextFloat() * 0.01f;
        float sustain = 0.92f + random.nextFloat() * 0.08f;
        float triangleBlend = 0.3f + random.nextFloat() * 0.3f;
        float driveAmount = 3.5f + random.nextFloat() * 3f;
        float clickLevel = 0.1f + random.nextFloat() * 0.1f;
        float slowGlideDepth = random.nextFloat() * 0.05f;

        float duration = 1.6f + random.nextFloat() * 0.6f;
        int samples = (int) (sampleRate * duration);
        float[] audio = new float[samples];

        PitchDropEnvelope glide = new PitchDropEnvelope(glideStartFreq, rootFreq, glideDuration, glideSteepness);
        AdsrEnvelope env = new AdsrEnvelope(attack, 0.15f, sustain, 0.35f, duration - 0.35f);
        Oscillator sine = new Oscillator(Oscillator.Waveform.SINE);
        Oscillator triangle = new Oscillator(Oscillator.Waveform.TRIANGLE);
        NoiseGenerator noise = new NoiseGenerator(random);
        BiquadFilter clickFilter = new BiquadFilter(sampleRate);
        clickFilter.set(BiquadFilter.Type.LOWPASS, 400f + random.nextFloat() * 500f, 1.2f);
        BiquadFilter subLowpass = new BiquadFilter(sampleRate);
        subLowpass.set(BiquadFilter.Type.LOWPASS, 140f, 0.85f);

        for (int i = 0; i < samples; i++) {
            float t = (float) i / sampleRate;
            float slowDrift = 1f - slowGlideDepth * Math.min(1f, t / duration);
            float f = glide.frequencyAt(t) * slowDrift;

            float wave = sine.next(f, sampleRate) * (1f - triangleBlend)
                    + triangle.next(f, sampleRate) * triangleBlend;

            float click = 0f;
            if (t < 0.012f) {
                float clickEnv = 1f - t / 0.012f;
                click = clickFilter.process(noise.white()) * clickEnv * clickLevel;
            }

            float body = subLowpass.process(wave) * env.amplitudeAt(t);
            audio[i] = saturate(body + click, driveAmount);
        }
        return audio;
    }

    private static float saturate(float x, float drive) {
        float driven = x * drive;
        float normalizer = (float) Math.tanh(drive);
        float shaped = (float) Math.tanh(driven) / Math.max(normalizer, 0.0001f);
        return Math.max(-1f, Math.min(1f, shaped));
    }
}