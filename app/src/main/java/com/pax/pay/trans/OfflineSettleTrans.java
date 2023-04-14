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

import android.content.Context;
import android.os.Handler;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.trans.action.ActionInputTransData;
import com.pax.pay.trans.action.ActionInputTransData.EInputType;
import com.pax.pay.trans.action.ActionPrintTransReceipt;
import com.pax.pay.trans.action.ActionSearchCard;
import com.pax.pay.trans.action.ActionSearchCard.CardInformation;
import com.pax.pay.trans.action.ActionSearchCard.ESearchCardUIType;
import com.pax.pay.trans.action.ActionSearchCard.SearchMode;
import com.pax.pay.trans.action.ActionSelectOption;
import com.pax.pay.trans.action.ActionSignature;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.OptionModel;
import com.pax.pay.trans.model.TransData.AuthMode;
import com.pax.settings.SysParam;
import com.pax.up.bjb.R;

import java.util.ArrayList;

/**
 * 离线结算
 */

public class OfflineSettleTrans extends BaseTrans {

    public OfflineSettleTrans(Context context, Handler handler, TransEndListener transListener) {
        super(context, handler, ETransType.OFFLINE_SETTLE, transListener);
    }

    @Override
    protected void bindStateOnAction() {
        // 输入金额
        ActionInputTransData amountAction = new ActionInputTransData(handler,
                ActionInputTransData.INFO_TYPE_SALE, null);
        amountAction.setTitle(context.getString(R.string.offline_settle));
        amountAction.setInfoTypeSale(context.getString(R.string.prompt_input_amount),
                EInputType.AMOUNT, 9, false);
        bind(State.ENTER_AMOUNT.toString(), amountAction);

        // 读卡
        ActionSearchCard searchCardAction = new ActionSearchCard(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                ((ActionSearchCard) action).setParam(getCurrentContext(), context.getString(R
                                .string.offline_settle),
                        SearchMode.KEYIN, transData.getAmount(), null, null,
                        ESearchCardUIType.DEFAULT,
                        context.getString(R.string.prompt_card_num_manual));
            }
        });
        bind(State.CHECK_CARD.toString(), searchCardAction);

        // 选择授权模式
        ArrayList<String> list = new ArrayList<>();
        list.add(context.getString(R.string.auth_mode_pos));
        list.add(context.getString(R.string.auth_mode_phone));
        if (FinancialApplication.getSysParam().get(SysParam.SUPPORT_SMALL_AUTH).equals
                (SysParam.Constant.YES)) {
            list.add(context.getString(R.string.auth_mode_small));
        }
        ActionSelectOption selectAuthModeAction = new ActionSelectOption(handler, null);
        selectAuthModeAction.setTitle(context.getString(R.string.offline_settle));
        selectAuthModeAction.setSubTitle(context.getString(R.string.select_auth_mode));
        selectAuthModeAction.setNameList(list);
        bind(State.SELECT_MODE.toString(), selectAuthModeAction);

        // 输入原授权码
        ActionInputTransData authCodeAction = new ActionInputTransData(handler, ActionInputTransData.INFO_TYPE_SALE,
                null);
        authCodeAction.setTitle(context.getString(R.string.offline_settle));
        authCodeAction.setInfoTypeSale(context.getString(R.string.input_orig_auth_code),
                EInputType.ALPHNUM, 6, 1, false);
        bind(State.INPUT_AUTH_CODE.toString(), authCodeAction);

        // 输入授权机构代码
        ActionInputTransData authInsCodeAction = new ActionInputTransData(handler, ActionInputTransData
                .INFO_TYPE_SALE, null);
        authInsCodeAction.setTitle(context.getString(R.string.offline_settle));
        authInsCodeAction.setInfoTypeSale(context.getString(R.string.input_auth_inst_code),
                EInputType.NUM, 11, 11, false);
        bind(State.INPUT_AUTH_INS_CODE.toString(), authInsCodeAction);

        // 输入国际信用卡组织代码
        ArrayList<String> options = new ArrayList<>();
        options.add(context.getString(R.string.cup));
        options.add(context.getString(R.string.vis));
        options.add(context.getString(R.string.mcc));
        options.add(context.getString(R.string.mae));
        options.add(context.getString(R.string.jcb));
        options.add(context.getString(R.string.dcc));
        options.add(context.getString(R.string.amx));
        ActionSelectOption orgCodeAction = new ActionSelectOption(handler, null);
        orgCodeAction.setTitle(context.getString(R.string.offline_settle));
        orgCodeAction.setSubTitle(context.getString(R.string.select_inter_code));
        orgCodeAction.setNameList(options);
        bind(State.INPUT_ORG_CODE.toString(), orgCodeAction);

        // 电子签名
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
        SELECT_MODE,
        INPUT_AUTH_CODE,
        INPUT_AUTH_INS_CODE,
        INPUT_ORG_CODE,
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
            case ENTER_AMOUNT:
                // 保存交易金额
                String amount = ((String) result.getData()).replace(".", "");
                transData.setAmount(amount);
                gotoState(State.CHECK_CARD.toString());
                break;
            case CHECK_CARD:
                CardInformation cardInfo = (CardInformation) result.getData();
                saveCardInfo(cardInfo, transData, false);
                // 选择授权方式
                gotoState(State.SELECT_MODE.toString());
                break;
            case SELECT_MODE:
                afterSelectMode(result);
                break;
            case INPUT_AUTH_CODE:
                String authCode = (String) result.getData();
                transData.setAuthCode(authCode);
                // 选择国际信用卡组织代码
                gotoState(State.INPUT_ORG_CODE.toString());
                break;
            case INPUT_AUTH_INS_CODE:
                String authInsCode = (String) result.getData();
                transData.setAuthInsCode(authInsCode);
                // 输入原授权码
                gotoState(State.INPUT_AUTH_CODE.toString());
                break;
            case INPUT_ORG_CODE:
                afterInputOrgCode(result);
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

    private void afterSelectMode(ActionResult result) {
        OptionModel option = (OptionModel) result.getData();
        String authMode = option.getId();
        transData.setAuthMode(authMode);

        if (authMode.equals(AuthMode.POS)) { // POS
            // 输入原授权码
            gotoState(State.INPUT_AUTH_CODE.toString());
        } else if (authMode.equals(AuthMode.PHONE)) { // 电话

            // 输入授权机构代码
            gotoState(State.INPUT_AUTH_INS_CODE.toString());
        } else if (authMode.equals(AuthMode.SMALL_GEN_AUTH)) { // 小额代授权
            // 选择国际信用卡组织代码
            gotoState(State.INPUT_ORG_CODE.toString());
        }
    }

    private void afterInputOrgCode(ActionResult result) {
        OptionModel model = (OptionModel) result.getData();
        String interOrgCode = model.getContent();
        transData.setInterOrgCode(interOrgCode);
        // 保存记录
        Component.incTransNo();
        transData.saveTrans();
        gotoState(State.SIGNATURE.toString());
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
