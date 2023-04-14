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

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.pax.up.bjb.R;

public class SettingsPwdManageDetailFragment extends Fragment {

    public static final String ARG_PD_TYPE = "arg_pwd_type";

    private EditText mOldPwd;
    private EditText mNewPwd;
    private EditText mConfirmPwd;
    private int titleId = -1;
    private int len = 8; // 密码长度
    private String pdKey = null;
    private String pdValue = null;

    private OnClickListener mIconClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!checkError()) {
                return;
            }

            savePasswd();
            getActivity().onBackPressed();
            Toast.makeText(getActivity(), R.string.password_modify_success, Toast
                    .LENGTH_SHORT).show();
        }
    };

    private boolean checkError() {
        boolean flag = false;
        // 比较原密码，两次新密码
        if (TextUtils.isEmpty(pdValue)) {
            Toast.makeText(getActivity(), R.string.input_old_password, Toast.LENGTH_SHORT).show();
        } else if (!pdValue.equals(mOldPwd.getText().toString())) {
            Toast.makeText(getActivity(), R.string.error_old_password, Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(mNewPwd.getText())) {
            Toast.makeText(getActivity(), R.string.input_new_password, Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(mConfirmPwd.getText())) {
            Toast.makeText(getActivity(), R.string.input_again_new_password, Toast.LENGTH_SHORT)
                    .show();
        } else if ((mNewPwd.length() != len)) {
            Toast.makeText(getActivity(), R.string.error_input_length, Toast.LENGTH_SHORT).show();
        } else if (!mNewPwd.getText().toString().equals(mConfirmPwd.getText().toString())) {
            Toast.makeText(getActivity(), R.string.error_password_no_same, Toast.LENGTH_SHORT)
                    .show();
        } else {
            flag = true;
        }
        return flag;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        getPasswdParam();
        View rootView = inflater.inflate(R.layout.settings_sys_pwd_manage_layout, container, false);
        TextView title = (TextView) rootView.findViewById(R.id.set_para1_title);
        title.setText(titleId);

        mOldPwd = (EditText) rootView.findViewById(R.id.input_old_password_text);
        mNewPwd = (EditText) rootView.findViewById(R.id.input_new_password_text);
        mConfirmPwd = (EditText) rootView.findViewById(R.id.input_again_new_password_text);

        mOldPwd.setFilters(new InputFilter[]{new InputFilter.LengthFilter(len)});
        mNewPwd.setFilters(new InputFilter[]{new InputFilter.LengthFilter(len)});
        mConfirmPwd.setFilters(new InputFilter[]{new InputFilter.LengthFilter(len)});
        mOldPwd.requestFocus();
        rootView.findViewById(R.id.btn_input_password_confirm).setOnClickListener
                (mIconClickListener);
        return rootView;
    }


    private void getPasswdParam() {
        int type = getArguments().getInt(ARG_PD_TYPE);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences
                (getActivity());
        if (type == SettingsPwdManageFragment.PWD_SYSTEM) {
            titleId = R.string.passwdmanage_system;
            pdKey = SysParam.SEC_SYSPWD;
            pdValue = sharedPreferences.getString(SysParam.SEC_SYSPWD, "");
            if (TextUtils.isEmpty(pdValue)) {
                pdValue = "88888888";
            }
        } else {
            titleId = R.string.passwdmanage_safe;
            pdKey = SysParam.SEC_SECPWD;
            pdValue = sharedPreferences.getString(SysParam.SEC_SECPWD, "");
            if (TextUtils.isEmpty(pdValue)) {
                pdValue = "12345678";
            }

        }
    }

    private void savePasswd() {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences
                (getActivity()).edit();
        editor.putString(pdKey, mNewPwd.getText().toString());
        editor.commit();
    }
}
