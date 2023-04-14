/*
 * ============================================================================
 * COPYRIGHT
 *               Pax CORPORATION PROPRIETARY INFORMATION
 *    This software is supplied under the terms of a license agreement or
 *    nondisclosure agreement with Pax Corporation and may not be copied
 *    or disclosed except in accordance with the terms in that agreement.
 *       Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 *  Module Date: 2017-8-30 4:48
 *  Module Author: liliang
 *  Description:
 *  ============================================================================
 */

package com.pax.pay.trans.pack;

import android.support.annotation.NonNull;

import com.pax.abl.core.ipacker.PackListener;
import com.pax.gl.packer.Iso8583Exception;
import com.pax.pay.trans.model.TransData;

/**
 * Created by liliang on 2017/8/30.
 */

public class PackCouponSaleVoid extends PackSaleVoid {

    public PackCouponSaleVoid(PackListener listener) {
        super(listener);
    }

    @Override
    protected void setBitData4(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("4", transData.getActualPayAmount());
    }

    @Override
    protected void setBitData7(@NonNull TransData transData) throws Iso8583Exception {
        //sandy
        //added original bit 7
        setBitData("7", String.valueOf(transData.getDateTimeTrans()));
    }


}
