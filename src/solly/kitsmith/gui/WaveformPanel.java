package solly.kitsmith.gui;

import solly.kitsmith.KitSlot;
import solly.kitsmith.audio.AudioCache;
import solly.kitsmith.audio.AudioEngine;
import solly.kitsmith.dsp.AudioConstants;

import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class WaveformPanel extends JPanel {

    private static final int BAR_COUNT = 50;
    private KitSlot slot;
    private float[] cachedPeaks;
    private boolean hovered;

    public WaveformPanel() {
        setOpaque(true);
        setBackground(Theme.PANEL_BACKGROUND);
        setBorder(javax.swing.BorderFactory.createLineBorder(Theme.BORDER, 1));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                repaint();
                if (slot != null) {
                    AudioEngine.getInstance().play(slot.getAudio(), AudioConstants.SAMPLE_RATE);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                repaint();
                AudioEngine.getInstance().stop();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (slot != null) {
                    AudioEngine.getInstance().play(slot.getAudio(), AudioConstants.SAMPLE_RATE);
                }
            }
        });
    }

    public void setSlot(KitSlot slot) {
        this.slot = slot;
        this.cachedPeaks = null;
        repaint();
    }

    public void setAudio(float[] audio) {
        if (slot != null) {
            AudioCache.getInstance().invalidate(slot);
        }
        repaint();
    }

    private float[] getPeaks() {
        if (cachedPeaks != null) return cachedPeaks;
        if (slot == null) {
            cachedPeaks = new float[BAR_COUNT];
            return cachedPeaks;
        }
        cachedPeaks = AudioCache.getInstance().getPeaks(slot, BAR_COUNT);
        return cachedPeaks;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        int midY = height / 2;

        g2.setColor(hovered ? Theme.BORDER_HOVER : Theme.BORDER);
        g2.drawLine(0, midY, width, midY);

        float[] peaks = getPeaks();
        float barWidth = (float) width / BAR_COUNT;

        for (int i = 0; i < BAR_COUNT && i < peaks.length; i++) {
            float amp = peaks[i];
            int barHeight = (int) (amp * (height * 0.44f));
            int x = (int) (i * barWidth);
            int w = Math.max(1, (int) (barWidth * 0.45f));

            if (barHeight < 1) {
                g2.setColor(Theme.WAVEFORM_TAIL);
                g2.fillRect(x + w / 2, midY - 1, 1, 2);
            } else {
                g2.setColor(hovered ? Theme.ACCENT : Theme.WAVEFORM_FILL);
                g2.fillRect(x, midY - barHeight, w, barHeight * 2);
            }
        }
    }
}