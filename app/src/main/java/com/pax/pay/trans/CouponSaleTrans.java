/*
 * ============================================================================
 * COPYRIGHT
 *               Pax CORPORATION PROPRIETARY INFORMATION
 *    This software is supplied under the terms of a license agreement or
 *    nondisclosure agreement with Pax Corporation and may not be copied
 *    or disclosed except in accordance with the terms in that agreement.
 *       Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 *  Module Date: 2017-7-13 11:5
 *  Module Author: liliang
 *  Description:
 *  ============================================================================
 */

package com.pax.pay.trans;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.device.Device;
import com.pax.eemv.entity.CTransResult;
import com.pax.eemv.enums.ECvmResult;
import com.pax.eemv.enums.ETransResult;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.emv.EmvQr;
import com.pax.pay.emv.clss.ClssTransProcess;
import com.pax.pay.trans.action.ActionClssPreProc;
import com.pax.pay.trans.action.ActionClssProcess;
import com.pax.pay.trans.action.ActionCouponSale;
import com.pax.pay.trans.action.ActionEmvProcess;
import com.pax.pay.trans.action.ActionEnterPin;
import com.pax.pay.trans.action.ActionInputTransData;
import com.pax.pay.trans.action.ActionPrintTransReceipt;
import com.pax.pay.trans.action.ActionSearchCard;
import com.pax.pay.trans.action.ActionSearchCard.CardInformation;
import com.pax.pay.trans.action.ActionSearchCard.ESearchCardUIType;
import com.pax.pay.trans.action.ActionSearchCard.SearchMode;
import com.pax.pay.trans.action.ActionSignature;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.settings.SysParam;
import com.pax.up.bjb.R;


/**
 * Created by liliang on 2017/7/13.
 */

public class CouponSaleTrans extends BaseTrans {

    private static final String TAG = "CouponSaleTrans";

    enum State {
        INPUT_AMOUNT,
        INPUT_COUPON,
        CHECK_CARD,
        ENTER_PIN,
        COUPON_SALE,
        EMV_PROC,
        CLSS_PREPROC,
        CLSS_PROC,
        SIGNATURE,
        PRINT_TICKET
    }

    public CouponSaleTrans(Context context, Handler handler, TransEndListener transListener) {
        super(context, handler, ETransType.COUPON_VERIFY, transListener);
    }

