package com.pax.pay.record;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pax.abl.utils.PanUtils;
import com.pax.pay.app.ActivityStack;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.constant.Constants;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.record.DetailsActivity.Options;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.utils.CollectionUtils;
import com.pax.pay.utils.TimeConverter;
import com.pax.settings.currency.Currency;
import com.pax.up.bjb.R;

import java.text.SimpleDateFormat;
import java.util.List;

public class TransDetailFragment extends Fragment implements OnItemClickListener, OnScrollListener {

    private static final String TAG = "TransDetailFragment";

    private ListView mListView;
    private View mListViewTab;

    private RecordListAdapter mAdapter;
    private RelativeLayout noTransRecordLayout;
    private RecordAsyncTask mRecordAsyncTask;

    private boolean supportDoTrans;
    /**
     * 每次加载的条数
     */
    private int loadItem = 7;
    /**
     * 总交易list集合
     */
    private List<TransData> mListItems;

    private int offset = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_trans_detail_layout, null);
        mListView = (ListView) view.findViewById(R.id.list_view);
        mListViewTab = view.findViewById(R.id.list_view_tab);
        mListView.setOnItemClickListener(this);
        mListView.setOnScrollListener(this);
        noTransRecordLayout = (RelativeLayout) view.findViewById(R.id.no_trans_record_layout);
        supportDoTrans = getArguments().getBoolean(EUIParamKeys.SUPPORT_DO_TRANS.toString(), true);
        
        if (mRecordAsyncTask != null) {
            mRecordAsyncTask.cancel(true);
            ActivityStack.getInstance().pop();
        }
        mRecordAsyncTask = new RecordAsyncTask();
        mRecordAsyncTask.execute();
        
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mRecordAsyncTask != null) {
            mRecordAsyncTask.cancel(true);
        }
        mRecordAsyncTask = null;
    }

    // 使用Task异步加载数据填充ListView
    class RecordAsyncTask extends AsyncTask<Void, Void, List<TransData>> {

        @Override
        protected List<TransData> doInBackground(Void... params) {
            //return TransData.readTransByLimitNo(loadItem, offset); //asli

            //tri
            String[] condition = new String[] {
                    ETransType.PDAM_OVERBOOKING.toString(),
                    ETransType.PASCABAYAR_OVERBOOKING.toString()
            }; //kecuali trans ini
            List<TransData> result =  TransData.readTransByLimit(loadItem, offset, condition);

            return result;

        }

        @Override
        protected void onPostExecute(List<TransData> result) { //后
            if (CollectionUtils.isEmpty(result)) {
                mListView.setVisibility(View.GONE);
                noTransRecordLayout.setVisibility(View.VISIBLE);
                return;
            }

            /**
            List<TransData> tmpResult = result;
            for(final TransData t : tmpResult){
                Log.d(TAG,String.format("Sandy.TransDetail : %s SaleDateTimeTrans : %s CouponRefNo : %s CouponDateTimeTrans %s",
                        t.getTransNo(),
                        t.getDateTimeTrans(),
                        t.getOrigCouponRefNo(),
                        t.getOrigCouponDateTimeTrans()));
            }
            **/


            mListViewTab.setVisibility(View.VISIBLE);
            //Collections.reverse(result);// 把list倒序，使最新一条记录在最上
            offset+=result.size();
            mListItems = result;
            if (mAdapter == null) {
                mAdapter = new RecordListAdapter(getActivity(), mListItems);
                mListView.setAdapter(mAdapter);
            } else {
                if (CollectionUtils.isEmpty(mListItems)) {
                    return;
                }
                mAdapter.addListToAdapter(mListItems);
                mAdapter.notifyDataSetChanged();
            }

        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        TransData transData = (TransData) mAdapter.getItem(position);
        // 交易详情
        Intent intent = new Intent(getActivity(), DetailsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(EUIParamKeys.NAV_TITLE.toString(), getString(R.string.trans_detail));
        bundle.putBoolean(EUIParamKeys.NAV_BACK.toString(), true);
        bundle.putSerializable(EUIParamKeys.CONTENT.toString(), transData); //Inserts a Serializable value into the mapping of this Bundle, replacing any existing value for the given key
        bundle.putBoolean(EUIParamKeys.SUPPORT_DO_TRANS.toString(), supportDoTrans);
        bundle.putString(EUIParamKeys.OPTIONS.toString(),Options.INTNET.toString());
        intent.putExtras(bundle);

        getActivity().startActivity(intent);

    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (view.getLastVisiblePosition() == (view.getCount() - 1) && scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
            mListItems = TransData.readTransByLimitNo(loadItem, offset);
            offset+=mListItems.size();
            mAdapter.addListToAdapter(mListItems);
            mAdapter.notifyDataSetChanged();

        }

    }

    @Override
    public void onScroll(AbsListView view, int firstItem, int visibleItem, int totalItem) {
        // Do nothing.
    }

}

@SuppressWarnings("ResourceType")
class RecordListAdapter extends BaseAdapter {

    private Context context;
    private List<TransData> data;
    private static final int TYPE_COUNT = 2;
    private static final int TYPE_DEFAULT = 0;
    private static final int TYPE_QR = 1;

    public RecordListAdapter(Context context, List<TransData> list) {
        super();
        this.context = context;
        this.data = list;
    }

    public void addListToAdapter(List<TransData> mList) {
        data.addAll(mList); //Adds the objects in the specified collection to the end of this List.
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        String transType = data.get(position).getTransType();
        if (transType.equals(ETransType.QR_SALE.toString()) ||
                transType.equals(ETransType.QR_VOID.toString()) ||
                transType.equals(ETransType.QR_REFUND.toString())) {
            return TYPE_QR;
        }
        return TYPE_DEFAULT;

    }

    @Override
    public int getViewTypeCount() {
        return TYPE_COUNT;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        switch (getItemViewType(position)) {
            case TYPE_DEFAULT:
                if (convertView == null) {
                    convertView = LayoutInflater.from(context).inflate(R.layout.list_item_new, null);
                } else if (convertView.getId() != R.layout.list_item) {
                    convertView = LayoutInflater.from(context).inflate(R.layout.list_item_new, null);
                }
                break;
            case TYPE_QR:
                if (convertView == null) {
                    convertView = LayoutInflater.from(context).inflate(R.layout.list_item_qr, null);
                } else if (convertView.getId() != R.layout.list_item_qr) {
                    convertView = LayoutInflater.from(context).inflate(R.layout.list_item_qr, null);
                }
                break;
            default:
                if (convertView == null) {
                    convertView = LayoutInflater.from(context).inflate(R.layout.list_item_new, null);
                }
                break;
        }

        TextView transTypeTv = BaseViewHolder.get(convertView, R.id.trans_type_tv);
        TextView transAmountTv = BaseViewHolder.get(convertView, R.id.trans_amount_tv);
        TextView authCodeTv = BaseViewHolder.get(convertView, R.id.auth_code_tv);
        TextView cardNoTv = BaseViewHolder.get(convertView, R.id.card_no_or_pay_code_tv);
        TextView transNoTv = BaseViewHolder.get(convertView, R.id.trans_no_tv);
        TextView transDateTv = BaseViewHolder.get(convertView, R.id.trans_date_tv);

        TransData transData = data.get(position);
        String transType = transData.getTransType();
        transTypeTv.setText(ETransType.valueOf(transType).getTransName());

        Currency currency = FinancialApplication.getSysParam().getCurrency();

        String amount = null;

        if(ETransType.COUPON_SALE.toString().equals(transType) ||
                ETransType.COUPON_SALE_VOID.toString().equals(transType) ){
            amount = FinancialApplication.getConvert().amountMinUnitToMajor(
                    String.valueOf(Long.parseLong(transData.getActualPayAmount())),
                    currency.getCurrencyExponent(), true);
        } else if (ETransType.INQ_PULSA_DATA.toString().equals(transType) ||
                ETransType.PURCHASE_PULSA_DATA.toString().equals(transType)) {
            amount = FinancialApplication.getConvert().amountMinUnitToMajor(
                    String.valueOf(Long.parseLong(transData.getSellPrice())),
                    currency.getCurrencyExponent(), true);
        } else {
            amount = FinancialApplication.getConvert().amountMinUnitToMajor(
                    String.valueOf(Long.parseLong(transData.getAmount())),
                    currency.getCurrencyExponent(), true);

        }



        if (Component.isDebitTransaction(ETransType.valueOf(transType))) {
            transAmountTv.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark));
            transAmountTv.setText("+" + amount);
        } else  {
            transAmountTv.setTextColor(context.getResources().getColor(R.color.trans_amount_color));
            transAmountTv.setText("-" + amount);
        }

        authCodeTv.setText(transData.getAuthCode());

        String cardNo = "";

        if (transType.equals(ETransType.AUTH.toString()) || transType.equals(ETransType.EC_SALE.toString())) {
            cardNo = transData.getPan();
        } else if (transType.equals(ETransType.QR_SALE.toString()) || transType.equals(ETransType.QR_VOID.toString())
                || transType.equals(ETransType.QR_REFUND.toString())) {
            authCodeTv.setText("");
            if (transData.getC2b() != null && transData.getC2b().length() > 0) {
                cardNo = PanUtils.maskedCardNo(ETransType.valueOf(transType), transData.getC2b());
            }
        }else if ( transType.equals(ETransType.INQ_PULSA_DATA.toString()) ||
                   transType.equals(ETransType.PASCABAYAR_INQUIRY.toString()) ||
                    transType.equals(ETransType.PDAM_INQUIRY.toString()) ||
                    transType.equals(ETransType.PURCHASE_PULSA_DATA.toString()) ||
                    transType.equals(ETransType.OVERBOOKING_PULSA_DATA.toString())) {
            setProductCode(transData);
            cardNo = PanUtils.maskedString(transData.getPhoneNo(),11);
         }else {
            cardNo = PanUtils.maskedCardNo(ETransType.valueOf(transType), transData.getPan());
            if (!transData.getIsOnlineTrans()) { //是否为联机交易
                cardNo = transData.getPan();
            }
        }
        cardNoTv.setText(cardNo);
        transNoTv.setText(String.format("%06d", transData.getTransNo()));

        String date = transData.getDate();
        String time = transData.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        String yearDate = sdf.format(new java.util.Date());

        String temp = yearDate.substring(0, 4) + "/" + date.substring(0, 2) + "/" + date.substring(2, 4) + " "
                + time.substring(0, 2) + ":" + time.substring(2, 4) + ":" + time.substring(4);
        String formattedDate = TimeConverter.convert(temp, Constants.TIME_PATTERN_DISPLAY,
                Constants.TIME_PATTERN_DISPLAY2);
        transDateTv.setText(formattedDate);

        return convertView;
    }

    static class BaseViewHolder {
        @SuppressWarnings("unchecked")
        public static <T extends View> T get(View view, int id) {

            SparseArray<View> viewHolder = (SparseArray<View>) view.getTag();

            if (viewHolder == null) {
                viewHolder = new SparseArray<>();
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


    //Sandy : for PLN purpose
    private void setProductCode(TransData transData){
        String[] f47 = transData.getField47().split("#");
        transData.setField48(f47[0]);
        transData.setPhoneNo(f47[1]);
        transData.setProduct_code(f47[2]);
        transData.setTypeProduct(f47[3]);
        transData.setOperator(f47[4]);
        transData.setKeterangan(f47[5]);
        transData.setProduct_name(f47[6]);

    }



}
