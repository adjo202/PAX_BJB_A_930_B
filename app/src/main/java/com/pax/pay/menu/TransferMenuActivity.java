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

import com.pax.pay.trans.FundTransferTrans;
import com.pax.pay.trans.OverbookingTrans;
import com.pax.up.bjb.R;
import com.pax.view.MenuPage;

public class TransferMenuActivity extends BaseMenuActivity {

    @Override
    public View createMenuPage() {
        MenuPage.Builder builder = new MenuPage.Builder(TransferMenuActivity.this, 6, 3);
        builder.addTransItem(getString(R.string.antar_bank), R.drawable.ic_transfer_antar_bank, new FundTransferTrans(TransferMenuActivity.this, handler, null, isByPassPIN, null));
        builder.addTransItem(getString(R.string.sesama_bank), R.drawable.ic_transfer_antar_rekening, new OverbookingTrans(TransferMenuActivity.this, handler, null, isByPassPIN, null));
        return builder.create();
    }

}
