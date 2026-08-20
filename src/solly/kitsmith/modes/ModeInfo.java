package solly.kitsmith.modes;

public class ModeInfo {
    private final String id;
    private final String name;
    private final String description;
    private final String category;
    private final Mode mode;

    public ModeInfo(String id, String name, String description, String category, Mode mode) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.mode = mode;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getCategory() { return category; }
    public Mode getMode() { return mode; }
}