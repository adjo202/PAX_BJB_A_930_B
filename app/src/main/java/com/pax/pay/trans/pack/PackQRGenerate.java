package com.pax.pay.trans.pack;

import android.support.annotation.NonNull;

import com.pax.abl.core.ipacker.PackListener;
import com.pax.gl.packer.Iso8583Exception;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;

public class PackQRGenerate extends PackIso8583 {

    public PackQRGenerate(PackListener listener){
        super(listener);
    }

    @Override
    protected int[] getRequiredFields() {
        return new int[]{ 2, 3, 4, 7, 11, 12, 13, 14, 18, 22, 23, 25, 26, 41, 42, 43, 49, 60, 63,64};
    }


    @Override
    protected void setBitData60(@NonNull TransData transData) throws Iso8583Exception {
        //sandy
        //63 = Generate QR
        ETransType transType = ETransType.valueOf(transData.getTransType());
        setBitData("60", String.format("000000%s",transType.getFuncCode()));
    }



}
