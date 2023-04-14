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
import com.pax.eemv.enums.ETransResult;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.emv.clss.ClssTransProcess;
import com.pax.pay.trans.action.ActionClssPreProc;
import com.pax.pay.trans.action.ActionClssProcess;
import com.pax.pay.trans.action.ActionEmvProcess;
import com.pax.pay.trans.action.ActionInputEcRefundData;
import com.pax.pay.trans.action.ActionInputPasword;
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
import com.pax.settings.SysParam;
import com.pax.settings.currency.Currency;
import com.pax.up.bjb.R;
import com.pax.view.dialog.CustomAlertDialog;
import com.pax.view.dialog.CustomAlertDialog.OnCustomClickListener;


public class EcRefundTrans extends BaseTrans {

    public EcRefundTrans(Context context, Handler handler, TransEndListener translistener) {
        super(context, handler, ETransType.EC_REFUND, translistener);
    }

    @Override
    protected void bindStateOnAction() {
        ActionInputPasword inputPaswordAction = new ActionInputPasword(handler, 6,
                context.getString(R.string.prompt_director_pwd), null);
        bind(State.INPUT_PWD.toString(), inputPaswordAction);

        // 输入金额
        ActionInputTransData amountAction = new ActionInputTransData(handler,
                ActionInputTransData.INFO_TYPE_SALE, null);
        amountAction.setTitle(context.getString(R.string.ec_offline_refund));
        amountAction.setInfoTypeSale(context.getString(R.string.prompt_input_refund_amount),
                EInputType.AMOUNT, 9, false);
        bind(State.ENTER_AMOUNT.toString(), amountAction);

        // 输入原交易信息
        ActionInputEcRefundData ecInfoAction = new ActionInputEcRefundData(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                ((ActionInputEcRefundData) action).setParam(context, handler,
                        context.getString(R.string.ec_trans_liff, context.getString(R.string.ec_offline_refund)));
            }
        });
        bind(State.ENTER_INFO.toString(), ecInfoAction);

        // 寻卡
        ActionSearchCard searchCardAction = new ActionSearchCard(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionSearchCard) action).setParam(getCurrentContext(),
                        context.getString(R.string.ec_trans_liff, context.getString(R.string.ec_offline_refund)),
                        Component.getCardReadMode(ETransType.EC_REFUND), transData.getAmount(), null, null,
                        ESearchCardUIType.EC);
            }
        });
        bind(State.CHECK_CARD.toString(), searchCardAction);

        // emv处理action
        ActionEmvProcess emvProcessAction = new ActionEmvProcess(handler, transData);
        bind(State.EMV_PROC.toString(), emvProcessAction);

        //clss preprocess action
        ActionClssPreProc clssPreProcAction = new ActionClssPreProc(transData, null);
        bind(State.CLSS_PREPROC.toString(), clssPreProcAction);

        //clss process action
        ActionClssProcess clssProcessAction = new ActionClssProcess(transData, null);
        bind(State.CLSS_PROC.toString(), clssProcessAction);

        // 联机action
        ActionTransOnline transOnlineAction = new ActionTransOnline(transData);

        bind(State.ONLINE.toString(), transOnlineAction);

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

        gotoState(State.CLSS_PREPROC.toString());

    }

    enum State {
        INPUT_PWD,
        ENTER_AMOUNT,
        ENTER_INFO,
        CHECK_CARD,
        ONLINE,
        EMV_PROC,
        CLSS_PREPROC,
        CLSS_PROC,
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
            case ENTER_AMOUNT:
                afterEnterAmount(result);
                break;
            case ENTER_INFO:
                afterEnterInfo(result);
                break;
            case CHECK_CARD:
                afterCheckCard(result);
                break;
            case ONLINE:
                // 写交易记录
                transData.saveTrans();
                gotoState(State.PRINT_TICKET.toString());
                break;
            case EMV_PROC:
                afterEmvProc(result);
                break;
            case CLSS_PREPROC:
                afterClssPreProc();
                break;
            case CLSS_PROC:
                afterClssProc(result);
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

    protected void afterInputPwd(ActionResult result) {
        String data = (String) result.getData();
        if (!data.equals(FinancialApplication.getSysParam().get(SysParam.SEC_MNGPWD))) {
            transEnd(new ActionResult(TransResult.ERR_PASSWORD, null));
            return;
        }
        gotoState(State.ENTER_AMOUNT.toString());
    }

    protected void afterEnterAmount(ActionResult result) {
        long amount = Long.parseLong(((String) result.getData()).replace(".", ""));
        long amountMax = Long.parseLong(FinancialApplication.getSysParam().get(SysParam.OTHTC_REFUNDLIMT)) * 100;

        if (amount > amountMax) {
            transEnd(new ActionResult(TransResult.ERR_AMOUNT, null));
            return;
        }

        transData.setAmount(String.valueOf(amount));
        showDialog();
    }

    private void showDialog() {
        // 确定输入金额
        handler.post(new Runnable() {
            @Override
            public void run() {
                Currency currency = FinancialApplication.getSysParam().getCurrency();
                CustomAlertDialog dialog = new CustomAlertDialog(getCurrentContext(),
                        CustomAlertDialog.NORMAL_TYPE);
                dialog.setTitleText(context.getString(R.string.ec_trans_liff,
                        context.getString(R.string.ec_offline_refund)));
                String amontStr = FinancialApplication.getConvert().
                        amountMinUnitToMajor(transData.getAmount(),
                                currency.getCurrencyExponent(), true);
                dialog.setContentText(context.getString(R.string.trans_amount_info, amontStr));
                dialog.setCanceledOnTouchOutside(false);

                dialog.show();

                dialog.showCancelButton(true);
                dialog.showConfirmButton(true);
                dialog.setCancelClickListener(new OnCustomClickListener() {

                    @Override
                    public void onClick(CustomAlertDialog alertDialog) {
                        alertDialog.dismiss();
                        // 交易结束
                        transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
                    }
                });
                dialog.setConfirmClickListener(new OnCustomClickListener() {

                    @Override
                    public void onClick(CustomAlertDialog alertDialog) {
                        alertDialog.dismiss();
                        // 输入信息
                        gotoState(State.ENTER_INFO.toString());
                    }
                });
            }
        });
    }

    protected void afterEnterInfo(ActionResult result) {
        String[] infos = (String[]) result.getData();
        transData.setOrigTermID(infos[0]);
        transData.setOrigBatchNo(Long.parseLong(infos[1]));
        transData.setOrigTransNo(Long.parseLong(infos[2]));
        transData.setOrigDate(infos[3]);
        gotoState(State.CHECK_CARD.toString());
    }

    protected void afterCheckCard(ActionResult result) {
        CardInformation cardInfo = (CardInformation) result.getData();
        saveCardInfo(cardInfo, transData, true);
        // 手输卡号处理
        byte mode = cardInfo.getSearchMode();
        if (mode == SearchMode.INSERT) {
            // EMV处理
            gotoState(State.EMV_PROC.toString());
        } else if (mode == SearchMode.TAP) {
            // Clss处理
            gotoState(State.CLSS_PROC.toString());
        }
    }

    protected void afterEmvProc(ActionResult result) {
        // 判断芯片卡交易是完整流程还是简单流程，如果是简单流程，接下来是联机处理，完整流程接下来是签名
        ETransResult transResult = (ETransResult) result.getData();
        // EMV完整流程 脱机批准或联机批准都进入签名流程
        Component.emvTransResultProcess(transResult, transData);
        if (transResult == ETransResult.ONLINE_APPROVED || transResult == ETransResult.OFFLINE_APPROVED) {// 联机批准/脱机批准处理
            // 写交易记录
            transData.saveTrans();
            // 电子签名
            gotoState(State.SIGNATURE.toString());
        } else if (transResult == ETransResult.ARQC || transResult == ETransResult.SIMPLE_FLOW_END) {
            gotoState(State.ONLINE.toString());
        } else {
            emvAbnormalResultProcess(transResult);
        }
    }

    protected void afterClssPreProc() {
        // 撤销退货类是否需要输入主管密码
        if (FinancialApplication.getSysParam().get(SysParam.OTHTC_VERIFY).equals(SysParam.Constant.YES)) {
            gotoState(State.INPUT_PWD.toString());
        } else {
            gotoState(State.ENTER_AMOUNT.toString());
        }
    }

    protected void afterClssProc(ActionResult result) {
        CTransResult clssResult = (CTransResult) result.getData();
        transData.setEmvResult((byte) clssResult.getTransResult().ordinal());
        if (clssResult.getTransResult() == ETransResult.ABORT_TERMINATED ||
                clssResult.getTransResult() == ETransResult.CLSS_OC_DECLINED ||
                clssResult.getTransResult() == ETransResult.ONLINE_DENIED) { // emv interrupt
            Device.beepErr();
            transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
            return;
        }

        ClssTransProcess.clssTransResultProcess(clssResult, FinancialApplication.getClss(), transData);
        gotoState(State.ONLINE.toString());
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

}
