package com.pax.pay.service;

import android.content.Context;
import android.content.Intent;
import android.os.ConditionVariable;
import android.os.RemoteException;

import com.pax.device.Device;
import com.pax.pay.PaymentActivity;
import com.pax.pay.app.ActivityStack;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.service.aidl.PayHelper.Stub;

public class Payment extends Stub {

    private Context context;
    private static Payment payment;

    private Payment(Context context) {
        this.context = context;
    }

    public static synchronized Payment getInstance(Context context) {
        if (payment == null) {
            payment = new Payment(context);
        }

        return payment;
    }

    private String result;
    private ConditionVariable cv;

    @Override
    public String doTrans(String jsonStr) throws RemoteException {
        ActivityStack.getInstance().popAll();
        result = "";
        cv = new ConditionVariable();
        Intent intent = new Intent(context, PaymentActivity.class);
        intent.putExtra("REQUEST", jsonStr);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        context.startActivity(intent);
        cv.block();
        if (FinancialApplication.getDal() != null) {
            //Device.enableHomeRecentKey(true);
            //Device.enableStatusBar(true);
        }
        return result;
    }

    public void setResult(String jsonRsp) {
        ActivityStack.getInstance().popAll();
        this.result = jsonRsp;
        if (cv != null) {
            cv.open();
        }
    }
}
