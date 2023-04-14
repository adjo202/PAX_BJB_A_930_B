package com.pax.pay.trans.pack;

import android.support.annotation.NonNull;

import com.pax.abl.core.ipacker.PackListener;
import com.pax.gl.packer.Iso8583Exception;
import com.pax.pay.trans.model.TransData;

public class PackPPOB extends PackIso8583 {

    public PackPPOB(PackListener listener) {
        super(listener, true);
    }

    @Override
    protected int[] getRequiredFields() {
        return new int[]{2, 3, 7, 11, 15, 22, 37, 41, 42, 43, 48, 56};
    }

    @Override
    protected void setBitData56(@NonNull TransData transData) throws Iso8583Exception {
        //BillID|Product Code
        //3333333333|
        setBitData("56", "3333333333|PDAMKOLPG");
    }

}

