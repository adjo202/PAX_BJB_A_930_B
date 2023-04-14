package com.pax.pay.trans.action.activity;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.pax.abl.core.ActionResult;
import com.pax.eemv.entity.EcRecord;
import com.pax.pay.BaseActivityWithTickForAction;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.TransResult;
import com.pax.settings.currency.Currency;
import com.pax.up.bjb.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Steven.W
 * 
 */
public class DispEcRecordActivity extends BaseActivityWithTickForAction implements OnClickListener {

    private ImageView ivBack;
    private String navTitle;
    private boolean navBack;

    private ArrayList<EcRecord> ecRecordList = new ArrayList<EcRecord>();

    @Override
    protected void loadParam() {
        Bundle bundle = getIntent().getExtras();
        navTitle = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());
        navBack = getIntent().getBooleanExtra(EUIParamKeys.NAV_BACK.toString(), false);
        ecRecordList = bundle.getParcelableArrayList(EUIParamKeys.ARRAY_LIST_1.toString());
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_ec_detail_layout;
    }

    @Override
    protected void initViews() {
        TextView tvTitle = (TextView) findViewById(R.id.header_title);
        tvTitle.setText(navTitle);
        ivBack = (ImageView) findViewById(R.id.header_back);

        TextView tvTotalNum = (TextView) findViewById(R.id.total_num);
        String totalContent = getString(R.string.ec_log_total_num,
                String.valueOf(ecRecordList.size()));
        SpannableString spanString = new SpannableString(totalContent);
        spanString.setSpan(new ForegroundColorSpan(Color.parseColor("#FD4800")),
                totalContent.indexOf(':') + 1, totalContent.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvTotalNum.setText(spanString);

        ListView mListView = (ListView) findViewById(R.id.list_view);
        EcRecordListAdapter mAdapter = new EcRecordListAdapter(DispEcRecordActivity.this,
                ecRecordList);
        mListView.setAdapter(mAdapter);

    }

    @Override
    protected void setListeners() {
        if (!navBack) {
            ivBack.setVisibility(View.GONE);
        } else {
            ivBack.setOnClickListener(this);
        }
    }

    @Override
    protected void handleMsg(Message msg) {

    }

    @Override
    public void onClick(View v) {
        ActionResult result = null;
        switch (v.getId()) {
            case R.id.header_back:
                result = new ActionResult(TransResult.ERR_ABORTED, null);
                finish(result);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            ActionResult result = new ActionResult(TransResult.ERR_ABORTED, null);
            finish(result);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}

class EcRecordListAdapter extends BaseAdapter {

    private Context context;
    private List<EcRecord> list;

    private Currency mCurrency;

    public EcRecordListAdapter(Context context, List<EcRecord> list) {
        super();
        this.context = context;
        this.list = list;
        this.mCurrency = FinancialApplication.getSysParam().getCurrency();
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
            convertView = LayoutInflater.from(context).inflate(R.layout.ec_list_item, null);
        }

        TextView transTypeTv = BaseViewHolder.get(convertView, R.id.ec_trans_type);
        TextView dateTv = BaseViewHolder.get(convertView, R.id.ec_date);
        TextView timeTv = BaseViewHolder.get(convertView, R.id.ec_time);
        TextView countryTv = BaseViewHolder.get(convertView, R.id.ec_country_code);
        TextView currencyTv = BaseViewHolder.get(convertView, R.id.ec_currency_code);
        TextView authAmountTv = BaseViewHolder.get(convertView, R.id.ec_trans_amount);
        TextView otherAmountTv = BaseViewHolder.get(convertView, R.id.ec_other_amount);
        TextView atcTv = BaseViewHolder.get(convertView, R.id.ec_atc);

        EcRecord record = list.get(position);

        String transType = record.getTransType();
        if (transType != null && transType.length() > 0) {
            transTypeTv.setText(transType);
        } else {
            transTypeTv.setText("");
        }

        String date = record.getDate(); // 日期格式为YYMMDD

        String time = record.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        String yearDate = sdf.format(new java.util.Date());

        if (date != null && date.length() > 0) {
            String dateTmp = yearDate.substring(0, 2) + date.substring(0, 2) + "/" + date.substring(2, 4) + "/"
                    + date.substring(4);
            dateTv.setText(dateTmp);
        } else {
            dateTv.setText("");
        }
        if (time != null && time.length() > 0) {
            String timeTmp = time.substring(0, 2) + ":" + time.substring(2, 4) + ":" + time.substring(4);
            timeTv.setText(timeTmp);
        } else {
            timeTv.setText("");
        }

        String countryCode = record.getCountryCode();
        if (countryCode != null && countryCode.length() > 0) {
            countryTv.setText(countryCode);
        } else {
            countryTv.setText("");
        }

        String currencyCode = record.getCurrencyCode();
        if (currencyCode != null && currencyCode.length() > 0) {
            currencyTv.setText(currencyCode);
        } else {
            currencyTv.setText("");
        }

        if (record.getAuthAmount() != null && record.getAuthAmount().length() > 0) {
            String amount = FinancialApplication.getConvert()
                    .amountMinUnitToMajor(String.valueOf(Long.parseLong(record.getAuthAmount())),
                            mCurrency.getCurrencyExponent(), true);
            authAmountTv.setText(amount);
        } else {
            authAmountTv.setText("");
        }

        if (record.getOtherAmount() != null && record.getOtherAmount().length() > 0) {
            String otherAmount = FinancialApplication.getConvert().amountMinUnitToMajor(
                    String.valueOf(Long.parseLong(record.getOtherAmount())),
                    mCurrency.getCurrencyExponent(), true);
            otherAmountTv.setText(otherAmount);
        } else {
            otherAmountTv.setText("");
        }

        String atc = record.getAtc();
        if (atc != null && atc.length() > 0) {
            atcTv.setText(atc);
        } else {
            atcTv.setText("");
        }

        return convertView;
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
