package solly.kitsmith.gui;

import solly.kitsmith.Kit;
import solly.kitsmith.KitGenerator;
import solly.kitsmith.dsp.AudioConstants;
import solly.kitsmith.export.ZipExporter;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.File;

public class KitLabFrame extends JFrame {

    private final KitGenerator generator = new KitGenerator();
    private final HeaderPanel headerPanel = new HeaderPanel();
    private final KitPanel kitPanel;
    private final JLabel statusLabel = new JLabel("Ready");
    private Kit currentKit;

    public KitLabFrame() {
        setTitle("KitSmith");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setUndecorated(true);
        setMinimumSize(new Dimension(700, 500));
        getContentPane().setBackground(Theme.BACKGROUND);
        setLayout(new BorderLayout());
        registerEscapeToClose();

        kitPanel = new KitPanel(generator, this::setStatus);

        JScrollPane scrollPane = new JScrollPane(kitPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Theme.BACKGROUND);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUI(new ThemedScrollBarUi());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);

        headerPanel.getRegenerateButton().addActionListener(e -> generateKit());
        headerPanel.getDownloadButton().addActionListener(e -> downloadZip());

        generateKit();
        enterFullScreen();
    }

    private void registerEscapeToClose() {
        getRootPane().registerKeyboardAction(
                e -> dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING)),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void enterFullScreen() {
        
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setVisible(true);
    }

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Theme.PANEL_HEADER);
        bar.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        statusLabel.setFont(Theme.FONT_STATUS);
        statusLabel.setForeground(Theme.TEXT_SECONDARY);
        bar.add(statusLabel, BorderLayout.WEST);
        return bar;
    }

    private void setStatus(String text) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(text));
    }

    private void generateKit() {
        setStatus("Generating kit...");
        headerPanel.getRegenerateButton().setEnabled(false);

        new Thread(() -> {
            Kit kit = generator.generateFullKit();
            SwingUtilities.invokeLater(() -> {
                currentKit = kit;
                kitPanel.showKit(kit);
                setStatus("Generated: " + currentKit.getAllSlots().size() + " sounds");
                headerPanel.getRegenerateButton().setEnabled(true);
            });
        }).start();
    }

    private void downloadZip() {
        if (currentKit == null || currentKit.isEmpty()) {
            setStatus("No kit to export");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        String safeName = currentKit.getName().replace(":", "-").replace(" ", "_");
        chooser.setSelectedFile(new File(safeName + ".zip"));
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();

            String path = file.getAbsolutePath();
            if (!path.toLowerCase().endsWith(".zip")) {
                file = new File(path + ".zip");
            }

            File finalFile = file;
            setStatus("Exporting zip...");
            headerPanel.getDownloadButton().setEnabled(false);

            new Thread(() -> {
                try {
                    ZipExporter.exportKit(currentKit, finalFile, AudioConstants.SAMPLE_RATE);
                    SwingUtilities.invokeLater(() -> {
                        setStatus("Exported: " + finalFile.getName() + " (" +
                                (finalFile.length() / 1024) + " KB)");
                        headerPanel.getDownloadButton().setEnabled(true);
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        setStatus("Export error: " + ex.getMessage());
                        headerPanel.getDownloadButton().setEnabled(true);
                    });
                    ex.printStackTrace();
                }
            }).start();
        }
    }
}