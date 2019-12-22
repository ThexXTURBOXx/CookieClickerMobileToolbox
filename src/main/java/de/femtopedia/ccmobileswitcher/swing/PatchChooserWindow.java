package de.femtopedia.ccmobileswitcher.swing;

import de.femtopedia.ccmobileswitcher.util.Utils;
import java.awt.Container;
import java.awt.FlowLayout;
import java.io.IOException;
import java.util.List;
import java.util.Vector;
import java.util.function.Function;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import se.vidstige.jadb.JadbConnection;
import se.vidstige.jadb.JadbDevice;
import se.vidstige.jadb.JadbException;
import se.vidstige.jadb.managers.Package;
import se.vidstige.jadb.managers.PackageManager;

public class PatchChooserWindow extends JFrame {

    private volatile String currentSelected = null;

    public PatchChooserWindow(Function<String, Boolean> onChoose) throws IOException,
            JadbException, InterruptedException {
        Utils.startProcess("adb start-server");
        JadbConnection connection = new JadbConnection();
        List<JadbDevice> devices = connection.getDevices();
        if (devices.size() < 1) {
            return;
        }
        JadbDevice device = devices.get(0);
        List<Package> packages = new PackageManager(device).getPackages();
        Vector<String> packagesCC = new Vector<>();
        for (Package p : packages) {
            if (p.toString().startsWith("org.dashnet.cookieclicker")) {
                packagesCC.add(p.toString());
            }
        }

        JList<String> patches = new JList<>(packagesCC);
        patches.addListSelectionListener(e -> {
            currentSelected = ((JList<String>) e.getSource()).getSelectedValue();
        });

        JButton apply = new JButton("Choose");
        apply.addActionListener(e -> {
            if (currentSelected != null) {
                if (onChoose.apply(currentSelected)) {
                    dispose();
                }
            }
        });

        Container c = getContentPane();
        c.setLayout(new FlowLayout());
        c.add(patches);
        c.add(apply);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(200, 200);
        setTitle("CCMobileSwitcher - Choose Patch...");
        setVisible(true);
    }

}
