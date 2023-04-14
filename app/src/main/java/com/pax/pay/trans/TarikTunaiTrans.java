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
import com.pax.pay.emv.clss.ClssTransProcess;
import com.pax.pay.pulse.PulseActivity;
import com.pax.pay.trans.action.ActionChooseAccountList;
import com.pax.pay.trans.action.ActionClssPreProc;
import com.pax.pay.trans.action.ActionClssProcess;
import com.pax.pay.trans.action.ActionDispSingleLineMsg;
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
import com.pax.pay.trans.action.ActionSearchCardCustom;
import com.pax.pay.trans.action.ActionSignature;
import com.pax.pay.trans.action.ActionTransOnline;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.AccountData;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.trans.model.TransData.EnterMode;
import com.pax.pay.utils.ToastUtils;
import com.pax.settings.SysParam;
import com.pax.settings.currency.Currency;
import com.pax.up.bjb.R;

public class TarikTunaiTrans extends BaseTrans {

    private static final String TAG = "TarikTunaiTrans";
    private byte searchCardMode = SearchMode.KEYIN; // Find card method
    private String amount;

    private boolean isFreePin;
    private boolean isSupportBypass = true;

    public TarikTunaiTrans(Context context, Handler handler, String amount, boolean isFreePin,
                           TransEndListener transListener) {
        super(context, handler, ETransType.ACCOUNT_LIST, transListener);
        //request Account list first
        this.amount = amount;
        this.isFreePin = isFreePin;
    }

