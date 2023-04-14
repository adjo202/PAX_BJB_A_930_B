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

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.pax.up.bjb.R;

public class SettingsCustomDialog extends Dialog {
    int dialogPadding;

    public SettingsCustomDialog(Context context, View view) {
        super(context, R.style.AlertDialogStyle);
        setContentView(view);
        dialogPadding = context.getResources().getDimensionPixelOffset(R.dimen.dialog_window_padding);
        Window window = getWindow();
        window.getDecorView().setPadding(dialogPadding, 0, dialogPadding, 0);// 设置距离屏幕两边的距离
    }

    public static class Builder {
        private Context mContext;
        private FrameLayout mCustomerContainer;
        private View mCustomerView;
        private CharSequence mTitle;
        private CharSequence mPositiveText;
        private CharSequence mNegativeText;
        private CharSequence mNeutralText;
        private CharSequence mMessage;
        private OnClickListener mNegativeButtonListener;
        private OnClickListener mPositiveButtonListener;
        private OnClickListener mNeutralButtonListener;

        public Builder(Context context) {
            mContext = context;
        }

        public Builder setTitle(int textID) {
            setTitle(mContext.getString(textID));
            return this;
        }

        public Builder setTitle(CharSequence title) {
            mTitle = title;
            return this;
        }

        public Builder setMessage(int textID) {
            mMessage = mContext.getString(textID);
            return this;
        }

        public Builder setPositiveButton(int textID, OnClickListener onClickListener) {
            setPositiveButton(mContext.getString(textID), onClickListener);
            return this;
        }

        public Builder setPositiveButton(CharSequence text, OnClickListener onClickListener) {
            mPositiveText = text;
            mPositiveButtonListener = onClickListener;
            return this;
        }

        public Builder setNegativeButton(int textID, OnClickListener onClickListener) {
            setNegativeButton(mContext.getString(textID), onClickListener);
            return this;
        }

        public Builder setNegativeButton(CharSequence text, OnClickListener onClickListener) {
            mNegativeText = text;
            mNegativeButtonListener = onClickListener;
            return this;
        }
        public Builder setNeutralButton(int textID, OnClickListener onClickListener) {
            setNegativeButton(mContext.getString(textID), onClickListener);
            return this;
        }

        public Builder setNeutralButton(CharSequence text, OnClickListener onClickListener) {
            mNeutralText = text;
            mNeutralButtonListener = onClickListener;
            return this;
        }

        public Builder setView(View view) {
            mCustomerView = view;
            return this;
        }

        public SettingsCustomDialog create() {
            View contentView = View.inflate(mContext, R.layout.settings_alert_dialog_layout, null);
            final SettingsCustomDialog dialog = new SettingsCustomDialog(mContext, contentView);

            TextView titleView = (TextView) contentView.findViewById(R.id.alertTitle);
            if (!TextUtils.isEmpty(mTitle)) {
                titleView.setVisibility(View.VISIBLE);
                titleView.setText(mTitle);
            } else {
                titleView.setVisibility(View.GONE);
            }

            FrameLayout customerContainer = (FrameLayout) contentView.findViewById(R.id.customer_container);
            if (mCustomerView != null) {
                customerContainer.setVisibility(View.VISIBLE);
                customerContainer.addView(mCustomerView);
            } else {
                customerContainer.setVisibility(View.GONE);
            }

            Button positiveButton = (Button) contentView.findViewById(R.id.positiveButton);
            if (mPositiveText != null) {
                positiveButton.setText(mPositiveText);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mPositiveButtonListener.onClick(dialog, BUTTON_POSITIVE);
                        dialog.dismiss();
                    }
                });
            } else {
                positiveButton.setVisibility(View.GONE);
            }

            Button negativeButton = (Button) contentView.findViewById(R.id.negativeButton);
            if (mNegativeText != null) {
                negativeButton.setText(mNegativeText);
                negativeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mNegativeButtonListener.onClick(dialog, BUTTON_NEGATIVE);
                        dialog.dismiss();
                    }
                });
            } else {
                negativeButton.setVisibility(View.GONE);
            }
            Button neutralButton = (Button) contentView.findViewById(R.id.neutralButton);
            if (mNeutralText != null) {
                neutralButton.setText(mNeutralText);
                neutralButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mNeutralButtonListener.onClick(dialog, BUTTON_NEUTRAL);
                        dialog.dismiss();
                    }
                });
            } else {
                neutralButton.setVisibility(View.GONE);
            }
            // 去掉包含确定和取消的viewgroup
            if (mPositiveText == null && mNegativeText == null && mNeutralText == null) {
                contentView.findViewById(R.id.buttonPanel_first_child).setVisibility(View.GONE);
            }
            // 单选取消的背景
            if (mPositiveText == null && mNegativeText != null && mNeutralText == null) {
                //
                negativeButton.setBackgroundResource(R.drawable.cancel_button_background_only);
            }
            ViewGroup buttonsContainer = (ViewGroup) contentView.findViewById(R.id.buttonPanel);
            buttonsContainer.setVisibility(shouldSetGone(buttonsContainer));
//            //I changed the code above to following codes Steven 2017-6-16 14:52:23
//            boolean flag = false;
//            for (int i = 0; i < buttonsContainer.getChildCount(); i++) {
//                if (buttonsContainer.getChildAt(i).getVisibility() != View.GONE) {
//                    flag = true;
//                    buttonsContainer.setVisibility(View.VISIBLE);
//                    break;
//                }
//            }
//            if(flag == false){
//                buttonsContainer.setVisibility(View.GONE);
//            }

            return dialog;
        }

        private int shouldSetGone(ViewGroup viewGroup) {
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                if (viewGroup.getChildAt(i).getVisibility() != View.GONE) {
                    return View.VISIBLE;
                }
            }
            return View.GONE;
        }
    }

    public Button getPositiveButton() {
        return (Button) getWindow().getDecorView().findViewById(R.id.positiveButton);
    }
}
