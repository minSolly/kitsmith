package solly.kitsmith.generator;

import solly.kitsmith.dsp.AudioAnalyzer;
import solly.kitsmith.dsp.Oscillator;
import solly.kitsmith.dsp.envelope.AdsrEnvelope;
import solly.kitsmith.dsp.filter.BiquadFilter;

import java.util.Random;

public class UserSampleSynthGenerator {

    public static float[] generate(float[] source, AudioAnalyzer.AnalysisResult analysis, int sampleRate, Random random) {
        float freq = analysis != null ? analysis.fundamentalFreq : 440f;
        float amp = analysis != null ? analysis.peak : 0.8f;
        float dur = analysis != null ? Math.min(analysis.duration, 0.6f) : 0.4f;

        float duration = Math.min(dur, 0.6f);
        int samples = (int) (sampleRate * duration);
        float[] audio = new float[samples];

        AdsrEnvelope env = new AdsrEnvelope(0.01f, 0.05f, 0.5f, 0.1f, duration - 0.1f);
        Oscillator osc = new Oscillator(Oscillator.Waveform.SAW, random.nextFloat());
        BiquadFilter filter = new BiquadFilter(sampleRate);

        float baseFreq = Math.min(800f, freq);

        for (int i = 0; i < samples; i++) {
            float t = (float) i / sampleRate;
            float envVal = env.amplitudeAt(t);
            float cutoff = 300f + 2000f * envVal;
            filter.set(BiquadFilter.Type.LOWPASS, Math.min(cutoff, sampleRate * 0.49f), 1.0f);
            audio[i] = filter.process(osc.next(baseFreq, sampleRate)) * envVal * amp;
        }
        return audio;
    }
}