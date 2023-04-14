package com.pax.pay.trans.action;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.pax.abl.core.AAction;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.TransContext;
import com.pax.pay.trans.action.activity.InputDataPBBActivity;

public class ActionInputPBB extends AAction {
    private Context context;
    private String title;
    private Handler handler;

    public ActionInputPBB(ActionStartListener listener) {
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
        handler.post(new InputPBBRunnable());
    }

    class InputPBBRunnable implements Runnable {

        @Override
        public void run() {

            Bundle bundle = new Bundle();
            bundle.putString(EUIParamKeys.NAV_TITLE.toString(), title);

            Intent intent = new Intent(context, InputDataPBBActivity.class);
            intent.putExtras(bundle);
            context.startActivity(intent);
        }
    }
}


