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
import com.pax.pay.trans.action.ActionChooseAccountList;
import com.pax.pay.trans.action.ActionClssPreProc;
import com.pax.pay.trans.action.ActionClssProcess;
import com.pax.pay.trans.action.ActionDispRekBSA;
import com.pax.pay.trans.action.ActionDispTransDetail;
import com.pax.pay.trans.action.ActionDispTransDetailVertical;
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
import com.pax.pay.utils.CurrencyConverter;
import com.pax.pay.utils.ToastUtils;
import com.pax.pay.utils.Utils;
import com.pax.settings.SysParam;
import com.pax.settings.currency.Currency;
import com.pax.up.bjb.R;

import java.util.LinkedHashMap;

public class AccountCancellationTrans extends BaseTrans {

    private static final String TAG = "AccountCancellationTrans";
    private byte searchCardMode = SearchMode.KEYIN; // Find card method
    private String amount;

    private boolean isFreePin;
    private boolean isSupportBypass = true;

    public AccountCancellationTrans(Context context, Handler handler, String amount, boolean isFreePin,
                                    TransEndListener transListener) {
        super(context, handler, ETransType.PEMBATAL_REK, transListener);
        this.amount = amount;
        this.isFreePin = isFreePin;
    }

    @Override
    public void bindStateOnAction() {
        String isIndopayMode = FinancialApplication.getSysParam().get(SysParam.INDOPAY_MODE);

        ActionDispRekBSA disp = new ActionDispRekBSA(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                ((ActionDispRekBSA) action).setParam(getCurrentContext(), handler,
                        context.getString(R.string.trans_cancelation_account));
            }
        });
        bind(State.DISP.toString(), disp);

        // Search Card action
        searchCardMode = Component.getCardReadMode(transType);
        ActionSearchCardCustom searchCardAction = new ActionSearchCardCustom(null);
        searchCardAction.setTitle(context.getString(R.string.trans_cancelation_account));
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
        ActionInputTransData inputId = new ActionInputTransData(handler, ActionInputTransData.INFO_TYPE_SALE, null);
        inputId.setTitle(context.getString(R.string.trans_cancelation_account));
        inputId.setInfoTypeSale(context.getString(R.string.prompt_input_id), EInputType.NUM, 12, false);
        bind(State.INPUT_ID.toString(), inputId);

        searchCardAction.setUiType(searchCardMode == ActionSearchCardCustom.SearchCustomMode.TAP ? ActionSearchCardCustom.ESearchCardUIType.QUICKPASS : ActionSearchCardCustom.ESearchCardUIType.DEFAULT);
        bind(State.CHECK_CARD.toString(), searchCardAction);

        // input CVN2 information
        ActionInputTransData enterInfosAction = new ActionInputTransData(handler, ActionInputTransData.INFO_TYPE_SALE, null);
        enterInfosAction.setTitle(context.getString(R.string.trans_cancelation_account));
        enterInfosAction.setInfoTypeSale(context.getString(R.string.prompt_input_cvn2), EInputType.NUM, 3, 3, false);
        bind(State.ENTER_INFO.toString(), enterInfosAction);

        // Input Tip amount when supporting TIP, add by richard 20170411
        ActionInputTransData tipAmountAction = new ActionInputTransData(handler, ActionInputTransData.INFO_TYPE_SALE, null);
        tipAmountAction.setInfoTypeSale(context.getString(R.string.prompt_input_tip_amount), EInputType.AMOUNT, 9, false);
        bind(State.ENTER_TIP.toString(), tipAmountAction);

        // 输入密码action
        ActionEnterPin enterPinAction = new ActionEnterPin(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                // 如果是闪付凭密,设置isSupportBypass为false,需要输入密码
                if (!isFreePin) {
                    isSupportBypass = false;
                }
                ((ActionEnterPin) action).setParam(getCurrentContext(), context.getString(R.string.trans_cancelation_account),
                        transData.getPan(), isSupportBypass, context.getString(R.string.prompt_bankcard_pwd),
                        context.getString(R.string.prompt_no_password), transData.getAmount(), EEnterPinType.ONLINE_PIN, transData.getEnterMode());
            }
        });
        bind(State.ENTER_PIN.toString(), enterPinAction);

        // emv处理action
        ActionEmvProcess emvProcessAction = new ActionEmvProcess(handler, transData);
        bind(State.EMV_PROC.toString(), emvProcessAction);

        //clss process action
        ActionClssProcess clssProcessAction = new ActionClssProcess(transData, null);
        bind(State.CLSS_PROC.toString(), clssProcessAction);


        //clss preprocess action
        ActionClssPreProc clssPreProcAction = new ActionClssPreProc(transData, null);
        bind(State.CLSS_PREPROC.toString(), clssPreProcAction);

        // 联机action
        ActionTransOnline transOnlineAction = new ActionTransOnline(transData);
        bind(State.ONLINE.toString(), transOnlineAction);

        ActionTransOnline transOnlineAction2 = new ActionTransOnline(transData);
        bind(State.ONLINE2.toString(), transOnlineAction2);

        ActionChooseAccountList accountListAction = new ActionChooseAccountList(getCurrentContext(), transData, transType.getTransName(), "Pilih Rekening");
        bind(State.ACCOUNT_LIST.toString(), accountListAction);

        ActionTransOnline transOnlineSaldoAction = new ActionTransOnline(transData);
        bind(State.ONLINE_TRX.toString(), transOnlineSaldoAction);

        ActionDispTransDetailVertical dispTransDetail = new ActionDispTransDetailVertical(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                LinkedHashMap<String, String> map = prepareDispBatalRek();
                ((ActionDispTransDetailVertical) action).setParam(getCurrentContext(), handler,
                        transType.getTransName(), map);
                TransContext.getInstance().setCurrentAction(action);
            }
        });
        bind(State.DETAIL_TRANSAKSI.toString(), dispTransDetail);

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
        ActionPrintTransReceipt printTransReceiptAction = new ActionPrintTransReceipt(transData, handler);
        bind(State.PRINT_TICKET.toString(), printTransReceiptAction);

        // 执行的第一action
        /*if (amount == null || amount.length() == 0) {
            gotoState(State.ENTER_AMOUNT.toString());
        } else {
            gotoState(State.CLSS_PREPROC.toString());
        }*/
        gotoState(State.DISP.toString());
