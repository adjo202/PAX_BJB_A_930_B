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

import com.pax.pay.ppob.PPOBPrabayarActivity;
import com.pax.pay.trans.CekStatusPulsaDataTrans;
import com.pax.pay.trans.PDAMTrans;
import com.pax.pay.trans.PascabayarTrans;
import com.pax.pay.trans.PosDownloadProduct;
import com.pax.up.bjb.R;
import com.pax.view.MenuPage;

public class PPOBActivity extends BaseMenuActivity {

    @Override
    public View createMenuPage() {
        MenuPage.Builder builder = new MenuPage.Builder(PPOBActivity.this, 10, 3);
        builder.addMenuItem(getString(R.string.trans_prabayar), R.drawable.ic_prabayar, PPOBPrabayarActivity.class);//pulsa --> prabayar
        builder.addTransItem(getString(R.string.trans_pascabayar), R.drawable.ic_pascabayar, new PascabayarTrans(PPOBActivity.this, handler, isByPassPIN,null));
        builder.addTransItem(getString(R.string.trans_pdam), R.drawable.ic_pdam, new PDAMTrans(PPOBActivity.this, handler, isByPassPIN, null));
        builder.addTransItem(getString(R.string.trans_download_product), R.drawable.ic_download_product, new PosDownloadProduct( PPOBActivity.this, handler, null));
        builder.addTransItem(getString(R.string.trans_cek_status), R.drawable.ic_cek_status, new CekStatusPulsaDataTrans(PPOBActivity.this, handler, null));
        return builder.create();
    }

}
