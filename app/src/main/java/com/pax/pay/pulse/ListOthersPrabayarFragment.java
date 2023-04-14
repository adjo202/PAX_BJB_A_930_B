package com.pax.pay.pulse;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pax.pay.app.ActivityStack;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.action.activity.InputPDAMDataActivity;
import com.pax.pay.trans.model.Controller;
import com.pax.pay.trans.model.ProductData;
import com.pax.pay.utils.CollectionUtils;
import com.pax.pay.utils.Controllers;
import com.pax.up.bjb.R;
import com.pax.view.dialog.CustomAlertDialog;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import in.galaxyofandroid.spinerdialog.OnSpinerItemClick;
import in.galaxyofandroid.spinerdialog.SpinnerDialog;

import static com.pax.pay.utils.Controllers.OTHERS;

public class ListOthersPrabayarFragment extends Fragment implements OnItemClickListener, OnScrollListener {

    private static final String TAG = "[ListPLNFragment]";
    private ListView mListView;
    private View mListViewTab;
    private OthersPrabayarListAdapter mAdapter;
    private RelativeLayout noTransRecordLayout;
    private RecordAsyncTask mRecordAsyncTask;
    private LinearLayout layout;
    //private boolean supportDoTrans;
    private List<ProductData> mListItems;
    private EditText phone_number;
    private Button btnCari;
    SpinnerDialog spinnerDialog;
    String nomer = "";
    String kode = "";
    private int selectedItem = -1;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_trans_detail_others_layout, null);

        layout = (LinearLayout) view.findViewById(R.id.parent_layout);
        mListView = (ListView) view.findViewById(R.id.list_view);
        phone_number = (EditText) view.findViewById(R.id.phone_number);
        phone_number.requestFocus();
        mListViewTab = view.findViewById(R.id.list_view_tab);
        mListView.setOnItemClickListener(this);
        mListView.setOnScrollListener(this);
        noTransRecordLayout = (RelativeLayout) view.findViewById(R.id.no_trans_record_layout);
        btnCari = (Button) view.findViewById(R.id.btn_cari);

        //spinnerDialog = new SpinnerDialog(getActivity(), getListTrans(), "Search");

        List<String> operatorList = Controllers.getOperatorByType(OTHERS);

        ArrayList<String> arrlistOperator = new ArrayList<String>(operatorList);

        spinnerDialog = new SpinnerDialog(getActivity(), arrlistOperator, "Search");

        spinnerDialog.setCancellable(true);
        spinnerDialog.setShowKeyboard(false);
        spinnerDialog.bindOnSpinerListener(new OnSpinerItemClick() {
            @Override
            public void onClick(String item, int position) {
                btnCari.setText(item);
                mListItems = Controllers.getAllProductDataByType(OTHERS, item);
                mAdapter.addListToAdapter(mListItems);
                mListView.setAdapter(mAdapter);

                Log.d("teg", "onClick --> : " + "");
            }
        });

        btnCari.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinnerDialog.showSpinerDialog();
            }
        });

        /*mListItems = Controllers.getAllProductDataByType(OTHERS);
        if (CollectionUtils.isEmpty(mListItems)) {
            layout.setVisibility(View.GONE);
            noTransRecordLayout.setVisibility(View.VISIBLE);
        } else {
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
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

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

    ArrayList<String> getListTrans(){
        ArrayList<String> listSpinner = new ArrayList<>();
        listSpinner.add("GOJEK");
        listSpinner.add("OVO");
        listSpinner.add("DANA");
        return listSpinner;
    }

    class RecordAsyncTask extends AsyncTask<Void, Void, List<ProductData>> {

        @Override
        protected List<ProductData> doInBackground(Void... params) {
            mListItems = Controllers.getAllProductDataByType(OTHERS);
            return mListItems;
        }

        @Override
        protected void onPostExecute(List<ProductData> result) {

            try {
                if (CollectionUtils.isEmpty(result)) {
                    layout.setVisibility(View.GONE);
                    noTransRecordLayout.setVisibility(View.VISIBLE);
                } else {
                    layout.setVisibility(View.VISIBLE);
                    noTransRecordLayout.setVisibility(View.GONE);
                    mAdapter = new OthersPrabayarListAdapter(getActivity(), result);
                    mListView.setAdapter(mAdapter);
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

        if (phone_number.getText().toString().length() < 9) {
            showErrorDialog("Billing Id minimal 9 karakter");
            return;
        }

        ProductData productData = (ProductData) mAdapter.getItem(position);

        //kode = getCodeByDescName(productData.getProductDescription().replace(".", ""));
        kode = generateCodeByDescName(productData.getProductDescription().trim());
        Log.d("teg", "nomer " + nomer + " desc : " + productData.getProductDescription() + " kode payment " + kode);
        Log.d("teg", "-->" + productData.toString());

        Intent intent = new Intent(getActivity(), PulseDetailsActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("mode", "others");
        bundle.putString("kodeproduk", kode);
        bundle.putString("phone_number", phone_number.getText().toString());
        bundle.putSerializable(EUIParamKeys.CONTENT.toString(), productData);
        intent.putExtras(bundle);
        getActivity().startActivity(intent);

    }

    static String arr[][] = {

            {"GJ20", "GOJEK 20000"},
            {"GJ50", "GOJEK 50000"},
            {"GJ100", "GOJEK 100000"},
            {"GJ250", "GOJEK 250000"},
            {"OV20", "OVO 20000"},
            {"OV25", "OVO 25000"},
            {"OV50", "OVO 50000"},
            {"OV100", "OVO 100000"},
            {"OV150", "OVO 150000"},
            {"OV200", "OVO 200000"}

    };

    String getCodeByDescName(String descName) {
        String code = "";
        for (int i = 0; i < arr.length; i++) {
            if (descName.equalsIgnoreCase(arr[i][1])) {
                code = arr[i][0];
                return code;
            }
        }
        return code;
    }


    String getPaymentCode(String tipe) {
        if (tipe.equalsIgnoreCase("GOJEK")) {
            return "GJ";
        } else if (tipe.equalsIgnoreCase("OVO")) {
            return "OV";
        } else if (tipe.equalsIgnoreCase("DANA")) {
            return "DN";
        }

        return null;

    }

    String generateCodeByDescName(String descName) {

        for (ProductData p : mListItems) {
            if (p.getProductDescription().equalsIgnoreCase(descName)) { //OVO 20.000

                if (p.getProductDescription().contains(" ")) {
                    String[] temp = p.getProductDescription().split(" ");
                    String tipe = temp[0]; //OVO
                    tipe = getPaymentCode(tipe); //OVO --> OV
                    String nominal = temp[1].replace(".", ""); //20.000 --> 20000

                    if (nominal.length() == 4) {
                        nominal = nominal.substring(0, 1);
                    } else if (nominal.length() == 5) {
                        nominal = nominal.substring(0, 2); //20000 -->20
                    } else if (nominal.length() == 6) {
                        nominal = nominal.substring(0, 3);
                    }

                    return tipe + nominal; //OVO 20.000 --> OV20

                }
            }
        }
        return null;
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
class OthersPrabayarListAdapter extends BaseAdapter {

    private Context context;
    private List<ProductData> data;
    private static final int TYPE_COUNT = 2;

    public OthersPrabayarListAdapter(Context context, List<ProductData> list) {
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

        convertView = LayoutInflater.from(context).inflate(R.layout.list_item_others, null);
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
