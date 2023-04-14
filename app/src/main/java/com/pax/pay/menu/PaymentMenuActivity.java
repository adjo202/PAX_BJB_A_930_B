/*
 * ============================================================================
 * COPYRIGHT
 *               Pax CORPORATION PROPRIETARY INFORMATION
 *    This software is supplied under the terms of a license agreement or
 *    nondisclosure agreement with Pax Corporation and may not be copied
 *    or disclosed except in accordance with the terms in that agreement.
 *       Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 *  Module Date: 2017-7-12 4:55
 *  Module Author: liliang
 *  Description:
 *  ============================================================================
 */

package com.pax.pay.menu;

import android.view.View;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ATransaction;
import com.pax.abl.core.ActionResult;
import com.pax.pay.app.ActivityStack;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.trans.ESamsatTrans;
import com.pax.pay.trans.PBBTrans;
import com.pax.pay.trans.action.ActionDispMultiLineMsg;
import com.pax.settings.SysParam;
import com.pax.up.bjb.R;
import com.pax.view.MenuPage;

public class PaymentMenuActivity extends BaseMenuActivity {

    private ATransaction.TransEndListener listener = result -> handler.post( () -> {

    } );


    @Override
    public View createMenuPage() {
        MenuPage.Builder builder = new MenuPage.Builder( PaymentMenuActivity.this, 12, 3);

        builder.addMenuItem(getString(R.string.trans_ppob),          R.drawable.ic_ppob, PPOBActivity.class);
        builder.addTransItem(getString(R.string.trans_pbb),     R.drawable.ic_pbb_p2, new PBBTrans(PaymentMenuActivity.this, handler,isByPassPIN, listener) );
        builder.addMenuItem(getString(R.string.trans_mpn),      R.drawable.ic_mpn_g2, MPNG2MenuActivity.class);
        builder.addTransItem(getString(R.string.trans_esamsat),     R.drawable.ic_esamsat, new ESamsatTrans(PaymentMenuActivity.this, handler, isByPassPIN, listener));
        builder.addMenuItem(getString(R.string.detail_bpjs_tk),     R.drawable.ic_bpjs_tk, BPJSKetenagakerjaanMenuActivity.class);
        //builder.addActionItem(getString(R.string.detail_bpjs_tk), R.drawable.app_bpjs_tk, availableSoon());
        return builder.create();
    }



    private AAction availableSoon() {

        ActionDispMultiLineMsg displayInfoAction = new ActionDispMultiLineMsg(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {

                String[] display = new String[]{
                        "           Available soon, stay tune!",
                        " ",

                };

                ((ActionDispMultiLineMsg) action).setParam(PaymentMenuActivity.this, handler,
                        getString(R.string.version),
                        getString(R.string.app_version),
                        display, 60);
            }
        });

        displayInfoAction.setEndListener(new AAction.ActionEndListener() {
            @Override
            public void onEnd(AAction action, ActionResult result) {
                ActivityStack.getInstance().popTo(PaymentMenuActivity.this);
            }
        });

        return displayInfoAction;
    }



}
