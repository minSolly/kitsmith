package solly.kitsmith.generator;

import solly.kitsmith.dsp.AudioAnalyzer;
import solly.kitsmith.dsp.NoiseGenerator;
import solly.kitsmith.dsp.envelope.DecayEnvelope;
import solly.kitsmith.dsp.filter.BiquadFilter;

import java.util.Random;

public class UserSampleSnareGenerator {

    public static float[] generate(float[] source, AudioAnalyzer.AnalysisResult analysis, int sampleRate, Random random) {
        float freq = analysis != null ? analysis.fundamentalFreq : 200f;
        float amp = analysis != null ? analysis.peak : 0.8f;
        float dur = analysis != null ? Math.min(analysis.duration, 0.4f) : 0.3f;

        float duration = Math.min(dur, 0.4f);
        float decayRate = 8f + 15f * amp;

        int samples = (int) (sampleRate * duration);
        float[] audio = new float[samples];

        DecayEnvelope env = new DecayEnvelope(0.002f, decayRate);
        NoiseGenerator noise = new NoiseGenerator(random);
        BiquadFilter filter = new BiquadFilter(sampleRate);
        float cutoff = Math.min(5000f, freq * 3f);
        filter.set(BiquadFilter.Type.BANDPASS, cutoff, 1.2f);

        for (int i = 0; i < samples; i++) {
            float t = (float) i / sampleRate;
            float noiseSample = filter.process(noise.white()) * amp;
            audio[i] = noiseSample * env.amplitudeAt(t);
        }
        return audio;
    }
}