package com.pax.pay.trans;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

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
import com.pax.pay.trans.action.ActionDispTransDetail;
import com.pax.pay.trans.action.ActionEmvProcess;
import com.pax.pay.trans.action.ActionEnterPin;
import com.pax.pay.trans.action.ActionPrintTransReceipt;
import com.pax.pay.trans.action.ActionSearchCard;
import com.pax.pay.trans.action.ActionTransOnline;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.utils.Fox;
import com.pax.pay.utils.ToastUtils;
import com.pax.settings.SysParam;
import com.pax.settings.currency.Currency;
import com.pax.up.bjb.R;

import java.util.LinkedHashMap;

public class ChangePinTrans extends BaseTrans {

    private static final String TAG = "ChangePinTrans";
    private byte searchCardMode = ActionSearchCard.SearchMode.KEYIN; // Find card method

    private boolean isFreePin = false;
    private boolean isSupportBypass = false;

    public ChangePinTrans(Context context, Handler handler,
                          TransEndListener transListener) {
        super(context, handler, ETransType.VERIFY_PIN, transListener);
    }

    private String newpin1, newpin2;


    @Override
    public void bindStateOnAction() {

        // Search Card action
        searchCardMode = Component.getCardReadMode(transType);
        ActionSearchCard searchCardAction = new ActionSearchCard(null);
        searchCardAction.setTitle(context.getString(R.string.trans_change_pin));
        searchCardAction.setMode(searchCardMode);
        searchCardAction.setUiType(searchCardMode == ActionSearchCard.SearchMode.TAP ? ActionSearchCard.ESearchCardUIType.QUICKPASS: ActionSearchCard.ESearchCardUIType.DEFAULT);
        bind( State.CHECK_CARD.toString(), searchCardAction);


        // 输入密码action
        ActionEnterPin enterPinAction = new ActionEnterPin(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {

                ((ActionEnterPin) action).setParam(getCurrentContext(), context.getString(R.string.trans_change_pin),
                        transData.getPan(), isSupportBypass, context.getString(R.string.prompt_bankcard_pwd),
                        context.getString(R.string.prompt_no_password), transData.getAmount(), ActionEnterPin.EEnterPinType.ONLINE_PIN, transData.getEnterMode());
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

        ActionEnterPin enterPinAction2 = new ActionEnterPin(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {


                ((ActionEnterPin) action).setParam(getCurrentContext(), context.getString(R.string.trans_change_pin),
                        transData.getPan(), isSupportBypass, "Please Input New PIN",
                        context.getString(R.string.prompt_no_password), transData.getAmount(), ActionEnterPin.EEnterPinType.ONLINE_PIN, transData.getEnterMode());
            }
        });
        bind( State.NEW_PIN1.toString(), enterPinAction2);

        ActionEnterPin enterPinAction3 = new ActionEnterPin(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {

                ((ActionEnterPin) action).setParam(getCurrentContext(), context.getString(R.string.trans_change_pin),
                        transData.getPan(), isSupportBypass, "Confirm PIN",
                        context.getString(R.string.prompt_no_password), transData.getAmount(), ActionEnterPin.EEnterPinType.ONLINE_PIN, transData.getEnterMode());
            }
        });
        bind( State.NEW_PIN2.toString(), enterPinAction3);

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
        EMV_PROC,
        CLSS_PREPROC,
        CLSS_PROC,
        NEW_PIN1,
        NEW_PIN2,
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
                action.setMode(ActionSearchCard.SearchMode.SWIPE);
                action.setUiType(ActionSearchCard.ESearchCardUIType.DEFAULT);
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
            case NEW_PIN1:
                onEnterNewPin(result);
                break;
            case NEW_PIN2:
                onEnterNewPin2(result);
                break;
            case ONLINE2:
                if (result.getRet()!=TransResult.SUCC){
                    transEnd(result);
                    return;
                }
                if (!transData.getResponseCode().equals("00")){
                    transEnd(result);
                    return;
                }
                transData.saveTrans();
                gotoState(State.PRINT_TICKET.toString());
                break;
            case PRINT_TICKET:
            default:
                transEnd(result);
                break;
        }
    }

    protected void onCheckCard(ActionResult result) {
        ActionSearchCard.CardInformation cardInfo = (ActionSearchCard.CardInformation) result.getData();
        saveCardInfo(cardInfo, transData, true);

        transData.setTransType(ETransType.VERIFY_PIN.toString());

        // 手输卡号处理
        byte mode = cardInfo.getSearchMode();
        if (mode != ActionSearchCard.SearchMode.TAP) {
            if(mode == ActionSearchCard.SearchMode.INSERT ){
                gotoState( State.EMV_PROC.toString());
            } else {
                transData.setTransType(ETransType.VERIFY_PIN.toString());
                gotoState( State.ENTER_PIN.toString());
            }
        } else{
            // EMV处理
            gotoState( State.CLSS_PROC.toString());
        }
    }

    protected void onEnterPin(ActionResult result) {
        if (result.getRet()!=TransResult.SUCC){
            transEnd(result);
            return;
        }

        String pinBlock = (String) result.getData();
        transData.setPin(pinBlock);
        if (pinBlock != null && pinBlock.length() > 0) {
            transData.setHasPin(true);
        }
        // 联机处理
        gotoState( State.ONLINE.toString());
    }

    protected void onOnline(ActionResult result) {
        if (result.getRet()!=TransResult.SUCC){
            transEnd(result);
            return;
        }

        if (transData.getEnterMode() == TransData.EnterMode.QPBOC) {
            transData.setEmvResult((byte) ETransResult.ONLINE_APPROVED.ordinal());
        }

        String isIndopayMode = FinancialApplication.getSysParam().get(SysParam.INDOPAY_MODE);
        if(SysParam.Constant.YES.equals(isIndopayMode))
            transData.setAmount(transData.getAmount().substring(0,transData.getAmount().length()-2));

        // 写交易记录
        gotoState(State.NEW_PIN1.toString());
    }

    protected void afterEmvProc(ActionResult result) {
        ETransResult transResult = (ETransResult) result.getData();
        Component.emvTransResultProcess(transResult, transData);
        if (transResult == ETransResult.ONLINE_APPROVED) {// 联机批准/脱机批准处理

            gotoState(State.NEW_PIN1.toString());

        } else if (transResult == ETransResult.ARQC || transResult == ETransResult.SIMPLE_FLOW_END) { // 请求联机/简化流程

            if (transResult == ETransResult.ARQC && !Component.isQpbocNeedOnlinePin()) {
                gotoState(State.ONLINE.toString());
                return;
            }
            // 输密码
            gotoState(State.ENTER_PIN.toString());
        } else if (transResult == ETransResult.OFFLINE_APPROVED) {
            transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
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

            gotoState(State.NEW_PIN1.toString());
        }
    }

    protected void onEnterNewPin(ActionResult result) {
        if (result.getRet()!=TransResult.SUCC){
            transEnd(result);
            return;
        }
        String pinBlock = (String) result.getData();

        newpin1 = pinBlock;

        gotoState( State.NEW_PIN2.toString());
    }

    protected void onEnterNewPin2(ActionResult result) {
        if (result.getRet()!=TransResult.SUCC){
            transEnd(result);
            return;
        }
        String pinBlock = (String) result.getData();

        newpin2 = pinBlock;

        if (!newpin1.equals(newpin2)){
            ToastUtils.showMessage("PIN TIDAK SAMA");
            Device.beepErr();
            gotoState(State.NEW_PIN2.toString());
            return;
        }else {
            transData.setTransNo(Long.parseLong(FinancialApplication.getSysParam().get(SysParam.TRANS_NO)));
            transData.setPin(pinBlock);
            transData.setTransType(ETransType.CHANGE_PIN.toString());
            gotoState(State.ONLINE2.toString());
        }
    }

}