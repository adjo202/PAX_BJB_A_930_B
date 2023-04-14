package com.pax.pay.trans.pack;

import android.support.annotation.NonNull;

import com.pax.abl.core.ipacker.PackListener;
import com.pax.gl.packer.Iso8583Exception;
import com.pax.pay.trans.model.TransData;

public class PackInstalSale extends PackIso8583 {

    public PackInstalSale(PackListener listener) {
        super(listener);
    }

    @Override
    protected int[] getRequiredFields() {
        return new int[]{2, 3, 4, 11, 14, 22, 23, 25, 26, 35, 36, 41, 42, 49, 52, 53, 55, 60, 63,
                64};
    }

    @Override
    protected void setBitData62(@NonNull TransData transData) throws Iso8583Exception {
        String f62 = transData.getInstalNum();
        f62 += transData.getPrjCode();
        setBitData("62", f62);
    }
}
