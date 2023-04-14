package com.pax.view;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.pax.pay.trans.model.AccountData;
import com.pax.pay.trans.model.OptionModel;
import com.pax.up.bjb.R;

import java.util.List;
import java.util.Vector;

@SuppressWarnings("rawtypes")
public class OptionsAdapter extends ListAdapter {

    private Activity activity;
    private List<OptionModel> models;
    private LayoutInflater inflater;
    private int pos;
    private int lastPosition = -1; // 记录上一次选中的图片位置，-1表示未选中
    private Vector<Boolean> vector = new Vector<Boolean>(); // 定义一个向量作为选中与否容器

    public OptionsAdapter(Activity activity, List<OptionModel> models) {
        super(activity);
        this.activity = activity;
        this.models = models;
        inflater = LayoutInflater.from(activity);

        for (int i = 0; i < models.size(); i++) {
            vector.add(false);
        }
    }

    @Override
    public int getCount() {

        return models.size();
    }

    @Override
    protected ViewHolder createViewHolder(View root) {
        OptionHolder hold = new OptionHolder();
        hold.tvOption = (TextView) root.findViewById(R.id.mode_grid_tv);
        hold.tvAdditional = (TextView) root.findViewById(R.id.mode_grid_tv_additional);
        return hold;
    }

    @Override
    protected void fillView(View root, Object item, ViewHolder holder, int position) {
        final OptionHolder hold = (OptionHolder) holder;
        hold.model = models.get(position);

        if (!"".equals(models.get(position).getContent())) {
            AccountData m = ((AccountData) models.get(position).getObject());
            hold.tvOption.setText(models.get(position).getContent().trim());    //ed denny

            //Sandy : if content doesn't same with accountTypeText then print it
            if(models.get(position).getContent().trim().equalsIgnoreCase(m.getAccountTypeText()) == false )
                hold.tvAdditional.setText(((AccountData) models.get(position).getObject()).getAccountTypeText() );    //sandy

        }
        if (vector.elementAt(position) == true) {
            hold.tvOption.setBackgroundResource(R.drawable.bg_selected);
        } else {
            hold.tvOption.setBackgroundResource(R.drawable.bg_default);
        }
    }

    @Override
    protected int getItemViewId() {
        return R.layout.mode_grid_item;
    }

    class OptionHolder extends ViewHolder {
        private TextView tvOption;
        private TextView tvAdditional;
        private OptionModel model;
    }

    /**
     * 修改选中时的状态
     * 
     * @param position
     */
    public void changeState(int position) {
        if (lastPosition != -1)
            vector.setElementAt(false, lastPosition); // 取消上一次的选中状态
        vector.setElementAt(!vector.elementAt(position), position); // 直接取反即可
        lastPosition = position; // 记录本次选中的位置
        notifyDataSetChanged(); // 通知适配器进行更新
    }
}
