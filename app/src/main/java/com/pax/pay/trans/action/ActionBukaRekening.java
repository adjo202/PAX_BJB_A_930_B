/*
 * ============================================================================
 * COPYRIGHT
 *               Pax CORPORATION PROPRIETARY INFORMATION
 *    This software is supplied under the terms of a license agreement or
 *    nondisclosure agreement with Pax Corporation and may not be copied
 *    or disclosed except in accordance with the terms in that agreement.
 *       Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 *  Module Date: 2017-8-9 3:4
 *  Module Author: liliang
 *  Description:
 *  ============================================================================
 */

package com.pax.pay.trans.action;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.pay.trans.TransContext;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.trans.transmit.TransProcessListener;
import com.pax.pay.trans.transmit.TransProcessListenerImpl;
import com.pax.pay.trans.transmit.Transmit;

/**
 * Created by liliang on 2017/8/9.
 */

public class ActionBukaRekening extends AAction {

    private TransProcessListenerImpl transProcessListenerImpl;
    private Context context;
    private TransData mTransData;

    public ActionBukaRekening(TransData data) {
        this(data, null);
        this.mTransData = data;
    }

    public ActionBukaRekening(TransData data, ActionStartListener listener) {
        super(listener);
        mTransData = data;
    }

    @Override
    protected void process() {
        context = TransContext.getInstance().getCurrentContext();
        new Thread(new Runnable() {
            @Override
            public void run() {
                transProcessListenerImpl = new TransProcessListenerImpl(context);
                int ret = Transmit.getInstance().transmit(mTransData, transProcessListenerImpl);
                transProcessListenerImpl.onHideProgress();
                transProcessListenerImpl = null;
                setResult(new ActionResult(ret, null));
                context = null;
            }
        }).start();
    }

}
