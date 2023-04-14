package com.pax.pay.trans.pack;

import com.pax.abl.core.ipacker.PackListener;

public class PackRefund extends PackIso8583 {
    public static final String TAG = "PackRefund";

    public PackRefund(PackListener listener) {
        super(listener);
    }

    @Override
    protected int[] getRequiredFields() {
        return new int[]{2, 3, 4, 11, 14, 22, 23, 25, 26, 35, 36, 37, 38, 41, 42, 48, 49, 52, 53,
                60, 61, 63, 64};
    }
}
