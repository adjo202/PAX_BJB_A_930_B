package com.pax.pay.trans.pack;

import com.pax.abl.core.ipacker.PackListener;

/**
 * Created by yanglj on 2017-03-30.
 */

public class PackRateDownload extends PackIso8583 {
    public static final String TAG = "PackRateDownload";

    public PackRateDownload(PackListener listener) {
        super(listener);

    }

    @Override
    protected int[] getRequiredFields() {
        return new int[]{3, 11, 25, 41, 42, 60, 62};
    }
}
