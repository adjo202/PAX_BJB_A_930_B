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

public class SettingsMenuGeneral extends PreferenceFragment {
    SettingsMenuGeneralAdapter mGeneralAdapter;
    boolean[] defaultValues; // 交易的选中状态
    String[] dispValues; // 用于界面显示的字符串
    String[] entries; // sharePreference 文件中对应的key
    String[] entryValues; // sharePreference 文件中对应的Value
    SysParam sysParam = FinancialApplication.getSysParam();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dispValues = new String[] { getString(R.string.st_general_menu_sale),
                getString(R.string.st_general_menu_unsale), getString(R.string.st_general_menu_refund),
                getString(R.string.st_general_menu_query), getString(R.string.st_general_menu_preauth),
                getString(R.string.st_general_menu_unpreauth), getString(R.string.st_general_menu_preauth_complete),
                getString(R.string.st_general_menu_preauth_offcomplete),
                getString(R.string.st_general_menu_preauth_uncomplete), getString(R.string.st_general_menu_offsettle),
                getString(R.string.st_general_menu_settleadjust) };
        entries = new String[] {
                SysParam.getAll().get(SysParam.TTS_SALE),
                SysParam.getAll().get(SysParam.TTS_VOID),
                SysParam.getAll().get(SysParam.TTS_REFUND),
                SysParam.getAll().get(SysParam.TTS_BALANCE),
                SysParam.getAll().get(SysParam.TTS_PREAUTH),
                SysParam.getAll().get(SysParam.TTS_PAVOID),
                SysParam.getAll().get(SysParam.TTS_PACREQUEST),
                SysParam.getAll().get(SysParam.TTS_PACADVISE),
                SysParam.getAll().get(SysParam.TTS_PACVOID),
                SysParam.getAll().get(SysParam.TTS_OFFLINE_SETTLE),
                SysParam.getAll().get(SysParam.TTS_ADJUST) };
        entryValues = new String[] {
                SysParam.TTS_SALE,
                SysParam.TTS_VOID,
                SysParam.TTS_REFUND,
                SysParam.TTS_BALANCE,
                SysParam.TTS_PREAUTH,
                SysParam.TTS_PAVOID,
                SysParam.TTS_PACREQUEST,
                SysParam.TTS_PACADVISE,
                SysParam.TTS_PACVOID,
                SysParam.TTS_OFFLINE_SETTLE,
                SysParam.TTS_ADJUST };
        defaultValues = new boolean[dispValues.length];

        for (int i = 0; i < defaultValues.length; i++) {
            defaultValues[i] = "Y".equals(entries[i]) ? true : false;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mGeneralAdapter = new SettingsMenuGeneralAdapter(getActivity(), dispValues, defaultValues, false);
        View view = inflater.inflate(R.layout.settings_menu_gerneral_layout, container, false);
        ListView listView = (ListView) view.findViewById(R.id.fragment_menugerneral_listview);
        View footView = inflater.inflate(R.layout.settings_confirm, listView, false);
        final Button confirmBtn = (Button) footView.findViewById(R.id.settings_button);
        confirmBtn.requestFocus();
        confirmBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mGeneralAdapter.saveForMenuGeral(entryValues, 0);
                getActivity().onBackPressed();
            }
        });
        if (!SettingsActivity.isSinglePane()) {
            View getFocusView = getActivity().getWindow().getDecorView().findFocus();
            getFocusView.setOnKeyListener(new OnKeyListener() {

                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                        mGeneralAdapter.saveForMenuGeral(entryValues, 0);
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
