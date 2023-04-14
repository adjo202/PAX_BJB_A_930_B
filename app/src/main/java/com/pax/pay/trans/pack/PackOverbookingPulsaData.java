package com.pax.pay.trans.pack;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.pax.abl.core.ipacker.PackListener;
import com.pax.gl.packer.Iso8583Exception;
import com.pax.pay.trans.model.TransData;

public class PackOverbookingPulsaData extends PackIso8583 {

    public PackOverbookingPulsaData(PackListener listener) {
        super(listener);
    }

    @Override
    protected int[] getRequiredFields() {
        return new int[]{2, 3, 4, 7, 11, 12, 15, 18, 22, 28, 32, 33, 35, 37, 41, 42, 43, 49, 52, 55, 56};
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
    protected void setBitData28(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("28", transData.getFeeTotalAmount());
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
    protected void setBitData56(@NonNull TransData transData) throws Iso8583Exception {
        String pcode = "";
        try {
            String[] f47 = transData.getField47().split("#"); // di 47 ada data pulsa data
            pcode = f47[2];
            if (pcode.contains("-")){
                String[] temp = pcode.split("-");
                Log.d("teg", "temp : "+temp[0]+" "+temp[1]);
                pcode = temp[1];
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        setBitData("56", transData.getPhoneNo() + "|" + pcode);
    }
}
