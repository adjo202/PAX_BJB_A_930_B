package com.pax.pay.trans.action;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.pax.abl.core.AAction;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.TransContext;
import com.pax.pay.trans.action.activity.InputDataCetakUlangActivity;
import com.pax.pay.trans.action.activity.InputESamsatDataActivity;

public class ActionInputCetakUlangData extends AAction {

    private Context context;
    private Handler handler;
    private String title;
    private String prompt1;
    private EInputType inputType1;
    private int maxLen1;
    private int minLen1;
    private boolean isVoidLastTrans;
    private boolean isAuthZero;
    private boolean isSupportScan;
    private static final String TAG = "ActionInputTransData";

    /**
     * 输入数据类型定义
     *
     * @author Steven.W
     *
     */
    public enum EInputType {
        AMOUNT,
        DATE,
        NUM, // 数字
        ALPHNUM, // 数字加字母
        TEXT, // 所有类型
    }

    public ActionInputCetakUlangData(ActionStartListener listener, Context context) {
        super(listener);
        this.context = context;
    }

    public ActionInputCetakUlangData(Handler handler, ActionStartListener listener) {
        super(listener);
        this.handler = handler;
    }

    public ActionInputCetakUlangData setParam(Context context, Handler handler, String title) {
        this.context = context;
        this.handler = handler;
        this.title = title;
        return this;
    }

    public ActionInputCetakUlangData setInfoTypeSale(String prompt, EInputType inputType, int maxLen, boolean isVoidLastTrans) {
        return setInfoTypeSale(prompt, inputType, maxLen, 0, isVoidLastTrans);
    }

    public ActionInputCetakUlangData setInfoTypeSale(String prompt, EInputType inputType, int maxLen, int minLen,
                                                     boolean isVoidLastTrans) {
        this.prompt1 = prompt;
        this.inputType1 = inputType;
        this.maxLen1 = maxLen;
        this.minLen1 = minLen;
        this.isVoidLastTrans = isVoidLastTrans;
        return this;
    }

    public ActionInputCetakUlangData setInfoTypeSale(String prompt, EInputType inputType, int maxLen, int minLen,
                                                     boolean isVoidLastTrans, boolean isAuthZero) {
        this.prompt1 = prompt;
        this.inputType1 = inputType;
        this.maxLen1 = maxLen;
        this.minLen1 = minLen;
        this.isVoidLastTrans = isVoidLastTrans;
        this.isAuthZero = isAuthZero;
        return this;
    }

    public ActionInputCetakUlangData setInfoTypeSale(String prompt, EInputType inputType, int maxLen, int minLen,
                                                     boolean isVoidLastTrans, boolean isAuthZero, boolean isSupportScan) {
        this.prompt1 = prompt;
        this.inputType1 = inputType;
        this.maxLen1 = maxLen;
        this.minLen1 = minLen;
        this.isVoidLastTrans = isVoidLastTrans;
        this.isAuthZero = isAuthZero;
        this.isSupportScan = isSupportScan;
        return this;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPrompt1(String prompt1) {
        this.prompt1 = prompt1;
    }

    public void setInputType1(EInputType inputType1) {
        this.inputType1 = inputType1;
    }

    public void setMaxLen1(int maxLen1) {
        this.maxLen1 = maxLen1;
    }

    public void setMinLen1(int minLen1) {
        this.minLen1 = minLen1;
    }



    public void setVoidLastTrans(boolean voidLastTrans) {
        isVoidLastTrans = voidLastTrans;
    }

    public void setAuthZero(boolean authZero) {
        isAuthZero = authZero;
    }

    public void setSupportScan(boolean supportScan) {
        isSupportScan = supportScan;
    }

    @Override
    protected void process() {
        context = TransContext.getInstance().getCurrentContext();
        handler.post(new InputTransDataRunnable());
    }

    class InputTransDataRunnable implements Runnable {
        @Override
        public void run() {

                Intent intent = new Intent(context, InputDataCetakUlangActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(EUIParamKeys.NAV_TITLE.toString(), title);

                /*bundle.putString(EUIParamKeys.PROMPT_1.toString(), prompt1);
                bundle.putInt(EUIParamKeys.INPUT_MAX_LEN_1.toString(), maxLen1);
                bundle.putInt(EUIParamKeys.INPUT_MIN_LEN_1.toString(), minLen1);
                bundle.putSerializable(EUIParamKeys.INPUT_TYPE_1.toString(), inputType1);
                bundle.putBoolean(EUIParamKeys.VOID_LAST_TRANS_UI.toString(), isVoidLastTrans);
                bundle.putBoolean(EUIParamKeys.INPUT_AUTH_ZERO.toString(), isAuthZero);
                bundle.putBoolean(EUIParamKeys.SUPPORT_SCAN.toString(), isSupportScan);*/
                intent.putExtras(bundle);
                context.startActivity(intent);

        }
    }
}
