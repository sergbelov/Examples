package ru.utils.files;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ArhiveService {

    private static final Logger LOG = LogManager.getLogger();
    private final int BUFFER_SIZE = 1024;

    public void extractFilesFromArhive(String sourceFile, String targetPathStr){
        try (ZipInputStream zin = new ZipInputStream(new FileInputStream(sourceFile))) {
            ZipEntry entry;
            String name;
            long size;
            byte[] tmp = new byte[BUFFER_SIZE];
            File targetPath;
            while ((entry = zin.getNextEntry()) != null) {
                name = entry.getName(); // получим название файла
                size = entry.getSize();  // получим его размер в байтах

                if (name.contains(File.separator)) {
                    targetPath = new File(targetPathStr, name.substring(0, name.lastIndexOf(File.separator)));
                } else {
                    targetPath = new File(targetPathStr);
                }
                if (!targetPath.exists()) {
                    targetPath.mkdirs();
                }

                // распаковка
                LOG.info("{} -> {}", name, targetPath.toString());
                FileOutputStream fout = new FileOutputStream(targetPathStr + File.separator + name);

                int c = -1;
                while ( (c = zin.read(tmp, 0, BUFFER_SIZE)) != -1){
                    fout.write(tmp, 0, c);
                }

                fout.flush();
                fout.close();
                zin.closeEntry();
            }
        } catch (Exception e) {
            LOG.error(e);
        }
    }



    public void addFilesToArhive(String sourceName, String arhiveFileName){
        File arhiveFile = new File(arhiveFileName);
        File source = new File(sourceName);
        if (source.exists()) {
            try (FileOutputStream fileOutPutStream = new FileOutputStream(arhiveFile)){
                fileOutPutStream.write(compress(source));
                fileOutPutStream.flush();
                fileOutPutStream.close();
            } catch (IOException e) {
                LOG.error(e);
            }
        }
    }

    private byte[] compress(File src) throws IOException {
        return compress(src, null);
    }

    private byte[] compress(File src, String comment) throws IOException {
        List<File> files = getFiles(src);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            try (ZipOutputStream zos = new ZipOutputStream(baos)) {
                for (File file : files) {
                    addToZip(src.isDirectory() ? src : src.getParentFile(), file, zos);
                }
                zos.setComment(comment);
            }
            return baos.toByteArray();
        }
    }

    private List<File> getFiles(File src) {
        List<File> files = new ArrayList<>();
        if (src.isFile()) {
            files.add(src);
        } else {
            for (File file : Optional.ofNullable(src.listFiles()).orElse(new File[0])) {
                files.addAll(getFiles(file));
            }
        }
        return files;
    }

    private void addToZip(File parent, File file, ZipOutputStream zos) throws IOException {
        String zipFilePath;
        if (parent == null){
            zipFilePath = file.getName();
        } else {
            zipFilePath = file.getCanonicalPath().substring(
                    parent.getCanonicalPath().length() + 1,
                    file.getCanonicalPath().length());
        }
        LOG.info("{}", zipFilePath);

        ZipEntry zipEntry = new ZipEntry(zipFilePath);
        zos.putNextEntry(zipEntry);

        try (InputStream is = new FileInputStream(file)) {
            int length;
            byte[] bytes = new byte[BUFFER_SIZE];
            while ((length = is.read(bytes)) >= 0) {
                zos.write(bytes, 0, length);
            }
        }
    }

}