    @Override
    protected void bindStateOnAction() {
        transData.setHeader("609100321301");

        // Input amount
        ActionInputTransData inputAmountAction = new ActionInputTransData(handler, ActionInputTransData.INFO_TYPE_SALE, null);
        inputAmountAction.setTitle(context.getString(R.string.coupon_sale));
        inputAmountAction.setPrompt1(context.getString(R.string.prompt_input_amount));
        inputAmountAction.setInputType1(ActionInputTransData.EInputType.AMOUNT);
        inputAmountAction.setMaxLen1(9);
        inputAmountAction.setMinLen1(0);
        bind(State.INPUT_AMOUNT.toString(), inputAmountAction);

        ActionInputTransData inputCouponAction = new ActionInputTransData(handler, ActionInputTransData
                .INFO_TYPE_SALE, null);
        inputCouponAction.setTitle(context.getString(R.string.coupon_sale));
        inputCouponAction.setPrompt1(context.getString(R.string.prompt_input_coupon));
        inputCouponAction.setInputType1(ActionInputTransData.EInputType.ALPHNUM);
        inputCouponAction.setMaxLen1(19);
        inputCouponAction.setMinLen1(0);
        inputCouponAction.setSupportScan(true);
        bind(State.INPUT_COUPON.toString(), inputCouponAction);

        byte searchCardMode = Component.getCardReadMode(transType);
        ActionSearchCard searchCardAction = new ActionSearchCard(null);
        searchCardAction.setTitle(context.getString(R.string.coupon_sale));
        searchCardAction.setMode(searchCardMode);
        searchCardAction.setAmount(transData.getAmount());
        searchCardAction.setUiType(searchCardMode == SearchMode.TAP ? ESearchCardUIType.QUICKPASS
                : ESearchCardUIType.DEFAULT);
        bind(State.CHECK_CARD.toString(), searchCardAction);

        ActionEmvProcess emvProcessAction = new ActionEmvProcess(handler, transData);
        bind(State.EMV_PROC.toString(), emvProcessAction);

        //clss process action
        ActionClssProcess clssProcessAction = new ActionClssProcess(transData, null);
        bind(State.CLSS_PROC.toString(), clssProcessAction);

        //clss preprocess action
        ActionClssPreProc clssPreProcAction = new ActionClssPreProc(transData, null);
        bind(State.CLSS_PREPROC.toString(), clssPreProcAction);

        ActionEnterPin enterPinAction = new ActionEnterPin(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                ((ActionEnterPin) action).setParam(getCurrentContext(), context.getString(R
                                .string.coupon_sale),
                        transData.getPan(), false, context.getString(R.string.prompt_bankcard_pwd),
                        context.getString(R.string.prompt_no_password), transData.getAmount(),
                        ActionEnterPin.EEnterPinType.ONLINE_PIN, transData.getEnterMode());
            }
        });
        bind(State.ENTER_PIN.toString(), enterPinAction);

        ActionCouponSale couponSaleAction = new ActionCouponSale(transData);
        bind(State.COUPON_SALE.toString(), couponSaleAction);

        // ç­¾åaction
        ActionSignature signatureAction = new ActionSignature(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                ((ActionSignature) action).setParam(getCurrentContext(), transData.getAmount(),
                        Component.genFeatureCode(transData));
            }
        });
        bind(State.SIGNATURE.toString(), signatureAction);

        // æ‰“å°action
        ActionPrintTransReceipt printTransReceiptAction = new ActionPrintTransReceipt(transData, handler);
        bind(State.PRINT_TICKET.toString(), printTransReceiptAction);
        gotoState(State.INPUT_AMOUNT.toString());
    }

    @Override
    public void onActionResult(String currentState, ActionResult result) {
        State state = State.valueOf(currentState);
        if (state != State.SIGNATURE) {
            // actionç»“æžœæ£€æŸ¥ï¼Œå¦‚æžœå¤±è´¥ï¼Œç»“æŸäº¤æ˜“
            int ret = result.getRet();
            if (ret != TransResult.SUCC) {
                transEnd(result);
                return;
            }
        }

        switch (state) {
            case INPUT_AMOUNT:
                onEnterAmount(result);
                break;
            case INPUT_COUPON:
                onInputCoupon(result);
                break;
            case CHECK_CARD:
                onCheckCard(result);
                break;
            case EMV_PROC:
                onEmvProc(result);
                break;
            case CLSS_PREPROC:
                gotoState(State.CLSS_PROC.toString());
                break;
            case CLSS_PROC:
                afterClssProcess(result);
                break;
            case ENTER_PIN:
                onEnterPin(result);
                break;
            case COUPON_SALE:
                toSignOrPrint();
                break;
            case SIGNATURE:
                onSignature(result);
                break;
            case PRINT_TICKET:
            default:
                transEnd(result);
                break;
        }
    }

    protected void onEnterAmount(ActionResult result) {

        String isIndopayMode = FinancialApplication.getSysParam().get(SysParam.INDOPAY_MODE);
        String amount = ((String) result.getData()).replace(",", "");

        ActionSearchCard action = (ActionSearchCard) getAction(State.CHECK_CARD.toString());
        action.setAmount(amount);

        //sandy
        if(SysParam.Constant.YES.equals(isIndopayMode))
            transData.setAmount(String.format("%s00",amount));
        else
            transData.setAmount(amount);
        gotoState(State.INPUT_COUPON.toString());
    }




    protected void onInputCoupon(ActionResult result) {
        //Sandy : Bypass Temporary

        String data = (String) result.getData();
        EmvQr emvQr = EmvQr.decodeEmvQrB64(data);
        if (emvQr == null) {
            normalCouponProcess(data);
        } else {
            emvQrProcess(emvQr);
        }


        //normalCouponProcess("481433347413923883");


    }

    private void normalCouponProcess(String couponNo) {
        if (TextUtils.isEmpty(couponNo) || couponNo.length() > 19) {
            transEnd(new ActionResult(TransResult.ERR_COUPON_NUM, null));
            return;
        }
        transData.setCouponNo(couponNo);
        gotoState(State.CHECK_CARD.toString());
    }

    private void emvQrProcess(EmvQr emvQr) {
        if (emvQr == null) {
            transEnd(new ActionResult(TransResult.ERR_INVALID_EMV_QR, null));
            return;
        }
        if (!emvQr.isUpiAid() || !emvQr.isSupportUplan()) {
            transEnd(new ActionResult(TransResult.ERR_NOT_SUPPORT_TRANS, null));
            return;
        }

        saveQrInfo(emvQr);
        gotoState(State.COUPON_SALE.toString());
    }

    private void saveQrInfo(EmvQr emvQr) {
        transData.setCardSerialNo(emvQr.getCardSeqNum());
        transData.setCouponNo(emvQr.getCouponNum());
        transData.setSendIccData(emvQr.getIccData());
        transData.setPan(emvQr.getPan());
        transData.setExpDate(emvQr.getExpireDate());
        transData.setTrack2(emvQr.getTrackData());
        transData.setEnterMode(TransData.EnterMode.QR);
        transData.setSignFree(true);
    }

    protected void onCheckCard(ActionResult result) {
        CardInformation cardInfo = (CardInformation) result.getData();
        saveCardInfo(cardInfo, transData, true);
        byte mode = cardInfo.getSearchMode();
        if (mode == SearchMode.SWIPE) {
            transData.setSignFree(false);
            gotoState(State.ENTER_PIN.toString());
        } else if (mode == SearchMode.INSERT) {
            gotoState(State.EMV_PROC.toString());
        } else if (mode == SearchMode.TAP) {
            gotoState(State.CLSS_PREPROC.toString());
        }
    }

    protected void onEmvProc(ActionResult result) {
        ETransResult transResult = (ETransResult) result.getData();

        String isIndopayMode = FinancialApplication.getSysParam().get(SysParam.INDOPAY_MODE);
        if(SysParam.Constant.YES.equals(isIndopayMode)){
            transData.setAmount(transData.getAmount().substring(0,transData.getAmount().length()-2));
            transData.setDiscountAmount(transData.getDiscountAmount().substring(0,transData.getDiscountAmount().length()-2));
            transData.setActualPayAmount(transData.getActualPayAmount().substring(0,transData.getActualPayAmount().length()-2));
        }


        Component.emvTransResultProcess(transResult, transData);
        if (transResult == ETransResult.ONLINE_APPROVED) {// è”æœºæ‰¹å‡†
            Log.d(TAG,"Sandy.Coupon.ONLINE_APPROVED Save trans " + transData.getOrigCouponRefNo());
            transData.saveTrans();

            toSignOrPrint();
        } else if (transResult == ETransResult.ARQC) { // è¯·æ±‚è”æœº
            if (!Component.isQpbocNeedOnlinePin() || Component.clssQPSProcess(transData)) {
                transData.setPinFree(true);
                gotoState(State.COUPON_SALE.toString());
                return;
            } else {
                // è¾“å¯†ç 
                transData.setPinFree(false);
                gotoState(State.ENTER_PIN.toString());
            }
        } else {
            emvAbnormalResultProcess(transResult);
        }
    }

    protected void afterClssProcess(ActionResult result) {
        CTransResult transResult = (CTransResult) result.getData();
        // è®¾ç½®äº¤æ˜“ç»“æžœ
        transData.setEmvResult((byte) transResult.getTransResult().ordinal());
        if (transResult.getTransResult() == ETransResult.ABORT_TERMINATED ||
                transResult.getTransResult() == ETransResult.CLSS_OC_DECLINED||
                transResult.getTransResult() == ETransResult.ONLINE_DENIED) { // emv interrupt
            Device.beepErr();
            transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
            return;
        }

        ClssTransProcess.clssTransResultProcess(transResult, FinancialApplication.getClss(),
                transData);
        // å†™äº¤æ˜“è®°å½•
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
    }

    protected void onEnterPin(ActionResult result) {
        String pinBlock = (String) result.getData();
        transData.setPin(pinBlock);
        if (pinBlock != null && pinBlock.length() > 0) {
            transData.setHasPin(true);
        }
        // Send coupon verification pack.
        gotoState(State.COUPON_SALE.toString());
    }

    protected void onSignature(ActionResult result) {
        // ä¿å­˜ç­¾åæ•°æ®
        byte[] signData = (byte[]) result.getData();
        if (signData != null && signData.length > 0) {
            transData.setSignData(signData);
            // æ›´æ–°äº¤æ˜“è®°å½•ï¼Œä¿å­˜ç”µå­ç­¾å
            transData.updateTrans();
        }
        gotoState(State.PRINT_TICKET.toString());
    }

    protected void toSignOrPrint() {
        //Sandy

        if(transData.getEnterMode() == TransData.EnterMode.SWIPE){
            String isIndopayMode = FinancialApplication.getSysParam().get(SysParam.INDOPAY_MODE);
            if(SysParam.Constant.YES.equals(isIndopayMode)){
                transData.setAmount(transData.getAmount().substring(0,transData.getAmount().length()-2));
                transData.setDiscountAmount(transData.getDiscountAmount().substring(0,transData.getDiscountAmount().length()-2));
                transData.setActualPayAmount(transData.getActualPayAmount().substring(0,transData.getActualPayAmount().length()-2));
            }
        }


        if (Component.isSignatureFree(transData)) {// å…ç­¾
            transData.setSignFree(true);
            // æ‰“å°
            gotoState(State.PRINT_TICKET.toString());
        } else {
            // ç”µå­ç­¾å
            transData.setSignFree(false);
            gotoState(State.SIGNATURE.toString());
        }
        transData.updateTrans();

    }

}