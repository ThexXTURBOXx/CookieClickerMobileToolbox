package de.femtopedia.ccmobileswitcher.util;

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

public class Utils {

    public static void startProcess(String cmd) throws IOException,
            InterruptedException {
        startProcess(cmd, System.out::print);
    }

    public static void startProcess(String cmd, Consumer<Character> consumer) throws IOException,
            InterruptedException {
        Process process = Runtime.getRuntime().exec(cmd);
        InputStream s = process.getInputStream();
        int c;
        while ((c = s.read()) != -1) {
            consumer.accept((char) c);
        }
        process.waitFor();
    }

    public static void printTarEntries(String input, File output) throws IOException {
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

    public static void extractTar(InputStream stream, File outputDir) throws IOException {
        TarArchiveInputStream in = new TarArchiveInputStream(stream);
        TarArchiveEntry entry;
        while ((entry = in.getNextTarEntry()) != null) {
            if (!entry.isDirectory()) {
                extractTarFile(in, entry, outputDir);
            }
        }
        in.close();
    }

    public static void extractTarFile(InputStream stream, String exFile,
                                      File outputDir) throws IOException {
        TarArchiveInputStream in = new TarArchiveInputStream(stream);
        TarArchiveEntry entry;
        while ((entry = in.getNextTarEntry()) != null) {
            if (entry.getName().equals(exFile)) {
                extractTarFile(in, entry, outputDir);
                break;
            }
        }
        in.close();
    }

    public static void extractTarFile(TarArchiveInputStream in,
                                      TarArchiveEntry entry, File outputDir) throws IOException {
        byte[] buffer = new byte[1024];
        File outFile = new File(outputDir, entry.getName());
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

    public static void packTar(String out, String list) throws IOException {
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
