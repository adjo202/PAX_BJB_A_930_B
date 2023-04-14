package com.pax.pay.trans.pack;

import android.support.annotation.NonNull;
import android.util.Log;

import com.pax.abl.core.ipacker.PackListener;
import com.pax.dal.entity.ETermInfoKey;
import com.pax.device.Device;
import com.pax.gl.packer.Iso8583Exception;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class PackEcho extends PackIso8583 {

    private static final String TAG = "PackEcho";

    public PackEcho(PackListener listener) {
        super(listener);
    }

    @Override
    protected int[] getRequiredFields() {
        return new int[]{41, 42, 60 ,63};
    }

    @Override
    protected void setBitData60(@NonNull TransData transData) throws Iso8583Exception {
        ETransType transType = ETransType.valueOf(transData.getTransType());
        StringBuilder f60 = new StringBuilder(transType.getFuncCode()); // f60.1：transaction
        // type// code[N2]
        f60.append(String.format("%06d", transData.getBatchNo())); // f60.2: Batch number[N6]
        f60.append(transType.getNetCode());// f60.3: network management information code[N3]
        //Log.d(TAG,"Sandy=TMKsetBitData60 called!" + f60);
        setBitData("60", f60.toString());
    }

    @Override
    protected void setBitData63(@NonNull TransData transData) throws Iso8583Exception {
        JSONObject deviceInfo = Device.getBaseInfo();
        setBitData("63", deviceInfo.toString());
    }



}
