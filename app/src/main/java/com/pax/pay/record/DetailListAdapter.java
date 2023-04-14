package com.pax.pay.record;

import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.pax.up.bjb.R;

import java.util.HashMap;
import java.util.List;

public class DetailListAdapter extends SimpleAdapter {
    private Context context;
    private LayoutInflater inflater;
    private int resource;
    private final int TYPE_WHITE = 0;
    private final int TYPE_GRAY = 1;
    private final int TYPE_WHITE_LAST = 2;
    private final int TYPE_GRAY_LAST = 3;

    public DetailListAdapter(Context context, List<HashMap<String, Object>> data, int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
        this.resource = resource;
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(resource, null);
        }
        changeBackgound(convertView, getItemViewType(position));
        return super.getView(position, convertView, parent);
    }

    private void changeBackgound(View convertView, int type) {

        switch (type) {
            case TYPE_GRAY:
                convertView.setBackgroundResource(R.color.background_land_main);
                break;
            case TYPE_WHITE:
                convertView.setBackgroundResource(R.color.sweet_dialog_bg_color);
                break;
            case TYPE_GRAY_LAST:
                convertView.setBackgroundResource(R.drawable.search_employee_item_grayright);
                break;
            case TYPE_WHITE_LAST:
                convertView.setBackgroundResource(R.drawable.search_employee_item_whiteright);
                break;
            default:
                break;
        }

    }

    @Override
    public int getItemViewType(int position) {
        return position % 2 == 0 ? (position + 1 == getCount() ? TYPE_WHITE_LAST : TYPE_WHITE)
                : (position + 1 == getCount() ? TYPE_GRAY_LAST : TYPE_GRAY);
    }

    @Override
    public int getViewTypeCount() {
        return 4;
    }

    @Override
    public void setViewText(TextView valueTv, String value) {
        super.setViewText(valueTv, value);
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
    }
}
