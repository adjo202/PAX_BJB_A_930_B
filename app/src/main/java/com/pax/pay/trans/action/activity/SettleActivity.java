package com.pax.pay.trans.action.activity;

import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.ConditionVariable;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pax.abl.core.ActionResult;
import com.pax.device.Device;
import com.pax.gl.convert.IConvert.ECurrencyExponent;
import com.pax.pay.BaseActivityWithTickForAction;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.constant.Constants;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.record.Printer;
import com.pax.pay.record.TransQueryActivity;
import com.pax.pay.trans.TransContext;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.Controller;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.trans.model.TransTotal;
import com.pax.pay.trans.transmit.TransOnline;
import com.pax.pay.trans.transmit.TransProcessListenerImpl;
import com.pax.pay.utils.Utils;
import com.pax.pay.utils.ViewUtils;
import com.pax.settings.SysParam;
import com.pax.settings.currency.Currency;
import com.pax.up.bjb.R;
import com.pax.view.dialog.CustomAlertDialog;
import com.pax.view.dialog.CustomAlertDialog.OnCustomClickListener;
import com.pax.view.dialog.DialogUtils;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class SettleActivity extends BaseActivityWithTickForAction implements OnClickListener {

    private Button settleBtn;
    private TransTotal total;
    private String navTitle;
    private boolean navBack;
    private boolean hasSettle;

    //setor, Tarik, Transfer, Pbb, Mpn, Pulsa, Infosaldo, Ministement
    private TextView setorAmtTv, setorFeeTv;
    private TextView tarikAmtTv, tarikFeeTv;
    private TextView transferAmtTv, transferFeeTv;
    private TextView trfSesamaAmtTv, trfSesamaFeeTv;
    private TextView pbbAmtTv, pbbFeeTv;
    private TextView mpnAmtTv, mpnFeeTv;
    private TextView pulsaAmtTv, pulsaFeeTv;
    private TextView infosaldoAmtTv, infosaldoFeeTv;
    private TextView ministatementAmtTv, ministatementFeeTv;

    private TextView pdamAmtTv, pdamFeeTv;
    private TextView pascabayarAmtTv, pascabayarFeeTv;
    private TextView samsatAmtTv, samsatFeeTv;

    private TextView bpjsTkPendaftaranAmtTv, bpjsTkPendaftaranFeeTv;
    private TextView bpjsTkPembayaranAmtTv, bpjsTkPembayaranFeeTv;




    private TextView termidTv, merchidTv, batchnoTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (FinancialApplication.getController().get(Controller.BATCH_UP_STATUS) == Controller.Constant.BATCH_UP) {
            settleBtn.performClick();
        }
    }

    @Override
    protected void loadParam() {
        Bundle bundle = getIntent().getExtras();
        total = (TransTotal) bundle.getSerializable(EUIParamKeys.CONTENT.toString());
        navTitle = getIntent().getStringExtra(EUIParamKeys.NAV_TITLE.toString());
        navBack = getIntent().getBooleanExtra(EUIParamKeys.NAV_BACK.toString(), false);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_settle_layout;
    }

    @Override
    protected void initViews() {

        TextView tvTitle = (TextView) findViewById(R.id.header_title);
        tvTitle.setText("SETTLEMENT");

        setorAmtTv = (TextView) findViewById(R.id.setor_amt);
        setorFeeTv = (TextView) findViewById(R.id.setor_fee);

        tarikAmtTv = (TextView) findViewById(R.id.tarik_amt);
        tarikFeeTv = (TextView) findViewById(R.id.tarik_fee);

        transferAmtTv = (TextView) findViewById(R.id.transfer_amt);
        transferFeeTv = (TextView) findViewById(R.id.transfer_fee);

        trfSesamaAmtTv = (TextView)findViewById(R.id.transfer_sesama_amt);
        trfSesamaFeeTv = (TextView)findViewById(R.id.transfer_sesama_fee);

        pbbAmtTv = (TextView) findViewById(R.id.pbb_amt);
        pbbFeeTv = (TextView) findViewById(R.id.pbb_fee);

        mpnAmtTv = (TextView) findViewById(R.id.mpn_amt);
        mpnFeeTv = (TextView) findViewById(R.id.mpn_fee);

        pulsaAmtTv = (TextView) findViewById(R.id.pulsa_amt);
        pulsaFeeTv = (TextView) findViewById(R.id.pulsa_fee);

        infosaldoAmtTv = (TextView)findViewById(R.id.infosaldo_amt);
        infosaldoFeeTv = (TextView)findViewById(R.id.infosaldo_fee);

        ministatementAmtTv = (TextView)findViewById(R.id.ministatement_amt);
        ministatementFeeTv = (TextView)findViewById(R.id.ministatement_fee);

        pdamAmtTv = (TextView) findViewById(R.id.pdam_amt);
        pdamFeeTv = (TextView) findViewById(R.id.pdam_fee);

        pascabayarAmtTv = (TextView) findViewById(R.id.pascabayar_amt);
        pascabayarFeeTv = (TextView) findViewById(R.id.pascabayar_fee);

        samsatAmtTv = (TextView) findViewById(R.id.samsat_amt);
        samsatFeeTv = (TextView) findViewById(R.id.samsat_fee);

        bpjsTkPendaftaranAmtTv =  (TextView) findViewById(R.id.bpjs_tk_pendaftaran_amt);
        bpjsTkPendaftaranFeeTv =  (TextView) findViewById(R.id.bpjs_tk_pendaftaran_fee);

        bpjsTkPembayaranAmtTv =  (TextView) findViewById(R.id.bpjs_tk_pembayaran_amt);
        bpjsTkPembayaranFeeTv =  (TextView) findViewById(R.id.bpjs_tk_pembayaran_fee);

        settleBtn = (Button) findViewById(R.id.confirm_btn);

        termidTv  = (TextView)findViewById(R.id.term_id);
        merchidTv = (TextView) findViewById(R.id.merch_id);
        batchnoTv = (TextView) findViewById(R.id.batch_no);

        termidTv.setText("Terminal No.      "+FinancialApplication.getSysParam().get(SysParam.TERMINAL_ID));
        merchidTv.setText("Merchant No.     "+FinancialApplication.getSysParam().get(SysParam.MERCH_ID));
        batchnoTv.setText("Batch No.            "+FinancialApplication.getSysParam().get(SysParam.BATCH_NO));

        initTables();

    }

    @Override
    protected void setListeners() {

        settleBtn.setOnClickListener(this);
        settleBtn.setFocusable(true);
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
            case R.id.confirm_btn:
                confirmSettle();
                break;
            default:
                break;
        }

    }

    private void confirmSettle() {
        if(hasSettle){
            return;
        }
        // 进入结算流程时间太长， 停止定时器
        hasSettle = true;
        tickTimerStop();

        new Thread(new Runnable() {

            @Override
            public void run() {
                TransProcessListenerImpl transProcessListenerImpl = new TransProcessListenerImpl(SettleActivity.this);
                // 结算
                int ret = TransOnline.settle(total, transProcessListenerImpl);
                transProcessListenerImpl.onHideProgress();
                if (ret != TransResult.SUCC) {
                    ActionResult result = new ActionResult(ret, null);
                    finish(result);
                    return;
                }
                // Sandy : Record the batch total and set the clear transaction record flag
                total.setMerchantID(FinancialApplication.getSysParam().get(SysParam.MERCH_ID));
                total.setTerminalID(FinancialApplication.getSysParam().get(SysParam.TERMINAL_ID));
                total.setOperatorID(TransContext.getInstance().getOperID());
                total.setBatchNo(FinancialApplication.getSysParam().get(SysParam.BATCH_NO));
                total.setDate(Device.getDate());
                total.setTime(Device.getTime());
                total.save();
                FinancialApplication.getController().set(Controller.CLEAR_LOG, Controller.Constant.YES);
                // 打印结算单
                Printer.printTransTotal(SettleActivity.this, handler, true);
                /*if (SysParam.Constant.YES.equals(FinancialApplication.getSysParam().get(SysParam.SETTLETC_PRNDETAIL))) {
                    // 打印明细
                    printDetail(false);
                    // 打印失败明细
                    printDetail(true);
                }*/

                // 批上送结算,将批上送断点赋值为0
                FinancialApplication.getController().set(Controller.BATCH_UP_STATUS, Controller.Constant.WORKED);
                // Clear transaction flow
                if (TransData.deleteAllTrans()) {
                    // Delete the failed transaction file
                    // Delete the electronic signature file
                    FinancialApplication.getController().set(Controller.CLEAR_LOG, Controller.Constant.NO);
                    Component.incBatchNo();
                }

                ActionResult result = new ActionResult(ret, null);
                finish(result);
            }
        }).start();
    }

    private void printDetail(final boolean isFailDetail) {
        final ConditionVariable cv = new ConditionVariable();
        handler.post(new Runnable() {

            @Override
            public void run() {
                final CustomAlertDialog dialog = new CustomAlertDialog(SettleActivity.this,
                        CustomAlertDialog.IMAGE_TYPE);
                String info = getString(R.string.print_settle_detail_or_not);
                if (isFailDetail) {
                    info = getString(R.string.print_fail_detail_or_not);
                }
                dialog.setContentText(info);
                dialog.show();
                dialog.setImage(BitmapFactory.decodeResource(getResources(), R.drawable.ic19));
                dialog.showCancelButton(true);
                dialog.showConfirmButton(true);
                dialog.setCancelClickListener(new OnCustomClickListener() {
                    @Override
                    public void onClick(CustomAlertDialog alertDialog) {
                        dialog.dismiss();
                        cv.open();
                    }
                });
                dialog.setConfirmClickListener(new OnCustomClickListener() {
                    @Override
                    public void onClick(CustomAlertDialog alertDialog) {
                        dialog.dismiss();
                        new Thread(new Runnable() {

                            @Override
                            public void run() {

                                int result = 0;
                                if (isFailDetail) {
                                    // 打印失败交易明细
                                    result = Printer.printFailDetail(SettleActivity.this, handler);
                                } else {
                                    // 打印交易明细
                                    result = Printer.printTransDetail(getString(R.string.settle_detail_list),
                                            SettleActivity.this, handler);
                                }
                                if (result != TransResult.SUCC) {
                                    DialogUtils.showErrMessage(SettleActivity.this, handler,
                                            getString(R.string.transtype_print),
                                            TransResult.getMessage(SettleActivity.this, result),
                                            new OnDismissListener() {

                                                @Override
                                                public void onDismiss(DialogInterface arg0) {
                                                    cv.open();
                                                }
                                            }, Constants.FAILED_DIALOG_SHOW_TIME);

                                } else {
                                    cv.open();
                                }

                            }
                        }).start();

                    }
                });
            }
        });

        cv.block();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (hasSettle) {
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            ActionResult result = new ActionResult(TransResult.ERR_ABORTED, null);
            finish(result);
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_ENTER) {
            confirmSettle();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    private void initTables() {

        TransTotal total = TransTotal.calcTotal();
        Currency currency = FinancialApplication.getSysParam().getCurrency();

        //setor tunai
        long setorAmt, setorFee;
        setorAmt = total.getTransTotalAmt()[0][1];
        setorFee = total.getTransTotalAmt()[0][2];
        String setorAmtTxt = FinancialApplication.getConvert().amountMinUnitToMajor( String.valueOf(setorAmt) , currency.getCurrencyExponent(), true);
        if (setorAmtTxt.equals("0")){
            setorAmtTv.setText( currency.getName() + " " +  setorAmtTxt);
        }else {
            setorAmtTv.setText( currency.getName() + " " +  setorAmtTxt);
        }
        if (setorFee == 0){
            setorFeeTv.setText( currency.getName() + " " +  setorFee);
        }else {
            setorFeeTv.setText(currency.getName() + " " + FinancialApplication.getConvert().amountMinUnitToMajor( String.valueOf(setorFee) , currency.getCurrencyExponent(), true) );
        }

        //tarik tunai
        long tarikAmt, tarikFee;
        tarikAmt = total.getTransTotalAmt()[1][1];
        tarikFee = total.getTransTotalAmt()[1][2];
        String tarikAmtTxt = FinancialApplication.getConvert().amountMinUnitToMajor( String.valueOf(tarikAmt) , currency.getCurrencyExponent(), true);
        if (tarikAmt == 0){
            tarikAmtTv.setText( currency.getName() + " " +  tarikAmt);
        }else {

            tarikAmtTv.setText( currency.getName() + " " +  tarikAmtTxt);
        }

        if(tarikFee==0){
            tarikFeeTv.setText(currency.getName() + " " +tarikFee );
        }else {
            tarikFeeTv.setText(currency.getName() + " " + FinancialApplication.getConvert().amountMinUnitToMajor( String.valueOf(tarikFee) , currency.getCurrencyExponent(), true) );
        }


        //transfer
        long transferAmt, transferFee;
        transferAmt = total.getTransTotalAmt()[2][1];
        transferFee = total.getTransTotalAmt()[2][2];

        if (transferAmt == 0){
            transferAmtTv.setText( currency.getName() + " " +  transferAmt);
        }else {
            transferAmtTv.setText( currency.getName() + " " + FinancialApplication.getConvert().amountMinUnitToMajor( String.valueOf(transferAmt) , currency.getCurrencyExponent(), true) );
        }

        if(transferFee==0){
            transferFeeTv.setText(currency.getName() + " " +transferFee );
        }else {
            transferFeeTv.setText(currency.getName() + " " + FinancialApplication.getConvert().amountMinUnitToMajor( String.valueOf(transferFee) , currency.getCurrencyExponent(), true) );
        }

        //transfer Sesama
        long trfSesama_Amt, trfSesamaFee;
        trfSesama_Amt = total.getTransTotalAmt()[8][1];
        trfSesamaFee = total.getTransTotalAmt()[8][2];

        if (trfSesama_Amt == 0){
            trfSesamaAmtTv.setText( currency.getName() + " " +  trfSesama_Amt);
        }else {
            trfSesamaAmtTv.setText( currency.getName() + " " + FinancialApplication.getConvert().amountMinUnitToMajor( String.valueOf(trfSesama_Amt) , currency.getCurrencyExponent(), true) );
        }

        if(trfSesamaFee==0){
            trfSesamaFeeTv.setText(currency.getName() + " " +trfSesamaFee );
        }else {
            trfSesamaFeeTv.setText(currency.getName() + " " + FinancialApplication.getConvert().amountMinUnitToMajor( String.valueOf(trfSesamaFee) , currency.getCurrencyExponent(), true) );
        }

        //pbb
        long pbbAmt, pbbFee;
        pbbAmt = total.getTransTotalAmt()[3][1];
        pbbFee = total.getTransTotalAmt()[3][2];

        if (pbbAmt == 0){
            pbbAmtTv.setText( currency.getName() + " " +  pbbAmt);
        }else {
            pbbAmtTv.setText( currency.getName() + " " + FinancialApplication.getConvert().amountMinUnitToMajor( String.valueOf(pbbAmt) , currency.getCurrencyExponent(), true) );
        }

        if(pbbFee==0){
            pbbFeeTv.setText(currency.getName() + " " +transferFee );
        }else {
            pbbFeeTv.setText(currency.getName() + " " + FinancialApplication.getConvert().amountMinUnitToMajor( String.valueOf(pbbFee) , currency.getCurrencyExponent(), true) );
        }


        //mpn
        long mpnAmt, mpnFee;
        mpnAmt = total.getTransTotalAmt()[4][1];
        mpnFee = total.getTransTotalAmt()[4][2];
        if (mpnAmt == 0){
            mpnAmtTv.setText( currency.getName() + " " +  mpnAmt);
        }else {
            mpnAmtTv.setText( currency.getName() + " " + FinancialApplication.getConvert().amountMinUnitToMajor( String.valueOf(mpnAmt) , currency.getCurrencyExponent(), true) );
        }

        if(mpnFee==0){
            mpnFeeTv.setText(currency.getName() + " " +mpnFee );
        }else {
            mpnFeeTv.setText(currency.getName() + " " + FinancialApplication.getConvert().amountMinUnitToMajor( String.valueOf(mpnFee) , currency.getCurrencyExponent(), true) );
        }


        //pulsa
        long pulsaAmt, pulsaFee;
        pulsaAmt = total.getTransTotalAmt()[5][1];
        pulsaFee = total.getTransTotalAmt()[5][2];
        if (pulsaAmt == 0){
            pulsaAmtTv.setText( currency.getName() + " " +  pulsaAmt);
        }else {
            String pulsaAmtTxt = FinancialApplication.getConvert().amountMinUnitToMajor( String.valueOf(pulsaAmt) , currency.getCurrencyExponent(), true);
            pulsaAmtTv.setText( currency.getName() + " " +  pulsaAmtTxt);
        }

        if(pulsaFee==0){
            pulsaFeeTv.setText(currency.getName() + " " +pulsaFee );
        }else {
            pulsaFeeTv.setText(currency.getName() + " " + FinancialApplication.getConvert().amountMinUnitToMajor( String.valueOf(pulsaFee) , currency.getCurrencyExponent(), true) );
        }


        //infosaldo
        long infosaldoAmt, infosaldoFee;
        infosaldoAmt = total.getTransTotalAmt()[6][1];
        infosaldoFee = total.getTransTotalAmt()[6][2];
        if (infosaldoAmt == 0){
            infosaldoAmtTv.setText( currency.getName() + " " +  infosaldoAmt);
        }else {
            infosaldoAmtTv.setText( currency.getName() + " " + FinancialApplication.getConvert().amountMinUnitToMajor( String.valueOf(infosaldoAmt) , currency.getCurrencyExponent(), true) );
        }

        if(infosaldoFee==0){
            infosaldoFeeTv.setText(currency.getName() + " " +infosaldoFee );
        }else {
            infosaldoFeeTv.setText(currency.getName() + " " + FinancialApplication.getConvert().amountMinUnitToMajor( String.valueOf(infosaldoFee) , currency.getCurrencyExponent(), true) );
        }

        //ministatement
        long ministatementAmt, ministatementFee;
        ministatementAmt = total.getTransTotalAmt()[7][1];
        ministatementFee = total.getTransTotalAmt()[7][2];
        if (ministatementAmt == 0){
            ministatementAmtTv.setText( currency.getName() + " " +  ministatementAmt);
        }else {
            ministatementAmtTv.setText( currency.getName() + " " + FinancialApplication.getConvert().amountMinUnitToMajor( String.valueOf(ministatementAmt) , currency.getCurrencyExponent(), true) );
        }

        if(ministatementFee==0){
            ministatementFeeTv.setText(currency.getName() + " " +ministatementFee );
        }else {
            ministatementFeeTv.setText(currency.getName() + " " + FinancialApplication.getConvert().amountMinUnitToMajor( String.valueOf(ministatementFee) , currency.getCurrencyExponent(), true) );
        }

        long pdamtAmt, pdamFee;
        pdamtAmt = total.getTransTotalAmt()[9][1];
        pdamFee = total.getTransTotalAmt()[9][2];
        if (pdamtAmt == 0){
            pdamAmtTv.setText( currency.getName() + " " +  pdamtAmt);
        }else {
            pdamAmtTv.setText( currency.getName() + " " + FinancialApplication.getConvert().amountMinUnitToMajor( String.valueOf(pdamtAmt) , currency.getCurrencyExponent(), true) );
        }

        if(pdamFee==0){
            pdamFeeTv.setText(currency.getName() + " " +pdamFee );
        }else {
            pdamFeeTv.setText(currency.getName() + " " + FinancialApplication.getConvert().amountMinUnitToMajor( String.valueOf(pdamFee) , currency.getCurrencyExponent(), true) );
        }

        long pascabarAmt, pascabarFee;
        pascabarAmt = total.getTransTotalAmt()[10][1];
        pascabarFee = total.getTransTotalAmt()[10][2];
        if (pascabarAmt == 0){
            pascabayarAmtTv.setText( currency.getName() + " " +  pascabarAmt);
        }else {
            pascabayarAmtTv.setText( currency.getName() + " " + FinancialApplication.getConvert().amountMinUnitToMajor( String.valueOf(pascabarAmt) , currency.getCurrencyExponent(), true) );
        }

        if(pascabarFee==0){
            pascabayarFeeTv.setText(currency.getName() + " " +pascabarFee );
        }else {
            pascabayarFeeTv.setText(currency.getName() + " " + FinancialApplication.getConvert().amountMinUnitToMajor( String.valueOf(pascabarFee) , currency.getCurrencyExponent(), true) );
        }

        long samsatAmt, samsatFee;
        samsatAmt = total.getTransTotalAmt()[11][1];
        samsatFee = total.getTransTotalAmt()[11][2];
        if (samsatAmt == 0){
            samsatAmtTv.setText( currency.getName() + " " +  samsatAmt);
        }else {
            samsatAmtTv.setText( currency.getName() + " " + FinancialApplication.getConvert().amountMinUnitToMajor( String.valueOf(samsatAmt) , currency.getCurrencyExponent(), true) );
        }

        if(samsatFee==0){
            samsatFeeTv.setText(currency.getName() + " " +samsatFee );
        }else {
            samsatFeeTv.setText(currency.getName() + " " + FinancialApplication.getConvert().amountMinUnitToMajor( String.valueOf(samsatFee) , currency.getCurrencyExponent(), true) );
        }

        long BPJSTkPendaftaranAmt, BPJSTkPendaftaranFee;
        BPJSTkPendaftaranFee = total.getTransTotalAmt()[16][1];
        BPJSTkPendaftaranAmt = total.getTransTotalAmt()[16][2];

        if(BPJSTkPendaftaranAmt == 0)
            bpjsTkPendaftaranAmtTv.setText(currency.getName() + " " +  BPJSTkPendaftaranAmt);
        else
            bpjsTkPendaftaranAmtTv.setText(currency.getName() + " " + FinancialApplication.getConvert().amountMinUnitToMajor(
                    String.valueOf(BPJSTkPendaftaranAmt) , currency.getCurrencyExponent(), true));

        if(BPJSTkPendaftaranFee == 0)
            bpjsTkPendaftaranFeeTv.setText(currency.getName() + " " +  BPJSTkPendaftaranFee);
        else
            bpjsTkPendaftaranFeeTv.setText(currency.getName() + " " + FinancialApplication.getConvert().amountMinUnitToMajor(
                    String.valueOf(BPJSTkPendaftaranFee) , currency.getCurrencyExponent(), true));



        long BPJSTkPembayaranAmt, BPJSTkPembayaranFee;
        BPJSTkPembayaranFee = total.getTransTotalAmt()[17][1];
        BPJSTkPembayaranAmt = total.getTransTotalAmt()[17][2];
        if(BPJSTkPembayaranAmt == 0)
            bpjsTkPembayaranAmtTv.setText(currency.getName() + " " +  BPJSTkPembayaranAmt);
        else
            bpjsTkPembayaranAmtTv.setText(currency.getName() + " " + FinancialApplication.getConvert().amountMinUnitToMajor(
                    String.valueOf(BPJSTkPembayaranAmt) , currency.getCurrencyExponent(), true));

        if(BPJSTkPembayaranFee == 0)
            bpjsTkPembayaranFeeTv.setText(currency.getName() + " " +  BPJSTkPembayaranFee);
        else
            bpjsTkPembayaranFeeTv.setText(currency.getName() + " " + FinancialApplication.getConvert().amountMinUnitToMajor(
                    String.valueOf(BPJSTkPembayaranFee) , currency.getCurrencyExponent(), true));



    }
}
