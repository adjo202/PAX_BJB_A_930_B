package com.pax.pay.trans.pack;

import android.support.annotation.NonNull;
import android.util.Log;

import com.pax.abl.core.ipacker.PackListener;
import com.pax.gl.packer.Iso8583Exception;
import com.pax.pay.trans.model.TransData;

public class PackOverbooking extends PackIso8583 {

    public PackOverbooking(PackListener listener) {
        super(listener, true);
    }

    //nambah bit 37 05/06/21
    @Override
    protected int[] getRequiredFields() {
        return new int[]{2, 3, 4, 7, 11, 12, 15, 18, 22, 28, 32, 33, 35, 37, 41, 42, 43, 48, 49, 52, 55, 102, 103};
    }

    @Override
    protected void setBitData28(@NonNull TransData transData) throws Iso8583Exception {
        super.setBitData("28", transData.getField28());
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
        Log.d("teg", "f28 "+transData.getField28());
        Log.d("teg", "f48 "+transData.getField48());
        super.setBitData("48", transData.getField48().trim());
    }

    @Override
    protected void setBitData102(@NonNull TransData transData) throws Iso8583Exception {
        //Source Account Number

        setBitData("102", transData.getAccNo());
    }

    @Override
    protected void setBitData103(@NonNull TransData transData) throws Iso8583Exception {
        //Destination Account Number

        setBitData("103", transData.getField103());
    }


}
