package com.pax.pay.trans.pack;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.pax.abl.core.ipacker.PackListener;
import com.pax.gl.packer.Iso8583Exception;
import com.pax.pay.trans.model.TransData;

public class PackSettleAdjust extends PackSettleOffline {

    public PackSettleAdjust(PackListener listener) {
        super(listener);
    }

    @Override
    protected int[] getRequiredFields() {
        return new int[]{2, 3, 4, 11, 14, 22, 25, 35, 36, 37, 38, 41, 42, 48, 49, 53, 60, 61, 63,
                64};
    }

    @Override
    protected void setBitData48(@NonNull TransData transData) throws Iso8583Exception {
        String tip = transData.getTipAmount();
        if (!TextUtils.isEmpty(tip)) {
            setBitData("48", String.format("%012d", Long.parseLong(tip)));
        }
    }

}
