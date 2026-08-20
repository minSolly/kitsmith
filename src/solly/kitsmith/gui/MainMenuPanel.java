package solly.kitsmith.gui;

import solly.kitsmith.Kit;
import solly.kitsmith.modes.ModeInfo;
import solly.kitsmith.modes.ModeManager;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class MainMenuPanel extends JPanel {

    private final ModeManager modeManager;
    private final ModeLaunchListener listener;
    private ModeInfo selectedMode;
    private final List<ModeButton> modeButtons = new ArrayList<>();
    private JLabel descriptionLabel;
    private JButton launchButton;
    private JPanel modeListPanel;
    private JLabel nameLabel;
    private JLabel categoryLabel;

    public interface ModeLaunchListener {
        void onModeLaunch(ModeInfo mode);
        void onStatusUpdate(String message);
        void onError(String error);
    }

    public MainMenuPanel(ModeManager modeManager, ModeLaunchListener listener) {
        this.modeManager = modeManager;
        this.listener = listener;
        setLayout(new BorderLayout());
        setBackground(Theme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        
        JPanel header = createHeader();
        add(header, BorderLayout.NORTH);

        
        JPanel content = new JPanel(new BorderLayout(20, 20));
        content.setBackground(Theme.BACKGROUND);

        
        modeListPanel = createModeListPanel();
        JScrollPane scrollPane = new JScrollPane(modeListPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Theme.BACKGROUND);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUI(new ThemedScrollBarUi());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setPreferredSize(new Dimension(350, 400));

        
        JPanel detailsPanel = createDetailsPanel();

        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        centerPanel.setBackground(Theme.BACKGROUND);
        centerPanel.add(scrollPane);
        centerPanel.add(detailsPanel);

        content.add(centerPanel, BorderLayout.CENTER);

        
        JPanel actionPanel = createActionPanel();
        content.add(actionPanel, BorderLayout.SOUTH);

        add(content, BorderLayout.CENTER);

        
        if (!modeButtons.isEmpty()) {
            selectMode(modeButtons.get(0));
        }
    }

    private JPanel createHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(Theme.BACKGROUND);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JLabel title = new JLabel("KitSmith", SwingConstants.CENTER);
        title.setFont(Theme.FONT_TITLE);
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("SELECT GENERATION MODE", SwingConstants.CENTER);
        subtitle.setFont(Theme.FONT_SUBTITLE);
        subtitle.setForeground(Theme.TEXT_SECONDARY);
        subtitle.setAlignmentX(CENTER_ALIGNMENT);

        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitle);

        return header;
    }

    private JPanel createModeListPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Theme.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        
        JLabel listTitle = new JLabel("AVAILABLE MODES");
        listTitle.setFont(Theme.FONT_LABEL);
        listTitle.setForeground(Theme.TEXT_SECONDARY);
        listTitle.setAlignmentX(LEFT_ALIGNMENT);
        listTitle.setBorder(BorderFactory.createEmptyBorder(0, 4, 8, 4));
        panel.add(listTitle);

        
        modeButtons.clear();
        for (ModeInfo mode : modeManager.getModes()) {
            ModeButton button = new ModeButton(mode);
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    selectMode(button);
                }
            });
            modeButtons.add(button);
            panel.add(button);
            panel.add(Box.createVerticalStrut(4));
        }

        panel.add(Box.createVerticalGlue());
        return panel;
    }

    private JPanel createDetailsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Theme.PANEL_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(Theme.BORDER, 1),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        JLabel titleLabel = new JLabel("Mode Details");
        titleLabel.setFont(Theme.FONT_LABEL);
        titleLabel.setForeground(Theme.TEXT_SECONDARY);
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(12));

        
        nameLabel = new JLabel("Select a mode");
        nameLabel.setFont(new Font("Consolas", Font.BOLD, 18));
        nameLabel.setForeground(Theme.TEXT_PRIMARY);
        nameLabel.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(nameLabel);
        panel.add(Box.createVerticalStrut(4));

        
        categoryLabel = new JLabel("");
        categoryLabel.setFont(Theme.FONT_SUBTITLE);
        categoryLabel.setForeground(Theme.ACCENT);
        categoryLabel.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(categoryLabel);
        panel.add(Box.createVerticalStrut(12));

        
        descriptionLabel = new JLabel("Choose a mode from the list to see details");
        descriptionLabel.setFont(Theme.FONT_STATUS);
        descriptionLabel.setForeground(Theme.TEXT_SECONDARY);
        descriptionLabel.setAlignmentX(LEFT_ALIGNMENT);
        descriptionLabel.setVerticalAlignment(SwingConstants.TOP);
        panel.add(descriptionLabel);

        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private JPanel createActionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panel.setBackground(Theme.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        launchButton = createStyledButton("LAUNCH MODE", true);
        launchButton.setFont(new Font("Consolas", Font.BOLD, 16));
        launchButton.setPreferredSize(new Dimension(200, 50));
        launchButton.addActionListener(e -> launchSelectedMode());

        panel.add(launchButton);

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
        button.setPreferredSize(new Dimension(160, 40));
        return button;
    }

    private void selectMode(ModeButton button) {
        for (ModeButton mb : modeButtons) {
            mb.setSelected(false);
        }
        button.setSelected(true);
        selectedMode = button.getModeInfo();
        updateDetails(selectedMode);
        launchButton.setEnabled(true);
    }

    private void updateDetails(ModeInfo mode) {
        nameLabel.setText(mode.getName());
        categoryLabel.setText("Category: " + mode.getCategory());

        String desc = mode.getMode().getDescription();
        String wrapped = wrapDescription(desc, 45);
        descriptionLabel.setText("<html>" + wrapped.replace("\n", "<br>") + "</html>");
    }

    private String wrapDescription(String text, int maxLineLength) {
        StringBuilder result = new StringBuilder();
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();

        for (String word : words) {
            if (word.contains("\n")) {
                String[] parts = word.split("\n");
                for (int i = 0; i < parts.length; i++) {
                    if (i > 0) {
                        result.append(line.toString().trim()).append("\n");
                        line = new StringBuilder();
                    }
                    if (line.length() + parts[i].length() + 1 > maxLineLength && line.length() > 0) {
                        result.append(line.toString().trim()).append("\n");
                        line = new StringBuilder();
                    }
                    if (line.length() > 0) line.append(" ");
                    line.append(parts[i]);
                }
            } else {
                if (line.length() + word.length() + 1 > maxLineLength && line.length() > 0) {
                    result.append(line.toString().trim()).append("\n");
                    line = new StringBuilder();
                }
                if (line.length() > 0) line.append(" ");
                line.append(word);
            }
        }
        if (line.length() > 0) {
            result.append(line.toString().trim());
        }
        return result.toString();
    }

    private void launchSelectedMode() {
        if (selectedMode == null) {
            listener.onError("Please select a mode first");
            return;
        }

        launchButton.setEnabled(false);
        listener.onStatusUpdate("Launching: " + selectedMode.getName());

        
        SwingUtilities.invokeLater(() -> {
            listener.onModeLaunch(selectedMode);
            launchButton.setEnabled(true);
        });
    }

    
    private class ModeButton extends JPanel {
        private final ModeInfo modeInfo;
        private boolean selected = false;

        public ModeButton(ModeInfo modeInfo) {
            this.modeInfo = modeInfo;
            setLayout(new BorderLayout());
            setBackground(Theme.PANEL_BACKGROUND);
            setBorder(new LineBorder(Theme.BORDER, 1));
            setPreferredSize(new Dimension(0, 40));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            JLabel nameLabel = new JLabel(modeInfo.getName());
            nameLabel.setFont(Theme.FONT_LABEL);
            nameLabel.setForeground(Theme.TEXT_PRIMARY);
            nameLabel.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));
            add(nameLabel, BorderLayout.WEST);

            JLabel categoryLabel = new JLabel(modeInfo.getCategory());
            categoryLabel.setFont(Theme.FONT_SUBTITLE);
            categoryLabel.setForeground(Theme.TEXT_SECONDARY);
            categoryLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 12));
            add(categoryLabel, BorderLayout.EAST);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    selectMode(ModeButton.this);
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!selected) {
                        setBorder(new LineBorder(Theme.BORDER_HOVER, 1));
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (!selected) {
                        setBorder(new LineBorder(Theme.BORDER, 1));
                    }
                }
            });
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
            if (selected) {
                setBackground(Theme.ACCENT_DIM);
                setBorder(new LineBorder(Theme.ACCENT, 2));
            } else {
                setBackground(Theme.PANEL_BACKGROUND);
                setBorder(new LineBorder(Theme.BORDER, 1));
            }
            repaint();
        }

        public ModeInfo getModeInfo() {
            return modeInfo;
        }
    }
}