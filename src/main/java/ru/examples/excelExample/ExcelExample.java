package ru.examples.excelExample;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ExcelExample {

    public static void main( String[] args )
    {

        String fileExcel = "sample_excel.xls";

        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet("Просто лист");

        sheet.setColumnWidth(1, 10000);

        // счетчик для строк
        int rowNum = 0;

        Row row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue("field1");
        row.createCell(1).setCellValue("field2");
        row.createCell(2).setCellValue("field3");

        CellStyle style = workbook.createCellStyle();
//        style.setBorderBottom(styleBorderBottom);
//        style.setBorderLeft(styleBorderLeft);
//        style.setBorderRight(styleBorderRight);
//        style.setBorderTop(styleBorderTop);

        style.setAlignment(HorizontalAlignment.CENTER);

        row.setRowStyle(style);

        // заполняем лист данными
        for (int i = 0; i < 100; i++){
            row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue("f1"+i);
            row.createCell(1).setCellValue("f2"+i);
            row.createCell(2).setCellValue((double) i/3);
        }

        row = sheet.createRow(rowNum++);
        row.createCell(0, CellType.STRING).setCellValue("12345678901234567890");
        row.createCell(1, CellType.NUMERIC).setCellValue("=СУММ(C2:C"+(rowNum-1)+")");
        row.createCell(2, CellType.NUMERIC).setCellFormula("SUM(C2:C"+(rowNum-1)+")");

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
    }

}
