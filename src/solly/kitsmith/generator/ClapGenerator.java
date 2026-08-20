
package solly.kitsmith.generator;

import solly.kitsmith.dsp.AudioConstants;
import solly.kitsmith.dsp.NoiseGenerator;
import solly.kitsmith.dsp.envelope.DecayEnvelope;
import solly.kitsmith.dsp.filter.BiquadFilter;

import java.util.Random;

public class ClapGenerator {

    public static float[] generate(Random random) {
        int sampleRate = AudioConstants.SAMPLE_RATE;
        int burstCount = 4 + random.nextInt(3);
        float burstSpacing = 0.009f + random.nextFloat() * 0.008f;
        float bandCenter = 1200f + random.nextFloat() * 1500f;
        float bandQ = 0.7f + random.nextFloat() * 0.6f;
        float burstDecayRate = 55f + random.nextFloat() * 25f;
        float overallDecayRate = 7f + random.nextFloat() * 5f;

        float duration = 0.28f + random.nextFloat() * 0.25f;
        int samples = (int) (sampleRate * duration);
        float[] audio = new float[samples];

        float[] burstTimes = new float[burstCount];
        for (int i = 0; i < burstCount; i++) {
            burstTimes[i] = i * burstSpacing;
        }

        NoiseGenerator noise = new NoiseGenerator(random);
        BiquadFilter bandpass = new BiquadFilter(sampleRate);
        bandpass.set(BiquadFilter.Type.BANDPASS, bandCenter, bandQ);
        DecayEnvelope overallEnv = new DecayEnvelope(0.001f, overallDecayRate);
        DecayEnvelope burstEnv = new DecayEnvelope(0.0005f, burstDecayRate);

        for (int i = 0; i < samples; i++) {
            float t = (float) i / sampleRate;
            float raw = bandpass.process(noise.white());

            float burstAmp = 0f;
            for (float burstTime : burstTimes) {
                if (t >= burstTime) {
                    burstAmp = Math.max(burstAmp, burstEnv.amplitudeAt(t - burstTime));
                }
            }

            audio[i] = raw * burstAmp * overallEnv.amplitudeAt(t);
        }
        return audio;
    }
}