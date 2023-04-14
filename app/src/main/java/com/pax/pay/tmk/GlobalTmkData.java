package com.pax.pay.tmk;

/**
 * Created by wangyb on 2017/4/12.
 */

public class GlobalTmkData {
    //RSA PUK RID
    private static String rsaPukRID;

    //RSA PUK ID
    private static int rsaPukID;

    //RSA PUK Modul
    private static String rsaPukModul;

    //Modul length
    private static int rsaPukModulLen;

    //RSA PUK Exponent
    private static String rsaPukExponent;

    //Random key
    private static String randomKey;

    public void setRandomKey(String key){ randomKey = key;}

    public String getRandomKey () { return randomKey;}

    public void setRsaPukModulLen (int len){ rsaPukModulLen = len; }

    public int getRsaPukModulLen () { return rsaPukModulLen; }

    public String getRsaPukRID() { return rsaPukRID; }

    public void setRsaPukRID(String rID) {

        rsaPukRID = rID;
    }

    public int getRsaPukID() {

        return rsaPukID;
    }

    public void setRsaPukID(int keyID) {

        rsaPukID = keyID;
    }

    public String getRsaPukExponent() {

        return rsaPukExponent;
    }

    public void setRsaPukExponent(String exponent) {

        rsaPukExponent = exponent;
    }

    public void  setRsaPukModul(String modul){
        rsaPukModul = modul;
    }

    public String getRsaPukModul(){
        return rsaPukModul;
    }


}
