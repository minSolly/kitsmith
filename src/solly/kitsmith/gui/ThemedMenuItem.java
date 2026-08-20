package solly.kitsmith.gui;

import javax.swing.JMenuItem;
import javax.swing.BorderFactory;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ThemedMenuItem extends JMenuItem {

    private boolean hovered = false;

    public ThemedMenuItem(String text) {
        super(text);
        setForeground(Theme.TEXT_PRIMARY);
        setBackground(Theme.PANEL_BACKGROUND);
        setFont(Theme.FONT_LABEL);
        setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        setOpaque(false);
        setFocusPainted(false);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (hovered) {
            g2.setColor(new Color(224, 68, 55, 60));
            g2.fillRoundRect(2, 1, getWidth() - 4, getHeight() - 2, 6, 6);
        }

        g2.setColor(hovered ? Theme.TEXT_PRIMARY : Theme.TEXT_SECONDARY);
        g2.setFont(getFont());
        g2.drawString(getText(), 12, getHeight() / 2 + g2.getFontMetrics().getAscent() / 2 - 2);

        g2.dispose();
        super.paintComponent(g);
    }
}