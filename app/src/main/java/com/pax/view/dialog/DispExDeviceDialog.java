package com.pax.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.pax.pay.utils.AppLog;
import com.pax.pay.utils.TickTimer;
import com.pax.pay.utils.TickTimer.TickTimerListener;
import com.pax.up.bjb.R;

public class DispExDeviceDialog extends Dialog {
    private static final String TAG = "DispExDeviceDialog";
    private Context context;
    private TickTimer tickTimer;

    private int resId;
    private int tickTime;
    private String content;

    public DispExDeviceDialog(Context context) {
        super(context);
    }

    public DispExDeviceDialog(Context context, int resId, String content, int tickTime) {
        super(context, R.style.alert_dialog);
        this.context = context;
        this.resId = resId;
        this.content = content;
        this.tickTime = tickTime;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View convertView = getLayoutInflater().inflate(R.layout.activity_disp_ex_device_layout, null);
        setContentView(convertView);
        initViews(convertView);
        setCancelable(false);
        tickTimerStop();
        tickTimerStart(tickTime);
    }

    private void initViews(View view) {
        ImageView imageView = (ImageView) findViewById(R.id.prompt_ex_img);
        imageView.setBackgroundResource(resId);

        TextView tvContent = (TextView) findViewById(R.id.prompt_ex_text);
        tvContent.setText(content);

    }

    @Override
    public void dismiss() {
        tickTimerStop();
        super.dismiss();
    }

    public void tickTimerStart(int timout) {
        if (tickTimer != null)
            tickTimer.cancel();
        tickTimer = new TickTimer(timout, 1);
        tickTimer.setTimeCountListener(new TickTimerListener() {

            @Override
            public void onTick(long leftTime) {
                AppLog.i(TAG, "onTick:" + leftTime);
            }

            @Override
            public void onFinish() {
                dismiss();
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

}
