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
import com.pax.pay.trans.action.ActionDispSingleLineMsg;
import com.pax.pay.trans.action.ActionEmvProcess;
import com.pax.pay.trans.action.ActionEnterPin;
import com.pax.pay.trans.action.ActionEnterPin.EEnterPinType;
import com.pax.pay.trans.action.ActionSearchCard;
import com.pax.pay.trans.action.ActionSearchCard.CardInformation;
import com.pax.pay.trans.action.ActionSearchCard.ESearchCardUIType;
import com.pax.pay.trans.action.ActionSearchCard.SearchMode;
import com.pax.pay.trans.action.ActionTransOnline;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.ETransType;
import com.pax.settings.currency.Currency;
import com.pax.up.bjb.R;


public class BalanceTrans extends BaseTrans {
    private byte searchCardMode = SearchMode.KEYIN; // 寻卡方式

    public BalanceTrans(Context context, Handler handler, TransEndListener transListener) {
        super(context, handler, ETransType.QUERY, transListener);
    }

    @Override
    protected void bindStateOnAction() {
        searchCardMode = Component.getCardReadMode(ETransType.QUERY);
        // 读卡
        ActionSearchCard searchCardAction = new ActionSearchCard(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                ((ActionSearchCard) action).setParam(getCurrentContext(), context.getString(R.string.trans_balance),
                        searchCardMode, null, null, null, ESearchCardUIType.DEFAULT);
            }
        });
        bind(State.CHECK_CARD.toString(), searchCardAction);

        // 输入密码action
        ActionEnterPin enterPinAction = new ActionEnterPin(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionEnterPin) action).setParam(getCurrentContext(), context.getString(R.string.trans_balance),
                        transData.getPan(), true, context.getString(R.string.prompt_bankcard_pwd),
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

        // 余额显示
        ActionDispSingleLineMsg balanceDispAction = new ActionDispSingleLineMsg(new AAction.ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                Currency currency = FinancialApplication.getSysParam().getCurrency();
                String content = FinancialApplication.getConvert()
                        .amountMinUnitToMajor(transData.getBalance(),
                                currency.getCurrencyExponent(), true);
                if (content == null || content.length() == 0) {
                    content = "0";
                }
                String amount = context.getString(R.string.trans_amount_default, currency.getName(),
                        content);

                ((ActionDispSingleLineMsg) action).setParam(getCurrentContext(), handler,
                        context.getString(R.string.trans_balance), context.getString(R.string.balance_prompt), amount, 5);
            }
        });
        bind(State.BALANCE_DISP.toString(), balanceDispAction);

        gotoState(State.CLSS_PREPROC.toString());
    }

    enum State {
        CHECK_CARD,
        ENTER_PIN,
        EMV_PROC,
        CLSS_PREPROC,
        CLSS_PROC,
        ONLINE,
        BALANCE_DISP
    }

    @Override
    public void onActionResult(String currentState, ActionResult result) {
        State state = State.valueOf(currentState);
        //fall back treatment
        if (state == State.EMV_PROC && transData.getIsFallback()) {
            searchCardMode = SearchMode.SWIPE;
            gotoState(State.CHECK_CARD.toString());
            return;
        }
        if (state != State.BALANCE_DISP) {
            int ret = result.getRet();
            if (ret != TransResult.SUCC) {
                transEnd(result);
                return;
            }
        }

        switch (state) {
            case CHECK_CARD: // 检测卡的后续处理
                afterCheckCard(result);
                break;
            case ENTER_PIN: // 输入密码的后续处理
                afterEnterPin(result);
                break;
            case ONLINE: // 联机的后续处理
                gotoState(State.BALANCE_DISP.toString());
                break;
            case EMV_PROC: // emv后续处理
                afterEmvProc(result);
                break;
            case CLSS_PREPROC:
                gotoState(State.CHECK_CARD.toString());
                break;
            case CLSS_PROC:
                CTransResult clssResult = (CTransResult) result.getData();
                afterClssProcess(clssResult);
                break;
            case BALANCE_DISP:
                transEnd(new ActionResult(TransResult.SUCC, null));
                break;
            default:
                transEnd(result);
                break;
        }
    }

    protected void afterCheckCard(ActionResult result) {
        CardInformation cardInfo = (CardInformation) result.getData();
        saveCardInfo(cardInfo, transData, true);
        // 手输卡号处理
        byte mode = cardInfo.getSearchMode();
        if (mode == SearchMode.KEYIN) {
            // 跑到这里， 请检查程序逻辑， 有问题
        } else if (mode == SearchMode.SWIPE) {
            // 输密码
            gotoState(State.ENTER_PIN.toString());
        } else if (mode == SearchMode.INSERT) {
            // EMV处理
            gotoState(State.EMV_PROC.toString());
        } else if (mode == SearchMode.TAP) {
            // 处理
            gotoState(State.CLSS_PROC.toString());
        }
    }

    protected void afterEnterPin(ActionResult result) {
        String pinBlock = (String) result.getData();
        transData.setPin(pinBlock);
        if (pinBlock != null && pinBlock.length() > 0) {
            transData.setHasPin(true);
        }
        // 联机处理
        gotoState(State.ONLINE.toString());
    }

    protected void afterEmvProc(ActionResult result) {
        ETransResult transResult = (ETransResult) result.getData();
        Component.emvTransResultProcess(transResult, transData);
        if (transResult == ETransResult.ONLINE_APPROVED) {// 联机批准/脱机批准处理
            gotoState(State.BALANCE_DISP.toString());
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

    private void afterClssProcess(CTransResult transResult) {

        // 设置交易结果
        transData.setEmvResult((byte) transResult.getTransResult().ordinal());
        if (transResult.getTransResult() == ETransResult.ABORT_TERMINATED ||
                transResult.getTransResult() == ETransResult.CLSS_OC_DECLINED ||
                transResult.getTransResult() == ETransResult.ONLINE_DENIED) { // emv interrupt
            Device.beepErr();
            transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
            return;
        }

        ClssTransProcess.clssTransResultProcess(transResult, FinancialApplication.getClss(),
                transData);

        if (transResult.getTransResult() == ETransResult.CLSS_OC_APPROVED || transResult.getTransResult() == ETransResult.ONLINE_APPROVED) {
            transData.setIsOnlineTrans(transResult.getTransResult() == ETransResult.ONLINE_APPROVED);
            gotoState(State.BALANCE_DISP.toString());
        }
        // ETransResult.CLSS_OC_ONLINE_REQUEST, online is handled in the module
    }

}
