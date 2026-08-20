package solly.kitsmith.generator;

import solly.kitsmith.dsp.AudioAnalyzer;
import solly.kitsmith.dsp.Oscillator;
import solly.kitsmith.dsp.envelope.AdsrEnvelope;
import solly.kitsmith.dsp.filter.BiquadFilter;

import java.util.Random;

public class UserSampleBassGenerator {

    public static float[] generate(float[] source, AudioAnalyzer.AnalysisResult analysis, int sampleRate, Random random) {
        float freq = analysis != null ? analysis.fundamentalFreq : 80f;
        float amp = analysis != null ? analysis.peak : 0.8f;
        float dur = analysis != null ? Math.min(analysis.duration, 0.8f) : 0.6f;

        float duration = Math.min(dur, 0.8f);
        int samples = (int) (sampleRate * duration);
        float[] audio = new float[samples];

        AdsrEnvelope env = new AdsrEnvelope(0.005f, 0.1f, 0.7f, 0.2f, duration - 0.2f);
        Oscillator osc = new Oscillator(Oscillator.Waveform.SAW);
        BiquadFilter filter = new BiquadFilter(sampleRate);

        float baseFreq = Math.min(120f, freq * 0.8f);

        for (int i = 0; i < samples; i++) {
            float t = (float) i / sampleRate;
            float envVal = env.amplitudeAt(t);
            float cutoff = 200f + 600f * envVal;
            filter.set(BiquadFilter.Type.LOWPASS, Math.min(cutoff, sampleRate * 0.49f), 0.8f);
            audio[i] = filter.process(osc.next(baseFreq, sampleRate)) * envVal * amp * 1.1f;
        }
        return audio;
    }
}