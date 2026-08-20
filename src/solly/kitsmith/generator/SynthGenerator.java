
package solly.kitsmith.generator;

import solly.kitsmith.dsp.AudioConstants;
import solly.kitsmith.dsp.Oscillator;
import solly.kitsmith.dsp.envelope.AdsrEnvelope;
import solly.kitsmith.dsp.filter.BiquadFilter;

import java.util.Random;

public class SynthGenerator {

    public static float[] generate(Random random) {
        int variant = random.nextInt(4);
        switch (variant) {
            case 0:
                return lead(random);
            case 1:
                return pad(random);
            case 2:
                return pluck(random);
            default:
                return stab(random);
        }
    }

    private static float[] lead(Random random) {
        int sampleRate = AudioConstants.SAMPLE_RATE;
        float freq = 220f + random.nextFloat() * 260f;
        float detune = 0.4f + random.nextFloat() * 1.5f;
        float vibratoRate = 4f + random.nextFloat() * 3f;
        float vibratoDepth = random.nextFloat() * 6f;
        float filterStart = 3500f + random.nextFloat() * 3000f;
        float filterEnd = 600f + random.nextFloat() * 800f;
        float filterDecayRate = 2f + random.nextFloat() * 3f;

        float duration = 0.8f + random.nextFloat() * 0.3f;
        int samples = (int) (sampleRate * duration);
        float[] audio = new float[samples];

        Oscillator[] voices = {
                new Oscillator(Oscillator.Waveform.SAW),
                new Oscillator(Oscillator.Waveform.SAW, 0.5f),
                new Oscillator(Oscillator.Waveform.SQUARE, 0.25f)
        };
        AdsrEnvelope env = new AdsrEnvelope(0.01f, 0.15f, 0.55f, 0.25f, duration - 0.25f);
        BiquadFilter filter = new BiquadFilter(sampleRate);

        for (int i = 0; i < samples; i++) {
            float t = (float) i / sampleRate;
            float vibrato = 1f + (float) Math.sin(2 * Math.PI * vibratoRate * t) * (vibratoDepth * 0.001f);
            float cutoff = filterEnd + (filterStart - filterEnd) * (float) Math.exp(-t * filterDecayRate);
            filter.set(BiquadFilter.Type.LOWPASS, cutoff, 0.9f);

            float wave = voices[0].next(freq * vibrato, sampleRate) * 0.4f
                    + voices[1].next(freq * vibrato * (1f + detune * 0.01f), sampleRate) * 0.35f
                    + voices[2].next(freq * vibrato * (1f - detune * 0.008f), sampleRate) * 0.25f;

            audio[i] = filter.process(wave) * env.amplitudeAt(t);
        }
        return audio;
    }

    private static float[] pad(Random random) {
        int sampleRate = AudioConstants.SAMPLE_RATE;
        float freq = 160f + random.nextFloat() * 140f;
        float lfoRate = 0.3f + random.nextFloat() * 1.2f;
        float lfoDepth = 200f + random.nextFloat() * 400f;
        float baseCutoff = 500f + random.nextFloat() * 500f;

        float duration = 2.2f + random.nextFloat() * 0.5f;
        int samples = (int) (sampleRate * duration);
        float[] audio = new float[samples];

        Oscillator sine = new Oscillator(Oscillator.Waveform.SINE);
        Oscillator triangle = new Oscillator(Oscillator.Waveform.TRIANGLE, 0.4f);
        Oscillator square = new Oscillator(Oscillator.Waveform.SQUARE, 0.6f);
        AdsrEnvelope env = new AdsrEnvelope(0.5f, 0.5f, 0.7f, 0.8f, duration - 0.8f);
        BiquadFilter filter = new BiquadFilter(sampleRate);

        for (int i = 0; i < samples; i++) {
            float t = (float) i / sampleRate;
            float cutoff = baseCutoff + (float) Math.sin(2 * Math.PI * lfoRate * t) * lfoDepth;
            filter.set(BiquadFilter.Type.LOWPASS, Math.max(200f, cutoff), 0.7f);

            float wave = sine.next(freq, sampleRate) * 0.45f
                    + triangle.next(freq * 1.5f, sampleRate) * 0.3f
                    + square.next(freq * 2f, sampleRate) * 0.12f;

            audio[i] = filter.process(wave) * env.amplitudeAt(t);
        }
        return audio;
    }

    private static float[] pluck(Random random) {
        int sampleRate = AudioConstants.SAMPLE_RATE;
        float freq = 260f + random.nextFloat() * 300f;
        float filterStart = 5000f + random.nextFloat() * 3000f;
        float filterEnd = 400f + random.nextFloat() * 500f;
        float filterDecayRate = 8f + random.nextFloat() * 10f;

        float duration = 0.4f + random.nextFloat() * 0.15f;
        int samples = (int) (sampleRate * duration);
        float[] audio = new float[samples];

        Oscillator saw = new Oscillator(Oscillator.Waveform.SAW);
        Oscillator triangle = new Oscillator(Oscillator.Waveform.TRIANGLE, 0.5f);
        AdsrEnvelope env = new AdsrEnvelope(0.002f, 0.25f, 0f, 0.1f, 0.25f);
        BiquadFilter filter = new BiquadFilter(sampleRate);

        for (int i = 0; i < samples; i++) {
            float t = (float) i / sampleRate;
            float cutoff = filterEnd + (filterStart - filterEnd) * (float) Math.exp(-t * filterDecayRate);
            filter.set(BiquadFilter.Type.LOWPASS, cutoff, 1.1f);

            float wave = saw.next(freq, sampleRate) * 0.6f + triangle.next(freq * 2f, sampleRate) * 0.3f;
            audio[i] = filter.process(wave) * env.amplitudeAt(t);
        }
        return audio;
    }

    private static float[] stab(Random random) {
        int sampleRate = AudioConstants.SAMPLE_RATE;
        float freq = 220f + random.nextFloat() * 140f;
        float filterCutoff = 2500f + random.nextFloat() * 2500f;
        float decayRate = 10f + random.nextFloat() * 6f;

        float duration = 0.25f + random.nextFloat() * 0.15f;
        int samples = (int) (sampleRate * duration);
        float[] audio = new float[samples];

        Oscillator square = new Oscillator(Oscillator.Waveform.SQUARE);
        Oscillator saw = new Oscillator(Oscillator.Waveform.SAW, 0.3f);
        BiquadFilter filter = new BiquadFilter(sampleRate);
        filter.set(BiquadFilter.Type.LOWPASS, filterCutoff, 1f);

        for (int i = 0; i < samples; i++) {
            float t = (float) i / sampleRate;
            float noteEnv = (float) Math.exp(-t * decayRate);
            float wave = square.next(freq, sampleRate) * 0.5f + saw.next(freq, sampleRate) * 0.4f;
            audio[i] = filter.process(wave) * noteEnv;
        }
        return audio;
    }
}