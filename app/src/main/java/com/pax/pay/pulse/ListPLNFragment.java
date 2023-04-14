package com.pax.pay.pulse;

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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.model.ProductData;
import com.pax.pay.utils.CollectionUtils;
import com.pax.pay.utils.Controllers;
import com.pax.up.bjb.R;
import com.pax.view.dialog.CustomAlertDialog;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import static com.pax.pay.utils.Controllers.PLN;

public class ListPLNFragment extends Fragment implements OnItemClickListener, OnScrollListener {

    private static final String TAG = "[ListPLNFragment]";
    private ListView mListView;
    private View mListViewTab;
    private PLNListAdapter mAdapter;
    private RelativeLayout noTransRecordLayout;
    private RecordAsyncTask mRecordAsyncTask;
    private LinearLayout layout;
    private List<ProductData> mListItems;
    private EditText phone_number;
    String nomer = "";
    String kode = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_trans_detail_pln_layout, null);
        layout = (LinearLayout) view.findViewById(R.id.parent_layout);
        layout.clearFocus();
        mListView = (ListView) view.findViewById(R.id.list_view);
        phone_number = (EditText) view.findViewById(R.id.phone_number);
        phone_number.setHint("Input Billing Id...");
        mListViewTab = view.findViewById(R.id.list_view_tab);
        mListView.setOnItemClickListener(this);
        mListView.setOnScrollListener(this);
        noTransRecordLayout = (RelativeLayout) view.findViewById(R.id.no_trans_record_layout);

        /*mListItems = Controllers.getAllProductDataByType(PLN);
        if (CollectionUtils.isEmpty(mListItems)) {
            layout.setVisibility(View.GONE);
            noTransRecordLayout.setVisibility(View.VISIBLE);
        } else {
            layout.setVisibility(View.VISIBLE);
            noTransRecordLayout.setVisibility(View.GONE);
        }*/

        /*if (mRecordAsyncTask != null) {
            mRecordAsyncTask.cancel(true);
            Log.d("teg", TAG+" mRecordAsyncTask pop");
            ActivityStack.getInstance().pop();
        }*/

        mRecordAsyncTask = new RecordAsyncTask();
        mRecordAsyncTask.execute();


        /*phone_number.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });*/

        return view;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mRecordAsyncTask != null) {
            Log.d("teg", TAG + " onDestroy");
            mRecordAsyncTask.cancel(true);
        }
        mRecordAsyncTask = null;
    }

    class RecordAsyncTask extends AsyncTask<Void, Void, List<ProductData>> {

        @Override
        protected List<ProductData> doInBackground(Void... params) {
            mListItems = Controllers.getAllProductDataByType(PLN);
            return mListItems;
        }

        @Override
        protected void onPostExecute(List<ProductData> result) { //后

            try {
                if (!CollectionUtils.isEmpty(result)) {
                    mListViewTab.setVisibility(View.VISIBLE);
                    mAdapter = new PLNListAdapter(getActivity(), result);
                    mListView.setAdapter(mAdapter);
                }else {
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
        nomer = phone_number.getText().toString().trim();
        if (phone_number.getText().toString().isEmpty()) {
            showErrorDialog(getString(R.string.input_billing));
            return;
        }

        if (phone_number.getText().toString().length() < 11) {
            showErrorDialog("Billing Id minimal 11 karakter");
            return;
        }

        ProductData productData = (ProductData) mAdapter.getItem(position);

        kode = getPLNCodeByDescName(productData.getProductDescription());
        Log.d("teg", "nomer " + nomer + " desc : " + productData.getProductDescription() + " kode pln " + kode);
        Log.d("teg", "-->" + productData.toString());

        Intent intent = new Intent(getActivity(), PulseDetailsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("mode", "pln");
        bundle.putString("kodeproduk", kode);
        bundle.putString("phone_number", phone_number.getText().toString());
        bundle.putSerializable(EUIParamKeys.CONTENT.toString(), productData);
        intent.putExtras(bundle);
        getActivity().startActivity(intent);

    }

    static String arrPLN[][] = {
            {"PLN20A", "PLN 20000"},
            {"PLN50A", "PLN 50000"},
            {"PLN100A", "PLN 100000"},
            {"PLN200A", "PLN 200000"},
            {"PLN500A", "PLN 500000"},
            {"PLN1000A", "PLN 1JUTA"}

    };

    String getPLNCodeByDescName(String descName) {
        String code = "";
        for (int i = 0; i < arrPLN.length; i++) {
            if (descName.equalsIgnoreCase(arrPLN[i][1])) {
                code = arrPLN[i][0];
                return code;
            }
        }
        return code;
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
class PLNListAdapter extends BaseAdapter {

    private Context context;
    private List<ProductData> data;
    private static final int TYPE_COUNT = 2;

    public PLNListAdapter(Context context, List<ProductData> list) {
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

        convertView = LayoutInflater.from(context).inflate(R.layout.list_item_pln, null);
        ProductData productData = data.get(position);

        TextView productName = BaseViewHolder.get(convertView, R.id.trans_product_name);
        TextView productPrice = BaseViewHolder.get(convertView, R.id.trans_product_price);

        NumberFormat formatter = new DecimalFormat("#,###");
        String price = String.format("IDR %s", formatter.format(Double.parseDouble(productData.getSellPrice())));

        productName.setText(productData.getProductName());
        productPrice.setText(price);

        //Log.d("teg", "data "+" name "+productData.getProductName()+" price "+price);

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
