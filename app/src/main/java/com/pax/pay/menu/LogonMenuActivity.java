package com.pax.pay.menu;

import com.pax.pay.operator.OperLogonActivity;
import com.pax.pay.trans.PosLogon;
import com.pax.up.bjb.R;
import com.pax.view.MenuPage;

/**
 * Created by huangzg on 2017/3/27.
 */

public class LogonMenuActivity extends BaseMenuActivity {
    @Override
    public MenuPage createMenuPage() {

        MenuPage.Builder builder = new MenuPage.Builder(LogonMenuActivity.this, 3, 3)
                // Terminal Logon
                .addTransItem(getString(R.string.pos_terminal_logon), R.drawable.app_poslogon,
                        new PosLogon(LogonMenuActivity.this, handler, null))
                //Operator Logon
                .addMenuItem(getString(R.string.oper_logon), R.drawable.app_operlogin,
                        OperLogonActivity.class);
        return builder.create();
    }
}
