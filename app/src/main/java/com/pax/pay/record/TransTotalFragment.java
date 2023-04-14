package com.pax.pay.record;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pax.pay.app.FinancialApplication;
import com.pax.pay.trans.model.TransTotal;
import com.pax.settings.currency.Currency;
import com.pax.up.bjb.R;

public class TransTotalFragment extends Fragment {

    private static final String TAG = "TransTotalFragment";

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
    private TextView BPJSTkPendaftaranAmtTv, BPJSTkPendaftaranFeeTv;
    private TextView BPJSTkPembayaranAmtTv, BPJSTkPembayaranFeeTv;
    private Currency currency = FinancialApplication.getSysParam().getCurrency();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_trans_total_layout, null);

        setorAmtTv = (TextView) view.findViewById(R.id.setor_amt);
        setorFeeTv = (TextView) view.findViewById(R.id.setor_fee);

        tarikAmtTv = (TextView) view.findViewById(R.id.tarik_amt);
        tarikFeeTv = (TextView) view.findViewById(R.id.tarik_fee);

        transferAmtTv = (TextView) view.findViewById(R.id.transfer_amt);
        transferFeeTv = (TextView) view.findViewById(R.id.transfer_fee);

        trfSesamaAmtTv = (TextView) view.findViewById(R.id.transfer_sesama_amt);
        trfSesamaFeeTv = (TextView) view.findViewById(R.id.transfer_sesama_fee);

        pbbAmtTv = (TextView) view.findViewById(R.id.pbb_amt);
        pbbFeeTv = (TextView) view.findViewById(R.id.pbb_fee);

        mpnAmtTv = (TextView) view.findViewById(R.id.mpn_amt);
        mpnFeeTv = (TextView) view.findViewById(R.id.mpn_fee);

        pulsaAmtTv = (TextView) view.findViewById(R.id.pulsa_amt);
        pulsaFeeTv = (TextView) view.findViewById(R.id.pulsa_fee);

        infosaldoAmtTv = (TextView) view.findViewById(R.id.infosaldo_amt);
        infosaldoFeeTv = (TextView) view.findViewById(R.id.infosaldo_fee);

        ministatementAmtTv = (TextView) view.findViewById(R.id.ministatement_amt);
        ministatementFeeTv = (TextView) view.findViewById(R.id.ministatement_fee);

        pdamAmtTv = (TextView) view.findViewById(R.id.pdam_amt);
        pdamFeeTv = (TextView) view.findViewById(R.id.pdam_fee);

        pascabayarAmtTv = (TextView) view.findViewById(R.id.pascabayar_amt);
        pascabayarFeeTv = (TextView) view.findViewById(R.id.pascabayar_fee);

        samsatAmtTv = (TextView) view.findViewById(R.id.samsat_amt);
        samsatFeeTv = (TextView) view.findViewById(R.id.samsat_fee);

        BPJSTkPendaftaranAmtTv = (TextView) view.findViewById(R.id.bpjs_tk_pendaftaran_amt);
        BPJSTkPendaftaranFeeTv = (TextView) view.findViewById(R.id.bpjs_tk_pendaftaran_fee);

        BPJSTkPembayaranAmtTv = (TextView) view.findViewById(R.id.bpjs_tk_pembayaran_amt);
        BPJSTkPembayaranFeeTv = (TextView) view.findViewById(R.id.bpjs_tk_pembayaran_fee);


        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();
        initTables();
    }

    // 初始化表格
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
        long transferAmt,transferAmt2, transferFee,transferFee2, totalAmountTransfer, totalFeeTransfer;
        transferAmt = total.getTransTotalAmt()[2][1];
        transferFee = total.getTransTotalAmt()[2][2];

        transferAmt2 = total.getTransTotalAmt()[13][1];
        transferFee2 = total.getTransTotalAmt()[13][2];
        totalAmountTransfer = transferAmt + transferAmt2;
        totalFeeTransfer = transferFee + transferFee2;

        if (totalAmountTransfer == 0){
            transferAmtTv.setText( currency.getName() + " " +  totalAmountTransfer);
        }else {
            transferAmtTv.setText( currency.getName() + " " + FinancialApplication.getConvert().amountMinUnitToMajor(
                    String.valueOf(totalAmountTransfer) , currency.getCurrencyExponent(), true) );
        }

        if(totalFeeTransfer==0){
            transferFeeTv.setText(currency.getName() + " " +totalFeeTransfer );
        }else {
            transferFeeTv.setText(currency.getName() + " " + FinancialApplication.getConvert().amountMinUnitToMajor(
                    String.valueOf(totalFeeTransfer) , currency.getCurrencyExponent(), true) );
        }

        //transfer Sesama
        long trfSesama_Amt,trfSesama_Amt2, trfSesamaFee,trfSesamaFee2,totalTransSesama,totalFeeTransSesama;
        trfSesama_Amt = total.getTransTotalAmt()[8][1];
        trfSesamaFee = total.getTransTotalAmt()[8][2];

        trfSesama_Amt2 = total.getTransTotalAmt()[14][1];
        trfSesamaFee2 = total.getTransTotalAmt()[14][2];

        totalTransSesama = trfSesama_Amt + trfSesama_Amt2;
        totalFeeTransSesama = trfSesamaFee + trfSesamaFee2;

        if (totalTransSesama == 0){
            trfSesamaAmtTv.setText( currency.getName() + " " +  totalTransSesama);
        }else {
            trfSesamaAmtTv.setText( currency.getName() + " " + FinancialApplication.getConvert().amountMinUnitToMajor(
                    String.valueOf(totalTransSesama) , currency.getCurrencyExponent(), true) );
        }

        if(totalFeeTransSesama==0){
            trfSesamaFeeTv.setText(currency.getName() + " " +totalFeeTransSesama );
        }else {
            trfSesamaFeeTv.setText(currency.getName() + " " + FinancialApplication.getConvert().amountMinUnitToMajor(
                    String.valueOf(totalFeeTransSesama) , currency.getCurrencyExponent(), true) );
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
        long infosaldoAmt, infosaldoFee,infosaldoAmt2, infosaldoFee2, totalInfoSaldoAmt, totalInfoSaldoFee ;
        infosaldoAmt = total.getTransTotalAmt()[6][1];
        infosaldoFee = total.getTransTotalAmt()[6][2];

        infosaldoAmt2 = total.getTransTotalAmt()[15][1];
        infosaldoFee2 = total.getTransTotalAmt()[15][2];

        totalInfoSaldoAmt = infosaldoAmt+infosaldoAmt2;
        totalInfoSaldoFee = infosaldoFee+infosaldoFee2;

        if (totalInfoSaldoAmt == 0){
            infosaldoAmtTv.setText( currency.getName() + " " +  totalInfoSaldoAmt);
        }else {
            infosaldoAmtTv.setText( currency.getName() + " " + FinancialApplication.getConvert().amountMinUnitToMajor(
                    String.valueOf(totalInfoSaldoAmt) , currency.getCurrencyExponent(), true) );
        }

        if(totalInfoSaldoFee==0){
            infosaldoFeeTv.setText(currency.getName() + " " +totalInfoSaldoFee );
        }else {
            infosaldoFeeTv.setText(currency.getName() + " " + FinancialApplication.getConvert().amountMinUnitToMajor(
                    String.valueOf(totalInfoSaldoFee) , currency.getCurrencyExponent(), true) );
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

        if(ministatementFee == 0){
            ministatementFeeTv.setText(currency.getName() + " " +ministatementFee );
        }else {
            ministatementFeeTv.setText(currency.getName() + " " + FinancialApplication.getConvert().amountMinUnitToMajor(
                    String.valueOf(ministatementFee) , currency.getCurrencyExponent(), true) );
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

        long pascabarAmt, pascabarFee,xxx;
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


        //BPJS Tk
        long BPJSTkPendaftaranAmt,BPJSTkPendaftaranFee, BPJSTkPembayaranAmt,BPJSTkPembayaranFee;
        BPJSTkPendaftaranAmt = total.getTransTotalAmt()[16][2];
        BPJSTkPendaftaranFee = total.getTransTotalAmt()[16][1];
        BPJSTkPembayaranAmt = total.getTransTotalAmt()[17][2];
        BPJSTkPembayaranFee = total.getTransTotalAmt()[17][1];


        if(BPJSTkPendaftaranAmt == 0){
            BPJSTkPendaftaranAmtTv.setText(currency.getName() + " " +BPJSTkPendaftaranAmt );
        }else {
            BPJSTkPendaftaranAmtTv.setText(currency.getName() + " " + FinancialApplication.getConvert().amountMinUnitToMajor( String.valueOf(BPJSTkPendaftaranAmt) ,
                    currency.getCurrencyExponent(), true) );
        }

        if(BPJSTkPendaftaranFee == 0){
            BPJSTkPendaftaranFeeTv.setText(currency.getName() + " " +BPJSTkPendaftaranFee );
        }else {
            BPJSTkPendaftaranFeeTv.setText(currency.getName() + " " + FinancialApplication.getConvert().amountMinUnitToMajor( String.valueOf(BPJSTkPendaftaranFee) ,
                    currency.getCurrencyExponent(), true) );
        }


        if(BPJSTkPembayaranAmt == 0){
            BPJSTkPembayaranAmtTv.setText(currency.getName() + " " +BPJSTkPembayaranAmt );
        }else {
            BPJSTkPembayaranAmtTv.setText(currency.getName() + " " + FinancialApplication.getConvert().amountMinUnitToMajor( String.valueOf(BPJSTkPembayaranAmt) ,
                    currency.getCurrencyExponent(), true) );
        }

        if(BPJSTkPembayaranAmt == 0){
            BPJSTkPembayaranFeeTv.setText(currency.getName() + " " +BPJSTkPembayaranFee );
        }else {
            BPJSTkPembayaranFeeTv.setText(currency.getName() + " " + FinancialApplication.getConvert().amountMinUnitToMajor( String.valueOf(BPJSTkPembayaranFee) ,
                    currency.getCurrencyExponent(), true) );
        }







    }


    private String getAmount(String amt) {
        try {
            String amtSubs;
            if (amt.equals("0")){
                amtSubs = amt;
            }else {
                amtSubs = amt.substring(0, amt.length() - 2);
            }

            long amount = Long.parseLong(amtSubs);
            String temp = FinancialApplication.getConvert().amountMinUnitToMajor(String.valueOf(amount),
                    currency.getCurrencyExponent(), true);
            temp = currency.getName() + " " + temp;
            return temp;
        } catch (Exception e) {
            return "";
        }
    }
}
