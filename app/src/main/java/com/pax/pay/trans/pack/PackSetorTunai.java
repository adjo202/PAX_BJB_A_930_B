package com.pax.pay.trans.pack;

import android.support.annotation.NonNull;

import com.pax.abl.core.ipacker.PackListener;
import com.pax.gl.packer.Iso8583Exception;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.utils.Fox;

public class PackSetorTunai extends PackIso8583 {

    public PackSetorTunai(PackListener listener) {
        super(listener, true);
    }

    //nambah bit 37 05/06/2021
    @Override
    protected int[] getRequiredFields() {
        return new int[]{2, 3, 4, 7, 11, 12, 15, 18, 22, 28, 32, 33, 35, 37, 41, 42, 43, 48, 49, 52, 55, 61, 103};
    }

    //override 28, 32,33,48,61,103
    @Override
    protected void setBitData28(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("28", "00002000");
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
        setBitData("48", transData.getField48());
    }

    @Override
    protected void setBitData61(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("61", Fox.Hex2Txt(transData.getField61()));
    }

    @Override
    protected void setBitData103(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("103", transData.getAccNo());
    }

}
