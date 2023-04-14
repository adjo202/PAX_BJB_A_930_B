package com.pax.pay.menu;

import com.pax.pay.trans.AuthCMTrans;
import com.pax.pay.trans.AuthCMVoidTrans;
import com.pax.pay.trans.AuthSettlementTrans;
import com.pax.pay.trans.AuthTrans;
import com.pax.pay.trans.AuthVoidTrans;
import com.pax.up.bjb.R;
import com.pax.view.MenuPage;

public class AuthMenuActivity extends BaseMenuActivity {

    /**
     * 预授权，预授权完成请求， 预授权完成通知， 预授权撤销， 预授权完成请求撤销
     */
    public MenuPage createMenuPage() {

        MenuPage.Builder builder = new MenuPage.Builder(AuthMenuActivity.this, 6, 3);
        builder.addTransItem(getString(R.string.auth_trans), R.drawable.app_auth,new AuthTrans(AuthMenuActivity.this, handler, true, null));
        builder.addTransItem(getString(R.string.auth_cm_req), R.drawable.authcm,new AuthCMTrans(AuthMenuActivity.this, handler, null));
        //builder.addTransItem(getString(R.string.auth_cm_adv), R.drawable.authcmn,new AuthSettlementTrans(AuthMenuActivity.this, handler, null));
        builder.addTransItem(getString(R.string.auth_void), R.drawable.authv,new AuthVoidTrans(AuthMenuActivity.this, handler, null));
        builder.addTransItem(getString(R.string.auth_cm_void), R.drawable.authcmv,new AuthCMVoidTrans(AuthMenuActivity.this, handler, null));

        return builder.create();
    }
}
