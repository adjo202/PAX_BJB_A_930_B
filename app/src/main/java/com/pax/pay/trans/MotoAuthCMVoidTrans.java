package com.pax.pay.trans;

import android.content.Context;
import android.os.Handler;

import com.pax.abl.core.ActionResult;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;

/**
 * Created by Richard on 2017/5/8.
 */

public class MotoAuthCMVoidTrans extends AuthCMVoidTrans {
    public MotoAuthCMVoidTrans(Context context, Handler handler, TransEndListener transListener) {
        super(context, handler, transListener);
        setTransType(ETransType.MOTO_AUTHCMVOID);
    }

    // Verify the original transaction
    @Override
    protected void validateOrigTransData(long origTransNo) {
        origRecord = TransData.readTrans(origTransNo);
        if (origRecord == null) {
            // 交易不存在
            transEnd(new ActionResult(TransResult.ERR_NO_ORIG_TRANS, null));
            return;
        }
        String trType = origRecord.getTransType();
        // 非预授权完成请求交易不能撤销
        if (!trType.equals(ETransType.MOTO_AUTHCM.toString())) {
            transEnd(new ActionResult(TransResult.ERR_VOID_UNSUPPORT, null));
            return;
        }

        String trStatus = origRecord.getTransState();
        // 已撤销交易，不能重复撤销
        if (trStatus.equals(TransData.ETransStatus.VOID.toString())) {
            transEnd(new ActionResult(TransResult.ERR_HAS_VOID, null));
            return;
        }
        copyOrigTransData();
        gotoState(State.TRANS_DETAIL.toString());

    }
}
