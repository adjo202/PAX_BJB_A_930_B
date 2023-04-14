/*******************************************************************************
 * ============================================================================
 * COPYRIGHT
 *               Pax CORPORATION PROPRIETARY INFORMATION
 *    This software is supplied under the terms of a license agreement or
 *    nondisclosure agreement with Pax Corporation and may not be copied
 *    or disclosed except in accordance with the terms in that agreement.
 *       Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 *  Module Date: 17-5-22 上午11:23
 *  Module Author: liliang
 *  Description:
 *  ============================================================================
 ******************************************************************************/

package com.pax.pay.trans;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.trans.action.ActionDispTransDetail;
import com.pax.pay.trans.action.ActionInputPasword;
import com.pax.pay.trans.action.ActionInputTransData;
import com.pax.pay.trans.action.ActionInputTransData.EInputType;
import com.pax.pay.trans.action.ActionPrintTransReceipt;
import com.pax.pay.trans.action.ActionSignature;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.trans.model.TransData.ETransStatus;
import com.pax.settings.SysParam;
import com.pax.settings.currency.Currency;
import com.pax.up.bjb.R;

import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;

/**
 * 结算调整
 * 
 */
@SuppressLint("SimpleDateFormat")
public class SettleAdjustTrans extends BaseTrans {
    public static final String TAG = "SettleAdjustTrans";

    private TransData origTransData;

    public SettleAdjustTrans(Context context, Handler handler, TransEndListener transEndListener) {
        super(context, handler, ETransType.SETTLE_ADJUST, transEndListener);
    }

    @Override
    protected void bindStateOnAction() {
        //输入主管密码
        ActionInputPasword inputPaswordAction = new ActionInputPasword(handler, 6,
                context.getString(R.string.prompt_director_pwd), null);
        bind(State.INPUT_PWD.toString(), inputPaswordAction);

        ActionInputTransData enterTransNoAction = new ActionInputTransData(handler, ActionInputTransData
                .INFO_TYPE_SALE, null);
        enterTransNoAction.setTitle(context.getString(R.string.settle_adjust));
        enterTransNoAction.setInfoTypeSale(context.getString(R.string.prompt_input_transno),
                EInputType.NUM, 6, 1, false);
        bind(State.ENTER_TRANSNO.toString(), enterTransNoAction);

        // 确认信息
        ActionDispTransDetail confirmInfoAction = new ActionDispTransDetail(handler, context.getString(R.string
                .settle_adjust));
        bind(State.TRANS_DETAIL.toString(), confirmInfoAction);

        // 输入金额
        ActionInputTransData amountAction = new ActionInputTransData(handler, ActionInputTransData.INFO_TYPE_SALE, null);
        amountAction.setTitle(context.getString(R.string.settle_adjust));
        String prompt;
        if (ETransType.SALE.toString().equals(origTransData.getTransType())) {
            prompt = context.getString(R.string.prompt_input_tip_amount);
        } else {
            prompt = context.getString(R.string.prompt_input_adjust_amount);
        }
        amountAction.setInfoTypeSale(prompt, EInputType.AMOUNT, 9, false);
        bind(State.ENTER_AMOUNT.toString(), amountAction);

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

        gotoState(State.INPUT_PWD.toString());
    }

    enum State {
        INPUT_PWD,
        ENTER_TRANSNO,
        TRANS_DETAIL,
        ENTER_AMOUNT,
        SIGNATURE,
        PRINT_TICKET
    }

    @Override
    public void onActionResult(String currentState, ActionResult result) {
        State state = State.valueOf(currentState);
        if (state != State.SIGNATURE) {
            // action结果检查，如果失败，结束交易
            int ret = result.getRet();
            if (ret != TransResult.SUCC) {
                transEnd(result);
                return;
            }
        }

        switch (state) {
            case INPUT_PWD:
                afterInputPwd(result);
                break;
            case ENTER_TRANSNO:
                String content = (String) result.getData();
                long transNo = 0;
                transNo = Long.parseLong(content);
                validateOrigTransData(transNo);
                break;
            case TRANS_DETAIL:
                gotoState(State.ENTER_AMOUNT.toString());
                break;
            case ENTER_AMOUNT:
                afterEnterAmount(result);
                break;
            case SIGNATURE:
                afterSignature(result);
                break;
            case PRINT_TICKET:
            default:
                // 交易结束
                transEnd(result);
                break;
        }
    }

