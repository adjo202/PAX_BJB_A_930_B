package com.pax.pay.trans.pack;

import android.support.annotation.NonNull;

import com.pax.abl.core.ipacker.PackListener;
import com.pax.gl.packer.Iso8583Exception;
import com.pax.pay.trans.model.TransData;

public class PackTarikTunai extends PackIso8583 {

    public PackTarikTunai(PackListener listener) {
        super(listener, true);
    }

    //nambah bit 37 05/06/2021
    @Override
    protected int[] getRequiredFields() {
        return new int[]{2, 3, 4, 7, 11, 12, 15, 18, 22, 28, 32, 33, 35, 37, 41, 42, 43, 49, 52, 55, 102};
    }

    @Override
    protected void setBitData28(@NonNull TransData transData) throws Iso8583Exception {
        transData.setField28("00002000");
        setBitData("28", transData.getField28());
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
    protected void setBitData102(@NonNull TransData transData) throws Iso8583Exception {
        //do nothing
        setBitData("102",transData.getAccNo()); // 0056763406101
    }

}
