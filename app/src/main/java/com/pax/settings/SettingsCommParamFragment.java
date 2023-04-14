/*
 * ============================================================================
 * COPYRIGHT
 *               Pax CORPORATION PROPRIETARY INFORMATION
 *    This software is supplied under the terms of a license agreement or
 *    nondisclosure agreement with Pax Corporation and may not be copied
 *    or disclosed except in accordance with the terms in that agreement.
 *       Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 *  Module Date: 2017-5-23 10:7
 *  Module Author: liliang
 *  Description:
 *  ============================================================================
 */

package com.pax.settings;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.pax.pay.app.FinancialApplication;
import com.pax.pay.utils.CollectionUtils;
import com.pax.settings.wifi.AccessPoint;
import com.pax.settings.wifi.WifiAdmin;
import com.pax.settings.wifi.WifiAdmin.WifiCipherType;
import com.pax.up.bjb.R;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 通讯参数
 * 
 * @author Sim.G
 * 
 */
public class SettingsCommParamFragment extends PreferenceFragment {
    public static final String TAG = "SettingsCommParamFrag";

    // 设置无密码的wifi热点是否显示
    private boolean dispNoSecurityWifi = true;
    // wifi
    private static final int WIFI_RESCAN_INTERVAL_MS = 10 * 1000;
    private WifiAdmin mWifiAdmin = null;

    private CheckBoxPreference mWifiCheckBox;
    private SettingsCustomDialog mDialog =null;

