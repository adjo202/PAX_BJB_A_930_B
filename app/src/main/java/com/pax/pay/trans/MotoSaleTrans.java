/*
 * ============================================================================
 * COPYRIGHT
 *               Pax CORPORATION PROPRIETARY INFORMATION
 *    This software is supplied under the terms of a license agreement or
 *    nondisclosure agreement with Pax Corporation and may not be copied
 *    or disclosed except in accordance with the terms in that agreement.
 *       Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 *  Module Date: 17-5-22 上午11:30
 *  Module Author: Richard
 *  Description:
 *  ============================================================================
 */

package com.pax.pay.trans;

import android.content.Context;
import android.os.Handler;

import com.pax.abl.core.ActionResult;
import com.pax.device.Device;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.trans.action.ActionSearchCard;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.utils.ToastUtils;
import com.pax.settings.SysParam;
import com.pax.up.bjb.R;


/**
 * Created by Richard on 2017/5/5.
 */

public class MotoSaleTrans extends SaleTrans {

    public MotoSaleTrans(Context context, Handler handler, TransEndListener transListener) {
        super(context, handler, null, false, transListener);
        setTransType(ETransType.MOTO_SALE);
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
            case ENTER_AMOUNT:
                // 保存交易金额
                String amount = ((String) result.getData()).replace(".", "");
                transData.setAmount(amount);
                gotoState(State.CHECK_CARD.toString());
                break;
            case CHECK_CARD: // 检测卡的后续处理
                ActionSearchCard.CardInformation cardInfo = (ActionSearchCard.CardInformation)
                        result.getData();
                saveCardInfo(cardInfo, transData, true);
                gotoState(State.ENTER_INFO.toString());
                break;
            case ENTER_INFO: // 检测卡的后续处理
                afterEnterInfo(result);
                break;
            case ENTER_TIP:
                //save tip amount
                afterEnterTip(result);
                break;
            case ONLINE: // 联机的后续处理
                // 写交易记录
                transData.saveTrans();
                // 判断是否需要电子签名或打印
                toSignOrPrint();
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

    private void afterEnterTip(ActionResult result) {
        String tipAmount = ((String) result.getData()).replace(".", "");
        long longTipAmount = Long.parseLong(tipAmount);
        long longAmount = Long.parseLong(transData.getAmount());

        if ((longTipAmount * 100) > (longAmount * Long.parseLong(FinancialApplication.getSysParam()
                .get(SysParam.TIP_RATE)))) {
            Device.beepErr();
            ToastUtils.showMessage(context, context.getString(R.string.prompt_amount_over_limit));
            gotoState(State.ENTER_TIP.toString());
            return;
        } else {
            transData.setTipAmount(tipAmount);
            transData.setAmount(String.valueOf(longAmount + longTipAmount));
        }
        gotoState(State.ONLINE.toString());
    }

    private void afterEnterInfo(ActionResult result) {
        String info = (String) result.getData();
        transData.setCardCVN2(info);
        if (FinancialApplication.getSysParam().get(SysParam.SUPPORT_TIP).equals(SysParam
                .Constant.NO)) {
            gotoState(State.ONLINE.toString());
        } else {
            gotoState(State.ENTER_TIP.toString());
        }
    }

    private void afterSignature(ActionResult result) {
        // 保存签名数据
        byte[] signData = (byte[]) result.getData();

        if (signData != null && signData.length > 0) {
            transData.setSignData(signData);
            // 更新交易记录，保存电子签名
            transData.updateTrans();
        }
        gotoState(State.PRINT_TICKET.toString());
    }
}
