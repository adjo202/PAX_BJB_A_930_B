package com.pax.pay.emv;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.ConditionVariable;
import android.os.Handler;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;

import com.pax.abl.utils.PanUtils;
import com.pax.abl.utils.TrackUtils;
import com.pax.eemv.IEmv;
import com.pax.eemv.IEmvListener;
import com.pax.eemv.entity.Amounts;
import com.pax.eemv.entity.CandList;
import com.pax.eemv.enums.EOnlineResult;
import com.pax.eemv.exception.EEmvExceptions;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.constant.Constants;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.trans.transmit.TransProcessListener;
import com.pax.up.bjb.R;
import com.pax.view.dialog.CustomAlertDialog;
import com.pax.view.dialog.CustomAlertDialog.OnCustomClickListener;

import java.util.List;

public class EmvListenerImpl extends EmvBaseListenerImpl implements IEmvListener {
    public static final String TAG = "EmvListenerImpl";
    private Context context;
    private Handler handler;
    private IEmv emv ;
    private boolean supportOnlinePin;

    public EmvListenerImpl(Context context, IEmv emv, Handler handler, TransData transData, TransProcessListener listener) {
        super(context, emv, transData, listener);
        this.context = context;
        this.handler = handler;
        this.intResult = -1;
        this.supportOnlinePin = true;
        this.emv = emv;
    }




    @Override
    public int onCardHolderPwd(final boolean isOnlinePin, final int offlinePinLeftTimes, byte[] pinData) {
        Log.d(TAG, "Sandy.EmvListenerImpl.onCardHolderPwd is called!" + isOnlinePin);
        if (!supportOnlinePin) {
            return EEmvExceptions.EMV_ERR_NO_PASSWORD.getErrCodeFromBasement();
        }

        if (transProcessListener != null) {
            transProcessListener.onHideProgress();
        }
        //Sandy : register here if you want to skip the password/pin
        if (transData.getTransType().equals(ETransType.EC_CASH_LOAD.toString())
                || transData.getTransType().equals(ETransType.TARIK_TUNAI.toString()) // add abdul
                || transData.getTransType().equals(ETransType.TARIK_TUNAI_2.toString()) // add sandy
                || transData.getTransType().equals(ETransType.PEMBATAL_REK.toString()) // add tri
                || transData.getTransType().equals(ETransType.PEMBUKAAN_REK.toString()) // add tri
                || transData.getTransType().equals(ETransType.DIRJEN_PAJAK.toString()) // add tri
                || transData.getTransType().equals(ETransType.DIRJEN_BEA_CUKAI.toString()) // add tri
                || transData.getTransType().equals(ETransType.DIRJEN_ANGGARAN.toString()) // add tri
                || transData.getTransType().equals(ETransType.TRANSFER.toString()) // add tri
                || transData.getTransType().equals(ETransType.TRANSFER_2.toString()) // add sandy
                || transData.getTransType().equals(ETransType.TRANSFER_INQ.toString()) // add tri
                || transData.getTransType().equals(ETransType.TRANSFER_INQ_2.toString()) // add sandy
                || transData.getTransType().equals(ETransType.OVERBOOKING.toString()) // add tri
                || transData.getTransType().equals(ETransType.OVERBOOKING_2.toString()) // add sandy
                || transData.getTransType().equals(ETransType.REDEEM_POIN_DATA_INQ.toString()) // add sandy
                || transData.getTransType().equals(ETransType.BPJS_OVERBOOKING.toString()) // add sandy


                || transData.getTransType().equals(ETransType.E_SAMSAT_INQUIRY.toString()) // add tri
                || transData.getTransType().equals(ETransType.E_SAMSAT.toString()) // add tri
                || transData.getTransType().equals(ETransType.PASCABAYAR_OVERBOOKING.toString()) // add tri
                || transData.getTransType().equals(ETransType.PDAM_OVERBOOKING.toString()) // add tri
                || transData.getTransType().equals(ETransType.SETOR_TUNAI.toString()) // add abdul
                || transData.getTransType().equals(ETransType.PBB_INQ.toString())   //add denny
                || transData.getTransType().equals(ETransType.PBB_PAY.toString())   //add denny
                || transData.getTransType().equals(ETransType.EC_CASH_LOAD_VOID.toString())) {
            // Electronic cash deposit (cash recharge) does not require a password
            return 0;
        }
        cv = new ConditionVariable();
        intResult = 0;

        enterPin(isOnlinePin);

        if (isOnlinePin) {
            cv.block();
        }
        return intResult;
    }


