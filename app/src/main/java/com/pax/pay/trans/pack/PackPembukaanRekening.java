package com.pax.pay.trans.pack;

import android.support.annotation.NonNull;
import android.util.Log;

import com.pax.abl.core.ipacker.PackListener;
import com.pax.gl.packer.Iso8583Exception;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.utils.AppLog;

public class PackPembukaanRekening extends PackIso8583 {

    public PackPembukaanRekening(PackListener listener) {
        super(listener, true);
    }

    //nambah bit 37 05/06/2021
    @Override
    protected int[] getRequiredFields() {
        return new int[]{2, 3, 4, 7, 11, 12, 15, 18, 22, 28, 32, 33, 35, 37, 41, 42, 43, 48, 49, 52, 55, 102};
    }

    /*@Override
    protected void setBitData4(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("4", transData.getAmount());
    }*/

    @Override
    protected void setBitData28(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("28", "00001000");
    }

    @Override
    protected void setBitData32(@NonNull TransData transData) throws Iso8583Exception {

        setBitData("32", "000110");
    }

    @Override
    protected void setBitData33(@NonNull TransData transData) throws Iso8583Exception {

        setBitData("33", "000110");
    }

    @Override
    protected void setBitData48(@NonNull TransData transData) throws Iso8583Exception {
        Log.d("teg", "setbit f48 : |"+transData.getField48()+"|");
        setBitData("48", transData.getField48());

        /*String nama             = "                          TRIARSONO";
        String tempatLahir      = "                         PEKALONGAN";
        String tgl              = "19940427";
        String hp               = "   089691007480";
        String id               = "    1234567890123457";
        String tes = nama+tempatLahir+tgl+hp+id;

        setBitData("48", tes);*/
    }

    @Override
    protected void setBitData102(@NonNull TransData transData) throws Iso8583Exception {
        //Source Account Number
        setBitData("102", transData.getAccNo());
    }

}

