/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 * Module Date: 2017-2-27
 * Module Author: Kim.L
 * Description:
 *
 * ============================================================================
 */
package com.pax.pay.trans.action;

import android.content.Context;
import android.os.Handler;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.pay.trans.TransResult;
import com.pax.up.bjb.R;
import com.pax.view.dialog.CustomAlertDialog;

public class ActionConfirmLock extends AAction {

    public ActionConfirmLock(ActionStartListener listener) {
        super(listener);
    }

    private Handler handler;
    private Context context;

    public void setParam(Context context, Handler handler) {
        this.context = context;
        this.handler = handler;
    }

    private CustomAlertDialog dialog = null;

    @Override
    protected void process() {
        handler.post(new Runnable() {

            @Override
            public void run() {
                dialog = new CustomAlertDialog(context, CustomAlertDialog.NORMAL_TYPE);
                dialog.setCancelClickListener(new CustomAlertDialog.OnCustomClickListener() {
                    @Override
                    public void onClick(CustomAlertDialog alertDialog) {
                        setResult(new ActionResult(TransResult.ERR_ABORTED, null));
                        dialog.dismiss();
                    }
                });
                dialog.setConfirmClickListener(new CustomAlertDialog.OnCustomClickListener() {
                    @Override
                    public void onClick(CustomAlertDialog alertDialog) {
                        setResult(new ActionResult(TransResult.SUCC, null));
                        dialog.dismiss();
                    }
                });
                dialog.show();
                dialog.setNormalText(context.getString(R.string.pos_lockup));
                dialog.showCancelButton(true);
                dialog.showConfirmButton(true);
            }
        });

    }

}
