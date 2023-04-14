package com.pax.pay.trans.pack;

import android.support.annotation.NonNull;

import com.pax.abl.core.ipacker.PackListener;
import com.pax.gl.convert.IConvert;
import com.pax.gl.packer.Iso8583Exception;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.trans.model.TransData;

public class PackOfflineTransSend extends PackIso8583 {

    public PackOfflineTransSend(PackListener listener) {
        super(listener);
    }

    @Override
    protected int[] getRequiredFields() {
        return new int[]{2, 3, 4, 11, 14, 22, 23, 25, 26, 35, 36, 41, 42, 44, 49, 52, 53, 55, 60,
                63, 64};
    }

    @Override
    protected void setBitData4(@NonNull TransData transData) throws Iso8583Exception {
        String amount;
        if (transData.getCurrencyRate() != null) {
            amount = transData.getForeignAmount();
        } else {
            amount = transData.getAmount();
        }
        setBitData("4", amount);
    }

    @Override
    protected void setBitData44(@NonNull TransData transData) throws Iso8583Exception {
        String temp = FinancialApplication.getSysParam().getCurrency().getCode() +
                FinancialApplication.getConvert().stringPadding(transData.getAmount(), '0', 12,
                        IConvert.EPaddingPosition.PADDING_LEFT);
        if (transData.getCurrencyRate() != null) {
            temp += transData.getCurrencyRate();
        } else {
            temp += "30001000";
        }
        setBitData("44", temp);
    }

    @Override
    protected void setBitData49(@NonNull TransData transData) throws Iso8583Exception {
        String currencyCode;
        if (transData.getCurrencyRate() != null) {
            currencyCode = transData.getCardCurrencyCode();
        } else {
            currencyCode = FinancialApplication.getSysParam().getCurrency().getCode();
        }
        setBitData("49", currencyCode);
    }
}
