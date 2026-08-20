package solly.kitsmith.gui;

import solly.kitsmith.Kit;
import solly.kitsmith.KitGenerator;
import solly.kitsmith.dsp.AudioConstants;
import solly.kitsmith.export.ZipExporter;
import solly.kitsmith.modes.ModeInfo;
import solly.kitsmith.modes.ModeManager;
import solly.kitsmith.modes.UserSampleMode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.File;

public class KitLabFrame extends JFrame {

    private final KitGenerator generator = new KitGenerator();
    private final ModeManager modeManager = new ModeManager();
    private final HeaderPanel headerPanel = new HeaderPanel();
    private final KitPanel kitPanel;
    private final JLabel statusLabel = new JLabel("Ready");
    private Kit currentKit;
    private CardLayout cardLayout;
    private JPanel mainContainer;
    private MainMenuPanel mainMenuPanel;

    public KitLabFrame() {
        setTitle("KitSmith");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setUndecorated(true);
        setMinimumSize(new Dimension(800, 600));
        getContentPane().setBackground(Theme.BACKGROUND);
        setLayout(new BorderLayout());
        registerEscapeToClose();

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);
        mainContainer.setBackground(Theme.BACKGROUND);

        mainMenuPanel = new MainMenuPanel(modeManager, new MainMenuPanel.ModeLaunchListener() {
            @Override
            public void onModeLaunch(ModeInfo mode) {
                launchMode(mode);
            }

            @Override
            public void onStatusUpdate(String message) {
                setStatus(message);
            }

            @Override
            public void onError(String error) {
                setStatus("ERROR: " + error);
                JOptionPane.showMessageDialog(KitLabFrame.this,
                        error, "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        kitPanel = new KitPanel(generator, this::setStatus);
        JScrollPane scrollPane = new JScrollPane(kitPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Theme.BACKGROUND);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUI(new ThemedScrollBarUi());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JPanel kitContainer = new JPanel(new BorderLayout());
        kitContainer.setBackground(Theme.BACKGROUND);
        kitContainer.add(scrollPane, BorderLayout.CENTER);

        mainContainer.add(mainMenuPanel, "menu");
        mainContainer.add(kitContainer, "kit");

        add(mainContainer, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);
        add(buildStatusBar(), BorderLayout.SOUTH);

        headerPanel.getRegenerateButton().addActionListener(e -> regenerateKit());
        headerPanel.getDownloadButton().addActionListener(e -> downloadZip());
        headerPanel.getBackButton().addActionListener(e -> goBackToMenu());

        cardLayout.show(mainContainer, "menu");
        headerPanel.showKitMode(false);
        headerPanel.showButtons(false);

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

    private void launchMode(ModeInfo modeInfo) {
        if (modeInfo.getId().equals("user_sample")) {
            
            UserSampleMode userMode = (UserSampleMode) modeInfo.getMode();
            UserSamplePanel userPanel = new UserSamplePanel(
                    userMode,
                    this::setStatus,
                    kit -> {
                        currentKit = kit;
                        kitPanel.showKit(kit);
                        cardLayout.show(mainContainer, "kit");
                        setStatus("Generated: " + currentKit.getAllSlots().size() + " sounds");
                        headerPanel.showKitMode(true);
                        headerPanel.showButtons(true);
                    }
            );
            mainContainer.add(userPanel, "user_sample");
            cardLayout.show(mainContainer, "user_sample");
            headerPanel.showKitMode(true);
            headerPanel.showButtons(false); 
        } else {
            
            setStatus("Generating kit with mode: " + modeInfo.getName());
            headerPanel.showKitMode(false);
            headerPanel.showButtons(false);

            new Thread(() -> {
                try {
                    Kit kit = modeInfo.getMode().generate();
                    SwingUtilities.invokeLater(() -> {
                        currentKit = kit;
                        kitPanel.showKit(kit);
                        cardLayout.show(mainContainer, "kit");
                        setStatus("Generated: " + currentKit.getAllSlots().size() + " sounds");
                        headerPanel.showKitMode(true);
                        headerPanel.showButtons(true);
                    });
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> {
                        setStatus("Error: " + e.getMessage());
                    });
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private void goBackToMenu() {
        
        for (Component comp : mainContainer.getComponents()) {
            if (comp instanceof UserSamplePanel) {
                mainContainer.remove(comp);
                break;
            }
        }
        cardLayout.show(mainContainer, "menu");
        headerPanel.showKitMode(false);
        headerPanel.showButtons(false);
        setStatus("Ready");
    }

    private void regenerateKit() {
        if (currentKit == null) {
            setStatus("No kit to regenerate");
            return;
        }

        setStatus("Regenerating kit...");
        headerPanel.getRegenerateButton().setEnabled(false);

        new Thread(() -> {
            Kit kit = generator.generateFullKit();
            SwingUtilities.invokeLater(() -> {
                currentKit = kit;
                kitPanel.showKit(kit);
                setStatus("Regenerated: " + currentKit.getAllSlots().size() + " sounds");
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