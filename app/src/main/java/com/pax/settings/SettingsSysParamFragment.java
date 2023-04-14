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
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.Toast;

import com.pax.dal.entity.EUartPort;
import com.pax.device.Device;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.trans.model.Controller;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.utils.CollectionUtils;
import com.pax.up.bjb.R;
import com.pax.view.dialog.CustomAlertDialog;

import java.util.Arrays;
import java.util.List;

/**
 * 系统参数
 * 
 * @author Sim.G
 * 
 */
public class SettingsSysParamFragment extends PreferenceFragment {
    public static final String TAG = "SettingsSysParamFragmen";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupSimplePreferencesScreen();
    }

    private void setupSimplePreferencesScreen() {
        addPreferencesFromResource(R.xml.settings_system_para_pref);
        CheckBoxPreference prefer = (CheckBoxPreference) findPreference(SysParam.SUPPORT_EXTERNAL_SCANNER);
        Preference p = findPreference(SysParam.INTERNAL_SCANNER);
        if (hasPinPad()) {
            prefer.setEnabled(true);

            if (prefer.isChecked()) {
                p.setEnabled(false);
            } else {
                p.setEnabled(true);
            }
        } else {
            prefer.setEnabled(false);
            p.setEnabled(false);
        }

        bindPreferenceSummaryToValue(findPreference(SysParam.TRANS_NO));
        bindPreferenceSummaryToValue(findPreference(SysParam.BATCH_NO));
        bindPreferenceSummaryToValue(findPreference(SysParam.PRINT_VOUCHER_NUM));

        bindPreferenceSummaryToValue(findPreference(SysParam.RESEND_TIMES));
        bindPreferenceSummaryToValue(findPreference(SysParam.RESEND_SIG_TIMES));
        bindPreferenceSummaryToValue(findPreference(SysParam.REVERSL_CTRL));
        bindPreferenceSummaryToValue(findPreference(SysParam.MAX_TRANS_COUNT));
        bindPreferenceSummaryToValue(findPreference(SysParam.TIP_RATE));
        bindPreferenceSummaryToValue(findPreference(SysParam.PRINT_GRAY));

        bindPreferenceSummaryToValue(findPreference(SysParam.EX_CONTACTLESS_CHOOSE));
        bindPreferenceSummaryToValue(findPreference(SysParam.EX_CONTACTLESS_SERIAL));
        bindPreferenceSummaryToValue(findPreference(SysParam.EX_ONTACTLESS_BAUD_RANT));

        bindPreferenceSummaryToValue(findPreference(SysParam.EXTERNAL_SCANNER));
        bindPreferenceSummaryToValue(findPreference(SysParam.INTERNAL_SCANNER));
        bindPreferenceSummaryToValue(findPreference(SysParam.SIGNATURE_SELECTOR));

        // 暂时不支持外置非接功能，，故先屏蔽掉
        getPreferenceScreen().removePreference(findPreference(SysParam.EX_CONTACTLESS_SET));
        getPreferenceScreen().removePreference(findPreference(SysParam.EX_CONTACTLESS_CHOOSE));
        getPreferenceScreen().removePreference(findPreference(SysParam.EX_CONTACTLESS_SERIAL));
        getPreferenceScreen().removePreference(findPreference(SysParam.EX_ONTACTLESS_BAUD_RANT));

    }

    private Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            String prefKey = preference.getKey();
            boolean flag = false;
            boolean hasTrans = false;

            if (preference instanceof SettingsSingleChoicePreference) {
                stringValue = value.toString();
                SettingsSingleChoicePreference listPreference = (SettingsSingleChoicePreference) preference;
                if (listPreference.getKey().equals(SysParam.SIGNATURE_SELECTOR)) {
                    String[] signature = initSignature();
                    if (signature != null && !Arrays.asList(signature).contains(stringValue)) {
                        preference.setSummary(signature[0]);
                        FinancialApplication.getSysParam().set(SysParam.SIGNATURE_SELECTOR, signature[0]);
                        return true;
                    }
                }

                int index = listPreference.findIndexOfValue(stringValue);
                preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
            }

            if (prefKey.equals(SysParam.EXTERNAL_SCANNER)
                    || prefKey.equals(SysParam.INTERNAL_SCANNER)) {
                SettingsSingleChoicePreference listPreference = (SettingsSingleChoicePreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);
                stringValue = (String) (index >= 0 ? listPreference.getEntries()[index] : null);
            } else if ((prefKey.equals(SysParam.TRANS_NO))) {
                // 当前流水号
                if (stringValue.length() > 6) {
                    flag = true;
                }

                // 有交易流水，流水号不得小于当前流水号
                if (TransData.getTransCount() != 0
                        && Long.parseLong(stringValue) < Long.parseLong(PreferenceManager.getDefaultSharedPreferences(
                        preference.getContext()).getString(preference.getKey(), null))) {
                    flag = true;
                }
            } else if ((prefKey.equals(SysParam.BATCH_NO))) {
                // 当前批次号
                if (stringValue.compareTo("000001") < 0) {
                    flag = true;
                }
                // 有交易流水，不允许修改当前批次号
                if (TransData.getTransCount() != 0) {
                    if (stringValue.equals(PreferenceManager.getDefaultSharedPreferences(preference.getContext())
                            .getString(preference.getKey(), null))) {
                        hasTrans = false;
                    } else {
                        hasTrans = true;
                    }
                }
            } else if ((prefKey.equals(SysParam.REVERSL_CTRL)) || prefKey.equals(SysParam.RESEND_SIG_TIMES)
                    || prefKey.equals(SysParam.RESEND_TIMES)) {
                // "冲正重发次数"
                if ((stringValue.length() != 1) || (stringValue.compareTo("1") < 0) || stringValue.compareTo("3") > 0) {
                    flag = true;
                }
            } else if (prefKey.equals(SysParam.PRINT_VOUCHER_NUM)) {
                // "打印张数设置"
                if ((stringValue.length() != 1) || (stringValue.compareTo("1") < 0) || stringValue.compareTo("3") > 0) {
                    flag = true;
                }
            } else if (prefKey.equals(SysParam.MAX_TRANS_COUNT)) {
                // "最大交易笔数"
                int cmpValue = -1;
                try {
                    cmpValue = Integer.parseInt(stringValue);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "", e);
                    cmpValue = -1;
                }
                //当前笔数大于设置笔数
                long transCount = TransData.getTransCount();
                if ((cmpValue < 1) || (cmpValue > 500)||(cmpValue < transCount)) {
                    flag = true;
                }
            } else if (prefKey.equals(SysParam.TIP_RATE)) {
                // "小费比例"
                int cmpValue = -1;
                try {
                    cmpValue = Integer.parseInt(stringValue);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "", e);
                    cmpValue = -1;
                }
                if (cmpValue == -1) {
                    flag = true;
                }
            } else if (prefKey.equals(SysParam.PRINT_GRAY)) {
                // "打印灰度"
                int cmpValue = -1;
                try {
                    cmpValue = Integer.parseInt(stringValue);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "", e);
                    cmpValue = -1;
                }
                if (cmpValue < 50 || cmpValue > 500) {
                    flag = true;
                }
            }

            if (flag) {
                Toast.makeText(preference.getContext(), R.string.input_err, Toast.LENGTH_SHORT).show();
                return false;
            }
            if (hasTrans) {
                CustomAlertDialog dialog = new CustomAlertDialog(preference.getContext(), CustomAlertDialog.ERROR_TYPE,
                        3);
                dialog.setTitleText(preference.getContext().getString(R.string.set_prompt));
                dialog.setContentText(preference.getContext().getString(R.string.set_settle));
                dialog.setCanceledOnTouchOutside(true);
                dialog.show();
                Device.beepErr();
                return false;
            }
            preference.setSummary(stringValue);
            return true;
        }
    };

    private void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, PreferenceManager
                .getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), ""));
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == findPreference(SysParam.SUPPORT_SM)
                || preference == findPreference(SysParam.SUPPORT_SM_PERIOD_2)) {
            if (TransData.getTransCount() != 0) {
                ((CheckBoxPreference) preference).setChecked(!((CheckBoxPreference) preference).isChecked());
                CustomAlertDialog dialog = new CustomAlertDialog(preference.getContext(), CustomAlertDialog.ERROR_TYPE,
                        3);
                dialog.setTitleText(getString(R.string.systempara_dialog_title));
                dialog.setContentText(getString(R.string.systempara_dialog_content));
                dialog.setCanceledOnTouchOutside(true);
                dialog.show();
                Device.beepErr();
                return false;
            }
            FinancialApplication.getController().set(Controller.POS_LOGON_STATUS, Controller.Constant.NO);
            FinancialApplication.getController().set(Controller.NEED_DOWN_CAPK, Controller.Constant.YES);
            FinancialApplication.getController().set(Controller.NEED_DOWN_AID, Controller.Constant.YES);
        } else if (preference == findPreference(SysParam.SUPPORT_EXTERNAL_SCANNER)) {
            Preference p = findPreference(SysParam.INTERNAL_SCANNER);
            if (((CheckBoxPreference) preference).isChecked()) {
                p.setEnabled(false);
            } else {
                p.setEnabled(true);
            }
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private String[] initSignature() {
        SettingsSingleChoicePreference posComList = (SettingsSingleChoicePreference) findPreference(SysParam.SIGNATURE_SELECTOR);
        if (posComList == null) {
            return null;
        }
        String[] v;
        if (hasPinPad()) {
            v = getResources().getStringArray(R.array.systempara_menu_signature_entries);
        } else {
            v = new String[] { getResources().getStringArray(R.array.systempara_menu_signature_entries)[0] };

        }
        posComList.setEntries(v);
        posComList.setEntryValues(v);
        posComList.setDefaultValue(v[0]);
        return v;
    }

    private boolean hasPinPad() {
        List<EUartPort> list = FinancialApplication.getDal().getCommManager().getUartPortList();
        if (CollectionUtils.isEmpty(list)) {
            return false;
        }

        for (EUartPort port : list) {
            if ("PINPAD".equals(port.name())) {
                return true;
            }
        }
        return false;
    }
}
