package com.pax.pay.trans.pack;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.pax.abl.core.ipacker.PackListener;
import com.pax.gl.packer.Iso8583Exception;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.utils.Fox;

public class PackMpn extends PackIso8583 {

    public PackMpn(PackListener listener) {
        super(listener, true);
    }

    @Override
    protected int[] getRequiredFields() {
        return new int[]{2, 3, 4, 7, 11, 12, 13, 15, 18, 22, 28, 32, 33, 35, 37, 41, 42, 43, 47, 48, 49, 52, 55, 59, 61, 102};
    }

    /*@Override
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
    }*/

    @Override
    protected void setBitData4(@NonNull TransData transData) throws Iso8583Exception {
        super.setBitData("4", transData.getAmount());
    }

    @Override
    protected void setBitData28(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("28", "00001000");
    }

    @Override
    protected void setBitData32(@NonNull TransData transData) throws Iso8583Exception {

        setBitData("32", "00110");
    }

    @Override
    protected void setBitData33(@NonNull TransData transData) throws Iso8583Exception {

        setBitData("33", "00110");
    }


    @Override
    protected void setBitData47(@NonNull TransData transData) throws Iso8583Exception {
        //N..1
        /*- Kode Biling DJP (Kode biling dengan prefiks 0, 1 , 2 dan 3)
        - Kode Biling DJBC (Kode biling dengan prefiks 4, 5 , dan 6 )
        - Kode Biling DJA (Kode biling dengan prefiks 7, 8 , dan 9 )*/
        String pref = transData.getBillingId().substring(0,1);

        setBitData("47", pref);
    }

    @Override
    protected void setBitData48(@NonNull TransData transData) throws Iso8583Exception {
        String data = transData.getBillingId()+transData.getField48();
        setBitData("48", data);
    }

    @Override
    protected void setBitData59(@NonNull TransData transData) throws Iso8583Exception {
        String temp = Fox.paddingKiri("PAY", 5);
        temp += Fox.paddingKiri("MPN", 5);

        setBitData("59", temp);
    }

    @Override
    protected void setBitData61(@NonNull TransData transData) throws Iso8583Exception {
        //N..15 Bill ID
        setBitData("61", transData.getBillingId());
    }

    @Override
    protected void setBitData102(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("102", transData.getAccNo());
    }
}

