package com.pax.pay.trans.pack;

import android.support.annotation.NonNull;

import com.pax.abl.core.ipacker.PackListener;
import com.pax.gl.packer.Iso8583Exception;
import com.pax.pay.trans.model.TransData;

public class PackESamsat extends PackIso8583 {

    public PackESamsat(PackListener listener) {
        super(listener, true);
    }

    @Override
    protected int[] getRequiredFields() {
        return new int[]{2, 3, 4, 7, 11, 12,13, 18, 22, 28, 32, 35, 37, 41, 42, 43, 49, 52, 55, 57, 59, 61, 102, 106, 125}; //12,13
//        return new int[]{2, 3, 4, 7, 11, 18, 22, 28, 32, 35, 37, 41, 42, 43, 49, 52, 57, 59, 61, 102}; //106, 125 gk diisi
    }



    @Override
    protected void setBitData28(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("28", "00001000");
    }

    @Override
    protected void setBitData32(@NonNull TransData transData) throws Iso8583Exception {

        setBitData("32", "000110");
    }


    @Override
    protected void setBitData59(@NonNull TransData transData) throws Iso8583Exception {
        /*Merchant Code
        PKB e-Samsat Jawa Barat
        PKC e-Samsat Banten
        PKE e-Samsat Kepulauan Riau
        */

        //setBitData("59", new String("PKB"));
        setBitData("59", new String(transData.getSamsatMerchantKode()));
    }

    @Override
    protected void setBitData61(@NonNull TransData transData) throws Iso8583Exception {
        //kode bayar 16
        //prefix : 32 (Jawa Barat) ,36 (Banten), 08(SAMOLNAS)

        //setBitData("61", new String("3222302805190102"));
        setBitData("61", new String(transData.getSamsatKodeBayar()));
    }

    @Override
    protected void setBitData102(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("102",transData.getAccNo()); // 0056763406101
    }

    @Override
    protected void setBitData106(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("106", "");
    }

    @Override
    protected void setBitData125(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("125", "");
    }
}

