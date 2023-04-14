package com.pax.pay.trans.model;

import com.pax.gl.db.IDb.AEntityBase;
import com.pax.gl.db.IDb.Column;

import java.io.Serializable;

/**
 *
 * @author sandy@indopay.com
 * 
 */
public class RedeemData extends AEntityBase implements Serializable {

    public static final String TAG = "RedeemData";

    private static final long serialVersionUID = 1L;


    @Column
    private String idBiller;

    @Column
    private String voucherNumber;

    public RedeemData(String idBiller, String voucherNumber) {
        this.idBiller = idBiller;
        this.voucherNumber = voucherNumber;
    }

    public String getIdBiller() {
        return idBiller;
    }

    public void setIdBiller(String idBiller) {
        this.idBiller = idBiller;
    }

    public String getVoucherNumber() {
        return voucherNumber;
    }

    public void setVoucherNumber(String voucherNumber) {
        this.voucherNumber = voucherNumber;
    }

    @Override
    public String toString() {
        return "RedeemData{" +
                "idBiller='" + idBiller + '\'' +
                ", voucherNumber='" + voucherNumber + '\'' +
                '}';
    }
}
