/*******************************************************************************
 * ============================================================================
 * COPYRIGHT
 *               Pax CORPORATION PROPRIETARY INFORMATION
 *    This software is supplied under the terms of a license agreement or
 *    nondisclosure agreement with Pax Corporation and may not be copied
 *    or disclosed except in accordance with the terms in that agreement.
 *       Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 *  Module Date: 17-5-22 上午11:18
 *  Module Author: liliang
 *  Description:
 *  ============================================================================
 ******************************************************************************/

package com.pax.settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.pax.pay.app.FinancialApplication;
import com.pax.up.bjb.R;

public class SettingsMenuElectronicCash extends PreferenceFragment {
    SettingsMenuGeneralAdapter mGeneralAdapter;
    boolean[] defaultValues; // 交易默认选中状态
    String[] dispValues;// 界面显示交易名称
    String[] entries;// sharePreference中的key
    String[] entryValues;// sharePreferene中的Value
    SysParam sysParam = FinancialApplication.getSysParam();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dispValues = new String[] { getString(R.string.st_ec_menu_ecsale_contact),
                getString(R.string.st_ec_menu_qc_acc), getString(R.string.st_ec_menu_qc_noacc),
                getString(R.string.st_ec_menu_cash), getString(R.string.st_ec_menu_uncash),
                getString(R.string.st_ec_menu_offrefund), getString(R.string.st_ec_menu_query) };
        entries = new String[] { SysParam.getAll().get(SysParam.ECTS_SALE), SysParam.getAll().get(SysParam.ECTS_LOAD),
                SysParam.getAll().get(SysParam.ECTS_TLOAD), SysParam.getAll().get(SysParam.ECTS_CALOAD),
                SysParam.getAll().get(SysParam.ECTS_CALOADVOID), SysParam.getAll().get(SysParam.ECTS_REFUND),
                SysParam.getAll().get(SysParam.ECTS_QUERY) };
        entryValues = new String[] { SysParam.ECTS_SALE, SysParam.ECTS_LOAD, SysParam.ECTS_TLOAD, SysParam.ECTS_CALOAD,
                SysParam.ECTS_CALOADVOID, SysParam.ECTS_REFUND, SysParam.ECTS_QUERY };
        defaultValues = new boolean[dispValues.length];

        for (int i = 0; i < defaultValues.length; i++) {
            defaultValues[i] = "Y".equals(entries[i]) ? true : false;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mGeneralAdapter = new SettingsMenuGeneralAdapter(getActivity(), dispValues, defaultValues, false);
        View view = inflater.inflate(R.layout.settings_menu_gerneral_layout, null);
        ListView listView = (ListView) view.findViewById(R.id.fragment_menugerneral_listview);
        View footView = inflater.inflate(R.layout.settings_confirm, listView, false);
        final Button confirmBtn = (Button) footView.findViewById(R.id.settings_button);
        confirmBtn.requestFocus();
        confirmBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mGeneralAdapter.saveForMenuGeral(entryValues, 1);
                getActivity().onBackPressed();
            }
        });

        if (!SettingsActivity.isSinglePane()) {
            View getFocusView = getActivity().getWindow().getDecorView().findFocus();
            getFocusView.setOnKeyListener(new OnKeyListener() {

                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                        mGeneralAdapter.saveForMenuGeral(entryValues, 1);
                        getActivity().onBackPressed();
                        return true;
                    }
                    return false;
                }
            });
        }
        listView.addFooterView(footView);
        listView.setAdapter(mGeneralAdapter);
        return view;
    }

}
