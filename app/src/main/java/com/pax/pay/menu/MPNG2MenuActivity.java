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

import com.pax.pay.trans.CetakUlangTrans;
import com.pax.pay.trans.MpnTrans;
import com.pax.pay.trans.model.ETransType;
import com.pax.up.bjb.R;
import com.pax.view.MenuPage;

public class MPNG2MenuActivity extends BaseMenuActivity {

    @Override
    public View createMenuPage() {
        MenuPage.Builder builder = new MenuPage.Builder(MPNG2MenuActivity.this, 6, 3);
        builder.addTransItem(getString(R.string.trans_djp), R.drawable.ic_djp, new MpnTrans(MPNG2MenuActivity.this, handler, null,isByPassPIN,  ETransType.DIRJEN_PAJAK_INQUIRY,1,null));
        builder.addTransItem(getString(R.string.trans_djbc), R.drawable.ic_djbc, new MpnTrans(MPNG2MenuActivity.this, handler, null,isByPassPIN, ETransType.DIRJEN_BEA_CUKAI_INQUIRY, 2,null));
        builder.addTransItem(getString(R.string.trans_dja), R.drawable.ic_dja, new MpnTrans(MPNG2MenuActivity.this, handler, null,isByPassPIN, ETransType.DIRJEN_ANGGARAN_INQUIRY, 3,null));
        builder.addTransItem(getString(R.string.trans_cetak_ulang), R.drawable.ic_cetak_ulang, new CetakUlangTrans(MPNG2MenuActivity.this, handler, null));
        return builder.create();
    }

}
