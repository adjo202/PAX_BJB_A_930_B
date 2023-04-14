package com.pax.pay.trans.action;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.pax.abl.core.AAction;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.TransContext;
import com.pax.pay.trans.action.activity.InputDataPBBActivity;
import com.pax.pay.trans.action.activity.InputDataRedeemPointActivity;

/**
 * sandy@indopay.com
 */
public class ActionInputRedeemPoint extends AAction {
    private Context context;
    private String title;
    private Handler handler;

    public ActionInputRedeemPoint(ActionStartListener listener) {
        super(listener);
    }

    public void setParam(Context context, String title, Handler handler) {
        this.context = context;
        this.title = title;
        this.handler = handler;
    }

    @Override
    protected void process() {
        context = TransContext.getInstance().getCurrentContext();
        handler.post(new InputRedeemRunnable());
    }

    class InputRedeemRunnable implements Runnable {

        @Override
        public void run() {

            Bundle bundle = new Bundle();
            bundle.putString(EUIParamKeys.NAV_TITLE.toString(), title);

            Intent intent = new Intent(context, InputDataRedeemPointActivity.class);
            intent.putExtras(bundle);
            context.startActivity(intent);
        }
    }
}


