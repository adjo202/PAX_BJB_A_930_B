/*******************************************************************************
 * ============================================================================
 * COPYRIGHT
 *               Pax CORPORATION PROPRIETARY INFORMATION
 *    This software is supplied under the terms of a license agreement or
 *    nondisclosure agreement with Pax Corporation and may not be copied
 *    or disclosed except in accordance with the terms in that agreement.
 *       Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 *  Module Date: 17-5-22 上午11:16
 *  Module Author: liliang
 *  Description:
 *  ============================================================================
 ******************************************************************************/

package com.pax.settings;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.pax.pay.app.ActivityStack;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.up.bjb.R;

import java.util.HashMap;
import java.util.List;

public class SettingsActivity extends SettingsHeaderPrefActivity implements OnClickListener {

    private ImageView backBtn;
    private TextView headerView;
    String title;
    HashMap<String, String> beforeSet = new HashMap<String, String>();
    HashMap<String, String> afterSet = new HashMap<String, String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initCustomActionBar();
        loadParam();
        initViews();
        setListeners();
        ActivityStack.getInstance().push(this);
        beforeSet = SysParam.getAll();
    }

    private static Header header;

    @Override
    public void onHeaderClick(Header header, int position) {
        super.onHeaderClick(header, position);
        /*
         * for (int i = 0; i < SysParam.settingsNames.length; i++) { if
         * (header.getTitle(getResources()).equals(SysParam.settingsNames[i])) {
         * headerView.setText(SysParam.settingsNames[i]); return; } }
         */
        this.header = header;
        SettingsEditTextPreference.mPositionOfPrefActHeader = position;
    }

    public void setSelectItemOfListView(int position) {
        if (mIsSinglePane) {//手机设备不执行该方法
            return;
        }
        getListView().setSelection(position);
    }

    private boolean initCustomActionBar() {
        ActionBar mActionbar = getActionBar();
        if (mActionbar == null) {
            return false;
        }
        mActionbar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        mActionbar.setDisplayShowCustomEnabled(true);
        mActionbar.setCustomView(R.layout.settings_header_layout);

        return true;
    }

    // 添加这个方法，以使2.x~4.3的代码在4.4上可以正常运行
    @SuppressLint("NewApi")
    @Override
    protected boolean isValidFragment(String fragmentName) {
        return true;
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        super.onBuildHeaders(target);
        loadHeadersFromResource(R.xml.settings_headers, target);
    }

    protected void loadParam() {
        title = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());
    }

    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        afterSet = SysParam.getAll();
        if (afterSet != null && !afterSet.toString().equals(beforeSet.toString())) {
            SysParam.uploadParamOnline(afterSet);
        }
        super.onDestroy();
    }

    private void initViews() {
        backBtn = (ImageView) findViewById(R.id.header_back);
        headerView = (TextView) findViewById(R.id.header_title);
        if (title != null) {
            headerView.setText(title);
        } else {
            headerView.setText(header.getTitle(getResources()));
        }
    }

    protected void setListeners() {
        backBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.header_back:
                setResult(100);// 第三方调用需要 //Call this to set the result that your activity will return to its caller.
                finish();
                break;
            default:
                break;
        }

    }

    protected void handleMsg(Message msg) {
    }

    public static boolean isSinglePane() {
        return mIsSinglePane;
    }
}
