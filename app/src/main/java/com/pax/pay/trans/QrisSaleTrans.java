package com.pax.pay.trans;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;

import com.pax.abl.core.ActionResult;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.trans.action.ActionInputTransData;
import com.pax.pay.trans.action.ActionPrintTransReceipt;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.settings.SysParam;
import com.pax.up.bjb.R;
import com.pax.pay.trans.action.ActionQrisSale;

public class QrisSaleTrans extends BaseTrans {

    public static final String TAG = "QrQrisTrans";

    private String amount;


    enum State {
        INPUT_AMOUNT,
        INPUT_QR,
        QRIS_SALE,
        PRINT_TICKET
    }

    public QrisSaleTrans(Context context, Handler handler, TransEndListener transListener) {
        super(context, handler, ETransType.DANA_QR_SALE, transListener);
    }


    @Override
    protected void bindStateOnAction() {

         // Input amount
        ActionInputTransData inputAmountAction = new ActionInputTransData(handler, ActionInputTransData.INFO_TYPE_SALE, null);
        inputAmountAction.setTitle("QRIS");
        inputAmountAction.setPrompt1(context.getString(R.string.prompt_input_amount));
        inputAmountAction.setInputType1(ActionInputTransData.EInputType.AMOUNT);
        inputAmountAction.setMaxLen1(9);
        inputAmountAction.setMinLen1(0);
        bind(State.INPUT_AMOUNT.toString(), inputAmountAction);



        ActionInputTransData inputQrisAction = new ActionInputTransData(handler, ActionInputTransData.INFO_TYPE_SALE, null);
        inputQrisAction.setTitle("QRIS");
        inputQrisAction.setPrompt1("QRIS SALE");
        inputQrisAction.setInputType1(ActionInputTransData.EInputType.ALPHNUM);
        inputQrisAction.setMaxLen1(30);
        inputQrisAction.setMinLen1(0);
        inputQrisAction.setSupportScan(true);
        bind(State.INPUT_QR.toString(), inputQrisAction);

        ActionQrisSale qrisSaleAction = new ActionQrisSale(transData);
        bind(State.QRIS_SALE.toString(), qrisSaleAction);


        //action
        ActionPrintTransReceipt printTransReceiptAction = new ActionPrintTransReceipt(transData, handler);
        bind(State.PRINT_TICKET.toString(), printTransReceiptAction);
        gotoState(State.INPUT_AMOUNT.toString());
    }


    @Override
    public void onActionResult(String currentState, ActionResult result) {
        State state = State.valueOf(currentState);

        switch (state) {
            case INPUT_AMOUNT:
                onEnterAmount(result);
                break;
            case INPUT_QR:
                onInputQR(result);
                break;
            case QRIS_SALE:
                toSignOrPrint();
                break;
            case PRINT_TICKET:
            default:
                transEnd(result);
                break;
        }
    }




    protected void onEnterAmount(ActionResult result) {

        String isIndopayMode = FinancialApplication.getSysParam().get(SysParam.INDOPAY_MODE);
        String amount = ((String) result.getData()).replace(",", "");

        //sandy
        if(SysParam.Constant.YES.equals(isIndopayMode))
            transData.setAmount(String.format("%s00",amount));
        else
            transData.setAmount(amount);

        gotoState(State.INPUT_QR.toString());
    }

    protected void onInputQR(ActionResult result) {
        //Sandy : Bypass Temporary
        String data = (String) result.getData();

        //Sandy : empty QR not allowed
        if (TextUtils.isEmpty(data)) {
            transEnd(new ActionResult(TransResult.ERR_INVALID_EMV_QR,null));
            return;
        }

        transData.setEnterMode(TransData.EnterMode.QR);
        transData.setField62(data);
        gotoState(State.QRIS_SALE.toString());

    }


    protected void toSignOrPrint() {
        //Sandy
        String isIndopayMode = FinancialApplication.getSysParam().get(SysParam.INDOPAY_MODE);
        if(SysParam.Constant.YES.equals(isIndopayMode))
            transData.setAmount(transData.getAmount().substring(0,transData.getAmount().length()-2));

        transData.setSignFree(true);
        //Sandy : user Coupon No for saving data
        transData.setCouponNo(transData.getField62());
        gotoState(State.PRINT_TICKET.toString());
        transData.updateTrans();

    }


}
