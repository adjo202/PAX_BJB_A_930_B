package com.pax.pay.trans;

import android.content.Context;
import android.os.Handler;

import com.pax.pay.trans.model.ETransType;

/**
 * Created by Richard on 2017/5/8.
 */

public class MotoAuthTrans extends AuthTrans {

    public MotoAuthTrans(Context context, Handler handler, TransEndListener transListener) {
        super(context, handler, true, transListener);
        setTransType(ETransType.MOTO_AUTH);
    }

}