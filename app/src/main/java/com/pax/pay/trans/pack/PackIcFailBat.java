package com.pax.pay.trans.pack;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.pax.abl.core.ipacker.PackListener;
import com.pax.gl.packer.Iso8583Exception;
import com.pax.pay.trans.model.TransData;

public class PackIcFailBat extends PackIso8583 {

    public PackIcFailBat(PackListener listener) {
        super(listener);
    }


    @Override
    protected int[] getRequiredFields() {
        return new int[]{3, 11, 22, 23, 25, 41, 42, 55, 60, 62};
    }

    @Override
    protected void setBitData60(@NonNull TransData transData) throws Iso8583Exception {
        String temp = transData.getField60();
        if (!TextUtils.isEmpty(temp)) {
            setBitData("60", temp);
        }
    }
}
