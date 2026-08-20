package solly.kitsmith.modes;

import solly.kitsmith.Kit;
import solly.kitsmith.KitSlot;
import solly.kitsmith.SoundCategory;
import solly.kitsmith.dsp.AudioAnalyzer;
import solly.kitsmith.dsp.AudioBuffer;
import solly.kitsmith.dsp.AudioConstants;
import solly.kitsmith.dsp.effect.EffectChain;
import solly.kitsmith.generator.*;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class UserSampleMode implements Mode {

    private File loadedFile;
    private float[] loadedAudio;
    private int sampleRate = AudioConstants.SAMPLE_RATE;
    private AudioAnalyzer.AnalysisResult analysis;
    private final Random random = new Random();

    public UserSampleMode() {}

    public void loadAudioFile(File file) throws IOException, UnsupportedAudioFileException {
        this.loadedFile = file;
        this.loadedAudio = loadAudioToFloat(file);
        this.analysis = AudioAnalyzer.analyze(loadedAudio, this.sampleRate);
    }

    public boolean hasAudio() {
        return loadedAudio != null && loadedAudio.length > 0;
    }

    public float[] getLoadedAudio() {
        return loadedAudio;
    }

    public AudioAnalyzer.AnalysisResult getAnalysis() {
        return analysis;
    }

    public String getFileName() {
        return loadedFile != null ? loadedFile.getName() : "No file loaded";
    }

    private float[] loadAudioToFloat(File file) throws IOException, UnsupportedAudioFileException {
        AudioInputStream stream = AudioSystem.getAudioInputStream(file);
        AudioFormat format = stream.getFormat();

        if (format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
            AudioFormat targetFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    format.getSampleRate(),
                    16,
                    format.getChannels(),
                    format.getChannels() * 2,
                    format.getSampleRate(),
                    false
            );
            stream = AudioSystem.getAudioInputStream(targetFormat, stream);
            format = targetFormat;
        }

        this.sampleRate = (int) format.getSampleRate();
        int channels = format.getChannels();
        int bytesPerSample = format.getSampleSizeInBits() / 8;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int read;
        while ((read = stream.read(buffer)) != -1) {
            baos.write(buffer, 0, read);
        }
        byte[] bytes = baos.toByteArray();
        stream.close();

        int totalSamples = bytes.length / (bytesPerSample * channels);
        float[] audio = new float[totalSamples];

        for (int i = 0; i < totalSamples; i++) {
            int sampleIndex = i * bytesPerSample * channels;
            int sample = 0;
            for (int b = 0; b < bytesPerSample; b++) {
                if (sampleIndex + b < bytes.length) {
                    sample |= (bytes[sampleIndex + b] & 0xFF) << (b * 8);
                }
            }
            float value = sample / (float) Math.pow(2, bytesPerSample * 8 - 1);
            if (value < -1f) value = -1f;
            if (value > 1f) value = 1f;

            if (channels == 2 && i % 2 == 0) {
                int rightIdx = sampleIndex + bytesPerSample;
                int rightSample = 0;
                for (int b = 0; b < bytesPerSample; b++) {
                    if (rightIdx + b < bytes.length) {
                        rightSample |= (bytes[rightIdx + b] & 0xFF) << (b * 8);
                    }
                }
                float rightValue = rightSample / (float) Math.pow(2, bytesPerSample * 8 - 1);
                if (rightValue < -1f) rightValue = -1f;
                if (rightValue > 1f) rightValue = 1f;
                value = (value + rightValue) / 2f;
            }
            audio[i / channels] = value;
        }

        AudioBuffer.normalize(audio, 0.9f);
        return audio;
    }

    private float[] generateFromAnalysis(SoundCategory category) {
        if (analysis == null) {
            return generateDefault(category);
        }

        float[] result;
        switch (category) {
            case KICK:
                result = UserSampleKickGenerator.generate(loadedAudio, analysis, sampleRate, random);
                break;
            case SNARE:
                result = UserSampleSnareGenerator.generate(loadedAudio, analysis, sampleRate, random);
                break;
            case CLAP:
                result = UserSampleClapGenerator.generate(loadedAudio, analysis, sampleRate, random);
                break;
            case HIHAT:
                result = UserSampleHatGenerator.generate(loadedAudio, analysis, sampleRate, false, random);
                break;
            case OPEN_HAT:
                result = UserSampleHatGenerator.generate(loadedAudio, analysis, sampleRate, true, random);
                break;
            case PERC:
                result = UserSamplePercGenerator.generate(loadedAudio, analysis, sampleRate, random);
                break;
            case BASS:
                result = UserSampleBassGenerator.generate(loadedAudio, analysis, sampleRate, random);
                break;
            case SYNTH:
                result = UserSampleSynthGenerator.generate(loadedAudio, analysis, sampleRate, random);
                break;
            case FX:
            default:
                result = UserSampleFxGenerator.generate(loadedAudio, analysis, sampleRate, random);
                break;
        }

        
        boolean allowLongTails = category == SoundCategory.SNARE ||
                category == SoundCategory.OPEN_HAT ||
                category == SoundCategory.CLAP ||
                category == SoundCategory.SYNTH ||
                category == SoundCategory.FX;
        EffectChain chain = EffectChain.buildRandom(random, allowLongTails);
        result = chain.process(result, sampleRate);
        AudioBuffer.normalize(result, 0.85f);
        AudioBuffer.fadeOutTail(result, Math.min(result.length, 400));

        return result;
    }

    private float[] generateDefault(SoundCategory category) {
        switch (category) {
            case KICK: return KickGenerator.generate(random);
            case SNARE: return SnareGenerator.generate(random);
            case CLAP: return ClapGenerator.generate(random);
            case HIHAT: return HiHatGenerator.generate(random, false);
            case OPEN_HAT: return HiHatGenerator.generate(random, true);
            case PERC: return PercGenerator.generate(random);
            case BASS: return BassGenerator.generate(random);
            case SUB_BASS: return BassGenerator.generate(random);
            case SYNTH: return SynthGenerator.generate(random);
            default: return FxGenerator.generate(random);
        }
    }

    @Override
    public Kit generate() {
        Kit kit = new Kit();
        kit.setName("UserSample_Kit_" + System.currentTimeMillis() % 10000);

        
        String[][] layout = {
                {"SYNTHS 1", "SYNTHS 2", "SYNTHS 3"},
                {"PERCS 1", "PERCS 2"},
                {"KICK", "SNARE", "CLAP"},
                {"HAT", "OPEN HAT", "FX 1"},
                {"BASS", "SUB BASS"}
        };

        for (String[] rowIds : layout) {
            List<KitSlot> row = new ArrayList<>();
            for (String id : rowIds) {
                SoundCategory category = categoryForId(id);
                float[] audio;
                if (hasAudio()) {
                    audio = generateFromAnalysis(category);
                } else {
                    audio = generateDefault(category);
                }
                row.add(new KitSlot(id, category, audio));
            }
            kit.addRow(row);
        }
        return kit;
    }

    private SoundCategory categoryForId(String id) {
        if (id.startsWith("SYNTHS")) return SoundCategory.SYNTH;
        if (id.startsWith("PERCS")) return SoundCategory.PERC;
        if (id.equals("KICK")) return SoundCategory.KICK;
        if (id.equals("SNARE")) return SoundCategory.SNARE;
        if (id.equals("CLAP")) return SoundCategory.CLAP;
        if (id.equals("HAT")) return SoundCategory.HIHAT;
        if (id.equals("OPEN HAT")) return SoundCategory.OPEN_HAT;
        if (id.startsWith("FX")) return SoundCategory.FX;
        if (id.equals("BASS")) return SoundCategory.BASS;
        if (id.equals("SUB BASS")) return SoundCategory.SUB_BASS;
        return SoundCategory.FX;
    }

    @Override
    public String getDescription() {
        if (hasAudio() && analysis != null) {
            return "User Sample Mode\n\n" +
                    "Loaded: " + getFileName();
        } else {
            return "User Sample Mode\n\n" +
                    "Load your own audio file (WAV/MP3/OGG)\n" +
                    "and generate a full kit based on it!\n\n";
        }
    }

    @Override
    public String getCategory() {
        return "User Samples";
    }
}