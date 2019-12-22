package de.femtopedia.ccmobileswitcher;

import de.femtopedia.ccmobileswitcher.swing.MenuWindow;
import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MenuWindow::new);
    }

}
