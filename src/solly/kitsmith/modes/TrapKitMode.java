package solly.kitsmith.modes;

import solly.kitsmith.Kit;
import solly.kitsmith.KitGenerator;

public class TrapKitMode implements Mode {

    private final KitGenerator generator = new KitGenerator();

    @Override
    public Kit generate() {
        return generator.generateFullKit();
    }

    @Override
    public String getDescription() {
        return "Classic trap kit with 808s, hi-hats, and drums. Includes:\n" +
                "• 808\n" +
                "• Snare\n" +
                "• Hi-hats\n" +
                "• Kick\n" +
                "• FX\n" +
                "• Synths";
    }

    @Override
    public String getCategory() {
        return "Trap / Hip-Hop";
    }
}