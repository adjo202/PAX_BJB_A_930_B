
/*******************************************************************************
 * ============================================================================
 * COPYRIGHT
 *               Pax CORPORATION PROPRIETARY INFORMATION
 *    This software is supplied under the terms of a license agreement or
 *    nondisclosure agreement with Pax Corporation and may not be copied
 *    or disclosed except in accordance with the terms in that agreement.
 *       Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 *  Module Date: 17-5-22 11:16
 *  Module Author: liliang
 *  Description:
 *  ============================================================================
 ******************************************************************************/

package com.pax.settings.currency;

import android.text.TextUtils;

import com.pax.gl.convert.IConvert;
import com.pax.pay.app.FinancialApplication;

/**
 * Created by liliang on 2017/4/11.
 * Used for currency settings.
 */

public enum  Currency {
    /**
     * Chinese Yuan
     */
    RMB("156", "RMB", 2),

    /**
     * Hong Kong Dollars
     */
    HKD("344", "HKD", 2),

    /**
     * Macao Pataca
     */
    MOP("446", "MOP", 2),

    /**
     * Malaysian Ringgit
     */
    MYR("458", "MYR", 2),

    /**
     * Singapore Dollars
     */
    SGD("702", "SGD", 2),

    /**
     * Thai Baht
     */
    THB("764", "THB", 2),

    /**
     * Indonesia Rupiah
     */
    IDR("360", "IDR", 0),

    /**
     * Japanese Yen
     */
    JPY("392", "JPY", 0),

    /**
     * Euro
     */
    EUR("978", "EUR", 2),

    /**
     * Philippine Pesos
     */
    PHP("608", "PHP", 2),

    /**
     * New Taiwanese Dollars
     */
    TWD("901", "TWD", 2),

    /**
     * US Dollars
     */
    USD("840", "USD", 2),

    /**
     * Vietnam DONG
     */
    VND("704", "VND", 0),

    /**
     * United Arab Durham
     */
    AED("784", "AED", 2),

    /**
     * Australian Dollars
     */
    AUD("036", "AUD", 2),

    /**
     * Canadian Dollars
     */
    CAD("124", "CAD", 2),

    /**
     * Cypriot Pounds
     */
    CYP("196", "CYP", 2),

    /**
     * Swiss Francs
     */
    CHF("756", "CHF", 2),

    /**
     * Danish Krone
     */
    DKK("208", "DKK", 2),

    /**
     * British Pounds Sterling
     */
    GBP("826", "GBP", 2),

    /**
     * Indian Rupee
     */
    INR("356", "INR", 2),

    /**
     * Icelandic krone
     */
    ISK("352", "ISK", 2),

    /**
     * South Korean Won
     */
    KRW("410", "KRW", 0),

    /**
     * Sri-Lanka Rupee
     */
    LKR("144", "LKR", 2),

    /**
     * Maltese Lira
     */
    MTL("470", "MTL", 2),

    /**
     * Norwegian Krone
     */
    NOK("578", "NOK", 2),

    /**
     * New Zealand Dollars
     */
    NZD("554", "NZD", 2),

    /**
     * Russian Ruble
     */
    RUB("643", "RUB", 2),

    /**
     * Saudi Riyal
     */
    SAR("682", "SAR", 2),

    /**
     * Swedish krone
     */
    SEK("752", "SEK", 2),

    /**
     * Turkey Lira
     */
    TRL("792", "TRL", 2),

    /**
     * Bolivar Fuerte (Venezuela)
     */
    VEF("937", "VEF", 2),

    /**
     * South African Rand
     */
    ZAR("710", "ZAR", 2),

    /**
     * Kuwaiti Dinar
     */
    KWD("414", "KWD", 3),

    /**
     * Chilean Piso
     */
    CLP("152", "CLP", 0),

    /**
     * Customized currency.
     */
    CUSTOM("", "", 0);

    /**
     * Currency code.
     */
    private String code;
    /**
     * Currency name.
     */
    private String name;
    /**
     * Currency decimals, 0-3.
     */
    private int decimals;
    /**
     *
     * @param code Currency code.
     * @param name Currency name.
     * @param decimals Currency decimals must be between 0 and 3.
     */
    Currency(String code, String name, int decimals) {
        if (decimals < 0 || decimals > 3) {
            throw new IllegalArgumentException("decimals must be 0-3.");
        }
        this.code = code;
        this.name = name;
        this.decimals = decimals;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        if (this != CUSTOM) {
            throw new IllegalAccessError("Only currency CUSTOM can be changed.");
        }
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (this != CUSTOM) {
            throw new IllegalAccessError("Only currency CUSTOM can be changed.");
        }
        this.name = name;
    }

    public int getDecimals() {
        return decimals;
    }

    public void setDecimals(int decimals) {
        if (this != CUSTOM) {
            throw new IllegalAccessError("Only currency CUSTOM can be changed.");
        }
        if (decimals < 0 || decimals > 3) {
            throw new IllegalArgumentException("decimals must be 0-3.");
        }
        this.decimals = decimals;
    }

    /**
     * Return the currency display format, there are 4 types of format according to decimals
     * (0-3):0,0.0, 0.00,0.000
     * @return The currency display format.
     */
    public String getFormat() {
        String format = null;
        switch (decimals) {
            case 0:
                format = "0";
                break;
            case 1:
                format = "0.0";
                break;
            case 2:
                format = "0.00";
                break;
            case 3:
                format = "0.000";
                break;
            default:
                break;
        }
        return format;
    }

    /**
     * Get the BCD code of currency code.
     * @return
     */
    public byte[] getCodeBcdBytes() {
        if (!TextUtils.isEmpty(code)) {
            return FinancialApplication
                    .getConvert().strToBcd(code, IConvert.EPaddingPosition.PADDING_LEFT);
        }

        return null;
    }

    /**
     * Get the BCD code of currency decimals.
     * @return
     */
    public byte[] getDecimalBcdBytes() {
        return FinancialApplication.getConvert()
                .strToBcd(String.valueOf(decimals), IConvert.EPaddingPosition.PADDING_LEFT);
    }

    /**
     * Get currency by currency code.
     * @param code currency code
     * @param defaultValue The value to be returned when the code does not exist.
     * @return
     */
    public static Currency queryCurrency(String code, Currency defaultValue) {
        if (TextUtils.isEmpty(code)) {
            return defaultValue;
        }

        for (Currency currency : Currency.values()) {
            if (code.equals(currency.code)) {
                return currency;
            }
        }

        return defaultValue;
    }

    public IConvert.ECurrencyExponent getCurrencyExponent() {
        return IConvert.ECurrencyExponent.values()[decimals];
    }
}
