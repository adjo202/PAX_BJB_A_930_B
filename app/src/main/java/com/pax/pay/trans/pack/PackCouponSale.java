/*
 * ============================================================================
 * COPYRIGHT
 *               Pax CORPORATION PROPRIETARY INFORMATION
 *    This software is supplied under the terms of a license agreement or
 *    nondisclosure agreement with Pax Corporation and may not be copied
 *    or disclosed except in accordance with the terms in that agreement.
 *       Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 *  Module Date: 2017-8-30 4:7
 *  Module Author: liliang
 *  Description:
 *  ============================================================================
 */

package com.pax.pay.trans.pack;

import android.support.annotation.NonNull;

import com.pax.abl.core.ipacker.PackListener;
import com.pax.dal.entity.ETermInfoKey;
import com.pax.device.Device;
import com.pax.gl.packer.Iso8583Exception;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.trans.model.TransData;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by liliang on 2017/8/30.
 */

public class PackCouponSale extends PackSale {

    @Override
    protected int[] getRequiredFields() {
        return new int[]{2, 3, 4, 7, 11, 12, 13, 14, 18, 22, 23, 25, 26, 35, 36, 41, 42, 43, 47, 49, 52, 53, 54, 55, 60, 62, 63,64};
    }

    public PackCouponSale(PackListener listener) {
        super(listener);
    }

    @Override
    protected void setBitData4(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("4", transData.getActualPayAmount());
    }


    @Override
    protected void setBitData63(@NonNull TransData transData) throws Iso8583Exception {
        JSONObject deviceInfo = Device.getBaseInfo();

        try {
            deviceInfo.put("trxType", "COUPON_SALE");
            deviceInfo.put("couponNo", transData.getCouponNo());
            deviceInfo.put("discountAmount", String.format("%012d", Long.parseLong(transData.getDiscountAmount())));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        setBitData("63",deviceInfo.toString());
    }




}
