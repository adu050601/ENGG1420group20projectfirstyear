package com.example.universitymanagementproject;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Excel数据管理器
 * 负责处理Excel文件的读写操作，包括事件数据的导入和导出
 */
public class ExcelDataManager {
    private static final String FILE_PATH = "UMS_Data.xlsx";
    private static final int EVENT_SHEET_INDEX = 4;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    //从Excel读取事件数据
     // @return 事件列表
    //  @throws IOException 如果文件操作失败

    public List<Event> readEvents() throws IOException {
        List<Event> events = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(FILE_PATH);
             Workbook workbook = new XSSFWorkbook(fis)) {
            System.out.println("Reading events from Excel file...");

            // 获取事件工作表
            Sheet sheet = workbook.getSheetAt(EVENT_SHEET_INDEX);
            if (sheet == null) {
                System.out.println("cannot find the table");
                return events;
            }

            // 跳过标题行(第一行)，从第二行开始读取数据
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                // 读取事件代码，如为空则跳过该行
                String eventCode = getCellValueAsString(row.getCell(0));
                if (eventCode.isEmpty()) continue;

                // 读取其他事件字段
                String eventName = getCellValueAsString(row.getCell(1));
                String description = getCellValueAsString(row.getCell(2));
                String location = getCellValueAsString(row.getCell(3));

                // 日期时间处理
                LocalDateTime dateTime = getCellValueAsLocalDateTime(row.getCell(4));
                if (dateTime == null) {
                    dateTime = LocalDateTime.now();
                }

                // 容量和费用处理
                String capacityStr = getCellValueAsString(row.getCell(5)).strip();
                int capacity = 0;
                try {
                    capacity = (int) Double.parseDouble(capacityStr);
                } catch (NumberFormatException e) {
                    System.out.println("the capacity is wrong: " + capacityStr);
                }

                String costStr = getCellValueAsString(row.getCell(6));
                double cost = 0.0;
                if (costStr.contains("Free") || costStr.equals("0.0") || costStr.isEmpty()) {
                    cost = 0.0;
                } else {
                    try {
                        // 尝试解析带有货币符号的金额，如"$10.0"或"￥10.0"
                        if (costStr.contains("$")) {
                            String[] parts = costStr.split("\\$");
                            if (parts.length > 1) {
                                costStr = parts[1].replaceAll("[^\\d.]", "");
                            }
                        } else {
                            costStr = costStr.replaceAll("[^\\d.]", ""); // 只保留数字和小数点
                        }
                        cost = Double.parseDouble(costStr);
                    } catch (NumberFormatException e) {
                        System.out.println("the cost is wrong: " + costStr);
                    }
                }

                // 头图路径，如为空则使用默认图片
                String headerImagePath = getCellValueAsString(row.getCell(7));
                if (headerImagePath.isEmpty()) {
                    headerImagePath = "default_header.png";
                    //the path need to be changed//
                }

                // 处理注册学生
                String registeredStudents = getCellValueAsString(row.getCell(8));
                List<String> registeredStudentsList = new ArrayList<>();
                if (!registeredStudents.isEmpty()) {
                    String[] studentIds = registeredStudents.split(",");
                    for (String id : studentIds) {
                        String trimmedId = id.trim();
                        if (!trimmedId.isEmpty()) {
                            registeredStudentsList.add(trimmedId);
                        }
                    }
                }

                // 创建事件对象
                Event event = new Event(eventName, eventCode, description, headerImagePath, location, dateTime, capacity, cost);
                event.setRegisteredStudents(registeredStudentsList);
                events.add(event);
            }
        }

        System.out.println("read successfully " + events.size() + " events！");
        return events;
    }

    /**
     * 将事件数据写入Excel
     * @param events 要写入的事件列表
     * @throws IOException 如果文件操作失败
     */
    public void writeEvents(List<Event> events) throws IOException {
        // 首先读取现有工作簿，以保留其他工作表
        Workbook workbook;
        try (FileInputStream fis = new FileInputStream(FILE_PATH)) {
            workbook = new XSSFWorkbook(fis);
        } catch (IOException e) {
            // 如果文件不存在，创建新工作簿
            workbook = new XSSFWorkbook();
            System.out.println("create a new file as the file you want to use is invalid");
        }

        // 检查事件工作表是否存在，如果存在则删除
        if (workbook.getNumberOfSheets() > EVENT_SHEET_INDEX) {
            Sheet existingSheet = workbook.getSheetAt(EVENT_SHEET_INDEX);
            if (existingSheet != null) {
                workbook.removeSheetAt(EVENT_SHEET_INDEX);
                System.out.println("removed");
            }
        }

        // 确保有足够的工作表
        while (workbook.getNumberOfSheets() < EVENT_SHEET_INDEX) {
            workbook.createSheet("Sheet" + workbook.getNumberOfSheets());
        }

        // 创建新的事件工作表
        Sheet sheet = workbook.createSheet("Events");
        if (EVENT_SHEET_INDEX < workbook.getNumberOfSheets()) {
            workbook.setSheetOrder("Events", EVENT_SHEET_INDEX);
        }

        // 创建标题行
        Row headerRow = sheet.createRow(0);
        String[] headers = {
                "EventCode", "EventName", "Description", "Location", "DateTime",
                "Capacity", "Cost", "HeaderImagePath", "RegisteredStudents"
        };

        // 设置列宽
        sheet.setColumnWidth(0, 15 * 256); // 事件代码
        sheet.setColumnWidth(1, 20 * 256); // 事件名称
        sheet.setColumnWidth(2, 40 * 256); // 描述
        sheet.setColumnWidth(3, 15 * 256); // 地点
        sheet.setColumnWidth(4, 20 * 256); // 日期时间
        sheet.setColumnWidth(5, 10 * 256); // 容量
        sheet.setColumnWidth(6, 10 * 256); // 费用
        sheet.setColumnWidth(7, 30 * 256); // 头图路径
        sheet.setColumnWidth(8, 50 * 256); // 注册学生

        // 创建标题单元格样式
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        // 写入标题
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // 创建数据单元格样式
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);

        // 创建日期时间单元格样式
        CellStyle dateTimeStyle = workbook.createCellStyle();
        dateTimeStyle.cloneStyleFrom(dataStyle);
        CreationHelper creationHelper = workbook.getCreationHelper();
        dateTimeStyle.setDataFormat(creationHelper.createDataFormat().getFormat("yyyy-mm-dd h:mm"));

        // 写入事件数据
        int rowNum = 1;
        for (Event event : events) {
            Row row = sheet.createRow(rowNum++);

            // 事件代码
            Cell codeCell = row.createCell(0);
            codeCell.setCellValue(event.getEventCode());
            codeCell.setCellStyle(dataStyle);

            // 事件名称
            Cell nameCell = row.createCell(1);
            nameCell.setCellValue(event.getEventName());
            nameCell.setCellStyle(dataStyle);

            // 描述
            Cell descCell = row.createCell(2);
            descCell.setCellValue(event.getDescription());
            descCell.setCellStyle(dataStyle);

            // 地点
            Cell locCell = row.createCell(3);
            locCell.setCellValue(event.getLocation());
            locCell.setCellStyle(dataStyle);

            // 日期时间
            Cell dateTimeCell = row.createCell(4);
            dateTimeCell.setCellValue(event.getDateTime());
            dateTimeCell.setCellStyle(dateTimeStyle);

            // 容量
            Cell capCell = row.createCell(5);
            capCell.setCellValue(event.getCapacity());
            capCell.setCellStyle(dataStyle);

            // 费用
            Cell costCell = row.createCell(6);
            if (event.getCost() == 0) {
                costCell.setCellValue("Free");
            } else {
                costCell.setCellValue("$" + event.getCost());
            }
            costCell.setCellStyle(dataStyle);

            // 头图路径
            Cell imgCell = row.createCell(7);
            imgCell.setCellValue(event.getHeaderImagePath());
            imgCell.setCellStyle(dataStyle);

            // 注册学生
            Cell studentsCell = row.createCell(8);
            List<String> students = event.getRegisteredStudents();
            if (students != null && !students.isEmpty()) {
                // 过滤掉空项并用逗号连接
                StringBuilder sb = new StringBuilder();
                for (String student : students) {
                    if (student != null && !student.trim().isEmpty()) {
                        if (sb.length() > 0) {
                            sb.append(", ");
                        }
                        sb.append(student.trim());
                    }
                }
                studentsCell.setCellValue(sb.toString());
            } else {
                studentsCell.setCellValue("");
            }
            studentsCell.setCellStyle(dataStyle);
        }

        // 将工作簿写入文件
        try (FileOutputStream fos = new FileOutputStream(FILE_PATH)) {
            workbook.write(fos);
        }

        // 关闭工作簿
        workbook.close();

        System.out.println("successfully " + events.size() + " saved events");
    }

    /**
     * 获取单元格的字符串值
     * @param cell Excel单元格
     * @return 单元格的字符串值，如果单元格为空则返回空字符串
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().format(DATE_TIME_FORMATTER);
                } else {
                    // 避免显示科学计数法
                    double value = cell.getNumericCellValue();
                    // 如果是整数，返回整数形式
                    if (value == Math.floor(value)) {
                        return String.valueOf((long) value);
                    }
                    return String.valueOf(value);
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    try {
                        return String.valueOf(cell.getNumericCellValue());
                    } catch (Exception e2) {
                        return "";
                    }
                }
            default:
                return "";
        }
    }

    /**
     * 获取单元格的LocalDateTime值
     * @param cell Excel单元格
     * @return 单元格的LocalDateTime值，如果单元格为空或不是日期格式则返回null
     */
    private LocalDateTime getCellValueAsLocalDateTime(Cell cell) {
        if (cell == null) {
            return null;
        }

        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue();
            } else if (cell.getCellType() == CellType.STRING) {
                // 尝试从字符串解析日期时间
                String dateTimeStr = cell.getStringCellValue();
                try {
                    return LocalDateTime.parse(dateTimeStr, DATE_TIME_FORMATTER);
                } catch (Exception e) {
                    System.out.println("invalid string: " + dateTimeStr);
                }
            }
        } catch (Exception e) {
            System.out.println("cannot get the date: " + e.getMessage());
        }

        return null;
    }
}