    // 检查原交易信息
    private void validateOrigTransData(long origTransNo) {
        origTransData = TransData.readTrans(origTransNo);
        if (origTransData == null) {
            // 交易不存在
            transEnd(new ActionResult(TransResult.ERR_NO_ORIG_TRANS, null));
            return;
        }

        // 非消费或离线结算交易不能调整
        String trType = origTransData.getTransType();
        if (!trType.equals(ETransType.SALE.toString()) && !trType.equals(ETransType.OFFLINE_SETTLE.toString())) {
            transEnd(new ActionResult(TransResult.ERR_ADJUST_UNSUPPORT, null));
            return;
        }

        // 消费交易小费开关未打开
        if (trType.equals(ETransType.SALE.toString())
                && FinancialApplication.getSysParam().get(SysParam.SUPPORT_TIP).equals(SysParam.Constant.NO)) {
                transEnd(new ActionResult(TransResult.ERR_NOT_SUPPORT_TRANS, null));
                return;
        }

        // 非外卡不能调整
        String orgCode = origTransData.getInterOrgCode();
        if (orgCode == null || "CUP".equals(orgCode)) {
            transEnd(new ActionResult(TransResult.ERR_ADJUST_UNSUPPORT, null));
            return;
        }

        // 已撤销/调整交易不能调整
        String trStatus = origTransData.getTransState();
        if (trStatus.equals(ETransStatus.VOID.toString())) {
            transEnd(new ActionResult(TransResult.ERR_HAS_VOID, null));
            return;
        } else if (trStatus.equals(ETransStatus.ADJUST.toString())) {
            transEnd(new ActionResult(TransResult.ERR_ADJUST_UNSUPPORT, null));
            return;
        }

        copyOrigTransData();
        ActionDispTransDetail action = (ActionDispTransDetail)getAction(State.TRANS_DETAIL
                .toString());
        action.setTransData(origTransData);
        gotoState(State.TRANS_DETAIL.toString());
    }

    // 设置原交易记录
    private void copyOrigTransData() {
        transData.setAmount(origTransData.getAmount());
        transData.setOrigBatchNo(origTransData.getBatchNo());
        transData.setOrigAuthCode(origTransData.getAuthCode());
        transData.setOrigRefNo(origTransData.getRefNo());
        transData.setOrigTransNo(origTransData.getTransNo());
        transData.setAuthCode(origTransData.getAuthCode());
        transData.setPan(origTransData.getPan());
        transData.setExpDate(origTransData.getExpDate());
        transData.setOrigDate(origTransData.getDate());
        transData.setAuthMode(origTransData.getAuthMode());
        transData.setAuthInsCode(origTransData.getAuthInsCode());
        transData.setInterOrgCode(origTransData.getInterOrgCode());
        transData.setEnterMode(TransData.EnterMode.MANUAL);
    }

    private void afterInputPwd(ActionResult result){
        String data = (String) result.getData();
        if (!data.equals(FinancialApplication.getSysParam().get(SysParam.SEC_MNGPWD))) {
            transEnd(new ActionResult(TransResult.ERR_PASSWORD, null));
            return;
        }
        gotoState(State.ENTER_TRANSNO.toString());
    }

