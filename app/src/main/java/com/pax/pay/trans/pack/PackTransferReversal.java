package com.pax.pay.trans.pack;

import android.support.annotation.NonNull;
import android.util.Log;

import com.pax.abl.core.ipacker.PackListener;
import com.pax.gl.packer.Iso8583Exception;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.utils.Fox;
import com.pax.settings.SysParam;

import org.apache.commons.lang.StringUtils;

public class PackTransferReversal extends PackIso8583 {

    public PackTransferReversal(PackListener listener) {
        super(listener, true);
    }

    SysParam sysParam = FinancialApplication.getSysParam();

    @Override
    protected int[] getRequiredFields() {
        return new int[]{2, 3, 4, 7, 11, 12, 15, 18, 22, 28, 32, 33, 35, 36, 37, 41, 42, 43, 48, 49, 59, 90, 100, 102, 103, 127};
    }

    protected void setBitData18(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("18", sysParam.get(SysParam.MCC));
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
    protected void setBitData37(@NonNull TransData transData) throws Iso8583Exception {
        try {
            if (transData.getRefNo() != null || transData.getRefNo().length() > 1) {
                setBitData("37", transData.getRefNo());
            } else {
                String padd = String.valueOf(transData.getTransNo());
                String bit7 = String.valueOf(transData.getDateTimeTrans());
                bit7 = StringUtils.leftPad(bit7, 10, "0");
                padd = StringUtils.leftPad(padd, 6, "0");
                setBitData("37", bit7 + padd.substring(4, 6)); // format 37 pakai datetime MMddHHmmss Pak
            }
        } catch (Exception e) {
            String padd = String.valueOf(transData.getTransNo());
            String bit7 = String.valueOf(transData.getDateTimeTrans());
            bit7 = StringUtils.leftPad(bit7, 10, "0");
            padd = StringUtils.leftPad(padd, 6, "0");
            setBitData("37", bit7 + padd.substring(4, 6)); // format 37 pakai datetime MMddHHmmss Pak
        }
    }

    protected void setBitData43(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("43", sysParam.get(SysParam.MERCH_EN));
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
    protected void setBitData90(@NonNull TransData transData) throws Iso8583Exception {
        //Original Message Type n4
        //Original STAN n6
        //Original Transmission Date and Time n10
        //Original Acquiring Institution ID Code Left justify, padded with space n11
        //Original Forwarding Institution ID n11 Left justify, padded with space n11

        String data = ETransType.TRANSFER.getMsgType();
        data += Component.getPaddedString(String.valueOf(transData.getTransNo()), 6, '0');
        data += transData.getDate() + transData.getTime();
        Log.i("dia21","datetime: "+transData.getDateTimeTrans());
        data += Fox.paddingKanan("000110", 11);
        data += Fox.paddingKanan("000110", 11);
        Log.i("dia21","data: "+data);

        setBitData("90", data);
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

