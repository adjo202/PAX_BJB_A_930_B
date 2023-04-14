package com.pax.pay.trans.pack;

import android.support.annotation.NonNull;

import com.pax.abl.core.ipacker.PackListener;
import com.pax.gl.packer.Iso8583Exception;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.utils.Fox;

import org.apache.commons.lang.StringUtils;

public class PackTransfer extends PackIso8583 {

    public PackTransfer(PackListener listener) {
        super(listener, true);
    }

    @Override
    protected int[] getRequiredFields() {
        return new int[]{2, 3, 4, 7, 11, 12, 15, 18, 22, 28, 32, 33, 35, 36, 37, 41, 42, 43, 48, 49, 52, 55, 59, 100, 102, 103, 127};
    }

    @Override
    protected void setBitData28(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("28",transData.getField28()); //
    }

    //override 32, 33, 36, 48, 59, 100, 102, 103, 127
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

        setBitData("36", transData.getField36());
    }

    @Override
    protected void setBitData48(@NonNull TransData transData) throws Iso8583Exception {
        //Destination Account Name

        setBitData("48", transData.getField48());
    }

    /*@Override
    protected void setBitData59(@NonNull TransData transData) throws Iso8583Exception {
//        No.   Transaction Type        Transaction Code
//        1     IBFT destination bjb        JAB
//        2     IBFT destination other      OTA
        String data = Fox.paddingKanan(transData.getField59(), 8);

        setBitData("59", data+data);
    }*/

    //bit 60 dihilangkan, dimasukin ke bit 59
    /*@Override
    protected void setBitData60(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("60", "JAB");
    }*/


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

