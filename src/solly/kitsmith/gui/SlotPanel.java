package solly.kitsmith.gui;

import solly.kitsmith.KitGenerator;
import solly.kitsmith.KitSlot;
import solly.kitsmith.audio.AudioCache;
import solly.kitsmith.audio.AudioEngine;
import solly.kitsmith.dsp.AudioConstants;
import solly.kitsmith.export.AudioExporter;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.function.Consumer;

public class SlotPanel extends JPanel {

    private final KitSlot slot;
    private final KitGenerator generator;
    private final WaveformPanel waveformPanel;
    private final Consumer<String> statusSink;
    private boolean menuHovered = false;

    public SlotPanel(KitSlot slot, KitGenerator generator, Consumer<String> statusSink) {
        this.slot = slot;
        this.generator = generator;
        this.statusSink = statusSink;

        setLayout(new BorderLayout());
        setBackground(Theme.PANEL_BACKGROUND);
        setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1));
        setPreferredSize(new Dimension(200, 110));

        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (menuHovered) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(224, 68, 55, 30));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
                    g2.dispose();
                }
            }
        };
        header.setBackground(Theme.PANEL_HEADER);
        header.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 4));

        JLabel label = new JLabel(slot.getId());
        label.setForeground(Theme.TEXT_PRIMARY);
        label.setFont(Theme.FONT_LABEL);
        header.add(label, BorderLayout.WEST);

        JButton menuButton = createMenuButton();

        JPopupMenu menu = buildMenu();
        menuButton.addActionListener(e -> {
            menu.setLightWeightPopupEnabled(true);
            menu.show(menuButton, menuButton.getWidth() - 40, menuButton.getHeight() + 2);
        });

        menuButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                menuHovered = true;
                header.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                menuHovered = false;
                header.repaint();
            }
        });

        header.add(menuButton, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        waveformPanel = new WaveformPanel();
        waveformPanel.setSlot(slot);
        add(waveformPanel, BorderLayout.CENTER);
    }

    private JButton createMenuButton() {
        JButton button = new JButton("\u25BE") {
            @Override
            protected void paintComponent(Graphics g) {
                if (getModel().isRollover()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(224, 68, 55, 40));
                    g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 6, 6);
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        button.setForeground(Theme.TEXT_SECONDARY);
        button.setBackground(Theme.PANEL_HEADER);
        button.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFont(Theme.FONT_BUTTON);
        button.setPreferredSize(new Dimension(24, 24));
        return button;
    }

    private JPopupMenu buildMenu() {
        ThemedPopupMenu menu = new ThemedPopupMenu();

        ThemedMenuItem play = new ThemedMenuItem("Play");
        play.addActionListener(e -> {
            AudioEngine.getInstance().play(slot.getAudio(), AudioConstants.SAMPLE_RATE);
        });
        menu.add(play);

        ThemedMenuItem regenerate = new ThemedMenuItem("Regenerate");
        regenerate.addActionListener(e -> regenerate());
        menu.add(regenerate);

        ThemedMenuItem export = new ThemedMenuItem("Export WAV...");
        export.addActionListener(e -> exportWav());
        menu.add(export);

        return menu;
    }

    private void regenerate() {
        generator.regenerateSlot(slot);
        AudioCache.getInstance().invalidate(slot);
        waveformPanel.setSlot(slot);
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