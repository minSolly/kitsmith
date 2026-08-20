package solly.kitsmith;

import solly.kitsmith.gui.KitLabFrame;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        try {
            javax.swing.UIManager.setLookAndFeel(
                    javax.swing.UIManager.getCrossPlatformLookAndFeelClassName()
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(KitLabFrame::new);
    }
}