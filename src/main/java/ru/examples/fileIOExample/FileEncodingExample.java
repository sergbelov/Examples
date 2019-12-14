package ru.examples.fileIOExample;

import javax.swing.*;
import java.io.*;
import java.util.Arrays;

public class FileEncodingExample {

    public static void main(String[] args) {

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); // выбор директории
//		fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES); // //разрешить выбор папок и файлов
        int ret = fileChooser.showDialog(null, null);//null, "Каталог с файлами");
        if (ret == JFileChooser.APPROVE_OPTION) {
            File dir = fileChooser.getSelectedFile();
            File dirNew = new File(dir + File.separator + "new" +File.separator);
            if (dirNew.exists() || dirNew.mkdirs()) {

                // файлы из каталога
                String currEncoding = "Cp1251";
                String newEncoding = "UTF-8";

                int bytesRead;
                byte[] buffer = new byte[1024];

                String fileName;
                File[] files = dir.listFiles();
                StringBuilder sb = new StringBuilder();
                for (File f : files) {
                    fileName = f.getAbsolutePath();
                    if (fileName.toLowerCase().endsWith(".txt")) {
                        sb.setLength(0);
                        System.out.println(fileName);
                        File fileNew = new File(dirNew, f.getName());

                        // читаем содержимое файла
                        try (FileInputStream fileInputStream = new FileInputStream(fileName)) {
                            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                                sb.append(new String(Arrays.copyOf(buffer, bytesRead), currEncoding));
                            }
                            fileInputStream.close();
                            writeFile(fileNew, sb, newEncoding);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private static void writeFile(File file, StringBuilder data, String newEncoding) {
        try (
            FileOutputStream fileOutPutStream = new FileOutputStream(file, false);
            BufferedWriter bufferWriter = new BufferedWriter(new OutputStreamWriter(fileOutPutStream, newEncoding))
            )
        {
            bufferWriter.append(data);
            bufferWriter.flush();
            bufferWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
