package com.pax.pay.trans.pack;

import android.support.annotation.NonNull;

import com.pax.abl.core.ipacker.PackListener;
import com.pax.gl.packer.Iso8583Exception;
import com.pax.pay.trans.model.TransData;

public class PackPembatalanRekInq extends PackIso8583 {

    public PackPembatalanRekInq(PackListener listener) {
        super(listener, true);
    }

    @Override
    protected int[] getRequiredFields() {
        return new int[]{2, 3, 4, 7, 11, 12, 15, 18, 22, 28, 32, 33, 35, 41, 42, 43, 48, 49, 52, 102};
    }

    @Override
    protected void setBitData28(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("28", "00001000");
    }

    //override 32, 33, 48, 102
    @Override
    protected void setBitData32(@NonNull TransData transData) throws Iso8583Exception {

        setBitData("32", "000110");
    }

    @Override
    protected void setBitData33(@NonNull TransData transData) throws Iso8583Exception {

        setBitData("33", "000110");
    }

    /*@Override
    protected void setBitData48(@NonNull TransData transData) throws Iso8583Exception {
        //ID Pembukaan Rekening

        setBitData("48", "060421264378");
    }*/

    @Override
    protected void setBitData102(@NonNull TransData transData) throws Iso8583Exception {
        //Source Account Number

        setBitData("102", transData.getAccNo());
    }

}


