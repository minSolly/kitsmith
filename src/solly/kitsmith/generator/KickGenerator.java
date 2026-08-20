package solly.kitsmith.generator;

import solly.kitsmith.dsp.AudioConstants;
import solly.kitsmith.dsp.NoiseGenerator;
import solly.kitsmith.dsp.Oscillator;
import solly.kitsmith.dsp.envelope.DecayEnvelope;
import solly.kitsmith.dsp.envelope.PitchDropEnvelope;
import solly.kitsmith.dsp.filter.BiquadFilter;

import java.util.Random;

public class KickGenerator {

    public static float[] generate(Random random) {
        final int sampleRate = AudioConstants.SAMPLE_RATE;

        float startFreq = 135f + random.nextFloat() * 90f;
        float endFreq = 68f + random.nextFloat() * 8f;

        float pitchDropTime =
                0.006f + random.nextFloat() * 0.002f;

        float pitchSteepness =
                9f + random.nextFloat() * 5f;

        float decayRate =
                34f + random.nextFloat() * 12f;

        float punchFreq =
                85f + random.nextFloat() * 25f;

        float punchDuration =
                0.005f + random.nextFloat() * 0.003f;

        float clickDuration =
                0.0004f + random.nextFloat() * 0.0005f;

        float bodyLevel =
                0.95f + random.nextFloat() * 0.15f;

        float punchLevel =
                0.45f + random.nextFloat() * 0.15f;

        float clickLevel =
                0.008f + random.nextFloat() * 0.012f;

        float duration =
                0.075f + random.nextFloat() * 0.035f;

        int samples =
                (int) (sampleRate * duration);

        float[] audio =
                new float[samples];

        PitchDropEnvelope pitch =
                new PitchDropEnvelope(
                        startFreq,
                        endFreq,
                        pitchDropTime,
                        pitchSteepness
                );

        DecayEnvelope bodyEnv =
                new DecayEnvelope(
                        0.0001f,
                        decayRate
                );

        Oscillator body =
                new Oscillator(
                        Oscillator.Waveform.SINE
                );

        Oscillator punch =
                new Oscillator(
                        Oscillator.Waveform.SINE
                );

        NoiseGenerator noise =
                new NoiseGenerator(random);

        BiquadFilter knockFilter =
                new BiquadFilter(sampleRate);

        knockFilter.set(
                BiquadFilter.Type.BANDPASS,
                220f + random.nextFloat() * 100f,
                0.9f
        );

        BiquadFilter bodyLowpass =
                new BiquadFilter(sampleRate);

        bodyLowpass.set(
                BiquadFilter.Type.LOWPASS,
                700f,
                0.7f
        );

        BiquadFilter clickHighpass =
                new BiquadFilter(sampleRate);

        clickHighpass.set(
                BiquadFilter.Type.HIGHPASS,
                3200f + random.nextFloat() * 1800f,
                0.7f
        );

        float saturation =
                1.5f + random.nextFloat() * 0.7f;

        for (int i = 0; i < samples; i++) {
            float t =
                    (float) i / sampleRate;

            float freq =
                    pitch.frequencyAt(t);

            float bodyEnvValue =
                    bodyEnv.amplitudeAt(t);

            float bodyTransientEnv =
                    (float) Math.exp(-t * 85f);

            float bodySample =
                    body.next(
                            freq,
                            sampleRate
                    )
                            * bodyEnvValue
                            * bodyTransientEnv
                            * bodyLevel;

            float punchSample = 0f;

            if (t < punchDuration) {
                float x =
                        t / punchDuration;

                float env =
                        1f - x;

                env *= env;
                env *= env;

                punchSample =
                        punch.next(
                                punchFreq,
                                sampleRate
                        )
                                * env
                                * punchLevel;
            }

            float knock = 0f;

            if (t < 0.006f) {
                float x =
                        t / 0.006f;

                float env =
                        1f - x;

                env *= env;
                env *= env;

                knock =
                        knockFilter.process(
                                noise.white()
                        )
                                * env
                                * 0.12f;
            }

            float click = 0f;

            if (t < clickDuration) {
                float x =
                        t / clickDuration;

                float env =
                        1f - x;

                env *= env;

                click =
                        clickHighpass.process(
                                noise.white()
                        )
                                * env
                                * clickLevel;
            }

            float mixed =
                    bodySample
                            + punchSample
                            + knock
                            + click;

            mixed *= 1.5f;

            mixed =
                    softClip(
                            mixed,
                            saturation
                    );

            mixed =
                    bodyLowpass.process(
                            mixed
                    );

            mixed =
                    hardEndEnvelope(
                            mixed,
                            t,
                            duration
                    );

            audio[i] = mixed;
        }

        float peak = 0f;

        for (float sample : audio) {
            peak = Math.max(
                    peak,
                    Math.abs(sample)
            );
        }

        if (peak > 0f) {
            float gain =
                    1.3f / peak;

            for (int i = 0; i < audio.length; i++) {
                audio[i] *= gain;
            }
        }

        return audio;
    }

    private static float hardEndEnvelope(
            float sample,
            float t,
            float duration
    ) {
        float cutoffStart =
                duration * 0.72f;

        if (t < cutoffStart) {
            return sample;
        }

        float x =
                (t - cutoffStart)
                        / (duration - cutoffStart);

        float env =
                1f - x;

        env *= env;
        env *= env;
        env *= env;

        return sample * env;
    }

    private static float softClip(
            float x,
            float drive
    ) {
        return (float) Math.tanh(
                x * drive
        );
    }
}
