package solly.kitsmith;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Kit {

    private String name = "Untitled Kit";
    private final List<List<KitSlot>> rows = new ArrayList<>();
    private final Map<String, KitSlot> slotsById = new LinkedHashMap<>();

    public void addRow(List<KitSlot> row) {
        rows.add(row);
        for (KitSlot slot : row) {
            slotsById.put(slot.getId(), slot);
        }
    }

    public List<List<KitSlot>> getRows() {
        return rows;
    }

    public KitSlot getSlot(String id) {
        return slotsById.get(id);
    }

    public List<KitSlot> getAllSlots() {
        System.out.println("Total slots: " + slotsById.size());
        return new ArrayList<>(slotsById.values());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEmpty() {
        return slotsById.isEmpty();
    }
}
