package com.pax.pay.utils;

import android.os.CountDownTimer;

public class TickTimer extends CountDownTimer {

    public interface TickTimerListener {
        public void onFinish();

        public void onTick(long leftTime);
    }

    private TickTimerListener listener;

    public void setTimeCountListener(TickTimerListener listener) {
        this.listener = listener;
    }

    /**
     *
     * @param timeout 表示以毫秒(1/1000S)为单位 倒计时的总数
     * @param tickInterval 表示 间隔 多少毫秒 调用一次 onTick 方法
     */
    public TickTimer(long timeout, long tickInterval) {
        super(timeout * 1000, tickInterval * 1000);
    }

    public TickTimer(long timeout, long tickInterval, TickTimerListener listener) {
        super(timeout * 1000, tickInterval * 1000);
        this.listener = listener;
    }

    @Override
    public void onFinish() {
        if (listener != null)
            listener.onFinish();
    }

    @Override
    public void onTick(long millisUntilFinished) {
        if (listener != null)
            listener.onTick(millisUntilFinished / 1000);
    }

}
