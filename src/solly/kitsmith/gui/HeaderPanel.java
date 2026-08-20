package solly.kitsmith.gui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;

public class HeaderPanel extends JPanel {

    private final JButton regenerateButton;
    private final JButton downloadButton;

    public HeaderPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 16, 12, 16));

        JLabel title = new JLabel("KitSmith", SwingConstants.CENTER);
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("EXPERIMENTAL SOUND SYNTHESIS & GENERATION", SwingConstants.CENTER);
        subtitle.setFont(Theme.FONT_SUBTITLE);
        subtitle.setForeground(Theme.TEXT_SECONDARY);

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new javax.swing.BoxLayout(titleBlock, javax.swing.BoxLayout.Y_AXIS));
        titleBlock.setBackground(Theme.BACKGROUND);
        title.setAlignmentX(CENTER_ALIGNMENT);
        subtitle.setAlignmentX(CENTER_ALIGNMENT);
        titleBlock.add(title);
        titleBlock.add(subtitle);
        titleBlock.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

        add(titleBlock, BorderLayout.NORTH);

        JPanel actions = new JPanel(new GridLayout(1, 2, 10, 0));
        actions.setBackground(Theme.BACKGROUND);

        regenerateButton = createActionButton("RE-GENERATE KIT", false);
        downloadButton = createActionButton("DOWNLOAD ZIP (doesn't work for now)", true);

        actions.add(regenerateButton);
        actions.add(downloadButton);
        add(actions, BorderLayout.CENTER);
    }

    private JButton createActionButton(String text, boolean highlighted) {
        JButton button = new JButton(text);
        button.setFont(Theme.FONT_BUTTON);
        button.setForeground(Theme.TEXT_PRIMARY);
        button.setBackground(Theme.PANEL_BACKGROUND);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(200, 46));
        Color borderColor = highlighted ? Theme.ACCENT : Theme.BORDER;
        button.setBorder(new LineBorder(borderColor, 1));
        return button;
    }

    public JButton getRegenerateButton() {
        return regenerateButton;
    }

    public JButton getDownloadButton() {
        return downloadButton;
    }
}
