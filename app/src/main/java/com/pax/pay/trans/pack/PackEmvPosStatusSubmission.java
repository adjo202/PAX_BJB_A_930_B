package com.pax.pay.trans.pack;

import com.pax.abl.core.ipacker.PackListener;

/**
 * IC卡公钥下载状态上送
 *
 * @author Steven.W
 */
public class PackEmvPosStatusSubmission extends PackIso8583 {

    public PackEmvPosStatusSubmission(PackListener listener) {
        super(listener);
    }

    @Override
    protected int[] getRequiredFields() {
        return new int[]{41, 42, 60, 62};
    }
}
