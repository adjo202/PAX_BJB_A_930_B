/*******************************************************************************
 * ============================================================================
 * COPYRIGHT
 *               Pax CORPORATION PROPRIETARY INFORMATION
 *    This software is supplied under the terms of a license agreement or
 *    nondisclosure agreement with Pax Corporation and may not be copied
 *    or disclosed except in accordance with the terms in that agreement.
 *       Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 *  Module Date: 17-5-22 上午11:22
 *  Module Author: liliang
 *  Description:
 *  ============================================================================
 ******************************************************************************/

package com.pax.pay.trans;

import android.content.Context;
import android.content.DialogInterface;
import android.nfc.Tag;
import android.os.Handler;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.trans.action.ActionSettle;
import com.pax.pay.trans.model.Controller;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.trans.model.TransTotal;
import com.pax.pay.trans.transmit.TransOnline;
import com.pax.pay.trans.transmit.TransProcessListener;
import com.pax.pay.trans.transmit.TransProcessListenerImpl;
import com.pax.settings.SysParam;
import com.pax.up.bjb.R;

import java.util.LinkedHashMap;

public class SettleTrans extends BaseTrans {


    public static final String TAG = "SettleTrans";

    public SettleTrans(Context context, Handler handler, TransEndListener listener) {
        super(context, handler, ETransType.SETTLE, listener);
    }

    @Override
    public void onActionResult(String currentState, ActionResult result) {
        if (result.getRet() != TransResult.SUCC) {
            transEnd(result);
            return;
        }

       // String yesNo = FinancialApplication.getSysParam().get(SysParam.SETTLETC_AUTOLOGOUT);
        if (SysParam.Constant.YES.equals(FinancialApplication.getSysParam().get(SysParam.SETTLETC_AUTOLOGOUT))) {
            dispResult(ETransType.SETTLE.getTransName(), result, new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    // Sandy : Transaction nested transaction, modify transaction running status flag
                    setTransRunning(false);
                    new PosLogout(getCurrentContext(), handler, transListener).execute();
                }
            });

        } else {
            transEnd(new ActionResult(TransResult.SUCC, null));
        }

    }

    @Override
    protected void bindStateOnAction() {

        /*
        TransProcessListenerImpl transProcessListenerImpl = new TransProcessListenerImpl(context);
        transProcessListenerImpl.onUpdateProgressTitle(ETransType.SETTLE.getTransName());
        transProcessListenerImpl.onShowProgress(context.getString(R.string.trans_calculating), Integer.parseInt(FinancialApplication.getSysParam().get(SysParam.COMM_TIMEOUT)));
         */

        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put(context.getString(R.string.merchant_id), FinancialApplication.getSysParam().get(SysParam.MERCH_ID));
        map.put(context.getString(R.string.terminal_id),
                FinancialApplication.getSysParam().get(SysParam.TERMINAL_ID));
        map.put(context.getString(R.string.batch_id),
                String.format("%06d", Long.valueOf(FinancialApplication.getSysParam().get(SysParam.BATCH_NO))));

        TransTotal total = TransTotal.calcTotal();

        ActionSettle settleAction = new ActionSettle(handler,null);



        settleAction.setTitle(context.getString(R.string.trans_settle));
        settleAction.setMap(map);
        settleAction.setTotal(total);
        bind(State.SETTLE.toString(), settleAction);

        gotoState(State.SETTLE.toString());
    }

    enum State {
        SETTLE
    }

}
