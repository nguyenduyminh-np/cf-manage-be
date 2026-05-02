package com.duyminhdev.cf_manager.utils;

import com.duyminhdev.cf_manager.annotation.ExcelColumn;

import com.duyminhdev.cf_manager.constant.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ExcelUtils {

    private static final Logger log = LoggerFactory.getLogger(ExcelUtils.class);
    private static CellStyle styleForDataCell;

    // style cho header excel
    public static CellStyle styleForHeader(SXSSFSheet sheet) {
        Workbook wb = sheet.getWorkbook();
        Font font = wb.createFont();
        font.setFontName("Times New Roman");
        font.setBold(true);

        CellStyle cellStyle = wb.createCellStyle();
        cellStyle.setFont(font);
        // cellStyle.setFillForegroundColor(IndexedColors.SEA_GREEN.getIndex());
        // cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        // border 4 cạnh
        setAllBorder(cellStyle, BorderStyle.THIN);

        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        return cellStyle;
    }

    public static void setAllBorder(CellStyle cellStyle, BorderStyle borderStyle) {
        cellStyle.setBorderTop(borderStyle);
        cellStyle.setBorderBottom(borderStyle);
        cellStyle.setBorderLeft(borderStyle);
        cellStyle.setBorderRight(borderStyle);
    }

    public static CellStyle styleForDataCell(SXSSFSheet sheet) {
        DataFormat df = sheet.getWorkbook().createDataFormat();
        CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
        cellStyle.setDataFormat(df.getFormat("@"));
        setAllBorder(cellStyle, BorderStyle.THIN);
        cellStyle.setAlignment(HorizontalAlignment.LEFT);
        return cellStyle;
    }

    public static CellStyle createTextStyle(Workbook wb, short fontSize, boolean bold, HorizontalAlignment halign, VerticalAlignment valign) {
        Font font = wb.createFont();
        font.setFontName("Times New Roman");
        font.setFontHeightInPoints(fontSize);
        font.setBold(bold);

        CellStyle style = wb.createCellStyle();
        style.setFont(font);
        style.setAlignment(halign);
        style.setVerticalAlignment(valign);
        return style;
    }

    public static CellStyle styleForSttCell(SXSSFSheet sheet) {
        DataFormat df = sheet.getWorkbook().createDataFormat();
        CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
        cellStyle.setDataFormat(df.getFormat("@"));
        setAllBorder(cellStyle, BorderStyle.THIN);
        cellStyle.setAlignment(HorizontalAlignment.CENTER);         // căn giữa ngang
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);   // căn giữa dọc
        return cellStyle;
    }

    public static void mergeAndStyle(Sheet sheet, CellRangeAddress range, String value, CellStyle style) {
        // Thực hiện merge
        sheet.addMergedRegion(range);

        // Gán text cho ô góc trên bên trái
        Row row = sheet.getRow(range.getFirstRow());
        if (row == null) {
            row = sheet.createRow(range.getFirstRow());
        }
        Cell cell = row.getCell(range.getFirstColumn());
        if (cell == null) {
            cell = row.createCell(range.getFirstColumn());
        }
        cell.setCellValue(value);

        // Gán style cho tất cả cell trong vùng merge
        for (int r = range.getFirstRow(); r <= range.getLastRow(); r++) {
            Row currentRow = sheet.getRow(r);
            if (currentRow == null) {
                currentRow = sheet.createRow(r);
            }
            for (int c = range.getFirstColumn(); c <= range.getLastColumn(); c++) {
                Cell currentCell = currentRow.getCell(c);
                if (currentCell == null) {
                    currentCell = currentRow.createCell(c);
                }
                currentCell.setCellStyle(style);
            }
        }

        // Kẻ viền cho toàn bộ vùng merge
        RegionUtil.setBorderTop(style.getBorderTop(), range, sheet);
        RegionUtil.setBorderBottom(style.getBorderBottom(), range, sheet);
        RegionUtil.setBorderLeft(style.getBorderLeft(), range, sheet);
        RegionUtil.setBorderRight(style.getBorderRight(), range, sheet);
    }

    // đầu trang excel
    public static int writeHeader(SXSSFSheet sheet, int rowIndex, List<String> columnHeader, String paramName) {
        Workbook wb = sheet.getWorkbook();

        // Số cột của bảng: STT + columnHeader.size()
        int totalCols = columnHeader.size() + 1;
        int lastCol = totalCols - 1;

        // Tạo các Style
        CellStyle titleStyle = createTextStyle(wb, (short) 12, true,
                HorizontalAlignment.LEFT,
                VerticalAlignment.CENTER);
        CellStyle dateStyle = createTextStyle(wb, (short) 11, true,
                HorizontalAlignment.LEFT,
                VerticalAlignment.CENTER);
        CellStyle paramStyle = createTextStyle(wb, (short) 20, true,
                HorizontalAlignment.CENTER,
                VerticalAlignment.CENTER);
        CellStyle headerStyle = styleForHeader(sheet); // styleForHeader đã có border + center

        // 1) Dòng Title: merge toàn vùng, font12, căn trái
        {
            SXSSFRow row = sheet.createRow(rowIndex);
            row.setHeightInPoints(22);
            Cell cell = row.createCell(0);
            cell.setCellStyle(titleStyle);
            cell.setCellValue("CHƯƠNG TRÌNH QUẢN LÝ QUÁN CAFE CF-M");
            sheet.addMergedRegion(new CellRangeAddress(
                    rowIndex, rowIndex,   // từ hàng này
                    0, lastCol            // merge từ cột 0 tới cột cuối
            ));
            rowIndex++;
        }

        // 2) Dòng Ngày tạo: font10, căn trái, tại cột 0
        {
            SXSSFRow row = sheet.createRow(rowIndex);
            row.setHeightInPoints(18);
            String formattedDate = LocalDate.now()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            Cell cell = row.createCell(0);
            cell.setCellStyle(dateStyle);
            cell.setCellValue("Ngày tạo: " + formattedDate);
            rowIndex++;
        }

        // 3) Dòng ParamName: merge toàn vùng, font14 bold, căn giữa, tăng row height
        {
            SXSSFRow row = sheet.createRow(rowIndex);
            row.setHeightInPoints(36);
            Cell cell = row.createCell(0);
            cell.setCellStyle(paramStyle);
            cell.setCellValue(paramName.toUpperCase());
            sheet.addMergedRegion(new CellRangeAddress(
                    rowIndex, rowIndex,
                    0, lastCol
            ));
            rowIndex += 2; // để trống 1 dòng trước khi vào header bảng
        }

        // 4) Dòng header của bảng: STT + UPPERCASE các columnHeader
        {
            SXSSFRow row = sheet.createRow(rowIndex);
            // STT
            Cell cell = row.createCell(0);
            row.setHeightInPoints(20);
            cell.setCellStyle(headerStyle);
            cell.setCellValue("STT");
            // Các column còn lại
            for (int i = 0; i < columnHeader.size(); i++) {
                cell = row.createCell(i + 1);
                cell.setCellStyle(headerStyle);
                cell.setCellValue(columnHeader.get(i).toUpperCase());
            }
            setWidthColumn(sheet, columnHeader);
            rowIndex++;
        }

        return rowIndex;
    }

    public static int writeHeader(SXSSFSheet sheet, int rowIndex, List<String> columnHeader, String paramName, boolean hasAdditionalInfo) {
        Workbook wb = sheet.getWorkbook();

        // Số cột của bảng: STT + columnHeader.size()
        int totalCols = columnHeader.size() + 1;
        int lastCol = totalCols - 1;

        // Tạo các Style
        CellStyle titleStyle = createTextStyle(wb, (short) 12, true,
                HorizontalAlignment.LEFT,
                VerticalAlignment.CENTER);
        CellStyle dateStyle = createTextStyle(wb, (short) 11, true,
                HorizontalAlignment.LEFT,
                VerticalAlignment.CENTER);
        CellStyle paramStyle = createTextStyle(wb, (short) 20, true,
                HorizontalAlignment.CENTER,
                VerticalAlignment.CENTER);
        CellStyle headerStyle = styleForHeader(sheet); // styleForHeader đã có border + center

        // 1) Dòng Title: merge toàn vùng, font12, căn trái
        if(hasAdditionalInfo) {
            {
                SXSSFRow row = sheet.createRow(rowIndex);
                row.setHeightInPoints(22);
                Cell cell = row.createCell(0);
                cell.setCellStyle(titleStyle);
                cell.setCellValue("NGÂN HÀNG TMCP ĐẦU TƯ VÀ PHÁT TRIỂN VIỆT NAM");
                sheet.addMergedRegion(new CellRangeAddress(
                        rowIndex, rowIndex,   // từ hàng này
                        0, lastCol            // merge từ cột 0 tới cột cuối
                ));
                rowIndex++;
            }

            // 2) Dòng Ngày tạo: font10, căn trái, tại cột 0
            {
                SXSSFRow row = sheet.createRow(rowIndex);
                row.setHeightInPoints(18);
                String formattedDate = LocalDate.now()
                        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                Cell cell = row.createCell(0);
                cell.setCellStyle(dateStyle);
                cell.setCellValue("Ngày tạo: " + formattedDate);
                rowIndex++;
            }

            // 3) Dòng ParamName: merge toàn vùng, font14 bold, căn giữa, tăng row height
            {
                SXSSFRow row = sheet.createRow(rowIndex);
                row.setHeightInPoints(36);
                Cell cell = row.createCell(0);
                cell.setCellStyle(paramStyle);
                cell.setCellValue(paramName.toUpperCase());
                sheet.addMergedRegion(new CellRangeAddress(
                        rowIndex, rowIndex,
                        0, lastCol
                ));
                rowIndex += 2; // để trống 1 dòng trước khi vào header bảng
            }
        }

        // 4) Dòng header của bảng: STT + UPPERCASE các columnHeader
        {
            SXSSFRow row = sheet.createRow(rowIndex);
            // STT
            Cell cell = row.createCell(0);
            row.setHeightInPoints(20);
            cell.setCellStyle(headerStyle);
            cell.setCellValue("STT");
            // Các column còn lại
            for (int i = 0; i < columnHeader.size(); i++) {
                cell = row.createCell(i + 1);
                cell.setCellStyle(headerStyle);
                cell.setCellValue(columnHeader.get(i).toUpperCase());
            }
            setWidthColumn(sheet, columnHeader);
            rowIndex++;
        }

        return rowIndex;
    }

    private static void setWidthColumn(SXSSFSheet sheet, List<String> columnHeader) {
        for (int i = 0; i < columnHeader.size(); i++) {
            int width = columnHeader.get(i).length();
            sheet.setColumnWidth(i + 1, (width * 400) < 3000 ? 3000 : width * 400); // i + 1 vì cột đầu tiên là STT
        }
    }

    // lấy ra tên field của đối tượng và value annotation @ExcelColumn
