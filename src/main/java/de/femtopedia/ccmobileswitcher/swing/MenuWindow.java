package de.femtopedia.ccmobileswitcher.swing;

import de.femtopedia.ccmobileswitcher.util.Utils;
import java.awt.Container;
import java.awt.FlowLayout;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import se.vidstige.jadb.JadbException;

public class MenuWindow extends JFrame {

    public MenuWindow() {
        JButton backup = new JButton("Backup");
        JButton restore = new JButton("Restore");
        JButton convert = new JButton("Convert save");

        backup.addActionListener(e -> {
            dispose();
            try {
                new PatchChooserWindow(pkg -> {
                    JFileChooser chooser = new JFileChooser();
                    chooser.setDialogTitle("Choose destination directory...");
                    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    int retrieval = chooser.showSaveDialog(null);
                    if (retrieval == JFileChooser.APPROVE_OPTION) {
                        try {
                            File filePath = chooser.getSelectedFile().getAbsoluteFile();
                            File abFile = new File(filePath, "backup.ab");
                            File tarFile = new File(filePath, "backup.tar");
                            Utils.startProcess("adb backup -f " + abFile + " " + pkg);
                            String[] switcher = new String[]{"unpack", abFile.toString(),
                                    tarFile.toString()};
                            org.nick.abe.Main.main(switcher);
                            Utils.extractTar(Files.newInputStream(tarFile.toPath()), filePath);

                            File packages = new File(filePath, "package.list");
                            Utils.printTarEntries(tarFile.toString(), packages);

                            abFile.delete();
                            tarFile.delete();
                            return true;
                        } catch (IOException | InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                    return false;
                });
            } catch (IOException | JadbException | InterruptedException ex) {
                ex.printStackTrace();
            }
        });
        restore.addActionListener(e -> {
            dispose();
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Choose source file...");
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setFileFilter(new FileNameExtensionFilter(
                    "Package List (*.list)", "list"));
            int retrieval = chooser.showOpenDialog(null);
            if (retrieval == JFileChooser.APPROVE_OPTION) {
                try {
                    File packages = chooser.getSelectedFile().getAbsoluteFile();
                    File filePath = packages.getParentFile();
                    File abFile = new File(filePath, "backup.ab");
                    File tarFile = new File(filePath, "backup.tar");
                    Utils.packTar(tarFile, packages, filePath);
                    String[] switcher = new String[]{"pack", tarFile.toString(),
                            abFile.toString()};
                    org.nick.abe.Main.main(switcher);
                    Utils.startProcess("adb restore " + abFile);
                    tarFile.delete();
                    abFile.delete();
                } catch (IOException | InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        });
        convert.addActionListener(e -> {
            dispose();
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Choose source file...");
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setFileFilter(new FileNameExtensionFilter(
                    "Package List (*.list)", "list"));
            int retrieval = chooser.showOpenDialog(null);
            if (retrieval != JFileChooser.APPROVE_OPTION) {
                return;
            }
            File packages = chooser.getSelectedFile().getAbsoluteFile();
            File filePath = packages.getParentFile();
            JFileChooser chooser2 = new JFileChooser();
            chooser2.setDialogTitle("Choose destination directory...");
            chooser2.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            retrieval = chooser2.showOpenDialog(null);
            if (retrieval != JFileChooser.APPROVE_OPTION) {
                return;
            }
            File destination = chooser2.getSelectedFile().getAbsoluteFile();
            try {
                FileUtils.copyDirectory(filePath, destination);
                File destPackage = new File(destination, "package.list");
                new PatchChooserWindow(pkg -> {
                    try {
                        String content = IOUtils.toString(Files.newInputStream(destPackage.toPath()),
                                StandardCharsets.UTF_8);
                        content = content.replaceAll(
                                "org\\.dashnet\\.cookieclicker.*?/",
                                pkg + "/");
                        IOUtils.write(content, Files.newOutputStream(destPackage.toPath()),
                                StandardCharsets.UTF_8);

                        File[] files = new File(destination, "apps").listFiles();
                        File appFolder = Objects.requireNonNull(files)[0];
                        File newFolder = new File(appFolder.getParent(), pkg);
                        appFolder.renameTo(newFolder);

                        File tempPath = new File(newFolder, "temp");
                        tempPath.mkdirs();
                        File abFile = new File(tempPath, "backup.ab");
                        File tarFile = new File(tempPath, "backup.tar");
                        Utils.startProcess("adb backup -f " + abFile + " " + pkg);
                        String[] switcher = new String[]{"unpack", abFile.toString(),
                                tarFile.toString()};
                        org.nick.abe.Main.main(switcher);
                        Utils.extractTarFile(Files.newInputStream(tarFile.toPath()),
                                "apps/" + pkg + "/_manifest", destination);
                        FileUtils.deleteDirectory(tempPath);
                        System.exit(0);
                        return true;
                    } catch (IOException | InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    return false;
                });
            } catch (IOException | JadbException | InterruptedException ex) {
                ex.printStackTrace();
            }
        });

        Container c = getContentPane();
        c.setLayout(new FlowLayout());
        c.add(backup);
        c.add(restore);
        c.add(convert);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(200, 200);
        setTitle("CCMobileSwitcher");
        setVisible(true);
    }

}
