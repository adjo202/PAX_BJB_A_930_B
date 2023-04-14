package com.pax.pay.trans;

import android.content.Context;
import android.os.Handler;

import com.pax.pay.trans.model.ETransType;


/**
 * Created by Richard on 2017/5/6.
 */

public class MotoRefundTrans extends RefundTrans {

    public MotoRefundTrans(Context context, Handler handler, TransEndListener transListener) {
        super(context, handler, transListener);
        setTransType(ETransType.MOTO_REFUND);
    }
}
