package com.pax.pay.trans.model;

import android.os.Parcel;
import android.os.Parcelable;

public class EcLoadRecord implements Parcelable {

    private String P1;
    private String P2;
    private String beforeLoadAmount;
    private String afterLoadAmount;
    private String date;
    private String time;
    private String countryCode;
    private String merchName;
    private String atc;

    public EcLoadRecord() {
    }

    public EcLoadRecord(String P1, String P2, String beforeLoadAmount, String afterLoadAmount, String date,
            String time, String countryCode, String merchName, String atc) {

        super();
        this.P1 = P1;
        this.P2 = P2;
        this.beforeLoadAmount = beforeLoadAmount;
        this.afterLoadAmount = afterLoadAmount;
        this.date = date;
        this.time = time;
        this.countryCode = countryCode;
        this.merchName = merchName;
        this.atc = atc;
    }

    @Override
    public int describeContents() {

        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(P1);
        dest.writeString(P2);
        dest.writeString(beforeLoadAmount);
        dest.writeString(afterLoadAmount);
        dest.writeString(date);
        dest.writeString(time);
        dest.writeString(countryCode);
        dest.writeString(merchName);
        dest.writeString(atc);
    }

    public static final Parcelable.Creator<EcLoadRecord> CREATOR = new Parcelable.Creator<EcLoadRecord>() {

        @Override
        public EcLoadRecord createFromParcel(Parcel source) {
            String P1 = source.readString();
            String P2 = source.readString();
            String beforeLoadAmount = source.readString();
            String afterLoadAmount = source.readString();
            String date = source.readString();
            String time = source.readString();
            String countryCode = source.readString();
            String merchName = source.readString();
            String atc = source.readString();

            EcLoadRecord record = new EcLoadRecord(P1, P2, beforeLoadAmount, afterLoadAmount, date, time, countryCode,
                    merchName, atc);

            return record;
        }

        @Override
        public EcLoadRecord[] newArray(int size) {
            return new EcLoadRecord[size];
        }
    };

    public String getP1() {
        return P1;
    }

    public void setP1(String p1) {
        P1 = p1;
    }

    public String getP2() {
        return P2;
    }

    public void setP2(String p2) {
        P2 = p2;
    }

    public String getBeforeLoadAmount() {
        return beforeLoadAmount;
    }

    public void setBeforeLoadAmount(String beforeLoadAmount) {
        this.beforeLoadAmount = beforeLoadAmount;
    }

    public String getAfterLoadAmount() {
        return afterLoadAmount;
    }

    public void setAfterLoadAmount(String afterLoadAmount) {
        this.afterLoadAmount = afterLoadAmount;
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

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getMerchName() {
        return merchName;
    }

    public void setMerchName(String merchName) {
        this.merchName = merchName;
    }

    public String getAtc() {
        return atc;
    }

    public void setAtc(String atc) {
        this.atc = atc;
    }
}
