package com.pax.pay.trans.action;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.pax.abl.core.AAction;
import com.pax.abl.utils.PanUtils;
import com.pax.device.Device;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.record.DetailsActivity;
import com.pax.pay.trans.TransContext;
import com.pax.pay.trans.action.activity.DispTransDetailActivity;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.utils.CurrencyConverter;
import com.pax.pay.utils.Fox;
import com.pax.pay.utils.Utils;
import com.pax.settings.SysParam;
import com.pax.settings.currency.Currency;
import com.pax.up.bjb.R;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class ActionDispTransDetail extends AAction {
    private static final String TAG = "ActionDispTransDetail";
    private Context context;
    private Handler handler;

    private Map<String, String> map;
    private String title;

    private boolean enableBackAct = true;
    private int inletType =0;
    private TransData transData = null;
    private boolean supportDoTrans =false;

    public ActionDispTransDetail(ActionStartListener listener) {
        super(listener);
    }

    public ActionDispTransDetail(Handler handler, String title) {
        super(null);
        this.handler = handler;
        this.title = title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setInletType(int inletType) {
        this.inletType = inletType;
    }

    public void setTransData(TransData transData) {
        this.transData = transData;
    }

    public void setSupportDoTrans(boolean supportDoTrans) {
        this.supportDoTrans = supportDoTrans;
    }

    /**
     * 参数设置
     * 
     * @param context
     *            ：应用上下文
     * @param handler
     *            ：handler
     * @param title
     *            ：抬头
     * @param map
     *            ：确认信息
     */
    public void setParam(Context context, Handler handler, String title, Map<String, String> map) {
        this.context = context;
        this.handler = handler;
        this.title = title;
        this.map = map;
    }

    /**
     * 参数设置
     *
     * @param context
     *            ：应用上下文
     * @param handler
     *            ：handler
     * @param title
     *            ：抬头
     * @param transData
     *          交易详情
     * @param supportDoTrans
     *         是否支持交易
     * @param  inletType   默认为0 ，1代表交易查询入口（进入交易详情页面）
     *         入口类型
     */
    public void setParam(Context context, Handler handler, String title, TransData transData,boolean supportDoTrans,int inletType) {
        this.context = context;
        this.handler = handler;
        this.title = title;
        this.transData = transData;
        this.supportDoTrans =supportDoTrans;
        this.inletType=inletType;
    }

    // add abdul untuk pulsa detailnya gaboleh back tambah param enable back
    public void setParam(Context context, Handler handler, String title, TransData transData,boolean supportDoTrans,int inletType, boolean enableBack) {
        this.context = context;
        this.handler = handler;
        this.title = title;
        this.transData = transData;
        this.supportDoTrans =supportDoTrans;
        this.inletType=inletType;
        this.enableBackAct = enableBack;
    }

    @Override
    protected void process() {
        context = TransContext.getInstance().getCurrentContext();
        if (map == null) {
            map = prepareDispTransDetail();
        }
        Log.d(TAG,"Sandy.DetailsActivity.DispTransRunnable " + TransContext.getInstance().getCurrentAction());
        handler.post(new DispTransRunnable());
    }

    private LinkedHashMap<String, String> prepareDispTransDetail() {
        if (transData == null) {
            return null;
        }
        Currency currency = FinancialApplication.getSysParam().getCurrency();
        ETransType transType = ETransType.valueOf(transData.getTransType());

        long amountL;
        // add abdul 310321 info saldo amount dari transdata.getbalance
        if (transType == ETransType.BALANCE_INQUIRY || transType == ETransType.BALANCE_INQUIRY_2) {
            amountL = Long.parseLong(transData.getBalance());
        } else if (transType == ETransType.TARIK_TUNAI ||
                   transType == ETransType.TARIK_TUNAI_2 ||
                transType == ETransType.SETOR_TUNAI) {
            amountL = Long.parseLong(transData.getAmount());
            amountL = amountL/100;
        } else {
            amountL = Long.parseLong(transData.getAmount());
        }
        String amount = FinancialApplication.getConvert().amountMinUnitToMajor(String.valueOf(amountL),
                currency.getCurrencyExponent(), true);
        amount = currency.getName() + " " + amount;

        // 日期时间
        String date = transData.getDate();
        String time = transData.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        String yearDate = sdf.format(new java.util.Date());

        String temp = yearDate.substring(0, 4) + "/" + date.substring(0, 2) + "/" + date.substring(2, 4) + " "
                + time.substring(0, 2) + ":" + time.substring(2, 4) + ":" + time.substring(4);

        LinkedHashMap<String, String> hashMap = new LinkedHashMap<>();
        //hashMap.put(context.getString(R.string.trans_type), transType.getTransName());
        // add abdul tarik tunai
        if (transType == ETransType.TARIK_TUNAI || transType == ETransType.TARIK_TUNAI_2) {
            hashMap.put(context.getString(R.string.detail_dari_rek), transData.getAccNo().replace(" ", ""));
            hashMap.put(context.getString(R.string.detail_nilai_penarikan), amount);
        } else if (transType == ETransType.SETOR_TUNAI) {
            String nama = transData.getField48().replace("  ", "");
            hashMap.put(context.getString(R.string.detail_no_rek), transData.getAccNo().replace(" ", ""));
            if (nama.length()>15) {
                hashMap.put(context.getString(R.string.detail_nm_penerima), "");
                hashMap.put("", nama);
            } else {
                hashMap.put(context.getString(R.string.detail_nm_penerima), nama);
            }
            hashMap.put(context.getString(R.string.detail_jml_setor), amount);
        } else if(transType == ETransType.BALANCE_INQUIRY || transType == ETransType.BALANCE_INQUIRY_2) {
            hashMap.put(context.getString(R.string.detail_no_rek), transData.getAccNo().replace(" ", ""));
            hashMap.put(context.getString(R.string.detail_jml_saldo), amount);
        } else if (transType == ETransType.INQ_PULSA_DATA ||
                transType == ETransType.PURCHASE_PULSA_DATA ||
                transType == ETransType.PASCABAYAR_INQUIRY ||
                transType == ETransType.PDAM_INQUIRY
        ) {
            String[] f47 = transData.getField47().split("#"); // di 47 ada data pulsa data
            transData.setField48(f47[0]);
            transData.setPhoneNo(f47[1]);
            transData.setProduct_code(f47[2]);
            transData.setTypeProduct(f47[3]);
            transData.setOperator(f47[4]);
            transData.setKeterangan(f47[5]);
            transData.setProduct_name(f47[6]);
            transData.setField47(transData.getField47());

            hashMap.put("OPERATOR", f47[4].replace(" ", ""));
            hashMap.put("PHONE NUMBER", f47[1].replace(" ", ""));
            hashMap.put("PRODUCT NAME", f47[6].replace(" ", ""));
            //hashMap.put("", f47[5].replace(" ", ""));
            NumberFormat formatter = new DecimalFormat("#,###");

            //tri add 18/06/21
            if (transData.getAmount().length()==14){
                transData.setAmount(transData.getAmount().substring(0, transData.getAmount().length() - 2));
            }
            //end

            String price = String.format("IDR %s",formatter.format(Double.parseDouble(transData.getAmount())));
            hashMap.put("PRICE", price);
            String fee;
            try {
                fee = String.format("IDR %s",formatter.format(Double.parseDouble(transData.getFeeTotalAmount())));
            } catch (Exception e) {
                e.printStackTrace();
                fee = "0";
            }

            hashMap.put("ADMIN FEE", fee);
            price = String.valueOf(formatter.format(Double.parseDouble(fee.replace("IDR ", "").replace(",", "")) + Double.parseDouble(transData.getAmount().replace("IDR ", "").replace(",", ""))));
            hashMap.put("TOTAL", price);
            String stts = "";
            try {
                if (transData.getPrintTimeout().equals("Y")) {
                    stts = "PENDING";
                } else {
                    stts = "BERHASIL";
                }
            } catch (Exception e) {
                e.printStackTrace();
                stts = "BERHASIL";
            }
            hashMap.put("STATUS", stts);
        } else {
            hashMap.put(context.getString(R.string.trans_amount), amount);
        }

        //sandy
        if (transType == ETransType.COUPON_SALE || transType == ETransType.COUPON_SALE_VOID ) {
            String formatterCost = FinancialApplication.getConvert().amountMinUnitToMajor(
                    String.valueOf(Long.parseLong(transData.getActualPayAmount())),
                    currency.getCurrencyExponent(), true);
            String amountCost = context.getString(R.string.trans_amount_default,
                    currency.getName(), formatterCost);

            String formatterDiscount = FinancialApplication.getConvert().amountMinUnitToMajor(
                    String.valueOf(Long.parseLong(transData.getDiscountAmount())),
                    currency.getCurrencyExponent(), true);

            String amountDiscount = context.getString(R.string.trans_amount_default,
                    currency.getName(), formatterDiscount);

            hashMap.put(context.getString(R.string.detail_cost), amountCost);
            hashMap.put(context.getString(R.string.detail_discount), amountDiscount);
            hashMap.put(context.getString(R.string.trans_coupon), transData.getCouponNo());

        }

        // dari sini remark
        /*hashMap.put(context.getString(R.string.trans_card_no), PanUtils.maskedCardNo(transType, transData.getPan()));
        if (transData.getAuthCode() != null)
            hashMap.put(context.getString(R.string.detail_auth_code), transData.getAuthCode());
        if (transData.getRefNo() != null)
            hashMap.put(context.getString(R.string.detail_trans_ref_no), transData.getRefNo());
        hashMap.put(context.getString(R.string.detail_trans_no), String.format("%06d", transData.getTransNo()));
        hashMap.put(context.getString(R.string.trans_date), temp);*/

        return hashMap;
    }


    class DispTransRunnable implements Runnable {
        @Override
        public void run() {
            Log.d(TAG,"Sandy.DetailsActivity.DispTransRunnable " + DetailsActivity.Options.ACTION.toString());

            if(inletType == 0) {
                ArrayList<String> leftColumns = new ArrayList<>();
                ArrayList<String> rightColumns = new ArrayList<>();

                Set<String> keys = map.keySet();
                for (String key : keys) {
                    leftColumns.add(key);
                    Object value = map.get(key);
                    if (value != null) {
                        rightColumns.add((String) value);
                    } else {
                        rightColumns.add("");
                    }

                }

                Bundle bundle = new Bundle();
                bundle.putString(EUIParamKeys.NAV_TITLE.toString(), title);
                bundle.putBoolean(EUIParamKeys.NAV_BACK.toString(), enableBackAct);
                bundle.putStringArrayList(EUIParamKeys.ARRAY_LIST_1.toString(), leftColumns);
                bundle.putStringArrayList(EUIParamKeys.ARRAY_LIST_2.toString(), rightColumns);

                Intent intent = new Intent(context, DispTransDetailActivity.class);
                intent.putExtras(bundle);
                context.startActivity(intent);
            }else if(inletType == 1){

                Intent intent = new Intent(context, DetailsActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(EUIParamKeys.NAV_TITLE.toString(), title);
                bundle.putBoolean(EUIParamKeys.NAV_BACK.toString(), true);
                bundle.putSerializable(EUIParamKeys.CONTENT.toString(), transData);
                bundle.putBoolean(EUIParamKeys.SUPPORT_DO_TRANS.toString(), supportDoTrans);
                bundle.putString(EUIParamKeys.OPTIONS.toString(), DetailsActivity.Options.ACTION.toString());

                intent.putExtras(bundle);
                context.startActivity(intent);
            }
        }
    }
}
