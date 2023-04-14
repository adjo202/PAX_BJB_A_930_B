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

import com.pax.pay.trans.CouponSaleTrans;
import com.pax.pay.trans.CouponVoidTrans;
import com.pax.up.bjb.R;
import com.pax.view.MenuPage;

public class CouponMenuActivity extends BaseMenuActivity {

    @Override
    public View createMenuPage() {
        MenuPage.Builder builder = new MenuPage.Builder(CouponMenuActivity.this, 6, 3);
        builder.addTransItem(getString(R.string.coupon_sale), R.drawable.app_ec, new CouponSaleTrans(CouponMenuActivity.this, handler, null));
        builder.addTransItem(getString(R.string.coupon_void), R.drawable.app_ec, new CouponVoidTrans(CouponMenuActivity.this, handler, null));
        return builder.create();
    }
}
