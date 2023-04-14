/*
 * ============================================================================
 * COPYRIGHT
 *               Pax CORPORATION PROPRIETARY INFORMATION
 *    This software is supplied under the terms of a license agreement or
 *    nondisclosure agreement with Pax Corporation and may not be copied
 *    or disclosed except in accordance with the terms in that agreement.
 *       Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 *  Module Date: 2017-7-17 3:1
 *  Module Author: liliang
 *  Description:
 *  ============================================================================
 */

package com.pax.pay.trans.pack;

import android.support.annotation.NonNull;
import android.util.Log;

import com.pax.abl.core.ipacker.PackListener;
import com.pax.gl.convert.IConvert;
import com.pax.gl.packer.Iso8583Exception;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.utils.AppLog;

import java.nio.ByteBuffer;

/**
 * Created by liliang on 2017/7/17.
 */

public class PackCouponVerify extends PackIso8583 {
    public static final String TAG = "PackCouponVerify";

    public PackCouponVerify(PackListener listener) {
        super(listener);
    }

    @Override
    protected int[] getRequiredFields() {
        return new int[] {2, 3, 4, 7, 11, 12,13, 14, 18, 22, 25,  35, 41, 42, 43, 49, 60, 62, 64};
    }


/**
    @Override
    protected void setBitData32(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("32", transData.getAcqCode());
    }
**/

    @Override
    protected void setBitData62(@NonNull TransData transData) throws Iso8583Exception {
        //sandy : modify here
        String coupon = String.format("%-19s", transData.getCouponNo());
        byte[] byteCoupon = coupon.getBytes();
        int couponLength = byteCoupon.length;
        int couponLengthTotal = byteCoupon.length + 2;


        ByteBuffer c = ByteBuffer.allocate(couponLength + 7);
        c.put("VA".getBytes());
        c.put(String.format("%03d",couponLengthTotal).getBytes());
        c.put( (byte) 0x01 );
        c.put( (byte) couponLength );
        c.put(byteCoupon);
        byte[] couponArray = c.array();
        setBitData("62", couponArray);
    }



}
