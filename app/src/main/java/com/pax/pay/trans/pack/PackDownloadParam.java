package com.pax.pay.trans.pack;

import android.support.annotation.NonNull;
import android.util.Log;

import com.pax.abl.core.ipacker.PackListener;
import com.pax.gl.packer.Iso8583Exception;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;

/**
 * 磁条卡参数下载
 *
 * @author Steven.W
 */
public class PackDownloadParam extends PackIso8583 {

    private static final String TAG = "PackDownloadParam";

    public PackDownloadParam(PackListener listener) {
        super(listener);
    }

    @Override
    protected int[] getRequiredFields() {
        return new int[]{41, 42, 60};
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
