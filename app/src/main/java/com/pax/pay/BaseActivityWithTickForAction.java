package com.pax.pay;

import android.os.Bundle;
import android.util.Log;

import com.pax.abl.core.AAction;
import com.pax.abl.core.ActionResult;
import com.pax.pay.trans.TransContext;
import com.pax.pay.trans.TransResult;
import com.pax.pay.utils.AppLog;
import com.pax.pay.utils.TickTimer;
import com.pax.pay.utils.TickTimer.TickTimerListener;

public abstract class BaseActivityWithTickForAction extends BaseActivity {
    private static final String TAG = "BaseActivityWithTick";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //tickTimerStart();
        super.onCreate(savedInstanceState);
    }

    private TickTimer tickTimer;

    /**
     * 界面超时定时器， 默认超时60秒
     */
    public void tickTimerStart() {
        tickTimerStart(60);
    }

    /**
     * 界面超时定时器
     * 
     * @param timout
     *            ： 单位秒
     */
    public void tickTimerStart(int timout) {
        if (tickTimer != null)
            tickTimer.cancel(); //Cancel the countdown.
        tickTimer = new TickTimer(timout, 1);
        tickTimer.setTimeCountListener(new TickTimerListener() {

            @Override
            public void onTick(long leftTime) {
                AppLog.i(TAG, "onTick:" + leftTime);
            }

            @Override
            public void onFinish() {
                finish(new ActionResult(TransResult.ERR_ABORTED, null));
            }
        });
        tickTimer.start();
    }

    public void tickTimerStop() {
        if (tickTimer != null) {
            tickTimer.cancel();
            tickTimer = null;
        }
    }

    boolean hasfinish = false;

    public void finish(ActionResult result) {
        if (hasfinish) {
            return;
        }
        hasfinish = true;
        Log.d(TAG,"Sandy.BaseActivityWithTickForAction.finish: " + result.getData());
        tickTimerStop();
        AAction action = TransContext.getInstance().getCurrentAction();
        Log.d(TAG,"Sandy.BaseActivityWithTickForAction.finish: " + action);
        if (action != null) {
            action.setResult(result);
            TransContext.getInstance().setCurrentAction(null);
        } else {
            finish();
        }
    }
}
