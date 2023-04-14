package com.pax.pay.pulse;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pax.pay.app.ActivityStack;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.model.ProductData;
import com.pax.pay.utils.CollectionUtils;
import com.pax.up.bjb.R;
import com.pax.view.dialog.CustomAlertDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class ListDataFragment extends Fragment implements OnItemClickListener, OnScrollListener {

    private static final String TAG = "[ListDataFragment]";

    private ListView mListView;
    //private View mListViewTab;
    private LinearLayout layout;

    private DataListAdapter mAdapter;
    private RelativeLayout noTransRecordLayout;
    private RecordAsyncTask mRecordAsyncTask;

    /**
     * 每次加载的条数
     */
    private int loadItem = 7;
    /**
     * 总交易list集合
     */
    private List<ProductData> mListItems;


    private EditText phone_number;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_trans_detail_data_layout, null);
        mListView = (ListView) view.findViewById(R.id.list_view);

        //mListViewTab = view.findViewById(R.id.list_view_tab);
        phone_number = (EditText) view.findViewById(R.id.phone_number);
        phone_number.requestFocus();
        layout = (LinearLayout) view.findViewById(R.id.parent_layout);
        mListView.setOnItemClickListener(this);
        mListView.setOnScrollListener(this);
        noTransRecordLayout = (RelativeLayout) view.findViewById(R.id.no_trans_record_layout);

        /*mListItems = getAllPaketData();
        if (CollectionUtils.isEmpty(mListItems)) {
            layout.setVisibility(View.GONE);
            noTransRecordLayout.setVisibility(View.VISIBLE);
        }else {
            //Log.d("teg", TAG+" size : "+mListItems.size());
            layout.setVisibility(View.VISIBLE);
            noTransRecordLayout.setVisibility(View.GONE);
        }*/

        /*if (mRecordAsyncTask != null) {
            mRecordAsyncTask.cancel(true);
            ActivityStack.getInstance().pop();
        }*/

        mRecordAsyncTask = new RecordAsyncTask();
        mRecordAsyncTask.execute();

        phone_number.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                Log.i("abdul", "beforeTextChanged = " + s);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                Log.i("abdul", "onTextChanged = " + s);
                // edit di after
                try {
                    if (s.length() == 4) {
                        mListItems = new ArrayList();
                        mListItems.clear();
                        mAdapter.addListToAdapter(mListItems);
                        mAdapter.notifyDataSetChanged();
                        mAdapter = new DataListAdapter(getActivity(), mListItems);
                        mListView.setAdapter(mAdapter);
                        JSONObject dat;
                        try {
                            String data = FinancialApplication.getSysParam().get("prefix");
//                        Log.i("abdul", "cek prefix = " + data);
                            dat = new JSONObject(data);
                            for (int i = 0; i < dat.length(); i++) {
                                JSONObject par = dat.getJSONObject("body" + i);
                                String operator = par.getString("operator");
                                String nopref = par.getString("prefix");
                                boolean pref = nopref.contains(s.toString());
                                if (pref) {
                                    refreshData(operator);
                                    break;
                                }
                            }
                        } catch (JSONException | IndexOutOfBoundsException e) {
                            e.printStackTrace();
                        }
                    } else if (s.length() < 4) {
                        try {
                            mListItems = new ArrayList();
                            mListItems.clear();
                            mAdapter.addListToAdapter(mListItems);
                            mAdapter.notifyDataSetChanged();
                            mAdapter = new DataListAdapter(getActivity(), mListItems);
                            mListView.setAdapter(mAdapter);
                            loadData();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showErrorDialog("Silahkan Download Pulsa/Data");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
//                Log.i("abdul", "afterTextChanged = " + s);
            }
        });
        return view;
    }

    private void refreshData(String prefix) {
        mListItems = new ArrayList();
        mListItems.clear();
        mAdapter.addListToAdapter(mListItems);
        mAdapter.notifyDataSetChanged();
        mAdapter = new DataListAdapter(getActivity(), mListItems);
        mListView.setAdapter(mAdapter);

        mListItems = new ArrayList();
        JSONObject dat;
        try {
            String data = FinancialApplication.getSysParam().get("download");
//            Log.i("abdul", "cek data = " + data);
            dat = new JSONObject(data);
            for (int i = 0; i < dat.length(); i++) {
                JSONObject par = dat.getJSONObject("body" + i);
                String productId = par.getString("productId");
                String productName = par.getString("productName");
                String productDesc = par.getString("productDesc");
                String operator = par.getString("operator");
                String basePrice = par.getString("basePrice");
                basePrice = basePrice.substring(0, basePrice.length() - 2);
                Long base = Long.parseLong(basePrice);
                basePrice = String.valueOf(base);
                String sellPrice = par.getString("sellPrice");
                sellPrice = sellPrice.substring(0, sellPrice.length() - 2);
                Long sell = Long.parseLong(sellPrice);
                sellPrice = String.valueOf(sell);
                String fee = par.getString("fee");
                fee = fee.substring(0, fee.length() - 2);
                Long feeL = Long.parseLong(fee);
                fee = String.valueOf(feeL);
                String type = par.getString("type");
                type = type.toUpperCase().trim();
                prefix = prefix.toUpperCase().trim();
                operator = operator.toUpperCase().trim();

                if (type.equals("DATA") && prefix.equals(operator))
                    mListItems.add(new ProductData(productId, type, productName, productDesc, operator, basePrice, sellPrice, fee));
//                Log.i("abdul", "cek json = " + dat);
            }
        } catch (IndexOutOfBoundsException | NullPointerException | JSONException e) {
            e.printStackTrace();
        }
        try {
            mAdapter.addListToAdapter(mListItems);
            mAdapter.notifyDataSetChanged();
            mListView.setAdapter(mAdapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    List<ProductData> getAllPaketData() {
        List<ProductData> list = new ArrayList<>();
        JSONObject dat;
        ProductData productData;
        String data = FinancialApplication.getSysParam().get("download");

        if (TextUtils.isEmpty(data)) return null;

        try {
            dat = new JSONObject(data);
            for (int i = 0; i < dat.length(); i++) {
                JSONObject par = dat.getJSONObject("body" + i);
                String type = par.getString("type");
                type = type.trim();
                if (type.equalsIgnoreCase("DATA")) {

                    String productId = par.getString("productId");
                    String productName = par.getString("productName").trim();
                    String productDesc = par.getString("productDesc").trim();
                    String operator = par.getString("operator");
                    String basePrice = par.getString("basePrice");
                    basePrice = basePrice.substring(0, basePrice.length() - 2);
                    Long base = Long.parseLong(basePrice);
                    basePrice = String.valueOf(base);
                    String sellPrice = par.getString("sellPrice");
                    sellPrice = sellPrice.substring(0, sellPrice.length() - 2);
                    Long sell = Long.parseLong(sellPrice);
                    sellPrice = String.valueOf(sell);
                    String fee = par.getString("fee");
                    fee = fee.substring(0, fee.length() - 2);
                    Long feeL = Long.parseLong(fee);
                    fee = String.valueOf(feeL);
                    operator = operator.toUpperCase().trim();

                    productData = new ProductData(productId, type, productName, productDesc, operator, basePrice, sellPrice, fee);
//                    Log.i("teg", "-->" + productData.toString());
                    list.add(productData);
                }
            }

        } catch (IndexOutOfBoundsException | NullPointerException | JSONException e) {
            e.printStackTrace();
        }

        return list;
    }

    private void loadData() {
        //sandy :
        //Load a data here.....
        mListItems = new ArrayList();
        JSONObject dat;
        try {
            String data = FinancialApplication.getSysParam().get("download");
//            Log.i("abdul", "cek data = " + data);
            dat = new JSONObject(data);
            for (int i = 0; i < dat.length(); i++) {
                JSONObject par = dat.getJSONObject("body" + i);
                String productId = par.getString("productId");
                String productName = par.getString("productName");
                String productDesc = par.getString("productDesc");
                String operator = par.getString("operator");
                operator = operator.toUpperCase();
                String basePrice = par.getString("basePrice");
                basePrice = basePrice.substring(0, basePrice.length() - 2);
                Long base = Long.parseLong(basePrice);
                basePrice = String.valueOf(base);
                String sellPrice = par.getString("sellPrice");
                sellPrice = sellPrice.substring(0, sellPrice.length() - 2);
                Long sell = Long.parseLong(sellPrice);
                sellPrice = String.valueOf(sell);
                String fee = par.getString("fee");
                fee = fee.substring(0, fee.length() - 2);
                Long feeL = Long.parseLong(fee);
                fee = String.valueOf(feeL);
                String type = par.getString("type");
                type = type.toUpperCase().trim();
                if (type.equals("DATA"))
                    mListItems.add(new ProductData(productId, type, productName, productDesc, operator, basePrice, sellPrice, fee));
//                Log.i("abdul", "cek json = " + dat);
            }
        } catch (IndexOutOfBoundsException | NullPointerException | JSONException e) {
            e.printStackTrace();
        }
        try {
            mAdapter.addListToAdapter(mListItems);
            mAdapter.notifyDataSetChanged();
            mListView.setAdapter(mAdapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showErrorDialog(String msg) {
        CustomAlertDialog dialog = new CustomAlertDialog(getActivity(), CustomAlertDialog.ERROR_TYPE);
        dialog.setContentText(msg);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        dialog.showCancelButton(false);
        dialog.showConfirmButton(true);
        dialog.setConfirmClickListener(new CustomAlertDialog.OnCustomClickListener() {
            @Override
            public void onClick(CustomAlertDialog alertDialog) {
                alertDialog.dismiss();

            }
        });
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
    class RecordAsyncTask extends AsyncTask<Void, Void, List<ProductData>> {

        @Override
        protected List<ProductData> doInBackground(Void... params) {
            mListItems = getAllPaketData();
            return mListItems;
        }

        @Override
        protected void onPostExecute(List<ProductData> result) { //后

            try {
                if (!CollectionUtils.isEmpty(result)) {
                    mAdapter = new DataListAdapter(getActivity(), result);
                    mListView.setAdapter(mAdapter);
                } else {
                    layout.setVisibility(View.GONE);
                    noTransRecordLayout.setVisibility(View.VISIBLE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        ProductData transData = (ProductData) mAdapter.getItem(position);
        if (phone_number.getText().toString().isEmpty()) {
            showErrorDialog(getString(R.string.trans_phone_number_is_required));
            return;
        }

        if (phone_number.getText().toString().length() < 9) {
            showErrorDialog("Nomor ponsel minimal 9 karakter");
            return;
        }

        // 交易详情
        Intent intent = new Intent(getActivity(), PulseDetailsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("phone_number", phone_number.getText().toString());
        bundle.putString("mode", "Data");
        bundle.putSerializable(EUIParamKeys.CONTENT.toString(), transData); //Inserts a Serializable value into the mapping of this Bundle, replacing any existing value for the given key
        intent.putExtras(bundle);

        getActivity().startActivity(intent);

    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (view.getLastVisiblePosition() == (view.getCount() - 1) && scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
            //mListItems = TransData.readTransByLimitNo(loadItem, offset);
            /*
            offset+=mListItems.size();
            mAdapter.addListToAdapter(mListItems);
            mAdapter.notifyDataSetChanged();
            */
        }

    }

    @Override
    public void onScroll(AbsListView view, int firstItem, int visibleItem, int totalItem) {
        // Do nothing.
    }

}

@SuppressWarnings("ResourceType")
class DataListAdapter extends BaseAdapter {

    private Context context;
    private List<ProductData> data;
    private static final int TYPE_COUNT = 2;

    public DataListAdapter(Context context, List<ProductData> list) {
        super();
        this.context = context;
        this.data = list;
    }

    public void addListToAdapter(List<ProductData> mList) {
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
    public int getViewTypeCount() {
        return TYPE_COUNT;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        convertView = LayoutInflater.from(context).inflate(R.layout.list_item_data, null);
        ProductData productData = data.get(position);

        TextView productName = BaseViewHolder.get(convertView, R.id.trans_product_name);
        TextView productDescription = BaseViewHolder.get(convertView, R.id.trans_product_description);
        TextView productPrice = BaseViewHolder.get(convertView, R.id.trans_product_price);
        TextView operator = BaseViewHolder.get(convertView, R.id.trans_operator);


        NumberFormat formatter = new DecimalFormat("#,###");
        String price = String.format("IDR %s", formatter.format(Double.parseDouble(productData.getSellPrice())));

        operator.setText(productData.getOperator());
        productName.setText(productData.getProductName());
        productPrice.setText(price);
        productDescription.setText(productData.getProductDescription());

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
}
