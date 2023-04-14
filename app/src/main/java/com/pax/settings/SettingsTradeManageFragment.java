/*
 * ============================================================================
 * COPYRIGHT
 *               Pax CORPORATION PROPRIETARY INFORMATION
 *    This software is supplied under the terms of a license agreement or
 *    nondisclosure agreement with Pax Corporation and may not be copied
 *    or disclosed except in accordance with the terms in that agreement.
 *       Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 *  Module Date: 2017-5-22 5:37
 *  Module Author: liliang
 *  Description:
 *  ============================================================================
 */

package com.pax.settings;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.pax.up.bjb.R;

/**
 * 交易管理
 * 
 * @author Sim.G
 * 
 */
public class SettingsTradeManageFragment extends PreferenceFragment {
    public static final String TAG = "TradeManageFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupSimplePreferencesScreen();
    }

    private void setupSimplePreferencesScreen() {
        addPreferencesFromResource(R.xml.settings_trade_manage_pref);
        bindPreferenceSummaryToValue(findPreference(SysParam.OFFLINETC_UPLOAD_TYPE));
        bindPreferenceSummaryToValue(findPreference(SysParam.HOME_TRANS));
        bindPreferenceSummaryToValue(findPreference(SysParam.OFFLINETC_UPLOADTIMES));
        bindPreferenceSummaryToValue(findPreference(SysParam.OFFLINETC_UPLOADNUM));
        bindPreferenceSummaryToValue(findPreference(SysParam.OTHTC_REFUNDLIMT));
        bindPreferenceSummaryToValue(findPreference(SysParam.OTHTC_EMV_OPR));   //操作员EMV选择
        //bindPreferenceSummaryToValue(findPreference(SysParam.OTHTC_ECR_TOUT));   //ECR超时时间
        bindPreferenceSummaryToValue(findPreference(SysParam.QUICK_PASS_TRANS_SIGN_PIN_FREE_AMOUNT)); // 免签免密金额
    }

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {

            String stringValue = value.toString();
            int cmpValue = -1;
            String pref_key = preference.getKey();

            boolean flag = false;
            if (pref_key.equals(SysParam.HOME_TRANS) || pref_key.equals(SysParam.OFFLINETC_UPLOAD_TYPE)
                    || pref_key.equals(SysParam.OTHTC_EMV_OPR)) {
                SettingsSingleChoicePreference listPreference = (SettingsSingleChoicePreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);
                stringValue = (String) (index >= 0 ? listPreference.getEntries()[index] : null);
            } else if (pref_key.equals(SysParam.OFFLINETC_UPLOADTIMES)) {// 设置离线上送次数
                try {
                    cmpValue = Integer.parseInt(stringValue);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "", e);
                    cmpValue = -1;
                }
                if (cmpValue < 1 || cmpValue > 9) {
                    flag = true;
                }
                stringValue = String.valueOf(cmpValue);
            } else if (pref_key.equals(SysParam.OFFLINETC_UPLOADNUM)) {
                // 设置自动上送累计笔数
                try {
                    cmpValue = Integer.parseInt(stringValue);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "", e);
                    cmpValue = -1;
                }
                if (cmpValue < 1 || cmpValue > 90) {
                    flag = true;
                }
                stringValue = String.valueOf(cmpValue);
            } else if (pref_key.equals(SysParam.OTHTC_REFUNDLIMT)) { // 设置最大退货金额,单位：元
                try {
                    cmpValue = Integer.parseInt(stringValue);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "", e);
                    cmpValue = -1;
                }
                if (cmpValue < 1 || cmpValue > 1000000) {
                    flag = true;
                }
                stringValue = String.valueOf(cmpValue);
            } else if (pref_key.equals(SysParam.QUICK_PASS_TRANS_SIGN_PIN_FREE_AMOUNT)) {    //Jerry add
                // 设置免签免密限额，单位：分
                try {
                    cmpValue = Integer.parseInt(stringValue);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "", e);
                    cmpValue = -1;
                }
                if (cmpValue < 1 || cmpValue > 100000) {
                    flag = true;
                }
                stringValue = String.valueOf(cmpValue);
            }/*else if (pref_key.equals(SysParam.OTHTC_ECR_TOUT)) {       //ECR连接超时时间 Jerry  单位：秒
                try {
                    cmpValue = Integer.parseInt(stringValue);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "", e);
                    cmpValue = -1;
                }
                if (cmpValue < 1 || cmpValue > 900) {
                    flag = true;
                }
                stringValue = String.valueOf(cmpValue);
            }*/

            if (flag) {
                Toast.makeText(preference.getContext(), R.string.input_err, Toast.LENGTH_SHORT).show();
                return false;
            }

            preference.setSummary(stringValue);

            return true;
        }
    };

    private static void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, PreferenceManager
                .getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), ""));
    }

}
