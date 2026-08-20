package solly.kitsmith.generator;

import solly.kitsmith.dsp.AudioAnalyzer;
import solly.kitsmith.dsp.NoiseGenerator;
import solly.kitsmith.dsp.envelope.DecayEnvelope;
import solly.kitsmith.dsp.filter.BiquadFilter;

import java.util.Random;

public class UserSampleClapGenerator {

    public static float[] generate(float[] source, AudioAnalyzer.AnalysisResult analysis, int sampleRate, Random random) {
        float freq = analysis != null ? analysis.fundamentalFreq : 200f;
        float amp = analysis != null ? analysis.peak : 0.8f;

        int samples = (int) (sampleRate * 0.3f);
        float[] audio = new float[samples];

        DecayEnvelope env = new DecayEnvelope(0.001f, 40f + 30f * amp);
        NoiseGenerator noise = new NoiseGenerator(random);
        BiquadFilter filter = new BiquadFilter(sampleRate);
        float cutoff = Math.min(4000f, freq * 2.5f);
        filter.set(BiquadFilter.Type.BANDPASS, cutoff, 1.5f);

        for (int i = 0; i < samples; i++) {
            float t = (float) i / sampleRate;
            float envVal = env.amplitudeAt(t);
            float pulse = 0f;
            for (int p = 0; p < 5; p++) {
                float offset = p * 0.015f;
                if (t >= offset) {
                    pulse += (float)Math.exp(-(t - offset) * 60f);
                }
            }
            audio[i] = filter.process(noise.white()) * envVal * pulse * amp * 0.8f;
        }
        return audio;
    }
}