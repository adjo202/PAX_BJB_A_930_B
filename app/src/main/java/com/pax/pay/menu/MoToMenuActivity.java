package com.pax.pay.menu;


import android.view.View;

import com.pax.pay.trans.MotoAuthAdviseTrans;
import com.pax.pay.trans.MotoAuthCMTrans;
import com.pax.pay.trans.MotoAuthCMVoidTrans;
import com.pax.pay.trans.MotoAuthTrans;
import com.pax.pay.trans.MotoAuthVoidTrans;
import com.pax.pay.trans.MotoRefundTrans;
import com.pax.pay.trans.MotoSaleTrans;
import com.pax.pay.trans.MotoVoidTrans;
import com.pax.up.bjb.R;
import com.pax.view.ListMenu;

/**
 * Created by Richard on 2017/5/3.
 */

public class MoToMenuActivity extends BaseMenuActivity {

    @Override
    public View createMenuPage() {

        ListMenu.Builder builder = new ListMenu.Builder(MoToMenuActivity.this)
                .addTransItem(getString(R.string.trans_moto_sale), R.drawable.ec_sale,
                        new MotoSaleTrans(MoToMenuActivity.this, handler, null))

                .addTransItem(getString(R.string.trans_moto_void), R.drawable.app_void,
                        new MotoVoidTrans(MoToMenuActivity.this, handler, null))

                .addTransItem(getString(R.string.trans_moto_refund), R.drawable.app_refund,
                        new MotoRefundTrans(MoToMenuActivity.this, handler, null))

                .addTransItem(getString(R.string.trans_moto_preauth), R.drawable.app_auth,
                        new MotoAuthTrans(MoToMenuActivity.this, handler, null))

                .addTransItem(getString(R.string.trans_moto_preauth_void), R.drawable.authv,
                        new MotoAuthVoidTrans(MoToMenuActivity.this, handler, null))

                .addTransItem(getString(R.string.trans_moto_preauth_comp), R.drawable.authcm,
                        new MotoAuthCMTrans(MoToMenuActivity.this, handler, null))

                .addTransItem(getString(R.string.trans_moto_preauth_comp_advise), R.drawable.authcmn,
                        new MotoAuthAdviseTrans(MoToMenuActivity.this, handler, null))

                .addTransItem(getString(R.string.trans_moto_preauth_comp_void), R.drawable.authcmv,
                        new MotoAuthCMVoidTrans(MoToMenuActivity.this, handler, null));

        return builder.create();
    }

}
