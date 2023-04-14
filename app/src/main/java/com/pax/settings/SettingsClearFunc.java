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

import com.pax.up.bjb.R;

/**
 * 其他管理--清除功能
 * 
 * @author Sim.G
 * 
 */
public class SettingsClearFunc extends PreferenceFragment {
    SettingsMenuGeneralAdapter mGeneralAdapter;
    boolean[] defaultValues;
    String[] dispValues;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dispValues = new String[] { getString(R.string.om_cleartrade_menu_reserval),
                getString(R.string.om_cleartrade_menu_trade_voucher),
                getString(R.string.om_cleartrade_menu_deal_require), getString(R.string.om_cleartrade_menu_script),
                getString(R.string.om_cleartrade_menu_black_list) };
        defaultValues = new boolean[dispValues.length];
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mGeneralAdapter = new SettingsMenuGeneralAdapter(getActivity(), dispValues, defaultValues, false);
        View view = inflater.inflate(R.layout.settings_menu_gerneral_layout, container, false);
        ListView listView = (ListView) view.findViewById(R.id.fragment_menugerneral_listview);
        View footView = inflater.inflate(R.layout.settings_cancle, listView, false);
        Button cancelBtn = (Button) footView.findViewById(R.id.settings_cancle);
        Button confirmBtn = (Button) footView.findViewById(R.id.settings_confirm);
        cancelBtn.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        confirmBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mGeneralAdapter.clearFunc();
                getActivity().onBackPressed();
            }
        });

        if (!SettingsActivity.isSinglePane()) {
            View getFocusView = getActivity().getWindow().getDecorView().findFocus();
            getFocusView.setOnKeyListener(new OnKeyListener() {

                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                        mGeneralAdapter.clearFunc();
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
