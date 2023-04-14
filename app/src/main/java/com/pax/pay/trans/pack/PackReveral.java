package com.pax.pay.trans.pack;

import com.pax.abl.core.ipacker.PackListener;

public class PackReveral extends PackIso8583 {

    public PackReveral(PackListener listener) {
        super(listener);
    }

    @Override
    protected int[] getRequiredFields() {
        return new int[]{2, 3, 4, 11, 14, 22, 23, 25, 38, 39, 41, 42, 49, 55, 60, 64};
    }
}
