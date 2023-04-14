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
import com.pax.eemv.entity.CTransResult;
import com.pax.eemv.enums.ECvmResult;
import com.pax.eemv.enums.ETransResult;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.emv.clss.ClssTransProcess;
import com.pax.pay.trans.action.ActionClssPreProc;
import com.pax.pay.trans.action.ActionClssProcess;
import com.pax.pay.trans.action.ActionDispSingleLineMsg;
import com.pax.pay.trans.action.ActionEmvProcess;
import com.pax.pay.trans.action.ActionEnterPin;
import com.pax.pay.trans.action.ActionEnterPin.EEnterPinType;
import com.pax.pay.trans.action.ActionInputTransData;
import com.pax.pay.trans.action.ActionInputTransData.EInputType;
import com.pax.pay.trans.action.ActionPrintTransReceipt;
import com.pax.pay.trans.action.ActionSearchCard;
import com.pax.pay.trans.action.ActionSearchCard.CardInformation;
import com.pax.pay.trans.action.ActionSearchCard.ESearchCardUIType;
import com.pax.pay.trans.action.ActionSearchCard.SearchMode;
import com.pax.pay.trans.action.ActionSignature;
import com.pax.pay.trans.action.ActionTransOnline;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.ETransType;
import com.pax.settings.currency.Currency;
import com.pax.up.bjb.R;

public class EcSaleTrans extends BaseTrans {

    public EcSaleTrans(Context context, Handler handler, TransEndListener translistener) {
        super(context, handler, ETransType.EC_SALE, translistener);
    }

