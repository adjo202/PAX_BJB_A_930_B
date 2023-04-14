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

import com.pax.pay.trans.AccountCancellationTrans;
import com.pax.pay.trans.BalanceInquiryTrans;
import com.pax.pay.trans.ChangePinTrans;
import com.pax.pay.trans.FundTransferTrans;
import com.pax.pay.trans.MiniStatementTrans;
import com.pax.pay.trans.OpenAccountTrans;
import com.pax.pay.trans.SetorTunaiTrans;
import com.pax.pay.trans.TarikTunaiTrans;
import com.pax.pay.trans.TestingTrans;
import com.pax.up.bjb.R;
import com.pax.view.MenuPage;

public class MiniBankingActivity extends BaseMenuActivity {

    @Override
    public View createMenuPage() {

        MenuPage.Builder builder = new MenuPage.Builder(MiniBankingActivity.this, 12, 3);
        //builder.addTransItem(getString(R.string.trans_account_list), R.drawable.app_balance, new AccountListTrans(MiniBankingActivity.this, handler, null,true, null));
        builder.addTransItem(getString(R.string.trans_balance_information), R.drawable.ic_info_saldo, new BalanceInquiryTrans(MiniBankingActivity.this, handler, null,isByPassPIN, null));
        builder.addTransItem(getString(R.string.trans_deposit), R.drawable.ic_setor_tunai, new SetorTunaiTrans(MiniBankingActivity.this, handler, null, isByPassPIN, null));
        builder.addTransItem(getString(R.string.trans_withdrawal), R.drawable.ic_tarik_tunai, new TarikTunaiTrans(MiniBankingActivity.this, handler, null, isByPassPIN, null));
        builder.addTransItem(getString(R.string.trans_mini_statement), R.drawable.ic_mini_statement, new MiniStatementTrans(MiniBankingActivity.this, handler, null));
        builder.addTransItem(getString(R.string.trans_change_pin), R.drawable.ic_ganti_pin, new ChangePinTrans(MiniBankingActivity.this, handler, null));
        builder.addTransItem(getString(R.string.trans_opening_account), R.drawable.ic_pembukaan_rekening, new OpenAccountTrans(MiniBankingActivity.this, handler, null, isByPassPIN, null));
        builder.addTransItem(getString(R.string.trans_cancelation_account), R.drawable.ic_pembatalan_rekening, new AccountCancellationTrans(MiniBankingActivity.this, handler, null, isByPassPIN, null));
//        builder.addTransItem(getString(R.string.trans_fund_transfer), R.drawable.transfer, new FundTransferTrans(MiniBankingActivity.this, handler, null, true, null));
        builder.addMenuItem(getString(R.string.trans_fund_transfer), R.drawable.ic_transfer, TransferMenuActivity.class);
        //builder.addTransItem("test", R.drawable.app_ec, new TestingTrans(MiniBankingActivity.this, handler, null, true, null));

        return builder.create();
    }

}
