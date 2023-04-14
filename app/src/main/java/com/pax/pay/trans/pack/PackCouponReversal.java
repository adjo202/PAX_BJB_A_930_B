/*******************************************************************************
 * ============================================================================
 * COPYRIGHT
 *               Pax CORPORATION PROPRIETARY INFORMATION
 *    This software is supplied under the terms of a license agreement or
 *    nondisclosure agreement with Pax Corporation and may not be copied
 *    or disclosed except in accordance with the terms in that agreement.
 *       Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 *  Module Date: 17-8-30 上午10:11
 *  Module Author: wangyq
 *  Description:
 *  ============================================================================
 ******************************************************************************/
package com.pax.pay.trans.pack;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.pax.abl.core.ipacker.PackListener;
import com.pax.gl.packer.Iso8583Exception;
import com.pax.pay.trans.model.TransData;


public class PackCouponReversal extends PackIso8583 {

    public PackCouponReversal(PackListener listener) {
        super(listener);
    }

    @Override
    protected int[] getRequiredFields() {
        return new int[]{2, 3, 4, 11, 14, 22, 23, 25, 38, 39, 41, 42, 49, 55, 60, 64};
    }

    @Override
    protected void setBitData4(@NonNull TransData transData) throws Iso8583Exception {
        String actualPayAmount = transData.getActualPayAmount();
        if (!TextUtils.isEmpty(actualPayAmount)) {
            setBitData("04", actualPayAmount);
        }
    }
}
