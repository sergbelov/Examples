package ru.utils.files;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by Сергей on 08.03.2019.
 */
public class FileUtils {

    private static final Logger LOG = LogManager.getLogger();
    private DateFormat dateFormatTimeYMDHMS = new SimpleDateFormat("yyyyMMddHHmmss");
    private String defaultEncoding = "UTF-8";


    public boolean writeFile(String fileName, String data) {
        return writeFile(fileName, data, defaultEncoding, false);
    }

    public boolean writeFile(String fileName, String data, boolean append) {
        return writeFile(fileName, data, defaultEncoding, append);
    }

    public boolean writeFile(String fileName, String data, String encoding) {
        return writeFile(fileName, data, encoding, false);
    }

    public boolean writeFile(String fileName, String data, String encoding, boolean append) {
        boolean r = false;
        try (
            FileOutputStream fileOutPutStream = new FileOutputStream(fileName, append);
            BufferedWriter bufferWriter = new BufferedWriter(new OutputStreamWriter(fileOutPutStream, encoding))
        )
        {
            bufferWriter.append(data);
//            bufferWriter.flush();
//            bufferWriter.close();
            r = true;
        } catch (IOException e) {

            if (e.getMessage().contains("(Процесс не может получить доступ к файлу, так как этот файл занят другим процессом)")) { //ToDo
                int pos;
                String newFileName;
                if ((pos = fileName.lastIndexOf(".")) > 0) {

                    newFileName = fileName.substring(0, pos) +
                            dateFormatTimeYMDHMS.format(System.currentTimeMillis()) +
                            fileName.substring(pos);
                } else newFileName = fileName + dateFormatTimeYMDHMS.format(System.currentTimeMillis());

                r = writeFile(newFileName, data, encoding,  append);

            } else {
//                e.printStackTrace();
                LOG.error(e);
            }
        }
        return r;
    }




    public void scanFiles(String filesPath, List<String> filesList) {
        scanFiles(filesPath, "", filesList);
    }

    public void scanFiles(String filesPath,
                          String filesMask,
                          List<String> filesList) {

        File file = new File(filesPath);
        if (file.exists()) {
            File[] listfiles = file.listFiles();
            for (File f : listfiles) {
                if (!f.isDirectory()) {
                    if (f.toString().contains(filesMask)) {
                        filesList.add(f.toString());
                    }
                } else if (f.isDirectory()) {
                    scanFiles(f.getPath(), filesMask, filesList);
                }
            }
        }
    }

    public void openFile( File file ) {
            String cmd = String.format( "cmd.exe /C start %s", file.getAbsolutePath());
        try {
            Runtime.getRuntime().exec( cmd );
        } catch (IOException e) {
            LOG.error(e);
        }
    }
}
