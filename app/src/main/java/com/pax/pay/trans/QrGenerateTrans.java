package com.pax.pay.trans;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.gl.convert.IConvert;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.trans.action.ActionDispQR;
import com.pax.pay.trans.action.ActionInputTransData;
import com.pax.pay.trans.action.ActionPrintTransReceipt;
import com.pax.pay.trans.action.ActionQRGenerate;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.settings.SysParam;
import com.pax.up.bjb.R;

public class QrGenerateTrans extends BaseTrans {

    public static final String TAG = "QrGenerateQrisTrans";

    private String amount;


    enum State {
        INPUT_AMOUNT,
        REQUEST_QR,
        DISPLAY_QR,
        INQUIRY_TRX,
        PRINT_TICKET
    }


    public QrGenerateTrans(Context context, Handler handler, TransEndListener transListener) {
        super(context, handler, ETransType.QR_GENERATE, transListener);
    }

    @Override
    protected void bindStateOnAction() {

        String isIndopayMode = FinancialApplication.getSysParam().get(SysParam.INDOPAY_MODE);

        // Input amount
        ActionInputTransData inputAmountAction = new ActionInputTransData(handler, ActionInputTransData.INFO_TYPE_SALE, null);
        inputAmountAction.setTitle("QRIS");
        inputAmountAction.setPrompt1(context.getString(R.string.prompt_input_amount));
        inputAmountAction.setInputType1(ActionInputTransData.EInputType.AMOUNT);
        inputAmountAction.setMaxLen1(9);
        inputAmountAction.setMinLen1(0);
        bind(State.INPUT_AMOUNT.toString(), inputAmountAction);

        //request QR
        ActionQRGenerate generateAction = new ActionQRGenerate(transData);
        bind(State.REQUEST_QR.toString(), generateAction );

        //display QR
        ActionDispQR displayQR = new ActionDispQR(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                //Sandy : since we modifying the 00 decimal point
                //we cut 00 at rear value
                String amt = transData.getAmount().substring(0,transData.getAmount().length()-2);
                String qr = new String(FinancialApplication.getConvert().strToBcd(transData.getRecvIccData(), IConvert.EPaddingPosition.PADDING_LEFT));

                ((ActionDispQR) action).setParam(getCurrentContext(), amt, qr);
            }
        });
        bind(State.DISPLAY_QR.toString(), displayQR);


        //inquiry transaction
        ActionQRGenerate inquiryAction = new ActionQRGenerate(transData,ActionQRGenerate.INQUIRY);
        bind(State.INQUIRY_TRX.toString(), inquiryAction);




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
            case REQUEST_QR:
                onGenerateQR(result);
                break;
            case DISPLAY_QR:
                onDisplayQR(result);
                break;
            case INQUIRY_TRX :
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

        gotoState(State.REQUEST_QR.toString());

    }



    protected void onDisplayQR(ActionResult result) {

        transData.setEnterMode(TransData.EnterMode.QR);
        gotoState(State.INQUIRY_TRX.toString());

    }





    protected void onGenerateQR(ActionResult result) {
        //Sandy : Bypass Temporary

        transData.setEnterMode(TransData.EnterMode.QR);
        gotoState(State.DISPLAY_QR.toString());

    }



    protected void toSignOrPrint() {
        //Sandy
        String isIndopayMode = FinancialApplication.getSysParam().get(SysParam.INDOPAY_MODE);
        if(SysParam.Constant.YES.equals(isIndopayMode))
            transData.setAmount(transData.getAmount().substring(0,transData.getAmount().length()-2));

        transData.setSignFree(true);
        //Sandy : user Coupon No for saving data
        transData.setPan("1234567890123456");
        transData.setCouponNo(transData.getField62());

        //Sandy : if approved, then print a receipt
        if(transData.getResponseCode().equals("00")){
            gotoState(State.PRINT_TICKET.toString());
            transData.saveTrans();
        }else {
            gotoState(State.INQUIRY_TRX.toString());

        }
    }



}
