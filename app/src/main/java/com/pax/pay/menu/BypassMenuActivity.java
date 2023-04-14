package com.pax.pay.menu;

import com.pax.pay.trans.AuthTrans;
import com.pax.pay.trans.SaleTrans;
import com.pax.pay.trans.action.ActionSearchCard;
import com.pax.up.bjb.R;
import com.pax.view.MenuPage;

public class BypassMenuActivity extends BaseMenuActivity {

    @Override
    public MenuPage createMenuPage() {
        MenuPage.Builder builder = new MenuPage.Builder(BypassMenuActivity.this, 3, 3)

        .addTransItem(getString(R.string.quick_pass_sale_force_pin), R.drawable.sale_bypin,
                new SaleTrans(BypassMenuActivity.this, handler, null, ActionSearchCard.SearchMode.TAP, false, null)).addTransItem(
                getString(R.string.quick_pass_auth_force_pin), R.drawable.auth_bypin,
                new AuthTrans(BypassMenuActivity.this, handler, false, null));
        return builder.create();
    }

}
