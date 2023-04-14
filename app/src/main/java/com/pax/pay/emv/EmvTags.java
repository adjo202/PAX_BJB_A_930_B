package com.pax.pay.emv;

import android.util.Log;

import com.pax.eemv.IEmvBase;
import com.pax.gl.packer.ITlv;
import com.pax.gl.packer.ITlv.ITlvDataObj;
import com.pax.gl.packer.ITlv.ITlvDataObjList;
import com.pax.gl.packer.TlvException;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.trans.model.ETransType;

public class EmvTags {

    private static final String TAG = "EmvTags";

    /**
     * 消费55域EMV标签
     */
    public static final int[] TAGS_SALE = {0x9F26, 0x9F27, 0x9F10, 0x9F37, 0x9F36, 0x95, 0x9A, 0x9C, 0x9F02, 0x5F2A,
            0x82, 0x9F1A, 0x9F03, 0x9F33, 0x9F34, 0x9F35, 0x9F1E, 0x84, 0x9F09, 0x9F41, 0x9F63, 0x5F34 };
    /**
     * 查余额55域EMV标签
     */
    public static final int[] TAGS_QUE = {0x9F26, 0x9F27, 0x9F10, 0x9F37, 0x9F36, 0x95, 0x9A, 0x9C, 0x9F02, 0x5F2A,
            0x82, 0x9F1A, 0x9F03, 0x9F33, 0x9F34, 0x9F35, 0x9F1E, 0x84, 0x9F09, 0x9F41, 0x9F63};
    /**
     * 脱机消费（PBOC）55域EMV标签
     */
    public static final int[] TAGS_PBOC_OFFLINE = {0x9F26, 0x9F27, 0x9F10, 0x9F37, 0x9F36, 0x95, 0x9A, 0x9C, 0x9F02,
            0x5F2A, 0x82, 0x9F1A, 0x9F03, 0x9F33, 0x9F1E, 0x84, 0x9F09, 0x9F41, 0x9F34, 0x9F35, 0x9F63, 0x8A};
    /**
     * 脱机消费（EC）55域EMV标签
     */
    public static final int[] TAGS_EC_OFFLINE = {0x9F26, 0x9F27, 0x9F10, 0x9F37, 0x9F36, 0x95, 0x9A, 0x9C, 0x9F02,
            0x5F2A, 0x82, 0x9F1A, 0x9F03, 0x9F33, 0x9F1E, 0x84, 0x9F09, 0x9F41, 0x9F34, 0x9F35, 0x9F63, 0x9F74, 0x8A};
    /**
     * 预授权55域EMV标签
     */
    public static final int[] TAGS_AUTH = {0x9F26, 0x9F27, 0x9F10, 0x9F37, 0x9F36, 0x95, 0x9A, 0x9C, 0x9F02, 0x5F2A,
            0x82, 0x9F1A, 0x9F03, 0x9F33, 0x9F34, 0x9F35, 0x9F1E, 0x84, 0x9F09, 0x9F41, 0x9F63};

    /**
     * 冲正
     */
    public static final int[] TAGS_DUP = {0x95, 0x9F10, 0x9F1E, 0xDF31};

    /**
     * 交易承兑但卡片拒绝时发起的冲正
     */
    public static final int[] TAGS_POSACCPDUP = {0x95, 0x9F10, 0x9F1E, 0x9F36, 0xDF31};

    /**
     * 脚本结果上送
     */
    public static final int[] TAGS_SCRIPT = {0x9F33, 0x95, 0x9F37, 0x9F1E, 0x9F10, 0x9F26, 0x9F36, 0x82, 0xDF31,
            0x9F1A, 0x9A};

    /**
     * 指定账户圈存55域EMV标签
     */
    public static final int[] TAGS_ECLOAD = {0x9F26, 0x9F27, 0x9F10, 0x9F37, 0x9F36, 0x95, 0x9A, 0x9C, 0x9F02, 0x5F2A,
            0x82, 0x9F1A, 0x9F03, 0x9F33, 0x9F34, 0x9F35, 0x9F1E, 0x84, 0x9F09, 0x9F41, 0x9F63};
    /**
     * 指定账户圈存冲正
     */
    public static final int[] TAGS_ECLOAD_REV = {0x95, 0x9F1E, 0x9F10, 0x9F36, 0xDF31};

    public static final int[] TAGS_NECLOAD = {0x9F26, 0x9F27, 0x9F10, 0x9F37, 0x9F36, 0x95, 0x9A, 0x9C, 0x9F02,
            0x5F2A, 0x82, 0x9F1A, 0x9F03, 0x9F33, 0x9F34, 0x9F35, 0x9F1E, 0x84, 0x9F09, 0x9F41, 0x9F63};

    /**
     * 电子现金现金充值55域EMV标签
     */
    public static final int[] TAGS_EC_CASH_LOAD = {0x9F26, 0x9F27, 0x9F10, 0x9F37, 0x9F36, 0x95, 0x9A, 0x9C, 0x9F02,
            0x5F2A, 0x82, 0x9F1A, 0x9F03, 0x9F33, 0x9F34, 0x9F35, 0x9F1E, 0x84, 0x9F09, 0x9F41, 0x9F63};
    /**
     * 电子现金现金充值撤销55域EMV标签
     */
    public static final int[] TAGS_EC_CASH_LOAD_VOID = {0x9F1A, 0x9F03, 0x9F33, 0x9F34, 0x9F35, 0x9F1E, 0x84, 0x9F09,
            0x9F41, 0x9F63, 0x9F26, 0x9F27, 0x9F10, 0x9F37, 0x9F36, 0x95, 0x9A, 0x9C, 0x9F02, 0x5F2A, 0x82};

