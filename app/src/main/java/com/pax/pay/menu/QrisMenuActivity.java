package com.pax.pay.menu;

import android.view.View;

import com.pax.pay.trans.QrGenerateTrans;
import com.pax.pay.trans.QrisSaleTrans;
import com.pax.up.bjb.R;
import com.pax.view.MenuPage;

public class QrisMenuActivity extends BaseMenuActivity {

    @Override
    public View createMenuPage() {
        MenuPage.Builder builder = new MenuPage.Builder(QrisMenuActivity.this,2,2);
                         builder.addTransItem(getString(R.string.txt_scan), R.drawable.scan_qr, new QrisSaleTrans(QrisMenuActivity.this, handler, null));
                         builder.addTransItem(getString(R.string.txt_generate), R.drawable.qr_generator, new QrGenerateTrans(QrisMenuActivity.this, handler, null));
        return builder.create();
    }
}
