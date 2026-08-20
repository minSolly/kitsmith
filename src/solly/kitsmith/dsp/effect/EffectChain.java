package solly.kitsmith.dsp.effect;

import solly.kitsmith.dsp.AudioBuffer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EffectChain {

    private final List<Effect> stages = new ArrayList<>();

    public static EffectChain buildRandom(Random random, boolean allowLongTails) {
        EffectChain chain = new EffectChain();

        if (random.nextFloat() < 0.30f) {
            float drive = 1.5f + random.nextFloat() * 4f;
            float mix = 0.25f + random.nextFloat() * 0.4f;
            chain.stages.add(new Saturator(drive, mix));
        }

        if (random.nextFloat() < 0.18f) {
            float drive = 2f + random.nextFloat() * 6f;
            float mix = 0.2f + random.nextFloat() * 0.35f;
            chain.stages.add(new Distortion(drive, mix));
        }

        if (random.nextFloat() < 0.15f) {
            int bits = 4 + random.nextInt(6);
            int downsample = 1 + random.nextInt(6);
            float mix = 0.2f + random.nextFloat() * 0.35f;
            chain.stages.add(new BitCrusher(bits, downsample, mix));
        }

        if (allowLongTails && random.nextFloat() < 0.35f) {
            float delayMs = 60f + random.nextFloat() * 300f;
            float feedback = 0.2f + random.nextFloat() * 0.45f;
            float mix = 0.15f + random.nextFloat() * 0.3f;
            chain.stages.add(new Delay(delayMs, feedback, mix));
        }

        if (allowLongTails && random.nextFloat() < 0.45f) {
            float roomSize = 0.3f + random.nextFloat() * 0.6f;
            float damping = 0.2f + random.nextFloat() * 0.5f;
            float mix = 0.15f + random.nextFloat() * 0.35f;
            int tailMs = 400 + random.nextInt(1200);
            chain.stages.add(new Reverb(roomSize, damping, mix, tailMs));
        }

        return chain;
    }

    public float[] process(float[] input, int sampleRate) {
        return process(input, sampleRate, 0.92f);
    }

    public float[] process(float[] input, int sampleRate, float targetPeak) {
        float[] current = input;
        for (Effect stage : stages) {
            current = stage.apply(current, sampleRate);
        }
        AudioBuffer.normalize(current, targetPeak);
        return current;
    }

    public List<Effect> getStages() {
        return stages;
    }
}
