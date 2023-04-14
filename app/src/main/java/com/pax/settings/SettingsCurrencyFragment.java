/*******************************************************************************
 * ============================================================================
 * COPYRIGHT
 *               Pax CORPORATION PROPRIETARY INFORMATION
 *    This software is supplied under the terms of a license agreement or
 *    nondisclosure agreement with Pax Corporation and may not be copied
 *    or disclosed except in accordance with the terms in that agreement.
 *       Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 *  Module Date: 17-5-22 11:16
 *  Module Author: liliang
 *  Description:
 *  ============================================================================
 ******************************************************************************/
package com.pax.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.pax.pay.trans.model.TransData;
import com.pax.up.bjb.R;
import com.pax.view.dialog.CustomAlertDialog;


/**
 * Created by liliang on 2017/4/10.
 */

public class SettingsCurrencyFragment extends PreferenceFragment implements SharedPreferences
        .OnSharedPreferenceChangeListener, Preference.OnPreferenceChangeListener{

    private Preference mPrefCurrencySys;//Preference for predefined currencies.
    private CheckBoxPreference mPrefUseCustomCurrency;//Preference for using custom currency or not.
    private Preference mPrefCurrencyCustomName;//Preference for custom currency name.
    private Preference mPrefCurrencyCustomCode;//Preference for custom currency code.
    private Preference mPrefCurrencyCustomDecimals;//Preference for currency decimals.

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupSimplePreferencesScreen();
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    private void setupSimplePreferencesScreen() {
        addPreferencesFromResource(R.xml.settings_currency_manage_pref);
        mPrefCurrencySys = findPreference(SysParam.CURRENCY_SYS);
        bindPreferenceSummaryToValue(mPrefCurrencySys);
        mPrefUseCustomCurrency = (CheckBoxPreference) findPreference(SysParam.CURRENCY_USE_CUSTOM);
        bindPreferenceSummaryToValue(mPrefUseCustomCurrency);
        mPrefCurrencyCustomName = findPreference(SysParam.CURRENCY_CUSTOM_NAME);
        bindPreferenceSummaryToValue(mPrefCurrencyCustomName);
        mPrefCurrencyCustomCode = findPreference(SysParam.CURRENCY_CUSTOM_CODE);
        bindPreferenceSummaryToValue(mPrefCurrencyCustomCode);
        mPrefCurrencyCustomDecimals = findPreference(SysParam.CURRENCY_CUSTOM_DECIMALS);
        bindPreferenceSummaryToValue(mPrefCurrencyCustomDecimals);
    }

    private void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(this);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences
                (preference.getContext());
        if (preference == mPrefCurrencySys)  {
            SettingsSingleChoicePreference listPreference =
                    (SettingsSingleChoicePreference) preference;
            int index = listPreference.findIndexOfValue(listPreference.getValue());
            preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
        } else if (preference != mPrefUseCustomCurrency) {
            String summary = sharedPref.getString(preference.getKey(), null);
            preference.setSummary(summary);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(SysParam.CURRENCY_SYS)) {
            //When a predefined currency is selected, uncheck preference for custom currency.
            mPrefUseCustomCurrency.setChecked(false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean flag = true;//Indicate if there is input error.
        String stringValue = newValue.toString();

        if (TransData.getTransCount() > 0) {//Settlement must be done before changing the currency.
            showSettleDialog();
            return false;
        }

        if (preference == mPrefCurrencySys)  {
            SettingsSingleChoicePreference listPreference = (SettingsSingleChoicePreference) preference;
            int index = listPreference.findIndexOfValue(stringValue);
            preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
            return true;
        }

        if (preference == mPrefCurrencyCustomCode) {
            if (stringValue.length() < 3) {
                //Currency code length must be 3
                flag = false;
            }
        }

        if (preference == mPrefCurrencyCustomDecimals) {
            int decimals = -1;
            try {
                decimals = Integer.valueOf(stringValue);
            } catch (NumberFormatException e) {
                Log.e("LoadRate", "", e);
            }

            if (decimals < 0 || decimals > 3) {
                //Decimals should be 0-3.
                flag = false;
            }
        }

        if (flag) {
            preference.setSummary(stringValue);
        } else {
            Toast.makeText(preference.getContext(), R.string.input_err, Toast.LENGTH_SHORT).show();
        }

        return flag;
    }

    /**
     * Show settlement indication.
     */
    private void showSettleDialog() {
        CustomAlertDialog dialog = new CustomAlertDialog(getActivity(),
                CustomAlertDialog.ERROR_TYPE);
        dialog.show();
        dialog.setContentText(getString(R.string.currency_change_indication));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.showConfirmButton(true);
        dialog.setConfirmClickListener(new CustomAlertDialog.OnCustomClickListener() {
            @Override
            public void onClick(CustomAlertDialog alertDialog) {
                alertDialog.dismiss();
            }
        });
        dialog.show();
    }
}
