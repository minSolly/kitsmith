package solly.kitsmith;

public class KitSlot {

    private final String id;
    private final SoundCategory category;
    private float[] audio;

    public KitSlot(String id, SoundCategory category, float[] audio) {
        this.id = id;
        this.category = category;
        this.audio = audio;
    }

    public String getId() {
        return id;
    }

    public SoundCategory getCategory() {
        return category;
    }

    public float[] getAudio() {
        return audio;
    }

    public void setAudio(float[] audio) {
        this.audio = audio;
    }
}
