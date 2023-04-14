package com.pax.pay.trans.action;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.pax.abl.core.AAction;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.action.activity.InputEcRefundDataActivity;

public class ActionInputEcRefundData extends AAction {

    public ActionInputEcRefundData(ActionStartListener listener) {
        super(listener);

    }

    private Context context;
    private Handler handler;
    private String title;

    public void setParam(Context context, Handler handler, String title) {
        this.context = context;
        this.handler = handler;
        this.title = title;
    }

    @Override
    protected void process() {

        handler.post(new Runnable() {

            @Override
            public void run() {
                Intent intent = new Intent(context, InputEcRefundDataActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(EUIParamKeys.NAV_TITLE.toString(), title);
                bundle.putBoolean(EUIParamKeys.NAV_BACK.toString(), true);
                intent.putExtras(bundle);
                context.startActivity(intent);
            }
        });

    }

}