    private IntentFilter mFilter = null;
    private BroadcastReceiver mReceiver = null;
    private WifiManager mWifiManager;
    private WifiInfo mLastInfo;
    private DetailedState mLastState;
    private Scanner mScanner = null;
    private AtomicBoolean mConnected = new AtomicBoolean(false);
    private boolean wifiConfigIsSave = false;
    private int wifiConfigIndex = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFilter = new IntentFilter();
        mFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mFilter.addAction(WifiManager.NETWORK_IDS_CHANGED_ACTION);
        mFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                handleEvent(intent);
            }
        };
        mScanner = new Scanner();
        mWifiAdmin = WifiAdmin.getInstance(getActivity());
        mWifiManager = (WifiManager) getActivity().getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);

        setupSimplePreferencesScreen();
    }

    @Override

    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(mReceiver, mFilter);
        updateAccessPoints();
    }


    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mReceiver);
        mScanner.pause();
    }

    private void setupSimplePreferencesScreen() {
        addPreferencesFromResource(R.xml.settings_comm_para_pref);
        mWifiCheckBox = (CheckBoxPreference) findPreference(getString(R.string.pref_open_wifi));

        String commType = FinancialApplication.getSysParam().get(SysParam.APP_COMM_TYPE_ACQUIRER);
        if (mWifiCheckBox.isChecked() && commType.equals(SysParam.Constant.COMMTYPE_WIFI)) {
            wifiEnable();
        } else {
            wifiDisable();
        }
        removeAccessPoint();

        bindPreferenceSummaryToValue(findPreference(SysParam.APP_TPDU));
        bindPreferenceSummaryToValue(findPreference(SysParam.APP_COMM_TYPE_ACQUIRER));
        bindPreferenceSummaryToValue(findPreference(SysParam.APP_COMM_TYPE_SSL));
        bindPreferenceSummaryToValue(findPreference(SysParam.COMM_TIMEOUT));

        bindPreferenceSummaryToValue(findPreference(SysParam.PTAG_MODEM_TELNO1));
        bindPreferenceSummaryToValue(findPreference(SysParam.PTAG_MODEM_TELNO2));
        bindPreferenceSummaryToValue(findPreference(SysParam.PTAG_MODEM_TELNO3));
        bindPreferenceSummaryToValue(findPreference(SysParam.PTAG_MODEM_PABX));
        bindPreferenceSummaryToValue(findPreference(SysParam.PTAG_MODEM_PABXDELAY));
        bindPreferenceSummaryToValue(findPreference(SysParam.PTAG_MODEM_DTIMES));
        bindPreferenceSummaryToValue(findPreference(SysParam.PTAG_MODEM_TIMEOUT));
        bindPreferenceSummaryToValue(findPreference(SysParam.PTAG_MODEM_LEVEL));
        bindPreferenceSummaryToValue(findPreference(SysParam.PTAG_MODEM_DP));
        bindPreferenceSummaryToValue(findPreference(SysParam.PTAG_MODEM_DT1));
        bindPreferenceSummaryToValue(findPreference(SysParam.PTAG_MODEM_DT2));
        bindPreferenceSummaryToValue(findPreference(SysParam.PTAG_MODEM_HT));
        bindPreferenceSummaryToValue(findPreference(SysParam.PTAG_MODEM_WT));
        bindPreferenceSummaryToValue(findPreference(SysParam.PTAG_MODEM_SSETUP));
        bindPreferenceSummaryToValue(findPreference(SysParam.PTAG_MODEM_ASMODE));

        bindPreferenceSummaryToValue(findPreference(SysParam.MOBILE_WLTELNO));
        bindPreferenceSummaryToValue(findPreference(SysParam.MOBILE_APN));
        bindPreferenceSummaryToValue(findPreference(SysParam.MOBILE_HOSTIP));
        bindPreferenceSummaryToValue(findPreference(SysParam.MOBILE_HOSTPORT));
        bindPreferenceSummaryToValue(findPreference(SysParam.MOBILE_BAK_HOSTIP));
        bindPreferenceSummaryToValue(findPreference(SysParam.MOBILE_BAK_HOSTPORT));
        bindPreferenceSummaryToValue(findPreference(SysParam.MOBILE_USER));
        bindPreferenceSummaryToValue(findPreference(SysParam.MOBILE_PWD));

        bindPreferenceSummaryToValue(findPreference(SysParam.LAN_HOSTIP));
        bindPreferenceSummaryToValue(findPreference(SysParam.LAN_HOSTPORT));
        bindPreferenceSummaryToValue(findPreference(SysParam.LAN_LOCALIP));
        bindPreferenceSummaryToValue(findPreference(SysParam.LAN_SUBNETMASK));
        bindPreferenceSummaryToValue(findPreference(SysParam.LAN_GATEWAY));
        bindPreferenceSummaryToValue(findPreference(SysParam.LAN_BAK_HOSTIP));
        bindPreferenceSummaryToValue(findPreference(SysParam.LAN_BAK_HOSTPORT));
        bindPreferenceSummaryToValue(findPreference(SysParam.LAN_DNS1));
        bindPreferenceSummaryToValue(findPreference(SysParam.LAN_DNS2));
        bindPreferenceSummaryToValue(findPreference(SysParam.LAN_DHCP));
    }

    private Preference.OnPreferenceChangeListener mBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();
            String prefKey = preference.getKey();

            if (preference instanceof SettingsSingleChoicePreference) {
                SettingsSingleChoicePreference listPreference = (SettingsSingleChoicePreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);
                preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);

                if (prefKey.equals(SysParam.APP_COMM_TYPE_ACQUIRER) && SysParam.Constant
                        .COMMTYPE_WIFI.equals(stringValue)) {
                        mWifiAdmin.openWifi();
                }
            } else {
                boolean flag = false;

                if (prefKey.equals(SysParam.APP_TPDU)) {// TPDU
                    if (stringValue.length() != 10) {
                        flag = true;
                    }
                } else if (prefKey.equals(SysParam.APP_COMM_TYPE_ACQUIRER)) {
                    if (stringValue.equals(SysParam.Constant.COMMTYPE_WIFI)) {
                        mWifiCheckBox.setChecked(true);
                        mWifiAdmin.openWifi();
                    }
                } else if ((prefKey.equals(SysParam.COMM_TIMEOUT))) {
                    int cmpValue = -1;
                    try {
                        cmpValue = Integer.parseInt(stringValue);
                    } catch (NumberFormatException e) {
                        Log.e(TAG, "", e);
                        cmpValue = -1;
                    }
                    if (cmpValue < 1) {
                        flag = true;
                    }
                } else if (prefKey.equals(SysParam.MOBILE_BAK_HOSTIP) || prefKey.equals(SysParam.LAN_LOCALIP)
                        || prefKey.equals(SysParam.LAN_SUBNETMASK) || prefKey.equals(SysParam.LAN_GATEWAY)
                        || prefKey.equals(SysParam.LAN_BAK_HOSTIP)) {// IP
                    if (!checkIp(stringValue)) {
                        flag = true;
                    }
                } else if ((prefKey.equals(SysParam.MOBILE_HOSTPORT))
                        || (prefKey.equals(SysParam.MOBILE_BAK_HOSTPORT)) || (prefKey.equals(SysParam.LAN_HOSTPORT))
                        || (prefKey.equals(SysParam.LAN_BAK_HOSTPORT))) { // PORT
                    if (TextUtils.isEmpty(stringValue)) {
                        flag = true;
                    }
                } else if ((prefKey.equals(SysParam.PTAG_MODEM_TELNO1))
                        || (prefKey.equals(SysParam.PTAG_MODEM_TELNO2))
                        || (prefKey.equals(SysParam.PTAG_MODEM_TELNO3))
                        || (prefKey.equals(SysParam.PTAG_MODEM_PABXDELAY) || (prefKey
                                .equals(SysParam.PTAG_MODEM_TIMEOUT))) || (prefKey.equals(SysParam.PTAG_MODEM_ASMODE))
                        || (prefKey.equals(SysParam.PTAG_MODEM_SSETUP))) {
                    if (TextUtils.isEmpty(stringValue)) {
                        flag = true;
                    }
                } else if (prefKey.equals(SysParam.PTAG_MODEM_LEVEL)) {
                    if (TextUtils.isEmpty(stringValue)) {
                        flag = true;
                    }
                    if (Integer.valueOf(stringValue) < 0 || Integer.valueOf(stringValue) > 15) {
                        flag = true;
                    }
                } else if (prefKey.equals(SysParam.PTAG_MODEM_DT1)) {
                    if (TextUtils.isEmpty(stringValue)) {
                        flag = true;
                    }
                    if (Integer.valueOf(stringValue) < 20 || Integer.valueOf(stringValue) > 255) {
                        flag = true;
                    }
                } else if (prefKey.equals(SysParam.PTAG_MODEM_DT2)) {
                    if (TextUtils.isEmpty(stringValue)) {
                        flag = true;
                    }
                    if (Integer.valueOf(stringValue) < 0 || Integer.valueOf(stringValue) > 255) {
                        flag = true;
                    }
                } else if (prefKey.equals(SysParam.PTAG_MODEM_HT)) {
                    if (TextUtils.isEmpty(stringValue)) {
                        flag = true;
                    }
                    if (Integer.valueOf(stringValue) < 50 || Integer.valueOf(stringValue) > 255) {
                        flag = true;
                    }
                } else if (prefKey.equals(SysParam.PTAG_MODEM_WT)) {
                    if (TextUtils.isEmpty(stringValue)) {
                        flag = true;
                    }
                    if (Integer.valueOf(stringValue) < 5 || Integer.valueOf(stringValue) > 25) {
                        flag = true;
                    }
                } else if (prefKey.equals(SysParam.PTAG_MODEM_PABX)) {
                    if (TextUtils.isEmpty(stringValue)) {
                        flag = true;
                    }
                    if (stringValue.length() > 15) {
                        flag = true;
                    }
                } else if (prefKey.equals(SysParam.PTAG_MODEM_DTIMES)) {
                    if (TextUtils.isEmpty(stringValue)) {
                        flag = true;
                    }
                    if (Integer.valueOf(stringValue) < 1 || Integer.valueOf(stringValue) > 9) {
                        flag = true;
                    }
                }

                if (flag) {
                    Toast.makeText(preference.getContext(), R.string.input_err, Toast.LENGTH_SHORT).show();
                    return false;
                }
                preference.setSummary(stringValue);
            }

            return true;
        }
    };

    private void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(mBindPreferenceSummaryToValueListener);

        if (preference instanceof CheckBoxPreference) {
            mBindPreferenceSummaryToValueListener.onPreferenceChange(preference, PreferenceManager
                    .getDefaultSharedPreferences(preference.getContext()).getBoolean(preference.getKey(), false));
        } else {
            mBindPreferenceSummaryToValueListener.onPreferenceChange(preference, PreferenceManager
                    .getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), ""));
        }
    }

    private boolean checkIp(String ip) {
        String validStr = ".0123456789";
        StringBuilder sb = new StringBuilder();
        int i = 0;
        int dotCount = 0;
        char temp;

        for (i = 0; i < ip.length(); i++) {
            temp = ip.charAt(i);
            if (validStr.indexOf(temp) >= 0) {
                sb.append(temp);
                if (temp == '.') {
                    dotCount++;
                }
            } else {
                dotCount = 0;
                break;
            }
        }
        if ((sb.toString().length() == 0) || (dotCount != 3)) {
            return false;
        }

        String[] arrIp = sb.toString().split("\\.");
        for (i = 0; i < arrIp.length && i < 4; i++) {
            if (TextUtils.isEmpty(arrIp[i])) {
                return false;
            }
            if (Integer.parseInt(arrIp[i]) > 255) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mWifiCheckBox) {
            if (mWifiCheckBox.isChecked()) {
                wifiEnable();
            } else {
                wifiDisable();
            }
        } else if (preference instanceof AccessPoint) {
            AccessPoint selectedAccessPoint = (AccessPoint) preference;
            if (selectedAccessPoint.getSecurity() == AccessPoint.SECURITY_NONE
                    && selectedAccessPoint.getNetworkId() == AccessPoint.INVALID_NETWORK_ID) {
                // 这里是处理无密码的WIFI连接问题
                WifiConfiguration config = mWifiAdmin.createWifiConfiguration(selectedAccessPoint.getSsid(), null,
                        WifiCipherType.WIFICIPHER_NOPASS);
                mWifiAdmin.addNetwork(config);
            } else {
                showConfigUi(selectedAccessPoint);
            }
        } else {
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        return true;
    }

    private void showConfigUi(AccessPoint accessPoint) {
        showDialog(accessPoint);
    }

    private void showDialog(final AccessPoint accessPoint) {
        SettingsCustomDialog.Builder builder = new SettingsCustomDialog.Builder(getActivity());
        View view = View.inflate(getActivity(), R.layout.include_wifi_setting, null);
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        view.setMinimumWidth(getResources().getDimensionPixelOffset(R.dimen.alert_width));

        builder.setTitle(accessPoint.getSsid());

        TextView signalLevel = (TextView) view.findViewById(R.id.signal_level);
        int level = accessPoint.getLevel();
        if (level > 3) {
            signalLevel.setText(getActivity().getResources().getString(R.string.strong).toString());
        } else if (level > 2) {
            signalLevel.setText(getActivity().getResources().getString(R.string.general).toString());
        } else {
            signalLevel.setText(getActivity().getResources().getString(R.string.weak).toString());
        }
        final WifiCipherType mWifiCipherType;
        TextView securityType = (TextView) view.findViewById(R.id.security_type);
        int type = accessPoint.getSecurity();
        if (type == AccessPoint.SECURITY_PSK) {
            securityType.setText("WPA/WPA2 PSK");
            mWifiCipherType = WifiCipherType.WIFICIPHER_WPA;
        } else if (type == AccessPoint.SECURITY_WEP) {
            securityType.setText("WEP");
            mWifiCipherType = WifiCipherType.WIFICIPHER_WEP;
        } else {
            securityType.setText("NONE");
            mWifiCipherType = WifiCipherType.WIFICIPHER_NOPASS;
        }

        final EditText password = (EditText) view.findViewById(R.id.password);
        password.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode != KeyEvent.KEYCODE_ENTER || event.getAction() != KeyEvent.ACTION_UP) {
                    return false;
                }

                if(mDialog !=null&& mDialog.getPositiveButton()!=null){
                    mDialog.getPositiveButton().performClick();
                    return true;
                }

                return false;
            }
        });

        wifiConfigIsSave = false;
        wifiConfigIndex = -1;
        final List<WifiConfiguration> configs = mWifiAdmin.getConfiguration();
        if (configs != null) {
            for (int i = 0; i < configs.size(); i++) {
                String configSsid = configs.get(i).SSID;
                if (configSsid == null)
                    continue;
                configSsid = configSsid.replace("\"", "");
                if (!configSsid.equals(accessPoint.getSsid()))
                    continue;

                wifiConfigIsSave = true;
                wifiConfigIndex = i;
                LinearLayout passwdView = (LinearLayout) view.findViewById(R.id.passwd_view);
                passwdView.setVisibility(View.GONE);

            }
        }

        if (wifiConfigIsSave) {
            builder.setNeutralButton(getString(R.string.set_cancle_save), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mWifiAdmin.removeNetwork(wifiConfigIndex);
                }
            });
        }
        builder.setPositiveButton(getActivity().getResources().getString(R.string.connect).toString(),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean isSuccess;

                        if (wifiConfigIsSave) {
                            isSuccess = mWifiAdmin.connectConfiguration(wifiConfigIndex);
                        } else {
                            WifiConfiguration config = mWifiAdmin.createWifiConfiguration(accessPoint.getSsid(),
                                    password.getText().toString(), mWifiCipherType);

                            isSuccess = mWifiAdmin.addNetwork(config);
                        }

                        if (!isSuccess) {
                            Toast.makeText(getActivity(),
                                    getActivity().getResources().getString(R.string.connect_fail).toString(),
                                    Toast.LENGTH_SHORT).show();
                        }

                        try {
                            Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                            field.setAccessible(true);
                            if (!isSuccess) { // 失败时不关闭对话框
                                field.set(dialog, false);
                            } else { // 成功时关闭对话框
                                field.set(dialog, true);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "", e);
                        }
                    }
                });

        builder.setNegativeButton(getActivity().getResources().getString(R.string.dialog_cancel).toString(),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        try {
                            Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                            field.setAccessible(true);
                            field.set(dialog, true);
                        } catch (Exception e) {
                            Log.e(TAG, "", e);
                        }
                    }
                });

        builder.setView(view);
        mDialog = builder.create();
        mDialog.show();
        mDialog.setCancelable(false);
    }


    private void wifiEnable() {
        mWifiAdmin.openWifi();
    }

    private void wifiDisable() {
        mWifiAdmin.closeWifi();
    }

    private void updateAccessPoints() {
        final int wifiState = mWifiManager.getWifiState();

        switch (wifiState) {
            case WifiManager.WIFI_STATE_ENABLED:
                addMessagePreference(R.string.wifi_started);
                mLastInfo = mWifiManager.getConnectionInfo();
                final Collection<AccessPoint> accessPoints = constructAccessPoints();
                for (AccessPoint accessPoint : accessPoints) {
                    getPreferenceScreen().addPreference(accessPoint);
                }
                break;

            case WifiManager.WIFI_STATE_ENABLING:
                addMessagePreference(R.string.wifi_starting);
                break;

            case WifiManager.WIFI_STATE_DISABLING:
                addMessagePreference(R.string.wifi_stopping);
                break;

            case WifiManager.WIFI_STATE_DISABLED:
                addMessagePreference(R.string.wifi_empty_list_wifi_off);
                break;
            default:
                break;
        }
    }

    private List<AccessPoint> constructAccessPoints() {
        ArrayList<AccessPoint> accessPoints = new ArrayList<>();
        /**
         * Lookup table to more quickly update AccessPoints by only considering objects with the correct SSID. Maps SSID
         * -> List of AccessPoints with the given SSID.
         */
        Multimap<String, AccessPoint> apMap = new Multimap<>();

        final List<WifiConfiguration> configs = mWifiManager.getConfiguredNetworks();
        if (configs != null) {
            for (WifiConfiguration config : configs) {
                AccessPoint accessPoint = new AccessPoint(getActivity(), config);
                if ((accessPoint.getSecurity() == AccessPoint.SECURITY_NONE) && (!dispNoSecurityWifi)) { // 无密码的过滤掉, 不显示
                    continue;
                }
                accessPoint.update(mLastInfo, mLastState);
                accessPoints.add(accessPoint);
                apMap.put(accessPoint.getSsid(), accessPoint);
            }
        }

        final List<ScanResult> results = mWifiManager.getScanResults();
        if (results != null) {
            for (ScanResult result : results) {
                // Ignore hidden and ad-hoc networks.
                if (result.SSID == null || result.SSID.length() == 0 || result.capabilities.contains("[IBSS]")) {
                    continue;
                }

                boolean found = false;
                for (AccessPoint accessPoint : apMap.getAll(result.SSID)) {
                    if (accessPoint.update(result))
                        found = true;
                }
                if (!found) {
                    AccessPoint accessPoint = new AccessPoint(getActivity(), result);
                    // 过滤掉无密码的
                    if ((accessPoint.getSecurity() == AccessPoint.SECURITY_NONE) && (!dispNoSecurityWifi)) {
                        continue;
                    }
                    accessPoints.add(accessPoint);
                    apMap.put(accessPoint.getSsid(), accessPoint);
                }
            }
        }

        // Pre-sort accessPoints to speed preference insertion
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
        if (!CollectionUtils.isEmpty(accessPoints))
            Collections.sort(accessPoints);
        return accessPoints;
    }

    private class Multimap<K, V> {
        private HashMap<K, List<V>> store = new HashMap<>();

        /** retrieve a non-null list of values with key K */
        List<V> getAll(K key) {
            List<V> values = store.get(key);
            return values != null ? values : Collections.<V> emptyList();
        }

        void put(K key, V val) {
            List<V> curVals = store.get(key);
            if (curVals == null) {
                curVals = new ArrayList<>(3);
                store.put(key, curVals);
            }
            curVals.add(val);
        }
    }

    private void addMessagePreference(int messageId) {
        mWifiCheckBox.setSummary(messageId);
        removeAccessPoint();
    }

    private void removeAccessPoint() {
        for (int i = getPreferenceScreen().getPreferenceCount() - 1; i >= 0; --i) {
            // Maybe there's a WifiConfigPreference
            Preference preference = getPreferenceScreen().getPreference(i);
            if (preference instanceof AccessPoint) {
                getPreferenceScreen().removePreference(preference);
            }
        }
    }

    private void handleEvent(Intent intent) {
        String action = intent.getAction();
        if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
            updateWifiState(intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN));
        } else if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
            updateAccessPoints();
        } else if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(action)) {
            // Ignore supplicant state changes when network is connected
            // we should deprecate SUPPLICANT_STATE_CHANGED_ACTION and
            // introduce a broadcast that combines the supplicant and network
            // network state change events so the apps dont have to worry about
            // ignoring supplicant state change when network is connected
            // to get more fine grained information.
            if (!mConnected.get()) {
                updateConnectionState(WifiInfo.getDetailedStateOf((SupplicantState) intent
                        .getParcelableExtra(WifiManager.EXTRA_NEW_STATE)));
            }
        } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
            NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            mConnected.set(info.isConnected());
            updateAccessPoints();
            updateConnectionState(info.getDetailedState());
        } else if (WifiManager.RSSI_CHANGED_ACTION.equals(action)) {
            updateConnectionState(null);
        }
    }

    private void updateConnectionState(DetailedState state) {
        /* sticky broadcasts can call this when wifi is disabled */
        if (!mWifiManager.isWifiEnabled()) {
            mScanner.pause();
            return;
        }

        if (state == DetailedState.OBTAINING_IPADDR) {
            mScanner.pause();
        } else {
            mScanner.resume();
        }

        mLastInfo = mWifiManager.getConnectionInfo();
        if (state != null) {
            mLastState = state;
        }

        for (int i = getPreferenceScreen().getPreferenceCount() - 1; i >= 0; --i) {
            // Maybe there's a WifiConfigPreference
            Preference preference = getPreferenceScreen().getPreference(i);
            if (preference instanceof AccessPoint) {
                final AccessPoint accessPoint = (AccessPoint) preference;
                accessPoint.update(mLastInfo, mLastState);
            }
        }
    }

    private void updateWifiState(int state) {
        getActivity().invalidateOptionsMenu();

        switch (state) {
            case WifiManager.WIFI_STATE_ENABLED:
                mScanner.resume();
                return; // not break, to avoid the call to pause() below

            case WifiManager.WIFI_STATE_ENABLING:
                addMessagePreference(R.string.wifi_starting);
                break;

            case WifiManager.WIFI_STATE_DISABLED:
                addMessagePreference(R.string.wifi_empty_list_wifi_off);
                break;
            default:
                break;
        }

        mLastInfo = null;
        mLastState = null;
        mScanner.pause();
    }

    @SuppressLint("HandlerLeak")
    private class Scanner extends Handler {
        private int mRetry = 0;

        void resume() {
            if (!hasMessages(0)) {
                sendEmptyMessage(0);
            }
        }

        @SuppressWarnings("unused")
        void forceScan() {
            removeMessages(0);
            sendEmptyMessage(0);
        }

        void pause() {
            mRetry = 0;
            removeMessages(0);
        }

        @Override
        public void handleMessage(Message message) {
            if (mWifiAdmin.startScan()) {
                mRetry = 0;
            } else if (++mRetry >= 3) {
                mRetry = 0;
                Toast.makeText(getActivity(), R.string.wifi_fail_to_scan, Toast.LENGTH_LONG).show();
                return;
            }
            sendEmptyMessageDelayed(0, WIFI_RESCAN_INTERVAL_MS);
        }
    }
}