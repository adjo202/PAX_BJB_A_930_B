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
import com.pax.pay.trans.action.ActionInputESamsatData;
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

import org.apache.commons.lang.StringUtils;

import java.util.LinkedHashMap;


public class ESamsatTrans extends BaseTrans {

    private static final String TAG = "ESamsatTrans";
    private byte searchCardMode = SearchMode.KEYIN; // Find card method
    private String amount;

    private boolean isFreePin;
    private boolean isSupportBypass = true;

    private LinkedHashMap<String, String> mapData = new LinkedHashMap<>();

    public ESamsatTrans(Context context, Handler handler,boolean isFreePin, TransEndListener transListener) {
        super(context, handler, ETransType.E_SAMSAT_INQUIRY, transListener);
        this.isFreePin = isFreePin;
    }

    @Override
    public void bindStateOnAction() {

        ActionInputESamsatData enterInfosAction = new ActionInputESamsatData(handler, null);
        enterInfosAction.setTitle(context.getString(R.string.trans_esamsat));
        bind(State.INPUT_DATA.toString(), enterInfosAction);

        // Search Card action
        searchCardMode = Component.getCardReadMode(transType);
        ActionSearchCardCustom searchCardAction = new ActionSearchCardCustom(null);
        searchCardAction.setTitle(context.getString(R.string.trans_esamsat));
        searchCardAction.setMode(searchCardMode);
        //Sandy : since we modifying the 00 decimal point
        //we cut 00 at rear value


        searchCardAction.setUiType(searchCardMode == ActionSearchCardCustom.SearchCustomMode.TAP ? ActionSearchCardCustom.ESearchCardUIType.QUICKPASS : ActionSearchCardCustom.ESearchCardUIType.DEFAULT);
        bind(State.CHECK_CARD.toString(), searchCardAction);

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
                ((ActionEnterPin) action).setParam(getCurrentContext(), context.getString(R.string.trans_esamsat),
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

        ActionEmvProcess emvProcessAction3 = new ActionEmvProcess(handler, transData);
        bind(State.EMV_PROC1.toString(), emvProcessAction3);

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

        ActionChooseAccountList accountListAction = new ActionChooseAccountList(getCurrentContext(), transData, transType.getTransName(), "Pilih Rekening");
        bind(State.ACCOUNT_LIST.toString(), accountListAction);

        ActionDispTransDetailVertical dispTransDetail = new ActionDispTransDetailVertical(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                ((ActionDispTransDetailVertical) action).setParam(getCurrentContext(), handler,
                        context.getString(R.string.trans_esamsat), mapData);
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
        //DISP,
        //ENTER_AMOUNT,
        INPUT_DATA,
        CHECK_CARD,
        ENTER_TIP,
        ENTER_PIN,
        ONLINE,
        ONLINE2, //inquiry
        ONLINE3, //payment
        ACCOUNT_LIST,
        EMV_PROC,
        EMV_PROC2, //payment
        EMV_PROC1, //inquiry
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
        if ((state != State.SIGNATURE) || (state != State.DETAIL_TRANSAKSI)) {
            // action结果检查，如果失败，结束交易
            int ret = result.getRet();
            if (ret != TransResult.SUCC) {
                transEnd(result);
                return;
            }
        }

        switch (state) {

            case CHECK_CARD: // 检测卡的后续处理
                onCheckCard(result);
//                onCheckCard2(result);
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
            //case ONLINE2: //inquiry
            case EMV_PROC1:
                onEmvProc1(result);
                break;
            //case ONLINE3: //payment
            case EMV_PROC2:
                onEmvProc2(result);
                //gotoState(State.PRINT_TICKET.toString());
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
                //gotoState(State.ONLINE3.toString());
                gotoState(State.EMV_PROC2.toString());
                break;
            case SIGNATURE:
                onSignature(result);
                break;
            case INPUT_DATA:
                onInputData(result);

            case ACCOUNT_LIST:
                //toSignOrPrint();
                if (result.getRet() == TransResult.SUCC) {
                    if(result.getData().getClass().equals(AccountData.class)){
                        AccountData accNo = ((AccountData) result.getData());
                        transData.setAccNo(accNo.getAccountNumber());
                        transData.setAccType(accNo.getAccountType());
                        transData.setTransNo(transData.getTransNo() + 1);
                        transData.setTransType(ETransType.E_SAMSAT_INQUIRY.toString());
                        setTransType(ETransType.E_SAMSAT_INQUIRY);
                        gotoState(State.INPUT_DATA.toString());
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
                        transData.setTransNo(transData.getTransNo() + 1);
                        transData.setTransType(ETransType.E_SAMSAT_INQUIRY.toString());
                        gotoState(State.INPUT_DATA.toString());
                        //gotoState(State.ONLINE2.toString()); //buat test
                    }
                }

                 */



                break;

            case PRINT_TICKET:
            default:
                transEnd(result);
                break;
        }
    }


    private void prepareDisp(ActionResult result) {
        /*NOMOR BAYAR 16	                0	16
        NOMOR RANGKA 25	                    16	41
        NOMOR MESIN 25	                    41	66
        NOMOR IDENTITAS 18	                66	84
        NAMA PEMILIK 25	                    84	109
        ALAMAT PEMILIK 40	                109	149
        NOMOR POLISI AN..9	                149	158
        WARNA PLAT AN..6	                158	164
        MILIK KENAMA N..3	                164	167
        JENIS KB A..15	                    167	182
        NAMA MEREK KB A..15	                182	197
        NAMA MODEL KB A..30	                197	227
        TAHUN BUATAN N..4	                227	231
        TGL AKHIR PAJAK LAMA (YMD) N..8	    231	239
        TGL AKHIR PAJAK BARU (YMD) N..8	    239	247
        POKOK BBN N..12	                    247	259
        DENDA BBN N..12	                    259	271
        POKOK PKB N..12	                    271	283
        DENDA PKB N..12	                    283	295
        POKOK SWD N..12	                    295	307
        DENDA SWD N..12	                    307	319
        POKOK ADM STNK N..12	            319	331
        POKOK ADM TNKB N..12	            331	343
        JUMLAH N..12	                    343	355
        KETERANGAN AN..90	                355	445
        RESERVED_01 AN..5	                445	450*/
        //String data = "3222302805190102MH1JFB119CK278890        JFB1E1280681             3217020811930006  ALDY RACHMAT SURYA       SETIABUDI REGENCI WING I NO 43H RT 06/18D 4799UARHITAM 001SEPEDA MOTOR   HONDA          ASTREA C100                   20122019090320220903000000000000000000000000000000200000000000000000000000035000000000000000000000000000000000000000000000235000TELAH DILAKUKAN REGIDENT RANMOR STNK TAHUNAN,STRUK INI SBG DOK LAIN YG DIPERSAMAKAN DGN SK12300";

        String data = transData.getField61().trim();
        transData.setSamsatKodeBayar(data);
        String kodeBayar = data.substring(0,16);
        String nama = data.substring(84,109).trim();
        String noPol = data.substring(149,158);
        String jenis = data.substring(167,182).trim();
        String tahun = data.substring(227,231);
        String jumlahBayar = data.substring(343,355);
        String biayaAdmin = transData.getField28();
        //String total = "0";
        String total = tambah(jumlahBayar, biayaAdmin);

        biayaAdmin = cnvrt(biayaAdmin);
        jumlahBayar = cnvrt(jumlahBayar);
        total = cnvrt(total);

        mapData.put(context.getString(R.string.detail_no_rek_tujuan), transData.getAccNo());
        mapData.put(context.getString(R.string.esamsat_kode_bayar), kodeBayar);
        mapData.put(context.getString(R.string.esamsat_nama_pemilik), nama);
        mapData.put(context.getString(R.string.esamsat_no_pol), noPol);
        mapData.put(context.getString(R.string.esamsat_jenis_kendaraan), jenis);
        mapData.put(context.getString(R.string.esamsat_tahun_pembuatan), tahun);
        mapData.put(context.getString(R.string.detail_jumlah_bayar), jumlahBayar);
        mapData.put(context.getString(R.string.detail_admin), biayaAdmin);
        mapData.put(context.getString(R.string.trans_total), total);
    }

    private String tambah(String a, String b){
        if (TextUtils.isEmpty(a))a="0";
        if (TextUtils.isEmpty(b))b="0";
        return String.valueOf(Long.parseLong(a) + Long.parseLong(b));
    }

    private String cnvrt(String a){
        Currency currency = FinancialApplication.getSysParam().getCurrency();
        if (TextUtils.isEmpty(a))a="0";
        return currency.getName() + " " + FinancialApplication.getConvert()
                .amountMinUnitToMajor(a, currency.getCurrencyExponent(), true);
    }

    protected void onInputData(ActionResult result) {
        String[] data = (String[]) result.getData();
        transData.setSamsatKodeBayar(data[0]);
        transData.setSamsatMerchantKode(data[2]);
        //gotoState(State.ONLINE2.toString());
        gotoState(State.EMV_PROC1.toString());
    }

    private LinkedHashMap<String, String> prepareDispBatalRek() {
        if (transData == null) {
            return null;
        }

        String amount = CurrencyConverter.convert(Utils.parseLongSafe(transData.getAmount(), 0), CurrencyConverter.getDefCurrency());
        LinkedHashMap<String, String> hashMap = new LinkedHashMap<>();
        String data = transData.getField48();
        String nama = data.substring(12, 47); //35
        String tempat = data.substring(47, 82); //35
        String tanggal = data.substring(82, 90); //8
        String no = data.substring(90, 105); //15
        String nik = data.substring(105, 125); //20

        hashMap.put(context.getString(R.string.detail_nama), nama);
        hashMap.put(context.getString(R.string.detail_nik), nik);
        hashMap.put(context.getString(R.string.detail_t_l), tempat);
        hashMap.put(context.getString(R.string.detail_tgl_l), tanggal);
        hashMap.put(context.getString(R.string.detail_no_hp), no);
        hashMap.put(context.getString(R.string.detail_nominal), amount);

        return hashMap;
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
        gotoState(State.ACCOUNT_LIST.toString());
    }


    protected void onEmvProc(ActionResult result) {
        ETransResult transResult = (ETransResult) result.getData();

        Component.emvTransResultProcess(transResult, transData);
        if (transResult == ETransResult.ONLINE_APPROVED || transResult == ETransResult.OFFLINE_APPROVED) {
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

    protected void onEmvProc1(ActionResult result) {
        ETransResult transResult = (ETransResult) result.getData();

        Component.emvTransResultProcess(transResult, transData);
        if (transResult == ETransResult.ONLINE_APPROVED || transResult == ETransResult.OFFLINE_APPROVED) {

            if (transResult == ETransResult.ONLINE_APPROVED) {

                transData.setTransNo(transData.getTransNo() + 1);
                transData.setTransType(ETransType.E_SAMSAT.toString());
                transData.setSamsatKodeBayar(transData.getField61()); //pinjem variable
                prepareDisp(result);
                gotoState(State.DETAIL_TRANSAKSI.toString());
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

    //23.000 --> 2.300.000
    String useDecimalPoint (String amt){
        String res = "0";
        if (StringUtils.isEmpty(amt))amt = "0";
        else amt=amt+"00";

        System.out.println("useDecimalPoint amt : " + amt);

        try {
            long amount  = Long.parseLong(amt);
            System.out.println("useDecimalPoint amount : " + amount);
            return new String(String.valueOf(amount));
        }catch (NumberFormatException e){
            return res;
        }

    }

    protected void onEmvProc2(ActionResult result) {
        ETransResult transResult = (ETransResult) result.getData();

        Component.emvTransResultProcess(transResult, transData);
        if (transResult == ETransResult.ONLINE_APPROVED || transResult == ETransResult.OFFLINE_APPROVED) {
            transData.setFeeTotalAmount(transData.getField28());
            //transData.setAmount(transData.getAmount().substring(0,10));
            transData.setAmount(useDecimalPoint(transData.getAmount().substring(0,10))); //dari host 000000235000 --> 000023500000
            transData.setReprintData(transData.getField61());
            transData.saveTrans();

            if (transResult == ETransResult.ONLINE_APPROVED) {
                //toSignOrPrint();
                gotoState(State.PRINT_TICKET.toString());
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
