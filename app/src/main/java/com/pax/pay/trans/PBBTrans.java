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
import com.pax.pay.trans.action.ActionClssPreProc;
import com.pax.pay.trans.action.ActionClssProcess;
import com.pax.pay.trans.action.ActionDispTransDetail;
import com.pax.pay.trans.action.ActionEmvProcess;
import com.pax.pay.trans.action.ActionEnterPin;
import com.pax.pay.trans.action.ActionInputPBB;
import com.pax.pay.trans.action.ActionPrintTransReceipt;
import com.pax.pay.trans.action.ActionSearchCard;
import com.pax.pay.trans.action.ActionSearchCardCustom;
import com.pax.pay.trans.action.ActionTransOnline;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.utils.Fox;
import com.pax.settings.SysParam;
import com.pax.settings.currency.Currency;
import com.pax.up.bjb.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class PBBTrans extends BaseTrans {

    private static final String TAG = "PBBTrans";
    private byte searchCardMode = ActionSearchCard.SearchMode.KEYIN; // Find card method

    private boolean isFreePin;
    private boolean isSupportBypass = true;

    public PBBTrans(Context context, Handler handler,boolean isFreePin, TransEndListener transListener) {
        super(context, handler, ETransType.PBB_INQ, transListener);
        this.isFreePin = isFreePin;
    }


    @Override
    public void bindStateOnAction() {
        String isIndopayMode = FinancialApplication.getSysParam().get(SysParam.INDOPAY_MODE);

        // Search Card action
        searchCardMode = Component.getCardReadMode(transType);
        ActionSearchCardCustom searchCardAction = new ActionSearchCardCustom(null);
        searchCardAction.setTitle("PBB-P2");
        searchCardAction.setMode(searchCardMode);
        String amount=null;
        if (amount != null && amount.length() > 0) {
            if (SysParam.Constant.YES.equals(isIndopayMode))
                searchCardAction.setAmount(transData.getAmount().substring(0, transData.getAmount().length() - 2));
            else
                searchCardAction.setAmount(transData.getAmount());
        }
        searchCardAction.setUiType(searchCardMode == ActionSearchCardCustom.SearchCustomMode.TAP ? ActionSearchCardCustom.ESearchCardUIType.QUICKPASS: ActionSearchCardCustom.ESearchCardUIType.DEFAULT);
        bind( State.CHECK_CARD.toString(), searchCardAction);

        // 输入密码action
        ActionEnterPin enterPinAction = new ActionEnterPin(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                // 如果是闪付凭密,设置isSupportBypass为false,需要输入密码
                if (!isFreePin) {
                    isSupportBypass = false;
                }
                ((ActionEnterPin) action).setParam(getCurrentContext(), "PBB-P2",
                        transData.getPan(), isSupportBypass, context.getString(R.string.prompt_bankcard_pwd),
                        context.getString(R.string.prompt_no_password), transData.getAmount(), ActionEnterPin.EEnterPinType.ONLINE_PIN, transData.getEnterMode());
            }
        });
        bind( State.ENTER_PIN.toString(), enterPinAction);

        ActionInputPBB actionInputPBB = new ActionInputPBB(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionInputPBB) action).setParam(getCurrentContext(), "PBB-P2",handler);
            }
        });
        bind(State.INPUT_DATA.toString(), actionInputPBB);

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

        ActionDispTransDetail dispTransDetail = new ActionDispTransDetail(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                LinkedHashMap<String, String> map = prepareDispPBB();
                ((ActionDispTransDetail) action).setParam(getCurrentContext(), handler,
                        context.getString(R.string.trans_detail), map);
                TransContext.getInstance().setCurrentAction(action);
            }
        });
        bind(State.DISP_DETAIL.toString(), dispTransDetail);

        // 联机action
        ActionTransOnline transOnlineAction2 = new ActionTransOnline(transData);
        bind( State.ONLINE2.toString(), transOnlineAction2);

        ActionEmvProcess emvProcessAction2 = new ActionEmvProcess(handler, transData);
        bind( State.EMV_PROC2.toString(), emvProcessAction2);

        // 打印action
        ActionPrintTransReceipt printTransReceiptAction = new ActionPrintTransReceipt(transData,handler);
        bind( State.PRINT_TICKET.toString(), printTransReceiptAction);

        gotoState( State.CLSS_PREPROC.toString());

    }

    protected enum State {
        CHECK_CARD,
        ENTER_PIN,
        INPUT_DATA,
        ONLINE,
        EMV_PROC,
        CLSS_PREPROC,
        CLSS_PROC,
        DISP_DETAIL,
        ONLINE2,
        EMV_PROC2,
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
                action.setMode(ActionSearchCard.SearchMode.SWIPE);
                action.setUiType(ActionSearchCard.ESearchCardUIType.DEFAULT);
                gotoState( State.CHECK_CARD.toString());
                return;
            }
        }


        switch (state) {
            case CHECK_CARD: // 检测卡的后续处理
                onCheckCardCustom(result);
                break;
            case ENTER_PIN: // 输入密码的后续处理
                onEnterPin(result);
                break;
            case INPUT_DATA:
                inputData(result);
                break;
            case ONLINE:
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
            case DISP_DETAIL:
                if (result.getRet() == TransResult.SUCC){
                    transData.setResponseCode(null);
                    transData.setTransType(ETransType.PBB_PAY.toString());
                    if (transData.getEnterMode() == TransData.EnterMode.INSERT){
                        gotoState(State.EMV_PROC2.toString());
                    }else {
                        gotoState(State.ONLINE2.toString());
                    }
                }else {
                    transEnd(result);
                }
                break;
            case ONLINE2:
                onOnline2(result);
                break;
            case EMV_PROC2:
                if (transData.getReason().equals(TransData.REASON_NO_RECV)){
                    transData.setPrintTimeout("y");
                    gotoState(State.PRINT_TICKET.toString());
                }else {
                    afterEmvProc2(result);
                }

                break;
            case PRINT_TICKET:
            default:
                transEnd(result);
                break;
        }
    }

    protected void onCheckCardCustom(ActionResult result) {
        ActionSearchCard.CardInformation cardInfo = (ActionSearchCard.CardInformation) result.getData();
        saveCardInfo(cardInfo, transData, true);
        transData.setTransType(ETransType.PBB_INQ.toString());

        byte mode = cardInfo.getSearchMode();
        if (mode != ActionSearchCard.SearchMode.TAP) {
            if(mode == ActionSearchCard.SearchMode.INSERT ){
                //gotoState( State.EMV_PROC.toString());
                transData.setPan(cardInfo.getPan());
                transData.setTrack2(cardInfo.getTrack2());
                gotoState( State.ENTER_PIN.toString());
            } else {
                gotoState( State.ENTER_PIN.toString());
            }
        } else{ // if (mode == SearchMode.TAP)
            // EMV处理
            gotoState( State.CLSS_PROC.toString());
        }
    }

    protected void onCheckCard(ActionResult result) {
        ActionSearchCard.CardInformation cardInfo = (ActionSearchCard.CardInformation) result.getData();
        saveCardInfo(cardInfo, transData, true);

        transData.setTransType(ETransType.PBB_INQ.toString());

        // 手输卡号处理
        byte mode = cardInfo.getSearchMode();
        if (mode != ActionSearchCard.SearchMode.TAP) {
            if(mode == ActionSearchCard.SearchMode.INSERT ){
                gotoState( State.EMV_PROC.toString());
            } else {
                gotoState( State.ENTER_PIN.toString());
            }
        } else{
            // EMV处理
            gotoState( State.CLSS_PROC.toString());
        }


    }

