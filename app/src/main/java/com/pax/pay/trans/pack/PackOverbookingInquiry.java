package com.pax.pay.trans.pack;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.pax.abl.core.ipacker.PackListener;
import com.pax.gl.packer.Iso8583Exception;
import com.pax.pay.trans.model.TransData;

public class PackOverbookingInquiry extends PackIso8583 {

    public PackOverbookingInquiry(PackListener listener) {
        super(listener, true);
    }

    @Override
    protected int[] getRequiredFields() {
        return new int[]{2, 3, 4, 7, 11, 12, 15, 18, 22, 32, 33, 35, 41, 42, 43, 49, 52, 103};
    }

    @Override
    protected void setBitData15(@NonNull TransData transData) throws Iso8583Exception {
        String settleDate = transData.getSettleDate();
        Log.i("abdul", "bit 15 = " + settleDate);
        if (!TextUtils.isEmpty(settleDate)) {
            setBitData("15", settleDate);
        }
        if (settleDate == null) {
            String bit15 = String.valueOf(transData.getDateTimeTrans()).substring(0, 3);
            setBitData("15", bit15);
            Log.i("abdul", "bit 15 null set = " + bit15);
        }
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
    protected void setBitData103(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("103",transData.getAccNo()); // 0056763406101
    }

}
