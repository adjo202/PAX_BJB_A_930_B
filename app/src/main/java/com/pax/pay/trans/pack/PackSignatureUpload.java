package com.pax.pay.trans.pack;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.pax.abl.core.ipacker.PackListener;
import com.pax.gl.convert.IConvert.EPaddingPosition;
import com.pax.gl.packer.Iso8583Exception;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.trans.model.TransData;

public class PackSignatureUpload extends PackIso8583 {

    public PackSignatureUpload(PackListener listener) {
        super(listener);
    }

    @Override
    protected int[] getRequiredFields() {
        return new int[]{2, 3, 4, 11, 15, 25, 37, 41, 42, 55, 62, 64};
    }

    @Override
    protected void setBitData55(@NonNull TransData transData) throws Iso8583Exception {
        String receiptElements = transData.getReceiptElements();
        if (!TextUtils.isEmpty(receiptElements)) {
            entity.setFieldValue("55", FinancialApplication.getConvert().strToBcd(receiptElements,
                    EPaddingPosition.PADDING_LEFT));
        }
    }

    @Override
    protected void setBitData62(@NonNull TransData transData) throws Iso8583Exception {
        byte[] sign = transData.getSignData();
        if (sign != null) {
            entity.setFieldValue("62", sign);
        }
    }
}
