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
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.text.InputFilter;
import android.text.method.NumberKeyListener;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.pax.up.bjb.R;

public class SettingsEditTextPreference extends SettingsCustomPreference {

    static int mPositionOfPrefActHeader = 0;
    EditText edittext;
    String mText;
    Context context;
    private int maxLength, maxLine, inputType, ems, minEms, maxEms;
    private boolean singLine, selectAllOnFocus;
    private Object mDefaultValue;
    private String hint, digits;

    public SettingsEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MyDialogPreference);
        maxLength = typedArray.getInt(R.styleable.MyDialogPreference_maxLength, 20);// 设置最大多少长度
        maxLine = typedArray.getInt(R.styleable.MyDialogPreference_maxLines, 1);// 设置最大行数
        singLine = typedArray.getBoolean(R.styleable.MyDialogPreference_singLine, false);// 设置是否单行
        digits = typedArray.getString(R.styleable.MyDialogPreference_digits);
        ems = typedArray.getInt(R.styleable.MyDialogPreference_ems, -1);
        maxEms = typedArray.getInt(R.styleable.MyDialogPreference_maxEms, -1);
        minEms = typedArray.getInt(R.styleable.MyDialogPreference_minEms, -1);
        inputType = typedArray.getInt(R.styleable.MyDialogPreference_inputType, EditorInfo.TYPE_CLASS_TEXT);
        selectAllOnFocus = typedArray.getBoolean(R.styleable.MyDialogPreference_selectAllOnFocus, false);
        hint = typedArray.getString(R.styleable.MyDialogPreference_hint);
        mDefaultValue = typedArray.getString(R.styleable.MyDialogPreference_defaultValue);
        if (mDefaultValue != null) {
            setDefaultValue(mDefaultValue);
        }
        // 使用完后回收
        typedArray.recycle();
    }

    public SettingsEditTextPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SettingsEditTextPreference(Context context) {
        this(context, null);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        SharedPreferences pref = getSharedPreferences();

        edittext.setText(pref.getString(getKey(), ""));

    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
            String value = edittext.getText().toString();
            if (callChangeListener(value)) {
                setText(value);
            }
        }
    }

    @Override
    public void findViewsInDialogById(View view) {
        edittext = (EditText) view.findViewById(R.id.edit_tv);
        edittext.setMaxLines(maxLine);
        edittext.setFilters(new InputFilter[] { new InputFilter.LengthFilter(maxLength) }); // 最大输入长度
        edittext.setSingleLine(singLine);
        if (hint != null) {
            edittext.setHint(hint);
        }
        edittext.setInputType(inputType);
        if (digits != null) {
            edittext.setKeyListener(new NumberKeyListener() {

                @Override
                public int getInputType() {
                    return inputType;
                }

                @Override
                protected char[] getAcceptedChars() {
                    return digits.trim().toCharArray();
                }
            });
        }

        edittext.setSelectAllOnFocus(selectAllOnFocus);
        edittext.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                    InputMethodManager imm = (InputMethodManager) context
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    onDialogClosed(true);
                    onActivityDestroy();
                    ((SettingsActivity) context).setSelection(mPositionOfPrefActHeader);
                }

                return false;
            }
        });

        if (ems > 0) {
            edittext.setEms(ems);
        }
        if (maxEms > 0) {
            edittext.setMaxEms(maxEms);
        }
        if (minEms > 0) {
            edittext.setMinEms(minEms);
        }
        // mText = edittext.getText().toString();
        mText = PreferenceManager.getDefaultSharedPreferences(this.getContext()).getString(this.getKey(), null);

    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        setText(restorePersistedValue ? getPersistedString(mText) : (String) defaultValue);
    }

    @Override
    public int setLayoutInDialog() {
        return R.layout.settings_dialog_layout;
    }

    /**
     * Gets the text from the {@link SharedPreferences}.
     * 
     * @return The current preference value.
     */
    public String getText() {
        return mText;
    }

    public void setText(String text) {
        final boolean wasBlocking = shouldDisableDependents();

        mText = text;

        persistString(text);

        final boolean isBlocking = shouldDisableDependents();
        if (isBlocking != wasBlocking) {
            notifyDependencyChange(isBlocking);
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            // No need to save instance state since it's persistent
            return superState;
        }

        final SavedState myState = new SavedState(superState);
        myState.text = getText();
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        setText(myState.text);
    }

    private static class SavedState extends BaseSavedState {
        String text;

        public SavedState(Parcel source) {
            super(source);
            text = source.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(text);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

}
