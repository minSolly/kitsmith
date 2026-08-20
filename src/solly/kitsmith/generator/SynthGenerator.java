package solly.kitsmith.generator;

import solly.kitsmith.dsp.AudioConstants;
import solly.kitsmith.dsp.Oscillator;
import solly.kitsmith.dsp.envelope.AdsrEnvelope;
import solly.kitsmith.dsp.filter.BiquadFilter;

import java.util.Random;

public class SynthGenerator {

    public static float[] generate(Random random) {
        int variant = random.nextInt(6);
        switch (variant) {
            case 0:
                return analogLead(random);
            case 1:
                return padSound(random);
            case 2:
                return pluckSound(random);
            case 3:
                return brassSound(random);
            case 4:
                return bellSound(random);
            default:
                return bassSynth(random);
        }
    }

    private static float[] analogLead(Random random) {
        int sampleRate = AudioConstants.SAMPLE_RATE;
        float freq = 220 + random.nextFloat() * 300;
        float duration = 0.6f + random.nextFloat() * 0.4f;
        int samples = (int)(sampleRate * duration);
        float[] audio = new float[samples];

        Oscillator osc1 = new Oscillator(Oscillator.Waveform.SAW, random.nextFloat());
        Oscillator osc2 = new Oscillator(Oscillator.Waveform.SQUARE, random.nextFloat());
        Oscillator osc3 = new Oscillator(Oscillator.Waveform.SINE, random.nextFloat());

        float detune = 0.5f + random.nextFloat() * 1.5f;
        float mix1 = 0.3f + random.nextFloat() * 0.3f;
        float mix2 = 0.2f + random.nextFloat() * 0.3f;
        float mix3 = 1f - mix1 - mix2;

        AdsrEnvelope env = new AdsrEnvelope(
                0.002f + random.nextFloat() * 0.01f,
                0.05f + random.nextFloat() * 0.1f,
                0.4f + random.nextFloat() * 0.3f,
                0.1f + random.nextFloat() * 0.15f,
                duration - 0.1f
        );

        BiquadFilter filter = new BiquadFilter(sampleRate);
        float filterCutoff = 2000 + random.nextFloat() * 3000;
        float resonance = 0.8f + random.nextFloat() * 1.2f;
        float filterEnv = 1f + random.nextFloat() * 3f;

        for (int i = 0; i < samples; i++) {
            float t = (float)i / sampleRate;
            float envVal = env.amplitudeAt(t);

            float cutoff = filterCutoff * (0.3f + 0.7f * envVal);
            filter.set(BiquadFilter.Type.LOWPASS, Math.min(cutoff, sampleRate * 0.49f), resonance);

            float wave = osc1.next(freq, sampleRate) * mix1 +
                    osc2.next(freq * (1 + detune * 0.01f), sampleRate) * mix2 +
                    osc3.next(freq * 2, sampleRate) * mix3;

            audio[i] = filter.process(wave) * envVal * 0.8f;
        }
        return audio;
    }

    private static float[] padSound(Random random) {
        int sampleRate = AudioConstants.SAMPLE_RATE;
        float freq = 160 + random.nextFloat() * 180;
        float duration = 2.0f + random.nextFloat() * 1.0f;
        int samples = (int)(sampleRate * duration);
        float[] audio = new float[samples];

        Oscillator[] oscs = new Oscillator[3];
        oscs[0] = new Oscillator(Oscillator.Waveform.SINE, random.nextFloat());
        oscs[1] = new Oscillator(Oscillator.Waveform.TRIANGLE, random.nextFloat());
        oscs[2] = new Oscillator(Oscillator.Waveform.SINE, random.nextFloat());

        float[] freqs = {
                freq,
                freq * (1.5f + random.nextFloat() * 0.5f),
                freq * 2
        };
        float[] amps = {
                0.4f + random.nextFloat() * 0.2f,
                0.2f + random.nextFloat() * 0.2f,
                0.1f + random.nextFloat() * 0.15f
        };

        AdsrEnvelope env = new AdsrEnvelope(
                0.3f + random.nextFloat() * 0.4f,
                0.2f + random.nextFloat() * 0.3f,
                0.5f + random.nextFloat() * 0.4f,
                0.5f + random.nextFloat() * 0.5f,
                duration - 0.3f
        );

        BiquadFilter filter = new BiquadFilter(sampleRate);
        float lfoRate = 0.2f + random.nextFloat() * 0.6f;
        float lfoDepth = 200 + random.nextFloat() * 400;

        for (int i = 0; i < samples; i++) {
            float t = (float)i / sampleRate;
            float lfo = (float)Math.sin(2 * Math.PI * lfoRate * t) * lfoDepth;
            float cutoff = 600 + random.nextFloat() * 400 + lfo;
            filter.set(BiquadFilter.Type.LOWPASS, Math.max(200, Math.min(cutoff, sampleRate * 0.49f)), 0.6f);

            float sum = 0;
            for (int v = 0; v < oscs.length; v++) {
                sum += oscs[v].next(freqs[v], sampleRate) * amps[v];
            }

            audio[i] = filter.process(sum) * env.amplitudeAt(t) * 0.7f;
        }
        return audio;
    }

