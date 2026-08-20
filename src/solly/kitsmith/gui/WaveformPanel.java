package solly.kitsmith.gui;

import solly.kitsmith.audio.AudioEngine;
import solly.kitsmith.dsp.AudioConstants;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class WaveformPanel extends JPanel {

    private static final int BAR_COUNT = 90;

    private float[] audio;
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
                AudioEngine.getInstance().play(audio, AudioConstants.SAMPLE_RATE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                repaint();
                AudioEngine.getInstance().stop();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                AudioEngine.getInstance().play(audio, AudioConstants.SAMPLE_RATE);
            }
        });
    }

    public void setAudio(float[] audio) {
        this.audio = audio;
        repaint();
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

        if (audio == null || audio.length == 0) return;

        float[] peaks = downsample(audio, BAR_COUNT);
        float barWidth = (float) width / BAR_COUNT;

        for (int i = 0; i < BAR_COUNT; i++) {
            float amp = peaks[i];
            int barHeight = (int) (amp * (height * 0.46f));
            int x = (int) (i * barWidth);
            int w = Math.max(1, (int) (barWidth * 0.55f));

            if (barHeight < 2) {
                g2.setColor(Theme.WAVEFORM_TAIL);
                g2.fillOval(x + w / 2, midY - 1, 2, 2);
            } else {
                g2.setColor(hovered ? Theme.ACCENT : Theme.WAVEFORM_FILL);
                g2.fillRect(x, midY - barHeight, w, barHeight * 2);
            }
        }
    }

    private float[] downsample(float[] source, int bars) {
        float[] peaks = new float[bars];
        int segment = Math.max(1, source.length / bars);
        float maxPeak = 0f;

        for (int b = 0; b < bars; b++) {
            int start = b * segment;
            int end = Math.min(source.length, start + segment);
            float peak = 0f;
            for (int i = start; i < end; i++) {
                float a = Math.abs(source[i]);
                if (a > peak) peak = a;
            }
            peaks[b] = peak;
            if (peak > maxPeak) maxPeak = peak;
        }

        if (maxPeak > 0.0001f) {
            for (int b = 0; b < bars; b++) {
                peaks[b] = peaks[b] / maxPeak;
            }
        }
        return peaks;
    }
}
