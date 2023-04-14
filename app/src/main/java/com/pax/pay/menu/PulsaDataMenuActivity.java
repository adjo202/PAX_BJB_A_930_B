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

import com.pax.pay.pulse.PulseActivity;
import com.pax.up.bjb.R;
import com.pax.view.MenuPage;

public class PulsaDataMenuActivity extends BaseMenuActivity {

    @Override
    public View createMenuPage() {
        MenuPage.Builder builder = new MenuPage.Builder( PulsaDataMenuActivity.this, 10, 2);
        builder.addMenuItem("Beli Pulsa/Data", R.drawable.app_pulsa_and_data, PulseActivity.class);
        return builder.create();
    }
}