    private void afterEnterAmount(ActionResult result){
        String amount = ((String) result.getData()).replace(".", "");
        long adjustAmount;
        long originalAmount;
        try {
            adjustAmount = Long.parseLong(amount);
            originalAmount = Long.parseLong(origTransData.getAmount());
            if (adjustAmount <= originalAmount) {
                transEnd(new ActionResult(TransResult.ERR_ADJUST_AMOUNT, null));
                return;
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "", e);
            transEnd(new ActionResult(TransResult.ERR_ADJUST_UNSUPPORT, null));
            return;
        }

        if (origTransData.getTransType().equals(ETransType.SALE.toString())) {
            if ("2" != FinancialApplication.getSysParam().get(SysParam.TIP_MODE)
                    || origTransData.getTipSupport() == 0
                    || origTransData.getIsOffUploadState()) {
                transEnd(new ActionResult(TransResult.ERR_ADJUST_UNSUPPORT, null));
                return;
            }

            String tipRate = FinancialApplication.getSysParam().get(SysParam.TIP_RATE);
            long tipAmount = adjustAmount - originalAmount;
            long amountMax;
            try {
                amountMax = originalAmount * Long.parseLong(tipRate);
                if (tipAmount * 100 > amountMax) {// 小费金额超限
                    transEnd(new ActionResult(TransResult.ERR_AMOUNT, null));
                    return;
                }

                transData.setAuthMode("00");     //新小费模式需要此域，原模式没有此域，故赋值为00
                transData.setTransType(ETransType.SETTLE_ADJUST_TIP.toString());
                transData.setAmount(String.valueOf(adjustAmount));
                transData.setTipAmount(String.valueOf(tipAmount));

                // 写交易记录
                Component.incTransNo();
                transData.saveTrans();
                // 更新原交易记录
                origTransData.setTransState(ETransStatus.ADJUST.toString());
                origTransData.updateTrans();
            } catch (NumberFormatException e) {
                Log.e(TAG, "", e);
                transEnd(new ActionResult(TransResult.ERR_ADJUST_UNSUPPORT, null));
                return;
            }
        } else {
            if (origTransData.getIsOffUploadState()) {
                // 生成新交易,使用结算调整报文
                transData.setAmount(String.valueOf(adjustAmount));
                // 保存记录
                Component.incTransNo();
                transData.saveTrans();
                // 更新原交易记录
                origTransData.setTransState(ETransStatus.ADJUST.toString());
                origTransData.setIsAdjustAfterUpload(true);
                origTransData.updateTrans();
            } else {
                // 尚未上送,使用离线结算报文
                // 现直接使用原交易数据
                transData = origTransData;
                transData.setOper(TransContext.getInstance().getOperID());
                transData.setAmount(String.valueOf(adjustAmount));
                transData.setTransState(ETransStatus.ADJUST.toString());
                transData.updateTrans();
            }
        }
        gotoState(State.SIGNATURE.toString());
    }

    private void afterSignature(ActionResult result){
        // 保存签名数据
        byte[] signData = (byte[]) result.getData();

        if (signData != null && signData.length > 0) {
            transData.setSignData(signData);
            // 更新交易记录，保存电子签名
            transData.updateTrans();
        }
        gotoState(State.PRINT_TICKET.toString());
    }

    private LinkedHashMap<String,String>copyDispData(){
        Currency currency = FinancialApplication.getSysParam().getCurrency();
        String transType = ETransType.valueOf(origTransData.getTransType()).getTransName();
        String formater = FinancialApplication.getConvert().amountMinUnitToMajor(
                String.valueOf(Long.parseLong(origTransData.getAmount())),
                currency.getCurrencyExponent(), true);
        String amount = context.getString(R.string.trans_amount_default, currency.getName(), formater);

        // 日期时间
        String date = transData.getDate();
        String time = transData.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        String yearDate = sdf.format(new java.util.Date());

        String temp = yearDate.substring(0, 4) + "/" + date.substring(0, 2) + "/" + date.substring(2, 4) + " "
                + time.substring(0, 2) + ":" + time.substring(2, 4) + ":" + time.substring(4);

        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        map.put(context.getString(R.string.trans_type), transType);
        map.put(context.getString(R.string.trans_amount), amount);
        map.put(context.getString(R.string.trans_card_no), origTransData.getPan());
        map.put(context.getString(R.string.detail_auth_code), origTransData.getAuthCode());
        map.put(context.getString(R.string.detail_trans_ref_no), origTransData.getRefNo());
        map.put(context.getString(R.string.detail_trans_no), String.format("%06d", origTransData.getTransNo()));
        map.put(context.getString(R.string.trans_date), temp);
        return map;
    }
}
