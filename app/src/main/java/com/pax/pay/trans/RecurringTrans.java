/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 * Module Date: 2017-5-08
 * Module Author: Barret
 * Description:
 *
 * ============================================================================
 */

package com.pax.pay.trans;

import android.content.Context;
import android.os.Handler;

import com.pax.abl.core.ATransaction;
import com.pax.pay.trans.model.ETransType;

/**
 * Created by xionggd on 2017/5/9.
 */
public class RecurringTrans extends MotoSaleTrans{
    public RecurringTrans(Context context, Handler handler, ATransaction.TransEndListener transListener) {
        super(context, handler, transListener);
        setTransType(ETransType.RECURRING_SALE);
    }
}
