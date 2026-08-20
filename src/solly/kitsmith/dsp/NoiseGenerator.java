package solly.kitsmith.dsp;

import java.util.Random;

public class NoiseGenerator {

    private final Random random;
    private float b0, b1, b2, b3, b4, b5, b6;

    public NoiseGenerator(Random random) {
        this.random = random;
    }

    public float white() {
        return random.nextFloat() * 2f - 1f;
    }

    public float pink() {
        float w = white();
        b0 = 0.99886f * b0 + w * 0.0555179f;
        b1 = 0.99332f * b1 + w * 0.0750759f;
        b2 = 0.96900f * b2 + w * 0.1538520f;
        b3 = 0.86650f * b3 + w * 0.3104856f;
        b4 = 0.55000f * b4 + w * 0.5329522f;
        b5 = -0.7616f * b5 - w * 0.0168980f;
        float out = b0 + b1 + b2 + b3 + b4 + b5 + b6 + w * 0.5362f;
        b6 = w * 0.115926f;
        return out * 0.11f;
    }
}
