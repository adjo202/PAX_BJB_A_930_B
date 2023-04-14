/*
 * ============================================================================
 * COPYRIGHT
 *               Pax CORPORATION PROPRIETARY INFORMATION
 *    This software is supplied under the terms of a license agreement or
 *    nondisclosure agreement with Pax Corporation and may not be copied
 *    or disclosed except in accordance with the terms in that agreement.
 *       Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 *  Module Date: 2017-5-23 11:6
 *  Module Author: liliang
 *  Description:
 *  ============================================================================
 */

package com.pax.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.text.format.Formatter;
import android.util.Log;

import com.pax.device.Device;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.trans.receipt.PrintListenerImpl;
import com.pax.pay.trans.receipt.ReceiptPrintParam;
import com.pax.up.bjb.R;

/**
 * 其他管理
 * 
 * @author Sim.G
 * 
 */
public class SettingsOtherManageFragment extends PreferenceFragment {
    private Context context = null;
    private Handler handler = null;
    private CheckBoxPreference mPrefPrintDebug;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        handler = new Handler();
        setupSimplePreferencesScreen();
    }

    private void setupSimplePreferencesScreen() {
        try {
            // Add 'general' preferences.
            addPreferencesFromResource(R.xml.settings_other_manage_pref);
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_settting_param_print)));
            mPrefPrintDebug = (CheckBoxPreference) findPreference(getString(R.string.pref_settting__print_debug));
            bindPreferenceSummaryToValue(mPrefPrintDebug);
            boolean debug = FinancialApplication.getSysParam().getPrintDebug();
            mPrefPrintDebug.setChecked(debug);
            bindPreferenceSummaryToValue(findPreference(SysParam.OTHER_HEAD_CONTENT));

            long space = Device.getAvailableSpace(getActivity());
            Preference spacePreference = findPreference(getString(R.string.pref_pos_available_space));
            spacePreference.setSummary(Formatter.formatFileSize(getActivity(), space));
        } catch (Exception e) {
            Log.i( "abdul", "error setup" + e );
            e.printStackTrace();
        }
    }

    private Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();
            if (preference instanceof SettingsEditTextPreference
                    && preference.getKey().equals(SysParam.OTHER_HEAD_CONTENT)) {
                 // 签购单抬头内容
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    @Override
    public boolean onPreferenceTreeClick(android.preference.PreferenceScreen preferenceScreen, Preference preference) {
        if (preference.getKey().equals(getString(R.string.pref_settting_param_print))) {
            paraPrint();
        } else if (preference.getKey().equals(getString(R.string.pref_settting__print_debug))) {
            // add print debug
            boolean test = FinancialApplication.getSysParam().getPrintDebug();
            Log.i("abdul", "enable log = " + test);
            if (test) {
                FinancialApplication.getSysParam().setPrintDebug(false);
                Log.i("abdul", "set false = " + FinancialApplication.getSysParam().getPrintDebug());
            } else {
                FinancialApplication.getSysParam().setPrintDebug(true);
                Log.i("abdul", "set true = " + FinancialApplication.getSysParam().getPrintDebug());
            }
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences
                (preference.getContext());
        if (preference.getKey().equals(SysParam.OTHER_HEAD_CONTENT))
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, PreferenceManager
                    .getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), ""));
        else if (preference != mPrefPrintDebug) {
            String summary = sharedPref.getString(preference.getKey(), null);
            Log.i( "abdul", "summary = " + summary );
        }
    }

    /**
     * 参数打印
     */

    private void paraPrint() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ReceiptPrintParam.getInstance().print(
                        getString(R.string.othermanage_menu_set_para_print),
                        new PrintListenerImpl(context, handler));
            }
        }).start();
    }

}
