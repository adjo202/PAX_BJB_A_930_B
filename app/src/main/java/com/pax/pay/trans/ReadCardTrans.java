package com.pax.pay.trans;

import android.content.Context;
import android.os.Handler;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.eemv.enums.ETransResult;
import com.pax.pay.trans.action.ActionEmvProcess;
import com.pax.pay.trans.action.ActionSearchCard;
import com.pax.pay.trans.action.ActionSearchCard.CardInformation;
import com.pax.pay.trans.action.ActionSearchCard.ESearchCardUIType;
import com.pax.pay.trans.action.ActionSearchCard.SearchMode;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.ETransType;
import com.pax.up.bjb.R;

public class ReadCardTrans extends BaseTrans {
    private byte searchCardMode = SearchMode.KEYIN; // 寻卡方式

    public ReadCardTrans(Context context, Handler handler, TransEndListener transListener) {
        super(context, handler, ETransType.READCARDNO, transListener);
    }

    @Override
    protected void bindStateOnAction() {
        searchCardMode = Component.getCardReadMode(ETransType.READCARDNO);
        // 寻卡
        ActionSearchCard searchCardAction = new ActionSearchCard(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                ((ActionSearchCard) action).setParam(getCurrentContext(), context.getString(R
                                .string.trans_readcard),
                        searchCardMode, null, null, null, ESearchCardUIType.DEFAULT);
            }
        });
        bind(State.CHECK_CARD.toString(), searchCardAction);

        // EMV处理流程
        ActionEmvProcess emvProcessAction = new ActionEmvProcess(handler, transData);
        bind(State.EMV_PROC.toString(), emvProcessAction);

        gotoState(State.CHECK_CARD.toString());

    }

    enum State {
        CHECK_CARD,
        EMV_PROC,
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
        if (result.getRet() != TransResult.SUCC) {
            // 交易结束
            transEnd(result);
            return;
        }

        switch (state) {
            case CHECK_CARD: // 检测卡的后续处理
                afterCheckCard(result);
                break;
            case EMV_PROC: // emv后续处理
                afterEMVProcess(result);
                break;
            default:
                transEnd(result);
                break;
        }

    }

    private void afterCheckCard(ActionResult result) {
        CardInformation cardInfo = (CardInformation) result.getData();
        saveCardInfo(cardInfo, transData, false);
        byte mode = cardInfo.getSearchMode();
        if (mode == SearchMode.SWIPE) {
            transEnd(new ActionResult(TransResult.SUCC, cardInfo));
        } else if (mode == SearchMode.INSERT || mode == SearchMode.TAP) {
            // EMV处理
            gotoState(State.EMV_PROC.toString());
        }
    }

    private void afterEMVProcess(ActionResult result) {
        ETransResult transResult = (ETransResult) result.getData();
        Component.emvTransResultProcess(transResult, transData);

        CardInformation sResult = new CardInformation();
        sResult.setPan(transData.getPan());
        transEnd(new ActionResult(TransResult.SUCC, sResult));
    }

}