    @Override
    protected void bindStateOnAction() {
        // 输入金额
        ActionInputTransData amountAction = new ActionInputTransData(handler, ActionInputTransData.INFO_TYPE_SALE, null);
        amountAction.setTitle(context.getString(R.string.ec_sale));
        amountAction.setInfoTypeSale(context.getString(R.string.prompt_input_amount),
                EInputType.AMOUNT, 9, false);
        bind(State.ENTER_AMOUNT.toString(), amountAction);
        // 读卡
        ActionSearchCard searchCardAction = new ActionSearchCard(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionSearchCard) action).setParam(getCurrentContext(), context.getString(R.string.ec_sale),
                        Component.getCardReadMode(ETransType.EC_SALE), transData.getAmount(), null, null,
                        ESearchCardUIType.EC);
            }
        });
        bind(State.CHECK_CARD.toString(), searchCardAction);

        // 输入密码action
        ActionEnterPin enterPinAction = new ActionEnterPin(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionEnterPin) action).setParam(getCurrentContext(), context.getString(R.string.ec_sale),
                        transData.getPan(), true, context.getString(R.string.prompt_bankcard_pwd),
                        context.getString(R.string.prompt_no_password), transData.getAmount(), EEnterPinType.ONLINE_PIN, transData.getEnterMode());
            }
        });
        bind(State.ENTER_PIN.toString(), enterPinAction);

        // 联机Action
        ActionTransOnline onlineAction = new ActionTransOnline(transData);
        bind(State.ONLINE.toString(), onlineAction);

        // emv处理action
        ActionEmvProcess emvProcessAction = new ActionEmvProcess(handler, transData);
        bind(State.EMV_PROC.toString(), emvProcessAction);

        //clss process action
        ActionClssProcess clssProcessAction = new ActionClssProcess(transData, null);
        bind(State.CLSS_PROC.toString(), clssProcessAction);

        //clss preprocess action
        ActionClssPreProc clssPreProcAction = new ActionClssPreProc(transData, null);
        bind(State.CLSS_PREPROC.toString(), clssPreProcAction);

        ActionDispSingleLineMsg displayInfoAction = new ActionDispSingleLineMsg(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                Currency currency = FinancialApplication.getSysParam().getCurrency();
                String content = FinancialApplication.getConvert()
                        .amountMinUnitToMajor(transData.getBalance(),
                                currency.getCurrencyExponent(), true);
                String amount = context.getString(R.string.trans_amount_default, currency.getName(),
                        content);

                ((ActionDispSingleLineMsg) action).setParam(getCurrentContext(), handler,
                        context.getString(R.string.ec_sale), context.getString(R.string.balance_prompt), amount, 3);
            }
        });
        bind(State.BALANCE_DISP.toString(), displayInfoAction);

        // 签名action
        ActionSignature signatureAction = new ActionSignature(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionSignature) action).setParam(getCurrentContext(), transData.getAmount(),
                        Component.genFeatureCode(transData));
            }
        });
        bind(State.SIGNATURE.toString(), signatureAction);

        // 打印action
        ActionPrintTransReceipt printTransReceiptAction = new ActionPrintTransReceipt(transData,
                handler);
        bind(State.PRINT_TICKET.toString(), printTransReceiptAction);

        gotoState(State.ENTER_AMOUNT.toString());

    }

    enum State {

        ENTER_AMOUNT,
        CHECK_CARD,
        ENTER_PIN,
        ONLINE,
        EMV_PROC,
        CLSS_PREPROC,
        CLSS_PROC,
        SIGNATURE,
        BALANCE_DISP,
        PRINT_TICKET
    }

    @Override
    public void onActionResult(String currentState, ActionResult result) {

        State state = State.valueOf(currentState);
        if (state != State.SIGNATURE && state != State.BALANCE_DISP) {
            // action结果检查，如果失败，结束交易
            int ret = result.getRet();
            if (ret != TransResult.SUCC) {
                transEnd(result);
                return;
            }
        }
        switch (state) {
            case ENTER_AMOUNT:// 输入交易金额后续处理
                afterEnterAmount(result);
                break;
            case CHECK_CARD:
                afterCheckCard(result);
                break;
            case ENTER_PIN:
                afterEnterPin(result);
                break;
            case EMV_PROC:
                afterEmvProc(result);
                break;
            case CLSS_PREPROC:
                gotoState(State.CHECK_CARD.toString());
                break;
            case CLSS_PROC:
                CTransResult clssResult = (CTransResult) result.getData();
                afterClssProcess(clssResult);
                break;
            case BALANCE_DISP:
                afterDisplayBalance(result);
                break;
            case ONLINE: // 联机的后续处理
                // 写交易记录
                transData.saveTrans();
                toSignOrPrint();
                break;
            case SIGNATURE:
                afterSignature(result);
                break;
            case PRINT_TICKET:
            default:
                transEnd(result);
                break;
        }

    }

    protected void afterEnterAmount(ActionResult result) {
        // 保存交易金额
        String amount = ((String) result.getData()).replace(".", "");
        transData.setAmount(amount);
        gotoState(State.CLSS_PREPROC.toString());
    }

    protected void afterCheckCard(ActionResult result) {
        CardInformation cardInfo = (CardInformation) result.getData();
        saveCardInfo(cardInfo, transData, false);
        byte mode = cardInfo.getSearchMode();
        // electronic cash only support insert or tap card
        if (mode == SearchMode.INSERT) {
            gotoState(State.EMV_PROC.toString());
        } else {
            gotoState(State.CLSS_PROC.toString());
        }
    }

    protected void afterEnterPin(ActionResult result) {
        String pinBlock = (String) result.getData();
        transData.setPin(pinBlock);

        if (pinBlock != null && pinBlock.length() > 0) {
            transData.setHasPin(true);
        }
        // 电子现金余额不足,转联机消费,重置交易类型
        transData.setTransType(ETransType.SALE.toString());
        // 联机处理
        gotoState(State.ONLINE.toString());
    }

    protected void afterEmvProc(ActionResult result) {
        ETransResult transResult = (ETransResult) result.getData();
        Component.emvTransResultProcess(transResult, transData);
        // 电子现金交易没有简化流程
        if (transResult == ETransResult.ONLINE_APPROVED || transResult == ETransResult.OFFLINE_APPROVED) {
            // 写交易记录
            transData.saveTrans();
            if (transResult == ETransResult.ONLINE_APPROVED) {
                toSignOrPrint();
                return;
            }

            gotoState(State.BALANCE_DISP.toString());
        } else if (transResult == ETransResult.ARQC) {
            // 电子现金金额不足,转联机扣款,非接交易也要判断免密免签
            transData.setTransType(ETransType.SALE.toString()); // 电子现金消费转联机时，就是普通的消费交易了

            if (!Component.isQpbocNeedOnlinePin()) {
                gotoState(State.ONLINE.toString());
                return;
            }

            if (Component.clssQPSProcess(transData)) { // 免密
                transData.setPinFree(true);
                gotoState(State.ONLINE.toString());
            } else {
                // 输密码
                transData.setPinFree(false);
                gotoState(State.ENTER_PIN.toString());
            }

        } else {
            emvAbnormalResultProcess(transResult);
        }
    }

    private void afterClssProcess(CTransResult transResult) {

        // 设置交易结果
        transData.setEmvResult((byte) transResult.getTransResult().ordinal());
        if (transResult.getTransResult() == ETransResult.ABORT_TERMINATED ||
                transResult.getTransResult() == ETransResult.CLSS_OC_DECLINED||
                transResult.getTransResult() == ETransResult.ONLINE_DENIED) { // emv interrupt
            Device.beepErr();
            transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
            return;
        }

        ClssTransProcess.clssTransResultProcess(transResult, FinancialApplication.getClss(), transData);
        // 写交易记录
        transData.saveTrans();

        if (transResult.getCvmResult() == ECvmResult.SIG) {
            transData.setSignFree(false);
            transData.setPinFree(true);
        } else {
            transData.setSignFree(true);
            transData.setPinFree(true);
        }

        if (transResult.getTransResult() == ETransResult.CLSS_OC_APPROVED || transResult.getTransResult() == ETransResult.ONLINE_APPROVED) {
            transData.setIsOnlineTrans(transResult.getTransResult() == ETransResult.ONLINE_APPROVED);
            toSignOrPrint();

        }
        // ETransResult.CLSS_OC_ONLINE_REQUEST, online is handled in the module
    }

    protected void afterDisplayBalance(ActionResult result) {
        // 电子现金交易无需签名
        if (Component.isSignatureFree(transData)) {// 免签
            transData.setSignFree(true);
        } else {
            transData.setSignFree(false);
        }
        transData.updateTrans();
        gotoState(State.PRINT_TICKET.toString());
    }

    protected void afterSignature(ActionResult result) {
        // 保存签名数据
        byte[] signData = (byte[]) result.getData();
        if (signData != null && signData.length > 0) {
            transData.setSignData(signData);
            // 更新交易记录，保存电子签名
            transData.updateTrans();
        }
        gotoState(State.PRINT_TICKET.toString());
    }

    // 判断是否需要电子签名或打印
    private void toSignOrPrint() {
        if (Component.isSignatureFree(transData)) {// 免签
            transData.setSignFree(true);
            // 打印
            gotoState(State.PRINT_TICKET.toString());
        } else {
            // 电子签名
            transData.setSignFree(false);
            gotoState(State.SIGNATURE.toString());
        }
        transData.updateTrans();
    }

}
