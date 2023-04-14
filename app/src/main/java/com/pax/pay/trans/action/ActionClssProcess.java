/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 * Module Date: 2017-7-27
 * Module Author: wangyq
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.trans.action;

import android.content.Context;
import android.util.Log;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.device.DeviceImplNeptune;
import com.pax.eemv.IClss;
import com.pax.eemv.entity.CTransResult;
import com.pax.jemv.device.DeviceManager;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.constant.Constants;
import com.pax.pay.emv.clss.ClssListenerImpl;
import com.pax.pay.emv.clss.ClssTransProcess;
import com.pax.pay.trans.TransContext;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.trans.model.TransData.EnterMode;
import com.pax.pay.trans.transmit.TransProcessListener;
import com.pax.pay.trans.transmit.TransProcessListenerImpl;
import com.pax.up.bjb.R;


public class ActionClssProcess extends AAction {
    private Context context;
    private IClss clss;
    private TransData transData;
    private TransProcessListener transProcessListener;
    private ClssListenerImpl clssListener;

    public ActionClssProcess(ActionStartListener listener) {
        super(listener);
    }

    public ActionClssProcess(TransData transData, ActionStartListener listener) {
        super(listener);
        this.transData = transData;
    }

    public void setParam(Context context, TransData transData) {
        this.context = context;
        this.transData = transData;
    }

    @Override
    protected void process() {
        DeviceManager.getInstance().setIDevice(DeviceImplNeptune.getInstance());
        this.context = TransContext.getInstance().getCurrentContext();
        clss = FinancialApplication.getClss();
        transProcessListener = new TransProcessListenerImpl(context);
        clssListener = new ClssListenerImpl(context, clss, transData, transProcessListener);
        new  Thread(new Runnable() {
            @Override
            public void run() {
                if (transData.getEnterMode() == EnterMode.QPBOC) {
                    transProcessListener.onShowProgress(context.getString(R.string.wait_process), 0);
                }
                ClssTransProcess clssTransProcess = new ClssTransProcess(clss);
                try {
                    CTransResult result = clssTransProcess.transProcess(transData, clssListener);
                    transProcessListener.onHideProgress();
                    setResult(new ActionResult(TransResult.SUCC, result));
                } catch (Exception e) {
                    Log.e("ActionClssProcess", "", e);
                    transProcessListener.onShowNormalMessageWithConfirm(e.getMessage(),
                            Constants.FAILED_DIALOG_SHOW_TIME);
                    setResult(new ActionResult(TransResult.ERR_ABORTED, null));
                } finally {
                    clss.setListener(null);
                }
            }
        }).start();
    }
}
