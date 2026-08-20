
package solly.kitsmith.generator;

import solly.kitsmith.dsp.AudioConstants;
import solly.kitsmith.dsp.NoiseGenerator;
import solly.kitsmith.dsp.Oscillator;
import solly.kitsmith.dsp.envelope.DecayEnvelope;
import solly.kitsmith.dsp.filter.BiquadFilter;

import java.util.Random;

public class FxGenerator {

    public static float[] generate(Random random) {
        int variant = random.nextInt(4);
        switch (variant) {
            case 0:
                return riser(random);
            case 1:
                return impact(random);
            case 2:
                return texture(random);
            default:
                return drone(random);
        }
    }

    private static float[] riser(Random random) {
        int sampleRate = AudioConstants.SAMPLE_RATE;
        float startFreq = 80f + random.nextFloat() * 100f;
        float endFreq = 900f + random.nextFloat() * 1400f;
        float noiseMix = 0.3f + random.nextFloat() * 0.3f;

        float duration = 1.0f + random.nextFloat() * 0.4f;
        int samples = (int) (sampleRate * duration);
        float[] audio = new float[samples];

        Oscillator osc = new Oscillator(Oscillator.Waveform.SAW);
        NoiseGenerator noise = new NoiseGenerator(random);
        BiquadFilter filter = new BiquadFilter(sampleRate);

        for (int i = 0; i < samples; i++) {
            float t = (float) i / sampleRate;
            float progress = t / duration;
            float freq = startFreq + (endFreq - startFreq) * progress * progress;
            filter.set(BiquadFilter.Type.BANDPASS, Math.max(100f, freq), 1.2f);

            float wave = osc.next(freq, sampleRate) * (1f - noiseMix)
                    + filter.process(noise.white()) * noiseMix;
            audio[i] = wave * progress;
        }
        return audio;
    }

    private static float[] impact(Random random) {
        int sampleRate = AudioConstants.SAMPLE_RATE;
        float lowFreq = 45f + random.nextFloat() * 40f;
        float decayRate = 2.5f + random.nextFloat() * 3f;

        float duration = 0.5f + random.nextFloat() * 0.2f;
        int samples = (int) (sampleRate * duration);
        float[] audio = new float[samples];

        Oscillator low = new Oscillator(Oscillator.Waveform.SINE);
        NoiseGenerator noise = new NoiseGenerator(random);
        DecayEnvelope env = new DecayEnvelope(0.003f, decayRate);
        BiquadFilter lowpass = new BiquadFilter(sampleRate);
        lowpass.set(BiquadFilter.Type.LOWPASS, 3000f, 0.8f);

        for (int i = 0; i < samples; i++) {
            float t = (float) i / sampleRate;
            float a = env.amplitudeAt(t);
            float click = t < 0.01f ? noise.white() * (1f - t / 0.01f) : 0f;
            float body = low.next(lowFreq, sampleRate) * a;
            audio[i] = lowpass.process(body + click * 0.6f);
        }
        return audio;
    }

    private static float[] texture(Random random) {
        int sampleRate = AudioConstants.SAMPLE_RATE;
        float lfoRate = 0.15f + random.nextFloat() * 0.6f;
        float baseCutoff = 500f + random.nextFloat() * 800f;
        float lfoDepth = 400f + random.nextFloat() * 900f;

        float duration = 1.6f + random.nextFloat() * 0.4f;
        int samples = (int) (sampleRate * duration);
        float[] audio = new float[samples];

        NoiseGenerator noise = new NoiseGenerator(random);
        BiquadFilter filter = new BiquadFilter(sampleRate);

        for (int i = 0; i < samples; i++) {
            float t = (float) i / sampleRate;
            float fadeIn = Math.min(1f, t / 0.3f);
            float fadeOut = Math.min(1f, (duration - t) / 0.3f);
            float cutoff = baseCutoff + (float) Math.sin(2 * Math.PI * lfoRate * t) * lfoDepth;
            filter.set(BiquadFilter.Type.BANDPASS, Math.max(120f, cutoff), 1.5f);
            audio[i] = filter.process(noise.pink()) * 3.5f * fadeIn * fadeOut;
        }
        return audio;
    }

    private static float[] drone(Random random) {
        int sampleRate = AudioConstants.SAMPLE_RATE;
        float freq = 55f + random.nextFloat() * 55f;
        float detune = 0.5f + random.nextFloat() * 1.5f;
        float lfoRate = 0.1f + random.nextFloat() * 0.4f;

        float duration = 2.5f + random.nextFloat() * 0.5f;
        int samples = (int) (sampleRate * duration);
        float[] audio = new float[samples];

        Oscillator a = new Oscillator(Oscillator.Waveform.SINE);
        Oscillator b = new Oscillator(Oscillator.Waveform.TRIANGLE, 0.4f);
        Oscillator c = new Oscillator(Oscillator.Waveform.SINE, 0.7f);

        for (int i = 0; i < samples; i++) {
            float t = (float) i / sampleRate;
            float fadeIn = Math.min(1f, t / 0.6f);
            float fadeOut = Math.min(1f, (duration - t) / 0.6f);
            float wobble = 1f + (float) Math.sin(2 * Math.PI * lfoRate * t) * 0.004f;

            float wave = a.next(freq * wobble, sampleRate) * 0.4f
                    + b.next(freq * (1f + detune * 0.01f) * wobble, sampleRate) * 0.3f
                    + c.next(freq * 1.5f * wobble, sampleRate) * 0.2f;

            audio[i] = wave * fadeIn * fadeOut;
        }
        return audio;
    }
}