    @Override
    public void bindStateOnAction() {
        String isIndopayMode = FinancialApplication.getSysParam().get(SysParam.INDOPAY_MODE);

        // Search Card action
        searchCardMode = Component.getCardReadMode(transType);
        ActionSearchCardCustom searchCardAction = new ActionSearchCardCustom(null);
        searchCardAction.setTitle(context.getString(R.string.trans_withdrawal));
        searchCardAction.setMode(searchCardMode);
        //Sandy : since we modifying the 00 decimal point
        //we cut 00 at rear value
        if (amount != null && amount.length() > 0) {
            if (SysParam.Constant.YES.equals(isIndopayMode))
                searchCardAction.setAmount(transData.getAmount().substring(0, transData.getAmount().length() - 2));
            else
                searchCardAction.setAmount(transData.getAmount());
        }

        // Input amount
        ActionInputTransData amountAction = new ActionInputTransData(handler,ActionInputTransData.INFO_TYPE_SALE, null);
        amountAction.setTitle(context.getString(R.string.trans_withdrawal));
        amountAction.setInfoTypeSale(context.getString(R.string.prompt_input_amount),EInputType.AMOUNT, 9, false);
        bind( SaleTrans.State.ENTER_AMOUNT.toString(), amountAction);

        searchCardAction.setUiType(searchCardMode == ActionSearchCardCustom.SearchCustomMode.TAP ? ActionSearchCardCustom.ESearchCardUIType.QUICKPASS: ActionSearchCardCustom.ESearchCardUIType.DEFAULT);
        bind( State.CHECK_CARD.toString(), searchCardAction);

        // input CVN2 information
        ActionInputTransData enterInfosAction = new ActionInputTransData(handler, ActionInputTransData.INFO_TYPE_SALE, null);
        enterInfosAction.setTitle(context.getString(R.string.trans_withdrawal));
        enterInfosAction.setInfoTypeSale(context.getString(R.string.prompt_input_cvn2), EInputType.NUM, 3, 3, false);
        bind( State.ENTER_INFO.toString(), enterInfosAction);

        // Input Tip amount when supporting TIP, add by richard 20170411
        ActionInputTransData tipAmountAction = new ActionInputTransData(handler, ActionInputTransData.INFO_TYPE_SALE, null);
        tipAmountAction.setInfoTypeSale(context.getString(R.string.prompt_input_tip_amount), EInputType.AMOUNT, 9, false);
        bind( State.ENTER_TIP.toString(), tipAmountAction);

        // 输入密码action
        ActionEnterPin enterPinAction = new ActionEnterPin(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                // 如果是闪付凭密,设置isSupportBypass为false,需要输入密码
                if (!isFreePin) {
                    isSupportBypass = false;
                }
                ((ActionEnterPin) action).setParam(getCurrentContext(), context.getString(R.string.trans_withdrawal),
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

        ActionTransOnline transOnlineSaldoAction = new ActionTransOnline(transData);
        bind( State.ONLINE_TRX.toString(), transOnlineSaldoAction);

        // edit abdul from ActionDispSingleLineMsg to A
        ActionDispTransDetail dispTransDetail = new ActionDispTransDetail( new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                ((ActionDispTransDetail) action).setParam( getCurrentContext(), handler,
                        context.getString(R.string.trans_detail), transData, false, 0);
                TransContext.getInstance().setCurrentAction(action);
            }
        });
        bind( State.DETAIL_TRANSAKSI.toString(), dispTransDetail);

        // 签名action
        ActionSignature signatureAction = new ActionSignature(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                ((ActionSignature) action).setParam(getCurrentContext(), transData.getAmount(),
                        Component.genFeatureCode(transData));
            }
        });
        bind( State.SIGNATURE.toString(), signatureAction);

        // 打印action
        ActionPrintTransReceipt printTransReceiptAction = new ActionPrintTransReceipt(transData,handler);
        bind( State.PRINT_TICKET.toString(), printTransReceiptAction);

        // 执行的第一action
        /*if (amount == null || amount.length() == 0) {
            gotoState(State.ENTER_AMOUNT.toString());
        } else {
            gotoState(State.CLSS_PREPROC.toString());
        }*/
        gotoState(State.CLSS_PREPROC.toString());

    }

    protected enum State {
        ENTER_AMOUNT,
        CHECK_CARD,
        ENTER_TIP,
        ENTER_INFO,
        ENTER_PIN,
        ONLINE,
        ACCOUNT_LIST,
        ONLINE_TRX,
        EMV_PROC,
        CLSS_PREPROC,
        CLSS_PROC,
        DETAIL_TRANSAKSI,
        SIGNATURE,
        PRINT_TICKET
    }

    @Override
    public void onActionResult(String currentState, ActionResult result) {
        State state = State.valueOf(currentState);
        if (state == State.EMV_PROC) {
            // Sandy : Regardless of the success or failure of
            // the emv processing result, update the alignment
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
        if ((state != State.SIGNATURE) && (state != State.DETAIL_TRANSAKSI)) {
            // action结果检查，如果失败，结束交易
            int ret = result.getRet();
            if (ret != TransResult.SUCC) {
                //Sandy : delete the reversal
                //please go through at com.pax.pay.emv.EmvBaseListenerImpl
                //the reversal deletion should be modify at that file
                if(!transData.getResponseCode().equals("00")){
                    TransData.deleteDupRecord();
                }
                transEnd(result);
                return;
            }
        }

        switch (state) {
            case ENTER_AMOUNT:
                onEnterAmount(result);
                break;
            case CHECK_CARD: // Sandy : Subsequent processing of the test card
                onCheckCard(result);
                break;
            case ENTER_TIP:  //add by richard 20170412. input tip in sale transaction
                onEnterTip(result);
                break;
            case ENTER_PIN: // 输入密码的后续处理
                onEnterPin(result);
                break;
            case ONLINE: // 联机的后续处理
                onOnline(result);
                break;
            case EMV_PROC: // EMV follow-up processing
                onEmvProc(result);
                //onChooseAccount(result);
                break;
            case CLSS_PREPROC:
                gotoState( State.CHECK_CARD.toString());
                break;
            case CLSS_PROC:
                afterClssProcess(result);
                break;
            case DETAIL_TRANSAKSI:
                //onDetailDisplay(result);
                gotoState( State.EMV_PROC.toString());
                break;
            case SIGNATURE:
                onSignature(result);
                break;
            case ACCOUNT_LIST:
                //toSignOrPrint();

                if (result.getRet() == TransResult.SUCC) {
                    AccountData accNo = ((AccountData) result.getData());
                    transData.setAccNo(accNo.getAccountNumber());
                    transData.setAccType(accNo.getAccountType());

                    if(accNo.getAccountType().equals(AccountData.SAVING)){
                        transData.setTransType(ETransType.TARIK_TUNAI.toString());
                        setTransType(ETransType.TARIK_TUNAI);
                    }else {
                        transData.setTransType(ETransType.TARIK_TUNAI_2.toString());
                        setTransType(ETransType.TARIK_TUNAI_2);
                    }

                    transData.setTransNo(transData.getTransNo()+1);
                    gotoState( State.ENTER_AMOUNT.toString());


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
                        transData.setTransType(ETransType.TARIK_TUNAI.toString());
                        setTransType(ETransType.TARIK_TUNAI);
                        //Log.i( "abdul", "Tarik Tunai" )
                        // inc transno
                        transData.setTransNo(transData.getTransNo()+1);
                        gotoState( State.ENTER_AMOUNT.toString());
                    }
                }
                */

                break;
            case ONLINE_TRX:
                toSignOrPrint();
                break;
            case PRINT_TICKET:
            default:
                transEnd(result);
                break;
        }
    }

    // 判断是否需要电子签名或打印
    protected void toSignOrPrint() {

        if (transData.getHasPin()) {
            transData.setSignFree(true);
            gotoState( State.PRINT_TICKET.toString());
        } else {
            transData.setSignFree(false);
            gotoState( State.SIGNATURE.toString());
        }
        transData.setFeeTotalAmount(transData.getField28());
        transData.updateTrans();
    }

    protected void onEnterAmount(ActionResult result) {
        // 保存交易金额

        Log.d(TAG,"Sandy.onEnterAmount=" + result.getData());
        //do not use Indonesia language setting, it will have a dot value in money separation
        String amount = ((String) result.getData()).replace(",", "").replace(".","");
        transData.setAmount(String.format("%s00",amount));
        gotoState( State.DETAIL_TRANSAKSI.toString());
    }

    protected void onCheckCard(ActionResult result) {
        CardInformation cardInfo = (CardInformation) result.getData();
        saveCardInfo(cardInfo, transData, true);
        // 手输卡号处理
        byte mode = cardInfo.getSearchMode();
        if (mode != SearchMode.TAP) {
            if(FinancialApplication.getSysParam().get(SysParam.SUPPORT_TIP).equals(SysParam.Constant.NO)) { //TODO mode 2
                if(mode == SearchMode.INSERT ){
                    //gotoState( State.EMV_PROC.toString());
                    transData.setPan(cardInfo.getPan());
                    transData.setTrack2(cardInfo.getTrack2());
                    gotoState( State.ENTER_PIN.toString());
                } else {
                    transData.setTransType(ETransType.BALANCE_INQUIRY.toString());
                    gotoState( State.ENTER_PIN.toString());
                }
            }else {
                //input tip
                gotoState( State.ENTER_TIP.toString());
            }
        } else{ // if (mode == SearchMode.TAP)
            // EMV处理
            gotoState( State.CLSS_PROC.toString());
        }
    }

    protected void onEnterTip(ActionResult result) {
        //get enter mode
        int enterMode = transData.getEnterMode();
        //save tip amount
        String tipAmount = ((String) result.getData()).replace(",", "");
        long longTipAmount = Long.parseLong(tipAmount);
        long longAmount = Long.parseLong(transData.getAmount());

        if ((longTipAmount * 100) > (longAmount * Long.parseLong(FinancialApplication.getSysParam().get(SysParam.TIP_RATE)))){
            Device.beepErr();
            ToastUtils.showMessage(context, context.getString(R.string.prompt_amount_over_limit));
            gotoState( State.ENTER_TIP.toString());
            return;
        } else {
            transData.setTipAmount(tipAmount);
            transData.setAmount(String.valueOf(longAmount + longTipAmount));
        }
        if (enterMode == EnterMode.INSERT ) {
            gotoState( State.EMV_PROC.toString());
        } else {
            transData.setTransType(ETransType.BALANCE_INQUIRY.toString());
            gotoState( State.ENTER_PIN.toString());
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
        /*if (transType != ETransType.ACCOUNT_LIST)
            transData.saveTrans();*/
        // 判断是否需要电子签名或打印
        //toSignOrPrint();
        gotoState(State.ACCOUNT_LIST.toString());
    }




    protected void onChooseAccount(ActionResult result) {
        //Sandy : Choose an account to be listed in combobox
        //
        //gotoState( State.ACCOUNT_LIST.toString());
        gotoState( State.ONLINE_TRX.toString());
    }





    protected void onEmvProc(ActionResult result) {
        // Determine whether the chip card transaction is a complete process or a simple process.
        // If it is a simple process, the next is online processing,
        // and the complete process is followed by a signature
        ETransResult transResult = (ETransResult) result.getData();

        // EMV complete process, offline approval or online approval
        // both enter the signature process
        Component.emvTransResultProcess(transResult, transData);
        if (transResult == ETransResult.ONLINE_APPROVED || transResult == ETransResult.OFFLINE_APPROVED) {
            // Online approval/offline approval processing
            // Write transaction records
            Log.d(TAG,"Sandy.onEmvProc");

            String isIndopayMode = FinancialApplication.getSysParam().get(SysParam.INDOPAY_MODE);
            if(SysParam.Constant.YES.equals(isIndopayMode))
                transData.setAmount(transData.getAmount().substring(0,transData.getAmount().length()-2));

            transData.saveTrans();
            if (transResult == ETransResult.ONLINE_APPROVED) {
                toSignOrPrint();
                return;
            }

            //if OFFLINE_APPROVED and the entry mode is TAP, the trans type will set to EC_SALE.
            if (!transData.getTransType().equals(ETransType.EC_SALE.toString())) {
                toSignOrPrint();
                return;
            }
            gotoState( State.DETAIL_TRANSAKSI.toString());

        } else if (transResult == ETransResult.ARQC) { // 请求联机
            if (!Component.isQpbocNeedOnlinePin()) {
                gotoState( State.ONLINE.toString());
                return;
            }
            if (isFreePin && Component.clssQPSProcess(transData)) { // 免密
                transData.setPinFree(true);
                gotoState( State.ONLINE.toString());
            } else {
                // 输密码
                transData.setPinFree(false);
                gotoState( State.ENTER_PIN.toString());
            }
        } else {
            emvAbnormalResultProcess(transResult);
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
            toSignOrPrint();
        }
    }

    protected void onDetailDisplay(ActionResult result) {
        // 电子现金交易无需签名
        if (Component.isSignatureFree(transData)) {// 免签
            transData.setSignFree(true);
        } else {
            transData.setSignFree(false);
        }
        transData.updateTrans();
        gotoState( State.ONLINE_TRX.toString());
    }

    protected void onSignature(ActionResult result) {

        // 保存签名数据
        byte[] signData = (byte[]) result.getData();

        if (signData != null && signData.length > 0) {
            transData.setSignData(signData);
            // Sandy : Update transaction records and save electronic signatures
            transData.updateTrans();
        }
        gotoState( State.PRINT_TICKET.toString());
    }

}
