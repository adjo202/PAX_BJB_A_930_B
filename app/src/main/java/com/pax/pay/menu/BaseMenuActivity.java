package com.pax.pay.menu;

import android.app.ActionBar.LayoutParams;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pax.pay.BaseActivity;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.up.bjb.R;

public abstract class BaseMenuActivity extends BaseActivity implements OnClickListener {

    /**
     * 菜单容器
     */
    private LinearLayout llContainer;
    /**
     * 抬头
     */
    private TextView tvTitle;
    /**
     * 返回按钮
     */
    private ImageView IvBack;

    /**
     * 显示的抬头
     */
    private String navTitle;
    /**
     * 是否显示返回按钮
     */
    private boolean navBack;


    protected boolean isByPassPIN = false;



    @Override
    protected int getLayoutId() {
        return R.layout.menu_layout;
    }

    @Override
    protected void loadParam() {
        navTitle = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());
        navBack = getIntent().getBooleanExtra(EUIParamKeys.NAV_BACK.toString(), false);
    }

    @Override
    protected void initViews() {
        tvTitle = (TextView) findViewById(R.id.header_title);
        tvTitle.setText(navTitle);
        IvBack = (ImageView) findViewById(R.id.header_back);
        llContainer = (LinearLayout) findViewById(R.id.ll_container);

        android.widget.LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);

        llContainer.addView(createMenuPage(), params);

    }

    public abstract View createMenuPage();

    @Override
    protected void setListeners() {

        if (!navBack) {
            IvBack.setVisibility(View.GONE);
        } else {
            IvBack.setOnClickListener(this);
        }

    }

    @Override
    protected void handleMsg(Message msg) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.header_back:
                finish();
                break;

            default:
                break;
        }

    }

}
