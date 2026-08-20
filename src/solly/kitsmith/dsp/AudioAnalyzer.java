package solly.kitsmith.dsp;

public class AudioAnalyzer {

    public static AnalysisResult analyze(float[] audio, int sampleRate) {
        AnalysisResult result = new AnalysisResult();
        result.sampleRate = sampleRate;
        result.length = audio.length;
        result.duration = (float) audio.length / sampleRate;

        
        float sum = 0f;
        float peak = 0f;
        float rms = 0f;
        float zeroCrossings = 0f;

        for (int i = 0; i < audio.length; i++) {
            float abs = Math.abs(audio[i]);
            sum += abs;
            if (abs > peak) peak = abs;
            rms += audio[i] * audio[i];
            if (i > 0 && (audio[i] * audio[i-1] < 0)) {
                zeroCrossings++;
            }
        }

        result.peak = peak;
        result.rms = (float) Math.sqrt(rms / audio.length);
        result.average = sum / audio.length;
        result.zeroCrossings = zeroCrossings / audio.length;

        
        result.fundamentalFreq = estimateFrequency(audio, sampleRate);

        
        result.spectralCentroid = calculateSpectralCentroid(audio);
        result.lowEnergy = calculateBandEnergy(audio, 0, 200, sampleRate);
        result.midEnergy = calculateBandEnergy(audio, 200, 2000, sampleRate);
        result.highEnergy = calculateBandEnergy(audio, 2000, 8000, sampleRate);

        
        result.soundType = classifySound(result);

        return result;
    }

    private static float estimateFrequency(float[] audio, int sampleRate) {
        
        int maxLag = Math.min(audio.length / 2, sampleRate / 20); 
        float bestCorr = 0f;
        int bestLag = 1;

        for (int lag = 1; lag < maxLag; lag++) {
            float corr = 0f;
            for (int i = 0; i < audio.length - lag; i++) {
                corr += audio[i] * audio[i + lag];
            }
            corr /= (audio.length - lag);
            if (corr > bestCorr) {
                bestCorr = corr;
                bestLag = lag;
            }
        }

        if (bestLag > 0 && bestLag < sampleRate / 20) {
            return (float) sampleRate / bestLag;
        }
        return 440f; 
    }

    private static float calculateSpectralCentroid(float[] audio) {
        
        float energy = 0f;
        float weighted = 0f;
        for (int i = 1; i < audio.length; i++) {
            float diff = Math.abs(audio[i] - audio[i-1]);
            energy += diff;
            weighted += diff * i;
        }
        if (energy > 0.001f) {
            return weighted / energy / audio.length;
        }
        return 0.5f;
    }

    private static float calculateBandEnergy(float[] audio, int lowHz, int highHz, int sampleRate) {
        
        
        int lowBin = (int) (lowHz * audio.length / (float) sampleRate);
        int highBin = (int) (highHz * audio.length / (float) sampleRate);
        lowBin = Math.max(0, Math.min(lowBin, audio.length - 1));
        highBin = Math.max(lowBin + 1, Math.min(highBin, audio.length - 1));

        
        float energy = 0f;
        for (int lag = lowBin; lag < highBin && lag < audio.length / 2; lag++) {
            float corr = 0f;
            for (int i = 0; i < audio.length - lag; i += 10) {
                corr += Math.abs(audio[i] * audio[i + lag]);
            }
            energy += corr / (audio.length - lag);
        }
        return energy / (highBin - lowBin);
    }

    private static String classifySound(AnalysisResult result) {
        
        float ratio = result.highEnergy / (result.lowEnergy + 0.001f);
        float rmsPeakRatio = result.rms / (result.peak + 0.001f);

        if (result.fundamentalFreq < 150 && result.lowEnergy > result.midEnergy) {
            return "BASS";
        } else if (result.fundamentalFreq > 2000 && ratio > 2f) {
            return "HIGH_PITCH";
        } else if (result.rms > 0.1f && result.peak > 0.5f) {
            return "PERCUSSIVE";
        } else if (rmsPeakRatio > 0.3f && result.duration > 0.5f) {
            return "SUSTAINED";
        } else if (result.fundamentalFreq > 800 && result.fundamentalFreq < 2000) {
            return "MID_RANGE";
        } else if (result.zeroCrossings > 0.3f) {
            return "NOISY";
        }
        return "GENERAL";
    }

    public static class AnalysisResult {
        public int sampleRate;
        public int length;
        public float duration;
        public float peak;
        public float rms;
        public float average;
        public float zeroCrossings;
        public float fundamentalFreq;
        public float spectralCentroid;
        public float lowEnergy;
        public float midEnergy;
        public float highEnergy;
        public String soundType;

        @Override
        public String toString() {
            return String.format(
                    "Duration: %.2fs | Freq: %.1fHz | Peak: %.2f | RMS: %.3f | Type: %s",
                    duration, fundamentalFreq, peak, rms, soundType
            );
        }
    }
}