//    if(result.getRet() == TransResult.SUCC){
//
//    }else {
//        transEnd(result);
//    }

    protected void onEnterPin(ActionResult result) {

        if(result.getRet() == TransResult.SUCC){
            String pinBlock = (String) result.getData();
            transData.setPin(pinBlock);
            if (pinBlock != null && pinBlock.length() > 0) {
                transData.setHasPin(true);
            }
            // 联机处理
            gotoState( State.INPUT_DATA.toString());
        }else {
            transEnd(result);
        }


    }

    protected void inputData(ActionResult result) {
        if(result.getRet() == TransResult.SUCC){
            String[] data = (String[]) result.getData();
            String nop = data[0];
            String tahun = data[1];
            String pemda = data[2];
            transData.setField61(nop+tahun);
            transData.setField107(pemda);
//            gotoState(State.ONLINE.toString());
            gotoState(State.EMV_PROC.toString());
        }else {
            transEnd(result);
        }

    }

    protected void onOnline(ActionResult result) {
        if(result.getRet() == TransResult.SUCC){
            if (transData.getEnterMode() == TransData.EnterMode.QPBOC) {
                transData.setEmvResult((byte) ETransResult.ONLINE_APPROVED.ordinal());
            }

            String isIndopayMode = FinancialApplication.getSysParam().get(SysParam.INDOPAY_MODE);
            if(SysParam.Constant.YES.equals(isIndopayMode))
                transData.setAmount(transData.getAmount().substring(0,transData.getAmount().length()-2));

            gotoState(State.DISP_DETAIL.toString());
        }else {
            transEnd(result);
        }
    }

    protected void afterEmvProc(ActionResult result) {
        ETransResult transResult = (ETransResult) result.getData();
        Component.emvTransResultProcess(transResult, transData);
        if (transResult == ETransResult.ONLINE_APPROVED) {

            gotoState(State.DISP_DETAIL.toString());

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
            emvAbnormalResultProcess(transResult);
        }
    }

    protected void afterClssProcess(ActionResult result) {
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
            gotoState(State.DISP_DETAIL.toString());
        }
    }

    //temp
    private LinkedHashMap<String, String> prepareDispPBB1() {

        LinkedHashMap<String, String> hashMap = new LinkedHashMap<>();
        hashMap.put("Kode Bayar", "111111111");
        hashMap.put("Tahun Pajak", "222222222");
        hashMap.put("Nama", "33333333333");
        hashMap.put("Lokasi", "3444444444");
        hashMap.put("Kelurahan", "22222222222222");
        hashMap.put("Kecamatan", "1111111111111");
        hashMap.put("Provinsi", "22222222222222");
        hashMap.put("LT", "11111111111111");
        hashMap.put("LB", "11111111111111");
        hashMap.put("Jumlah Bayar", "11111111111");
        hashMap.put(context.getString(R.string.detail_biaya_admin), "1111111");
        hashMap.put("Denda", "111111111");
        hashMap.put("Diskon", "1111111");
        hashMap.put(context.getString(R.string.detail_total), "111111");

        return hashMap;
    }


        private LinkedHashMap<String, String> prepareDispPBB() {
        if (transData == null) {
            return null;
        }

        //contoh bit61 "3206060003002025302020DENNY IMAM AZHARI                  JL. PISANG MAS 4 NO. 691           DUREN JAYA                         BEKASI TIMUR                       JAWA BARAT                         00000000006000000000006020211230000000120000000000001000000000121000"
        String bit61 = Fox.Hex2Txt(transData.getField61());
        String norek, kodebayar, jumlahbayar, denda, total, admin="", diskon = "",
                tahunPajak,namaWP,
                lokasi, kelurahan, kecamatan,provinsi,LT,LB, jatuhTempo;;



        kodebayar = Fox.Substr(bit61, 1,18);
        jumlahbayar = Fox.Substr(bit61, 230,12);
        denda = Fox.Substr(bit61, 242,12);
        total = Fox.Substr(bit61, 254,12);
        diskon = Fox.Substr(bit61, 266,12); //fase 2
        //additional
        tahunPajak = Fox.Substr(bit61, 19, 4);
        namaWP = Fox.Substr(bit61, 23, 35).trim();
        lokasi = Fox.Substr(bit61, 58, 35).trim();
        kelurahan = Fox.Substr(bit61, 93, 35).trim();
        kecamatan = Fox.Substr(bit61, 128, 35).trim();
        provinsi = Fox.Substr(bit61, 163, 35).trim();
        LT = Fox.Substr(bit61, 198, 12);
        LB = Fox.Substr(bit61, 210, 12);
        jatuhTempo = Fox.Substr(bit61, 222, 12);

        admin = transData.getField28();
        if (TextUtils.isEmpty(admin)) admin = "0";
        if (TextUtils.isEmpty(diskon)) diskon = "0";

        String sLT = String.valueOf(Long.parseLong(LT));
        String sLB = String.valueOf(Long.parseLong(LB));



        Currency currency = FinancialApplication.getSysParam().getCurrency();

        String jmlBayarAmt = currency.getName() + " " + FinancialApplication.getConvert()
                .amountMinUnitToMajor(String.valueOf(Long.parseLong(jumlahbayar)), currency.getCurrencyExponent(), true);

        String biayaAdminAmt = currency.getName() + " " + FinancialApplication.getConvert()
                .amountMinUnitToMajor(String.valueOf(Long.parseLong(admin)),
                        currency.getCurrencyExponent(), true);

        String dendaAmt = currency.getName() + " " + FinancialApplication.getConvert()
                .amountMinUnitToMajor(String.valueOf(Long.parseLong(denda)),
                        currency.getCurrencyExponent(), true);

        String diskonAmt = currency.getName() + " " + FinancialApplication.getConvert()
                .amountMinUnitToMajor(String.valueOf(Long.parseLong(diskon)),
                        currency.getCurrencyExponent(), true);

        long totalAmount = Long.parseLong(total);

        String totalBayar = currency.getName() + " " + FinancialApplication.getConvert()
                .amountMinUnitToMajor(String.valueOf(totalAmount),
                        currency.getCurrencyExponent(), true);

        LinkedHashMap<String, String> hashMap = new LinkedHashMap<>();

        hashMap.put("Kode Bayar", kodebayar);
        hashMap.put("Tahun Pajak", tahunPajak);
        hashMap.put("Nama", namaWP);
        //Sandy : need to open this later
        /*
        hashMap.put("Lokasi", lokasi);
        hashMap.put("Kelurahan", kelurahan);
        hashMap.put("Kecamatan", kecamatan);
        hashMap.put("Provinsi", provinsi);
        hashMap.put("LT", sLT);
        hashMap.put("LB", sLB);
         */
        hashMap.put("Jumlah Bayar", jmlBayarAmt);
        hashMap.put(context.getString(R.string.detail_biaya_admin), biayaAdminAmt);
        hashMap.put("Denda", dendaAmt);
        hashMap.put("Diskon", diskonAmt);
        hashMap.put(context.getString(R.string.detail_total), totalBayar);

        return hashMap;
    }

    private void onOnline2(ActionResult result){
        if (result.getRet() == TransResult.SUCC && (transData.getResponseCode().equals("00")
                || transData.getResponseCode().equals("68") || transData.getResponseCode().equals("69") ) ){

            //gak dipake, spek fase 2
            /*if (transData.getResponseCode().equals("68") || transData.getResponseCode().equals("69")){
                transData.setPrintTimeout("y");
            }else {
                transData.setPrintTimeout("n");
            }*/

            transData.setAmount(transData.getAmount().substring(0,10));
            transData.setFeeTotalAmount(transData.getField28());
            transData.saveTrans();
            gotoState(State.PRINT_TICKET.toString());

        }else {
            transEnd(result);
        }
    }

    protected void afterEmvProc2(ActionResult result) {
        ETransResult transResult = (ETransResult) result.getData();
        Component.emvTransResultProcess(transResult, transData);
        if (transResult == ETransResult.ONLINE_APPROVED) {

            //gak dipake, spek fase 2
            /*if (transData.getResponseCode().equals("68") || transData.getResponseCode().equals("69")){
                transData.setPrintTimeout("y");
            }else {
                transData.setPrintTimeout("n");
            }*/

            transData.setAmount(transData.getAmount().substring(0,10));
            transData.setFeeTotalAmount(transData.getField28());
            transData.saveTrans();
            gotoState(State.PRINT_TICKET.toString());

        } /*else if (transResult == ETransResult.ARQC || transResult == ETransResult.SIMPLE_FLOW_END) { // 请求联机/简化流程

            if (transResult == ETransResult.ARQC && !Component.isQpbocNeedOnlinePin()) {
                gotoState(State.ONLINE.toString());
                return;
            }
            // 输密码
            gotoState(State.ENTER_PIN.toString());
        } else if (transResult == ETransResult.OFFLINE_APPROVED) {
            transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
        }*/ else {
            emvAbnormalResultProcess(transResult);
        }
    }

}
