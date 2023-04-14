package com.pax.pay.trans.pack;

import android.support.annotation.NonNull;
import android.util.Log;

import com.pax.abl.core.ipacker.PackListener;
import com.pax.gl.packer.Iso8583Exception;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.utils.Fox;

import org.apache.commons.lang.StringUtils;

public class PackTransferInq extends PackIso8583 {

    public PackTransferInq(PackListener listener) {
        super(listener, true);
    }

    @Override
    protected int[] getRequiredFields() {
        return new int[]{2, 3, 4, 7, 11, 12, 15, 18, 22, 32, 33, 35, 36, 41, 42, 43, 49, 52, 55,59, 100, 102, 103, 127};
    }

    //override 32, 33, 36, 59, 100, 102, 103, 127
    @Override
    protected void setBitData32(@NonNull TransData transData) throws Iso8583Exception {

        setBitData("32", "000110");
    }

    @Override
    protected void setBitData33(@NonNull TransData transData) throws Iso8583Exception {

        setBitData("33", "000110");
    }

    @Override
    protected void setBitData36(@NonNull TransData transData) throws Iso8583Exception {
//        n3 spaces(3)
//        Issuer Bank Name          an15         Left justify, padded with space
//        Destination Bank Name     an15    Left justify, padded with space
        /*String issName = Fox.paddingKiri("Bank CIMB Niaga", 15);
        String bankName = Fox.paddingKiri("Bank CIMB Niaga", 15);*/

        String spasi = "   ";
        String bankName = Fox.paddingKiri(transData.getDestBank(), 15);
        String data = spasi+bankName+bankName;
        setBitData("36", data);
    }


    @Override
    protected void setBitData59(@NonNull TransData transData) throws Iso8583Exception {
//        No.   Transaction Type        Transaction Code
//        1     IBFT destination bjb        JAB
//        2     IBFT destination other      OTA

        setBitData("59", transData.getField59());
    }


    @Override
    protected void setBitData100(@NonNull TransData transData) throws Iso8583Exception {
        //Kode Bank Pengirim
        String bjb = "110";

        setBitData("100", bjb);
    }

    @Override
    protected void setBitData102(@NonNull TransData transData) throws Iso8583Exception {
        //Source Account Number

        setBitData("102", StringUtils.deleteWhitespace(transData.getAccNo()));
    }

    @Override
    protected void setBitData103(@NonNull TransData transData) throws Iso8583Exception {
        //Destination Account Number

        setBitData("103", transData.getField103());
    }

    @Override
    protected void setBitData127(@NonNull TransData transData) throws Iso8583Exception {
        //Kode Bank Tujuan

        setBitData("127", transData.getField127());
    }

}
