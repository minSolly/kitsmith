package solly.kitsmith.generator;

import solly.kitsmith.dsp.AudioAnalyzer;
import solly.kitsmith.dsp.Oscillator;
import solly.kitsmith.dsp.envelope.DecayEnvelope;
import solly.kitsmith.dsp.envelope.PitchDropEnvelope;

import java.util.Random;

public class UserSampleKickGenerator {

    public static float[] generate(float[] source, AudioAnalyzer.AnalysisResult analysis, int sampleRate, Random random) {
        float freq = analysis != null ? analysis.fundamentalFreq : 100f;
        float amp = analysis != null ? analysis.peak : 0.8f;
        float dur = analysis != null ? Math.min(analysis.duration, 0.3f) : 0.2f;

        float startFreq = Math.min(250f, freq * 1.5f);
        float endFreq = Math.min(80f, freq * 0.7f);
        float duration = Math.min(dur, 0.3f);
        float decayRate = 25f + 25f * amp;

        int samples = (int) (sampleRate * duration);
        float[] audio = new float[samples];

        PitchDropEnvelope pitch = new PitchDropEnvelope(startFreq, endFreq, 0.05f, 10f);
        DecayEnvelope env = new DecayEnvelope(0.001f, decayRate);
        Oscillator osc = new Oscillator(Oscillator.Waveform.SINE);

        for (int i = 0; i < samples; i++) {
            float t = (float) i / sampleRate;
            audio[i] = osc.next(pitch.frequencyAt(t), sampleRate) * env.amplitudeAt(t) * amp * 1.2f;
        }
        return audio;
    }
}