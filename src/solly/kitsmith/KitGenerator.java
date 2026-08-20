package solly.kitsmith;

import solly.kitsmith.dsp.AudioBuffer;
import solly.kitsmith.dsp.AudioConstants;
import solly.kitsmith.dsp.effect.EffectChain;
import solly.kitsmith.generator.BassGenerator;
import solly.kitsmith.generator.ClapGenerator;
import solly.kitsmith.generator.FxGenerator;
import solly.kitsmith.generator.HiHatGenerator;
import solly.kitsmith.generator.KickGenerator;
import solly.kitsmith.generator.PercGenerator;
import solly.kitsmith.generator.SnareGenerator;
import solly.kitsmith.generator.SubBassGenerator;
import solly.kitsmith.generator.SynthGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.time.LocalTime;

public class KitGenerator {

    private final Random random = new Random();

    public Kit generateFullKit() {
        Kit kit = new Kit();

        int hour = LocalTime.now().getHour();
        int min = LocalTime.now().getHour();

        kit.setName("KitSmith_Kit_" + hour + ":" + min);

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
                row.add(new KitSlot(id, category, generateRaw(category)));
            }
            kit.addRow(row);
        }
        return kit;
    }

    public float[] regenerateSlot(KitSlot slot) {
        float[] fresh = generateRaw(slot.getCategory());
        slot.setAudio(fresh);
        return fresh;
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

    private float[] generateRaw(SoundCategory category) {
        float[] raw;
        boolean allowLongTails;

        switch (category) {
            case KICK:
                raw = KickGenerator.generate(random);
                allowLongTails = false;
                break;
            case SNARE:
                raw = SnareGenerator.generate(random);
                allowLongTails = true;
                break;
            case HIHAT:
                raw = HiHatGenerator.generate(random, false);
                allowLongTails = false;
                break;
            case OPEN_HAT:
                raw = HiHatGenerator.generate(random, true);
                allowLongTails = true;
                break;
            case CLAP:
                raw = ClapGenerator.generate(random);
                allowLongTails = true;
                break;
            case PERC:
                raw = PercGenerator.generate(random);
                allowLongTails = true;
                break;
            case BASS:
                raw = BassGenerator.generate(random);
                allowLongTails = false;
                break;
            case SUB_BASS:
                raw = SubBassGenerator.generate(random);
                allowLongTails = false;
                break;
            case SYNTH:
                raw = SynthGenerator.generate(random);
                allowLongTails = true;
                break;
            case FX:
            default:
                raw = FxGenerator.generate(random);
                allowLongTails = true;
                break;
        }

        AudioBuffer.normalize(raw, 0.85f);
        EffectChain chain = EffectChain.buildRandom(random, allowLongTails);
        float[] processed = chain.process(raw, AudioConstants.SAMPLE_RATE);
        AudioBuffer.fadeOutTail(processed, Math.min(processed.length, 400));
        return processed;
    }
}
