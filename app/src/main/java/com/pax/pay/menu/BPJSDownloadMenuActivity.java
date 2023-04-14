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
import com.pax.abl.core.ActionResult;
import com.pax.pay.app.ActivityStack;
import com.pax.pay.trans.BPJSTkPembayaranTrans;
import com.pax.pay.trans.BPJSTkPendaftaranTrans;
import com.pax.pay.trans.PosDownloadBpjsTkData;
import com.pax.pay.trans.action.ActionBPJSTkDownload;
import com.pax.pay.trans.action.ActionTransOnline;
import com.pax.pay.trans.action.function.ActionOnlineParamProcess;
import com.pax.pay.trans.model.ETransType;
import com.pax.up.bjb.R;
import com.pax.view.MenuPage;
/**
 *
 sandy@indopay.com
 */

public class BPJSDownloadMenuActivity extends BaseMenuActivity {

    @Override
    public View createMenuPage() {
        MenuPage.Builder builder = new MenuPage.Builder(BPJSDownloadMenuActivity.this, 3, 3);
        builder.addActionItem(getString(R.string.trans_bpjs_location_text_only),R.drawable.ic_bpjs_tk_lokasi, createOnlineFunction(ETransType.DOWNLOAD_LOCATION_DATA_BPJS_TK.toString()));
        builder.addActionItem(getString(R.string.trans_bpjs_branch_office_text_only),R.drawable.ic_bpjs_tk_kantorcabang, createOnlineFunction(ETransType.DOWNLOAD_BRANCH_OFFICE_DATA_BPJS_TK.toString()));
        builder.addActionItem(getString(R.string.trans_bpjs_district_data_text_only),R.drawable.ic_bpjs_tk_kabkot, createOnlineFunction(ETransType.DOWNLOAD_DISTRICT_DATA_BPJS_TK.toString()));

        return builder.create();

    }



    private AAction createOnlineFunction(final String transTpye) {

        ActionBPJSTkDownload actionOnlineParamProcess = new ActionBPJSTkDownload(new AAction.ActionStartListener() {
            @Override
            public void onStart(AAction action) {
                ((ActionBPJSTkDownload) action).setParam(BPJSDownloadMenuActivity.this, transTpye);
            }
        });

        actionOnlineParamProcess.setEndListener(new AAction.ActionEndListener() {
            @Override
            public void onEnd(AAction action, ActionResult result) {
                ActivityStack.getInstance().popAllButBottom();
            }
        });

        return actionOnlineParamProcess;

    }


}
