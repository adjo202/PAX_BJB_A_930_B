package com.pax.pay.trans.pack;

import com.pax.abl.core.ipacker.PackListener;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;

public class PackOfflineBat extends PackIso8583 {

    public PackOfflineBat(PackListener listener) {
        super(listener);
    }

    @Override
    protected int[] getRequiredFields() {
        return new int[0];
    }

    @Override
    public byte[] pack(TransData transData) {

        if (transData == null) {
            return null;
        }
        // 当前交易类型，通知类批上送
        String transType = transData.getTransType();
        byte[] buf = null;
        byte[] temp = ETransType.OFFLINE_TRANS_SEND.getpackager(listener).pack(transData);
        if (temp != null) {
            buf = new byte[temp.length - 8];
            System.arraycopy(temp, 0, buf, 0, temp.length - 8);
            // 把0220转成0320
            buf[11] = 0x03;
            // 去掉bit64
            buf[11 + 2 + 8 - 1] &= 0xfe;
        }
        // 恢复交易类型
        transData.setTransType(transType);

        return buf;
    }

}
