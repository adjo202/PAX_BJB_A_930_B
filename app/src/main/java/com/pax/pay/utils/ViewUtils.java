package com.pax.pay.utils;

import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pax.up.bjb.R;

public class ViewUtils {

    /**
     * 生成每一行记录
     * 
     * @param title
     * @param value
     * @return
     */
    public static LinearLayout genSingleLineLayoutNew(Context context, String title, Object value) {
        LinearLayout layout = new LinearLayout(context);

        TextView titleTv = new TextView(context);
        titleTv.setText(title);
        titleTv.setGravity(Gravity.START);
        titleTv.setTextSize(18f);
        titleTv.setTextColor(context.getResources().getColor(R.color.prompt_text_color));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        /*lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);// 与父容器的左侧对齐
        lp.addRule(RelativeLayout.CENTER_VERTICAL);*/
        layout.setOrientation(LinearLayout.VERTICAL);
        titleTv.setLayoutParams(lp);// 设置布局参数
        layout.addView(titleTv);

        /**************************************************/

        TextView valueTv = new TextView(context);
        valueTv.setText(String.valueOf(value));
        valueTv.setGravity(Gravity.END);
        valueTv.setTextSize(18f);
        if (String.valueOf(value).contains("￥")) {
            SpannableString msp = new SpannableString(String.valueOf(value));
            msp.setSpan(new RelativeSizeSpan(0.7f), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            valueTv.setTextColor(context.getResources().getColor(R.color.trans_amount_color));
            valueTv.setTextSize(26f);
            valueTv.setText(msp);
        } else if (String.valueOf(value).equals(context.getString(R.string.voided))
                || String.valueOf(value).equals(context.getString(R.string.upload))) {
            valueTv.setTextColor(context.getResources().getColor(R.color.trans_amount_color));

        } else if (String.valueOf(value).equals(context.getString(R.string.normal))
                || String.valueOf(value).equals(context.getString(R.string.adjust))
                || String.valueOf(value).equals(context.getString(R.string.un_upload))) {
            valueTv.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
        }

        RelativeLayout.LayoutParams rp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        rp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);// 与父容器的右侧对齐
        rp.addRule(RelativeLayout.CENTER_VERTICAL);
        valueTv.setLayoutParams(rp);// 设置布局参数
        layout.addView(valueTv);

        return layout;
    }

    public static RelativeLayout genSingleLineLayout(Context context, String title, Object value) {
        RelativeLayout layout = new RelativeLayout(context);

        TextView titleTv = new TextView(context);
        titleTv.setText(title);
        titleTv.setGravity(Gravity.START);
        titleTv.setTextSize(18f);
        titleTv.setTextColor(context.getResources().getColor(R.color.prompt_text_color));

        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);// 与父容器的左侧对齐
        lp.addRule(RelativeLayout.CENTER_VERTICAL);
        titleTv.setLayoutParams(lp);// 设置布局参数
        layout.addView(titleTv);

        /**************************************************/

        TextView valueTv = new TextView(context);
        valueTv.setText(String.valueOf(value));
        valueTv.setGravity(Gravity.END);
        valueTv.setTextSize(18f);
        if (String.valueOf(value).contains("￥")) {
            SpannableString msp = new SpannableString(String.valueOf(value));
            msp.setSpan(new RelativeSizeSpan(0.7f), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            valueTv.setTextColor(context.getResources().getColor(R.color.trans_amount_color));
            valueTv.setTextSize(26f);
            valueTv.setText(msp);
        } else if (String.valueOf(value).equals(context.getString(R.string.voided))
                || String.valueOf(value).equals(context.getString(R.string.upload))) {
            valueTv.setTextColor(context.getResources().getColor(R.color.trans_amount_color));

        } else if (String.valueOf(value).equals(context.getString(R.string.normal))
                || String.valueOf(value).equals(context.getString(R.string.adjust))
                || String.valueOf(value).equals(context.getString(R.string.un_upload))) {
            valueTv.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
        }

        RelativeLayout.LayoutParams rp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        rp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);// 与父容器的右侧对齐
        rp.addRule(RelativeLayout.CENTER_VERTICAL);
        valueTv.setLayoutParams(rp);// 设置布局参数
        layout.addView(valueTv);

        return layout;
    }

    public static RelativeLayout genSingleLineLayoutBlack(Context context, String title, Object value) {
        RelativeLayout layout = new RelativeLayout(context);

        TextView titleTv = new TextView(context);
        titleTv.setText(title);
        titleTv.setGravity(Gravity.START);
        titleTv.setTextSize(17f);


        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);// 与父容器的左侧对齐
        lp.addRule(RelativeLayout.CENTER_VERTICAL);
        titleTv.setLayoutParams(lp);// 设置布局参数
        layout.addView(titleTv);

        /**************************************************/

        TextView valueTv = new TextView(context);
        valueTv.setText(String.valueOf(value));
        valueTv.setGravity(Gravity.END);
        valueTv.setTextSize(17f);
        if (String.valueOf(value).contains("￥")) {
            SpannableString msp = new SpannableString(String.valueOf(value));
            msp.setSpan(new RelativeSizeSpan(0.7f), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            valueTv.setTextColor(context.getResources().getColor(R.color.trans_amount_color));
            valueTv.setTextSize(26f);
            valueTv.setText(msp);
        } else if (String.valueOf(value).equals(context.getString(R.string.voided))
                || String.valueOf(value).equals(context.getString(R.string.upload))) {
            valueTv.setTextColor(context.getResources().getColor(R.color.trans_amount_color));

        } else if (String.valueOf(value).equals(context.getString(R.string.normal))
                || String.valueOf(value).equals(context.getString(R.string.adjust))
                || String.valueOf(value).equals(context.getString(R.string.un_upload))) {
            valueTv.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
        }

        RelativeLayout.LayoutParams rp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        rp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);// 与父容器的右侧对齐
        rp.addRule(RelativeLayout.CENTER_VERTICAL);
        valueTv.setLayoutParams(rp);// 设置布局参数
        layout.addView(valueTv);

        return layout;
    }

}
