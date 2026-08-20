package solly.kitsmith.gui;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

public class HeaderPanel extends JPanel {

    private final JButton regenerateButton;
    private final JButton downloadButton;
    private final JButton backButton;

    public HeaderPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(12, 16, 8, 16));

        
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftPanel.setBackground(Theme.BACKGROUND);
        backButton = createBackButton();
        leftPanel.add(backButton);
        add(leftPanel, BorderLayout.WEST);

        
        JPanel centerPanel = new JPanel();
        centerPanel.setBackground(Theme.BACKGROUND);
        add(centerPanel, BorderLayout.CENTER);

        
        JPanel rightPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        rightPanel.setBackground(Theme.BACKGROUND);

        regenerateButton = createActionButton("RE-GENERATE KIT", false);
        downloadButton = createActionButton("DOWNLOAD ZIP", true);

        rightPanel.add(regenerateButton);
        rightPanel.add(downloadButton);
        add(rightPanel, BorderLayout.EAST);

        
        showKitMode(false);
        showButtons(false);
    }

    private JButton createBackButton() {
        JButton button = new JButton("← BACK");
        button.setFont(Theme.FONT_BUTTON);
        button.setForeground(Theme.TEXT_PRIMARY);
        button.setBackground(Theme.PANEL_BACKGROUND);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(new LineBorder(Theme.BORDER, 1));
        button.setPreferredSize(new Dimension(100, 36));
        return button;
    }

    private JButton createActionButton(String text, boolean highlighted) {
        JButton button = new JButton(text);
        button.setFont(Theme.FONT_BUTTON);
        button.setForeground(Theme.TEXT_PRIMARY);
        button.setBackground(Theme.PANEL_BACKGROUND);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(180, 40));
        Color borderColor = highlighted ? Theme.ACCENT : Theme.BORDER;
        button.setBorder(new LineBorder(borderColor, 1));
        return button;
    }

    public void showKitMode(boolean isKitMode) {
        backButton.setVisible(isKitMode);
    }

    public void showButtons(boolean show) {
        regenerateButton.setVisible(show);
        downloadButton.setVisible(show);
    }

    public JButton getRegenerateButton() {
        return regenerateButton;
    }

    public JButton getDownloadButton() {
        return downloadButton;
    }

    public JButton getBackButton() {
        return backButton;
    }
}