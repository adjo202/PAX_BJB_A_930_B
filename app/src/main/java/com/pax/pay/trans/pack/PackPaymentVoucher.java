package com.pax.pay.trans.pack;

import android.support.annotation.NonNull;

import com.pax.abl.core.ipacker.PackListener;
import com.pax.gl.packer.Iso8583Exception;
import com.pax.pay.trans.model.TransData;

public class PackPaymentVoucher extends PackIso8583 {

    public PackPaymentVoucher(PackListener listener) {
        super(listener,true);
    }

    @Override
    protected int[] getRequiredFields() {
        return new int[]{2, 3, 7, 11, 15, 22, 41, 42, 43, 48};
    }

    @Override
    protected void setBitData48(@NonNull TransData transData) throws Iso8583Exception {
        // number phone destination n20 left justify, padding space. Product code An20 left justify, padding space
        setBitData("48", transData.getField48());
    }

}
