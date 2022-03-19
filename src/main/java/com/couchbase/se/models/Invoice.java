package com.couchbase.se.models;

public class Invoice {
    private String invoice_date;
    private Integer invoice_number;
    private Integer customer_ID;
    private String due_date;
    private String customerName;
    private String customerCompany;
    private String customerStreet;
    private String customerCityStateZip;
    private String customerPhone;
    private String PDFasText;
    private String PDFasPDF;

    public Invoice(String invoice_date, Integer invoice_number, Integer customer_ID, String due_date, String customerName, String customerCompany, String customerStreet, String customerCityStateZip, String customerPhone, String PDFasText, String PDFasPDF) {
        this.invoice_date = invoice_date;
        this.invoice_number = invoice_number;
        this.customer_ID = customer_ID;
        this.due_date = due_date;
        this.customerName = customerName;
        this.customerCompany = customerCompany;
        this.customerStreet = customerStreet;
        this.customerCityStateZip = customerCityStateZip;
        this.customerPhone = customerPhone;
        this.PDFasText = PDFasText;
        this.PDFasPDF = PDFasPDF;
    }

    public String getInvoice_date() {
        return invoice_date;
    }

    public void setInvoice_date(String invoice_date) {
        this.invoice_date = invoice_date;
    }

    public Integer getInvoice_number() {
        return invoice_number;
    }

    public void setInvoice_number(Integer invoice_number) {
        this.invoice_number = invoice_number;
    }

    public Integer getCustomer_ID() {
        return customer_ID;
    }

    public void setCustomer_ID(Integer customer_ID) {
        this.customer_ID = customer_ID;
    }

    public String getDue_date() {
        return due_date;
    }

    public void setDue_date(String due_date) {
        this.due_date = due_date;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerCompany() {
        return customerCompany;
    }

    public void setCustomerCompany(String customerCompany) {
        this.customerCompany = customerCompany;
    }

    public String getCustomerStreet() {
        return customerStreet;
    }

    public void setCustomerStreet(String customerStreet) {
        this.customerStreet = customerStreet;
    }

    public String getCustomerCityStateZip() {
        return customerCityStateZip;
    }

    public void setCustomerCityStateZip(String customerCityStateZip) {
        this.customerCityStateZip = customerCityStateZip;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getPDFasText() {
        return PDFasText;
    }

    public void setPDFasText(String PDFasText) {
        this.PDFasText = PDFasText;
    }

    public String getPDFasPDF() {
        return PDFasPDF;
    }

    public void setPDFasPDF(String PDFasPDF) {
        this.PDFasPDF = PDFasPDF;
    }
}
