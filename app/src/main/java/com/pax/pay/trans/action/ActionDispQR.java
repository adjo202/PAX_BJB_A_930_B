package com.pax.pay.trans.action;

import android.content.Context;
import android.content.Intent;

import com.pax.abl.core.AAction;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.action.activity.DispQRActivity;

/**
 * sandy@indopay.com
 * 2019-06-28
 */

public class ActionDispQR extends AAction {

    private String amount;
    private String qr;
    private Context context;


    public ActionDispQR(ActionStartListener listener){
        super(listener);
    }


    public void setParam(Context context, String amount, String qr) {
        this.context = context;
        this.amount = amount;
        this.qr = qr;
    }


    @Override
    protected void process() {
        Intent intent = new Intent(context, DispQRActivity.class);
        intent.putExtra(EUIParamKeys.TRANS_AMOUNT.toString(), amount);
        intent.putExtra(EUIParamKeys.CONTENT.toString(), qr);
        context.startActivity(intent);
    }


}
