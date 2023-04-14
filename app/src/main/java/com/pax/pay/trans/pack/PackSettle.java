package com.pax.pay.trans.pack;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.pax.abl.core.ipacker.PackListener;
import com.pax.gl.packer.Iso8583Exception;
import com.pax.pay.trans.model.TransData;

public class PackSettle extends PackIso8583 {

    public PackSettle(PackListener listener) {
        super(listener);
    }

    @Override
    protected int[] getRequiredFields() {
        return new int[]{2, 3, 4, 11, 14, 22, 25, 38, 41, 42, 48, 49, 53, 60, 61, 63};
    }

    @Override
    protected void setBitData63(@NonNull TransData transData) throws Iso8583Exception {
        String temp = transData.getOper();
        if (TextUtils.isEmpty(temp)) {
            temp = "01";
        }
        String f63 = temp + " ";
        setBitData("63", f63);
    }
}
