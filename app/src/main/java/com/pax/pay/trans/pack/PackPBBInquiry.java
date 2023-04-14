package com.pax.pay.trans.pack;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.pax.abl.core.ipacker.PackListener;
import com.pax.gl.packer.Iso8583Exception;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.utils.Fox;
import com.pax.settings.SysParam;

import org.apache.commons.lang.StringUtils;

import static com.pax.pay.trans.component.Component.incTransNo;

public class PackPBBInquiry extends PackIso8583 {

    public PackPBBInquiry(PackListener listener) {
        super(listener, true);
    }

    @Override
    protected int[] getRequiredFields() {
        return new int[]{2, 3, 4, 7, 11, 12, 15, 18, 22, 28, 32, 33, 35,41, 42, 43, 49, 52, 55, 59, 61, 102, 107};
    }

    @Override
    protected void setBitData11(@NonNull TransData transData) throws Iso8583Exception {
        if (transData.getTransTypeEnum().equals(ETransType.PBB_INQ)){
            setBitData("11", String.valueOf(transData.getTransNo()));
        }else {
            incTransNo();
            transData.setTransNo(Long.parseLong(FinancialApplication.getSysParam().get(SysParam.TRANS_NO)));
            setBitData("11", String.valueOf(transData.getTransNo()));
        }

    }

    @Override
    protected void setBitData15(@NonNull TransData transData) throws Iso8583Exception {
        String date = transData.getDate();
        if (!TextUtils.isEmpty(date)) {
            setBitData("15", date);
        }
    }

    @Override
    protected void setBitData28(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("28", "00002500");
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
    protected void setBitData59(@NonNull TransData transData) throws Iso8583Exception {

        String data = Fox.paddingKanan("PAY", 5);
        data = data + Fox.paddingKanan("PBB", 5);

        setBitData("59", data);
    }

    @Override
    protected void setBitData61(@NonNull TransData transData) throws Iso8583Exception {
        //NOP 18 + Tahun 4
        if (transData.getTransTypeEnum().equals(ETransType.PBB_INQ)){
            setBitData("61", transData.getField61());
        }else {
            setBitData("61", Fox.Hex2Txt(transData.getField61()));
        }
    }

    @Override
    protected void setBitData102(@NonNull TransData transData) throws Iso8583Exception {
        if (transData.getTransTypeEnum().equals(ETransType.PBB_PAY)){
            setBitData("102", transData.getField102());
        }
    }

    @Override
    protected void setBitData107(@NonNull TransData transData) throws Iso8583Exception {
        //Kode Pemda
        setBitData("107", transData.getField107());
    }

}