//    private static <T> void extractFiled(Class<T> clazz, List<String> anFiled, List<String> nameFiled) {
//        // Class<?> clazz = object.getClass();
//        Field[] fields = clazz.getDeclaredFields();
//
//        for (Field field : fields) {
//            ExcelColumn annotation = field.getAnnotation(ExcelColumn.class);
//            if (annotation != null) {
//                // if (!field.canAccess(object)) {
//                field.setAccessible(true); // Cân nhắc giữ lại dòng này nếu không còn cách nào khác
//                // }
//                nameFiled.add(field.getName());
//                anFiled.add(annotation.value());
//            }
//        }
//        // Class<?> superClazz = clazz.getSuperclass();
//        // if (superClazz != null){
//        // extractFieldsFromClass(superClazz, anFiled, nameFiled);
//        // }
//    }

    private static <T> void extractFiled(Class<?> clazz, List<String> anFiled, List<String> nameFiled) {
        // Class<?> clazz = object.getClass();
        while (clazz != null && clazz != Object.class) {
            Field[] fields = clazz.getDeclaredFields();

            for (Field field : fields) {
                ExcelColumn annotation = field.getAnnotation(ExcelColumn.class);
                if (annotation != null) {
                    field.setAccessible(true); // Cho phép truy cập cả field private
                    nameFiled.add(field.getName());
                    anFiled.add(annotation.value());
                }
            }

            // Di chuyển lên class cha để tiếp tục duyệt
            clazz = clazz.getSuperclass();
        }

    }

    //lấy anotaion từ thằng cha
    private static Field getFieldRecursive(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        while (clazz != null && clazz != Object.class) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass(); // tiếp tục tìm ở class cha
            }
        }
        throw new NoSuchFieldException("Field not found: " + fieldName);
    }
    // ghi một dòng excel v1 không lấy anotaion từ kế thừa
