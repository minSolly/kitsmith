package solly.kitsmith.gui;

import solly.kitsmith.Kit;
import solly.kitsmith.KitGenerator;
import solly.kitsmith.KitSlot;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
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
        if (kit == null || kit.isEmpty()) {
            JPanel emptyPanel = new JPanel();
            emptyPanel.setBackground(Theme.BACKGROUND);
            emptyPanel.setLayout(new BoxLayout(emptyPanel, BoxLayout.Y_AXIS));

            JLabel emptyLabel = new JLabel("No kit generated yet");
            emptyLabel.setFont(Theme.FONT_LABEL);
            emptyLabel.setForeground(Theme.TEXT_SECONDARY);
            emptyLabel.setAlignmentX(CENTER_ALIGNMENT);
            emptyPanel.add(Box.createVerticalGlue());
            emptyPanel.add(emptyLabel);
            emptyPanel.add(Box.createVerticalGlue());
            add(emptyPanel);
        } else {
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
        }
        revalidate();
        repaint();
    }
}