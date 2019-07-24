package com.home.assignment.controller;

import com.home.assignment.model.InvalidInputException;
import com.home.assignment.service.InvoiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Map;

@Controller
public class ApiController {
    @Autowired
    InvoiceService invoiceService;

    @GetMapping("/")
    public String index() {
        return "upload";
    }
    @GetMapping("/upload")
    public String getUpload() {
        return "upload";
    }
    @PostMapping("/upload")
    public String csvUpload(@RequestParam("file") MultipartFile file, Model model) {
        boolean isReportGenerated = false;
        if (file.isEmpty()) {
            model.addAttribute("statusMessage", "Please select a file to upload!");
            return "upload";
        }
        File csv = new File(System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename());
        try {
            file.transferTo(csv);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (csv.getName().endsWith(".csv")) {
            try {
                Map<String, Map> companyInvoices = invoiceService.getCompanyInvoices(csv);
                ArrayList<String> companies = new ArrayList<String>(companyInvoices.keySet());
                isReportGenerated = invoiceService.generateReport(companies,companyInvoices);
            } catch (IOException e) {
                e.printStackTrace();
                model.addAttribute("statusMessage", "File could not be processed: '" + file.getOriginalFilename() + "'");
                return "upload";
            } catch (ParseException e) {
                e.printStackTrace();
                model.addAttribute("statusMessage", "File could not be processed: '" + file.getOriginalFilename() + "'");
                return "upload";
            }catch (InvalidInputException e){
                e.printStackTrace();
                model.addAttribute("statusMessage",e.getMessage());
                return "upload";
            }
        } else {
            model.addAttribute("statusMessage", "Invalid file type for '" + file.getOriginalFilename() + "'");
            return "upload";
        }
        if(isReportGenerated){
            model.addAttribute("statusMessage","Uploaded Successfully");
        }else {
            model.addAttribute("statusMessage","Something went wrong, please check with the administrator");
        }

        return "upload";
    }

}