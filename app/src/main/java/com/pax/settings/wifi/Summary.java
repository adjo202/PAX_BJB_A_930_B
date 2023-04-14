package com.pax.settings.wifi;

import android.content.Context;
import android.net.NetworkInfo.DetailedState;

import com.pax.up.bjb.R;

public class Summary {
    static String get(Context context, String ssid, DetailedState state) {
        String[] formats = context.getResources().getStringArray(R.array.wifi_status);
        int index = state.ordinal();
        if (index >= formats.length || formats[index].length() == 0) {
            return null;
        }
        return String.format(formats[index - 1], ssid);
    }

    static String get(Context context, DetailedState state) {
        return get(context, null, state);
    }
}
