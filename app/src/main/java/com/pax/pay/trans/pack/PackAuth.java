package com.pax.pay.trans.pack;

import com.pax.abl.core.ipacker.PackListener;

public class PackAuth extends PackIso8583 {

    public static final String TAG = "PackAuth";

    public PackAuth(PackListener listener) {
        super(listener);
    }


    @Override
    protected int[] getRequiredFields() {
        return new int[] {2, 3, 4, 7, 11, 12, 13, 14, 18, 22, 23, 25, 26, 35, 36, 41, 42, 43, 49, 52, 53, 55, 60, 64};
    }
}