//        gotoState(State.CLSS_PREPROC.toString());

    }

    protected enum State {
        DISP,
        INPUT_ID,
        CHECK_CARD,
        ENTER_TIP,
        ENTER_INFO,
        ENTER_PIN,
        ONLINE,
        ONLINE2,
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
            // 不管emv处理结果成功还是失败，都更新一下冲正
            byte[] f55Dup = EmvTags.getF55(FinancialApplication.getEmv(), transType, true, false);
            if (f55Dup != null && f55Dup.length > 0) {
                TransData.updateDupF55(FinancialApplication.getConvert().bcdToStr(f55Dup));
            }
            //fall back treatment
            if (transData.getIsFallback()) {
                ActionSearchCard action = (ActionSearchCard) getAction(State.CHECK_CARD.toString());
                action.setMode(SearchMode.SWIPE);
                action.setUiType(ESearchCardUIType.DEFAULT);
                gotoState(State.CHECK_CARD.toString());
                return;
            }
        }
        if ((state != State.SIGNATURE) && (state != State.DETAIL_TRANSAKSI)) {
            // action结果检查，如果失败，结束交易
            int ret = result.getRet();
            if (ret != TransResult.SUCC) {
                transEnd(result);
                return;
            }
        }

        switch (state) {
            case DISP:
                gotoState(State.CLSS_PREPROC.toString());
                break;
            case INPUT_ID:
                afterInputId(result);
                break;
            case CHECK_CARD: // 检测卡的后续处理
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
//                afterEmv(result);
                break;
            case CLSS_PREPROC:
                gotoState(State.CHECK_CARD.toString());
                break;
            case CLSS_PROC:
                afterClssProcess(result);
                break;
            case DETAIL_TRANSAKSI:
                gotoState(State.EMV_PROC.toString());
                break;
            case SIGNATURE:
                onSignature(result);
                break;
            case ACCOUNT_LIST:
                if (result.getRet() == TransResult.SUCC) {
                    if(result.getData().getClass().equals(AccountData.class)) {
                        AccountData accNo = ((AccountData) result.getData());
                        transData.setAccNo(accNo.getAccountNumber());
                        transData.setAccType(accNo.getAccountType());
                        transData.setTransType(ETransType.PEMBATAL_REK_INQ.toString());
                        transData.setTransNo(transData.getTransNo() + 1);
                        gotoState(State.INPUT_ID.toString());
                    }
                }

                /*
                String accNo = ((String) result.getData().toString());
                String[] no = transData.getAccNo().split("#");
                String[] type = transData.getAccType().split("#");
                int i = no.length;
                int a = type.length;
                for (int n = 0; n < no.length; n++) {
                    if (no[n].equals(accNo)) {
                        transData.setAccNo(no[n]);
                        transData.setAccType(type[n]);
                        transData.setTransType(ETransType.PEMBATAL_REK_INQ.toString());
                        //Log.i( "abdul", "Tarik Tunai" )
                        // inc transno
                        transData.setTransNo(transData.getTransNo() + 1);
                        gotoState(State.INPUT_ID.toString());
                    }
                }
                 */


                break;
            case ONLINE_TRX:
                toSignOrPrint();
                break;
            case ONLINE2:
                afterInquiry();
                break;
            case PRINT_TICKET:
            default:
                transEnd(result);
                break;
        }
    }

    private LinkedHashMap<String, String> prepareDispBatalRek() {
        if (transData == null) {
            return null;
        }

        //String amount = CurrencyConverter.convert(Utils.parseLongSafe(transData.getAmount(), 0), CurrencyConverter.getDefCurrency());

        String amt = transData.getAmount().substring(0, transData.getAmount().length() - 2);

        Currency currency = FinancialApplication.getSysParam().getCurrency();
        String amount = currency.getName() + " " + FinancialApplication.getConvert()
                .amountMinUnitToMajor(amt, currency.getCurrencyExponent(), true);

        LinkedHashMap<String, String> hashMap = new LinkedHashMap<>();
        String data = transData.getField48();
        String nama = data.substring(12, 47); //35
        String tempat = data.substring(47, 82); //35
        String tanggal = data.substring(82, 90); //8
        String no = data.substring(90, 105); //15
        String nik = data.substring(105, 125); //20

        hashMap.put(context.getString(R.string.detail_nama), nama.trim());
        hashMap.put(context.getString(R.string.detail_nik), nik.trim());
        hashMap.put(context.getString(R.string.detail_t_l), tempat.trim());
        hashMap.put(context.getString(R.string.detail_tgl_l), tanggal.trim());
        hashMap.put(context.getString(R.string.detail_no_hp), no.trim());
        hashMap.put(context.getString(R.string.detail_nominal), amount.trim());

        return hashMap;
    }

    protected void afterInquiry() {
        String detail = transData.getField48();
        detail = detail.substring(0, 125);
        Log.d("teg", "detail : " + detail);
        transData.setTransType(ETransType.PEMBATAL_REK.toString());
        transData.setTransNo(transData.getTransNo() + 1);
        gotoState(State.DETAIL_TRANSAKSI.toString());
    }

    // 判断是否需要电子签名或打印
    protected void toSignOrPrint() {

        if (transData.getHasPin()) {
            transData.setSignFree(true);
            gotoState(State.PRINT_TICKET.toString());
        } else {
            transData.setSignFree(false);
            gotoState(State.SIGNATURE.toString());
        }
        transData.updateTrans();
    }

    protected void afterInputId(ActionResult result) {

        String isIndopayMode = FinancialApplication.getSysParam().get(SysParam.INDOPAY_MODE);
        String id = ((String) result.getData()).replace(",", "");
        transData.setField48(id);
        gotoState(State.ONLINE2.toString());
    }

    protected void onCheckCard(ActionResult result) {
        transData.setTransType(ETransType.ACCOUNT_LIST.toString());

        CardInformation cardInfo = (CardInformation) result.getData();
        saveCardInfo(cardInfo, transData, true);
        // 手输卡号处理
        byte mode = cardInfo.getSearchMode();
        if (mode != SearchMode.TAP) {
            if (FinancialApplication.getSysParam().get(SysParam.SUPPORT_TIP).equals(SysParam.Constant.NO)) {
                if (mode == SearchMode.INSERT) {
                    //gotoState( State.EMV_PROC.toString());
                    transData.setPan(cardInfo.getPan());
                    transData.setTrack2(cardInfo.getTrack2());
                    gotoState(State.ENTER_PIN.toString());
                } else {

                    gotoState(State.ENTER_PIN.toString());
                }
            } else {
                //input tip
                gotoState(State.ENTER_TIP.toString());
            }
        } else { // if (mode == SearchMode.TAP)
            // EMV处理
            gotoState(State.CLSS_PROC.toString());
        }
    }

    protected void onEnterTip(ActionResult result) {
        //get enter mode
        int enterMode = transData.getEnterMode();
        //save tip amount
        String tipAmount = ((String) result.getData()).replace(",", "");
        long longTipAmount = Long.parseLong(tipAmount);
        long longAmount = Long.parseLong(transData.getAmount());

        if ((longTipAmount * 100) > (longAmount * Long.parseLong(FinancialApplication.getSysParam().get(SysParam.TIP_RATE)))) {
            Device.beepErr();
            ToastUtils.showMessage(context, context.getString(R.string.prompt_amount_over_limit));
            gotoState(State.ENTER_TIP.toString());
            return;
        } else {
            transData.setTipAmount(tipAmount);
            transData.setAmount(String.valueOf(longAmount + longTipAmount));
        }
        if (enterMode == EnterMode.INSERT) {
            gotoState(State.EMV_PROC.toString());
        } else {
            transData.setTransType(ETransType.PEMBATAL_REK_INQ.toString());
            gotoState(State.ENTER_PIN.toString());
        }
    }

    protected void onEnterPin(ActionResult result) {
        String pinBlock = (String) result.getData();
        transData.setPin(pinBlock);
        if (pinBlock != null && pinBlock.length() > 0) {
            transData.setHasPin(true);
        }
        // 联机处理
        gotoState(State.ONLINE.toString());
    }

    protected void onOnline(ActionResult result) {
        if (transData.getEnterMode() == EnterMode.QPBOC) {
            transData.setEmvResult((byte) ETransResult.ONLINE_APPROVED.ordinal());
        }

        String isIndopayMode = FinancialApplication.getSysParam().get(SysParam.INDOPAY_MODE);
        if (SysParam.Constant.YES.equals(isIndopayMode))
            transData.setAmount(transData.getAmount().substring(0, transData.getAmount().length() - 2));

        // 写交易记录
//        transData.saveTrans();
        // 判断是否需要电子签名或打印
        //toSignOrPrint();
        gotoState(State.ACCOUNT_LIST.toString());
    }


    protected void afterEmv(ActionResult result) {

        toSignOrPrint();
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

            String isIndopayMode = FinancialApplication.getSysParam().get(SysParam.INDOPAY_MODE);
            if (SysParam.Constant.YES.equals(isIndopayMode))
                transData.setAmount(transData.getAmount().substring(0, transData.getAmount().length() - 2));

            transData.setReprintData(transData.getField48());
            transData.setFeeTotalAmount(transData.getField28());
            transData.saveTrans();
            if (transResult == ETransResult.ONLINE_APPROVED) {
                toSignOrPrint();
                return;
            }

            //if OFFLINE_APPROVED and the entry mode is TAP, the trans type will set to EC_SALE.
            /*if (!transData.getTransType().equals(ETransType.EC_SALE.toString())) {
                toSignOrPrint();
                return;
            }
            gotoState(State.DETAIL_TRANSAKSI.toString());*/

        } else if (transResult == ETransResult.ARQC) { // 请求联机
            if (!Component.isQpbocNeedOnlinePin()) {
                gotoState(State.ONLINE.toString());
                return;
            }
            if (isFreePin && Component.clssQPSProcess(transData)) { // 免密
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

    protected void afterClssProcess(ActionResult result) {
        CTransResult transResult = (CTransResult) result.getData();
        // 设置交易结果
        transData.setEmvResult((byte) transResult.getTransResult().ordinal());
        if (transResult.getTransResult() == ETransResult.ABORT_TERMINATED ||
                transResult.getTransResult() == ETransResult.CLSS_OC_DECLINED ||
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
        gotoState(State.ONLINE_TRX.toString());
    }

    protected void onSignature(ActionResult result) {

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
