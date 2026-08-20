package solly.kitsmith;

import solly.kitsmith.gui.KitLabFrame;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(KitLabFrame::new);
    }
}
