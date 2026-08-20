package solly.kitsmith.gui;

import javax.swing.JPopupMenu;
import javax.swing.BorderFactory;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class ThemedPopupMenu extends JPopupMenu {

    public ThemedPopupMenu() {
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        setBackground(Theme.PANEL_BACKGROUND);
        setLightWeightPopupEnabled(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(new Color(24, 17, 18));
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

        g2.setColor(Theme.BORDER);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
        g2.dispose();
    }
}