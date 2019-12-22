package de.femtopedia.ccmobileswitcher;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.function.Consumer;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

public class Main {

    public static void main(String[] args) throws Exception {
        //Get normal save
        startProcess("adb backup -f oldsave.ab org.dashnet.cookieclicker");
        //TODO Password?
        String[] switcher = new String[]{"unpack", "oldsave.ab", "oldsave.tar"};
        org.nick.abe.Main.main(switcher);
        extractTar(new FileInputStream("oldsave.tar"));

        //Generate package list
        File oldPackage = new File("oldpackage.list");
        printTarEntries("oldsave.tar", oldPackage);
        File newPackage = new File("newpackage.list");
        BufferedReader reader = new BufferedReader(new FileReader(oldPackage));
        PrintWriter writer = new PrintWriter(newPackage);
        String line;
        while ((line = reader.readLine()) != null) {
            writer.println(line.replace("org.dashnet.cookieclicker",
                    "org.dashnet.cookieclickerPSPatch"));
        }
        writer.flush();
        reader.close();
        writer.close();

        //Rename folder
        File folder = new File("./apps/org.dashnet.cookieclicker");
        folder.renameTo(new File("./apps/org.dashnet.cookieclickerPSPatch"));

        //Get Patch save
        startProcess("adb backup -f oldpatchedsave.ab "
                + "org.dashnet.cookieclickerPSPatch");
        //TODO Password?
        String[] switcher3 = new String[]{"unpack", "oldpatchedsave.ab",
                "oldpatchedsave.tar"};
        org.nick.abe.Main.main(switcher3);
        extractTarFile(new FileInputStream("oldpatchedsave.tar"),
                "apps/org.dashnet.cookieclickerPSPatch/_manifest");

        //Pack new save
        packTar("newsave.tar", "newpackage.list");
        //TODO Password?
        String[] switcher2 = new String[]{"pack", "newsave.tar", "newsave.ab"};
        org.nick.abe.Main.main(switcher2);
        startProcess("adb restore newsave.ab");
    }

    private static void startProcess(String cmd) throws IOException,
            InterruptedException {
        startProcess(cmd, System.out::print);
    }

    private static void startProcess(String cmd, Consumer<Character> consumer) throws IOException,
            InterruptedException {
        Process process = Runtime.getRuntime().exec(cmd);
        InputStream s = process.getInputStream();
        int c;
        while ((c = s.read()) != -1) {
            consumer.accept((char) c);
        }
        process.waitFor();
    }

    private static void printTarEntries(String input, File output) throws IOException {
        PrintWriter writer = new PrintWriter(output);
        TarArchiveInputStream tarStream = new TarArchiveInputStream(
                new FileInputStream(input));
        ArchiveEntry entry;
        while ((entry = tarStream.getNextEntry()) != null) {
            writer.println(entry.getName());
        }
        tarStream.close();
        writer.flush();
        writer.close();
    }

    private static void extractTar(InputStream stream) throws IOException {
        TarArchiveInputStream in = new TarArchiveInputStream(stream);
        TarArchiveEntry entry;
        while ((entry = in.getNextTarEntry()) != null) {
            if (!entry.isDirectory()) {
                extractTarFile(in, entry);
            }
        }
        in.close();
    }

    private static void extractTarFile(InputStream stream, String exFile) throws IOException {
        TarArchiveInputStream in = new TarArchiveInputStream(stream);
        TarArchiveEntry entry;
        while ((entry = in.getNextTarEntry()) != null) {
            if (entry.isDirectory()) {
                entry.getFile().mkdirs();
            } else {
                if (entry.getName().equals(exFile)) {
                    extractTarFile(in, entry);
                    break;
                }
            }
        }
        in.close();
    }

    private static void extractTarFile(TarArchiveInputStream in,
                                       TarArchiveEntry entry) throws IOException {
        byte[] buffer = new byte[1024];
        File outFile = new File(entry.getName());
        outFile.getParentFile().mkdirs();
        outFile.createNewFile();
        BufferedOutputStream out =
                new BufferedOutputStream(new FileOutputStream(outFile));
        int len;
        while ((len = in.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }
        out.close();
        out.flush();
        out.close();
    }

    private static void packTar(String out, String list) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(list));
        TarArchiveOutputStream writer =
                new TarArchiveOutputStream(new FileOutputStream(out));
        String line;
        while ((line = reader.readLine()) != null) {
            File in = new File(line);
            writer.createArchiveEntry(in, in.getPath());
        }
        reader.close();
        writer.flush();
        writer.close();
    }

}
