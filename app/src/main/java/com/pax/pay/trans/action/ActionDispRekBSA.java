package com.pax.pay.trans.action;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.pax.abl.core.AAction;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.TransContext;
import com.pax.pay.trans.action.activity.DispRekBSAActivity;

public class ActionDispRekBSA extends AAction {

    public static final int INFO_TYPE_SALE = 1;
    public static final int INFO_TYPE_AUTH = 2;

    private Context context;
    private Handler handler;
    private String title;
    private String prompt1;
    private EInputType inputType1;
    private int maxLen1;
    private int minLen1;
    private String prompt2;
    private EInputType inputType2;
    private int maxLen2;
    private int minLen2;
    private int infoType;
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


    public ActionDispRekBSA(ActionStartListener listener) {
        super(listener);
    }



    public ActionDispRekBSA(Handler handler, int infoType, ActionStartListener listener) {
        super(listener);
        this.handler = handler;
        this.infoType = infoType;
    }


    public ActionDispRekBSA setParam(Context context, Handler handler, String title) {
        this.context = context;
        this.handler = handler;
        this.title = title;
        return this;
    }

    public ActionDispRekBSA setInfoTypeSale(String prompt, EInputType inputType, int maxLen, boolean isVoidLastTrans) {
        return setInfoTypeSale(prompt, inputType, maxLen, 0, isVoidLastTrans);
    }

    public ActionDispRekBSA setInfoTypeSale(String prompt, EInputType inputType, int maxLen, int minLen,
                                            boolean isVoidLastTrans) {
        this.prompt1 = prompt;
        this.inputType1 = inputType;
        this.maxLen1 = maxLen;
        this.minLen1 = minLen;
        this.isVoidLastTrans = isVoidLastTrans;
        return this;
    }

    public ActionDispRekBSA setInfoTypeSale(String prompt, EInputType inputType, int maxLen, int minLen,
                                            boolean isVoidLastTrans, boolean isAuthZero) {
        this.prompt1 = prompt;
        this.inputType1 = inputType;
        this.maxLen1 = maxLen;
        this.minLen1 = minLen;
        this.isVoidLastTrans = isVoidLastTrans;
        this.isAuthZero = isAuthZero;
        return this;
    }

    public ActionDispRekBSA setInfoTypeSale(String prompt, EInputType inputType, int maxLen, int minLen,
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

    public ActionDispRekBSA setIntypeAuth(String prompt, EInputType inputType, int maxLen) {
        return setIntypeAuth(prompt, inputType, maxLen, 0);
    }

    public ActionDispRekBSA setIntypeAuth(String prompt, EInputType inputType, int maxLen, int minLen) {
        this.prompt2 = prompt;
        this.inputType2 = inputType;
        this.maxLen2 = maxLen;
        this.minLen2 = minLen;
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

    public void setPrompt2(String prompt2) {
        this.prompt2 = prompt2;
    }

    public void setInputType2(EInputType inputType2) {
        this.inputType2 = inputType2;
    }

    public void setMaxLen2(int maxLen2) {
        this.maxLen2 = maxLen2;
    }

    public void setMinLen2(int minLen2) {
        this.minLen2 = minLen2;
    }

    public void setInfoType(int infoType) {
        this.infoType = infoType;
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

            Intent intent = new Intent(context, DispRekBSAActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString(EUIParamKeys.NAV_TITLE.toString(), title);
            bundle.putBoolean(EUIParamKeys.NAV_BACK.toString(), true);
            bundle.putString(EUIParamKeys.PROMPT_1.toString(), prompt1);
            bundle.putInt(EUIParamKeys.INPUT_MAX_LEN_1.toString(), maxLen1);
            bundle.putInt(EUIParamKeys.INPUT_MIN_LEN_1.toString(), minLen1);
            bundle.putSerializable(EUIParamKeys.INPUT_TYPE_1.toString(), inputType1);
            bundle.putString(EUIParamKeys.PROMPT_2.toString(), prompt2);
            bundle.putInt(EUIParamKeys.INPUT_MAX_LEN_2.toString(), maxLen2);
            bundle.putInt(EUIParamKeys.INPUT_MIN_LEN_2.toString(), minLen2);
//            bundle.putSerializable(EUIParamKeys.INPUT_TYPE_2.toString(), inputType2);
            intent.putExtras(bundle);
            context.startActivity(intent);


        }
    }
}
