package ru.examples.excelExample;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class ExcelExampleXlsx {


    public static void main(String[] args) {

        DateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");

        String fileExcel = "sample_excel.xlsx";

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Просто лист");

        XSSFFont fontRed = workbook.createFont();
        fontRed.setColor(IndexedColors.RED.getIndex());

        XSSFFont fontBold = workbook.createFont();
        fontBold.setBold(true);


        DataFormat format = workbook.createDataFormat();

        BorderStyle thin = BorderStyle.THIN;

        CellStyle styleHeader = workbook.createCellStyle();
        styleHeader.setBorderLeft(thin);
        styleHeader.setBorderTop(thin);
        styleHeader.setBorderRight(thin);
        styleHeader.setBorderBottom(thin);
        styleHeader.setFont(fontBold);
//        styleHeader.setAlignment(HorizontalAlignment.LEFT);
        styleHeader.setAlignment(HorizontalAlignment.CENTER);
        styleHeader.setVerticalAlignment(VerticalAlignment.TOP);
        styleHeader.setWrapText(true);

        XSSFCellStyle styleError = workbook.createCellStyle();
        styleError.setAlignment(HorizontalAlignment.LEFT);
        styleError.setVerticalAlignment(VerticalAlignment.TOP);
        styleError.setWrapText(true);
        styleError.setFont(fontRed);
        styleError.setFillForegroundColor(new XSSFColor(new java.awt.Color(255, 255, 0)));
        styleError.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        XSSFCellStyle styleString = workbook.createCellStyle();
        styleString.setFillForegroundColor(new XSSFColor(new java.awt.Color(255, 255, 0)));
        styleString.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        XSSFCellStyle styleString2 = workbook.createCellStyle();
        styleString2.setFillForegroundColor(new XSSFColor(new java.awt.Color(128, 128, 0)));
        styleString2.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        XSSFCellStyle styleNumber = workbook.createCellStyle();
        styleNumber.setDataFormat(format.getFormat("#,##0.00"));
        styleNumber.setFillForegroundColor(new XSSFColor(new java.awt.Color(0, 255, 255)));
        styleNumber.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        XSSFCellStyle styleDateTime = workbook.createCellStyle();
        styleDateTime.setDataFormat(format.getFormat("dd/mm/yyyy hh:mm:ss.000"));
//        styleDateTime.setFont(fontRed);
//        styleDateTime.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        styleDateTime.setFillForegroundColor(new XSSFColor(new java.awt.Color(200, 0, 200)));
        styleDateTime.setFillPattern(FillPatternType.SOLID_FOREGROUND);


        // счетчик для строк
        int rowNum = 0;

        Row row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue("field1 1234567890");
        row.createCell(1).setCellValue("field2");
        row.createCell(2).setCellValue("field3");
        row.createCell(3).setCellValue("field3");
        row.createCell(4).setCellValue("field3");

        sheet.setColumnWidth(0, 10000);
        sheet.setColumnWidth(1, 10000);
        sheet.setColumnWidth(2, 10000);
        sheet.setColumnWidth(3, 10000);
        sheet.setColumnWidth(4, 10000);

        for (int r = 0; r < 5; r++) {
            row.getCell(r).setCellStyle(styleHeader);
        }
//        row.setRowStyle(styleNorm);

        // заполняем лист данными
        for (int i = 0; i < 100; i++) {
            row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue("f1" + i);
            row.createCell(1).setCellValue("f2" + i);
            row.createCell(2, CellType.NUMERIC).setCellValue((double) i / 3);
            row.createCell(3, CellType.STRING).setCellValue("40702810011234567890");
            row.createCell(4).setCellValue(dateTimeFormat.format(System.currentTimeMillis()));


            row.getCell(1).setCellStyle(styleError);
            row.getCell(2).setCellStyle(styleNumber);
            row.getCell(4).setCellStyle(styleDateTime);

            if (i % 2 == 0) {
                row.getCell(3).setCellStyle(styleString2);
            } else {
                row.getCell(3).setCellStyle(styleString);
            }

        }

        row = sheet.createRow(rowNum++);
        row.createCell(0, CellType.STRING).setCellValue("12345678901234567890");
        row.createCell(1, CellType.NUMERIC).setCellValue("=СУММ(C2:C" + (rowNum - 1) + ")");
        row.createCell(2, CellType.NUMERIC).setCellFormula("SUM(C2:C" + (rowNum - 1) + ")");

        CellRangeAddress region = new CellRangeAddress(1, 3, 0, 0);
        sheet.addMergedRegion(region);

        region = new CellRangeAddress(5, 10, 0, 0);
        sheet.addMergedRegion(region);

        sheet.setVerticallyCenter(true);
        sheet.autoSizeColumn(0);

        // записываем созданный в памяти Excel документ в файл
        try (FileOutputStream out = new FileOutputStream(new File(fileExcel))) {
            workbook.write(out);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Excel файл " + fileExcel + " успешно создан!");

        addPicture(new File("picture/Disney_icons_48182.png"));
    }

    /**
     * Картинка в Excel
     * @param file
     */
    private static void addPicture(File file) {

        if (!file.exists()) {
            System.out.println("Файл " + file.getName() + " не найден");
            return;
        }
        BufferedImage img = null;
        try {
            img = ImageIO.read(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

/*
        int newWidth = 1000;
        int newHeight = 1000;
        BufferedImage img = resize(img0, newWidth, newHeight);
*/

        int col = 1, row = 1;
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet testsheet = wb.createSheet("test");
        System.out.println("The work book is created");
        try {
            FileOutputStream fos = new FileOutputStream("sample.xls");
            System.out.println("File sample.xls is created");
            HSSFClientAnchor anchor = new HSSFClientAnchor(0, 0, 0, 0, (short) col, row, (short) 6, 10);

            ByteArrayOutputStream bas = new ByteArrayOutputStream();
            ImageIO.getWriterFormatNames();
            ImageIO.write(img, "png", bas);
            byte[] data = bas.toByteArray();

            int index = wb.addPicture(bas.toByteArray(), HSSFWorkbook.PICTURE_TYPE_JPEG);
            HSSFSheet sheet = wb.getSheet("test");
            HSSFPatriarch patriarch = sheet.createDrawingPatriarch();
            patriarch.createPicture(anchor, index);
            anchor.setAnchorType(2);
            wb.write(fos);
            System.out.println("Writing data to the xls file");
            fos.close();
            System.out.println("File closed");
        } catch (IOException ioe) {
            System.out.println("Hi ! You got an exception. " + ioe.getMessage());
        }
    }

    /**
     * Изменение размера картинки
     * @param img
     * @param newW
     * @param newH
     * @return
     */
    public static BufferedImage resize(BufferedImage img, int newW, int newH) {
        int w = img.getWidth();
        int h = img.getHeight();
        BufferedImage dimg = new BufferedImage(newW, newH, img.getType());
        Graphics2D g = dimg.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(img, 0, 0, newW, newH, 0, 0, w, h, null);
        g.dispose();
        return dimg;
    }
}
