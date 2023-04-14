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

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pax.device.Device;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.emv.CardBin;
import com.pax.pay.trans.model.Controller;
import com.pax.pay.trans.model.TransData;
import com.pax.up.bjb.R;

import java.util.HashSet;
import java.util.Set;

public class SettingsMenuGeneralAdapter extends BaseAdapter {
    LayoutInflater mInflater;
    boolean[] defaultValues;
    String[] dispValues;
    Context mContext;
    private boolean isRadio; // 判断单选、多选按钮

    private OnItemClickForDownloadListener listener;

    public interface OnItemClickForDownloadListener {
        void onItemClick(int index);
    }

    public SettingsMenuGeneralAdapter(Context context, String[] dispValues, boolean[] defaultValues, boolean isRadio) {
        this.defaultValues = defaultValues;
        this.dispValues = dispValues;
        this.mInflater = LayoutInflater.from(context);
        this.mContext = context;
        this.isRadio = isRadio;
    }

    public void setOnItemClickForDownLoadListener(OnItemClickForDownloadListener listener) {
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return defaultValues.length;
    }

    @Override
    public Object getItem(int position) {
        return defaultValues[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.settings_item_menu_gerneral, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.textView.setText(dispValues[position]);
        if (isRadio) {
            viewHolder.imageView.setImageResource(defaultValues[position] ? R.drawable.checked : R.drawable.unchecked);
            convertView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    for (int i = 0; i < defaultValues.length; i++) {
                        defaultValues[i] = false;
                    }
                    defaultValues[position] = true;
                    notifyDataSetChanged();
                    listener.onItemClick(position);
                }
            });
        } else {
            viewHolder.imageView.setImageResource(defaultValues[position] ? R.drawable.multichecked
                    : R.drawable.unchecked);
            convertView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    defaultValues[position] = !defaultValues[position];
                    notifyDataSetChanged();
                }
            });
        }
        return convertView;
    }

    static class ViewHolder {
        TextView textView;
        ImageView imageView;

        public ViewHolder(View view) {
            textView = (TextView) view.findViewById(R.id.item_menugerneral_textview);
            imageView = (ImageView) view.findViewById(R.id.item_menugerneral_imageview);
        }
    }

    public void saveForMenuGeral(String[] origEntryValues, int index) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        Editor editor = sharedPreferences.edit();
        if (index == 0) {
            editor.remove(SysParam.TTS); //传统交易开关
        } else if (index == 1) {
            editor.remove(SysParam.ECTS); //电子现金交易开关
        } else if (index == 2) {
            editor.remove(SysParam.SCTS); //扫码交易开关
        }
        for (int i = 0; i < origEntryValues.length; i++) {
            editor.remove(origEntryValues[i]);
            SysParam.getAll().remove(origEntryValues[i]);
        }
        Set<String> set = new HashSet<>();
        for (int i = 0; i < defaultValues.length; i++) {
            if (defaultValues[i]) {
                set.add(origEntryValues[i]);
            } else {
                editor.putBoolean(origEntryValues[i], true);
            }
        }
        if (index == 0) {
            editor.putStringSet(SysParam.TTS, set);
        } else if (index == 1) {
            editor.putStringSet(SysParam.ECTS, set);
        } else if (index == 2) {
            editor.putStringSet(SysParam.SCTS, set);
        }
        editor.commit();
    }

    public void clearFunc() {
        boolean canBeep = false;
        if (defaultValues[0]) {
            canBeep = true;
            Log.d("teg", "8");
            TransData.deleteDupRecord();
        }
        if (defaultValues[1]) {
            canBeep = true;                    //批上送状态 {@link Constant#WORKED}未进行批上送
            FinancialApplication.getController().set(Controller.BATCH_UP_STATUS, Controller.Constant.WORKED);
            TransData.deleteAllTrans();        //是否需要清除交易记录: NO:不清除, YES:清除
            FinancialApplication.getController().set(Controller.CLEAR_LOG, Controller.Constant.NO);
        }
        if (defaultValues[2]) {
            canBeep = true;                    //报文头处理要求A(1-8)
            FinancialApplication.getController().set(Controller.HEADER_PROC_REQ_A, Controller.Constant.NO);
            FinancialApplication.getController().set(Controller.HEADER_PROC_REQ_B, Controller.Constant.NO);
        }                                      //报文头处理要求B(9-16)
        if (defaultValues[3]) {
            canBeep = true;
            TransData.deleteScript();
        }
        if (defaultValues[4]) {
            canBeep = true;
            CardBin.deleteBlack();
            FinancialApplication.getController().set(Controller.NEED_DOWN_BLACK, Controller.Constant.YES);
        }

        if (canBeep) {
            Device.beepOk();
        }
    }
}
