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

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.pax.device.Device;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.trans.model.Controller;
import com.pax.pay.trans.model.TransData;
import com.pax.up.bjb.R;
import com.pax.view.dialog.CustomAlertDialog;
import com.pax.view.dialog.CustomAlertDialog.OnCustomClickListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * 商户参数
 *
 * @author Sim.G
 */
public class SettingsMerchantParamFragment extends PreferenceFragment {
    private Preference mPrefMerchId;
    private Preference mPrefTerminalId;
    private Preference mPrefMerchEN;
    //<!--disable request dari indopay doc. UAT 30062021-->
    //private Preference mPrefAcqCode;
    //private Preference mPrefTerminalTime;
    //private Preference mPrefMerchCN;
    private static String newValue = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupSimplePreferencesScreen();
    }

    private void setupSimplePreferencesScreen() {
        addPreferencesFromResource(R.xml.settings_merchant_para_pref);

        mPrefMerchId = findPreference(SysParam.MERCH_ID);
        bindPreferenceSummaryToValue(mPrefMerchId);

        mPrefTerminalId = findPreference(SysParam.TERMINAL_ID);
        bindPreferenceSummaryToValue(mPrefTerminalId);

        /*mPrefAcqCode = findPreference(SysParam.ACQUIRER);
        bindPreferenceSummaryToValue(mPrefAcqCode);*/

        mPrefMerchEN = findPreference(SysParam.MERCH_EN);
        bindPreferenceSummaryToValue(mPrefMerchEN);

        /*mPrefMerchCN = findPreference(SysParam.MERCH_CN);
        bindPreferenceSummaryToValue(mPrefMerchCN);*/

        /*mPrefTerminalTime = findPreference(SysParam.TERMINAL_DATE_TIME);
        bindPreferenceSummaryToValue(mPrefTerminalTime);*/

        initPrefSummary();
    }

    private void initPrefSummary() {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getActivity().getApplicationContext());

        String merchId = sharedPreferences.getString(SysParam.MERCH_ID, null);
        mPrefMerchId.setSummary(merchId);


        String terminalId = sharedPreferences.getString(SysParam.TERMINAL_ID, null);
        mPrefTerminalId.setSummary(terminalId);

        /*String acqCode = sharedPreferences.getString(SysParam.ACQUIRER, null);
        mPrefAcqCode.setSummary(acqCode);*/

        String merchEN = sharedPreferences.getString(SysParam.MERCH_EN, null);
        mPrefMerchEN.setSummary(merchEN);

        /*String merchCN = sharedPreferences.getString(SysParam.MERCH_CN, null);
        mPrefMerchCN.setSummary(merchCN);*/
    }

    private Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new
            Preference.OnPreferenceChangeListener() {
        @SuppressLint("SimpleDateFormat")
        @Override
        public boolean onPreferenceChange(final Preference preference, Object value) {
            String stringValue = value.toString();
            newValue = stringValue;// 保存值

            //disable
            /*if (preference == mPrefTerminalTime) {
                // 设置终端日期和时间
                SharedPreferences sharedPreferences = PreferenceManager
                        .getDefaultSharedPreferences(getActivity().getApplicationContext());

                if ((stringValue.length() != 12)) {
                    Toast.makeText(preference.getContext(), R.string.input_len_err, Toast
                            .LENGTH_SHORT).show();
                } else if (isValidDate(stringValue)) {
                    FinancialApplication.getDal().getSys().setDate(stringValue);
                    Device.beepOk();
                } else {
                    Toast.makeText(preference.getContext(), R.string.input_err, Toast
                            .LENGTH_SHORT).show();
                }

                Editor edit = sharedPreferences.edit();
                edit.remove(SysParam.TERMINAL_DATE_TIME);
                edit.commit();
                return false;
            }*/

            int maxLen = 0;
            boolean flag = false;

            if (preference == mPrefMerchId) {
                maxLen = 15;
                flag = true;
            } else if (preference == mPrefTerminalId) {
                maxLen = 8;
                flag = true;
            } /*else if (preference == mPrefAcqCode) {
                maxLen = 8;
                flag = true;
            }*/

            Log.d("teg", "onPreferenceChange");

            // 设置商户号，终端号，要判断是否有交易记录和输安全密码
            if (flag) {
                final String oldValue = ((SettingsEditTextPreference) preference).getText();
                // 先检测输入数据长度的有效性
                if (stringValue.length() != maxLen) {
                    Toast.makeText(preference.getContext(), R.string.input_len_err, Toast
                            .LENGTH_SHORT).show();
                    return false;
                }
                // 如果未修改按确定则直接退出
                if (stringValue.equals(PreferenceManager.getDefaultSharedPreferences(preference
                        .getContext())
                        .getString(preference.getKey(), null))) {
                    return false;
                }

                // 判断是否 有交易记录
                if (TransData.getTransCount() != 0) {
                    CustomAlertDialog dialog = new CustomAlertDialog(preference.getContext(),
                            CustomAlertDialog.ERROR_TYPE, 3);
                    dialog.setTitleText(preference.getContext().getString(R.string.set_prompt));
                    dialog.setContentText(preference.getContext().getString(R.string.set_settle));
                    dialog.setCanceledOnTouchOutside(true);
                    dialog.show();
                    Device.beepErr();
                    return false;
                }

                // 输入安全密码
                final String passwd = FinancialApplication.getSysParam().get(SysParam.SEC_SECPWD);

                final CustomAlertDialog dialog = new CustomAlertDialog(preference.getContext(),
                        CustomAlertDialog.CUSTOM_ENTER_TYPE);
                dialog.show();
                dialog.showCancelButton(true);
                dialog.showConfirmButton(true);
                dialog.setTitleText(preference.getContext().getString(R.string.set_print_password));
                dialog.setCancelClickListener(new OnCustomClickListener() {

                    @Override
                    public void onClick(CustomAlertDialog alertDialog) {
                        ((SettingsEditTextPreference) preference).setText(oldValue);
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                });

                dialog.setConfirmClickListener(new OnCustomClickListener() {

                    @Override
                    public void onClick(CustomAlertDialog alertDialog) {
                        String input = dialog.getContentEditText();
                        //sandy
                        //Toast.makeText(preference.getContext(), passwd, Toast.LENGTH_SHORT).show();

                        if (input.equals(passwd)) {
                            // 更新summary ,并保存
                            preference.setSummary(newValue);
                            FinancialApplication.getSysParam().set(preference.getKey(), newValue);
                            FinancialApplication.getController().set(Controller.NEED_DOWN_CAPK,
                                    Controller.Constant.YES);
                            FinancialApplication.getController().set(Controller.NEED_DOWN_AID,
                                    Controller.Constant.YES);
                            FinancialApplication.getController().set(Controller.NEED_DOWN_BLACK,
                                    Controller.Constant.YES);
                            FinancialApplication.getController().set(Controller.POS_LOGON_STATUS,
                                    Controller.Constant.NO);
                            FinancialApplication.getController().set(Controller.OPERATOR_LOGON_STATUS,
                                    Controller.Constant.NO);
                            dialog.dismiss();
                        } else {
                            dialog.setErrTipsText(preference.getContext().getString(R.string.set_err_password));
                        }
                    }
                });
            }else {
                preference.setSummary(stringValue);
            }


            return true;
        }
    };


    @SuppressLint("SimpleDateFormat")
    private static boolean isValidDate(String str) {
        boolean convertSuccess = true;
        SimpleDateFormat format = new SimpleDateFormat("yyMMddHHmmss");
        try {
            format.setLenient(false);
            format.parse(str);
        } catch (ParseException e) {
            convertSuccess = false;
        }
        return convertSuccess;
    }

    private void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
    }

}
