package com.home.assignment.service;

import com.home.assignment.model.InvalidInputException;
import com.home.assignment.model.Invoice;
import com.home.assignment.model.TimeTracker;
import com.opencsv.CSVReader;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class InvoiceService {
    private static String[] columns = { "Employee ID", "Number of Hours", "Unit Price", "Cost" };
    private static double MILLI_TO_HOUR = 3600000;
    public Map getCompanyInvoices(File csv) throws IOException, ParseException, IllegalArgumentException, InvalidInputException {
        CSVReader reader = new CSVReader(new FileReader(csv));
        Map<String, Map> companyInvoices = new HashMap<>();
        Map<String, Map> employeeTimes = new HashMap<>();
        ArrayList<String> companies = new ArrayList<>();
        String[] csvLine;
        while ((csvLine = reader.readNext()) != null) {
            if (!csvLine[0].endsWith("Employee ID")) {
                Map<String, Invoice> staffValues = new HashMap<>();
                Map<Date, ArrayList<TimeTracker>> timeTrackerMap = new HashMap<>();
                Invoice invoice;
                ArrayList<TimeTracker> tracker;
                String employeeId = csvLine[0];
                double rateInKobo = Double.parseDouble(csvLine[1]) * 100;
                String company = csvLine[2];
                String dateAsString = csvLine[3];
                String startTimeAsString = csvLine[4];
                String endTimeAsString = csvLine[5];

                SimpleDateFormat formatForTime = new SimpleDateFormat("HH:mm");
                SimpleDateFormat formatForDate = new SimpleDateFormat("yyyy-mm-dd");
                Date startTime = formatForTime.parse(startTimeAsString);
                Date endTime = formatForTime.parse(endTimeAsString);
                Date dateOfWork = formatForDate.parse(dateAsString);
                if (startTime.after(endTime)) {
                    throw new IllegalArgumentException("End Time must be after start time");
                }
                double hours = (endTime.getTime() - startTime.getTime()) / MILLI_TO_HOUR;

                double lineCostInKobo = rateInKobo * hours;

                if(!companyInvoices.containsKey(company)){
                    companies.add(company);
                    invoice = new Invoice(employeeId, hours, rateInKobo, lineCostInKobo);
                    staffValues.put(employeeId, invoice);
                    companyInvoices.put(company, staffValues);
                    tracker = new ArrayList<>();
                    TimeTracker lineTracker = new TimeTracker(startTime,endTime);
                    tracker.add(lineTracker);
                    timeTrackerMap.put(dateOfWork,tracker);
                    employeeTimes.put(employeeId,timeTrackerMap);
                    continue;
                }
                if(companyInvoices.containsKey(company)){
                    staffValues = companyInvoices.get(company);
                    if(!staffValues.containsKey(employeeId)){
                        invoice = new Invoice(employeeId,hours,rateInKobo,lineCostInKobo);
                        staffValues.put(employeeId,invoice);
                        TimeTracker lineTracker = new TimeTracker(startTime, endTime);
                        tracker = new ArrayList<>();
                        tracker.add(lineTracker);
                        timeTrackerMap.put(dateOfWork,tracker);
                        employeeTimes.put(employeeId,timeTrackerMap);
                        companyInvoices.replace(company,staffValues);
                        continue;
                    }
                    if(staffValues.containsKey(employeeId)){
                        timeTrackerMap=employeeTimes.get(employeeId);
                        if(timeTrackerMap.containsKey(dateOfWork)){
                            tracker=timeTrackerMap.get(dateOfWork);
                            for(int i=0;i<tracker.size();i++){
                                if (!endTime.before(tracker.get(i).getStartTime()) || !startTime.after(tracker.get(i).getEndTime())) {
                                    throw new InvalidInputException("You've logged some or all of these hours for employee: " + employeeId + " for " + dateAsString);
                                }
                            }

                        }else {
                            tracker=new ArrayList<>();
                        }
                        invoice=staffValues.get(employeeId);
                        invoice.setHours(invoice.getHours()+hours);
                        invoice.setTotalCost(invoice.getTotalCost()+lineCostInKobo);
                        staffValues.replace(employeeId,invoice);
                        tracker.add(new TimeTracker(startTime,endTime));
                        timeTrackerMap.replace(dateOfWork,tracker);
                        employeeTimes.replace(employeeId,timeTrackerMap);
                        companyInvoices.replace(company,staffValues);
                        continue;
                    }
                }
            }
        }
        return companyInvoices;
    }

    public boolean generateReport(ArrayList<String> companies, Map<String, Map> companyInvoices) {
        boolean isComplete = false;
        for(int i=0;i<companies.size();i++){
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet(companies.get(i));
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 14);
            headerFont.setColor(IndexedColors.WHITE.getIndex());

            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(headerFont);
            headerCellStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
            headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Row companyRow = sheet.createRow(0);
            Cell nameCell = companyRow.createCell(0);
            nameCell.setCellValue(companies.get(i));
            Row headerRow = sheet.createRow(1);

            for (int j = 0; j < columns.length; j++) {
                Cell cell = headerRow.createCell(j);
                cell.setCellValue(columns[j]);
                cell.setCellStyle(headerCellStyle);
            }
            int rowNum = 2;
            double totalSum=0;
            ArrayList<Invoice> invoiceArrayList = new ArrayList<Invoice>(companyInvoices.get(companies.get(i)).values());
            for (Invoice invoice : invoiceArrayList) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(invoice.getEmployeeID());
                row.createCell(1).setCellValue(invoice.getHours());
                row.createCell(2).setCellValue(invoice.getRate()/100);
                row.createCell(3).setCellValue(invoice.getTotalCost()/100);
                totalSum+=invoice.getTotalCost();
            }
            Row totalRow = sheet.createRow(rowNum++);
            totalRow.createCell(2).setCellValue("Total");
            totalRow.createCell(3).setCellValue(totalSum/100);

            for (int k = 0; k < columns.length; k++) {
                sheet.autoSizeColumn(k);
            }
            File excel = new File(""+companies.get(i)+".xlsx");
            FileOutputStream fileOut = null;
            try {
                fileOut = new FileOutputStream(excel);
                workbook.write(fileOut);
                fileOut.close();
            } catch (IOException e) {
                e.printStackTrace();
                isComplete=false;
                break;
            }
            isComplete=true;
        }
        return isComplete;
    }
}
