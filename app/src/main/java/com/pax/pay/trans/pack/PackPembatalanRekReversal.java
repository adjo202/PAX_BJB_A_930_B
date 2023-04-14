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

public class PackPembatalanRekReversal extends PackIso8583 {

    public PackPembatalanRekReversal(PackListener listener) {
        super(listener, true);
    }
    SysParam sysParam = FinancialApplication.getSysParam();

    @Override
    protected int[] getRequiredFields() {
        return new int[]{2, 3, 4, 7, 11, 12, 15, 18, 22, 28, 32, 33, 35, 37, 41, 42, 43, 48, 49, 90, 102};
    }

    protected void setBitData18(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("18", sysParam.get(SysParam.MCC));
    }

    @Override
    protected void setBitData28(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("28", transData.getField28());
    }

    //override 32, 33, 48, 102
    @Override
    protected void setBitData32(@NonNull TransData transData) throws Iso8583Exception {

        setBitData("32", "000110");
    }

    @Override
    protected void setBitData33(@NonNull TransData transData) throws Iso8583Exception {

        setBitData("33", "000110");
    }

    protected void setBitData43(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("43", sysParam.get(SysParam.MERCH_EN));
    }

    @Override
    protected void setBitData48(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("48", transData.getField48());
    }

    @Override
    protected void setBitData90(@NonNull TransData transData) throws Iso8583Exception {
        //Original Message Type n4
        //Original STAN n6
        //Original Transmission Date and Time n10
        //Original Acquiring Institution ID Code Left justify, padded with space n11
        //Original Forwarding Institution ID n11 Left justify, padded with space n11

        String data = ETransType.PEMBATAL_REK.getMsgType();
        data += Component.getPaddedString(String.valueOf(transData.getTransNo()), 6, '0');
        data += transData.getDate() + transData.getTime();
        Log.i("dia21","datetime: "+transData.getDateTimeTrans());
        data += Fox.paddingKanan("000110", 11);
        data += Fox.paddingKanan("000110", 11);
        Log.i("dia21","data: "+data);

        setBitData("90", data);
    }

    @Override
    protected void setBitData102(@NonNull TransData transData) throws Iso8583Exception {
        //Source Account Number

        setBitData("102", transData.getAccNo());
    }

}


