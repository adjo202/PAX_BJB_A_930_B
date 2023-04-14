package com.pax.pay.trans.pack;

import com.pax.abl.core.ipacker.PackListener;

public class PackBalance extends PackIso8583 {
    public static final String TAG = "PackBalance";

    public PackBalance(PackListener listener) {
        super(listener);
    }

    @Override
    protected int[] getRequiredFields() {
        return new int[] {2, 3, 11, 14, 22, 23, 25, 26, 35, 36, 41, 42, 49, 52, 53, 55, 60, 62, 64};
    }
}