    /**
     * 分期付款EMV标签
     */
    public static final int[] TAGS_INSTALLMENT_SALE = {0x9F33, 0x95, 0x9F1E, 0x9F10, 0x9F26, 0x9F36, 0x82, 0x9C, 0x9F1A,
            0x9A, 0x9F02, 0x5F2A, 0x9F03, 0x9F35, 0x9F34, 0x9F37, 0x9F27, 0x9F41};



    private EmvTags() {

    }

    /**
     * Sandy : Get 55 domain TLV data based on transaction type
     *
     * @param transType
     * @param isDup
     * @param isEC
     * @return
     */
    public static byte[] getF55(IEmvBase emv, ETransType transType, boolean isDup, boolean isEC) {
        switch (transType) {
            case SALE:
            case BALANCE_INQUIRY:
            case BALANCE_INQUIRY_2: //sandy
            case ACCOUNT_LIST:
            case COUPON_VERIFY:
            case OVERBOOK_INQUIRY: // add abdul
            case TARIK_TUNAI: // add abdul
            case TARIK_TUNAI_2: // add sandy

            case SETOR_TUNAI: // add abdul
            case OVERBOOKING_PULSA_DATA: // add abdul
            case BPJS_OVERBOOKING: //add sandy
            case PASCABAYAR_OVERBOOKING:
            case PDAM_OVERBOOKING:
            case COUPON_SALE:
            case MINISTATEMENT:
            case PBB_INQ:
            case PBB_PAY:
            case PEMBATAL_REK:
            case PEMBUKAAN_REK:
            case DIRJEN_ANGGARAN:
            case DIRJEN_BEA_CUKAI:
            case DIRJEN_PAJAK:
            case CETAK_ULANG:
            case TRANSFER:
            case TRANSFER_2: //sandy
            case TRANSFER_INQ:
            case TRANSFER_INQ_2: //sandy
            case OVERBOOKING:
            case OVERBOOKING_2:
            case REDEEM_POIN_DATA_INQ: //sandy
            case REDEEM_POIN_DATA_PAY: //sandy

            case E_SAMSAT:
            case E_SAMSAT_INQUIRY:
                if (isDup) {
                    return getValueList(emv, TAGS_DUP);
                }
                if (isEC) {
                    return getValueList(emv, TAGS_EC_OFFLINE);
                }
                return getValueList(emv, TAGS_SALE);
            case QUERY:
                return getValueList(emv, TAGS_QUE);
            case AUTH:
                if (isDup) {
                    return getValueList(emv, TAGS_DUP);
                }
                return getValueList(emv, TAGS_AUTH);
            case IC_SCR_SEND:
                return getValueList(emv, TAGS_SCRIPT);
            case EC_SALE:
                return getValueList(emv, TAGS_EC_OFFLINE);
            case EC_LOAD:
                if (isDup) {
                    return getValueList(emv, TAGS_ECLOAD_REV);
                }
                return getValueList(emv, TAGS_ECLOAD);
            case EC_CASH_LOAD:
                return getValueList(emv, TAGS_EC_CASH_LOAD);
            case EC_CASH_LOAD_VOID:
                if (isDup) {
                    return getValueList(emv, TAGS_DUP);
                }
                return getValueList(emv, TAGS_EC_CASH_LOAD_VOID);
            case EC_TRANSFER_LOAD:
                return getValueList(emv, TAGS_NECLOAD);
            case INSTAL_SALE:
                return getValueList(emv, TAGS_INSTALLMENT_SALE);
            default:
                break;
        }
        return null;
    }

    public static byte[] getF55forPosAccpDup(IEmvBase emv) {
        return getValueList(emv, TAGS_POSACCPDUP);
    }

    public static byte[] getValueList(IEmvBase emv, int[] tags) {
        if (tags == null || tags.length == 0) {
            return null;
        }

        ITlv tlv = FinancialApplication.getPacker().getTlv();
        ITlvDataObjList tlvList = tlv.createTlvDataObjectList();
        for (int tag : tags) {
            try {
                byte[] value = emv.getTlv(tag);
                if (value == null || value.length == 0) {
                    if (tag != 0x9f03) {
                        continue;
                    }
                    value = new byte[6];
                }
                Log.d(TAG, "Sandy.EMV=(" + Integer.toHexString(tag) + ")"+ FinancialApplication.getConvert().bcdToStr(value));
                ITlvDataObj obj = tlv.createTlvDataObject();
                obj.setTag(tag);
                obj.setValue(value);
                tlvList.addDataObj(obj);
            } catch (Exception e) {
                Log.e(TAG, "", e);
            }
        }

        try {
            return tlv.pack(tlvList);
        } catch (TlvException e) {
            Log.e(TAG, "", e);
        }

        return null;
    }
}
