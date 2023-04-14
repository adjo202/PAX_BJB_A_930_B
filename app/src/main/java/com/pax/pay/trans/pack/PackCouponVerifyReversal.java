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
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;

public class PackCouponVerifyReversal extends PackIso8583 {

    public PackCouponVerifyReversal(PackListener listener) {
        super(listener);
    }

    @Override
    protected int[] getRequiredFields() {
        return new int[]{2, 3, 4, 7, 11,12,13,18, 22, 25, 37, 38, 41, 42,43, 49, 60, 64};
    }


    protected void setBitData7(@NonNull TransData transData) throws Iso8583Exception {
        //sandy
        //added original bit 7
        if (ETransType.COUPON_VERIFY_VOID.toString().equals(transData.getTransType())) {
            setBitData("7", String.valueOf(transData.getOrigCouponDateTimeTrans()));
        }
    }


    @Override
    protected void setBitData11(@NonNull TransData transData) throws Iso8583Exception {
        if (ETransType.COUPON_VERIFY_VOID.toString().equals(transData.getTransType())) {
            setBitData("11", String.valueOf(transData.getOrigTransNo()));
        } else {
            setBitData("11", String.valueOf(transData.getTransNo()));
        }
    }

    protected void setBitData22(@NonNull TransData transData) throws Iso8583Exception {
        //sandy : need an original value of Enter Mode
        setBitData("22", getInputMethod(transData.getOrigEnterMode(), transData.getOrigHasPin()));
    }

    @Override
    protected void setBitData25(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("25", "63");
    }

    @Override
    protected void setBitData32(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("32", transData.getAcqCode());
    }


    protected void setBitData37(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("37", transData.getOrigCouponRefNo());
    }




    @Override
    protected void setBitData62(@NonNull TransData transData) throws Iso8583Exception {
        String couponNo = transData.getCouponNo();
        if (!TextUtils.isEmpty(couponNo)) {
            setBitData("62", String.format("%19s", couponNo));
        }
    }
}
