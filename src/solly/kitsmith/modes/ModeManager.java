package solly.kitsmith.modes;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ModeManager {

    private final Map<String, ModeInfo> modes = new LinkedHashMap<>();

    public ModeManager() {
        registerDefaultModes();
    }

    private void registerDefaultModes() {
        
        registerMode(new ModeInfo(
                "trap_kit",
                "Trap Kit",
                "Classic trap kit with 808s, hi-hats, and aggressive drums. Includes:\n" +
                        "• Heavy 808 bass with pitch glide\n" +
                        "• Snappy snare with layered noise\n" +
                        "• Metallic hi-hats (open & closed)\n" +
                        "• Deep kicks with punch and click\n" +
                        "• Synth leads and FX risers",
                "Trap / Hip-Hop",
                new TrapKitMode()
        ));

        
        registerMode(new ModeInfo(
                "user_sample",
                "User Sample",
                "Load your own audio file and generate a full kit based on it!\n" +
                        "Each sound in the kit will be a variation of your sample.",
                "User Samples",
                new UserSampleMode()
        ));
    }

    public void registerMode(ModeInfo modeInfo) {
        modes.put(modeInfo.getId(), modeInfo);
    }

    public ModeInfo getMode(String id) {
        return modes.get(id);
    }

    public List<ModeInfo> getModes() {
        return new ArrayList<>(modes.values());
    }

    public List<ModeInfo> getModesByCategory(String category) {
        List<ModeInfo> result = new ArrayList<>();
        for (ModeInfo mode : modes.values()) {
            if (mode.getCategory().equals(category)) {
                result.add(mode);
            }
        }
        return result;
    }

    public List<String> getCategories() {
        List<String> categories = new ArrayList<>();
        for (ModeInfo mode : modes.values()) {
            if (!categories.contains(mode.getCategory())) {
                categories.add(mode.getCategory());
            }
        }
        return categories;
    }
}