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
import com.pax.jemv.clcommon.RetCode;
import com.pax.jemv.emv.api.EMVCallback;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.emv.EmvTags;
import com.pax.pay.emv.clss.ClssTransProcess;
import com.pax.pay.trans.action.ActionChooseAccountList;
import com.pax.pay.trans.action.ActionClssPreProc;
import com.pax.pay.trans.action.ActionClssProcess;
import com.pax.pay.trans.action.ActionDispSingleLineMsg;
import com.pax.pay.trans.action.ActionDispTransDetail;
import com.pax.pay.trans.action.ActionDispTransDetailVertical;
import com.pax.pay.trans.action.ActionEmvProcess;
import com.pax.pay.trans.action.ActionEnterPin;
import com.pax.pay.trans.action.ActionEnterPin.EEnterPinType;
import com.pax.pay.trans.action.ActionInputDataNasabah;
import com.pax.pay.trans.action.ActionInputDataOverBooking;
import com.pax.pay.trans.action.ActionInputDataTransfer;
import com.pax.pay.trans.action.ActionInputDataTransferNew;
import com.pax.pay.trans.action.ActionInputESamsatData;
import com.pax.pay.trans.action.ActionInputKodeBilling;
import com.pax.pay.trans.action.ActionInputPDAMData;
import com.pax.pay.trans.action.ActionInputPascabayarData;
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
import com.pax.pay.trans.model.ProductData;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.trans.model.TransData.EnterMode;
import com.pax.pay.trans.transmit.TransProcessListenerImpl;
import com.pax.pay.utils.Controllers;
import com.pax.pay.utils.ToastUtils;
import com.pax.settings.SysParam;
import com.pax.settings.currency.Currency;
import com.pax.up.bjb.R;

import org.apache.commons.lang.StringUtils;

import java.util.LinkedHashMap;

import static com.pax.pay.trans.FundTransferTrans.arr;
import static com.pax.pay.trans.action.ActionInputKodeBilling.EInputType.NUM;
import static com.pax.pay.trans.action.ActionInputKodeBilling.INFO_TYPE_SALE;
import static com.pax.pay.utils.Controllers.PULSA;


public class TestingTrans extends BaseTrans {

    private TransProcessListenerImpl transProcessListenerImpl;
    private static final String TAG = "TestingTrans";
    private byte searchCardMode = SearchMode.KEYIN; // Find card method
    private String amount;

    private boolean isFreePin;
    private boolean isSupportBypass = true;

    public TestingTrans(Context context, Handler handler, String amount, boolean isFreePin,
                        TransEndListener transListener) {
        super(context, handler, ETransType.E_SAMSAT, transListener);
        //request Account list first
        this.amount = amount;
        this.isFreePin = isFreePin;

    }


