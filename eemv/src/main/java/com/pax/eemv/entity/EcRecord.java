package com.pax.eemv.entity;

import android.os.Parcel;
import android.os.Parcelable;

public class EcRecord implements Parcelable {
    private String date;
    private String time;
    private String authAmount;
    private String otherAmount;
    private String countryCode;
    private String currencyCode;
    private String merchName;
    private String transType;
    private String atc;

    public EcRecord() {
    }

    public EcRecord(String date, String time, String authAmount, String otherAmount, String countryCode,
            String currencyCode, String merchName, String transType, String atc) {
        super();
        this.date = date;
        this.time = time;
        this.authAmount = authAmount;
        this.otherAmount = otherAmount;
        this.countryCode = countryCode;
        this.currencyCode = currencyCode;
        this.merchName = merchName;
        this.transType = transType;
        this.atc = atc;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getAuthAmount() {
        return authAmount;
    }

    public void setAuthAmount(String authAmount) {
        this.authAmount = authAmount;
    }

    public String getOtherAmount() {
        return otherAmount;
    }

    public void setOtherAmount(String otherAmount) {
        this.otherAmount = otherAmount;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getMerchName() {
        return merchName;
    }

    public void setMerchName(String merchName) {
        this.merchName = merchName;
    }

    public String getTransType() {
        return transType;
    }

    public void setTransType(String transType) {
        this.transType = transType;
    }

    public String getAtc() {
        return atc;
    }

    public void setAtc(String atc) {
        this.atc = atc;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(date);
        dest.writeString(time);
        dest.writeString(authAmount);
        dest.writeString(otherAmount);
        dest.writeString(countryCode);
        dest.writeString(currencyCode);
        dest.writeString(merchName);
        dest.writeString(transType);
        dest.writeString(atc);

    }

    public static final Parcelable.Creator<EcRecord> CREATOR = new Parcelable.Creator<EcRecord>() {

        @Override
        public EcRecord createFromParcel(Parcel source) {
            String date = source.readString();
            String time = source.readString();
            String authAmount = source.readString();
            String otherAmount = source.readString();
            String countryCode = source.readString();
            String currencyCode = source.readString();
            String merchName = source.readString();
            String transType = source.readString();
            String atc = source.readString();

            EcRecord record = new EcRecord(date, time, authAmount, otherAmount, countryCode, currencyCode, merchName,
                    transType, atc);

            return record;
        }

        @Override
        public EcRecord[] newArray(int size) {
            return new EcRecord[size];
        }
    };

}
