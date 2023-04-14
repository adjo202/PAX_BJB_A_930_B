package com.pax.settings.uartloadkey;

import android.os.SystemClock;

import com.pax.dal.entity.EUartPort;

public class DownloadKey {

    public static int startLoad(EUartPort uartPort) {
        boolean result = UartLoadKey.getInstance().open(uartPort, "9600,8,E,1");
        if (!result) {
            return -1;
        }
        int ret = UartLoadKey.getInstance().loadKey();
        SystemClock.sleep(300);
        UartLoadKey.getInstance().close();
        return ret;
    }

    public static void stopLoad() {
        UartLoadKey.getInstance().cancel();
    }
}
