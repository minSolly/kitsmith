package solly.kitsmith.generator;

import solly.kitsmith.dsp.AudioAnalyzer;
import solly.kitsmith.dsp.NoiseGenerator;
import solly.kitsmith.dsp.Oscillator;
import solly.kitsmith.dsp.envelope.DecayEnvelope;

import java.util.Random;

public class UserSamplePercGenerator {

    public static float[] generate(float[] source, AudioAnalyzer.AnalysisResult analysis, int sampleRate, Random random) {
        float freq = analysis != null ? analysis.fundamentalFreq : 300f;
        float amp = analysis != null ? analysis.peak : 0.8f;
        float dur = analysis != null ? Math.min(analysis.duration, 0.3f) : 0.2f;

        int samples = (int) (sampleRate * Math.min(dur, 0.3f));
        float[] audio = new float[samples];

        DecayEnvelope env = new DecayEnvelope(0.002f, 10f + 15f * amp);
        Oscillator osc = new Oscillator(Oscillator.Waveform.SINE);
        NoiseGenerator noise = new NoiseGenerator(random);
        float noiseMix = 0.1f + 0.3f * amp;

        for (int i = 0; i < samples; i++) {
            float t = (float) i / sampleRate;
            float tone = osc.next(freq * (1f - t * 0.5f), sampleRate);
            float noiseSample = noise.white() * 0.3f;
            audio[i] = (tone * (1f - noiseMix) + noiseSample * noiseMix) * env.amplitudeAt(t) * amp;
        }
        return audio;
    }
}