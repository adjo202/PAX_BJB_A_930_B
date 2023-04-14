package com.pax.pay.trans.model;

import com.pax.gl.db.IDb.AEntityBase;
import com.pax.gl.db.IDb.Column;
import com.pax.pay.app.FinancialApplication;
import com.pax.settings.currency.Currency;

import java.io.Serializable;
import java.util.Locale;

/**
 *
 * @author sandy@indopay.com
 * 
 */
public class BPJSTkData extends AEntityBase implements Serializable {

    public static final String TAG = "BPJSTkData";

    private static final long serialVersionUID = 1L;

    @Column
    private String nik;

    @Column
    private String customerName;

    @Column
    private String address;

    @Column
    private String email;

    @Column
    private String birthDate;

    @Column
    private String hp;

    @Column
    private String jobType;

    @Column
    private String jobTypeCode;

    @Column
    private String jobLocation;

    @Column
    private String BPJSLocation;

    @Column
    private String startTime;

    @Column
    private String endTime;

    @Column
    private String salary;

    @Column
    private String monthProgram;

    @Column
    private String monthProgramCode;

    @Column
    private String formatProgram;

    @Column
    private String formatProgramCode;

    @Column
    private String jobLocationCode;

    @Column
    private String BPJSLocationCode;

    @Column
    private String jobType2;

    @Column
    private String jobTypeCode2;


    public BPJSTkData(String nik,
                      String customerName,
                      String birthDate,
                      String hp) {
        this.nik                = nik;
        this.customerName       = customerName;
        this.birthDate          = birthDate;
        this.hp                 = hp;

    }



    public BPJSTkData(String nik,
                      String customerName,
                      String email,
                      String address,
                      String birthDate,
                      String hp,
                      String salary,
                      String monthProgram,
                      String monthProgramCode,
                      String formatProgram,
                      String formatProgramCode,
                      String jobType,
                      String jobTypeCode,
                      String jobLocation,
                      String jobLocationCode,
                      String BPJSLocation,
                      String BPJSLocationCode,
                      String startTime,
                      String endTime,
                      String jobType2,
                      String jobTypeCode2) {
        this.nik                = nik;
        this.customerName       = customerName;
        this.email              = email;
        this.address            = address;
        this.birthDate          = birthDate;
        this.hp                 = hp;
        this.salary             = salary;
        this.monthProgram       = monthProgram;
        this.monthProgramCode   = monthProgramCode;
        this.formatProgram      = formatProgram;
        this.formatProgramCode  = formatProgramCode;
        this.BPJSLocation       = BPJSLocation;
        this.jobLocation        = jobLocation;
        this.jobType            = jobType;
        this.jobTypeCode        = jobTypeCode;
        this.BPJSLocationCode   = BPJSLocationCode;
        this.jobLocationCode    = jobLocationCode;
        this.startTime          = startTime;
        this.endTime            = endTime;
        this.jobType2           = jobType2;
        this.jobTypeCode2       = jobTypeCode2;


    }

    public BPJSTkData(String nik,String monthProgram, String monthProgramCode){
            this.nik = nik;
            this.monthProgram = monthProgram;
            this.monthProgramCode = monthProgramCode;
    }





    public String getNik() {
        return nik;
    }

    public void setNik(String nik) {
        this.nik = nik;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getJobType() {
        return jobType;
    }

    public void setJobType(String jobType) {
        this.jobType = jobType;
    }


    public String getJobLocation() {
        return jobLocation;
    }

    public void setJobLocation(String jobLocation) {
        this.jobLocation = jobLocation;
    }

    public String getBPJSLocation() {
        return BPJSLocation;
    }

    public void setBPJSLocation(String BPJSLocation) {
        this.BPJSLocation = BPJSLocation;
    }

    public String getAddress() {
        String theAddress = address == null ? "" : address.trim();
        return theAddress;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getHp() {
        return hp;
    }

    public void setHp(String hp) {
        this.hp = hp;
    }

    public String getJobTypeCode() {
        return jobTypeCode;
    }

    public void setJobTypeCode(String jobTypeCode) {
        this.jobTypeCode = jobTypeCode;
    }

    public String getJobLocationCode() {
        return jobLocationCode;
    }

    public void setJobLocationCode(String jobLocationCode) {
        this.jobLocationCode = jobLocationCode;
    }

    public String getBPJSLocationCode() {
        return BPJSLocationCode;
    }

    public void setBPJSLocationCode(String BPJSLocationCode) {
        this.BPJSLocationCode = BPJSLocationCode;
    }

    public String getFormattedSalary() {
        Currency currency = FinancialApplication.getSysParam().getCurrency();
        String amount = currency.getName() + " " + FinancialApplication.getConvert()
                .amountMinUnitToMajor(salary, currency.getCurrencyExponent(), true);
        return amount;
    }


    public String getSalary() {
        return salary;
    }

    public void setSalary(String salary) {
        this.salary = salary;
    }

    public String getMonthProgram() {
        return monthProgram;
    }

    public void setMonthProgram(String monthProgram) {
        this.monthProgram = monthProgram;
    }

    public String getMonthProgramCode() {
        return monthProgramCode;
    }

    public void setMonthProgramCode(String monthProgramCode) {
        this.monthProgramCode = monthProgramCode;
    }

    public String getFormatProgram() {
        return formatProgram;
    }

    public void setFormatProgram(String formatProgram) {
        this.formatProgram = formatProgram;
    }

    public String getFormatProgramCode() {
        return formatProgramCode;
    }

    public void setFormatProgramCode(String formatProgramCode) {
        this.formatProgramCode = formatProgramCode;
    }

    public String getJobType2() {
        return jobType2;
    }

    public void setJobType2(String jobType2) {
        this.jobType2 = jobType2;
    }

    public String getJobTypeCode2() {
        return jobTypeCode2;
    }

    public void setJobTypeCode2(String jobTypeCode2) {
        this.jobTypeCode2 = jobTypeCode2;
    }

    @Override
    public String toString() {
        return "-";
    }
}
