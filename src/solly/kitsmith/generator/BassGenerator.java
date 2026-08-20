
package solly.kitsmith.generator;

import solly.kitsmith.dsp.AudioConstants;
import solly.kitsmith.dsp.Oscillator;
import solly.kitsmith.dsp.envelope.AdsrEnvelope;
import solly.kitsmith.dsp.filter.BiquadFilter;

import java.util.Random;

public class BassGenerator {

    public static float[] generate(Random random) {
        int sampleRate = AudioConstants.SAMPLE_RATE;
        float freq = 45f + random.nextFloat() * 45f;
        float detune = 0.3f + random.nextFloat() * 1.2f;
        float attack = 0.005f + random.nextFloat() * 0.03f;
        float decay = 0.15f + random.nextFloat() * 0.25f;
        float sustain = 0.4f + random.nextFloat() * 0.4f;
        float filterStart = 2200f + random.nextFloat() * 2500f;
        float filterEnd = 200f + random.nextFloat() * 400f;
        float filterDecayRate = 3f + random.nextFloat() * 5f;
        float resonance = 0.7f + random.nextFloat() * 1.3f;

        float duration = 0.6f + random.nextFloat() * 0.2f;
        int samples = (int) (sampleRate * duration);
        float[] audio = new float[samples];

        Oscillator sawA = new Oscillator(Oscillator.Waveform.SAW);
        Oscillator sawB = new Oscillator(Oscillator.Waveform.SAW, 0.33f);
        Oscillator sub = new Oscillator(Oscillator.Waveform.SINE);
        AdsrEnvelope env = new AdsrEnvelope(attack, decay, sustain, 0.15f, duration - 0.15f);
        BiquadFilter filter = new BiquadFilter(sampleRate);

        for (int i = 0; i < samples; i++) {
            float t = (float) i / sampleRate;
            float cutoff = filterEnd + (filterStart - filterEnd) * (float) Math.exp(-t * filterDecayRate);
            filter.set(BiquadFilter.Type.LOWPASS, cutoff, resonance);

            float wave = sawA.next(freq, sampleRate) * 0.5f
                    + sawB.next(freq * (1f + detune * 0.01f), sampleRate) * 0.35f
                    + sub.next(freq * 0.5f, sampleRate) * 0.35f;

            audio[i] = filter.process(wave) * env.amplitudeAt(t);
        }
        return audio;
    }
}