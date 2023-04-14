package com.pax.pay.pulse;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
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

import com.pax.pay.app.FinancialApplication;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.model.ProductData;
import com.pax.pay.utils.CollectionUtils;
import com.pax.pay.utils.Controllers;
import com.pax.up.bjb.R;
import com.pax.view.dialog.CustomAlertDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import static com.pax.pay.utils.Controllers.PULSA;

public class ListPulsaFragment extends Fragment implements OnItemClickListener, OnScrollListener {

    private static final String TAG = "[ListPulsaFragment]";

    private ListView mListView;
    private View mListViewTab;

    private PulseListAdapter mAdapter;
    private RelativeLayout noTransRecordLayout;
    private RecordAsyncTask mRecordAsyncTask;
    private LinearLayout layout;


    //private boolean supportDoTrans;
    private List<ProductData> mListItems;
    private List<ProductData> mListItems2;

    private EditText phone_number;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_trans_detail_pulse_layout, null);
        mListView = (ListView) view.findViewById(R.id.list_view);
        layout = (LinearLayout)  view.findViewById(R.id.parent_layout);
        phone_number = (EditText) view.findViewById(R.id.phone_number);
        phone_number.requestFocus();
        mListViewTab = view.findViewById(R.id.list_view_tab);
        mListView.setOnItemClickListener(this);
        mListView.setOnScrollListener(this);
        noTransRecordLayout = (RelativeLayout) view.findViewById(R.id.no_trans_record_layout);
        //supportDoTrans = getArguments().getBoolean(EUIParamKeys.SUPPORT_DO_TRANS.toString(), true);

        //loadData();
        /*mListItems = Controllers.getAllProductDataByType(PULSA);
        if (CollectionUtils.isEmpty(mListItems)) {
            layout.setVisibility(View.GONE);
            noTransRecordLayout.setVisibility(View.VISIBLE);
        }else {
            //Log.d("teg", TAG+" size : "+mListItems.size());
            layout.setVisibility(View.VISIBLE);
            noTransRecordLayout.setVisibility(View.GONE);
        }*/

        /*if (mRecordAsyncTask != null) {
            Log.d("teg", TAG+" mRecordAsyncTask pop");
            mRecordAsyncTask.cancel(true);
            ActivityStack.getInstance().pop();
        }*/

        mRecordAsyncTask = new RecordAsyncTask();
        mRecordAsyncTask.execute();

        //hide a keyboard
        //phone_number.setInputType(InputType.TYPE_NULL);
        /*phone_number.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                phone_number.setInputType(InputType.TYPE_CLASS_NUMBER);
            }
        });*/

        phone_number.addTextChangedListener( new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.i("teg", "onTextChanged : " + s);
                try {
                    if (s.length() == 4) {
                        mListItems = Controllers.getAllProductDataByPrefix(s.toString());
                        mAdapter.addListToAdapter(mListItems);
                        mListView.setAdapter(mAdapter);

                    } else if (s.length() == 3) {
                        mListItems = Controllers.getAllProductDataByPrefix(s.toString());
                        mAdapter.addListToAdapter(mListItems);
                        mListView.setAdapter(mAdapter);

                    }else if(s.length() < 3 && s.length()>0){
                        mListItems = Controllers.getAllProductDataByType(PULSA);
                        mAdapter.addListToAdapter(mListItems);
                        mListView.setAdapter(mAdapter);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showErrorDialog("Silahkan Download Pulsa/Data");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        } );

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
    class RecordAsyncTask extends AsyncTask<Void, Void, List<ProductData>> {

        @Override
        protected List<ProductData> doInBackground(Void... params) {
            mListItems = Controllers.getAllProductDataByType(PULSA);
            return mListItems;
        }

        @Override
        protected void onPostExecute(List<ProductData> result) { //后
            try {

                if (!CollectionUtils.isEmpty(result)) {
                    mListViewTab.setVisibility(View.VISIBLE);
                    mAdapter = new PulseListAdapter(getActivity(), result);
                    mListView.setAdapter(mAdapter);
                }else {
                    layout.setVisibility(View.GONE);
                    noTransRecordLayout.setVisibility(View.VISIBLE);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        ProductData transData = (ProductData) mAdapter.getItem(position);
        if(phone_number.getText().toString().isEmpty()){
            showErrorDialog(getString(R.string.trans_phone_number_is_required));
            return;
        }
        if (phone_number.getText().toString().length() < 9) {
            showErrorDialog("Nomor ponsel minimal 9 karakter");
            return;
        }

        Intent intent = new Intent(getActivity(), PulseDetailsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("phone_number", phone_number.getText().toString());
        bundle.putString("mode", "Pulsa");
        bundle.putSerializable(EUIParamKeys.CONTENT.toString(), transData); //Inserts a Serializable value into the mapping of this Bundle, replacing any existing value for the given key
        intent.putExtras(bundle);
        getActivity().startActivity(intent);

    }

    private void showErrorDialog(String msg){
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
class PulseListAdapter extends BaseAdapter {

    private Context context;
    private List<ProductData> data;
    private static final int TYPE_COUNT = 2;

    public PulseListAdapter(Context context, List<ProductData> list) {
        super();
        this.context = context;
        this.data = list;
    }

    public void addListToAdapter(List<ProductData> mList) {
        data.clear();
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

        convertView = LayoutInflater.from(context).inflate(R.layout.list_item_pulse, null);
        ProductData productData = data.get(position);

        TextView productName = BaseViewHolder.get(convertView, R.id.trans_product_name);
        TextView productPrice = BaseViewHolder.get(convertView, R.id.trans_product_price);

        NumberFormat formatter = new DecimalFormat("#,###");
        String price = String.format("IDR %s",formatter.format(Double.parseDouble(productData.getSellPrice())));

        productName.setText(productData.getProductName());
        productPrice.setText(price);

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
