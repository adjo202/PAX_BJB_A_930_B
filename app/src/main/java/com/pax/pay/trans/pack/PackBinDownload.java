package com.pax.pay.trans.pack;

import android.support.annotation.NonNull;

import com.pax.abl.core.ipacker.PackListener;
import com.pax.gl.packer.Iso8583Exception;
import com.pax.pay.trans.model.TransData;

/**
 * 磁条卡参数下载
 * 
 * @author Steven.W
 * 
 */
public class PackBinDownload extends PackIso8583 {
    public static final String TAG = "PackBinDownload";

    public PackBinDownload(PackListener listener) {
        super(listener);
    }

    @Override
    protected int[] getRequiredFields() {
        return new int[] {41, 42, 60, 62};
    }

    @Override
    protected void setBitData62(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("62", transData.getField62());
    }
}
