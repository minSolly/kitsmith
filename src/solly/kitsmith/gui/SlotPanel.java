package solly.kitsmith.gui;

import solly.kitsmith.KitGenerator;
import solly.kitsmith.KitSlot;
import solly.kitsmith.audio.AudioEngine;
import solly.kitsmith.dsp.AudioConstants;
import solly.kitsmith.export.AudioExporter;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.io.IOException;
import java.util.function.Consumer;

public class SlotPanel extends JPanel {

    private final KitSlot slot;
    private final KitGenerator generator;
    private final WaveformPanel waveformPanel;
    private final Consumer<String> statusSink;

    public SlotPanel(KitSlot slot, KitGenerator generator, Consumer<String> statusSink) {
        this.slot = slot;
        this.generator = generator;
        this.statusSink = statusSink;

        setLayout(new BorderLayout());
        setBackground(Theme.PANEL_BACKGROUND);
        setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1));
        setPreferredSize(new Dimension(200, 110));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.PANEL_HEADER);
        header.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 4));

        JLabel label = new JLabel(slot.getId());
        label.setForeground(Theme.TEXT_PRIMARY);
        label.setFont(Theme.FONT_LABEL);
        header.add(label, BorderLayout.WEST);

        JButton menuButton = new JButton("\u25BE");
        menuButton.setForeground(Theme.TEXT_SECONDARY);
        menuButton.setBackground(Theme.PANEL_HEADER);
        menuButton.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 0));
        menuButton.setFocusPainted(false);
        menuButton.setContentAreaFilled(false);
        menuButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPopupMenu menu = buildMenu();
        menuButton.addActionListener(e -> menu.show(menuButton, 0, menuButton.getHeight()));
        header.add(menuButton, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        waveformPanel = new WaveformPanel();
        waveformPanel.setAudio(slot.getAudio());
        add(waveformPanel, BorderLayout.CENTER);
    }

    private JPopupMenu buildMenu() {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem play = new JMenuItem("Play");
        play.addActionListener(e -> AudioEngine.getInstance().play(slot.getAudio(), AudioConstants.SAMPLE_RATE));
        menu.add(play);

        JMenuItem regenerate = new JMenuItem("Regenerate");
        regenerate.addActionListener(e -> regenerate());
        menu.add(regenerate);

        JMenuItem export = new JMenuItem("Export WAV...");
        export.addActionListener(e -> exportWav());
        menu.add(export);

        return menu;
    }

    private void regenerate() {
        generator.regenerateSlot(slot);
        waveformPanel.setAudio(slot.getAudio());
        statusSink.accept("Regenerated: " + slot.getId());
    }

    private void exportWav() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new java.io.File(slot.getId().replace(" ", "_") + ".wav"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                java.io.File file = chooser.getSelectedFile();
                if (!file.getName().toLowerCase().endsWith(".wav")) {
                    file = new java.io.File(file.getPath() + ".wav");
                }
                AudioExporter.exportWav(slot.getAudio(), file.getPath(), AudioConstants.SAMPLE_RATE);
                statusSink.accept("Exported: " + file.getName());
            } catch (IOException ex) {
                statusSink.accept("Export error: " + ex.getMessage());
            }
        }
    }
}
