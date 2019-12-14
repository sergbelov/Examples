package ru.examples.arhiveExample;

import ru.utils.files.ArhiveService;

public class ArhiveExample {


    public static void main(String[] args) {

        String arhiveFileName = "arhive_example.zip";

//        String sourceName = "C:/TEMP/EXTRACT";
        String sourceName = "src/";
//        String sourceName = "ClassAList.json";

//        String pathForExtract = "C:/TEMP/EXTRACT";
        String pathForExtract = "arhive_extract";


        ArhiveService arhiveService = new ArhiveService();

        arhiveService.addFilesToArhive(sourceName, arhiveFileName);

        arhiveService.extractFilesFromArhive(arhiveFileName, pathForExtract);
    }


}