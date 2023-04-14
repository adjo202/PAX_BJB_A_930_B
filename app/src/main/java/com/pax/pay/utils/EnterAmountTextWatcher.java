package com.pax.pay.utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import com.pax.pay.app.FinancialApplication;
import com.pax.settings.currency.Currency;

import java.text.DecimalFormat;

public class EnterAmountTextWatcher implements TextWatcher {
    public static final String TAG = "EnterAmountTextWatcher";

    private boolean mEditing;
    private String strPre = "";
    private final int MAX_DIGITS = 9;
    //sandy : added amount pattern
    private final String PATTERN = "###,###";

    public EnterAmountTextWatcher() {
        mEditing = false;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (!mEditing) {
            mEditing = true;
            String digits = s.toString().replace(".", "").trim().replaceAll("[^(0-9)]", "");
            //Log.d(TAG,"Sandy=afterTextChanged");
            String displayAmount = "";
            displayAmount = getDisplayAmount(digits);
            try {
                s.replace(0, s.length(), displayAmount);
                strPre = displayAmount;
            } catch (NumberFormatException nfe) {
                s.clear();
            }
            mEditing = false;
        }

    }

    private String getDisplayAmount(String digits) {
        String displayAmount = "";
        Currency currency = FinancialApplication.getSysParam().getCurrency();

        Log.d(TAG, currency.toString());

        if (digits.length() > MAX_DIGITS) {
            return strPre;
        }

        if (digits == null || digits.length() == 0) {
            return currency.getFormat();
        }

        //如果输入非法数据时， 设置成所选货币的默认显示格式
        try {
            /*
            if (currency.getDecimals() > 0) {
                String formatStr = "%d.%0" + currency.getDecimals() + "d";
                int rate = (int) Math.pow(10, currency.getDecimals());
                displayAmount = String.format(formatStr, Long.valueOf(digits) / rate, Long.valueOf(digits) % rate);
            } else {
                displayAmount = String.format("%d", Long.valueOf(digits));
            }*/

            //Sandy : Change the amount format into Indonesian Format

            DecimalFormat decimalFormat = new DecimalFormat(PATTERN);
            displayAmount = decimalFormat.format(Long.valueOf(digits));


        } catch (Exception e) {
            Log.e(TAG, "", e);
            displayAmount = currency.getFormat();
        }

        return displayAmount;
    }
}