//    private static <T> void writeValue(List<String> fields, Row row, T item, int index, SXSSFSheet sheet, CellStyle dataStyle, CellStyle sttStyle)
//            throws NoSuchFieldException, IllegalAccessException {
//        // Chuẩn bị style reuse
//        // 1) Ghi STT và căn giữa
//        Cell cell = row.createCell(0);
//        cell.setCellValue(index);
//        cell.setCellStyle(sttStyle);  // <-- áp styleForSttCell
//
//        // 2) Ghi các cột dữ liệu còn lại
//        for (int i = 0; i < fields.size(); i++) {
//            cell = row.createCell(i + 1);
//            Field field = item.getClass().getDeclaredField(fields.get(i));
//            field.setAccessible(true);
//            Object value = field.get(item);
//
//            // chuyển thành chuỗi hoặc text, giữ default Text style
//            setRowValue(cell, value, sheet);
//
//            // rồi áp dataStyle (căn trái, có border)
//            cell.setCellStyle(dataStyle);
//        }
//    }

    private static <T> void writeValue(List<String> fields, Row row, T item, int index, SXSSFSheet sheet, CellStyle dataStyle, CellStyle sttStyle)
            throws NoSuchFieldException, IllegalAccessException {
        // Chuẩn bị style reuse
        // 1) Ghi STT và căn giữa
        Cell cell = row.createCell(0);
        cell.setCellValue(index);
        cell.setCellStyle(sttStyle);  // <-- áp styleForSttCell

        // 2) Ghi các cột dữ liệu còn lại
        for (int i = 0; i < fields.size(); i++) {
            cell = row.createCell(i + 1);
            String fieldName = fields.get(i);

            // Lấy field từ class hoặc superclass
            Field field = getFieldRecursive(item.getClass(), fieldName);
            field.setAccessible(true);
            Object value = field.get(item);

            // Ghi giá trị vào cell
            setRowValue(cell, value, sheet);

            // Áp style cho dữ liệu
            cell.setCellStyle(dataStyle);
        }
    }

    private static <T> void writeValueAsTextStyle(List<String> data, Row row, T clazz, int index, SXSSFSheet sheet, CellStyle dataStyle, CellStyle sttStyle) throws NoSuchFieldException, IllegalAccessException {
        Cell cell = row.createCell(0);
        cell.setCellValue(index);
        cell.setCellStyle(sttStyle);

        // Các cột text
        for (int i = 0; i < data.size(); i++) {
            cell = row.createCell(i + 1);
            Field field = clazz.getClass().getDeclaredField(data.get(i));
            field.setAccessible(true);
            Object value = field.get(clazz);

            setTextRowValue(cell, value, sheet);
            cell.setCellStyle(dataStyle);
        }
    }

    // Chuyển dữ liệu từ java -> excel
    public static void setRowValue(Cell cell, Object value, SXSSFSheet sheet) {
        if (value == null) {
            cell.setCellValue("");
            return;
        }
        if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else if (value instanceof Date) {
            String text = new SimpleDateFormat(Constants.DateFormatType.DD_MM_YYYY_HH_MM_SS2).format((Date) value);
            cell.setCellValue(text);
        } else if (value instanceof Calendar) {
            String text = new SimpleDateFormat(Constants.DateFormatType.DD_MM_YYYY_HH_MM_SS2).format(((Calendar) value).getTime());
            cell.setCellValue(text);
        } else if (value instanceof LocalDateTime) {
            String text = ((LocalDateTime) value).format(DateTimeFormatter.ofPattern(Constants.DateFormatType.DD_MM_YYYY_HH_MM_SS2));
            cell.setCellValue(text);
        } else if (value instanceof LocalDate) {
            String text = ((LocalDate) value).format(DateTimeFormatter.ofPattern(Constants.DateFormatType.DD_MM_YYYY)) + " 00:00:00";
            cell.setCellValue(text);
        } else {
            cell.setCellValue(String.valueOf(value));
        }
    }

    /**
     * Hám set value và kiểu dữ liệu của ô là text
     * bị lỗi nếu dữ liệu lớn <code>The maximum number of Cell Styles was exceeded. You can define up to 64000 style in a .xlsx Workbook</code>
     * @param cell
     * @param value
     * @param sheet
     */
    private static void setTextRowValue(Cell cell, Object value, SXSSFSheet sheet) {
        CellStyle textStyle = styleForDataCell(sheet);

        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Number) {
            cell.setCellValue(String.valueOf(value));
        } else if (value instanceof Boolean) {
            cell.setCellValue(String.valueOf(value));
        } else if (value instanceof Date) {
            String text = new SimpleDateFormat(Constants.DateFormatType.DD_MM_YYYY_HH_MM_SS2).format((Date) value);
            cell.setCellValue(text);
        } else if (value instanceof Calendar) {
            String text = new SimpleDateFormat(Constants.DateFormatType.DD_MM_YYYY_HH_MM_SS2).format(((Calendar) value).getTime());
            cell.setCellValue(text);
        } else if (value instanceof LocalDateTime) {
            String text = ((LocalDateTime) value).format(DateTimeFormatter.ofPattern(Constants.DateFormatType.DD_MM_YYYY_HH_MM_SS2));
            cell.setCellValue(text);
        } else if (value instanceof LocalDate) {
            String text = ((LocalDate) value).format(DateTimeFormatter.ofPattern(Constants.DateFormatType.DD_MM_YYYY)) + " 00:00:00";
            cell.setCellValue(text);
        } else {
            // Trường hợp mặc định, ép sang chuỗi
            cell.setCellValue(String.valueOf(value));
        }
        cell.setCellStyle(textStyle);
    }

    // viết excel
    public static <T> Workbook getWorkBox(Class<T> clazz, List<T> data, String paramName) {
        SXSSFWorkbook workbook = new SXSSFWorkbook();
        SXSSFSheet sheet = workbook.createSheet("Sheet1");

        // Tính lastCol
        List<String> alias = new ArrayList<>();
        List<String> column = new ArrayList<>();
        extractFiled(clazz, alias, column);

        // 2) Viết header và data như bình thường
        int rowIndex = writeHeader(sheet, 0, alias, paramName);
        CellStyle sttStyle = styleForSttCell(sheet);
        CellStyle dataStyle = styleForDataCell(sheet);
        int index = 1;
        try {
            for (T item : data) {
                Row row = sheet.createRow(rowIndex++);
                writeValue(column, row, item, index++, sheet, dataStyle, sttStyle);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException("error when export to excel");
        }

        return workbook;
    }

    public static <T> Workbook getWorkBox(Class<T> clazz, List<T> data, String paramName, boolean hasAdditionalInfo) {
        SXSSFWorkbook workbook = new SXSSFWorkbook();
        SXSSFSheet sheet = workbook.createSheet("Sheet1");

        // Tính lastCol
        List<String> alias = new ArrayList<>();
        List<String> column = new ArrayList<>();
        extractFiled(clazz, alias, column);

        // 2) Viết header và data như bình thường
        int rowIndex = writeHeader(sheet, 0, alias, paramName, hasAdditionalInfo);
        CellStyle sttStyle = styleForSttCell(sheet);
        CellStyle dataStyle = styleForDataCell(sheet);
        int index = 1;
        try {
            for (T item : data) {
                Row row = sheet.createRow(rowIndex++);
                writeValue(column, row, item, index++, sheet, dataStyle, sttStyle);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException("error when export to excel");
        }

        return workbook;
    }

    public static <T> Workbook getTextStyleWorkbook(Class<T> clazz, List<T> data, String paramName) {
        SXSSFWorkbook workbook = new SXSSFWorkbook();
        SXSSFSheet sheet = workbook.createSheet("Sheet1");

        // Lấy columnHeader, tính lastCol
        List<String> alias = new ArrayList<>();
        List<String> column = new ArrayList<>();
        extractFiled(clazz, alias, column);
        // Viết header
        int rowIndex = writeHeader(sheet, 0, alias, paramName);

        // Viết data dưới dạng text
        int index = 1;
        CellStyle sttStyle = styleForSttCell(sheet);
        CellStyle dataStyle = styleForDataCell(sheet);
        try {
            for (T item : data) {
                Row row = sheet.createRow(rowIndex++);

                writeValueAsTextStyle(column, row, item, index++, sheet, dataStyle, sttStyle);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException("error when export to excel");
        }
        return workbook;
    }

    public static <T> Workbook getTextStyleWorkbook(Class<T> clazz, List<T> data, String paramName, boolean hasAdditionalInfo, boolean hasStyleRow) {
        SXSSFWorkbook workbook = new SXSSFWorkbook();
        SXSSFSheet sheet = workbook.createSheet("Sheet1");

        // Lấy columnHeader, tính lastCol
        List<String> alias = new ArrayList<>();
        List<String> column = new ArrayList<>();
        extractFiled(clazz, alias, column);
        // Viết header
        int rowIndex = writeHeader(sheet, 0, alias, paramName, hasAdditionalInfo);

        // Viết data dưới dạng text
        int index = 1;
        CellStyle sttStyle = null;
        CellStyle dataStyle = null;
        if(hasStyleRow) {
            sttStyle = styleForSttCell(sheet);
            dataStyle = styleForDataCell(sheet);
        }
        try {
            for (T item : data) {
                Row row = sheet.createRow(rowIndex++);

                writeValueAsTextStyle(column, row, item, index++, sheet, dataStyle, sttStyle);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException("error when export to excel");
        }
        return workbook;
    }

    /**
     * @param workbook
     * @param rowIndex dòng bắt đầu ghi data
     * @param data
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> SXSSFWorkbook writeWordBook(SXSSFWorkbook workbook, int rowIndex, List<T> data, Class<T> clazz) {
        SXSSFSheet sheet = (SXSSFSheet) workbook.getSheetAt(0);
        // lấy ra các trường của class
        Field[] fields = clazz.getDeclaredFields();
        // danh sách tên các trường của class
        List<String> listColumn = new ArrayList<>();
        for (Field field : fields) {
            listColumn.add(field.getName());
        }
        // ghi từ dòng index
        int index = 1;
        CellStyle sttStyle = styleForSttCell(sheet);
        CellStyle dataStyle = styleForDataCell(sheet);
        try {
            for (T item : data) {
                Row row = sheet.createRow(rowIndex);
                writeValue(listColumn, row, item, index, sheet, dataStyle, sttStyle);
                rowIndex++;
                index++;
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException("error when export to excel");
        }
        return workbook;
    }

    public static <T> void exportExcel(HttpServletResponse response, String path, String fileName, List<T> listData,
                                       int rowIndex, Class<T> clazz) throws IOException {
        response.setContentType("application/octet-stream");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=" + fileName + ".xlsx";
        InputStream fileExcelInpS;
        try {
            fileExcelInpS = new ClassPathResource(path).getInputStream();
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new IOException("Template file not found");
        }
        XSSFWorkbook workbook = new XSSFWorkbook(fileExcelInpS);
        // tạo file excel dùng để xuất dữ liệu lớn
        SXSSFWorkbook sxssfWorkbook = new SXSSFWorkbook(workbook);
        writeWordBook(sxssfWorkbook, rowIndex, listData, clazz);
        ServletOutputStream outputStream = response.getOutputStream();
        sxssfWorkbook.write(outputStream);
        response.setHeader(headerKey, headerValue);
        workbook.close();
        outputStream.close();
    }

    // hàm export
    public static <T> void export(HttpServletResponse response, Class<T> clazz, List<T> data, String fileName)
            throws IOException {
        export(response, clazz, data, fileName, fileName);
    }

    public static <T> void export(HttpServletResponse response, Class<T> clazz, List<T> data, String fileName,
                                  String paramName) throws IOException {
        Workbook workbook = getWorkBox(clazz, data, paramName);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String headerKey = "Content-Disposition";
        String downloadFileName = fileName.endsWith(".xlsx") ? fileName : fileName + ".xlsx";
        String headerValue = "attachment; filename=\"" + downloadFileName + "\"";
        response.setHeader(headerKey, headerValue);
        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
    }

    public static <T> void export(HttpServletResponse response, Class<T> clazz, List<T> data, String fileName,
                                  String paramName, boolean hasAdditionalInfo) throws IOException {
        Workbook workbook = getWorkBox(clazz, data, paramName, hasAdditionalInfo);
        response.setContentType("application/octet-stream");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=" + fileName + ".xlsx";
        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        response.setHeader(headerKey, headerValue);
        workbook.close();
        outputStream.close();
    }

    public static <T> void exportTextStyle(HttpServletResponse response, Class<T> clazz, List<T> data, String fileName,
                                           String paramName) throws IOException {
        Workbook workbook = getTextStyleWorkbook(clazz, data, paramName);
        response.setContentType("application/octet-stream");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=" + fileName + ".xlsx";
        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        response.setHeader(headerKey, headerValue);
        workbook.close();
        outputStream.close();
    }

    public static <T> void exportResultUpdateWorkingCalendar(HttpServletResponse response, Class<T> clazz, List<T> data, String fileName,
                                                             String headerName) throws IOException {
        SXSSFWorkbook workbook = new SXSSFWorkbook();
        SXSSFSheet sheet = workbook.createSheet("Sheet1");

        // Tính lastCol
        List<String> alias = new ArrayList<>();
        List<String> column = new ArrayList<>();
        extractFiled(clazz, alias, column);

        // 2) Viết header và data như bình thường
        int totalCols = alias.size() + 1;
        int lastCol = totalCols - 1;

        // Tạo các Style
        CellStyle titleStyle = createTextStyle(workbook, (short) 12, true,
                HorizontalAlignment.LEFT,
                VerticalAlignment.CENTER);
        CellStyle dateStyle = createTextStyle(workbook, (short) 11, true,
                HorizontalAlignment.LEFT,
                VerticalAlignment.CENTER);
        CellStyle paramStyle = createTextStyle(workbook, (short) 20, true,
                HorizontalAlignment.CENTER,
                VerticalAlignment.CENTER);
        CellStyle headerStyle = styleForHeader(sheet); // styleForHeader đã có border + center

        // 1) Dòng Title: merge toàn vùng, font12, căn trái
        int rowIndex = 1;
        {
            SXSSFRow row = sheet.createRow(rowIndex);
            row.setHeightInPoints(22);
            Cell cell = row.createCell(0);
            cell.setCellStyle(titleStyle);
            cell.setCellValue("CHƯƠNG TRÌNH QUẢN LÝ QUÁN CAFE CF-M");
            sheet.addMergedRegion(new CellRangeAddress(
                    rowIndex, rowIndex,   // từ hàng này
                    0, lastCol            // merge từ cột 0 tới cột cuối
            ));
            rowIndex++;
        }

        // 2) Dòng Ngày tạo: font10, căn trái, tại cột 0
        {
            SXSSFRow row = sheet.createRow(rowIndex);
            row.setHeightInPoints(18);
            String formattedDate = LocalDate.now()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            Cell cell = row.createCell(0);
            cell.setCellStyle(dateStyle);
            cell.setCellValue("Ngày tạo: " + formattedDate);
            rowIndex++;
        }

        // 3) Dòng ParamName: merge toàn vùng, font14 bold, căn giữa, tăng row height
        {
            SXSSFRow row = sheet.createRow(rowIndex);
            row.setHeightInPoints(36);
            Cell cell = row.createCell(0);
            cell.setCellStyle(paramStyle);
            cell.setCellValue(headerName.toUpperCase());
            sheet.addMergedRegion(new CellRangeAddress(
                    rowIndex, rowIndex,
                    0, lastCol
            ));
            rowIndex += 2; // để trống 1 dòng trước khi vào header bảng
        }

        // 4) Dòng header của bảng: STT + UPPERCASE các columnHeader
        {
            SXSSFRow row1 = sheet.createRow(rowIndex);
            // STT + kênh thanh toán + cấu phần xử lý + Ngày làm việc
            Cell cell = row1.createCell(0);
            row1.setHeightInPoints(20);
            cell.setCellStyle(headerStyle);
            cell.setCellValue("STT");
            cell.setCellStyle(headerStyle);

            for (int i = 0; i < 3; i++) {
                cell = row1.createCell(i + 1);
                cell.setCellStyle(headerStyle);
                cell.setCellValue(alias.get(i).toUpperCase());
            }

            // Merge nhóm “Thông tin trước chỉnh sửa” (4-9)
            CellRangeAddress beforeRange = new CellRangeAddress(rowIndex, rowIndex, 4, 10);
            mergeAndStyle(sheet, beforeRange, "THÔNG TIN TRƯỚC CHỈNH SỬA", headerStyle);

            CellRangeAddress afterRange = new CellRangeAddress(rowIndex, rowIndex, 11, 17);
            mergeAndStyle(sheet, afterRange, "THÔNG TIN SAU CHỈNH SỬA", headerStyle);

            // Các cột còn lại
            rowIndex++;
            SXSSFRow row2 = sheet.createRow(rowIndex);
            for (int i = 3; i < alias.size(); i++) {
                cell = row2.createCell(i + 1);
                cell.setCellStyle(headerStyle);
                cell.setCellValue(alias.get(i).toUpperCase());
            }

            // merge ô STT (cột 0, từ rowIndex-1 đến rowIndex)
            mergeAndStyle(sheet, new CellRangeAddress(rowIndex - 1, rowIndex, 0, 0), "STT", headerStyle);

            // merge ô Kênh thanh toán
            mergeAndStyle(sheet, new CellRangeAddress(rowIndex - 1, rowIndex, 1, 1), "KÊNH THANH TOÁN", headerStyle);

            // merge ô Cấu phần
            mergeAndStyle(sheet, new CellRangeAddress(rowIndex - 1, rowIndex, 2, 2), "CẤU PHẦN", headerStyle);

            // merge ô Ngày làm việc
            mergeAndStyle(sheet, new CellRangeAddress(rowIndex - 1, rowIndex, 3, 3), "NGÀY LÀM VIỆC", headerStyle);

            setWidthColumn(sheet, alias);
            rowIndex++;
        }


        CellStyle sttStyle = styleForSttCell(sheet);
        CellStyle dataStyle = styleForDataCell(sheet);
        int index = 1;
        try {
            for (T item : data) {
                Row row = sheet.createRow(rowIndex++);
                writeValue(column, row, item, index++, sheet, dataStyle, sttStyle);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException("error when export to excel");
        }
        response.setContentType("application/octet-stream");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=" + fileName + ".xlsx";
        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        response.setHeader(headerKey, headerValue);
        workbook.close();
        outputStream.close();
    }

    public static <T> void exportTextStyle(HttpServletResponse response, Class<T> clazz, List<T> data, String fileName,
                                           String paramName, boolean hasAdditionalInfo, boolean hasStyleRow) throws IOException {
        Workbook workbook = getTextStyleWorkbook(clazz, data, paramName, hasAdditionalInfo, hasStyleRow);
        response.setContentType("application/octet-stream");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=" + fileName + ".xlsx";
        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        response.setHeader(headerKey, headerValue);
        workbook.close();
        outputStream.close();
    }

    /**
     * @param excelFile file excel
     * @param index     vị trí cột header, không tính những dòng không có giá trị,
     *                  bắt đầu từ 1
     * @param clazz     lớp đối tượng cần ánh xạ dữ liệu
     * @param <T>
     * @return
     * @throws IOException
     * @throws IllegalArgumentException
     */
    public static <T> List<T> getListImport(MultipartFile excelFile, int index, Class<T> clazz, int... skipRow)
            throws IOException, IllegalArgumentException {
        // kiểm tra xem có phải đuôi file là .xlsx hay không
        if (!checkFile(excelFile)) {
            throw new IllegalArgumentException("The excel file is not in the correct format");
        }
        // Lấy ra các giá trị được đánh dấu với @ExcelColumn
        // List<String> fieldName = new ArrayList<>();
        Field[] fields = clazz.getDeclaredFields();

        // Map chứa tên trường và tên cột excel column
        Map<String, String> fieldToColumnMap = new HashMap<>();
        for (Field field : fields) {
            ExcelColumn annotation = field.getAnnotation(ExcelColumn.class);
            if (annotation != null) {
                field.setAccessible(true);
                fieldToColumnMap.put(annotation.value(), field.getName());
            }
        }
        // Nếu không đánh dấu cột nào thì hủy
        if (fieldToColumnMap.isEmpty()) {
            throw new IllegalArgumentException("The excel file is not in the correct format");
        }
        // List cột bỏ qua
        Set<Integer> skipRows = null;
        if (skipRow != null && skipRow.length > 0) {
            skipRows = Arrays.stream(skipRow).boxed().collect(Collectors.toSet());
        }
        // Danh sách chứa kết quả đối tượng sau cùng
        List<T> result = new ArrayList<>();
        // Map chứa tên trường đối tượng và vị trí đọc giá trị của nó trong excel
        Map<String, Integer> fieldColumnIndices = null;
        XSSFWorkbook workbook = new XSSFWorkbook(excelFile.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> iterator = sheet.iterator();
        int rowColumn = 1;
        while (iterator.hasNext()) {
            Row nextRow = iterator.next();

            // Nếu rowColumn < index thì tăng rowColumn và bỏ qua phần còn lại
            if (rowColumn < index) {
                rowColumn++;
                continue; // Giữ lại một lệnh continue
            }

            // Nếu là cột header thì thêm vào map
            if (rowColumn == index) {
                Map<String, Integer> columnIndices = new HashMap<>();
                Iterator<Cell> cellIterator = nextRow.cellIterator();
                int columnIndex = 0;
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    String cellValue = cell.getStringCellValue();
                    if (cellValue != null) {
                        cellValue = cellValue.trim();
                        cellValue = cellValue.replace("\n", "");
                    }
                    columnIndices.put(cellValue, columnIndex++);
                }
                // Lấy ra map chứa tên trường và vị trí để đọc giá trị của nó trong excel
                fieldColumnIndices = getMapFieldNameIndex(fieldToColumnMap, columnIndices);
                rowColumn++;
            } else {
                // Kiểm tra xem map có bị rỗng hay không
                if (fieldColumnIndices == null || fieldColumnIndices.isEmpty()) {
                    throw new IllegalArgumentException("The excel file is not in the correct format");
                }
                if (isEmptyRow(nextRow)) {
                    rowColumn++;
                    continue;
                }
                // Kiểm
                if (skipRows != null && skipRows.contains(rowColumn)) {
                    rowColumn++;
                    continue;
                }
                Iterator<Cell> cellIterator = nextRow.cellIterator();
                Map<String, Integer> finalColumnIndices = fieldColumnIndices;
                // Map chứa tên cột và Cell tương ứng
                Map<String, Cell> cellMap = new HashMap<>();
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    fieldColumnIndices.keySet().stream()
                            .filter(key -> finalColumnIndices.get(key).equals(cell.getColumnIndex())) // chỉ lấy tên cột
                            // khi cùng chỉ số
                            // với cell
                            .findFirst().ifPresent(columnName -> cellMap.put(columnName, cell));
                }

                // Nếu cellMap không rỗng thì mới xử lý tiếp
                if (!cellMap.isEmpty()) {
                    T value = getObjectGeneric(cellMap, clazz);
                    if (value != null) {
                        result.add(value);
                    }
                }
                rowColumn++;
            }
        }
        workbook.close();
        return result;
    }

    /**
     * @param excelFile Multipath file excel
     * @param handler   Hàm xử lý cach lấy ra đối tượng liệu với string là tên
     *                  trường và Cell là giá trị của ô
     * @param index     vị trí cột header, không tính những dòng không có giá trị,
     *                  bắt đầu từ 1
     * @param <T>
     * @return
     * @throws IOException
     */
    public static <T> List<T> getListImportExcel(MultipartFile excelFile, Function<Map<String, Cell>, T> handler,
                                                 int index, Class<T> clazz) throws IOException, IllegalArgumentException {
        // kiểm tra xem có phải đuôi file là .xlsx hay không
        if (!checkFile(excelFile)) {
            throw new IllegalArgumentException("The excel file is not in the correct format");
        }
        // Lấy ra các giá trị được đánh dấu với @ExcelColumn
        // List<String> fieldName = new ArrayList<>();
        Field[] fields = clazz.getDeclaredFields();

        // Map chứa tên trường và tên cột excel column
        Map<String, String> fieldToColumnMap = new HashMap<>();
        for (Field field : fields) {
            ExcelColumn annotation = field.getAnnotation(ExcelColumn.class);
            if (annotation != null) {
                field.setAccessible(true);
                fieldToColumnMap.put(annotation.value(), field.getName());
            }
        }
        // Nếu không đánh dấu cột nào thì hủy
        if (fieldToColumnMap.isEmpty()) {
            throw new IllegalArgumentException("The excel file is not in the correct format");
        }
        // Danh sách chứa kết quả đối tượng sau cùng
        List<T> result = new ArrayList<>();
        // Map chứa tên cột excel và vị trí của nó
        Map<String, Integer> fieldColumnIndices = null;
        XSSFWorkbook workbook = new XSSFWorkbook(excelFile.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> iterator = sheet.iterator();
        int rowColumn = 1;
        while (iterator.hasNext()) {
            Row nextRow = iterator.next();

            // Nếu rowColumn < index thì tăng rowColumn và bỏ qua phần còn lại
            if (rowColumn < index) {
                rowColumn++;
                continue; // Giữ lại một lệnh continue
            }

            // Nếu là cột header thì thêm vào map
            if (rowColumn == index) {
                Map<String, Integer> columnIndices = new HashMap<>();
                Iterator<Cell> cellIterator = nextRow.cellIterator();
                int columnIndex = 0;
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    columnIndices.put(cell.getStringCellValue(), columnIndex++);
                }

                fieldColumnIndices = getMapFieldNameIndex(fieldToColumnMap, columnIndices);
                rowColumn++;
            } else {
                // Kiểm tra xem map có bị rỗng hay không
                if (fieldColumnIndices == null || fieldColumnIndices.isEmpty()) {
                    throw new IllegalArgumentException("The excel file is not in the correct format");
                }
                if (isEmptyRow(nextRow)) {
                    continue;
                }
                Iterator<Cell> cellIterator = nextRow.cellIterator();
                // Map chứa tên cột và Cell tương ứng
                Map<String, Cell> cellMap = new HashMap<>();
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    Map<String, Integer> finalColumnIndices = fieldColumnIndices;
                    fieldColumnIndices.keySet().stream()
                            .filter(key -> finalColumnIndices.get(key).equals(cell.getColumnIndex())) // chỉ lấy tên cột
                            // khi cùng chỉ số
                            // với cell
                            .findFirst().ifPresent(columnName -> cellMap.put(columnName, cell));
                }

                // Nếu cellMap không rỗng thì mới xử lý tiếp
                if (!cellMap.isEmpty()) {
                    T value = handler.apply(cellMap);
                    if (value != null) {
                        result.add(value);
                    }
                }
            }
        }
        workbook.close();
        return result;
    }

    public static <T> List<T> importExcel(MultipartFile excelFile,
                                          Function<Map<String, Cell>, T> handler,
                                          int index,
                                          Class<T> clazz) throws IOException, IllegalArgumentException {
        return importExcel(excelFile, handler, index, clazz, null);
    }

    /**
     * @param excelFile Multipath file excel
     * @param handler   Hàm xử lý ánh xạ dữ liệu
     * @param index     vị trí cột header, không tính những dòng không có giá trị,
     *                  bắt đầu từ 1
     * @param <T>
     * @return
     * @throws IOException
     */
    public static <T> List<T> importExcel(MultipartFile excelFile, Function<Map<String, Cell>, T> handler, int index,
                                          Class<T> clazz, Integer skipRow) throws IOException, IllegalArgumentException {
        // kiểm tra xem có phải đuôi file là .xlsx hay không
        if (!checkFile(excelFile)) {
            throw new IllegalArgumentException("The excel file is not in the correct format");
        }
        // Lấy ra các giá trị được đánh dấu với @ExcelColumn
        List<String> fieldName = new ArrayList<>();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            ExcelColumn annotation = field.getAnnotation(ExcelColumn.class);
            if (annotation != null) {
                // if (!field.canAccess(clazz)) {
                field.setAccessible(true); // Cân nhắc giữ lại dòng này nếu không còn cách nào khác
                // }
                fieldName.add(annotation.value());

            }
        }
        // Nếu không đánh dấu cột nào thì hủy
        if (fieldName.isEmpty()) {
            throw new IllegalArgumentException("The excel file is not in the correct format");
        }
        // Danh sách chứa kết quả đối tượng sau cùng
        List<T> result = new ArrayList<>();
        // Map chứa tên cột excel và vị trí của nó
        Map<String, Integer> columnIndices = new HashMap<>();
        XSSFWorkbook workbook = new XSSFWorkbook(excelFile.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> iterator = sheet.iterator();
        int rowColumn = 1;
        while (iterator.hasNext()) {
            Row nextRow = iterator.next();

            // Nếu rowColumn < index thì tăng rowColumn và bỏ qua phần còn lại
            if (rowColumn < index) {
                rowColumn++;
                continue; // Giữ lại một lệnh continue
            }

            // Nếu là cột header thì thêm vào map
            if (rowColumn == index) {
                Iterator<Cell> cellIterator = nextRow.cellIterator();
                int columnIndex = 0;
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    columnIndices.put(cell.getStringCellValue(), columnIndex++);
                }
                // Chỉ lấy những cột mà nằm trong danh sách value @ExcelColumn
                columnIndices = columnIndices.entrySet().stream()
                        .filter(entry -> fieldName.contains(entry.getKey()))
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue));
                rowColumn++;
            } else {
                if (skipRow != null && skipRow.equals(rowColumn)) {
                    rowColumn++;
                    continue;
                }
                // Kiểm tra xem map có bị rỗng hay không
                if (columnIndices.isEmpty()) {
                    throw new IllegalArgumentException("The excel file is not in the correct format");
                }

                if (isEmptyRow(nextRow)) {
                    continue;
                }

                Iterator<Cell> cellIterator = nextRow.cellIterator();
                // Map chứa tên cột và Cell tương ứng
                Map<String, Cell> cellMap = new HashMap<>();
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    Map<String, Integer> finalColumnIndices = columnIndices;
                    columnIndices.keySet().stream()
                            .filter(key -> finalColumnIndices.get(key).equals(cell.getColumnIndex()))
                            .findFirst().ifPresent(columnName -> cellMap.put(columnName, cell));
                }

                // Nếu cellMap không rỗng thì mới xử lý tiếp
                if (!cellMap.isEmpty()) {
                    T value = handler.apply(cellMap);
                    if (value != null) {
                        result.add(value);
                    }
                }
            }
        }

        workbook.close();
        return result;
    }

    public static <T> List<T> importExcelOption(MultipartFile excelFile, Function<Map<String, Cell>, T> handler,
                                                int index, Class<T> clazz) throws IOException, IllegalArgumentException {
        // kiểm tra xem có phải đuôi file là .xlsx hay không
        if (!checkFile(excelFile)) {
            throw new IllegalArgumentException("The excel file is not in the correct format");
        }
        // Lấy ra các giá trị được đánh dấu với @ExcelColumn
        List<String> fieldName = new ArrayList<>();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            ExcelColumn annotation = field.getAnnotation(ExcelColumn.class);
            if (annotation != null) {
                // if (!field.canAccess(clazz)) {
                field.setAccessible(true); // Cân nhắc giữ lại dòng này nếu không còn cách nào khác
                // }
                fieldName.add(annotation.value());

            }
        }
        // Nếu không đánh dấu cột nào thì hủy
        if (fieldName.isEmpty()) {
            throw new IllegalArgumentException("The excel file is not in the correct format");
        }
        // Danh sách chứa kết quả đối tượng sau cùng
        List<T> result = new ArrayList<>();
        // Map chứa tên cột excel và vị trí của nó
        Map<String, Integer> columnIndices = new HashMap<>();
        XSSFWorkbook workbook = new XSSFWorkbook(excelFile.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);
        Iterator<Row> iterator = sheet.iterator();
        int rowColumn = 1;
        while (iterator.hasNext()) {
            Row nextRow = iterator.next();

            // Nếu rowColumn < index thì tăng rowColumn và bỏ qua phần còn lại
            if (rowColumn < index) {
                rowColumn++;
                continue; // Giữ lại một lệnh continue
            }
            // Nếu là cột header thì thêm vào map
            if (rowColumn == index) {
                Iterator<Cell> cellIterator = nextRow.cellIterator();
                int columnIndex = 0;
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    columnIndices.put(cell.getStringCellValue(), columnIndex++);
                }
                // Chỉ lấy những cột mà nằm trong danh sách value @ExcelColumn
                columnIndices = columnIndices.entrySet().stream()
                        .filter(entry -> fieldName.contains(entry.getKey()))
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue));
                rowColumn++;

            } else if (rowColumn == index + 1) {
                rowColumn++;
            } else {
                // Kiểm tra xem map có bị rỗng hay không
                if (columnIndices.isEmpty()) {
                    throw new IllegalArgumentException("The excel file is not in the correct format");
                }

                if (isEmptyRow(nextRow)) {
                    continue;
                }

                Iterator<Cell> cellIterator = nextRow.cellIterator();
                // Map chứa tên cột và Cell tương ứng
                Map<String, Cell> cellMap = new HashMap<>();
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    Map<String, Integer> finalColumnIndices = columnIndices;
                    columnIndices.keySet().stream()
                            .filter(key -> finalColumnIndices.get(key).equals(cell.getColumnIndex()))
                            .findFirst().ifPresent(columnName -> cellMap.put(columnName, cell));
                }

                // Nếu cellMap không rỗng thì mới xử lý tiếp
                if (!cellMap.isEmpty()) {
                    T value = handler.apply(cellMap);
                    if (value != null) {
                        result.add(value);
                    }
                }
            }
        }

        workbook.close();
        return result;
    }

    private static boolean checkFile(MultipartFile file) {
        return Objects.requireNonNull(file.getOriginalFilename()).endsWith(".xlsx")
                || Objects.requireNonNull(file.getOriginalFilename()).endsWith(".xlsm");
    }

    public static Object getCellValue(Cell cell) {
        CellType cellType = cell.getCellType();
        Object cellValue = null;
        switch (cellType) {
            case BOOLEAN:
                cellValue = cell.getBooleanCellValue();
                break;
            case FORMULA:
                Workbook workbook = cell.getSheet().getWorkbook();
                FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
                cellValue = evaluator.evaluate(cell).getNumberValue();
                break;
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    cellValue = cell.getDateCellValue();
                } else {
                    cellValue = cell.getNumericCellValue();
                }
                break;
            case STRING:
                cellValue = cell.getStringCellValue();
                break;
            case BLANK:
            case ERROR:
            default:
                break;
        }

        return cellValue;
    }

    /**
     * Author: khainv_llq<br/>
     * Since: 06/12/2025 11:16 PM<br/>
     * Description: Phương thức lấy giá trị hiển thị của ô<br/>
     * Ví dụ<br/>
     * <code>
     * BOOLEAN -> "TRUE"/"FALSE"<br/>
     * <table border="1" cellpadding="4" cellspacing="0">
     *   <thead>
     *     <tr>
     *       <th>Dữ liệu thực tế</th>
     *       <th>Định dạng ô</th>
     *       <th>Giá trị trả về</th>
     *     </tr>
     *   </thead>
     *   <tbody>
     *     <tr>
     *       <td><code>12345.0</code></td>
     * <td>General</td>
     * <td><code>"12345"</code></td>
     * </tr>
     * <tr>
     * <td><code>12345.0</code></td>
     * <td><code>0.00</code></td>
     * <td><code>"12345.00"</code></td>
     * </tr>
     * <tr>
     * <td><code>12/05/2024</code></td>
     * <td><code>dd-MM-yyyy</code></td>
     * <td><code>"12-05-2024"</code></td>
     * </tr>
     * <tr>
     * <td><code>"0123"</code></td>
     * <td>Text</td>
     * <td><code>"0123"</code></td>
     * </tr>
     * </tbody>
     * </table>
     * </code>
     */
    public static String getCellValueFormatted(Cell cell) {
        DataFormatter formatter = new DataFormatter(Locale.US);
        return formatter.formatCellValue(cell);
    }

    public static String getCellValueFormatedForBigNumberValue(Cell cell) {

        DataFormatter formatter = new DataFormatter(Locale.US);

        // Lấy style của cell
        CellStyle style = cell.getCellStyle();

        // Lấy formatIndex (mã định dạng số của cell)
        int formatIndex = style.getDataFormat();

        // Lấy formatString (chuỗi định dạng số của cell)
        String formatString = style.getDataFormatString();

        // Nếu formatString là null, dùng định dạng mặc định
        if (formatString == null) {
            formatString = BuiltinFormats.getBuiltinFormat(formatIndex);
        }

        // Định dạng giá trị của cell
        String formattedValue = formatter.formatRawCellContents(
                BigDecimal.valueOf(cell.getNumericCellValue()).doubleValue(), formatIndex, formatString);

        return formattedValue.replaceAll("[^\\d.]", "");
    }

    public static void exportTemplate(HttpServletResponse response, String fileName, String path) throws IOException {
        response.setContentType("application/octet-stream");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=" + fileName + ".xlsx";
        InputStream fileExcel = new ClassPathResource(path).getInputStream();
        Workbook workbook = WorkbookFactory.create(fileExcel);
        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        response.setHeader(headerKey, headerValue);
        workbook.close();
        outputStream.close();
    }

    public static void writeHeaderData(String[] headerRowValues, SXSSFSheet sheet, int rowIndex) {
        CellStyle cellStyle = styleForHeader(sheet);
        Cell cell;
        Row headerRow;
        headerRow = sheet.createRow(rowIndex);
        cell = headerRow.createCell(1);
        cell.setCellValue("TABLE DATA");
        cell.setCellStyle(cellStyle);

        // Tạo tiêu đề cho cột
        headerRow = sheet.createRow(rowIndex + 2);
        for (int i = 0; i < headerRowValues.length; i++) {
            cell = headerRow.createCell(i);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(headerRowValues[i]);
        }
    }

    public static <T> List<String> getAlias(Class<T> clazz) {
        List<String> alias = new ArrayList<>();
        List<String> column = new ArrayList<>();
        extractFiled(clazz, alias, column);
        return alias;
    }

    /**
     * @param response
     * @param fileName
     * @param path
     * @param lists
     * @param headers      danh sách các cột trong sheet để nhập dữ liệu
     * @param ignoreFields
     */
    public static void exportData(HttpServletResponse response, String fileName, String path,
                                  Map<String, List<String>> lists, List<String> headers, List<String> ignoreFields) {
        SXSSFWorkbook workbook = null;
        ServletOutputStream outputStream = null;
        try {
            String headerKey = "Content-Disposition";
            String headerValue = "attachment; filename=" + fileName + ".xlsx";
            workbook = ExcelUtils.getWorkBook(path);
            outputStream = response.getOutputStream();

            XSSFSheet tempSheet;
            SXSSFSheet sheet;
            SXSSFSheet sheet1 = workbook.getSheetAt(0);

            // Tạo sheet để lưu trữ data
            if (workbook.getNumberOfSheets() > 1) {
                sheet = workbook.getSheetAt(1);
            } else {
                sheet = workbook.createSheet("Data");
            }
            int rowIndex = 0;
            // Lưu tên trường cho cột
            List<String> headersDataSheet = new ArrayList<>();
            headers.forEach(header -> {
                if (lists.containsKey(header)) {
                    headersDataSheet.add(header);
                }
            });
            ExcelUtils.writeHeaderData(headersDataSheet.toArray(new String[0]), sheet, rowIndex);
            rowIndex += 3; // trừ dòng TABLE DATA, dòng trống và header
            // Lưu dữ liệu vào sheet 2
            Optional<Integer> maxOptional = lists.keySet().stream()
                    .map(key -> lists.get(key).size())
                    .max(Integer::compareTo);

            int max = maxOptional.orElse(0); // Trả về 0 nếu không có giá trị max
            for (int i = 0; i < max; i++) {
                Row row = sheet.getRow(i + rowIndex);
                if (row == null) {
                    row = sheet.createRow(i + rowIndex);
                }

                for (Map.Entry<String, List<String>> entry : lists.entrySet()) {
                    String key = entry.getKey();
                    List<String> values = entry.getValue();

                    if (i < values.size()) {
                        row.createCell(headersDataSheet.indexOf(key)).setCellValue(values.get(i));
                    }
                }
            }

            // Tạo workbook để lưu tạm thời
            XSSFWorkbook tempWorkbook = new XSSFWorkbook();
            tempSheet = tempWorkbook.createSheet("Temp");

            // Tạo droplist
            CellRangeAddressList addressList;
            String formula;
            for (String key : headersDataSheet) {
                if (!(!ObjectUtils.isEmpty(ignoreFields) && ignoreFields.contains(key))) {
                    int colInDataSheet = headersDataSheet.indexOf(key);
                    int col = headers.indexOf(key);
                    String colName = getColumnIndex(colInDataSheet);
                    addressList = new CellRangeAddressList(5, 5, col + 1, col + 1);
                    formula = "Data!$" + colName + "$" + (rowIndex + 1) + ":$" + colName + "$"
                            + (rowIndex + lists.get(key).size());
                    // Đẩy dữ liệu vào sheet 1 với ô nhớ cố định
                    ExcelUtils.getDataValidation(addressList, formula, tempSheet, sheet1);
                }
            }

            response.setHeader(headerKey, headerValue);
            response.setContentType("application/octet-stream");

            workbook.write(outputStream);
        } catch (Exception e) {
            log.error(e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            try {
                if (workbook != null) {
                    workbook.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                log.error(e.getMessage());
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
    }

    public static String getColumnIndex(String colName, List<String> headers) {
        int col = headers.indexOf(colName);
        return getColumnIndex(col);
    }

    public static String getColumnIndex(int col) {
        StringBuilder colIdx = new StringBuilder("");
        while (col >= 0) {
            if (col < 26) { // đổi sang ký tự
                colIdx.append((char) ('A' + col));
            }
            col -= 26;
        }
        return colIdx.toString();
    }

    public static void getDataValidation(CellRangeAddressList cellRangeAddressList, String formula, XSSFSheet tempSheet,
                                         SXSSFSheet sheet) {
        // Xử lý lấy dữ liệu từ tempSheet
        DataValidationHelper validationHelper = new XSSFDataValidationHelper(tempSheet);
        DataValidationConstraint dataValidationConstraint = validationHelper.createFormulaListConstraint(formula);

        // Xử lý trả ra dữ liệu và add vào sheet 1
        DataValidation dataValidation = validationHelper.createValidation(dataValidationConstraint,
                cellRangeAddressList);
        dataValidation.setSuppressDropDownArrow(true);
        // Ngăn chặn nhập dữ liệu không hợp lệ
        dataValidation.setErrorStyle(DataValidation.ErrorStyle.STOP);

        dataValidation.createErrorBox("Title", "Message");
        dataValidation.setShowErrorBox(true);

        dataValidation.createPromptBox("List", "Please select a value from the list.");
        dataValidation.setShowPromptBox(true);

        tempSheet.addValidationData(dataValidation);
        for (DataValidation dv : tempSheet.getDataValidations()) {
            sheet.addValidationData(dv);
        }
    }

    // public static SXSSFWorkbook getWorkBook(String fileName, String path) throws
    // IOException {
    // File file = ResourceUtils.getFile("classpath:" + path);
    // FileInputStream fileInputStream = new FileInputStream(file);
    // XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream);
    // return new SXSSFWorkbook(workbook);
    // }

    public static SXSSFWorkbook getWorkBook(String path) throws IOException {
        // Sử dụng ClassPathResource để lấy file từ classpath
        ClassPathResource resource = new ClassPathResource(path);

        // Mở InputStream từ resource
        try (InputStream fileInputStream = resource.getInputStream()) {
            // Tạo XSSFWorkbook từ InputStream
            XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream);

            // Tạo và trả về SXSSFWorkbook từ XSSFWorkbook
            return new SXSSFWorkbook(workbook);
        }
    }

    // public static SXSSFWorkbook getWorkBook(String fileName, String path) throws
    // IOException {
    // try (InputStream inputStream = new ClassPathResource(path).getInputStream())
    // {
    // XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
    // return new SXSSFWorkbook(workbook);
    // }
    // }

    /**
     * Xuất dữ liệu sang Excel với các cột động
     *
     * @param response                   HttpServletResponse để ghi tệp Excel
     * @param data                       Danh sách các đối tượng dữ liệu để xuất
     * @param staticColumnsBeforeDynamic Map của tên trường với tên tiêu đề cho các
     *                                   cột tĩnh trước cột động
     * @param dynamicColumnsMap          Map của khóa cột động với tên tiêu đề
     * @param staticColumnsAfterDynamic  Map của tên trường với tên tiêu đề cho các
     *                                   cột tĩnh sau cột động
     * @param fileName                   Tên của tệp Excel không bao gồm phần mở
     *                                   rộng
     * @param title                      Tiêu đề để hiển thị trong tệp Excel
     * @throws IOException Nếu xảy ra lỗi I/O
     */
    public static <T> void exportWithDynamicColumns(
            HttpServletResponse response,
            List<T> data,
            Map<String, String> staticColumnsBeforeDynamic,
            Map<String, String> dynamicColumnsMap,
            Map<String, String> staticColumnsAfterDynamic,
            String fileName,
            String title) throws IOException {

        SXSSFWorkbook workbook = new SXSSFWorkbook();
        SXSSFSheet sheet = workbook.createSheet("Data");
        // Danh sách tiêu đề cột
        List<String> columnHeaders = new ArrayList<>();

        // Thêm các cột tĩnh trước cột động
        List<String> staticFieldsBeforeDynamic = new ArrayList<>(staticColumnsBeforeDynamic.keySet());
        staticColumnsBeforeDynamic.values().forEach(columnHeaders::add);

        // Thêm các cột động
        List<String> dynamicColumnKeys = new ArrayList<>(dynamicColumnsMap.keySet());
        dynamicColumnsMap.values().forEach(columnHeaders::add);

        // Thêm các cột tĩnh sau cột động
        List<String> staticFieldsAfterDynamic = new ArrayList<>(staticColumnsAfterDynamic.keySet());
        staticColumnsAfterDynamic.values().forEach(columnHeaders::add);

        // Viết tiêu đề
        int rowIndex = writeHeader(sheet, 0, columnHeaders, title);

        // Tùy chỉnh ObjectMapper để serialize enum thành value
        ObjectMapper objectMapper = new ObjectMapper();

        // 5) Styles cho STT và data
        CellStyle sttStyle  = styleForSttCell(sheet);
        styleForDataCell = styleForDataCell(sheet);

        // Đánh số thứ tự
        int seq = 1;

        // Ghi dữ liệu
        for (T item : data) {
            Row row = sheet.createRow(rowIndex++);
            int columnIndex = 0;

            // Chuyển đối tượng thành Map
            Map<String, Object> dataMap = objectMapper.convertValue(item, Map.class);

            // Ghi số thứ tự
            Cell cell = row.createCell(columnIndex++);
            cell.setCellValue(seq++);
            cell.setCellStyle(sttStyle);

            // Ghi các cột tĩnh trước cột động
            for (String fieldName : staticFieldsBeforeDynamic) {
                cell = row.createCell(columnIndex++);
                setRowValue(cell, dataMap.get(fieldName), sheet);
                cell.setCellStyle(styleForDataCell);
            }

            // Ghi các cột động
            for (String key : dynamicColumnKeys) {
                cell = row.createCell(columnIndex++);
                Object value = dataMap.get(key);
                if (value != null) {
                    setRowValue(cell, value, sheet);
                }
                cell.setCellStyle(styleForDataCell);
            }

            // Ghi các cột tĩnh sau cột động
            for (String fieldName : staticFieldsAfterDynamic) {
                cell = row.createCell(columnIndex++);
                setRowValue(cell, dataMap.get(fieldName), sheet);
                cell.setCellStyle(styleForDataCell);
            }
        }

        // Điều chỉnh độ rộng cột STT
        sheet.setColumnWidth(0, 2000);

        // Điều chỉnh độ rộng cột
        for (int i = 1; i < columnHeaders.size() + 1; i++) {
            String header = columnHeaders.get(i - 1);
            int width = header.length() * 400;
            sheet.setColumnWidth(i, Math.max(width, 3000));
        }

        // Ghi và đóng workbook
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName + ".xlsx");
        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
    }

    public static boolean isEmptyRow(Row row) {
        if (row == null) {
            return true;
        }

        for (int i = row.getFirstCellNum(); i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = ExcelUtils.getCellValueFormatted(cell);
                if (value != null && !String.valueOf(value).trim().isEmpty()) {
                    return false; // Có ít nhất một ô có dữ liệu
                }
            }
        }

        return true; // Tất cả các ô đều trống
    }

    /**
     * ánh xạ dữ liệu từ một map chứa tên cột và ExcelColumn, và map chứa tên cột và
     * Cell tương ứng
     *
     * @param fieldToColumnMap map chứa giá trị với key là value của ExcelColumn và
     *                         value là tên trường
     * @param mapIndex         map chứa giá trị với key là tên cột excel và value
     *                         index của cột trong excel
     * @return
     */
    private static Map<String, Integer> getMapFieldNameIndex(Map<String, String> fieldToColumnMap,
                                                             Map<String, Integer> mapIndex) {
        Map<String, Integer> fieldColumnIndices = new HashMap<>();
        // Lặp qua từng tên cột excel
        mapIndex.keySet().forEach(columnName -> {
            // lấy ra tên @ExcelColumn tương ứng với tên cột excel
            fieldToColumnMap.keySet().stream().filter(item -> item != null && item.trim().equals(columnName))
                    .findFirst().ifPresent(fieldName -> {
                        Integer index = mapIndex.get(columnName);
                        fieldColumnIndices.put(fieldToColumnMap.get(fieldName), index);
                    });
        });
        return fieldColumnIndices;
    }

//    private static <T> T getObjectGeneric(Map<String, Cell> cellMap, Class<T> clazz) {
//        try {
//            T instance = clazz.getDeclaredConstructor().newInstance();
//            for (Map.Entry<String, Cell> entry : cellMap.entrySet()) {
//                String fieldName = entry.getKey();
//                Cell cell = entry.getValue();
//                Field field = clazz.getDeclaredField(fieldName);
//                field.setAccessible(true);
//                Object value = getCellValueFormatted(cell);
    ////                if (value instanceof String) {
    ////                    if (((String) value).endsWith(".0")) {
    ////                        value = ((String) value).substring(0, ((String) value).length() - 2);
    ////                    }
    ////                }
//                field.set(instance, value);
//            }
//            return instance;
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
    private static <T> T getObjectGeneric(Map<String, Cell> cellMap, Class<T> clazz) {
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();
            for (Map.Entry<String, Cell> entry : cellMap.entrySet()) {
                String fieldName = entry.getKey();
                Cell cell = entry.getValue();
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);

                // Lấy giá trị từ cell và đảm bảo nó là String.
                DataFormatter dataFormatter = new DataFormatter();
                String stringValue = dataFormatter.formatCellValue(cell).trim();

                if (stringValue.isEmpty()) {
                    continue; // Bỏ qua nếu ô trống
                }

                Class<?> fieldType = field.getType();
                Object finalValue = null;

                // Kiểm tra xem trường có phải là kiểu Enum không
                if (fieldType.isEnum()) {
                    // Nếu đúng, gọi phương thức chuyển đổi Enum của chúng ta
                    finalValue = findEnumConstantByValue(fieldType, stringValue);
                } else {
                    // Nếu không phải Enum, giữ nguyên là String như cũ
                    finalValue = stringValue;
                }

                // Gán giá trị cuối cùng vào trường của đối tượng
                if (finalValue != null) {
                    field.set(instance, finalValue);
                }
            }
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tạo đối tượng từ file Excel: " + e.getMessage(), e);
        }
    }
    /**
     * Tìm một hằng số Enum dựa trên giá trị chuỗi.
     * Nó sẽ thử các phương thức phổ biến (fromKey, fromValue) trước,
     * sau đó mới so sánh với tên của hằng số.
     *
     * @param enumType Kiểu của Enum (ví dụ: DirectionEnum.class)
     * @param value Giá trị chuỗi cần tìm (ví dụ: "1")
     * @return Hằng số Enum tìm được, hoặc null nếu không tìm thấy.
     */
    private static Object findEnumConstantByValue(Class<?> enumType, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        // Tìm phương thức với tham số là Integer.class
        try {
            Method fromKeyMethod = enumType.getMethod("fromKey", Integer.class);

            // Chuyển đổi chuỗi thành Integer trước khi gọi
            Integer intValue = Integer.parseInt(value.trim());

            // Gọi phương thức tĩnh fromKey(Integer)
            return fromKeyMethod.invoke(null, intValue);

        } catch (NumberFormatException e) {
            return null; // Trả về null nếu giá trị từ Excel không phải là số
        } catch (NoSuchMethodException e) {
            // Nếu không có phương thức fromKey(Integer), thì thử so sánh bằng tên
            for (Object enumConstant : enumType.getEnumConstants()) {
                if (enumConstant.toString().equalsIgnoreCase(value.trim())) {
                    return enumConstant;
                }
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }
}
