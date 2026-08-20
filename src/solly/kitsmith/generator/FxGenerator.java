package solly.kitsmith.generator;

import solly.kitsmith.dsp.AudioConstants;
import solly.kitsmith.dsp.NoiseGenerator;
import solly.kitsmith.dsp.Oscillator;
import solly.kitsmith.dsp.envelope.DecayEnvelope;
import solly.kitsmith.dsp.envelope.AdsrEnvelope;
import solly.kitsmith.dsp.filter.BiquadFilter;

import java.util.Random;

public class FxGenerator {

    public static float[] generate(Random random) {
        int variant = random.nextInt(6);
        switch (variant) {
            case 0:
                return riser(random);
            case 1:
                return impact(random);
            case 2:
                return sweep(random);
            case 3:
                return glitchHit(random);
            case 4:
                return noiseBurst(random);
            default:
                return toneSwell(random);
        }
    }

    private static float[] riser(Random random) {
        int sampleRate = AudioConstants.SAMPLE_RATE;
        float startFreq = 100 + random.nextFloat() * 100;
        float endFreq = 2000 + random.nextFloat() * 2000;
        float duration = 0.6f + random.nextFloat() * 0.4f;
        int samples = (int)(sampleRate * duration);
        float[] audio = new float[samples];

        Oscillator osc = new Oscillator(Oscillator.Waveform.SAW, random.nextFloat());
        NoiseGenerator noise = new NoiseGenerator(random);
        BiquadFilter filter = new BiquadFilter(sampleRate);

        for (int i = 0; i < samples; i++) {
            float t = (float)i / sampleRate;
            float progress = t / duration;
            float freq = startFreq + (endFreq - startFreq) * progress * progress;
            filter.set(BiquadFilter.Type.BANDPASS, Math.min(freq, sampleRate * 0.49f), 1.0f + random.nextFloat() * 0.5f);

            float wave = osc.next(freq, sampleRate) * 0.7f;
            float noiseSample = filter.process(noise.white()) * 0.3f;

            float env = (float)Math.sin(Math.PI * progress);
            audio[i] = (wave + noiseSample) * env * 0.8f;
        }
        return audio;
    }

    private static float[] impact(Random random) {
        int sampleRate = AudioConstants.SAMPLE_RATE;
        float duration = 0.2f + random.nextFloat() * 0.15f;
        int samples = (int)(sampleRate * duration);
        float[] audio = new float[samples];

        Oscillator osc = new Oscillator(Oscillator.Waveform.SINE, random.nextFloat());
        NoiseGenerator noise = new NoiseGenerator(random);
        BiquadFilter filter = new BiquadFilter(sampleRate);

        float freq = 60 + random.nextFloat() * 80;
        float noiseAmount = 0.3f + random.nextFloat() * 0.3f;

        for (int i = 0; i < samples; i++) {
            float t = (float)i / sampleRate;
            float env = (float)Math.exp(-t * 8);
            filter.set(BiquadFilter.Type.LOWPASS, 1000 + 2000 * env, 0.8f);

            float wave = osc.next(freq * (1 - t * 2), sampleRate) * (1 - noiseAmount);
            float noiseSample = filter.process(noise.white()) * noiseAmount;

            audio[i] = (wave + noiseSample) * env * 0.9f;
        }
        return audio;
    }

    private static float[] sweep(Random random) {
        int sampleRate = AudioConstants.SAMPLE_RATE;
        float duration = 0.4f + random.nextFloat() * 0.3f;
        int samples = (int)(sampleRate * duration);
        float[] audio = new float[samples];

        Oscillator osc = new Oscillator(Oscillator.Waveform.SINE, random.nextFloat());
        BiquadFilter filter = new BiquadFilter(sampleRate);

        float centerFreq = 400 + random.nextFloat() * 800;
        float sweepDepth = 200 + random.nextFloat() * 300;

        for (int i = 0; i < samples; i++) {
            float t = (float)i / sampleRate;
            float freq = centerFreq + sweepDepth * (float)Math.sin(2 * Math.PI * 2 * t);
            filter.set(BiquadFilter.Type.BANDPASS, Math.min(Math.max(freq, 100), sampleRate * 0.49f), 2.0f);

            float env = (float)Math.exp(-t * 3);
            float wave = osc.next(200 + 200 * t, sampleRate);
            audio[i] = filter.process(wave) * env * 0.7f;
        }
        return audio;
    }

    private static float[] glitchHit(Random random) {
        int sampleRate = AudioConstants.SAMPLE_RATE;
        float duration = 0.1f + random.nextFloat() * 0.08f;
        int samples = (int)(sampleRate * duration);
        float[] audio = new float[samples];

        NoiseGenerator noise = new NoiseGenerator(random);
        BiquadFilter filter = new BiquadFilter(sampleRate);

        for (int i = 0; i < samples; i++) {
            float t = (float)i / sampleRate;
            float env = (float)Math.exp(-t * 20);
            float freq = 800 + 2000 * (float)Math.exp(-t * 5);
            filter.set(BiquadFilter.Type.BANDPASS, Math.min(freq, sampleRate * 0.49f), 3.0f);

            float sample = filter.process(noise.white()) * env;
            audio[i] = sample * 0.7f;
        }
        return audio;
    }

    private static float[] noiseBurst(Random random) {
        int sampleRate = AudioConstants.SAMPLE_RATE;
        float duration = 0.15f + random.nextFloat() * 0.1f;
        int samples = (int)(sampleRate * duration);
        float[] audio = new float[samples];

        NoiseGenerator noise = new NoiseGenerator(random);
        BiquadFilter filter = new BiquadFilter(sampleRate);

        for (int i = 0; i < samples; i++) {
            float t = (float)i / sampleRate;
            float env = (float)Math.exp(-t * 15) * (1 + (float)Math.sin(2 * Math.PI * 30 * t) * 0.5f);
            filter.set(BiquadFilter.Type.HIGHPASS, 800 + 2000 * (1 - (float)Math.exp(-t * 3)), 0.8f);

            audio[i] = filter.process(noise.pink()) * env * 0.8f;
        }
        return audio;
    }

    private static float[] toneSwell(Random random) {
        int sampleRate = AudioConstants.SAMPLE_RATE;
        float duration = 0.6f + random.nextFloat() * 0.4f;
        int samples = (int)(sampleRate * duration);
        float[] audio = new float[samples];

        Oscillator osc = new Oscillator(Oscillator.Waveform.SINE, random.nextFloat());
        BiquadFilter filter = new BiquadFilter(sampleRate);

        float freq = 300 + random.nextFloat() * 400;
        float lfoRate = 1f + random.nextFloat() * 2f;

        AdsrEnvelope env = new AdsrEnvelope(
                0.1f + random.nextFloat() * 0.1f,
                0.1f + random.nextFloat() * 0.1f,
                0.5f + random.nextFloat() * 0.3f,
                0.1f + random.nextFloat() * 0.1f,
                duration - 0.1f
        );

        for (int i = 0; i < samples; i++) {
            float t = (float)i / sampleRate;
            float envVal = env.amplitudeAt(t);

            float cutoff = 500 + 2000 * envVal;
            filter.set(BiquadFilter.Type.LOWPASS, Math.min(cutoff, sampleRate * 0.49f), 0.7f);

            float wave = osc.next(freq * (1 + 0.02f * (float)Math.sin(2 * Math.PI * lfoRate * t)), sampleRate);
            audio[i] = filter.process(wave) * envVal * 0.7f;
        }
        return audio;
    }
}