    private static float[] pluckSound(Random random) {
        int sampleRate = AudioConstants.SAMPLE_RATE;
        float freq = 200 + random.nextFloat() * 300;
        float duration = 0.3f + random.nextFloat() * 0.2f;
        int samples = (int)(sampleRate * duration);
        float[] audio = new float[samples];

        Oscillator osc = new Oscillator(Oscillator.Waveform.SAW, random.nextFloat());

        AdsrEnvelope env = new AdsrEnvelope(
                0.001f + random.nextFloat() * 0.003f,
                0.1f + random.nextFloat() * 0.15f,
                0.0f,
                0.05f + random.nextFloat() * 0.05f,
                0.1f
        );

        BiquadFilter filter = new BiquadFilter(sampleRate);
        float filterStart = 3000 + random.nextFloat() * 3000;
        float filterDecay = 4f + random.nextFloat() * 6f;

        for (int i = 0; i < samples; i++) {
            float t = (float)i / sampleRate;
            float envVal = env.amplitudeAt(t);

            float cutoff = filterStart * (float)Math.exp(-t * filterDecay) + 200;
            filter.set(BiquadFilter.Type.LOWPASS, Math.min(cutoff, sampleRate * 0.49f), 1.2f);

            float wave = osc.next(freq, sampleRate);
            audio[i] = filter.process(wave) * envVal * 0.8f;
        }
        return audio;
    }

    private static float[] brassSound(Random random) {
        int sampleRate = AudioConstants.SAMPLE_RATE;
        float freq = 180 + random.nextFloat() * 220;
        float duration = 0.8f + random.nextFloat() * 0.4f;
        int samples = (int)(sampleRate * duration);
        float[] audio = new float[samples];

        Oscillator osc1 = new Oscillator(Oscillator.Waveform.SAW, random.nextFloat());
        Oscillator osc2 = new Oscillator(Oscillator.Waveform.SAW, random.nextFloat());

        AdsrEnvelope env = new AdsrEnvelope(
                0.005f + random.nextFloat() * 0.01f,
                0.1f + random.nextFloat() * 0.1f,
                0.3f + random.nextFloat() * 0.2f,
                0.15f + random.nextFloat() * 0.1f,
                duration - 0.1f
        );

        BiquadFilter filter = new BiquadFilter(sampleRate);
        float filterEnvAmount = 2f + random.nextFloat() * 2f;

        for (int i = 0; i < samples; i++) {
            float t = (float)i / sampleRate;
            float envVal = env.amplitudeAt(t);

            float cutoff = 800 + 3000 * envVal * (0.2f + 0.8f * envVal);
            filter.set(BiquadFilter.Type.LOWPASS, Math.min(cutoff, sampleRate * 0.49f), 0.9f);

            float wave = osc1.next(freq, sampleRate) * 0.6f +
                    osc2.next(freq * 1.5f, sampleRate) * 0.3f;

            audio[i] = filter.process(wave) * envVal * 0.8f;
        }
        return audio;
    }

    private static float[] bellSound(Random random) {
        int sampleRate = AudioConstants.SAMPLE_RATE;
        float freq = 400 + random.nextFloat() * 600;
        float duration = 0.3f + random.nextFloat() * 0.3f;
        int samples = (int)(sampleRate * duration);
        float[] audio = new float[samples];

        Oscillator[] oscs = new Oscillator[4];
        for (int i = 0; i < oscs.length; i++) {
            oscs[i] = new Oscillator(Oscillator.Waveform.SINE, random.nextFloat());
        }

        float[] freqs = {
                freq,
                freq * 1.5f,
                freq * 2.2f,
                freq * 3.8f
        };
        float[] amps = {
                0.5f + random.nextFloat() * 0.2f,
                0.2f + random.nextFloat() * 0.15f,
                0.1f + random.nextFloat() * 0.1f,
                0.05f + random.nextFloat() * 0.05f
        };
        float[] decays = {
                1f + random.nextFloat() * 2f,
                2f + random.nextFloat() * 2f,
                4f + random.nextFloat() * 3f,
                6f + random.nextFloat() * 4f
        };

        for (int i = 0; i < samples; i++) {
            float t = (float)i / sampleRate;
            float sum = 0;

            for (int v = 0; v < oscs.length; v++) {
                float env = (float)Math.exp(-t * decays[v]);
                sum += oscs[v].next(freqs[v], sampleRate) * amps[v] * env;
            }

            audio[i] = sum * 0.7f;
        }
        return audio;
    }

    private static float[] bassSynth(Random random) {
        int sampleRate = AudioConstants.SAMPLE_RATE;
        float freq = 60 + random.nextFloat() * 80;
        float duration = 0.5f + random.nextFloat() * 0.3f;
        int samples = (int)(sampleRate * duration);
        float[] audio = new float[samples];

        Oscillator osc1 = new Oscillator(Oscillator.Waveform.SINE, random.nextFloat());
        Oscillator osc2 = new Oscillator(Oscillator.Waveform.SQUARE, random.nextFloat());

        AdsrEnvelope env = new AdsrEnvelope(
                0.005f + random.nextFloat() * 0.01f,
                0.1f + random.nextFloat() * 0.1f,
                0.4f + random.nextFloat() * 0.3f,
                0.1f + random.nextFloat() * 0.1f,
                duration - 0.1f
        );

        BiquadFilter filter = new BiquadFilter(sampleRate);

        for (int i = 0; i < samples; i++) {
            float t = (float)i / sampleRate;
            float envVal = env.amplitudeAt(t);

            float cutoff = 300 + 500 * envVal;
            filter.set(BiquadFilter.Type.LOWPASS, Math.min(cutoff, sampleRate * 0.49f), 0.7f);

            float wave = osc1.next(freq, sampleRate) * 0.7f +
                    osc2.next(freq * 0.5f, sampleRate) * 0.3f;

            audio[i] = filter.process(wave) * envVal * 0.9f;
        }
        return audio;
    }
}