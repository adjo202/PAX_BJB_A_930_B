package com.pax.pay.trans.action;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.pax.abl.core.AAction;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.TransContext;
import com.pax.pay.trans.action.activity.InputBPJSTkPaymentActivity;

public class ActionInputBPJSTkPayment extends AAction {

    public static final int INFO_TYPE_SALE = 1;
    public static final int INFO_TYPE_AUTH = 2;
    private static final String TAG = "ActionInputTransData";
    private Context context;
    private Handler handler;
    private String title;
    private boolean isDisplayCombobox;
    private int trans;


    public enum EInputType {
        AMOUNT,
        DATE,
        NUM,
        ALPHNUM,
        TEXT,
    }

    public ActionInputBPJSTkPayment(Handler handler, ActionStartListener listener, int trans) {
        super(listener);
        this.handler = handler;
        this.trans = trans;
    }


    public ActionInputBPJSTkPayment setParam(Context context, Handler handler, String title) {
        this.context = context;
        this.handler = handler;
        this.title = title;
        return this;
    }

    public ActionInputBPJSTkPayment setIntypeAuth(String prompt, EInputType inputType, int maxLen) {
        return setIntypeAuth(prompt, inputType, maxLen, 0);
    }

    public ActionInputBPJSTkPayment setIntypeAuth(String prompt, EInputType inputType, int maxLen, int minLen) {
        return this;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void displayCombobox(){
        this.isDisplayCombobox = true;
    }

    public void hideCombobox(){
        this.isDisplayCombobox = false;
    }

    @Override
    protected void process() {
        context = TransContext.getInstance().getCurrentContext();
        handler.post(new InputTransDataRunnable());
    }

    class InputTransDataRunnable implements Runnable {
        @Override
        public void run() {

            Intent intent = new Intent(context, InputBPJSTkPaymentActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString(EUIParamKeys.NAV_TITLE.toString(), title);
            bundle.putBoolean(EUIParamKeys.INPUT_TYPE_1.toString(),isDisplayCombobox);
            bundle.putInt("trans", trans);
            intent.putExtras(bundle);
            context.startActivity(intent);

        }
    }
}
