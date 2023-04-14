package com.pax.pay.menu;

import com.pax.pay.trans.OfflineSettleTrans;
import com.pax.pay.trans.SettleAdjustTrans;
import com.pax.up.bjb.R;
import com.pax.view.MenuPage;

/**
 * Created by huangzg on 2017/3/27.
 */

public class OfflineMenuActivity extends BaseMenuActivity {
    @Override
    public MenuPage createMenuPage() {

        MenuPage.Builder builder = new MenuPage.Builder(OfflineMenuActivity.this, 3, 3)
                .addTransItem(getString(R.string.offline_settle), R.drawable.offline_settle,
                        new OfflineSettleTrans(OfflineMenuActivity.this, handler, null))
                .addTransItem(getString(R.string.settle_adjust), R.drawable.settle_adjust,
                        new SettleAdjustTrans(OfflineMenuActivity.this, handler, null));
        return builder.create();
    }
}
