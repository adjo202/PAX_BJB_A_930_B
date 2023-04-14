package com.pax.pay.trans;

import android.content.Context;
import android.os.Handler;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.device.Device;
import com.pax.eemv.entity.CTransResult;
import com.pax.eemv.enums.ECvmResult;
import com.pax.eemv.enums.ETransResult;
import com.pax.gl.convert.IConvert;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.emv.EmvTags;
import com.pax.pay.emv.clss.ClssTransProcess;
import com.pax.pay.trans.action.ActionClssPreProc;
import com.pax.pay.trans.action.ActionClssProcess;
import com.pax.pay.trans.action.ActionDispTransDetail;
import com.pax.pay.trans.action.ActionEmvProcess;
import com.pax.pay.trans.action.ActionEnterPin;
import com.pax.pay.trans.action.ActionInputRedeemPoint;
import com.pax.pay.trans.action.ActionPrintTransReceipt;
import com.pax.pay.trans.action.ActionSearchCard;
import com.pax.pay.trans.action.ActionSearchCardCustom;
import com.pax.pay.trans.action.ActionTransOnline;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.RedeemData;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.utils.Fox;
import com.pax.settings.SysParam;
import com.pax.up.bjb.R;

import org.apache.commons.lang.StringUtils;

import java.util.LinkedHashMap;

public class RedeemPoinTrans extends BaseTrans {

    private static final String TAG = "RedeemPoinTrans";
    private byte searchCardMode = ActionSearchCard.SearchMode.KEYIN; // Find card method

    private boolean isFreePin = false;
    private boolean isSupportBypass = true;

    public RedeemPoinTrans(Context context, Handler handler,
                           TransEndListener transListener) {
        super(context, handler, ETransType.REDEEM_POIN_DATA_INQ, transListener);
    }


    @Override
    public void bindStateOnAction() {
        String isIndopayMode = FinancialApplication.getSysParam().get(SysParam.INDOPAY_MODE);

        // Search Card action
        searchCardMode = Component.getCardReadMode(transType);
        ActionSearchCardCustom searchCardAction = new ActionSearchCardCustom(null);
        searchCardAction.setTitle("Tukar Voucher");
        searchCardAction.setMode(searchCardMode);
        String amount=null;
        if (amount != null && amount.length() > 0) {
            if (SysParam.Constant.YES.equals(isIndopayMode))
                searchCardAction.setAmount(transData.getAmount().substring(0, transData.getAmount().length() - 2));
            else
                searchCardAction.setAmount(transData.getAmount());
        }
        //ActionSearchCard.ESearchCardUIType
        searchCardAction.setUiType(searchCardMode == ActionSearchCardCustom.SearchCustomMode.TAP ? ActionSearchCardCustom.ESearchCardUIType.QUICKPASS: ActionSearchCardCustom.ESearchCardUIType.DEFAULT);
        bind( State.CHECK_CARD.toString(), searchCardAction);


        ActionInputRedeemPoint actionInputRedeemPoint = new ActionInputRedeemPoint(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                ((ActionInputRedeemPoint) action).setParam(getCurrentContext(), "Tukar Poin",handler);
            }
        });
        bind(State.INPUT_DATA.toString(), actionInputRedeemPoint);

        // emv处理action
        ActionEmvProcess emvProcessAction = new ActionEmvProcess(handler, transData);
        bind( State.EMV_PROC.toString(), emvProcessAction);


        //clss preprocess action
        ActionClssPreProc clssPreProcAction = new ActionClssPreProc(transData, null);
        bind( State.CLSS_PREPROC.toString(), clssPreProcAction);

        // 联机action
        ActionTransOnline transOnlineAction = new ActionTransOnline(transData);
        bind( State.ONLINE.toString(), transOnlineAction);

        ActionDispTransDetail dispTransDetail = new ActionDispTransDetail(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                LinkedHashMap<String, String> map = prepareDispRedeem();
                ((ActionDispTransDetail) action).setParam(getCurrentContext(), handler,
                        context.getString(R.string.trans_detail), map);
                TransContext.getInstance().setCurrentAction(action);
            }
        });
        bind(State.DISP_DETAIL.toString(), dispTransDetail);

        // 联机action
        ActionTransOnline transOnlineAction2 = new ActionTransOnline(transData);
        bind( State.ONLINE2.toString(), transOnlineAction2);


        // print
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
                    transData.setTransType(ETransType.REDEEM_POIN_DATA_PAY.toString());
                    setTransType(ETransType.REDEEM_POIN_DATA_PAY);
                    gotoState(State.ONLINE2.toString());

                }else {
                    transEnd(result);
                }
                break;
            case ONLINE2:
                onOnline2(result);
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
        transData.setTransType(ETransType.REDEEM_POIN_DATA_INQ.toString());

        byte mode = cardInfo.getSearchMode();
        if (mode != ActionSearchCard.SearchMode.TAP) {
            if(mode == ActionSearchCard.SearchMode.INSERT ){
                //gotoState( State.EMV_PROC.toString());
                transData.setPan(cardInfo.getPan());
                transData.setTrack2(cardInfo.getTrack2());
                gotoState( State.INPUT_DATA.toString());

            } else {
                gotoState( State.ENTER_PIN.toString());
            }
        } else{

            gotoState( State.CLSS_PROC.toString());
        }
    }

    /*
    protected void onCheckCard(ActionResult result) {
        ActionSearchCard.CardInformation cardInfo = (ActionSearchCard.CardInformation) result.getData();
        saveCardInfo(cardInfo, transData, true);

        transData.setTransType(ETransType.REDEEM_POIN_DATA_INQ.toString());

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

     */

