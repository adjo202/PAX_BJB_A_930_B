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
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.pax.settings.SettingsRadioGroup.OnCheckedChangeListener;
import com.pax.up.bjb.R;

public class SettingsSingleChoicePreference extends SettingsCustomPreference {

    private SettingsRadioGroup radioGroup;
    private CharSequence[] mEntries;
    private CharSequence[] mEntryValues;
    private String mValue;
    private String mSummary;
    private int mClickedDialogEntryIndex;
    private boolean mValueSet;

    public SettingsSingleChoicePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MySingleChoiceDialogPreference);
        mEntries = typedArray.getTextArray(R.styleable.MySingleChoiceDialogPreference_entries);
        mEntryValues = typedArray.getTextArray(R.styleable.MySingleChoiceDialogPreference_entryValues);
        typedArray.recycle();

        /*
         * Retrieve the Preference summary attribute since it's private in the Preference class.
         */
        typedArray = context.obtainStyledAttributes(attrs, R.styleable.MySingleChoiceDialogPreference);
        mSummary = typedArray.getString(R.styleable.MySingleChoiceDialogPreference_summary);
        typedArray.recycle();

    }

    public SettingsSingleChoicePreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SettingsSingleChoicePreference(Context context) {
        this(context, null);
    }

    /**
     * Sets the human-readable entries to be shown in the list. This will be shown in subsequent dialogs.
     * <p>
     * Each entry must have a corresponding index in {@link #setEntryValues(CharSequence[])}.
     * 
     * @param entries
     *            The entries.
     * @see #setEntryValues(CharSequence[])
     */
    public void setEntries(CharSequence[] entries) {
        mEntries = entries;
    }

    /**
     * @see #setEntries(CharSequence[])
     * @param entriesResId
     *            The entries array as a resource.
     */
    public void setEntries(int entriesResId) {
        setEntries(getContext().getResources().getTextArray(entriesResId));
    }

    /**
     * The list of entries to be shown in the list in subsequent dialogs.
     * 
     * @return The list as an array.
     */
    public CharSequence[] getEntries() {
        return mEntries;
    }

    /**
     * The array to find the value to save for a preference when an entry from entries is selected. If a user clicks on
     * the second item in entries, the second item in this array will be saved to the preference.
     * 
     * @param entryValues
     *            The array to be used as values to save for the preference.
     */
    public void setEntryValues(CharSequence[] entryValues) {
        mEntryValues = entryValues;
    }

    /**
     * @see #setEntryValues(CharSequence[])
     * @param entryValuesResId
     *            The entry values array as a resource.
     */
    public void setEntryValues(int entryValuesResId) {
        setEntryValues(getContext().getResources().getTextArray(entryValuesResId));
    }

    /**
     * Returns the array of values to be saved for the preference.
     * 
     * @return The array of values.
     */
    public CharSequence[] getEntryValues() {
        return mEntryValues;
    }

    public String getValue() {
        return mValue;
    }

    public void setValue(String value) {
        // Always persist/notify the first time.
        final boolean changed = !TextUtils.equals(mValue, value);
        if (changed || !mValueSet) {
            mValue = value;
            mValueSet = true;
            persistString(value);
            if (changed) {
                notifyChanged();
            }
        }
    }

    /**
     * Returns the entry corresponding to the current value.
     * 
     * @return The entry corresponding to the current value, or null.
     */
    public CharSequence getEntry() {
        int index = getValueIndex();
        return index >= 0 && mEntries != null ? mEntries[index] : null;
    }

    private int getValueIndex() {
        return findIndexOfValue(mValue);
    }

    public int findIndexOfValue(String value) {
        if (value != null && mEntryValues != null) {
            for (int i = mEntryValues.length - 1; i >= 0; i--) {
                if (mEntryValues[i].equals(value)) {
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    public CharSequence getSummary() {
        final CharSequence entry = getEntry();
        if (mSummary == null || entry == null) {
            return super.getSummary();
        } else {
            return String.format(mSummary, entry);
        }
    }

    @Override
    public void setSummary(CharSequence summary) {
        super.setSummary(summary);
        if (summary == null && mSummary != null) {
            mSummary = null;
        } else if (summary != null && !summary.equals(mSummary)) {
            mSummary = summary.toString();
        }
    }

    public void setValueIndex(int index) {
        if (mEntryValues != null) {
            setValue(mEntryValues[index].toString());
        }
    }

    @Override
    public void findViewsInDialogById(View view) {
        radioGroup = (SettingsRadioGroup) view.findViewById(R.id.single_layout_radiogroup);
    }

    // onBindDialog后于findViwesInDialogById
    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        SharedPreferences pref = getSharedPreferences();
        mValue = pref.getString(getKey(), "");// 获取到当前的key
        mClickedDialogEntryIndex = getValueIndex();
        initSingleChoiceDialog();
    }

    // 不弹出输入框
    @Override
    protected boolean needInputMethod() {
        return false;
    }

    private void initSingleChoiceDialog() {
        for (int i = 0; i < getEntries().length; i++) {
            // radioButton设置布局样式
            SettingsRadioButton radioButton = new SettingsRadioButton(getContext());
            radioButton.setTextTitle(getEntries()[i].toString());
            if (i == 0) {
                radioButton.setLinevisibility(View.INVISIBLE);
            }
            radioGroup.addView(radioButton);

        }
        if (mClickedDialogEntryIndex != -1) {
            ((SettingsRadioButton) radioGroup.getChildAt(mClickedDialogEntryIndex)).setChecked(true);
        }
        radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(SettingsRadioGroup group, int checkedId) {
                for (int i = 0; i < radioGroup.getChildCount(); i++) {
                    // radiobutton被选中
                    if (radioGroup.getChildAt(i).getId() == checkedId
                            && radioGroup.getChildAt(i) instanceof SettingsRadioButton) {
                        ((SettingsRadioButton) radioGroup.getChildAt(i)).setChecked(true);
                        mClickedDialogEntryIndex = i;
                        
                        ((SettingsRadioButton) radioGroup.getChildAt(mClickedDialogEntryIndex)).setChecked(true);
                        SettingsSingleChoicePreference.this.onClick(getDialog(), DialogInterface
                                .BUTTON_POSITIVE);
                        getDialog().dismiss();
                    }
                }

            }
        });
    }

    @Override
    public int setLayoutInDialog() {
        return R.layout.settings_single_dlg_layout;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult && mClickedDialogEntryIndex >= 0 && mEntryValues != null) {
            String value = mEntryValues[mClickedDialogEntryIndex].toString();
            if (callChangeListener(value)) {
                setValue(value);
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        setValue(restoreValue ? getPersistedString(mValue) : (String) defaultValue);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            // No need to save instance state since it's persistent
            return superState;
        }

        final SavedState myState = new SavedState(superState);
        myState.value = getValue();
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
        setValue(myState.value);
    }

    private static class SavedState extends BaseSavedState {
        String value;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public SavedState(Parcel source) {
            super(source);
            value = source.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(value);
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
