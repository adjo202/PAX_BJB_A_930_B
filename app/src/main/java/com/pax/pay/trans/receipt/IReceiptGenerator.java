/*******************************************************************************
 * ============================================================================
 * COPYRIGHT
 *               Pax CORPORATION PROPRIETARY INFORMATION
 *    This software is supplied under the terms of a license agreement or
 *    nondisclosure agreement with Pax Corporation and may not be copied
 *    or disclosed except in accordance with the terms in that agreement.
 *       Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 *  Module Date: 17-5-22 上午11:24
 *  Module Author: liliang
 *  Description:
 *  ============================================================================
 ******************************************************************************/

package com.pax.pay.trans.receipt;

import android.graphics.Bitmap;

import com.pax.pay.constant.Constants;

/**
 * 凭单生成器
 * 
 * @author Steven.W
 * 
 */
interface IReceiptGenerator {
    public static final int FONT_BIG = 30;
    public static final int FONT_NORMAL = 24;
    public static final int FONT_SMALL = 20;
    public static final int FONT_VERY_SMALL = 10;
    public static final String TYPE_FACE = Constants.FONT_PATH + Constants.FONT_NAME;

    /**
     * 生成凭单
     * 
     * @return
     */
    public Bitmap generate();
}
