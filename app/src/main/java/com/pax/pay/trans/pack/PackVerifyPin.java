package com.pax.pay.trans.pack;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.pax.abl.core.ipacker.PackListener;
import com.pax.gl.packer.Iso8583Exception;
import com.pax.pay.trans.model.TransData;

public class PackVerifyPin extends PackIso8583 {

    public PackVerifyPin(PackListener listener) {
        super(listener);
    }

    @Override
    protected int[] getRequiredFields() {
        return new int[]{2, 3, 4, 7, 11, 12, 15, 18, 22, 32, 33, 35, 41, 42, 43, 49, 52, 59, 63};
    }

    @Override
    protected void setBitData15(@NonNull TransData transData) throws Iso8583Exception {
        String date = transData.getDate();
        if (!TextUtils.isEmpty(date)) {
            setBitData("15", date);
        }
    }

    @Override
    protected void setBitData22(@NonNull TransData transData) throws Iso8583Exception {

        setBitData("22", "021");
    }

    @Override
    protected void setBitData32(@NonNull TransData transData) throws Iso8583Exception {

        setBitData("32", "000110");
    }

    @Override
    protected void setBitData33(@NonNull TransData transData) throws Iso8583Exception {

        setBitData("33", "000110");
    }

    @Override
    protected void setBitData59(@NonNull TransData transData) throws Iso8583Exception {

        setBitData("59", "JAB");
    }

    @Override
    protected void setBitData63(@NonNull TransData transData) throws Iso8583Exception {

        setBitData("63", "01");
    }

}
