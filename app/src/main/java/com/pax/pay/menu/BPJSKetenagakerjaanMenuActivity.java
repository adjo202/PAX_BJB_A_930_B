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

import com.pax.pay.trans.BPJSTkPembayaranTrans;
import com.pax.pay.trans.BPJSTkPendaftaranTrans;
import com.pax.pay.trans.CetakUlangTrans;
import com.pax.pay.trans.MpnTrans;
import com.pax.pay.trans.PosDownloadBpjsTkData;
import com.pax.pay.trans.model.ETransType;
import com.pax.up.bjb.R;
import com.pax.view.MenuPage;

public class BPJSKetenagakerjaanMenuActivity extends BaseMenuActivity {

    @Override
    public View createMenuPage() {
        MenuPage.Builder builder = new MenuPage.Builder(BPJSKetenagakerjaanMenuActivity.this, 3, 3);
        builder.addTransItem(getString(R.string.detail_bpjs_pendaftaran), R.drawable.ic_bpjs_tk_pendaftaran, new BPJSTkPendaftaranTrans(BPJSKetenagakerjaanMenuActivity.this, handler, null,true,  ETransType.BPJS_TK_PENDAFTARAN,1,null));
        builder.addTransItem(getString(R.string.detail_bpjs_pembayaran), R.drawable.ic_bpjs_tk_pembayaran, new BPJSTkPembayaranTrans(BPJSKetenagakerjaanMenuActivity.this, handler, null,true, ETransType.BPJS_TK_PEMBAYARAN, 2,null));
        builder.addMenuItem(getString(R.string.detail_bpjs_tk_update_data),R.drawable.ic_bpjs_tk_download,BPJSDownloadMenuActivity.class);
        return builder.create();
    }

}
