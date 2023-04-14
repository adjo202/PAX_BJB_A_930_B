package com.pax.pay.trans.pack;

import com.pax.abl.core.ipacker.PackListener;

public class PackTotalProductPulsaData extends PackIso8583 {
    private static final String TAG = "PackTotalProductPulsaData";

    public PackTotalProductPulsaData(PackListener listener) {
        super(listener);
    }

    @Override
    protected int[] getRequiredFields() {
        return new int[]{3, 7, 11, 15, 41, 42, 43};
    }

}
