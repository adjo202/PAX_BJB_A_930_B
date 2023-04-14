package com.pax.pay.trans;

import android.content.Context;
import android.os.Handler;

import com.pax.pay.trans.model.ETransType;

/**
 * Created by Richard on 2017/5/6.
 */

public class MotoVoidTrans extends SaleVoidTrans {

    public MotoVoidTrans(Context context, Handler handler, TransEndListener transListener) {
        super(context, handler, transListener);
        setTransType(ETransType.MOTO_VOID);
    }
}
