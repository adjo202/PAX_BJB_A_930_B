package com.pax.pay.trans.pack;

import android.support.annotation.NonNull;

import com.pax.abl.core.ipacker.PackListener;
import com.pax.device.Device;
import com.pax.gl.packer.Iso8583Exception;
import com.pax.pay.trans.model.TransData;

import org.json.JSONException;
import org.json.JSONObject;

public class PackAccountList extends PackIso8583 {
    private static final String TAG = "PackAccountList";

    public PackAccountList(PackListener listener) {
        super(listener, true);
//        super(listener);
    }

    @Override
    protected int[] getRequiredFields() {
        return new int[]{2, 3, 4, 7, 11, 12, 15, 18, 22, 32, 33, 35, 41, 42, 43, 49, 52};
    }

    @Override
    protected void setBitData63(@NonNull TransData transData) throws Iso8583Exception {

        JSONObject deviceInfo = Device.getBaseInfo();
        try {
            deviceInfo.put("trxType",  "SALE");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        setBitData("63",deviceInfo.toString());

    }


}
