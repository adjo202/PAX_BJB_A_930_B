/*******************************************************************************
 * ============================================================================
 * COPYRIGHT
 *               Pax CORPORATION PROPRIETARY INFORMATION
 *    This software is supplied under the terms of a license agreement or
 *    nondisclosure agreement with Pax Corporation and may not be copied
 *    or disclosed except in accordance with the terms in that agreement.
 *       Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 *  Module Date: 17-8-30 下午16:11
 *  Module Author: wangyq
 *  Description:
 *  ============================================================================
 ******************************************************************************/

package com.pax.pay.trans.pack;

import android.support.annotation.NonNull;

import com.pax.abl.core.ipacker.PackListener;
import com.pax.gl.packer.Iso8583Exception;
import com.pax.pay.trans.model.TransData;

/**
 * Created by wangyq on 2017/8/30.
 */

public class PackMotoSale extends PackIso8583 {
    public PackMotoSale(PackListener listener) {
        super(listener);
    }

    @Override
    protected int[] getRequiredFields() {
        return new int[]{2, 3, 4, 11, 14, 22, 23, 25, 26, 35, 36, 41, 42, 49, 52, 53, 55, 60, 62,
                63, 64};
    }

    @Override
    protected void setBitData62(@NonNull TransData transData) throws Iso8583Exception {
        String f62 = "92CV003" + transData.getCardCVN2();
        setBitData("62", f62);
    }
}
