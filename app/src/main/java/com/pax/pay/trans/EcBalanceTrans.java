/*******************************************************************************
 * ============================================================================
 * COPYRIGHT
 *               Pax CORPORATION PROPRIETARY INFORMATION
 *    This software is supplied under the terms of a license agreement or
 *    nondisclosure agreement with Pax Corporation and may not be copied
 *    or disclosed except in accordance with the terms in that agreement.
 *       Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 *  Module Date: 17-5-22 上午11:19
 *  Module Author: liliang
 *  Description:
 *  ============================================================================
 ******************************************************************************/

package com.pax.pay.trans;

import android.content.Context;
import android.os.Handler;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.device.Device;
import com.pax.device.DeviceImplNeptune;
import com.pax.eemv.exception.EEmvExceptions;
import com.pax.eemv.exception.EmvException;
import com.pax.jemv.device.DeviceManager;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.constant.Constants;
import com.pax.pay.emv.EmvListenerImpl;
import com.pax.pay.emv.EmvTransProcess;
import com.pax.pay.emv.clss.ClssListenerImpl;
import com.pax.pay.emv.clss.ClssTransProcess;
import com.pax.pay.trans.action.ActionClssPreProc;
import com.pax.pay.trans.action.ActionDispSingleLineMsg;
import com.pax.pay.trans.action.ActionSearchCard;
import com.pax.pay.trans.action.ActionSearchCard.CardInformation;
import com.pax.pay.trans.action.ActionSearchCard.ESearchCardUIType;
import com.pax.pay.trans.action.ActionSearchCard.SearchMode;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.transmit.TransProcessListener;
import com.pax.pay.trans.transmit.TransProcessListenerImpl;
import com.pax.pay.utils.AppLog;
import com.pax.settings.currency.Currency;
import com.pax.up.bjb.R;

public class EcBalanceTrans extends BaseTrans {
    private static final String TAG = "EcBalanceTrans";
    private long ecBalance;

    public EcBalanceTrans(Context context, Handler handler, TransEndListener transListener) {
        super(context, handler, ETransType.EC_QUERY, transListener);
    }

    @Override
    protected void bindStateOnAction() {
        //clss preprocess action
        ActionClssPreProc clssPreProcAction = new ActionClssPreProc(transData, null);
        bind(State.CLSS_PREPROC.toString(), clssPreProcAction);

        // 读卡
        ActionSearchCard searchCardAction = new ActionSearchCard(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                ((ActionSearchCard) action).setParam(getCurrentContext(),
                        context.getString(R.string.ec_trans_liff, context.getString(R.string
                                .trans_balance)),
                        Component.getCardReadMode(ETransType.EC_QUERY), null, null, null,
                        ESearchCardUIType.EC);
            }
        });
        bind(State.CHECK_CARD.toString(), searchCardAction);

        ActionDispSingleLineMsg displayInfoAction = new ActionDispSingleLineMsg(new AAction
                .ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                Currency currency = FinancialApplication.getSysParam().getCurrency();
                String content = FinancialApplication.getConvert().amountMinUnitToMajor(String.valueOf
                                (ecBalance),
                        currency.getCurrencyExponent(), true);
                String amount = context.getString(R.string.trans_amount_default, currency.getName(),
                        content);

                ((ActionDispSingleLineMsg) action).setParam(getCurrentContext(), handler,
                        context.getString(R.string.ec_trans_liff, context.getString(R.string
                                .trans_balance)),
                        context.getString(R.string.balance_prompt), amount, 5);
            }
        });
        bind(State.BALANCE_DISP.toString(), displayInfoAction);

        gotoState(State.CLSS_PREPROC.toString());

    }

    enum State {
        CLSS_PREPROC,
        CHECK_CARD,
        BALANCE_DISP
    }

    @Override
    public void onActionResult(String currentState, ActionResult result) {
        if (!currentState.equals(State.BALANCE_DISP.toString())) {
            int ret = result.getRet();
            if (ret != TransResult.SUCC) {
                transEnd(result);
                return;
            }
        }

        State state = State.valueOf(currentState);
        switch (state) {
            case CLSS_PREPROC:
                gotoState(State.CHECK_CARD.toString());
                break;
            case CHECK_CARD:
                CardInformation cardInfo = (CardInformation) result.getData();
                saveCardInfo(cardInfo, transData, false);
                byte mode = cardInfo.getSearchMode();
                readEcBalance(mode);
                break;
            case BALANCE_DISP:
                transEnd(new ActionResult(TransResult.SUCC, null));
                break;
            default:
                transEnd(result);
                break;
        }
    }

    /**
     * 读电子现金余额
     *
     * @param searchMode
     */
    private void readEcBalance(final byte searchMode) {
        DeviceManager.getInstance().setIDevice(DeviceImplNeptune.getInstance());
        new Thread(new Runnable() {

            @Override
            public void run() {
                TransProcessListener transProcessListener = new TransProcessListenerImpl
                        (TransContext.getInstance()
                        .getCurrentContext());
                EmvListenerImpl emvListener = new EmvListenerImpl(getCurrentContext(),
                        FinancialApplication.getEmv(), handler, transData,
                        transProcessListener);
                EmvTransProcess emvTransProcess = EmvTransProcess.getInstance();

                ClssListenerImpl clssListener = new ClssListenerImpl(getCurrentContext(),
                        FinancialApplication.getClss(), transData, transProcessListener);
                ClssTransProcess clssTransProcess = new ClssTransProcess(FinancialApplication.getClss());

                try {
                    transProcessListener.onShowProgress(getCurrentContext().getString(R.string
                            .process_please_wait), 0);

                    if (searchMode == SearchMode.TAP) {
                        ecBalance = clssTransProcess.getEcBalance(clssListener, transData);
                    } else {
                        ecBalance = emvTransProcess.getEcBalance(emvListener, transData);
                    }
                    transProcessListener.onHideProgress();
                    gotoState(State.BALANCE_DISP.toString());
                } catch (EmvException e) {
                    Device.beepErr();
                    if (e.getErrCode() != EEmvExceptions.EMV_ERR_UNKNOWN.getErrCodeFromBasement()) {
                        transProcessListener.onShowErrMessageWithConfirm(e.getErrMsg(), Constants
                                .FAILED_DIALOG_SHOW_TIME);
                    }
                    transProcessListener.onHideProgress();
                    transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
                    AppLog.e(TAG, "", e);
                }

            }
        }).start();
    }
}