    @Override
    public void bindStateOnAction() {
        String isIndopayMode = FinancialApplication.getSysParam().get(SysParam.INDOPAY_MODE);

        ActionInputTransData inputAmountAction = new ActionInputTransData(handler, ActionInputTransData.INFO_TYPE_SALE, null);
        inputAmountAction.setTitle(context.getString(R.string.trans_opening_account));
        inputAmountAction.setPrompt1(context.getString(R.string.prompt_input_amount));
        inputAmountAction.setInputType1(EInputType.AMOUNT);
        inputAmountAction.setMaxLen1(9);
        inputAmountAction.setMinLen1(0);
        bind(State.ENTER_AMOUNT.toString(), inputAmountAction);

        // Search Card action
        searchCardMode = Component.getCardReadMode(transType);
        ActionSearchCard searchCardAction = new ActionSearchCard(null);
        searchCardAction.setTitle(context.getString(R.string.trans_opening_account));
        searchCardAction.setMode(searchCardMode);
        //Sandy : since we modifying the 00 decimal point
        //we cut 00 at rear value
        if (amount != null && amount.length() > 0) {
            if (SysParam.Constant.YES.equals(isIndopayMode))
                searchCardAction.setAmount(transData.getAmount().substring(0, transData.getAmount().length() - 2));
            else
                searchCardAction.setAmount(transData.getAmount());
        }

        searchCardAction.setUiType(searchCardMode == SearchMode.TAP ? ESearchCardUIType.QUICKPASS : ESearchCardUIType.DEFAULT);
        bind(State.CHECK_CARD.toString(), searchCardAction);

        // input CVN2 information
        ActionInputDataNasabah enterInfosAction = new ActionInputDataNasabah(handler, ActionInputTransData.INFO_TYPE_SALE, null);
        enterInfosAction.setTitle(context.getString(R.string.trans_opening_account));
        bind(State.INPUT_DATA.toString(), enterInfosAction);

        ActionInputDataTransferNew enter = new ActionInputDataTransferNew(handler, ActionInputTransData.INFO_TYPE_SALE, null);
        enter.setTitle(context.getString(R.string.trans_fund_transfer));
        bind(State.INPUT_DATA2.toString(), enter);

        ActionInputDataOverBooking enter4 = new ActionInputDataOverBooking(handler, ActionInputTransData.INFO_TYPE_SALE, null);
        enter4.setTitle(context.getString(R.string.trans_opening_account));
        bind(State.INPUT_DATA4.toString(), enter4);

        /*ActionInputESamsatData i = new ActionInputESamsatData(handler, null);
        i.setTitle("TEST");
        bind(State.INPUT_DATA5.toString(), i);*/

        /*ActionInputPDAMData i = new ActionInputPDAMData(handler, null);
        i.setTitle("TEST");
        bind(State.INPUT_DATA5.toString(), i);*/

        ActionInputPascabayarData i = new ActionInputPascabayarData(handler, null);
        i.setTitle("TEST");
        bind(State.INPUT_DATA5.toString(), i);

        ActionDispTransDetail dispTransDetail = new ActionDispTransDetail(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                ((ActionDispTransDetail) action).setParam(getCurrentContext(), handler,
                        context.getString(R.string.trans_detail), prepareDisp());
                TransContext.getInstance().setCurrentAction(action);
            }
        });
        bind(State.DETAIL_TRANSAKSI.toString(), dispTransDetail);

        /*ActionInputPDAMData i = new ActionInputPDAMData(handler, null);
        i.setTitle("TEST");
        bind(State.INPUT_DATA5.toString(), i);*/

        /*ActionInputTransData inputId = new ActionInputTransData(handler, ActionInputTransData.INFO_TYPE_SALE, null);
        inputId.setTitle(context.getString(R.string.trans_mpn_g2));
        inputId.setInfoTypeSale(context.getString(R.string.prompt_input_billing), EInputType.NUM, 15, false);
        bind(State.INPUT_BILLING.toString(), inputId);*/

        ActionInputKodeBilling inputId = new ActionInputKodeBilling(handler, INFO_TYPE_SALE, null);
        inputId.setTitle(context.getString(R.string.trans_mpn_g2));
        inputId.setInfoTypeSale(context.getString(R.string.prompt_input_billing), NUM, 15, false);
        bind(State.INPUT_BILLING.toString(), inputId);

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
                ((ActionEnterPin) action).setParam(getCurrentContext(), context.getString(R.string.trans_opening_account),
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


        ActionChooseAccountList accountListAction = new ActionChooseAccountList(getCurrentContext(), transData);
        bind(State.ACCOUNT_LIST.toString(), accountListAction);

        ActionTransOnline transOnlineSaldoAction = new ActionTransOnline(transData);
        bind(State.INFO_SALDO.toString(), transOnlineSaldoAction);

        /*ActionBukaRekening bukaRek = new ActionBukaRekening(transData);
        bind(State.BUKA_REK.toString(), bukaRek);*/

        ActionTransOnline transOnlineAction2 = new ActionTransOnline(transData);
        bind(MiniStatementTrans.State.ONLINE2.toString(), transOnlineAction2);

        // 余额显示
        ActionDispSingleLineMsg displayInfoAction = new ActionDispSingleLineMsg(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                Currency currency = FinancialApplication.getSysParam().getCurrency();
                String content = FinancialApplication.getConvert().amountMinUnitToMajor(transData.getBalance(),
                        currency.getCurrencyExponent(), true);
                String amount = context.getString(R.string.trans_amount_default,
                        currency.getName(), content);

                ((ActionDispSingleLineMsg) action).setParam(getCurrentContext(), handler,
                        context.getString(R.string.trans_opening_account), context.getString(R.string.balance_prompt), amount, 3);
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
        ActionPrintTransReceipt printTransReceiptAction = new ActionPrintTransReceipt(transData, handler);
        bind(State.PRINT_TICKET.toString(), printTransReceiptAction);

        /*ActionDispTransDetailVertical dispTransDetail = new ActionDispTransDetailVertical(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                ((ActionDispTransDetailVertical) action).setParam(getCurrentContext(), handler,
                        context.getString(R.string.trans_esamsat), prepareDisp());
                TransContext.getInstance().setCurrentAction(action);
            }
        });
        bind(State.DETAIL_TRANSAKSI.toString(), dispTransDetail);*/

        // 执行的第一action
        /*if (amount == null || amount.length() == 0) {
            gotoState(State.ENTER_AMOUNT.toString());
        } else {
            gotoState(State.CLSS_PREPROC.toString());
        }*/
//        gotoState( State.CLSS_PREPROC.toString());
        /*transData.setPrintTimeout("y");
        transData.setAmount("000000000000");
        transData.setBillingId("01234567891234");*/

        //--long[] objPulsa = TransData.getTransNumAndAmountPulsa(ETransType.INQ_PULSA_DATA.toString(), TransData.ETransStatus.NORMAL, false);
        long[] objPulsa_ = TransData.getTransNumAndAmountPulsa(ETransType.INQ_PULSA_DATA.toString(), TransData.ETransStatus.NORMAL, true);
        long[] objPulsa1 = TransData.getTransNumAndAmountPulsa(ETransType.PURCHASE_PULSA_DATA.toString(), TransData.ETransStatus.NORMAL, false);
        long[] objPulsa1_ = TransData.getTransNumAndAmountPulsa(ETransType.PURCHASE_PULSA_DATA.toString(), TransData.ETransStatus.NORMAL, true);
        long[] objPulsa2 = TransData.getTransNumAndAmountPulsa(ETransType.OVERBOOKING_PULSA_DATA.toString(), TransData.ETransStatus.NORMAL, false);
        long[] objPulsa2_ = TransData.getTransNumAndAmountPulsa(ETransType.OVERBOOKING_PULSA_DATA.toString(), TransData.ETransStatus.NORMAL, true);

        //--long[] feePulsa = TransData.getTransFeeNumAndAmount(ETransType.INQ_PULSA_DATA.toString(), TransData.ETransStatus.NORMAL, false);
        long[] feePulsa_ = TransData.getTransFeeNumAndAmount(ETransType.INQ_PULSA_DATA.toString(), TransData.ETransStatus.NORMAL, true);
        long[] feePulsa1 = TransData.getTransFeeNumAndAmount(ETransType.PURCHASE_PULSA_DATA.toString(), TransData.ETransStatus.NORMAL, false);
        long[] feePulsa1_ = TransData.getTransFeeNumAndAmount(ETransType.PURCHASE_PULSA_DATA.toString(), TransData.ETransStatus.NORMAL, true);
        long[] feePulsa2 = TransData.getTransFeeNumAndAmount(ETransType.OVERBOOKING_PULSA_DATA.toString(), TransData.ETransStatus.NORMAL, false);
        long[] feePulsa2_ = TransData.getTransFeeNumAndAmount(ETransType.OVERBOOKING_PULSA_DATA.toString(), TransData.ETransStatus.NORMAL, true);

        //String a = "body86":"{\"productId\":\"ca70d3281\",\"productName\":\"SIMPATI 10000\",\"productDesc\":\"SIMPATI 10000\",\"operator\":\"TELKOMSEL\",\"basePrice\":\"25000\",\"sellPrice\":\"30000\",\"fee\":\"5000\",\"type\":\"PULSA\"}".toString();

        gotoState(State.DETAIL_TRANSAKSI.toString());

    }

    String cnvrtAmount(String amt) {
        Currency currency = FinancialApplication.getSysParam().getCurrency();
        String amount = currency.getName() + " " + FinancialApplication.getConvert()
                .amountMinUnitToMajor(amt, currency.getCurrencyExponent(), true);
        return amount;
    }

    private LinkedHashMap<String, String> prepareDisp() {
        LinkedHashMap<String, String> mapData = new LinkedHashMap<>();
        String data = "PDAMIDPEL    : 222222222222|NAMA         : LIM SUI SIAN|BLN          : SEP18|REFF         : 70FF04E6617742E99D9E70D0BDA781A5|TAG          : 138479|ADMIN        : 2500|TOTAL        : 140979|";
        String[] record = data.split("\\|");

        for (int i = 0; i < record.length; i++) {
            String[] column = record[i].split(":");
            if (column[0].trim().equalsIgnoreCase("TAG") ||
                    column[0].trim().equalsIgnoreCase("ADMIN") ||
                    column[0].trim().equalsIgnoreCase("TOTAL")) {

                mapData.put(column[0].trim(), cnvrtAmount(column[1].trim()));
            } else {
                mapData.put(column[0].trim(), column[1].trim());
            }
        }

        return mapData;
    }

    protected enum State {
        //        BUKA_REK,
        INPUT_BILLING,
        ENTER_AMOUNT,
        CHECK_CARD,
        ENTER_TIP,
        INPUT_DATA,
        INPUT_DATA2,
        INPUT_DATA3,
        INPUT_DATA4,
        INPUT_DATA5,
        DETAIL_TRANSAKSI,
        ENTER_PIN,
        ONLINE,
        ONLINE2,
        ACCOUNT_LIST,
        INFO_SALDO,
        EMV_PROC,
        CLSS_PREPROC,
        CLSS_PROC,
        BALANCE_DISP,
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
        /*if ((state != State.SIGNATURE) && (state != State.BALANCE_DISP)) {
            // action结果检查，如果失败，结束交易
            int ret = result.getRet();
            if (ret != TransResult.SUCC) {
                transEnd(result);
                return;
            }
        }*/

        switch (state) {
            case INPUT_BILLING:
                String d = (String) result.getData();
                Log.d("teg", " data : " + d);
                gotoState(State.INPUT_DATA.toString());
                break;
            case ENTER_AMOUNT:
                onEnterAmount(result);
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
                //onEmvProc(result);
                onChooseAccount(result);
                break;
            case CLSS_PREPROC:
                gotoState(State.CHECK_CARD.toString());
                break;
            case CLSS_PROC:
                afterClssProcess(result);
                break;
            case BALANCE_DISP:
                onDisplayBalance(result);
                break;
            case SIGNATURE:
                onSignature(result);
                break;
            case ACCOUNT_LIST:
                //toSignOrPrint();
                String accNo = ((String) result.getData().toString());
                String[] no = transData.getAccNo().split("#");
                String[] type = transData.getAccType().split("#");
                int i = no.length;
                int a = type.length;
                for (int n = 0; n < no.length; n++) {
                    if (no[n].equals(accNo)) {
                        transData.setAccNo(no[n]);
                        transData.setAccType(type[n]);
                        // inc transno
                        transData.setTransNo(transData.getTransNo() + 1);
                    }
                }
                transData.setTransType(ETransType.PEMBUKAAN_REK.toString());
                gotoState(State.ONLINE2.toString()); //pembukaan rekening
//                gotoState( State.INPUT_DATA.toString());
                break;
            case INFO_SALDO:
                toSignOrPrint();
                break;
            case DETAIL_TRANSAKSI:

                if (result.getRet() == TransResult.ERR_ABORTED) {
                    int ix = (int) result.getRet();
                    Log.d("teg", "ret : " + ix);
                    transEnd(result);
                }

                break;
            case INPUT_DATA:
                onInputData(result);
                break;
            case INPUT_DATA2:
                onInputData2(result);
                break;
            case INPUT_DATA5:
                String[] res = (String[]) result.getData();
                String kodeBayar = res[0]; //ex : 222222222222
                String productId = res[1];
                String fee = res[2]; //ex : PDAMKOBDG

                Log.d("teg", "kodeBayar : " + kodeBayar + " productId : " + productId + " fee : " + fee);

                //onInputData5(result);
                transEnd(null);
                break;
            case INPUT_DATA4:
                onInputData4(result);
                break;
            case PRINT_TICKET:
            default:
                transEnd(result);
                break;
        }
    }

    public static String paddingKiri(String s, int n) {
        return StringUtils.leftPad(s, n, " ");
    }

    protected void onInputData(ActionResult result) {
        String[] data = (String[]) result.getData();
        /*data[0] = paddingKiri(data[0], 35);
        data[1] = paddingKiri(data[1], 35);
        data[3] = paddingKiri(data[3], 15);
        data[4] = paddingKiri(data[4], 20);*/
        String d = data[0] + data[1] + data[2] + data[3] + data[4];
        Log.d("teg", "data : |" + d + "|");
        /*transData.setF48bukaRek(f48);
        Log.d("teg", "f48 : |"+transData.getF48bukaRek()+"|");*/

        gotoState(State.INPUT_DATA2.toString());
    }

    private String[] getData(String names) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i][0].equalsIgnoreCase(names)) {
                return new String[]{arr[i][0], arr[i][1], arr[i][2]};
            }
        }
        return null;
    }

    protected void onInputData4(ActionResult result) {
        String[] data = (String[]) result.getData();

        Log.d("teg", "data " + data.toString());
    }

    protected void onInputData5(ActionResult result) {
        ProductData productData = (ProductData) result.getData();

        Log.d("teg", "onInputData5 " + productData.toString());
    }

    protected void onInputData2(ActionResult result) {
        String isIndopayMode = FinancialApplication.getSysParam().get(SysParam.INDOPAY_MODE);
        String bankName = "", noRekening = "", nominal = "", reffNo = "";

        String[] data = (String[]) result.getData();
        bankName = data[0];
        noRekening = data[1];
        nominal = data[2];
        reffNo = data[3];

        if (SysParam.Constant.YES.equals(isIndopayMode)) {
            transData.setAmount(String.format("%s00", nominal));
        } else {
            transData.setAmount(nominal);
        }

        //String d = data[0] + data[1] + data[2] + data[3] ;
        String data1[] = getData(bankName);

        String f127 = data1[1]; //Kode Bin Bank Tujuan
        String f59 = data1[2];

        transData.setField59(f59);
        transData.setDestBank(bankName);
        transData.setField103(noRekening);
        transData.setField127(f127);

        Log.d("teg", "bank : |" + data1[0] + "-" + f127 + "-" + f59 + "|");
        Log.d("teg", "x : |" + bankName + "-" + noRekening + "-" + nominal + "-" + reffNo + "|");

        transEnd(new ActionResult(TransResult.SUCC, null));
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

    protected void onEnterAmount(ActionResult result) {
        // 保存交易金额

        Log.d(TAG, "Sandy.onEnterAmount=" + result.getData());
        String isIndopayMode = FinancialApplication.getSysParam().get(SysParam.INDOPAY_MODE);
        String amount = ((String) result.getData()).replace(",", "");
        Log.d("teg", "amount " + amount);


        ActionSearchCard action = (ActionSearchCard) getAction(State.CHECK_CARD.toString());
        action.setAmount(amount);
        //sandy
        if (SysParam.Constant.YES.equals(isIndopayMode))
            transData.setAmount(String.format("%s00", amount));
        else
            transData.setAmount(amount);
        gotoState(State.CLSS_PREPROC.toString());
    }

    protected void onCheckCard(ActionResult result) {
        CardInformation cardInfo = (CardInformation) result.getData();
        saveCardInfo(cardInfo, transData, true);
        // 手输卡号处理
        byte mode = cardInfo.getSearchMode();
        if (mode != SearchMode.TAP) {
            if (FinancialApplication.getSysParam().get(SysParam.SUPPORT_TIP).equals(SysParam.Constant.NO)) { //TODO mode 2
                if (mode == SearchMode.INSERT) {
                    gotoState(State.EMV_PROC.toString());
                } else {
                    transData.setTransType(ETransType.BALANCE_INQUIRY.toString());
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
            transData.setTransType(ETransType.BALANCE_INQUIRY.toString());
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
        transData.saveTrans();
        // 判断是否需要电子签名或打印
        toSignOrPrint();
    }


    protected void onChooseAccount(ActionResult result) {
        //Sandy : Choose an account to be listed in combobox
        //
        gotoState(State.ACCOUNT_LIST.toString());

    }

    private void test() {
        byte test[] = "000001000000".getBytes();
        int ret = EMVCallback.EMVSetTLVData((short) 0x9F02, test, 6);
        if (ret != RetCode.EMV_OK) {
            Log.i(TAG, "9f02 gagal");
        }

        byte test2[] = "51".getBytes();
        int ret2 = EMVCallback.EMVSetTLVData((short) 0x9C, test2, 1);
        if (ret2 != RetCode.EMV_OK) {
            Log.i(TAG, "9c gagal");
        }

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
            Log.d(TAG, "Sandy.onEmvProc");

            String isIndopayMode = FinancialApplication.getSysParam().get(SysParam.INDOPAY_MODE);
            if (SysParam.Constant.YES.equals(isIndopayMode))
                transData.setAmount(transData.getAmount().substring(0, transData.getAmount().length() - 2));

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
            gotoState(State.BALANCE_DISP.toString());

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
        Log.d(TAG, "Sandy=SaleTrans.afterClssProcess called!");
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

    protected void onDisplayBalance(ActionResult result) {
        // 电子现金交易无需签名
        if (Component.isSignatureFree(transData)) {// 免签
            transData.setSignFree(true);
        } else {
            transData.setSignFree(false);
        }
        transData.updateTrans();
        gotoState(State.PRINT_TICKET.toString());
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
