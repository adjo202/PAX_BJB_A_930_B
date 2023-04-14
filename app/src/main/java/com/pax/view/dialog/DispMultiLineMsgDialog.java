package com.pax.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.pax.pay.utils.TickTimer;
import com.pax.pay.utils.TickTimer.TickTimerListener;
import com.pax.up.bjb.R;

public class DispMultiLineMsgDialog extends Dialog {

    private TickTimer tickTimer;
    //private Button btnConfirm;
    private Context context;

    private String title;
    private String[] content;

    private int tickTime;

    public DispMultiLineMsgDialog(Context context) {
        super(context);
    }

    public DispMultiLineMsgDialog(Context context, String title, String[] content, int tickTime) {
        super(context, R.style.alert_dialog);
        this.context = context;
        this.title = title;
        this.content = content;
        this.tickTime = tickTime;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View convertView = getLayoutInflater().inflate(R.layout.activity_disp_multiple_line_msg_layout, null);
        setContentView(convertView);
        initViews(convertView);
        tickTimerStop();
        tickTimerStart(tickTime);
    }

    private void initViews(View view) {

        LinearLayout lines = (LinearLayout) findViewById(R.id.multilineContainer);
        for(int i=0;i<content.length;i++){
            lines.addView(addTextView(i, content[i]));
        }



         Button button = new Button(context);
         TableRow.LayoutParams paramsButton = new TableRow.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                100,1.0f);
         paramsButton.setMargins(17,25,17,15);
         button.setId(R.id.confirm_btn);
         button.setText("OK");
         button.setBackground(getContext().getResources().getDrawable(R.drawable.button_click_background));
         button.setTextColor(getContext().getResources().getColor(R.color.key_normal_color));
         button.setTextSize(20);
         button.setLayoutParams(paramsButton);
         button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                tickTimerStop();
                dismiss();
            }
        });
         lines.addView(button);



    }


    private TextView addTextView(int id, String text){

        TextView textView = new TextView(context);
        textView.setId(id);
        TableRow.LayoutParams paramsTv = new TableRow.LayoutParams(
                500,
                LinearLayout.LayoutParams.WRAP_CONTENT,1.0f);
        paramsTv.setMargins(17,15,0,0);

        textView.setText(text);
        textView.setLayoutParams(paramsTv);
        textView.setGravity(Gravity.START);
        textView.setTextColor(getContext().getResources().getColor(R.color.prompt_no_pwd_color) );
        textView.setTextSize(14);
        return textView;
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