    @Override
    public boolean onChkExceptionFile() {
        byte[] track2 = emv.getTlv(0x57);
        String strTrack2 = FinancialApplication.getConvert().bcdToStr(track2);
        strTrack2 = strTrack2.split("F")[0];
        // 卡号
        String pan = TrackUtils.getPan(strTrack2);
        boolean ret = CardBin.isInBlack(pan);
        if (ret) {
            transProcessListener.onShowErrMessageWithConfirm(context.getString(R.string.emv_card_in_black_list),
                    Constants.FAILED_DIALOG_SHOW_TIME);
            return true;
        }

        return false;
    }

    @Override
    public int onConfirmCardNo(final String cardno) {
        if (transProcessListener != null) {
            transProcessListener.onHideProgress();
        }
        cv = new ConditionVariable();
        handler.post(new Runnable() {

            @Override
            public void run() {
                CustomAlertDialog dialog = new CustomAlertDialog(context, CustomAlertDialog.NORMAL_TYPE);
                dialog.setTitleText(context.getString(R.string.emv_cardno_confirm));
                dialog.setContentText(PanUtils.separateWithSpace(cardno));
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();

                dialog.showCancelButton(true);
                dialog.showConfirmButton(true);
                dialog.setCancelClickListener(new OnCustomClickListener() {

                    @Override
                    public void onClick(CustomAlertDialog alertDialog) {
                        intResult = EEmvExceptions.EMV_ERR_USER_CANCEL.getErrCodeFromBasement();
                        alertDialog.dismiss();
                        cv.open();
                    }
                });
                dialog.setConfirmClickListener(new OnCustomClickListener() {

                    @Override
                    public void onClick(CustomAlertDialog alertDialog) {
                        intResult = EEmvExceptions.EMV_OK.getErrCodeFromBasement();
                        alertDialog.dismiss();
                        cv.open();
                    }
                });
            }
        });

        cv.block();
        return intResult;
    }

    @Override
    public Amounts onGetAmounts() {
        Amounts amt = new Amounts();
        amt.setTransAmount(transData.getAmount());
        return amt;
    }

    @Override
    public EOnlineResult onOnlineProc() {
        return onlineProc();
    }


    @Override
    public int onWaitAppSelect(final boolean arg0, final List<CandList> arg1) {
        if (transProcessListener != null) {
            transProcessListener.onHideProgress();
        }
        cv = new ConditionVariable();
        handler.post(new Runnable() {

            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                if (arg0) {
                    builder.setTitle(context.getString(R.string.emv_application_choose));
                } else {
                    SpannableString sstr = new SpannableString(context.getString(R.string.emv_application_choose_again));
                    sstr.setSpan(new ForegroundColorSpan(Color.RED), 5, 9, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    builder.setTitle(sstr);
                }
                String[] appNames = new String[arg1.size()];
                for (int i = 0; i < appNames.length; i++) {
                    appNames[i] = (String) arg1.get(i).getAppName();
                }
                builder.setSingleChoiceItems(appNames, -1, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        intResult = arg1;
                        arg0.dismiss();
                        cv.open();
                    }
                });

                builder.setPositiveButton(context.getString(R.string.dialog_cancel),
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                intResult = EEmvExceptions.EMV_ERR_USER_CANCEL.getErrCodeFromBasement();
                                arg0.dismiss();
                                cv.open();
                            }
                        });
                builder.setCancelable(false);
                builder.create().show();

            }
        });

        cv.block();
        return intResult;
    }
}
