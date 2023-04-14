package com.pax.pay.trans.pack;

import android.support.annotation.NonNull;

import com.pax.abl.core.ipacker.PackListener;
import com.pax.gl.packer.Iso8583Exception;
import com.pax.pay.trans.model.TransData;

public class PackDownloadProductPulsaData extends PackIso8583 {

    public PackDownloadProductPulsaData(PackListener listener) {
        super(listener);
    }

    @Override
    protected int[] getRequiredFields() {
        return new int[]{3, 7, 11, 15, 41, 42, 43, 48};
    }

    @Override
    protected void setBitData48(@NonNull TransData transData) throws Iso8583Exception {
        // set bit 48 index product
        setBitData("48", transData.getField48());
    }

}
