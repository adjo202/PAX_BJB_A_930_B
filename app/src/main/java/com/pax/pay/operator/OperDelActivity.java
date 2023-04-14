package com.pax.pay.operator;

import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.pax.pay.BaseActivity;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.TransContext;
import com.pax.pay.utils.ToastUtils;
import com.pax.up.bjb.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 删除操作员
 * 
 * @author Steven.W
 * 
 */
public class OperDelActivity extends BaseActivity implements OnClickListener {

    private ImageView ivBack;

    private GridView mGridView;
    private Button btnConfirm;

    private SimpleAdapter mAdapter;

    private List<HashMap<String, Object>> dispOperList = new ArrayList<>();

    private String navTitle;
    private static final int MSG_SUCCESS = 0;

    @Override
    protected void loadParam() {
        navTitle = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());
    }

    @Override
    protected int getLayoutId() {
        return R.layout.del_oper_layout;
    }

    @Override
    protected void initViews() {
        TextView tvTitle = (TextView) findViewById(R.id.header_title);
        tvTitle.setText(navTitle);
        ivBack = (ImageView) findViewById(R.id.header_back);
        mGridView = (GridView) findViewById(R.id.grid_delete);
        btnConfirm = (Button) findViewById(R.id.oper_confirm);

        String curOperId = TransContext.getInstance().getOperID();
        List<Operator> operList = Operator.findAll();
        for (Operator oper : operList) {
            if ("00".equals(oper.getOperId()) || "99".equals(oper.getOperId()))
                continue;
            HashMap<String, Object> map = new HashMap<>();
            if (!oper.getOperId().equals(curOperId)) {
                map.put("oper", oper.getOperId());
                dispOperList.add(map);
            }
        }

        mAdapter = new SimpleAdapter(OperDelActivity.this, dispOperList, R.layout.manager_delete_oper_item,
                new String[] { "oper" }, new int[] { R.id.oper_del_check });
        mGridView.setAdapter(mAdapter);
    }

    @Override
    protected void setListeners() {
        ivBack.setOnClickListener(this);
        btnConfirm.setOnClickListener(this);

    }

    @Override
    protected void handleMsg(Message msg) {
        switch (msg.what) {
            case MSG_SUCCESS:
                //保持信息提示一致
                ToastUtils.showMessage(OperDelActivity.this, getString(R.string.delete_succ));
                break;
            default:
                break;
        }

    }

    @Override
    public void onClick(View v) {
        if (quickClickProtection.isStarted()) {
            return;
        }
        quickClickProtection.start();
        switch (v.getId()) {
            case R.id.header_back:
                finish();
                break;
            case R.id.oper_confirm:
                checkOperator();
                quickClickProtection.stop();
                break;

            default:
                quickClickProtection.stop();
                break;
        }

    }

    private void checkOperator() {
        boolean hasDel = false;
        int delNum = 0;
        int total = mGridView.getChildCount();

        for (int i = 0; i < total; i++) {
            View view = mGridView.getChildAt(i);
            CheckBox mCheckBox = (CheckBox) view.findViewById(R.id.oper_del_check);

            if (mCheckBox.isChecked()) {
                String operId = mCheckBox.getText().toString();
                boolean isSuccess = Operator.delete(operId);

                if (isSuccess) {
                    hasDel = true;
                    dispOperList.remove(i - delNum);
                    delNum++;
                } else {
                    ToastUtils.showMessage(OperDelActivity.this, getString(R.string.delete_fail));
                }

            }
        }

        if (!hasDel) {
            ToastUtils.showMessage(OperDelActivity.this, getString(R.string.please_choose_delete_oper));
        } else {
            Message.obtain(handler, MSG_SUCCESS).sendToTarget();
        }

        mAdapter.notifyDataSetChanged();
        int rcnt = mGridView.getChildCount();
        for (int i = 0; i < rcnt; i++) {
            View view = mGridView.getChildAt(i);
            CheckBox mCheckBox = (CheckBox) view.findViewById(R.id.oper_del_check);
            mCheckBox.setChecked(false);
        }
    }
}
