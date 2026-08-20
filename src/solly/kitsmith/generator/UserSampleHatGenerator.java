package solly.kitsmith.generator;

import solly.kitsmith.dsp.AudioAnalyzer;
import solly.kitsmith.dsp.NoiseGenerator;
import solly.kitsmith.dsp.Oscillator;
import solly.kitsmith.dsp.envelope.DecayEnvelope;
import solly.kitsmith.dsp.filter.BiquadFilter;

import java.util.Random;

public class UserSampleHatGenerator {

    public static float[] generate(float[] source, AudioAnalyzer.AnalysisResult analysis, int sampleRate, boolean open, Random random) {
        float freq = analysis != null ? analysis.fundamentalFreq : 400f;
        float amp = analysis != null ? analysis.peak : 0.8f;

        float duration = open ? 0.4f : 0.1f;
        int samples = (int) (sampleRate * duration);
        float[] audio = new float[samples];

        float decayRate = open ? 4f + 6f * amp : 25f + 20f * amp;
        DecayEnvelope env = new DecayEnvelope(0.001f, decayRate);
        NoiseGenerator noise = new NoiseGenerator(random);
        BiquadFilter highpass = new BiquadFilter(sampleRate);
        float cutoff = Math.min(8000f, freq * 4f);
        highpass.set(BiquadFilter.Type.HIGHPASS, cutoff, 0.8f);

        float[] ratios = {1f, 1.34f, 1.79f, 2.31f};
        Oscillator[] oscs = new Oscillator[ratios.length];
        for (int i = 0; i < oscs.length; i++) {
            oscs[i] = new Oscillator(Oscillator.Waveform.SQUARE, random.nextFloat());
        }

        for (int i = 0; i < samples; i++) {
            float t = (float) i / sampleRate;
            float metallic = 0f;
            for (int v = 0; v < oscs.length; v++) {
                metallic += oscs[v].next(freq * ratios[v], sampleRate);
            }
            metallic /= oscs.length;

            float noiseSample = highpass.process(noise.white());
            float mix = 0.3f + 0.3f * amp;
            audio[i] = (metallic * (1f - mix) + noiseSample * mix) * env.amplitudeAt(t) * amp;
        }
        return audio;
    }
}