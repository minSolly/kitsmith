package solly.kitsmith.gui;

import solly.kitsmith.Kit;
import solly.kitsmith.KitGenerator;
import solly.kitsmith.KitSlot;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import java.awt.GridLayout;
import java.util.function.Consumer;

public class KitPanel extends JPanel {

    private final KitGenerator generator;
    private final Consumer<String> statusSink;

    public KitPanel(KitGenerator generator, Consumer<String> statusSink) {
        this.generator = generator;
        this.statusSink = statusSink;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(6, 10, 10, 10));
    }

    public void showKit(Kit kit) {
        removeAll();
        for (java.util.List<KitSlot> row : kit.getRows()) {
            JPanel rowPanel = new JPanel(new GridLayout(1, row.size(), 8, 8));
            rowPanel.setBackground(Theme.BACKGROUND);
            rowPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
            rowPanel.setAlignmentX(LEFT_ALIGNMENT);
            rowPanel.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 130));

            for (KitSlot slot : row) {
                rowPanel.add(new SlotPanel(slot, generator, statusSink));
            }
            add(rowPanel);
        }
        revalidate();
        repaint();
    }
}
