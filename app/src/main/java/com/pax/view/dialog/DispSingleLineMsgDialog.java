package com.pax.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.pax.pay.utils.TickTimer;
import com.pax.pay.utils.TickTimer.TickTimerListener;
import com.pax.up.bjb.R;

public class DispSingleLineMsgDialog extends Dialog {

    private TickTimer tickTimer;
    private Button btnConfirm;
    private Context context;

    private String title;
    private String content;

    private int tickTime;

    public DispSingleLineMsgDialog(Context context) {
        super(context);
    }

    public DispSingleLineMsgDialog(Context context, String title, String content, int tickTime) {
        super(context, R.style.alert_dialog);
        this.context = context;
        this.title = title;
        this.content = content;
        this.tickTime = tickTime;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View convertView = getLayoutInflater().inflate(R.layout.activity_disp_single_line_msg_layout, null);
        setContentView(convertView);
        initViews(convertView);
        tickTimerStop();
        tickTimerStart(tickTime);
    }

    private void initViews(View view) {
        TextView tvPrompt = (TextView) findViewById(R.id.version_prompt);
        tvPrompt.setText(title);

        TextView tvContent = (TextView) findViewById(R.id.version_tv);
        tvContent.setText(content);
        if (content.contains("￥")) {
            SpannableString msp = new SpannableString(content);
            msp.setSpan(new RelativeSizeSpan(0.7f), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            tvContent.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    context.getResources().getDimensionPixelSize(R.dimen.disp_single_amount_text));
            tvContent.setText(msp);
        } else {
            tvContent.setTextColor(context.getResources().getColor(R.color.cancel_button_text));
            tvContent.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    context.getResources().getDimensionPixelSize(R.dimen.disp_single_version_text));
        }

        btnConfirm = (Button) findViewById(R.id.confirm_btn);
        btnConfirm.setOnClickListener(new android.view.View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                tickTimerStop();
                dismiss();
            }
        });

    }

    public void tickTimerStart(int timout) {
        if (tickTimer != null)
            tickTimer.cancel();
        tickTimer = new TickTimer(timout, 1);
        tickTimer.setTimeCountListener(new TickTimerListener() {

            @Override
            public void onTick(long leftTime) {
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
