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
import android.text.TextUtils;
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
import com.pax.pay.trans.action.ActionDispTransDetailVertical;
import com.pax.pay.trans.action.ActionEmvProcess;
import com.pax.pay.trans.action.ActionEnterPin;
import com.pax.pay.trans.action.ActionEnterPin.EEnterPinType;
import com.pax.pay.trans.action.ActionInputDataTransfer;
import com.pax.pay.trans.action.ActionInputDataTransferNew;
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

import java.util.LinkedHashMap;

public class FundTransferTrans extends BaseTrans {

    private static final String TAG = "FundTransferTrans";
    private byte searchCardMode = SearchMode.KEYIN; // Find card method
    private String amount;

    private boolean isFreePin;
    private boolean isSupportBypass = true;

    public FundTransferTrans(Context context, Handler handler, String amount, boolean isFreePin,
                             TransEndListener transListener) {
        super(context, handler, ETransType.TRANSFER, transListener);
        this.amount = amount;
        this.isFreePin = isFreePin;
    }

    @Override
    public void bindStateOnAction() {
        String isIndopayMode = FinancialApplication.getSysParam().get(SysParam.INDOPAY_MODE);

        ActionInputTransData inputAmountAction = new ActionInputTransData(handler, ActionInputTransData.INFO_TYPE_SALE, null);
        inputAmountAction.setTitle(context.getString(R.string.trans_transfer));
        inputAmountAction.setPrompt1(context.getString(R.string.prompt_input_amount));
        inputAmountAction.setInputType1(ActionInputTransData.EInputType.AMOUNT);
        inputAmountAction.setMaxLen1(9);
        inputAmountAction.setMinLen1(0);
        bind(State.ENTER_AMOUNT.toString(), inputAmountAction);

        // Search Card action
        searchCardMode = Component.getCardReadMode(transType);
        ActionSearchCardCustom searchCardAction = new ActionSearchCardCustom(null);
        searchCardAction.setTitle(context.getString(R.string.trans_transfer));
        searchCardAction.setMode(searchCardMode);
        //Sandy : since we modifying the 00 decimal point
        //we cut 00 at rear value
        if (amount != null && amount.length() > 0) {
            if (SysParam.Constant.YES.equals(isIndopayMode))
                searchCardAction.setAmount(transData.getAmount().substring(0, transData.getAmount().length() - 2));
            else
                searchCardAction.setAmount(transData.getAmount());
        }

        ActionInputTransData inputId = new ActionInputTransData(handler, ActionInputTransData.INFO_TYPE_SALE, null);
        inputId.setTitle(context.getString(R.string.trans_transfer));
        inputId.setInfoTypeSale(context.getString(R.string.prompt_input_id), EInputType.NUM, 12, false);
        bind(State.INPUT_ID.toString(), inputId);

        ActionInputDataTransferNew enter = new ActionInputDataTransferNew(handler, ActionInputTransData.INFO_TYPE_SALE, null);
        enter.setTitle(context.getString(R.string.trans_transfer));
        bind(State.INPUT_DATA2.toString(), enter);

        searchCardAction.setUiType(searchCardMode == ActionSearchCardCustom.SearchCustomMode.TAP ? ActionSearchCardCustom.ESearchCardUIType.QUICKPASS : ActionSearchCardCustom.ESearchCardUIType.DEFAULT);
        bind(State.CHECK_CARD.toString(), searchCardAction);

        // input CVN2 information
        ActionInputTransData enterInfosAction = new ActionInputTransData(handler, ActionInputTransData.INFO_TYPE_SALE, null);
        enterInfosAction.setTitle(context.getString(R.string.trans_transfer));
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

                if (!isFreePin) {
                    isSupportBypass = false;
                }
                ((ActionEnterPin) action).setParam(getCurrentContext(), context.getString(R.string.trans_transfer),
                        transData.getPan(), isSupportBypass, context.getString(R.string.prompt_bankcard_pwd),
                        context.getString(R.string.prompt_no_password), transData.getAmount(), EEnterPinType.ONLINE_PIN, transData.getEnterMode());
            }
        });
        bind(State.ENTER_PIN.toString(), enterPinAction);

        // emv处理action
        ActionEmvProcess emvProcessAction = new ActionEmvProcess(handler, transData);
        bind(State.EMV_PROC.toString(), emvProcessAction);

        ActionEmvProcess emvProcessAction2 = new ActionEmvProcess(handler, transData);
        bind(State.EMV_PROC2.toString(), emvProcessAction2);

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


        ActionTransOnline transOnlineAction3 = new ActionTransOnline(transData);
        bind(State.ONLINE3.toString(), transOnlineAction3);



        ActionChooseAccountList accountListAction = new ActionChooseAccountList(getCurrentContext(),
                transData, context.getString(R.string.trans_transfer), "Pilih Rekening");
        bind(State.ACCOUNT_LIST.toString(), accountListAction);

        ActionTransOnline transOnlineSaldoAction = new ActionTransOnline(transData);
        bind(State.ONLINE_TRX.toString(), transOnlineSaldoAction);

        ActionDispTransDetailVertical dispTransDetail = new ActionDispTransDetailVertical(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                LinkedHashMap<String, String> map = prepareDisp();
                ((ActionDispTransDetailVertical) action).setParam(getCurrentContext(), handler,
                        context.getString(R.string.trans_transfer), map);
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

        gotoState(State.CLSS_PREPROC.toString());

    }

    protected enum State {
        ENTER_AMOUNT,
        INPUT_ID,
        INPUT_DATA2,
        CHECK_CARD,
        ENTER_TIP,
        ENTER_INFO,
        ENTER_PIN,
        ONLINE,
        ONLINE2,
        ONLINE3,
        ACCOUNT_LIST,
        ONLINE_TRX,
        EMV_PROC,
        EMV_PROC2,
        CLSS_PREPROC,
        CLSS_PROC,
        DETAIL_TRANSAKSI,
        SIGNATURE,
        PRINT_TICKET
    }

    public static String arr[][] = {
            {"Bank Rakyat Indonesia", "002", "OTA"},
            {"Bank Central Asia", "014", "OTA"},
            {"Bank Negara Indonesia", "009", "OTA"},
            {"Permata Bank", "013", "OTA"},
            {"Bank Tabungan Negara", "200", "OTA"},
            {"Bank Sumselbabel", "120", "OTA"},
            {"Standard Chartered Bank", "050", "OTA"},
            {"Bank Sinarmas", "153", "OTA"},
            {"Bank Panin", "019", "OTA"},
            {"Bank Papua", "132", "OTA"},
            {"Bank OCBC NISP", "028", "OTA"},
            {"Bank Muamalat Indonesia", "147", "OTA"},
            {"Bank Mega", "426", "OTA"},
            {"Bank Maybank Indonesia", "016", "OTA"},
            {"Bank HSBC Indonesia", "087", "OTA"},
            {"Bank Danamon Indonesia", "011", "OTA"},
            {"Bank Commonwealth", "950", "OTA"},
            {"Citibank", "031", "OTA"},
            {"Bank CIMB Niaga", "022", "OTA"},
            {"Bank Bukopin", "022", "OTA"},
            {"Bank Agris", "945", "OTA"},
            {"Bank Artha Graha", "037", "OTA"},
            {"Bank BCA Syariah", "536", "OTA"},
            {"Bank BPD DIY", "112", "OTA"},
            {"Bank BPD Kaltim Kaltara", "124", "OTA"},
            {"Bank BRI Syariah", "422", "OTA"},
            {"Bank BTPN", "213", "OTA"},
            {"Bank BTPN Syariah", "547", "OTA"},
            {"Bank Bumi Arta", "076", "OTA"},
            {"Bank China Construction Bank Indonesia", "036", "OTA"},
            {"Bank CTBC Indonesia", "949", "OTA"},
            {"Bank DBS", "046", "OTA"},
            {"Bank DKI", "111", "OTA"},
            {"Bank Ina Perdana", "513", "OTA"},
            {"Bank Jabar Banten Syariah", "425", "OTA"},
            {"Bank Jasa Jakarta", "472", "OTA"},
            {"Bank Jateng", "113", "OTA"},
            {"Bank Jatim", "114", "OTA"},
            {"Bank Jtrust", "095", "OTA"},
            {"Bank Kalbar", "123", "OTA"},
            {"Bank KEB Hana", "484", "OTA"},
            {"Bank Maspion", "157", "OTA"},
            {"Bank Mayapada Internasional", "097", "OTA"},
            {"Bank Mega Syariah", "506", "OTA"},
            {"Bank Mestika Dharma", "151", "OTA"},
            {"Bank Multiarta Sentosa", "548", "OTA"},
            {"Bank of China", "069", "OTA"},
            {"Bank Pembangunan Daerah Banten", "137", "OTA"},
            {"Bank Panin Dubai Syariah", "517", "OTA"},
            {"Bank Riau", "119", "OTA"},
            {"Bank Royal", "501", "OTA"},
            {"Bank Sahabat Sampoerna", "523", "OTA"},
            {"Bank SBI Indonesia", "498", "OTA"},
            {"Bank Shinhan Indonesia", "152", "OTA"},
            {"Bank Nagari", "118", "OTA"},
            {"Bank Sulselbar", "126", "OTA"},
            {"Bank Syariah Bukopin", "521", "OTA"},
            {"Bank Syariah Indonesia", "451", "OTA"},
            {"Bank Mandiri", "008", "OTA"},
            {"Bank UOB Indonesia", "023", "OTA"},
            {"Bank Victoria International", "566", "OTA"},
            {"Bank Woori Saudara", "212", "OTA"},
            {"Bank BPD Bali", "129", "OTA"},
            {"Bank QNB Indonesia", "167", "OTA"},
            {"Bank MNC Internasional", "485", "OTA"},
            {"Bank NTT", "130", "OTA"},
            {"Bank Dinar", "526", "OTA"},
            {"Bank NTB", "128", "OTA"},
            {"Bank Kalsel", "122", "OTA"},
            {"Bank ICBC Indonesia", "164", "OTA"},
            {"Bank Sumut", "117", "OTA"},
            {"Bank BNI Syariah", "427", "OTA"},
            {"Bank Kalteng", "125", "OTA"},
            {"Bank SulutGo", "127", "OTA"},
            {"Bank Lampung", "121", "OTA"},
            {"Bank Mayora", "553", "OTA"},
            {"Bank Ganesha", "161", "OTA"},
            {"Bank Nobu", "503", "OTA"},
            {"Bank BJB", "110", "JAB"}
//            {"--Pilih Bank--", "", ""  }

    };

    private String[] getData(String names) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i][0].equalsIgnoreCase(names)) {
                return new String[]{arr[i][0], arr[i][1], arr[i][2]};
            }
        }
        return null;
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

        String data1[] = getData(bankName);

        String f127 = data1[1]; //kode bank tujuan
        String f59 = data1[2];  // OTA / JAB

        transData.setField59(f59);
        transData.setDestBank(bankName);
        transData.setField103(noRekening);
        transData.setField127(f127);
        transData.setRefferenceNo(reffNo);

        /*Log.d("teg", "bank : |" + data1[0] + "-" + f127 + "-" + f59 + "|");
        Log.d("teg", "x : |" + bankName + "-" + noRekening + "-" + nominal + "-" + reffNo + "|");*/



        //Sandy : swipe or chip will switch here...
        if(transData.getEnterMode() == EnterMode.SWIPE){
            gotoState(State.ONLINE2.toString());
        }else if(transData.getEnterMode() == EnterMode.INSERT){
            gotoState(State.EMV_PROC2.toString());
        }

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
        if (state != State.SIGNATURE) {
            // action结果检查，如果失败，结束交易
            int ret = result.getRet();
            if (ret != TransResult.SUCC) {
                transEnd(result);
                return;
            }
        }


        switch (state) {
            case INPUT_ID:
                afterInputId(result);
                break;
            case INPUT_DATA2:
                onInputData2(result);
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
                break;
            case CLSS_PREPROC:
                gotoState(State.CHECK_CARD.toString());
                break;
            case CLSS_PROC:
                afterClssProcess(result);
                break;
            case DETAIL_TRANSAKSI:
                //Sandy : swipe or chip will switch here...
                if(transData.getEnterMode() == EnterMode.SWIPE){
                    gotoState(State.ONLINE3.toString());
                }else if(transData.getEnterMode() == EnterMode.INSERT){
                    gotoState(State.EMV_PROC.toString());
                }
                break;
            case ONLINE3: //Sandy : finishing the swipe transaction
                onOnline3(result);
                break;
            case SIGNATURE:
                onSignature(result);
                break;
            case ENTER_AMOUNT:
                onEnterAmount(result);
                break;
            case ACCOUNT_LIST:
                //toSignOrPrint();

                if (result.getRet() == TransResult.SUCC) {
                    AccountData accNo = ((AccountData) result.getData());
                    transData.setAccNo(accNo.getAccountNumber());
                    transData.setAccType(accNo.getAccountType());

                    if(accNo.getAccountType().equals(AccountData.SAVING))
                        transData.setTransType(ETransType.TRANSFER_INQ.toString());
                    else
                        transData.setTransType(ETransType.TRANSFER_INQ_2.toString());

                    transData.setTransNo(transData.getTransNo() + 1);
                    gotoState(State.INPUT_DATA2.toString());
                }





                break;
            case ONLINE_TRX:
                toSignOrPrint();
                break;
            case ONLINE2:
                afterInquiry();
                break;
            case EMV_PROC2:
                onEmvProc2(result);
                break;
            case PRINT_TICKET:
            default:
                transEnd(result);
                break;
        }
    }

    private int validate() {
        int err = 0;
        String amt = transData.getAmount();
        String fee = transData.getField28();
        String f48 = transData.getField48();
        String reff = transData.getRefNo();

        if (TextUtils.isEmpty(amt)) {
            err = TransResult.ERR_INVALID_RESPONSE_DATA;
        }

        // remark abdul
        /*if (TextUtils.isEmpty(fee)) {
            err = TransResult.ERR_INVALID_RESPONSE_DATA;
        }*/

        if (TextUtils.isEmpty(f48)) {
            err = TransResult.ERR_INVALID_RESPONSE_DATA;
        }

        if (TextUtils.isEmpty(reff)) {
            err = TransResult.ERR_INVALID_RESPONSE_DATA;
        }

        return err;
    }

    private LinkedHashMap<String, String> prepareDisp() {
        if (transData == null) {
            return null;
        }

        String amt = transData.getAmount().substring(0, transData.getAmount().length() - 2);

        Currency currency = FinancialApplication.getSysParam().getCurrency();
        String amount = currency.getName() + " " + FinancialApplication.getConvert()
                .amountMinUnitToMajor(amt, currency.getCurrencyExponent(), true);


        String biayaAdmin = currency.getName() + " " + FinancialApplication.getConvert()
                .amountMinUnitToMajor(String.valueOf(transData.getField28()),
                        currency.getCurrencyExponent(), true);

        long total = Long.parseLong(amt) + Long.parseLong(transData.getField28());

        String totaltrf = currency.getName() + " " + FinancialApplication.getConvert()
                .amountMinUnitToMajor(String.valueOf(total),
                        currency.getCurrencyExponent(), true);

        LinkedHashMap<String, String> hashMap = new LinkedHashMap<>();

        String data = transData.getField48();
        String destName = data.substring(0, 30);        //nama rekening tujuan
        String reffnum = data.substring(30, 46);        //reffnum
        //String sourceName = data.substring(46, 76);     //nama rekening asal

        Log.d("teg", "data : " + data);

        hashMap.put(context.getString(R.string.detail_rek_asal), transData.getAccNo().trim());  //rekening asal
        //hashMap.put(context.getString(R.string.detail_nm_rek_asal), sourceName.trim()); //nama rekening asal
        hashMap.put(context.getString(R.string.detail_norek_tujuan), transData.getField103()); //rekening tujuan
        hashMap.put(context.getString(R.string.detail_nama_rek_tujuan), destName.trim()); //nama rekening tujuan
        hashMap.put(context.getString(R.string.detail_bank_tujuan), transData.getDestBank()); //bank tujuan
        hashMap.put(context.getString(R.string.detail_nominal_tf), amount); //jumlah transfer

        String sShowAdminFee = FinancialApplication.getSysParam().get(SysParam.SHOW_ADMIN_FEE);
        //set default = true
        boolean showAdminFee = true;
        //if there is am admin fee configured, then follow it from database.
        if(sShowAdminFee != null){
            showAdminFee = sShowAdminFee.equals("1") == true ? true : false;
        }
        //admin fee
       if(transData.getField28() != null && Integer.parseInt(transData.getField28()) != 0 && showAdminFee == true)
            hashMap.put(context.getString(R.string.detail_biaya_admin), biayaAdmin); //biaya admin

        hashMap.put(context.getString(R.string.detail_total), totaltrf); //total
        hashMap.put(context.getString(R.string.detail_referensi), transData.getRefferenceNo()); //no referansi

        return hashMap;
    }

    protected void afterInquiry() {
        int er = validate();
        if (er != 0){
            transEnd(new ActionResult(er, null));
            return;
        }else {
            //sandy
            if(transData.getAccType().equals(AccountData.SAVING))
                transData.setTransType(ETransType.TRANSFER.toString());
            else
                transData.setTransType(ETransType.TRANSFER_2.toString());

            transData.setTransNo(transData.getTransNo() + 1);
            gotoState(State.DETAIL_TRANSAKSI.toString());
        }
    }

    // Sandy : Determine if electronic signature or printing is required
    protected void toSignOrPrint() {
        //Sandy : only if there is an response code 68 has been received
        if(transData.getResponseCode().equals("68"))
            transData.setPrintTimeout("y");

        if (transData.getHasPin()) {
            transData.setSignFree(true);
            gotoState(State.PRINT_TICKET.toString());
        } else {
            transData.setSignFree(false);
            gotoState(State.SIGNATURE.toString());
        }
        transData.setFeeTotalAmount(transData.getField28());
        transData.updateTrans();
    }

    protected void afterInputId(ActionResult result) {


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

    protected void onEnterAmount(ActionResult result) {
        // 保存交易金额

        Log.d(TAG, "Sandy.onEnterAmount=" + result.getData());
        String isIndopayMode = FinancialApplication.getSysParam().get(SysParam.INDOPAY_MODE);
        String amount = ((String) result.getData()).replace(",", "");
        Log.d("teg", "amount " + amount);


        ActionSearchCardCustom action = (ActionSearchCardCustom) getAction(State.CHECK_CARD.toString());
        action.setAmount(amount);
        //sandy
        if (SysParam.Constant.YES.equals(isIndopayMode)) {
            transData.setAmount(String.format("%s00", amount));
        } else {
            transData.setAmount(amount);
        }
        gotoState(State.ONLINE2.toString());
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
            gotoState(State.ENTER_PIN.toString());
        }
    }

    protected void onEnterPin(ActionResult result) {
        String pinBlock = (String) result.getData();
        transData.setPin(pinBlock);
        if (pinBlock != null && pinBlock.length() > 0) {
            transData.setHasPin(true);
        }
        gotoState(State.ONLINE.toString());
    }

    protected void onOnline(ActionResult result) {
        if (transData.getEnterMode() == EnterMode.QPBOC) {
            transData.setEmvResult((byte) ETransResult.ONLINE_APPROVED.ordinal());
        }

        String isIndopayMode = FinancialApplication.getSysParam().get(SysParam.INDOPAY_MODE);
        if (SysParam.Constant.YES.equals(isIndopayMode))
            transData.setAmount(transData.getAmount().substring(0, transData.getAmount().length() - 2));

        gotoState(State.ACCOUNT_LIST.toString());
    }

    /**
     * Sandy : for online swipe result
     */
    protected void onOnline3(ActionResult result) {
        if (result.getRet() == TransResult.SUCC) {
            String isIndopayMode = FinancialApplication.getSysParam().get(SysParam.INDOPAY_MODE);
            if (SysParam.Constant.YES.equals(isIndopayMode))
                transData.setAmount(transData.getAmount().substring(0, transData.getAmount().length() - 2));

            transData.setFeeTotalAmount(transData.getField28());
            transData.setReprintData(transData.getField48());
            transData.saveTrans();
            toSignOrPrint();
        }else
             transEnd(result);



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

            transData.setFeeTotalAmount(transData.getField28());
            transData.setReprintData(transData.getField48());
            transData.saveTrans();
            if (transResult == ETransResult.ONLINE_APPROVED) {

                toSignOrPrint();
                return;
            }

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

    protected void onEmvProc2(ActionResult result) {
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

            if(transData.getAccType().equals(AccountData.SAVING))
                transData.setTransType(ETransType.TRANSFER.toString());
            else
                transData.setTransType(ETransType.TRANSFER_2.toString());

            transData.setTransNo(transData.getTransNo() + 1);
            gotoState(State.DETAIL_TRANSAKSI.toString());


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
            //String x = "";
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