//    if(result.getRet() == TransResult.SUCC){
//
//    }else {
//        transEnd(result);
//    }

    /*
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

     */




    protected void inputData(ActionResult result) {
        if(result.getRet() == TransResult.SUCC){
            RedeemData data = (RedeemData) result.getData();
            String idBiller = data.getIdBiller();
            String idVoucher = data.getVoucherNumber();
            transData.setTransType(ETransType.REDEEM_POIN_DATA_INQ.toString());
            transData.setField48(genBit48(idBiller,idVoucher) );
            //gotoState(State.EMV_PROC.toString());
            gotoState(State.ONLINE.toString());

        }else {
            transEnd(result);
        }

    }

    private String genBit48(String s1, String s2) {
        if (StringUtils.isEmpty(s1)) {
            s1 = "";
        }

        if (StringUtils.isEmpty(s2)) {
            s2 = "";
        }

        String dt48 = Component.getPaddedString(s1, 20, ' ', IConvert.EPaddingPosition.PADDING_LEFT)
                + Component.getPaddedString(s2, 20, ' ', IConvert.EPaddingPosition.PADDING_LEFT);

        return dt48;
    }




    /*
    protected void inputData(ActionResult result) {
        if(result.getRet() == TransResult.SUCC){
            String[] data = (String[]) result.getData();
            String nop = data[0];
            String tahun = data[1];
            String pemda = data[2];
            transData.setField61(nop+tahun);
            transData.setField107(pemda);
            gotoState(State.EMV_PROC.toString());
        }else {
            transEnd(result);
        }

    }
*/
    protected void onOnline(ActionResult result) {
        if(result.getRet() == TransResult.SUCC){
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

        private LinkedHashMap<String, String> prepareDispRedeem() {
        if (transData == null) {
            return null;
        }

        String bit63 = Fox.Hex2Txt(transData.getField63());
        String[] informations = bit63.split("\\|");

        LinkedHashMap<String, String> hashMap = new LinkedHashMap<>();
        for(int i = 0; i < informations.length;i++){
            String[] information = informations[i].toString().split(":");
            hashMap.put(information[0], information[1]);
        }

        return hashMap;
    }

    private void onOnline2(ActionResult result){
        if (result.getRet() == TransResult.SUCC && (transData.getResponseCode().equals("00") ) ){
            transData.saveTrans();
            gotoState(State.PRINT_TICKET.toString());

        }else {
            transEnd(result);
        }
    }

    /*
    protected void afterEmvProc2(ActionResult result) {
        ETransResult transResult = (ETransResult) result.getData();
        Component.emvTransResultProcess(transResult, transData);
        if (transResult == ETransResult.ONLINE_APPROVED) {

            transData.setAmount(transData.getAmount().substring(0,10));
            transData.setFeeTotalAmount(transData.getField28());
            transData.saveTrans();
            gotoState(State.PRINT_TICKET.toString());

        }  else {
            emvAbnormalResultProcess(transResult);
        }
    }

     */

}
