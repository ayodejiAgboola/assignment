package com.home.assignment.model;

public class Invoice {
    private String employeeID;
    private double hours;
    private double rate;
    private double totalCost;

    public Invoice(String employeeID, double hours, double rate, double totalCost){
        this.employeeID=employeeID;
        this.hours=hours;
        this.rate=rate;
        this.totalCost=totalCost;
    }

    public String getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(String employeeID) {
        this.employeeID = employeeID;
    }

    public double getHours() {
        return hours;
    }

    public void setHours(double hours) {
        this.hours = hours;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }
}
