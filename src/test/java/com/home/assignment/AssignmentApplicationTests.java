package com.home.assignment;

import com.home.assignment.model.InvalidInputException;
import com.home.assignment.model.Invoice;
import com.home.assignment.service.InvoiceService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import sun.print.resources.serviceui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AssignmentApplicationTests {
@Autowired
	InvoiceService service;
	@Test
	public void InvalidInputExceptionWhenWrongTimeIsPassed() {
		File file = new File("test - Error.csv");
			assertThatExceptionOfType(InvalidInputException.class).isThrownBy(()->{service.getCompanyInvoices(file);});

	}

	@Test
	public void checkNumberOfCompanies(){
		File file = new File("test.csv");
		try {
			assertThat(service.getCompanyInvoices(file).size()).isEqualTo(2);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (InvalidInputException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void checkFacebookHas2EmployeesInInvoiceAndGoogleHas1(){
		File file = new File("test.csv");
		try {
			Map<String, Map> companyValues = service.getCompanyInvoices(file);
			Map<String, Invoice> facebookStaffValues = companyValues.get("Facebook");
			Map<String, Invoice> googleStaffValues = companyValues.get("Google");
			assertThat(facebookStaffValues.size()).isEqualTo(2);
			assertThat(googleStaffValues.size()).isEqualTo(1);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (InvalidInputException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void checkSumOfEmployee1HoursAndAmountForGoogle(){
		File file = new File("test.csv");
		try {
			Map<String, Map> companyValues = service.getCompanyInvoices(file);
			Map<String, Invoice> googleStaffValues = companyValues.get("Google");
			assertThat(googleStaffValues.get("1").getHours()).isEqualTo(15);
			assertThat(googleStaffValues.get("1").getTotalCost()).isEqualTo(450000);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (InvalidInputException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void checkThatFacebookReportIsCreated(){
		File file = new File("test.csv");
		try {
			Map<String, Map> companyValues = service.getCompanyInvoices(file);
			ArrayList<String> companies = new ArrayList<>(companyValues.keySet());
			service.generateReport(companies,companyValues);
			File report = new File("docs/Facebook.xslx");
			assertThat(report.exists());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (InvalidInputException e) {
			e.printStackTrace();
		}
	}

}
