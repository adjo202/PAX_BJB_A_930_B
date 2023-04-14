package com.pax.pay.menu;

import com.pax.pay.trans.EcBalanceTrans;
import com.pax.pay.trans.EcDetailQueryTrans;
import com.pax.pay.trans.EcRefundTrans;
import com.pax.pay.trans.EcSaleTrans;
import com.pax.up.bjb.R;
import com.pax.view.MenuPage;

public class EcMenuActivity extends BaseMenuActivity {

    /**
     * 电子现金消费，圈存， 电子现金余额查询， 明细查询， 脱机退货
     */
    public MenuPage createMenuPage() {
        MenuPage.Builder builder = new MenuPage.Builder(EcMenuActivity.this, 6, 3)
                .addTransItem(getString(R.string.ec_sale), R.drawable.ec_sale,
                        new EcSaleTrans(EcMenuActivity.this, handler, null))

//                .addMenuItem(getString(R.string.ec_load), R.drawable.ec_tsference, EcLoadMenuActivity.class)
                .addTransItem(getString(R.string.ec_balance), R.drawable.ec_balance,
                        new EcBalanceTrans(EcMenuActivity.this, handler, null))
                .addTransItem(getString(R.string.ec_detail), R.drawable.ec_detail,
                        new EcDetailQueryTrans(EcMenuActivity.this, handler, null))
                .addTransItem(getString(R.string.ec_offline_refund), R.drawable.offline_refund,
                        new EcRefundTrans(EcMenuActivity.this, handler, null));

        return builder.create();
    }
}
