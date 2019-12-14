package ru.examples.fileIOExample;

import ru.utils.files.FileUtils;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileExample {
    public static void main(String[] args) {

        JFileChooser fileChooser = new JFileChooser();
//        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES); // //разрешить выбор папок и файлов
//        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); // выбор директории
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY); // //разрешить выбор файлов
        int ret = fileChooser.showDialog(null, null);
        if (ret == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            Path path = Paths.get(file.toString());

            System.out.println(file);

            System.out.println(path.getParent());
            System.out.println(path.getFileName());
        }


        //==============================//
        List<String> files = new ArrayList<>();
        FileUtils fileScan = new FileUtils();
        fileScan.scanFiles(System.getProperty("user.dir"), ".java", files);
        files.stream().forEach(x -> System.out.println(x));


        //==============================//
        String fileName = "pom.xml";
        System.out.println("\n\n=======================\nЧтение файла " + fileName + "\n=======================\n");
        List<String> fileData = null;
        try {
            fileData = Files.readAllLines(Paths.get(fileName), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < fileData.size(); i++){
            System.out.println(fileData.get(i));
        }
    }
}
