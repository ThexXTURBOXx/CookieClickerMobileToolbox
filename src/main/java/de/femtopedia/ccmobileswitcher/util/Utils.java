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
import org.apache.commons.compress.utils.IOUtils;

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
            if (entry.isDirectory()) {
                new File(outputDir, entry.getName()).mkdirs();
            } else {
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

    public static void packTar(File out, File list, File dir) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(list));
        TarArchiveOutputStream writer =
                new TarArchiveOutputStream(new FileOutputStream(out));
        writer.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
        writer.setAddPaxHeadersForNonAsciiNames(true);
        String line;
        while ((line = reader.readLine()) != null) {
            File in = new File(dir, line);
            File entryDir = new File(line).getParentFile();
            addToArchiveCompression(writer, in, entryDir.toString());
        }
        reader.close();
        writer.flush();
        writer.close();
    }

    private static void addToArchiveCompression(TarArchiveOutputStream out, File file, String dir) throws IOException {
        String entry = dir + File.separator + file.getName();
        if (file.isFile()) {
            out.putArchiveEntry(new TarArchiveEntry(file, entry));
            try (FileInputStream in = new FileInputStream(file)) {
                IOUtils.copy(in, out);
            }
            out.closeArchiveEntry();
        } else if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    addToArchiveCompression(out, child, entry);
                }
            }
        } else {
            System.out.println(file.getName() + " is not supported");
        }
    }

}
