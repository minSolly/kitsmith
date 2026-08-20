package solly.kitsmith.generator;

import solly.kitsmith.dsp.AudioAnalyzer;
import solly.kitsmith.dsp.NoiseGenerator;
import solly.kitsmith.dsp.envelope.DecayEnvelope;
import solly.kitsmith.dsp.filter.BiquadFilter;

import java.util.Random;

public class UserSampleFxGenerator {

    public static float[] generate(float[] source, AudioAnalyzer.AnalysisResult analysis, int sampleRate, Random random) {
        float freq = analysis != null ? analysis.fundamentalFreq : 400f;
        float amp = analysis != null ? analysis.peak : 0.8f;
        float dur = analysis != null ? Math.min(analysis.duration, 0.5f) : 0.3f;

        float duration = Math.min(dur, 0.5f);
        int samples = (int) (sampleRate * duration);
        float[] audio = new float[samples];

        DecayEnvelope env = new DecayEnvelope(0.005f, 8f + 10f * amp);
        NoiseGenerator noise = new NoiseGenerator(random);
        BiquadFilter filter = new BiquadFilter(sampleRate);

        float centerFreq = Math.min(3000f, freq * 2f);
        filter.set(BiquadFilter.Type.BANDPASS, centerFreq, 2f);

        for (int i = 0; i < samples; i++) {
            float t = (float) i / sampleRate;
            float sweep = centerFreq + (centerFreq * 0.5f) * (float)Math.sin(2 * Math.PI * 3 * t);
            filter.set(BiquadFilter.Type.BANDPASS, Math.min(sweep, sampleRate * 0.49f), 2f);
            audio[i] = filter.process(noise.white()) * env.amplitudeAt(t) * amp * 1.2f;
        }
        return audio;
    }
}