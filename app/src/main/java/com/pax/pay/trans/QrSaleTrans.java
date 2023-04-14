package com.pax.pay.trans;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.pax.abl.core.ActionResult;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.emv.EmvQr;
import com.pax.pay.trans.action.ActionCouponSale;
import com.pax.pay.trans.action.ActionPrintTransReceipt;
import com.pax.pay.trans.action.ActionScanCode;
import com.pax.pay.trans.action.ActionTransOnline;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.settings.SysParam;

public class QrSaleTrans extends BaseTrans {
    public static final String TAG = "QrSaleTrans";

    private String amount;

    public QrSaleTrans(Context context, Handler handler, String amount, TransEndListener transListener) {
        super(context, handler, ETransType.EMV_QR_SALE, transListener);
        this.amount = amount;
    }

    enum State {
        SCAN_CODE,
        QR_SALE,
        COUPON_SALE,
        PRINT_TICKET
    }

    @Override
    protected void bindStateOnAction() {

        String isIndopayMode = FinancialApplication.getSysParam().get(SysParam.INDOPAY_MODE);

        if (amount != null && amount.length() > 0) {
            //Sandy :
            //modifying 2 digits (00) decimal point
            if(SysParam.Constant.YES.equals(isIndopayMode))
                transData.setAmount(String.format("%s00",amount.replace(",", "")));
            else
                transData.setAmount(amount.replace(",", ""));
        }






        ActionScanCode scanCodeAction = new ActionScanCode(null);
        bind(State.SCAN_CODE.toString(), scanCodeAction);

        ActionTransOnline transOnlineAction = new ActionTransOnline(transData);
        bind(State.QR_SALE.toString(), transOnlineAction);

        ActionCouponSale couponSaleAction = new ActionCouponSale(transData);
        bind(State.COUPON_SALE.toString(), couponSaleAction);

        ActionPrintTransReceipt printTransReceiptAction = new ActionPrintTransReceipt(transData, handler);
        bind(State.PRINT_TICKET.toString(), printTransReceiptAction);

        gotoState(State.SCAN_CODE.toString());
    }

    @Override
    public void onActionResult(String currentState, ActionResult result) {
        State state = State.valueOf(currentState);
        // action结果检查，如果失败，结束交易
        int ret = result.getRet();
        if (ret != TransResult.SUCC) {
            transEnd(result);
            return;
        }

        switch (state) {
            case SCAN_CODE:
                afterScanCode(result);
                break;
            case QR_SALE:
                transData.saveTrans();
                gotoState(State.PRINT_TICKET.toString());
                break;
            case COUPON_SALE:
                gotoState(State.PRINT_TICKET.toString());
                break;
            case PRINT_TICKET:
                transEnd(result);
                break;
            default:
                transEnd(result);
                break;
        }
    }

    private void afterScanCode(ActionResult result) {
        // 扫码
        String qrCode = (String) result.getData();
        if (qrCode == null || qrCode.length() == 0) {
            transEnd(new ActionResult(TransResult.ERR_INVALID_EMV_QR, null));
            return;
        }
        Log.d(TAG,"qrCode :" + qrCode);


        EmvQr emvQr = EmvQr.decodeEmvQrB64(qrCode);
        if (emvQr == null) {
            transEnd(new ActionResult(TransResult.ERR_INVALID_EMV_QR, null));
            return;
        }
        if (!emvQr.isUpiAid()) {
            transEnd(new ActionResult(TransResult.ERR_NOT_SUPPORT_TRANS, null));
            return;
        }

        saveQrInfo(emvQr);

        if (emvQr.isSupportUplan()) {
            gotoState(State.COUPON_SALE.toString());
        } else {
            gotoState(State.QR_SALE.toString());
        }

    }


    /*
    private void saveQrInfo(String qr) {
        //transData.setPan(qr);
        //EmvQr emvQr;
        transData.setSendIccData(qr);
        transData.setEnterMode(TransData.EnterMode.QR);
        transData.setSignFree(true);
    }*/


    private void saveQrInfo(                           EmvQr emvQr) {
        transData.setCardSerialNo(emvQr.getCardSeqNum());
        transData.setCouponNo(emvQr.getCouponNum());
        transData.setSendIccData(emvQr.getIccData());
        transData.setPan(emvQr.getPan());
        transData.setExpDate(emvQr.getExpireDate());
        transData.setTrack2(emvQr.getTrackData());
        transData.setEnterMode(TransData.EnterMode.QR);
        transData.setSignFree(true);
    }

}
