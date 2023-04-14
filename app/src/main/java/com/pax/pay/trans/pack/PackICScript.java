package com.pax.pay.trans.pack;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.pax.abl.core.ipacker.PackListener;
import com.pax.gl.packer.Iso8583Exception;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;

public class PackICScript extends PackIso8583 {

    public PackICScript(PackListener listener) {
        super(listener);
    }

    @Override
    protected int[] getRequiredFields() {
        return new int[]{2, 3, 11, 22, 23, 25, 32, 37, 38, 41, 42, 49, 55, 60, 61, 64};
    }

    @Override
    protected void setBitData3(@NonNull TransData transData) throws Iso8583Exception {
        ETransType origTransType = ETransType.valueOf(transData.getOrigTransType());
        String temp = origTransType.getProcCode();
        if (!TextUtils.isEmpty(temp)) {
            setBitData("3", temp);
            // 为后续解包比较，做准备
            transData.setField3(temp);
        }
    }

    @Override
    protected void setBitData25(@NonNull TransData transData) throws Iso8583Exception {
        ETransType origTransType = ETransType.valueOf(transData.getOrigTransType());
        String temp = origTransType.getServiceCode();
        if (!TextUtils.isEmpty(temp)) {
            setBitData("25", temp);
        }
    }
}
