package solly.kitsmith.generator;

import solly.kitsmith.dsp.AudioConstants;
import solly.kitsmith.dsp.NoiseGenerator;
import solly.kitsmith.dsp.Oscillator;
import solly.kitsmith.dsp.envelope.DecayEnvelope;
import solly.kitsmith.dsp.filter.BiquadFilter;

import java.util.Random;

public class SnareGenerator {

    public static float[] generate(Random random) {
        int sampleRate = AudioConstants.SAMPLE_RATE;
        float toneFreq = 160f + random.nextFloat() * 110f;
        float toneDecay = 12f + random.nextFloat() * 10f;
        float noiseDecay = 8f + random.nextFloat() * 8f;
        float noiseAmount = 0.55f + random.nextFloat() * 0.3f;
        float toneAmount = 1f - noiseAmount;
        float bandCenter = 1600f + random.nextFloat() * 2200f;
        float bandQ = 0.6f + random.nextFloat() * 0.8f;

        float duration = 0.3f + random.nextFloat() * 0.25f;
        int samples = (int) (sampleRate * duration);
        float[] audio = new float[samples];

        Oscillator tone = new Oscillator(Oscillator.Waveform.TRIANGLE);
        DecayEnvelope toneEnv = new DecayEnvelope(0.001f, toneDecay);
        DecayEnvelope noiseEnv = new DecayEnvelope(0.001f, noiseDecay);
        NoiseGenerator noise = new NoiseGenerator(random);
        BiquadFilter bandpass = new BiquadFilter(sampleRate);
        bandpass.set(BiquadFilter.Type.BANDPASS, bandCenter, bandQ);
        BiquadFilter highpass = new BiquadFilter(sampleRate);
        highpass.set(BiquadFilter.Type.HIGHPASS, 400f, 0.7f);

        for (int i = 0; i < samples; i++) {
            float t = (float) i / sampleRate;
            float toneSample = tone.next(toneFreq, sampleRate) * toneEnv.amplitudeAt(t) * toneAmount;
            float noiseSample = bandpass.process(noise.white());
            noiseSample = highpass.process(noiseSample) * noiseEnv.amplitudeAt(t) * noiseAmount;
            audio[i] = toneSample + noiseSample;
        }
        return audio;
    }
}
