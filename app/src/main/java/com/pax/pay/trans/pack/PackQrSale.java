package com.pax.pay.trans.pack;

import android.support.annotation.NonNull;

import com.pax.abl.core.ipacker.PackListener;
import com.pax.gl.packer.Iso8583Exception;
import com.pax.pay.trans.model.TransData;

public class PackQrSale extends PackIso8583 {

    public PackQrSale(PackListener listener) {
        super(listener);
    }

    @Override
    protected int[] getRequiredFields() {
        return new int[]{2, 3, 4, 11, 14, 22, 23, 25, 35, 36, 41, 42, 49, 59, 60};
    }

    @Override
    protected void setBitData59(@NonNull TransData transData) throws Iso8583Exception {
        String c2bMessage = transData.getC2b(); // 55域TagA3 扫码付C2B信息码
        String tagA3 = "A3" + String.format("%03d", c2bMessage.length()) + c2bMessage;
        byte[] f59 = new byte[tagA3.length()];
        System.arraycopy(tagA3.getBytes(), 0, f59, 0, tagA3.length());
        entity.setFieldValue("59", f59);
    }
}
