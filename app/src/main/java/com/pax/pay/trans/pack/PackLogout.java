package com.pax.pay.trans.pack;

import com.pax.abl.core.ipacker.PackListener;

public class PackLogout extends PackIso8583 {

    public PackLogout(PackListener listener) {
        super(listener);
    }

    @Override
    protected int[] getRequiredFields() {
        return new int[]{11, 41, 42, 60};
    }
}
