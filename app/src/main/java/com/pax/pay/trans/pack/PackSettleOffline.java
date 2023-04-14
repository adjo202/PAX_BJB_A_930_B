package com.pax.pay.trans.pack;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.pax.abl.core.ipacker.PackListener;
import com.pax.gl.packer.Iso8583Exception;
import com.pax.pay.trans.model.TransData;

public class PackSettleOffline extends PackIso8583 {

    public PackSettleOffline(PackListener listener) {
        super(listener);
    }

    @Override
    protected int[] getRequiredFields() {
        return new int[]{2, 3, 4, 11, 14, 22, 25, 26, 35, 36, 38, 41, 42, 49, 61, 63, 64};
    }

    @Override
    protected void setBitData38(@NonNull TransData transData) throws Iso8583Exception {
        String temp = transData.getAuthCode();
        if (!TextUtils.isEmpty(temp)) {
            setBitData("38", temp);
        }
    }

    @Override
    protected void setBitData61(@NonNull TransData transData) throws Iso8583Exception {
        // 原批次号
        long origBatchNo = transData.getOrigBatchNo();
        // 原流水号
        long origTransNo = transData.getOrigTransNo();
        // 原交易日期
        String origDate = transData.getOrigDate();
        // 原授权方式
        String authMode = transData.getAuthMode();
        // 原授权机构
        String authInsCode = transData.getAuthInsCode();

        //field 61.1
        String temp = String.format("%06d", origBatchNo);
        //field 61.2
        temp += String.format("%06d", origTransNo);
        //field 61.3
        if (origDate != null && origDate.length() == 4) {
            temp += origDate;
        } else {
            temp += "0000";
        }

        //field 1.4
        if (authMode != null && authMode.length() == 2) {
            temp += authMode;
        } else {
            temp += "00";
        }

        //field 61.5
        if (!TextUtils.isEmpty(authInsCode)) {
            temp += authInsCode;
        }
        setBitData("61", temp);
    }
}
