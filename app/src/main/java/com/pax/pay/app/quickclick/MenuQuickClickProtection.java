package com.pax.pay.app.quickclick;

public class MenuQuickClickProtection extends QuickClickProtection {
    private static MenuQuickClickProtection menuQuickClickProtection;

    private MenuQuickClickProtection(long timeoutMs) {
        super(timeoutMs);
    }

    public static synchronized MenuQuickClickProtection getInstance() {
        if (menuQuickClickProtection == null) {
            menuQuickClickProtection = new MenuQuickClickProtection(800);
        }

        return menuQuickClickProtection;
    }

}
