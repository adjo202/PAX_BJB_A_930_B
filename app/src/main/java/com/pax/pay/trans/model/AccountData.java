package com.pax.pay.trans.model;

import com.pax.gl.db.IDb.AEntityBase;
import com.pax.gl.db.IDb.Column;

import java.io.Serializable;

/**
 *
 * @author sandy@indopay.com
 * 
 */
public class AccountData extends AEntityBase implements Serializable {

    public static final String TAG = "AccountData";

    private static final long serialVersionUID = 1L;

    public static final String SAVING   = "10";
    public static final String CURRENT  = "20";



    @Column
    private String accountType;

    @Column
    private String accountNumber;

    public AccountData(String accountType, String accountNumber) {
        this.accountType = accountType;
        this.accountNumber = accountNumber;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getAccountTypeText(){
        if(accountType.equals(SAVING))
            return "Tabungan";
        else
            return "Giro";
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    @Override
    public String toString() {
        return "AccountData{" +
                "accountType='" + accountType + '\'' +
                ", accountNumber='" + accountNumber + '\'' +
                '}';
    }
}
