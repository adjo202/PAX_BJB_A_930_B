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
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import com.pax.up.bjb.R;

/**
 * 密码管理
 * 
 * @author Sim.G
 * 
 */
public class SettingsPwdManageFragment extends PreferenceFragment {
    public static final int PWD_SYSTEM = 0;
    public static final int PWD_SAFE = 1;
    private Preference sysPwdPreference;
    private Preference safePwdPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_pwd_manager_pref);
        initPrefers();
    }

    private void initPrefers() {
        sysPwdPreference = findPreference("setting_passwd_system");
        safePwdPreference = findPreference("setting_passwd_safe");
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == sysPwdPreference) {
            preference.getExtras().putInt(SettingsPwdManageDetailFragment.ARG_PD_TYPE, PWD_SYSTEM);
        } else if (preference == safePwdPreference) {
            preference.getExtras().putInt(SettingsPwdManageDetailFragment.ARG_PD_TYPE, PWD_SAFE);
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}
