package com.pax.pay.trans.pack;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.pax.abl.core.ipacker.PackListener;
import com.pax.device.Device;
import com.pax.gl.packer.Iso8583Exception;
import com.pax.pay.trans.model.TransData;

import org.json.JSONException;
import org.json.JSONObject;


public class PackBalanceInquiry extends PackIso8583 {
    private static final String TAG = "PackBalanceInquiry";

    public PackBalanceInquiry(PackListener listener) {
        super(listener, true);
    }

    //nambah bit 37 08/06/2021
    @Override
    protected int[] getRequiredFields() {
        return new int[]{2, 3, 4, 7, 11, 12, 15, 18, 22, 32, 33, 35, 37, 41, 42, 43, 49, 52, 55, 102};
    }

    @Override
    protected void setBitData102(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("102",transData.getAccNo()); // 0056763406101
    }


    @Override
    protected void setBitData15(@NonNull TransData transData) throws Iso8583Exception {
        String settleDate = transData.getSettleDate();
        if (!TextUtils.isEmpty(settleDate)) {
            setBitData("15", settleDate);
        }
        if (settleDate == null) {
            String bit15 = String.valueOf(transData.getDateTimeTrans()).substring(0, 3);
            setBitData("15", bit15);
            Log.i("teg", "bit 15" + bit15);
        }
    }

}
