package com.pax.pay.operator;

import android.content.Context;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.pax.pay.BaseActivity;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.utils.Utils;
import com.pax.up.bjb.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * 显示操作员
 * 
 * @author Steven.W
 * 
 */
public class OperQueryActivity extends BaseActivity implements OnClickListener {

    private TextView tvTitle;
    private ImageView ivBack;
    private String navTitle;

    @Override
    protected void loadParam() {
        navTitle = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());
    }

    @Override
    protected int getLayoutId() {
        return R.layout.query_oper_layout;
    }

    @Override
    protected void initViews() {
        tvTitle = (TextView) findViewById(R.id.header_title);
        tvTitle.setText(navTitle);
        ivBack = (ImageView) findViewById(R.id.header_back);

        ListView listview = (ListView) findViewById(R.id.query_oper_listview);

        List<Operator> operList = Operator.findAll();
        List<HashMap<String, String>> dispOperList = new ArrayList<HashMap<String, String>>();
        for (Operator oper : operList) {
            HashMap<String, String> map = new HashMap<String, String>();
            // 目前数据库中只存储操作员，不存储管理员，此处无需判断
            // if (!(oper.getOperId().equals(SysParam.OPER_MANAGE) || oper.getOperId().equals(SysParam.OPER_SYS))) {
            map.put("oper", oper.getOperId());
            map.put("operName", oper.getName());
            dispOperList.add(map);
            // }
        }
        // 排序
        Collections.sort(dispOperList, new Comparator<HashMap<String, String>>() {
            @Override
            public int compare(HashMap<String, String> arg0, HashMap<String, String> arg1) {
                return Integer.parseInt(arg0.get("oper")) - Integer.parseInt(arg1.get("oper"));
            }
        });

        if (Utils.isScreenOrientationPortrait(OperQueryActivity.this)) {
            SimpleAdapter saImageItems1 = new SimpleAdapter(OperQueryActivity.this, dispOperList,
                    R.layout.manager_query_oper_listitem, new String[] { "oper", "operName" }, new int[] {
                            R.id.query_operator_id, R.id.query_operator_name });
            listview.setAdapter(saImageItems1);
        } else {
            SimpleListAdapter saImageItems1 = new SimpleListAdapter(OperQueryActivity.this, dispOperList,
                    R.layout.manager_query_oper_listitem, new String[] { "oper", "operName" }, new int[] {
                            R.id.query_operator_id, R.id.query_operator_name });
            listview.setAdapter(saImageItems1);
        }

    }

    @Override
    protected void setListeners() {
        ivBack.setOnClickListener(this);
    }

    @Override
    protected void handleMsg(Message msg) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.header_back:
                finish();
                break;

            default:
                break;
        }

    }

    class SimpleListAdapter extends SimpleAdapter {
        private LayoutInflater inflater;
        private int resource;
        private final int TYPE_WHITE = 0;
        private final int TYPE_GRAY = 1;

        public SimpleListAdapter(Context context, List<HashMap<String, String>> data, int resource, String[] from,
                int[] to) {
            super(context, data, resource, from, to);
            this.resource = resource;
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
                default:
                    break;
            }

        }

        @Override
        public int getItemViewType(int position) {
            return position % 2 == 0 ? TYPE_GRAY : TYPE_WHITE;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

    }
}
