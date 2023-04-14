package com.pax.view;

import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.pax.up.bjb.R;

import java.util.HashMap;
import java.util.List;

public class DetailListAdapter extends BaseAdapter {
    private final int TYPE_WHITE = 0;
    private final int TYPE_GRAY = 1;
    private Context context;
    private List<HashMap<String, Object>> list;

    public DetailListAdapter(Context context, List<HashMap<String, Object>> list) {
        super();
        this.context = context;
        this.list = list;
    }

    @Override
    public int getViewTypeCount() {
        return 4;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.trans_query_listitem, null);
        }

        TextView typeTv = BaseViewHolder.get(convertView, R.id.trans_type);
        TextView valueTv = BaseViewHolder.get(convertView, R.id.trans_value);

        HashMap<String, Object> item = list.get(position);

        String type = (String) item.get("transType");
        String value = (String) item.get("transValue");

        typeTv.setText(type);
        valueTv.setText(value);

        if (value.contains("￥")) {
            SpannableString msp = new SpannableString(value);
            msp.setSpan(new RelativeSizeSpan(0.7f), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            valueTv.setTextColor(context.getResources().getColor(R.color.trans_amount_color));
            valueTv.setText(msp);
            valueTv.setTextSize(26f);
        } else if (value.equals(context.getString(R.string.voided)) || value.equals(context.getString(R.string.upload))) {
            valueTv.setTextColor(context.getResources().getColor(R.color.trans_amount_color));
        } else if (value.equals(context.getString(R.string.normal)) || value.equals(context.getString(R.string.adjust))
                || value.equals(context.getString(R.string.un_upload))) {
            valueTv.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
        } else {
            valueTv.setTextColor(context.getResources().getColor(R.color.text_land_main));
        }

        changeBackgound(convertView, getItemViewType(position));

        return convertView;
    }

    private void changeBackgound(View convertView, int type) {

        switch (type) {
            case TYPE_GRAY:
                convertView.setBackgroundResource(R.color.background_land_main);
                break;
            case TYPE_WHITE:
                convertView.setBackgroundResource(R.color.sweet_dialog_bg_color);
                break;
            default:
                break;
        }

    }

    @Override
    public int getItemViewType(int position) {
        return position % 2 == 0 ? TYPE_GRAY : TYPE_WHITE;
    }

    static class BaseViewHolder {
        @SuppressWarnings("unchecked")
        public static <T extends View> T get(View view, int id) {

            SparseArray<View> viewHolder = (SparseArray<View>) view.getTag();

            if (viewHolder == null) {
                viewHolder = new SparseArray<View>();
                view.setTag(viewHolder);
            }

            View childView = viewHolder.get(id);
            if (childView == null) {
                childView = view.findViewById(id);
                viewHolder.put(id, childView);
            }

            return (T) childView;
        }
    }
}
