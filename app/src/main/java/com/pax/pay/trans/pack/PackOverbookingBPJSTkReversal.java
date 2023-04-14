package com.pax.pay.trans.pack;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.pax.abl.core.ipacker.PackListener;
import com.pax.gl.packer.Iso8583Exception;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.utils.Fox;
import com.pax.settings.SysParam;

public class PackOverbookingBPJSTkReversal extends PackIso8583 {

    SysParam sysParam = FinancialApplication.getSysParam();

    public PackOverbookingBPJSTkReversal(PackListener listener) {
        super(listener, true);
    }

    @Override
    protected int[] getRequiredFields() {
        return new int[]{2, 3, 4, 7, 11, 12, 15, 18, 22, 28, 32, 33, 35, 37, 41, 42, 43, 49, 56, 90};
    }

    /*@Override
    protected void setBitData11(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("11", String.valueOf(transData.getDupTransNo()));
    }*/

    @Override
    protected void setBitData15(@NonNull TransData transData) throws Iso8583Exception {
        String settleDate = transData.getSettleDate();
        Log.i("abdul", "bit 15 = " + settleDate);

        if (!TextUtils.isEmpty(settleDate)) {
            setBitData("15", settleDate);
        }

        if (settleDate == null) {
            String bit15 = String.valueOf(transData.getDateTimeTrans()).substring(0, 3);
            setBitData("15", bit15);
            Log.i("abdul", "bit 15 null set = " + bit15);
        }
    }

    @Override
    protected void setBitData18(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("18", sysParam.get(SysParam.MCC));
    }

    @Override
    protected void setBitData28(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("28", transData.getFeeTotalAmount());
    }

    @Override
    protected void setBitData32(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("32", "000110");
    }

    @Override
    protected void setBitData33(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("33", "000110");
    }

    /*@Override
    protected void setBitData37(@NonNull TransData transData) throws Iso8583Exception {
        try {
            Log.i("abdul", "pack overpls refno = " + transData.getRefNo());
            if (transData.getRefNo() != null && transData.getRefNo().length() > 0) {
                setBitData("37", transData.getRefNo());
            } else {
                String padd = String.valueOf(transData.getTransNo());
                String bit7 = String.valueOf(transData.getOrigDateTimeTrans());
                bit7 = StringUtils.leftPad(bit7, 10, "0");
                padd = StringUtils.leftPad(padd, 6, "0");
                setBitData("37", bit7 + padd.substring(4, 6)); // format 37 pakai datetime MMddHHmmss Pak
            }
        } catch (Exception e) {
            String padd = String.valueOf(transData.getTransNo());
            String bit7 = String.valueOf(transData.getOrigDateTimeTrans());
            bit7 = StringUtils.leftPad(bit7, 10, "0");
            padd = StringUtils.leftPad(padd, 6, "0");
            setBitData("37", bit7 + padd.substring(4, 6)); // format 37 pakai datetime MMddHHmmss Pak
        }
    }*/

    @Override
    protected void setBitData43(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("43", sysParam.get(SysParam.MERCH_EN));
    }

    @Override
    protected void setBitData56(@NonNull TransData transData) throws Iso8583Exception {
        String pcode = "";
        try {
            String[] f47 = transData.getField47().split("#"); // di 47 ada data pulsa data
            pcode = f47[2];
            if (pcode.contains("-")){
                String[] temp = pcode.split("-");
                Log.d("teg", "temp : "+temp[0]+" "+temp[1]);
                pcode = temp[1];
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        setBitData("56", transData.getPhoneNo() + "|" + pcode);

        /*try {
            String[] f47 = transData.getField47().split("#"); // di 47 ada data pulsa data
            setBitData("56", f47[1] + "|" + f47[2]);
        } catch (Exception e) {
            e.printStackTrace();
            setBitData("56", transData.getPhoneNo());
        }*/
    }

    @Override
    protected void setBitData90(@NonNull TransData transData) throws Iso8583Exception {
        //Original Message Type n4
        //Original STAN n6
        //Original Transmission Date and Time n10ce
        //Original Acquiring Institution ID Code Left justify, padded with space n11
        //Original Forwarding Institution ID n11 Left justify, padded with space n11

        String data = ETransType.PDAM_OVERBOOKING.getMsgType();
        data += Component.getPaddedString(String.valueOf(transData.getTransNo()), 6, '0');
        data += transData.getDate() + transData.getOrigCouponDateTimeTrans(); //numpang variable
        data += Fox.paddingKanan("000110", 11);
        data += Fox.paddingKanan("000110", 11);
        Log.i("dia21","data: "+data);

        setBitData("90", data);
    }

}
