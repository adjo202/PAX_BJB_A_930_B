package com.pax.pay.trans.pack;

import com.pax.abl.core.ipacker.PackListener;

public class PackBatchUp extends PackIso8583 {
    public static final String TAG = "PackBatchUp";

    public PackBatchUp(PackListener listener) {
        super(listener);
    }

    @Override
    protected int[] getRequiredFields() {
        return new int[] {11, 41, 42, 48, 60};
    }
}
