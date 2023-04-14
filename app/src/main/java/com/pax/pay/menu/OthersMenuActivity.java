package com.pax.pay.menu;

import com.pax.view.MenuPage;



public class OthersMenuActivity extends BaseMenuActivity {

    @Override
    public MenuPage createMenuPage() {

        MenuPage.Builder builder = new MenuPage.Builder(OthersMenuActivity.this, 9, 3);

        //DANA
        //builder.addTransItem(getString(R.string.dana), R.drawable.app_dana, new DanaSaleTrans(OthersMenuActivity.this, handler, null));


        //E-CASH
        //builder.addMenuItem(getString(R.string.electronic_cash), R.drawable.app_ec, EcMenuActivity.class);

        //Installment
        //builder.addMenuItem(getString(R.string.installment_trans), R.drawable.installment, InstalMenuActivity.class);

        //Balance inquiry
        //builder.addTransItem(getString(R.string.trans_balance), R.drawable.balance_query, new BalanceTrans(OthersMenuActivity.this, handler, null ));

        //Exchange rate download
        //builder.addTransItem(getString(R.string.rate_down), R.drawable.rate_download, new DownloadRate(OthersMenuActivity.this, handler, null ));

        //MO/TO
        //builder.addMenuItem(getString(R.string.trans_moto), R.drawable.mo_to, MoToMenuActivity.class);

        //Recurring
        //builder.addMenuItem(getString(R.string.trans_recurring), R.drawable.recurring, RecurringActivity.class);

        return builder.create();
    }
}
