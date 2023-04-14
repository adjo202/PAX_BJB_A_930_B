package com.pax.pay.trans.pack;

import android.support.annotation.NonNull;

import com.pax.abl.core.ipacker.PackListener;
import com.pax.gl.packer.Iso8583Exception;
import com.pax.pay.trans.model.TransData;

public class PackQrSaleVoid extends PackIso8583 {
    public static final String TAG = "PackQrSaleVoid";

    public PackQrSaleVoid(PackListener listener) {
        super(listener);
    }

    @Override
    protected int[] getRequiredFields() {
        return new int[]{2, 3, 4, 11, 14, 22, 23, 25, 35, 36, 37, 38, 41, 42, 49, 60, 61, 64};
    }

    @Override
    protected void setBitData59(@NonNull TransData transData) throws Iso8583Exception {
        String temp = transData.getOrigC2bVoucher(); // 原付款凭证码
        String tagA4 = "A4" + String.format("%03d", temp.length()) + temp;
        byte[] f59 = new byte[tagA4.length()];
        System.arraycopy(tagA4.getBytes(), 0, f59, 0, tagA4.length());
        entity.setFieldValue("59", f59);
    }
}
