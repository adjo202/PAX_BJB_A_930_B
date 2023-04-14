package com.pax.pay.trans.pack;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.pax.abl.core.ipacker.PackListener;
import com.pax.gl.convert.IConvert.EPaddingPosition;
import com.pax.gl.packer.Iso8583Exception;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;

/**
 * IC卡公钥下载状态上送
 *
 * @author Steven.W
 */
public class PackEmvParamDownload extends PackIso8583 {

    private static final String TAG = "PackEmvParamDownload";

    public PackEmvParamDownload(PackListener listener) {
        super(listener);

    }

    @Override
    protected int[] getRequiredFields() {
        return new int[]{3, 25, 41, 42, 60, 62};
    }

    @Override
    protected void setBitData62(@NonNull TransData transData) throws Iso8583Exception {
        String temp = transData.getField62();
        if (!TextUtils.isEmpty(temp)) {
            setBitData("62", FinancialApplication.getConvert().strToBcd(temp,
                    EPaddingPosition.PADDING_LEFT));
        }
    }

    @Override
    protected void setBitData60(@NonNull TransData transData) throws Iso8583Exception {
        ETransType transType = ETransType.valueOf(transData.getTransType());
        StringBuilder f60 = new StringBuilder(transType.getFuncCode()); // f60.1：transaction
        // type// code[N2]
        f60.append(String.format("%06d", transData.getBatchNo())); // f60.2: Batch number[N6]
        f60.append(transType.getNetCode());// f60.3: network management information code[N3]


        Log.d(TAG,"Sandy=TMKsetBitData60 called!" + f60);
        setBitData("60", f60.toString());
    }


}
