package com.pax.pay.trans.pack;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.pax.abl.core.ipacker.PackListener;
import com.pax.gl.packer.Iso8583Exception;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.utils.Fox;

public class PackBpjsTkRegister extends PackIso8583 {

    public PackBpjsTkRegister(PackListener listener) {
        super(listener, true);
    }

    @Override
    protected int[] getRequiredFields() {
        return new int[]{2, 3, 4, 7, 11, 12, 13, 15, 18, 22, 28, 35, 41, 42, 43, 48, 49, 52, 55};
    }


    @Override
    protected void setBitData15(@NonNull TransData transData) throws Iso8583Exception {
        String settleDate = transData.getSettleDate();
        Log.i("teg", "bit 15 = " + settleDate);
        if (!TextUtils.isEmpty(settleDate)) {
            setBitData("15", settleDate);
        }
        if (settleDate == null) {
            String bit15 = String.valueOf(transData.getDateTimeTrans()).substring(0, 3);
            setBitData("15", bit15);
            Log.i("teg", "bit 15" + bit15);
        }
    }

    @Override
    protected void setBitData48(@NonNull TransData transData) throws Iso8583Exception {
        String f48 = transData.getField48();
        //F48 is json
        setBitData("48", f48);
    }


}

