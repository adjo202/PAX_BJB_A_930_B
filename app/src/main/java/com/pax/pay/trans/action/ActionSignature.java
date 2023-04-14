package com.pax.pay.trans.action;

import android.content.Context;
import android.content.Intent;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.action.activity.SignatureActivity;
import com.pax.settings.SysParam;

public class ActionSignature extends AAction {

    private String amount;
    private String featureCode;
    private Context context;

    public ActionSignature(ActionStartListener listener) {
        super(listener);
    }

    public void setParam(Context context, String amount, String featureCode) {
        this.context = context;
        this.amount = amount;
        this.featureCode = featureCode;
    }

    @Override
    protected void process() {
        // 终端设置不支持电子签名
        if (FinancialApplication.getSysParam().get(SysParam.OTHTC_SINGATURE).equals(SysParam.Constant.NO)) {
            setResult(new ActionResult(TransResult.SUCC, null));
            return;
        }

        Intent intent = new Intent(context, SignatureActivity.class);
        intent.putExtra(EUIParamKeys.TRANS_AMOUNT.toString(), amount);
        intent.putExtra(EUIParamKeys.SIGN_FEATURE_CODE.toString(), featureCode);
        context.startActivity(intent);
    }

}
