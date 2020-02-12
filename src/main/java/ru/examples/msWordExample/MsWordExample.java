package ru.examples.msWordExample;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class MsWordExample {
    public static void main(String[] args) {
        String wordFile = "word_example.docx";
        String pathPict = "picture/";
        String[] pictFiles = {"Disney_icons_48182.png", "Disney_icons_48185.png"};

        XWPFDocument document = new XWPFDocument();
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun xwpfRun = paragraph.createRun();

        int typePict = XWPFDocument.PICTURE_TYPE_GIF;
        for (String pictFile: pictFiles) {
            pictFile = pathPict + pictFile;
            xwpfRun.setText(pictFile);
            xwpfRun.addBreak();
            try {
                xwpfRun.addPicture(new FileInputStream(pictFile),
                        typePict,
                        pictFile,
                        Units.toEMU(200),
                        Units.toEMU(200)); // 200x200 pixels
            } catch (InvalidFormatException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //xwpfRun.addBreak(BreakType.PAGE);
        }

        try (FileOutputStream out = new FileOutputStream(wordFile)) {
            document.write(out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
