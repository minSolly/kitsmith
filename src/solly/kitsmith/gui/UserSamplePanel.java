package solly.kitsmith.gui;

import solly.kitsmith.Kit;
import solly.kitsmith.dsp.AudioAnalyzer;
import solly.kitsmith.modes.UserSampleMode;

import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

public class UserSamplePanel extends JPanel {

    private final UserSampleMode mode;
    private final Consumer<String> statusSink;
    private final Consumer<Kit> kitSink;
    private JLabel fileNameLabel;
    private JLabel analysisLabel;
    private JButton loadButton;
    private JButton generateButton;
    private JPanel previewPanel;

    public UserSamplePanel(UserSampleMode mode, Consumer<String> statusSink, Consumer<Kit> kitSink) {
        this.mode = mode;
        this.statusSink = statusSink;
        this.kitSink = kitSink;

        setLayout(new BorderLayout());
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        
        JPanel centerPanel = createCenterPanel();
        add(centerPanel, BorderLayout.CENTER);

        
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Theme.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel title = new JLabel("KitSmith", SwingConstants.CENTER);
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("USER SAMPLE MODE", SwingConstants.CENTER);
        subtitle.setFont(Theme.FONT_SUBTITLE);
        subtitle.setForeground(Theme.TEXT_SECONDARY);
        subtitle.setAlignmentX(CENTER_ALIGNMENT);

        panel.add(title);
        panel.add(Box.createVerticalStrut(4));
        panel.add(subtitle);

        return panel;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Theme.BACKGROUND);
        panel.setAlignmentX(CENTER_ALIGNMENT);

        
        JPanel loadPanel = createLoadPanel();
        loadPanel.setAlignmentX(CENTER_ALIGNMENT);
        loadPanel.setMaximumSize(new Dimension(600, 120));
        panel.add(loadPanel);
        panel.add(Box.createVerticalStrut(20));

        
        JPanel infoPanel = createInfoPanel();
        infoPanel.setAlignmentX(CENTER_ALIGNMENT);
        infoPanel.setMaximumSize(new Dimension(600, 100));
        panel.add(infoPanel);

        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private JPanel createLoadPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Theme.PANEL_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.BORDER, 1),
                BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));

        JLabel label = new JLabel("CHOOSE YOUR FILE");
        label.setFont(Theme.FONT_LABEL);
        label.setForeground(Theme.TEXT_SECONDARY);
        label.setAlignmentX(CENTER_ALIGNMENT);
        panel.add(label);
        panel.add(Box.createVerticalStrut(12));

        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        row.setBackground(Theme.PANEL_BACKGROUND);

        loadButton = createStyledButton("BROWSE", false);
        loadButton.setPreferredSize(new Dimension(120, 38));
        loadButton.addActionListener(e -> loadAudioFile());

        fileNameLabel = new JLabel("No file selected");
        fileNameLabel.setFont(Theme.FONT_STATUS);
        fileNameLabel.setForeground(Theme.TEXT_SECONDARY);

        row.add(loadButton);
        row.add(fileNameLabel);
        panel.add(row);

        return panel;
    }

    private JPanel createInfoPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Theme.PANEL_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.BORDER, 1),
                BorderFactory.createEmptyBorder(16, 30, 16, 30)
        ));

        analysisLabel = new JLabel("Load a file to see analysis");
        analysisLabel.setFont(Theme.FONT_STATUS);
        analysisLabel.setForeground(Theme.TEXT_SECONDARY);
        analysisLabel.setAlignmentX(CENTER_ALIGNMENT);
        panel.add(analysisLabel);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panel.setBackground(Theme.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        generateButton = createStyledButton("GENERATE KIT", true);
        generateButton.setFont(new Font("Consolas", Font.BOLD, 16));
        generateButton.setPreferredSize(new Dimension(220, 50));
        generateButton.addActionListener(e -> generateKit());
        generateButton.setEnabled(false);

        panel.add(generateButton);

        return panel;
    }

    private JButton createStyledButton(String text, boolean primary) {
        JButton button = new JButton(text);
        button.setFont(Theme.FONT_BUTTON);
        button.setForeground(Theme.TEXT_PRIMARY);
        button.setBackground(primary ? Theme.ACCENT : Theme.PANEL_BACKGROUND);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new LineBorder(primary ? Theme.ACCENT : Theme.BORDER, 1));
        return button;
    }

    private void loadAudioFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Audio Files", "wav", "ogg", "mp3", "aiff", "aif"
        ));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                mode.loadAudioFile(file);
                fileNameLabel.setText(file.getName());
                fileNameLabel.setForeground(Theme.ACCENT);
                generateButton.setEnabled(true);

                AudioAnalyzer.AnalysisResult analysis = mode.getAnalysis();
                analysisLabel.setText(String.format(
                        "Duration: %.2fs | Freq: %.1fHz | Peak: %.2f | Type: %s",
                        analysis.duration, analysis.fundamentalFreq, analysis.peak, analysis.soundType
                ));
                analysisLabel.setForeground(Theme.ACCENT);

                statusSink.accept("Loaded: " + file.getName());
            } catch (UnsupportedAudioFileException e) {
                analysisLabel.setText("✗ Unsupported audio format");
                analysisLabel.setForeground(new Color(255, 80, 80));
                statusSink.accept("Error: Unsupported format");
            } catch (IOException e) {
                analysisLabel.setText("✗ Error reading file");
                analysisLabel.setForeground(new Color(255, 80, 80));
                statusSink.accept("Error: " + e.getMessage());
            }
        }
    }

    private void generateKit() {
        if (!mode.hasAudio()) {
            statusSink.accept("Please load a sample first");
            return;
        }

        generateButton.setEnabled(false);
        loadButton.setEnabled(false);
        statusSink.accept("Generating kit from sample...");

        new Thread(() -> {
            try {
                Kit kit = mode.generate();
                SwingUtilities.invokeLater(() -> {
                    kitSink.accept(kit);
                    statusSink.accept("Kit generated: " + kit.getName() + " (5 rows, 12 sounds)");
                    generateButton.setEnabled(true);
                    loadButton.setEnabled(true);
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    statusSink.accept("Error: " + e.getMessage());
                    generateButton.setEnabled(true);
                    loadButton.setEnabled(true);
                });
                e.printStackTrace();
            }
        }).start();
    }
}