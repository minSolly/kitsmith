package solly.kitsmith.modes;

import solly.kitsmith.Kit;

@FunctionalInterface
public interface Mode {
    Kit generate();

    default String getDescription() {
        return "No description available";
    }

    default String getCategory() {
        return "General";
    }
}