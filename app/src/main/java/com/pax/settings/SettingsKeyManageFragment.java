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

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.pax.dal.entity.EUartPort;
import com.pax.device.Device;
import com.pax.gl.convert.IConvert.EPaddingPosition;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.constant.Constants;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.transmit.ModemCommunicate;
import com.pax.pay.trans.transmit.TransOnline;
import com.pax.pay.trans.transmit.TransProcessListenerImpl;
import com.pax.pay.utils.CollectionUtils;
import com.pax.up.bjb.R;

import java.util.Arrays;
import java.util.List;

/**
 * 密钥管理
 * 
 * @author Sim.G
 * 
 */
public class SettingsKeyManageFragment extends PreferenceFragment {
    public static final String TAG = "SettingsKeyManageFragment";
    private static final int MAX_CNT = 7;
    private  Activity activity;
    private int cnt = 0;

    private Handler handler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        handler = new Handler();
        setupSimplePreferencesScreen();
    }

    private void setupSimplePreferencesScreen() {
        addPreferencesFromResource(R.xml.settings_key_manage_pref);

        bindPreferenceSummaryToValue(findPreference(SysParam.KEY_ALGORITHM));
        bindPreferenceSummaryToValue(findPreference(SysParam.EX_PINPAD));
        bindPreferenceSummaryToValue(findPreference(SysParam.INTERNAL_PED_UI_STYLE));

        bindPreferenceSummaryToValue(findPreference(SysParam.MK_INDEX));
        bindPreferenceSummaryToValue(findPreference(SysParam.INPUT_KEY_MANUALLY));

        /*adde by Wangyb at 20170331*/
        Preference preference = findPreference(SysParam.MK_DOWNLOAD);

        //设置Preference的点击事件监听
        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener
                () {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        TransProcessListenerImpl listenerImpl = new TransProcessListenerImpl(getActivity());
                        int ret = TransOnline.downloadTmk(listenerImpl);
                        if (listenerImpl != null) {
                            listenerImpl.onHideProgress();
                        }

                        if (ret == TransResult.SUCC) {
                            Device.beepOk();
                        } else if (ret != TransResult.ERR_ABORTED
                                && ret != TransResult.ERR_HOST_REJECT && listenerImpl != null) {
                            //listenerImpl.onShowErrMessageWithConfirm(TransResult.getMessage(getActivity(), ret), Constants.FAILED_DIALOG_SHOW_TIME);
                        }
                        exit();
                    }
                }).start();

                return true;
            }
        });

    }

    /*added by BrianWang at 20170401*/
    private void exit() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                ModemCommunicate.getInstance().onClose();
            }
        });
    }

    private String[] initExPinPadDisp() {
        SettingsSingleChoicePreference posComList = (SettingsSingleChoicePreference) findPreference(SysParam.EX_PINPAD);
        if (posComList == null) {
            return null;
        }
        List<EUartPort> list = FinancialApplication.getDal().getCommManager().getUartPortList();
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }

        boolean hasPinPad = false;
        for (EUartPort port : list) {
            if ("PINPAD".equals(port.name())) {
                hasPinPad = true;
            }
        }

        String[] v;
        if (hasPinPad) {
            v = getResources().getStringArray(R.array.keymanage_menu_pwd_pad_list_entries);
        } else {
            v = new String[] { getResources().getStringArray(R.array.keymanage_menu_pwd_pad_list_entries)[0] };

        }
        posComList.setEntries(v);
        posComList.setEntryValues(v);
        posComList.setDefaultValue(v[0]);
        return v;
    }

    private Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();
            cnt++;
            String key = preference.getKey();
            if (preference instanceof SettingsSingleChoicePreference) {
                stringValue = value.toString();
                SettingsSingleChoicePreference listPreference = (SettingsSingleChoicePreference) preference;
                if (listPreference.getKey().equals(SysParam.EX_PINPAD)) {
                    String[] com = initExPinPadDisp();
                    if (com != null && !Arrays.asList(com).contains(stringValue)) {
                        preference.setSummary(com[0]);
                        FinancialApplication.getSysParam().set(SysParam.EX_PINPAD, com[0]);
                        return true;
                    }
                }

                int index = listPreference.findIndexOfValue(stringValue);
                preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);

            } else if (SysParam.INPUT_KEY_MANUALLY.equals(key)) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
                String index = sharedPreferences.getString(SysParam.MK_INDEX, null);
                if (cnt >= MAX_CNT) { // 由于隐藏了设置TMS主控密钥值,此处暂不加1
                    if (index == null || index.length() == 0) {
                        Toast.makeText(preference.getContext(), R.string.keymanage_menu_tmk_index_err,
                                Toast.LENGTH_SHORT).show();
                    } else if ((Integer.parseInt(index) < 0) || (Integer.parseInt(index) > 49)) {
                        Toast.makeText(preference.getContext(), R.string.keymanage_menu_tmk_index_no,
                                Toast.LENGTH_SHORT).show();
                    } else if ((stringValue.length() != 32) && (stringValue.length() != 48)) {
                        // 目前只支持3des,所以对于密钥长度做了限制，如果后续添加des,需要增加16位长度的限制
                        Toast.makeText(preference.getContext(), R.string.input_len_err, Toast.LENGTH_SHORT).show();
                    } else {
                        // 写主密钥
                        Device.writeTMK((byte) Integer.parseInt(index),
                                FinancialApplication.getConvert().strToBcd(stringValue, EPaddingPosition.PADDING_LEFT));
                        Device.beepOk();
                    }
                }

                Editor edit = sharedPreferences.edit();
                edit.remove(SysParam.INPUT_KEY_MANUALLY);
                edit.commit();
                return false;
            } else if (key.equals(SysParam.MK_INDEX)) {
                boolean flag = false;
                if (Integer.valueOf(TextUtils.isEmpty(stringValue) ? "0" : stringValue) < 0
                        || Integer.valueOf(TextUtils.isEmpty(stringValue) ? "0" : stringValue) >49) {
                    flag = true;
                } else {
                    preference.setSummary(value.toString());
                    FinancialApplication.getSysParam().set(SysParam.MK_INDEX, value.toString());
                }

                if (flag) {
                    Toast.makeText(preference.getContext(), R.string.input_err, Toast.LENGTH_SHORT).show();
                    return false;
                }

            } else {
                preference.setSummary(value.toString());
            }

            return true;
        }
    };

    private void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, PreferenceManager
                .getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), ""));
    }

    @Override
    public void onResume() {
        super.onResume();
        cnt = MAX_CNT;
    }

    @Override
    public void onPause() {
        super.onPause();
        cnt = 0;
    }
}