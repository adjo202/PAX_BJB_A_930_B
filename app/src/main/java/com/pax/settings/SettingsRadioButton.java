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
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Checkable;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pax.up.bjb.R;

public class SettingsRadioButton extends RelativeLayout implements Checkable {

    private View lineImageView;
    private TextView mTitle;
    private RadioButton mRadioButton;

    private boolean mChecked; // 状态是否选中

    private boolean mBroadcasting;
    private int id;
    private OnCheckedChangeListener mOnCheckedChangeWidgetListener;

    public SettingsRadioButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.settings_radio_btn_layout, this);

        mTitle = (TextView) findViewById(R.id.title);
        lineImageView = (View) findViewById(R.id.line);
        mRadioButton = (RadioButton) findViewById(R.id.check);
        mRadioButton.setButtonDrawable(R.drawable.settings_radio_btn);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.SXRadioButton);

        Drawable d = array.getDrawable(R.styleable.SXRadioButton_radio);
        if (d != null) {
            mRadioButton.setButtonDrawable(d);
        }

        String title = array.getString(R.styleable.SXRadioButton_title1);
        if (title != null) {
            setTextTitle(title);
        }

        boolean checked = array.getBoolean(R.styleable.SXRadioButton_checked, false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mTitle.setTextColor(getContext().getResources()
                    .getColorStateList(R.color.settings_radio_btn_textcolor, null));
        } else {
            mTitle.setTextColor(getContext().getResources()
                    .getColorStateList(R.color.settings_radio_btn_textcolor));
        }
        mRadioButton.setChecked(checked);

        array.recycle();
        setClickable(true);

        id = getId();
    }

    public SettingsRadioButton(Context context) {
        // TODO Auto-generated constructor stub
        this(context, null);
    }

    public void setButtonDrawable(int drawable) {
        mRadioButton.setButtonDrawable(drawable);
    }

    /***
     * 分割线Visiblility
     * 
     * @param visibility
     *            :View.GONE、 View.VISIBLE 、View.INVISIBLE
     */
    public void setLinevisibility(int visibility) {
        lineImageView.setVisibility(visibility);
    }

    @Override
    public boolean isChecked() {
        // TODO Auto-generated method stub
        return mChecked;
    }

    @Override
    public void setChecked(boolean checked) {
        // TODO Auto-generated method stub
        if (mChecked != checked) {
            mChecked = checked;
            mRadioButton.refreshDrawableState();
            mRadioButton.setChecked(mChecked);
            mTitle.setSelected(checked);

            // Avoid infinite recursions if setChecked() is called from a
            // listener
            if (mBroadcasting) {
                return;
            }

            mBroadcasting = true;
            if (mOnCheckedChangeWidgetListener != null) {
                mOnCheckedChangeWidgetListener.onCheckedChanged(this, mChecked);
            }
            mBroadcasting = false;
        }
    }

    @Override
    public void toggle() {
        // TODO Auto-generated method stub
        if (!isChecked()) {
            setChecked(!mChecked);
        }
    }

    @Override
    public boolean performClick() {
        // TODO Auto-generated method stub
        /*
         * XXX: These are tiny, need some surrounding 'expanded touch area', which will need to be implemented in Button
         * if we only override performClick()
         */

        /* When clicked, toggle the state */
        toggle();
        return super.performClick();
    }

    /**
     * Register a callback to be invoked when the checked state of this button changes. This callback is used for
     * internal purpose only.
     * 
     * @param listener
     *            the callback to call on checked state change
     * @hide
     */
    void setOnCheckedChangeWidgetListener(OnCheckedChangeListener listener) {
        mOnCheckedChangeWidgetListener = listener;
    }

    /**
     * Interface definition for a callback to be invoked when the checked state of a compound button changed.
     */
    public static interface OnCheckedChangeListener {
        /**
         * Called when the checked state of a compound button has changed.
         * 
         * @param buttonView
         *            The compound button view whose state has changed.
         * @param isChecked
         *            The new checked state of buttonView.
         */
        void onCheckedChanged(SettingsRadioButton buttonView, boolean isChecked);
    }

    public void setTextTitle(String s) {
        if (s != null) {
            mTitle.setText(s);
        }
    }

    public String getTextTitle() {
        String s = mTitle.getText().toString();
        return s == null ? "" : s;
    }

    public void setChangeImg(int checkedId) {
        if (checkedId == id) {
            mRadioButton.setChecked(true);
        } else {
            mRadioButton.setChecked(false);
        }
    }

}
