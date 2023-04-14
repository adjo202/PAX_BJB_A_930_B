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
import android.os.Handler;
import android.util.Log;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.device.Device;
import com.pax.eemv.entity.CTransResult;
import com.pax.eemv.enums.ECvmResult;
import com.pax.eemv.enums.ETransResult;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.emv.EmvTags;
import com.pax.pay.emv.EmvTransProcess;
import com.pax.pay.emv.clss.ClssTransProcess;
import com.pax.pay.trans.action.ActionChooseAccountList;
import com.pax.pay.trans.action.ActionClssPreProc;
import com.pax.pay.trans.action.ActionClssProcess;
import com.pax.pay.trans.action.ActionDispSingleLineMsg;
import com.pax.pay.trans.action.ActionDispTransArrayList;
import com.pax.pay.trans.action.ActionDispTransDetail;
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
import com.pax.pay.trans.model.AccountData;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.trans.model.TransData.EnterMode;
import com.pax.pay.utils.Fox;
import com.pax.pay.utils.ToastUtils;
import com.pax.settings.SysParam;
import com.pax.settings.currency.Currency;
import com.pax.up.bjb.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class MiniStatementTrans extends BaseTrans {

    private static final String TAG = "MiniStatementTrans";
    private byte searchCardMode = SearchMode.KEYIN; // Find card method

    private boolean isFreePin = false;
    private boolean isSupportBypass = true;

    public MiniStatementTrans(Context context, Handler handler,
                              TransEndListener transListener) {
        super(context, handler, ETransType.MINISTATEMENT, transListener);
    }


    @Override
    public void bindStateOnAction() {

        // Search Card action
        searchCardMode = Component.getCardReadMode(transType);
        ActionSearchCard searchCardAction = new ActionSearchCard(null);
        searchCardAction.setTitle(context.getString(R.string.trans_mini_statement));
        searchCardAction.setMode(searchCardMode);
        searchCardAction.setUiType(searchCardMode == SearchMode.TAP ? ESearchCardUIType.QUICKPASS: ESearchCardUIType.DEFAULT);
        bind( State.CHECK_CARD.toString(), searchCardAction);


        // 输入密码action
        ActionEnterPin enterPinAction = new ActionEnterPin(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                // 如果是闪付凭密,设置isSupportBypass为false,需要输入密码
                if (!isFreePin) {
                    isSupportBypass = false;
                }
                ((ActionEnterPin) action).setParam(getCurrentContext(), context.getString(R.string.trans_mini_statement),
                        transData.getPan(), isSupportBypass, context.getString(R.string.prompt_bankcard_pwd),
                        context.getString(R.string.prompt_no_password), transData.getAmount(), EEnterPinType.ONLINE_PIN, transData.getEnterMode());
            }
        });
        bind( State.ENTER_PIN.toString(), enterPinAction);

        // emv处理action
        ActionEmvProcess emvProcessAction = new ActionEmvProcess(handler, transData);
        bind( State.EMV_PROC.toString(), emvProcessAction);

        //clss process action
        ActionClssProcess clssProcessAction = new ActionClssProcess(transData, null);
        bind( State.CLSS_PROC.toString(), clssProcessAction);


        //clss preprocess action
        ActionClssPreProc clssPreProcAction = new ActionClssPreProc(transData, null);
        bind( State.CLSS_PREPROC.toString(), clssPreProcAction);

        // 联机action
        ActionTransOnline transOnlineAction = new ActionTransOnline(transData);
        bind( State.ONLINE.toString(), transOnlineAction);

        ActionChooseAccountList accountListAction = new ActionChooseAccountList(getCurrentContext(),transData);
        bind( State.ACCOUNT_LIST.toString(), accountListAction);


        // 确认信息
        ActionDispTransArrayList confirmInfoAction = new ActionDispTransArrayList(new AAction
                .ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                //
//                String bit47 = "06/19/19 D              2,000 BY INFO SALDO            06/19/19 D              2,000 BY INFO SALDO            06/19/19 D              2,000 BY INFO SALDO            06/19/19 D              2,000 BY INFO SALDO            06/19/19 D              2,000 BY INFO SALDO            06/19/19 D              2,000 BY INFO SALDO            06/19/19 D              2,000 BY INFO SALDO            06/19/19 D              2,000 BY INFO SALDO";
                String bit47 = transData.getField47();
                String[] date = new String[bit47.length()/55];
                String[] sign = new String[bit47.length()/55];
                String[] amount = new String[bit47.length()/55];
                String[] desc = new String[bit47.length()/55];

                int lendata = bit47.length()/55;
                int panjangsatudata = 0;

                ArrayList<String> leftcolumn = new ArrayList<>();
                ArrayList<String> rightcolumn = new ArrayList<>();

                for (int i=0; i<lendata; i++){
                    date[i] = Fox.Substr(bit47, 1+panjangsatudata, 8);
                    sign[i] = Fox.Substr(bit47, 10+panjangsatudata, 1);
                    amount[i] = Fox.Substr(bit47, 12+panjangsatudata, 18).replace(" ","");
                    desc[i] = Fox.Substr(bit47, 31+panjangsatudata, 25).trim();

                    String fullamt;
                    if (sign[i].equals("D")){
                        fullamt = "- Rp"+amount[i];

                        /*if (desc[i].contains("TARIK") || desc[i].contains("POTONGAN") || desc[i].contains("TRF")){
                            fullamt = "- Rp"+amount[i];
                        }else {
                            fullamt = "Rp"+amount[i];
                        }*/

                    }else {
                        fullamt = "+ Rp"+amount[i];
                    }

                    leftcolumn.add(desc[i] + "(" + sign[i] +")");
                    rightcolumn.add("");
                    leftcolumn.add(date[i]);
                    rightcolumn.add(fullamt);

                    leftcolumn.add("");
                    rightcolumn.add("");

                    panjangsatudata += 55;

                }

                ((ActionDispTransArrayList) action).setParam(getCurrentContext(), handler,
                        "MINI STATEMENT", leftcolumn, rightcolumn, transData.getAccNo());
            }
        });
        bind(State.DISP_DETAIL.toString(), confirmInfoAction);

        // 联机action
        ActionTransOnline transOnlineAction2 = new ActionTransOnline(transData);
        bind( State.ONLINE2.toString(), transOnlineAction2);

        // 打印action
        ActionPrintTransReceipt printTransReceiptAction = new ActionPrintTransReceipt(transData,handler);
        bind( State.PRINT_TICKET.toString(), printTransReceiptAction);

       gotoState( State.CLSS_PREPROC.toString());

    }

    protected enum State {
        CHECK_CARD,
        ENTER_PIN,
        ONLINE,
        ACCOUNT_LIST,
        EMV_PROC,
        CLSS_PREPROC,
        CLSS_PROC,
        DISP_DETAIL,
        ONLINE2,
        PRINT_TICKET
    }

    @Override
    public void onActionResult(String currentState, ActionResult result) {
        State state = State.valueOf(currentState);
        if (state == State.EMV_PROC) {
            // 不管emv处理结果成功还是失败，都更新一下冲正
            byte[] f55Dup = EmvTags.getF55(FinancialApplication.getEmv(), transType, true, false);
            if (f55Dup != null && f55Dup.length > 0) {
                TransData.updateDupF55(FinancialApplication.getConvert().bcdToStr(f55Dup));
            }
            //fall back treatment
            if(transData.getIsFallback()){
                ActionSearchCard action = (ActionSearchCard)getAction( State.CHECK_CARD.toString());
                action.setMode(SearchMode.SWIPE);
                action.setUiType(ESearchCardUIType.DEFAULT);
                gotoState( State.CHECK_CARD.toString());
                return;
            }
        }


        switch (state) {
            case CHECK_CARD: // 检测卡的后续处理
                onCheckCard(result);
                break;
            case ENTER_PIN: // 输入密码的后续处理
                onEnterPin(result);
                break;
            case ONLINE: // 联机的后续处理
                onOnline(result);
                break;
            case EMV_PROC: // EMV follow-up processing
                afterEmvProc(result);
                break;
            case CLSS_PREPROC:
                gotoState( State.CHECK_CARD.toString());
                break;
            case CLSS_PROC:
                afterClssProcess(result);
                break;
            case ACCOUNT_LIST:
                //toSignOrPrint();
                if (result.getRet() == TransResult.SUCC) {
                    AccountData accNo = ((AccountData) result.getData());
                    transData.setAccNo(accNo.getAccountNumber());
                    transData.setAccType(accNo.getAccountType());
                    transData.setTransNo(transData.getTransNo()+1);
                    transData.setTransType(ETransType.MINISTATEMENT.toString());
                    gotoState( State.ONLINE2.toString());

                }

                /*
                String accNo = ((String) result.getData().toString());
                String[] no = transData.getAccNo().split("#");
                String[] type = transData.getAccType().split("#");
                int i = no.length;
                int a = type.length;
                for (int n =0; n<no.length; n++) {
                    if (no[n].equals(accNo)) {
                        transData.setAccNo(no[n]);
                        transData.setAccType(type[n]);
                        // inc transno
                        transData.setTransNo(transData.getTransNo()+1);
                    }
                }
                transData.setTransType(ETransType.MINISTATEMENT.toString());
                gotoState( State.ONLINE2.toString());
                */
                break;
            case ONLINE2:
                if (result.getRet() == TransResult.SUCC){
                    transData.setFeeTotalAmount(transData.getField28());
                    transData.saveTrans();
                    gotoState(State.DISP_DETAIL.toString());
                }else {
                    transEnd(result);
                }
                break;
            case DISP_DETAIL:
                gotoState(State.PRINT_TICKET.toString());
                break;
            case PRINT_TICKET:
            default:
                transEnd(result);
                break;
        }
    }

    protected void onCheckCard(ActionResult result) {
        CardInformation cardInfo = (CardInformation) result.getData();
        saveCardInfo(cardInfo, transData, true);

        transData.setTransType(ETransType.ACCOUNT_LIST.toString());

        // 手输卡号处理
        byte mode = cardInfo.getSearchMode();
        if (mode != SearchMode.TAP) {
            if(mode == SearchMode.INSERT ){
                gotoState( State.EMV_PROC.toString());
            } else {
                transData.setTransType(ETransType.BALANCE_INQUIRY.toString());
                gotoState( State.ENTER_PIN.toString());
            }
        } else{
            // EMV处理
            gotoState( State.CLSS_PROC.toString());
        }
    }

    protected void onEnterPin(ActionResult result) {
        String pinBlock = (String) result.getData();
        transData.setPin(pinBlock);
        if (pinBlock != null && pinBlock.length() > 0) {
            transData.setHasPin(true);
        }
        // 联机处理
        gotoState( State.ONLINE.toString());
    }

    protected void onOnline(ActionResult result) {
        if (transData.getEnterMode() == EnterMode.QPBOC) {
            transData.setEmvResult((byte) ETransResult.ONLINE_APPROVED.ordinal());
        }

        String isIndopayMode = FinancialApplication.getSysParam().get(SysParam.INDOPAY_MODE);
        if(SysParam.Constant.YES.equals(isIndopayMode))
            transData.setAmount(transData.getAmount().substring(0,transData.getAmount().length()-2));

        // 写交易记录
        transData.saveTrans();
        gotoState(State.ACCOUNT_LIST.toString());
    }

    protected void afterEmvProc(ActionResult result) {
        ETransResult transResult = (ETransResult) result.getData();
        Component.emvTransResultProcess(transResult, transData);
        if (transResult == ETransResult.ONLINE_APPROVED) {// 联机批准/脱机批准处理

            gotoState(State.ACCOUNT_LIST.toString());

        } else if (transResult == ETransResult.ARQC || transResult == ETransResult.SIMPLE_FLOW_END) { // 请求联机/简化流程

            if (transResult == ETransResult.ARQC && !Component.isQpbocNeedOnlinePin()) {
                gotoState(BalanceTrans.State.ONLINE.toString());
                return;
            }
            // 输密码
            gotoState(BalanceTrans.State.ENTER_PIN.toString());
        } else if (transResult == ETransResult.OFFLINE_APPROVED) {
            transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
        } else {
//            emvAbnormalResultProcess(transResult);
            transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
        }
    }

    protected void afterClssProcess(ActionResult result) {
        Log.d(TAG,"Sandy=SaleTrans.afterClssProcess called!");
        CTransResult transResult = (CTransResult)result.getData();
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
            //do signature after online
            transData.setSignFree(false);
            transData.setPinFree(true);
        } else {
            transData.setSignFree(true);
            transData.setPinFree(true);
        }

        if (transResult.getTransResult() == ETransResult.CLSS_OC_APPROVED || transResult.getTransResult() == ETransResult.ONLINE_APPROVED) {
            transData.setIsOnlineTrans(transResult.getTransResult() == ETransResult.ONLINE_APPROVED);
//            toSignOrPrint();
            gotoState(State.ACCOUNT_LIST.toString());
        }
    }

    protected void onSignature(ActionResult result) {

        // 保存签名数据
        byte[] signData = (byte[]) result.getData();

        if (signData != null && signData.length > 0) {
            transData.setSignData(signData);
            // 更新交易记录，保存电子签名
            transData.updateTrans();
        }
        gotoState( State.PRINT_TICKET.toString());
    }

}
