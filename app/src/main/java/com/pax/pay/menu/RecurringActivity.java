/*
 * ============================================================================
 * COPYRIGHT
 *              Pax CORPORATION PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with Pax Corporation and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *      Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 * Module Date: 2017-5-08
 * Module Author: Barret
 * Description:
 *
 * ============================================================================
 */

package com.pax.pay.menu;

import com.pax.pay.trans.RecurringTrans;
import com.pax.pay.trans.RecurringVoidTrans;
import com.pax.up.bjb.R;
import com.pax.view.MenuPage;

/**
 * Created by xionggd on 2017/5/9.
 */
public class RecurringActivity extends BaseMenuActivity{
    public MenuPage createMenuPage() {
        MenuPage.Builder builder = new MenuPage.Builder(RecurringActivity.this, 3, 3)
                .addTransItem(getString(R.string.trans_recurring), R.drawable.ec_sale,
                        new RecurringTrans(RecurringActivity.this, handler, null))

                .addTransItem(getString(R.string.trans_recurring_void), R.drawable.app_void,
                        new RecurringVoidTrans(RecurringActivity.this, handler, null));

        return builder.create();
    }

}
