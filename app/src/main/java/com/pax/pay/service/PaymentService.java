package com.pax.pay.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class PaymentService extends Service {

    @Override
    public IBinder onBind(Intent arg0) {
        return Payment.getInstance(this);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // android.os.Debug.waitForDebugger();// 跨应用调试
    }

}
