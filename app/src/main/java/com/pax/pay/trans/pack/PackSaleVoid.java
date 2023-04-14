package com.pax.pay.trans.pack;

import android.support.annotation.NonNull;

import com.pax.abl.core.ipacker.PackListener;
import com.pax.gl.packer.Iso8583Exception;
import com.pax.pay.trans.model.TransData;

public class PackSaleVoid extends PackIso8583 {

    public PackSaleVoid(PackListener listener) {
        super(listener);
    }

    @Override
    protected int[] getRequiredFields() {
        return new int[]{2, 3, 4, 7 , 11, 12, 13, 14, 18, 22, 23, 25, 26, 35, 36, 37, 38, 41, 42, 43, 49, 52, 53,
                55, 60, 61, 64};
    }

    @Override
    protected void setBitData7(@NonNull TransData transData) throws Iso8583Exception {
        //sandy
        //added original bit 7
        setBitData("7", String.valueOf(transData.getOrigDateTimeTrans()));
    }
}
