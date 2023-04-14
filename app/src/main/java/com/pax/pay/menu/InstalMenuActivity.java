package com.pax.pay.menu;


import com.pax.pay.trans.InstalSaleTrans;
import com.pax.pay.trans.InstalVoidTrans;
import com.pax.up.bjb.R;
import com.pax.view.MenuPage;

public class InstalMenuActivity extends BaseMenuActivity {

    /**
     * 分期消费，分期撤销
     */
    public MenuPage createMenuPage() {
        MenuPage.Builder builder = new MenuPage.Builder(InstalMenuActivity.this, 3, 3)
                .addTransItem(getString(R.string.installment_Sale), R.drawable.ec_sale,
                        new InstalSaleTrans(InstalMenuActivity.this, handler, null))

                .addTransItem(getString(R.string.installment_Void), R.drawable.app_void,
                        new InstalVoidTrans(InstalMenuActivity.this, handler, null));

        return builder.create();
    }
}
