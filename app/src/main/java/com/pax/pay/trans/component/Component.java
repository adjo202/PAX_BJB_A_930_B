/*******************************************************************************
 * ============================================================================
 * COPYRIGHT
 *               Pax CORPORATION PROPRIETARY INFORMATION
 *    This software is supplied under the terms of a license agreement or
 *    nondisclosure agreement with Pax Corporation and may not be copied
 *    or disclosed except in accordance with the terms in that agreement.
 *       Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 *  Module Date: 17-5-22 上午11:26
 *  Module Author: liliang
 *  Description:
 *  ============================================================================
 ******************************************************************************/

package com.pax.pay.trans.component;

import android.content.Context;
import android.content.DialogInterface.OnDismissListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.os.StatFs;
import android.support.annotation.NonNull;
import android.text.format.Time;
import android.util.Log;

import com.pax.abl.utils.TrackUtils;
import com.pax.dal.entity.EPedDesMode;
import com.pax.dal.entity.EPedType;
import com.pax.dal.entity.EPiccType;
import com.pax.dal.exceptions.PedDevException;
import com.pax.dal.exceptions.PiccDevException;
import com.pax.device.Device;
import com.pax.eemv.IEmv;
import com.pax.eemv.entity.ClssInputParam;
import com.pax.eemv.entity.Config;
import com.pax.eemv.entity.InputParam;
import com.pax.eemv.enums.ECvmResult;
import com.pax.eemv.enums.EFlowType;
import com.pax.eemv.enums.ETransResult;
import com.pax.eemv.exception.EmvException;
import com.pax.gl.convert.IConvert;
import com.pax.gl.convert.IConvert.EPaddingPosition;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.constant.Constants;
import com.pax.pay.emv.EmvAid;
import com.pax.pay.emv.EmvTags;
import com.pax.pay.emv.EmvTransProcess;
import com.pax.pay.trans.TransContext;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.action.ActionSearchCard.SearchMode;
import com.pax.pay.trans.model.Controller;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.trans.model.TransData.ETransStatus;
import com.pax.pay.trans.model.TransData.EnterMode;
import com.pax.pay.trans.transmit.TransOnline;
import com.pax.pay.trans.transmit.TransProcessListenerImpl;
import com.pax.settings.SysParam;
import com.pax.settings.SysParam.Constant;
import com.pax.settings.currency.Currency;
import com.pax.up.bjb.R;
import com.pax.view.dialog.CustomAlertDialog;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Component {
    private static final String TAG = "Component";

    private static final long MAX_TRANS_NO = 999999;
    private static final long MAX_BATCH_NO = 999999;

    private static final byte ONLINEPIN_CVM = (byte) 0x80;
    private static final byte SIGNATURE_CVM = 0x40;
    private static final byte CD_CVM = (byte) 0x80;

    /**
     * 交易预处理，检查是否签到， 是否需要结算， 是否继续批上送， 是否支持该交易， 是否需要参数下载
     *
     * @param context
     * @param transType
     * @return
     */
    public static int transPreDeal(final Context context, ETransType transType) {
        if (!isNeedPreDeal(transType)) {  //不需要预处理那就返回
            return TransResult.SUCC;
        }
        // 检测电量状态，暂不处理，后续再确定需不需要 fix me
        // 判断终端签到状态
        if (!isLogon()) {
            return TransResult.ERR_NOT_LOGON;
        }
        // 判断是否需要结算
        int ret = checkSettle();
        if (ret != TransResult.SUCC) {
            return ret;
        }
        // 批上送断点
        if (isNeedBatchuUp()) {
            return TransResult.ERR_BATCH_UP_NOT_COMPLETED;
        }
        // 根据交易类型判断是否支持此交易
        if (!isSupportTran(transType)) {
            return TransResult.ERR_NOT_SUPPORT_TRANS;
        }
        // 判断是否有参数下载
        // 全部参数下载
        TransProcessListenerImpl listenerImpl = new TransProcessListenerImpl(context);
        ret = TransOnline.downLoadCheck(true, true, listenerImpl);
        if (listenerImpl != null) {
            listenerImpl.onHideProgress();
        }
        // EMV内核初始化
        // 配置终端相关参数

        // EMV初始化， 交易开始前设置emv配置，capk和aid，减少后面emv处理时间
        EmvTransProcess.getInstance().init();
        return ret;
    }

    public static void getIccInterOrgCode(@NonNull TransData transData) {
        if (transData.getEnterMode() != SearchMode.INSERT) {
            return;
        }
        byte[] aid = FinancialApplication.getConvert().strToBcd(transData.getAid(), EPaddingPosition.PADDING_LEFT);
        boolean ret = FinancialApplication.getGl().getUtils().isByteArrayValueSame(aid, 0, new byte[]{(byte) 0xA0, 0x00, 0x00, 0x03, 0x33}, 0, 5);
        if (ret) {
            transData.setInterOrgCode("CUP");
        } else {
            if ("4".equals(transData.getPan().substring(0, 1))) {
                transData.setInterOrgCode("VIS");
            } else if ("5".equals(transData.getPan().substring(0, 1))) {
                transData.setInterOrgCode("MCC");
            } else if ("34".equals(transData.getPan().substring(0, 2)) || "37".equals(transData.getPan().substring(0, 2))) {
                transData.setInterOrgCode("AMX");
            } else if ("30".equals(transData.getPan().substring(0, 2)) || "36".equals(transData.getPan().substring(0, 2))
                    || "38".equals(transData.getPan().substring(0, 2)) || "39".equals(transData.getPan().substring(0, 2))) {
                transData.setInterOrgCode("DCC");
            } else if ("35".equals(transData.getPan().substring(0, 2))) {
                transData.setInterOrgCode("JCB");
            }
        }

    }

    /**
     * 获取读卡方式
     *
     * @param transType ：交易类型{@link ETransType}
     * @return
     */
    public static byte getCardReadMode(ETransType transType) {
        byte mode = SearchMode.SWIPE;

        switch (transType) {
            case SALE:
            case COUPON_SALE:
            case COUPON_VERIFY:
                //case BALANCE_INQUIRY:
            case PEMBUKAAN_REK:
            case PEMBATAL_REK:
            case DIRJEN_PAJAK_INQUIRY:
            case DIRJEN_BEA_CUKAI_INQUIRY:
            case DIRJEN_ANGGARAN_INQUIRY:
            case CETAK_ULANG:
            case TRANSFER:
            case ACCOUNT_LIST: // add abdul
            case REDEEM_POIN_DATA_INQ: //add sandy
            case REDEEM_POIN_DATA_PAY: //add sandy
            case OVERBOOK_INQUIRY: // add abdul
            case OVERBOOKING_PULSA_DATA: // add abdul
            case BPJS_OVERBOOKING: //add sandy
            case VOID:
            case QUERY:
            case REFUND:
            case READCARDNO:
            case MINISTATEMENT:
            case CHANGE_PIN:
            case VERIFY_PIN:
            case PBB_INQ:
            case SETOR_TUNAI:
            case OVERBOOKING:
            case OVERBOOK_INQUIRY1:
            case E_SAMSAT_INQUIRY: //ppobnew
            case PDAM_INQUIRY: //ppobnew pascabayar
            case PASCABAYAR_INQUIRY: //ppobnew pascabayar
            case BPJS_TK_PENDAFTARAN: //sandy bpjs register
            case BPJS_TK_PEMBAYARAN: //sandy bpjs payment
                //Support insert card, wave card
                mode |= SearchMode.INSERT | SearchMode.TAP;
                break;
            case AUTH:
            case AUTHVOID:
            case AUTHCM:
            case AUTH_SETTLEMENT:
            case AUTHCMVOID:
                // 支持插卡、挥卡
                mode |= SearchMode.INSERT | SearchMode.TAP;
                if (FinancialApplication.getSysParam().get(SysParam.OTHTC_KEYIN).equals(Constant.YES)) {
                    mode |= SearchMode.KEYIN;
                }
                break;
            case INSTAL_SALE:
            case INSTAL_VOID:
                // 支持插卡
                mode |= SearchMode.INSERT;
                break;
            case EC_QUERY:
            case EC_SALE:
            case EC_DETAIL:
            case EC_REFUND:
                mode |= SearchMode.INSERT | SearchMode.TAP;
                mode ^= SearchMode.SWIPE;
                break;
            case MOTO_SALE:
            case MOTO_VOID:
            case MOTO_REFUND:
            case MOTO_AUTH:
            case MOTO_AUTHCM:
            case MOTO_AUTHVOID:
            case MOTO_AUTHCMVOID:
            case MOTO_AUTH_SETTLEMENT:
            case RECURRING_SALE:
            case RECURRING_VOID:
                mode ^= SearchMode.SWIPE;
                mode |= SearchMode.KEYIN;
                break;
            default:
                break;
        }
        if (!FinancialApplication.getSysParam().get(SysParam.QUICK_PASS_TRANS_SWITCH).equals(SysParam.Constant.YES)) {
            mode &= ~SearchMode.TAP;
        }
        if (!FinancialApplication.getSysParam().get(SysParam.OTHTC_SUPP_EMV).equals(SysParam.Constant.YES)) {
            mode &= ~SearchMode.INSERT;
        }
        return mode;
    }

    /**
     * 根据交易类型、冲正标识确认当前交易是否预处理
     *
     * @param transType
     * @return true:需要预处理 false:不需要预处理 备注：签到，签退，结算，参数下发，公钥下载，冲正类不需要预处理,新增交易类型时，需修改添加交易类型判断
     */
    private static boolean isNeedPreDeal(ETransType transType) {
        if (transType == ETransType.LOGON   //签到
                || transType == ETransType.LOGOUT  //签退
                // 上送交易
                // 批上送类
                // 参数下载类
                || transType == ETransType.EMV_MON_CA || transType == ETransType.EMV_CA_DOWN //IC卡公钥下载状态上送|IC卡公钥下载
                || transType == ETransType.EMV_CA_DOWN_END || transType == ETransType.EMV_MON_PARAM //IC卡公钥下载结束|IC卡参数下载状态上送
                //|| transType == ETransType.EMV_PARAM_DOWN || transType == ETransType.EMV_CA_DOWN_END //IC卡参数下载|IC卡公钥下载结束
                || transType == ETransType.EMV_PARAM_DOWN || transType == ETransType.EMV_PARAM_DOWN_END //IC卡参数下载|IC卡参数下载结束, modified by steven 20170401, repeat EMV_CA_DOWN_END
                || transType == ETransType.BLACK_DOWN || transType == ETransType.BLACK_DOWN_END //黑名单下载|黑名单下载结束
                // 结算
                || transType == ETransType.SETTLE
            // 冲正类交易
        ) {
            return false;
        }
        return true;
    }

    /**
     * 判断终端是否签到
     *
     * @return true：已签到 false：未签到
     */
    private static boolean isLogon() {
        return FinancialApplication.getController().get(Controller.POS_LOGON_STATUS) ==
                Controller.Constant.YES;
    }

    /**
     * 检查是否达结算要求
     *
     * @return 0：不用结算 1：结算提醒,立即 2：结算提醒，稍后 3：结算提醒,空间不足
     */
    private static int checkSettle() {
        // 获取交易笔数
        long cnt = TransData.getTransCount();
        // 获取允许的最大交易笔数
        long maxCnt = Long.MAX_VALUE;
        String temp = FinancialApplication.getSysParam().get(SysParam.MAX_TRANS_COUNT);
        if (temp != null) {
            maxCnt = Long.parseLong(temp);
        }
        // 判断交易笔数是否超限
        if (cnt >= maxCnt) {
            if (cnt >= maxCnt + 10) {
                return TransResult.ERR_NEED_SETTLE_NOW; // 结算提醒,立即
            } else {
                return TransResult.ERR_NEED_SETTLE_LATER; // 结算提醒,稍后
            }
        }
        // 判断存储空间大小
        if (!hasFreeSpace()) {
            return TransResult.ERR_NO_FREE_SPACE; // 存储空间不足,需要结算
        }
        return TransResult.SUCC; // 不用结算
    }

    /**
     * 判断是否有剩余空间
     *
     * @return true: 有空间 false：无空间
     */
    @SuppressWarnings("deprecation")
    private static boolean hasFreeSpace() {
        File datapath = Environment.getDataDirectory(); //Gets the Android data directory.
        StatFs dataFs = new StatFs(datapath.getPath()); //Retrieve overall information about the space on a filesystem
        long sizes = (long) dataFs.getFreeBlocks() * (long) dataFs.getBlockSize(); //The total number of blocks that are free on the file system, including reserved blocks
        long available = sizes / (1024 * 1024); //M            //The size, in bytes, of a block on the file system.

        return available >= 1;
    }

    private static boolean isNeedBatchuUp() {
        return FinancialApplication.getController().get(Controller.BATCH_UP_STATUS) ==
                Controller.Constant.BATCH_UP;
    }

    /**
     * 判断是否支持该交易
     *
     * @param transType
     * @return
     */
    private static boolean isSupportTran(ETransType transType) {
        switch (transType) {
            case SALE:
                return FinancialApplication.getSysParam().get(SysParam.TTS_SALE).equals(SysParam.Constant.YES);
            case VOID:
                return FinancialApplication.getSysParam().get(SysParam.TTS_VOID).equals(SysParam.Constant.YES);
            case REFUND:
                return FinancialApplication.getSysParam().get(SysParam.TTS_REFUND).equals(SysParam.Constant.YES);
            case QUERY:
                return FinancialApplication.getSysParam().get(SysParam.TTS_BALANCE).equals(SysParam.Constant.YES);
            case AUTH:
                return FinancialApplication.getSysParam().get(SysParam.TTS_PREAUTH).equals(SysParam.Constant.YES);
            case AUTHVOID:
                return FinancialApplication.getSysParam().get(SysParam.TTS_PAVOID).equals(SysParam.Constant.YES);
            case AUTHCM:
                return FinancialApplication.getSysParam().get(SysParam.TTS_PACREQUEST).equals(SysParam.Constant.YES);
            case AUTHCMVOID:
                return FinancialApplication.getSysParam().get(SysParam.TTS_PACVOID).equals(SysParam.Constant.YES);
            case AUTH_SETTLEMENT:
                return FinancialApplication.getSysParam().get(SysParam.TTS_PACADVISE).equals(SysParam.Constant.YES);
            case EC_SALE:
                return FinancialApplication.getSysParam().get(SysParam.ECTS_SALE).equals(SysParam.Constant.YES);
            case EC_LOAD:
                return FinancialApplication.getSysParam().get(SysParam.ECTS_LOAD).equals(SysParam.Constant.YES);
            case EC_REFUND:
                return FinancialApplication.getSysParam().get(SysParam.ECTS_REFUND).equals(SysParam.Constant.YES);
            case EC_QUERY:
                return FinancialApplication.getSysParam().get(SysParam.ECTS_QUERY).equals(SysParam.Constant.YES);
            case EC_CASH_LOAD:
                return FinancialApplication.getSysParam().get(SysParam.ECTS_CALOAD).equals(SysParam.Constant.YES);
            case EC_CASH_LOAD_VOID:
                return FinancialApplication.getSysParam().get(SysParam.ECTS_CALOADVOID).equals(SysParam.Constant.YES);
            case EC_TRANSFER_LOAD:
            case EC_TRANSFER_LOAD_OUT:
                return FinancialApplication.getSysParam().get(SysParam.ECTS_TLOAD).equals(SysParam.Constant.YES);
            case SETTLE_ADJUST:
            case SETTLE_ADJUST_TIP:
                return FinancialApplication.getSysParam().get(SysParam.TTS_ADJUST).equals(SysParam.Constant.YES);
            case OFFLINE_SETTLE:
                return FinancialApplication.getSysParam().get(SysParam.TTS_OFFLINE_SETTLE).equals(SysParam.Constant.YES);
            case QR_SALE:
                return FinancialApplication.getSysParam().get(SysParam.SCTS_SALE).equals(SysParam.Constant.YES);
            case QR_VOID:
                return FinancialApplication.getSysParam().get(SysParam.SCTS_VOID).equals(SysParam.Constant.YES);
            case QR_REFUND:
                return FinancialApplication.getSysParam().get(SysParam.SCTS_REFUND).equals(SysParam.Constant.YES);
            case INSTAL_SALE:
                return FinancialApplication.getSysParam().get(SysParam.OTTS_INSTALLMENT).equals(SysParam.Constant.YES);
            case INSTAL_VOID:
                return FinancialApplication.getSysParam().get(SysParam.OTTS_INSTALLMENTVOID).equals(SysParam.Constant.YES);
            case MOTO_SALE:
            case MOTO_VOID:
            case MOTO_REFUND:
            case MOTO_AUTH:
            case MOTO_AUTHCM:
            case MOTO_AUTHCMVOID:
            case MOTO_AUTH_SETTLEMENT:
            case MOTO_AUTHVOID:
                return FinancialApplication.getSysParam().get(SysParam.OTTS_MOTO).equals(SysParam.Constant.YES);
            case RECURRING_SALE:
            case RECURRING_VOID:
                return FinancialApplication.getSysParam().get(SysParam.OTTS_RECURRING).equals(SysParam.Constant.YES);
            default:
                break;
        }

        return true;
    }

    /**
     * EMV结果处理
     *
     * @param result
     * @param transData
     */
    public static void emvTransResultProcess(ETransResult result, TransData transData) {
        ETransType transType = ETransType.valueOf(transData.getTransType());
        IEmv emv = FinancialApplication.getEmv();

        // 保存emv TSI值
        saveTvrTsi(transData);
        // 脚本结果处理
        checkScriptResult(transData);
        if (result == ETransResult.OFFLINE_APPROVED) {
            // 脱机处理
            try {
                emv.setTlv(0x8a, "Y1".getBytes());
            } catch (EmvException e) {
                Log.e(TAG, "", e);
            }
            // 设置交易结果
            transData.setEmvResult((byte) result.ordinal());
            // 取55域数据到交易结构
            byte[] f55 = EmvTags.getF55(emv, ETransType.valueOf(transData.getTransType()), false, true);
            transData.setSendIccData(FinancialApplication.getConvert().bcdToStr(f55));
            // 流水号+1
            incTransNo();

            saveCardInfoAndCardSeq(transData);
            transData.setEmvResult((byte) result.ordinal());

            // 国际代码
            byte[] aid = emv.getTlv(0x4f);
            if (aid != null && aid.length > 0) {
                boolean ret = FinancialApplication.getGl().getUtils().isByteArrayValueSame(aid, 0,
                        new byte[]{(byte) 0xA0, 0x00, 0x00, 0x03, 0x33}, 0, 5);
                if (ret) {
                    transData.setInterOrgCode("CUP");
                }
            }

            if (transData.getEnterMode() == EnterMode.QPBOC || transData.getEnterMode() == EnterMode.CLSS_PBOC) {
                transData.setTransType(ETransType.EC_SALE.toString());
            }

            byte[] tag = emv.getTlv(0x9f74);
            if (tag != null && tag.length > 0) {
                transData.setTransType(ETransType.EC_SALE.toString());
            }
        } else if (result == ETransResult.ONLINE_APPROVED) {
            // 联机批准
            // 删除冲正, 在saveTrans中
            // 读55域数据
            byte[] f55 = EmvTags.getF55(emv, transType, false, false);
            transData.setSendIccData(FinancialApplication.getConvert().bcdToStr(f55));
            // 设置交易结果
            transData.setEmvResult((byte) result.ordinal());
        } else if (result == ETransResult.ARQC || result == ETransResult.SIMPLE_FLOW_END) {
            try {
                saveCardInfoAndCardSeq(transData);
                transData.setEmvResult((byte) result.ordinal());
                if (result == ETransResult.ARQC) {
                    generateF55AfterARQC(transData);
                }
            } catch (Exception e) {
                Log.e(TAG, "", e);
            } finally {
                try {
                    FinancialApplication.getDal().getPicc(EPiccType.INTERNAL).close();
                } catch (PiccDevException e) {
                    Log.e(TAG, "", e);
                }
            }
            return;
        } else if (result == ETransResult.ONLINE_DENIED) {
            // 联机拒绝

        } else if (result == ETransResult.OFFLINE_DENIED) {
            try {
                emv.setTlv(0x8a, "Z1".getBytes());
            } catch (EmvException e) {
                Log.e(TAG, "", e);
            }
            // 流水号增加
            incTransNo();
            // 设置交易结果
            transData.setEmvResult((byte) result.ordinal());
        } else if (result == ETransResult.ONLINE_CARD_DENIED) {
            // 平台批准卡片拒绝
            byte[] f55 = EmvTags.getF55forPosAccpDup(emv);
            if (f55 != null && f55.length > 0) {
                TransData.updateDupF55(FinancialApplication.getConvert().bcdToStr(f55));
            }
        }
    }

    /**
     * ARQC时， 读取55域（交易/冲正）
     *
     * @param transData
     */
    private static void generateF55AfterARQC(TransData transData) {
        ETransType transType = ETransType.valueOf(transData.getTransType());
        IEmv emv = FinancialApplication.getEmv();
        byte[] f55 = EmvTags.getF55(emv, transType, false, false);
        if (f55 == null) {
            return;
        }
        transData.setSendIccData(FinancialApplication.getConvert().bcdToStr(f55));
        byte[] arqc = emv.getTlv(0x9f26);
        if (arqc != null && arqc.length > 0) {
            transData.setArqc(FinancialApplication.getConvert().bcdToStr(arqc));
        }

        byte[] f55Dup = EmvTags.getF55(emv, transType, true, false);
        if (f55Dup != null && f55Dup.length > 0) {
            transData.setDupIccData(FinancialApplication.getConvert().bcdToStr(f55Dup));
        }
    }

    /**
     * 保存磁道信息， 卡号， 有效期， 卡片序列号
     *
     * @param transData
     */
    public static void saveCardInfoAndCardSeq(TransData transData) {
        byte[] track2 = FinancialApplication.getEmv().getTlv(0x57);
        String strTrack2 = FinancialApplication.getConvert().bcdToStr(track2);
        strTrack2 = strTrack2.split("F")[0];
        transData.setTrack2(strTrack2);
        // 卡号
        String pan = TrackUtils.getPan(strTrack2);
        transData.setPan(pan);
        // 有效期
        byte[] expDate = FinancialApplication.getEmv().getTlv(0x5f24);
        if (expDate != null && expDate.length > 0) {
            String temp = FinancialApplication.getConvert().bcdToStr(expDate);
            transData.setExpDate(temp.substring(0, 4));
        }
        // 获取卡片序列号
        byte[] cardSeq = FinancialApplication.getEmv().getTlv(0x5f34);
        if (cardSeq != null && cardSeq.length > 0) {
            String temp = FinancialApplication.getConvert().bcdToStr(cardSeq);
            transData.setCardSerialNo(temp.substring(0, 2));
        }

    }

    /**
     * 纯电子现金卡做联机交易拒绝
     */
    public static int pureEcOnlineReject(TransData transData) {

        ETransType transType = ETransType.valueOf(transData.getTransType());
        byte[] aid = FinancialApplication.getEmv().getTlv(0x4f);

        if (aid != null && aid.length > 0) {
            boolean ret = FinancialApplication.getGl().getUtils().isByteArrayValueSame(aid, 0,
                    new byte[]{(byte) 0xA0, 0x00, 0x00, 0x03, 0x33, 0x01, 0x01, 0x06}, 0, 8);
            if (ret) {
                // 如果交易不是电子现金圈存-指定账户, 电子现金圈存-非指定账户,电子现金现金充值,电子现金现金充值撤销 等电子现金,则终止
                if (transType != ETransType.EC_CASH_LOAD && transType != ETransType.EC_LOAD
                        && transType != ETransType.EC_TRANSFER_LOAD && transType != ETransType.EC_CASH_LOAD_VOID) {
                    return -1;
                }
            }
        }
        return 0;
    }

    private static void saveTvrTsi(TransData transData) {

        IEmv emv = FinancialApplication.getEmv();
        IConvert convert = FinancialApplication.getConvert();

        String transType = transData.getTransType();
        // 电子现金交易消费读电子现金余额
        //
        if (transType.equals(ETransType.EC_SALE.toString()) || transType.equals(ETransType.EC_LOAD.toString())
                || transType.equals(ETransType.EC_CASH_LOAD.toString())
                || transType.equals(ETransType.EC_TRANSFER_LOAD.toString())
                || transType.equals(ETransType.SALE.toString())) {
            byte[] ecBalance = null;
            if (transType.equals(ETransType.SALE.toString()) && transData.getIsOnlineTrans()) {
                // 联机消费交易不需要读电子现金余额
            } else if (transData.getEnterMode() == EnterMode.QPBOC
                    || transData.getTransferEnterMode() == EnterMode.QPBOC) {

                ecBalance = emv.getTlv(0x9f5d);
                if (ecBalance != null && ecBalance.length >= 6) {
                    String balance = convert.bcdToStr(ecBalance);
                    transData.setBalance(balance.substring(0, 12));
                }
                if (transData.getBalance() == null || transData.getBalance().length() == 0) {
                    ecBalance = emv.getTlv(0x9f10);
                    if (ecBalance != null && ecBalance.length >= 15) {
                        String balance = convert.bcdToStr(ecBalance);
                        transData.setBalance(balance.substring(20, 30));
                    }
                }

            } else {
                try {
                    if (transType.equals(ETransType.EC_LOAD.toString())
                            || transType.equals(ETransType.EC_CASH_LOAD.toString())
                            || transType.equals(ETransType.EC_TRANSFER_LOAD.toString())) {
                        ecBalance = emv.getTlv(0x9f79);
                        if (ecBalance != null && ecBalance.length >= 6) {
                            String balance = convert.bcdToStr(ecBalance);
                            transData.setBalance(balance.substring(0, 12));
                        }
                    } else { // 插卡电子现金消费
                        ecBalance = emv.getTlv(0x9f10);
                        if (ecBalance != null && ecBalance.length >= 15) {
                            String balance = convert.bcdToStr(ecBalance);
                            transData.setBalance(balance.substring(20, 30));
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "", e);
                }
            }

        }
        // TVR
        byte[] tvr = emv.getTlv(0x95);
        if (tvr != null && tvr.length > 0) {
            transData.setTvr(convert.bcdToStr(tvr));
        }
        // ATC
        byte[] atc = emv.getTlv(0x9f36);
        if (atc != null && atc.length > 0) {
            transData.setAtc(convert.bcdToStr(atc));
        }
        //
        // TSI
        byte[] tsi = emv.getTlv(0x9b);
        if (tsi != null && tsi.length > 0) {
            transData.setTsi(convert.bcdToStr(tsi));
        }
        // TC
        byte[] tc = emv.getTlv(0x9f26);
        if (tc != null && tc.length > 0) {
            transData.setTc(convert.bcdToStr(tc));
        }

        // AppLabel
        byte[] appLabel = emv.getTlv(0x50);
        if (appLabel != null && appLabel.length > 0) {
            transData.setEmvAppLabel(new String(appLabel));
        }
        // AppName
        byte[] appName = emv.getTlv(0x9f12);
        if (appName != null && appName.length > 0) {
            transData.setEmvAppName(new String(appName));
        }
        // AID
        byte[] aid = emv.getTlv(0x4f);
        if (aid != null && aid.length > 0) {
            transData.setAid(convert.bcdToStr(aid));
        }

    }

    /**
     * 检查脚本结果，并保存
     *
     * @param transData
     */
    private static void checkScriptResult(TransData transData) {
        IEmv emv = FinancialApplication.getEmv();
        byte[] issuScript71 = emv.getTlv(0x71);
        byte[] issuScript72 = emv.getTlv(0x72);
        if ((issuScript71 != null && issuScript71.length > 0) || (issuScript72 != null && issuScript72.length > 0)) {
            // 保存脚本
            byte[] f55 = EmvTags.getF55(emv, ETransType.IC_SCR_SEND, false, false);
            transData.setScriptData(FinancialApplication.getConvert().bcdToStr(f55));
            transData.setOrigBatchNo(transData.getBatchNo());
            transData.setOrigTransNo(transData.getTransNo());
            transData.setOrigRefNo(transData.getRefNo());
            transData.setOrigAuthCode(transData.getAuthCode());
            transData.setOrigDate(transData.getDate());
            transData.saveScript();
        }
    }

    /**
     * 流水号+1
     */
    public static void incTransNo() {
        long transNo = Long.parseLong(FinancialApplication.getSysParam().get(SysParam.TRANS_NO));
        if (transNo >= MAX_TRANS_NO) {
            transNo = 0;
        }
        transNo++;
        FinancialApplication.getSysParam().set(SysParam.TRANS_NO, String.valueOf(transNo));
    }

    /**
     * 批次号+1
     */
    public static void incBatchNo() {
        long batchNo = Long.parseLong(FinancialApplication.getSysParam().get(SysParam.BATCH_NO));
        if (batchNo >= MAX_BATCH_NO) {
            batchNo = 0;
        }
        batchNo++;
        FinancialApplication.getSysParam().set(SysParam.BATCH_NO, String.valueOf(batchNo));
    }

    /**
     * 生成凭单水印
     */
    public static String genFeatureCode(TransData transData) {
        ETransType transType = ETransType.valueOf(transData.getTransType());
        String data1 = "";
        String data2 = "";
        if (transType == ETransType.SALE && transData.getEmvResult() == ETransResult.OFFLINE_APPROVED.ordinal()) {
            data1 = String.format("%06d", transData.getBatchNo());
            String temp = String.format("%06d", transData.getTransNo());
            data1 += temp.substring(0, 2);
            data2 = temp.substring(2) + "0000";
        } else if (transType == ETransType.OFFLINE_SETTLE || transType == ETransType.SETTLE_ADJUST
                || transType == ETransType.SETTLE_ADJUST_TIP) {
            data1 = String.format("%06d", transData.getBatchNo());
            String temp = String.format("%06d", transData.getTransNo());
            data1 += temp.substring(0, 2);
            data2 = temp.substring(2) + "0000";

        } else {
            data1 = transData.getSettleDate();
            if (data1 == null || data1.length() == 0) {
                data1 = "0000";
            }
            data2 = transData.getRefNo();
            if (data2 == null || data2.length() == 0) {
                data2 = "000000000000";
            }

            data1 += data2.substring(0, 4);
            data2 = data2.substring(4);
        }

        IConvert convert = FinancialApplication.getConvert();
        byte[] xorData = new byte[4];
        byte[] bData1 = convert.strToBcd(data1, EPaddingPosition.PADDING_LEFT);
        byte[] bData2 = convert.strToBcd(data2, EPaddingPosition.PADDING_LEFT);
        for (int i = 0; i < 4; i++) {
            xorData[i] = (byte) (bData1[i] ^ bData2[i]);
        }

        return convert.bcdToStr(xorData);
    }

    /**
     * 交易初始化
     *
     * @return
     */
    public static TransData transInit() {
        SysParam sysParam = FinancialApplication.getSysParam();
        TransData transData = new TransData();
        transData.setMerchID(sysParam.get(SysParam.MERCH_ID));
        transData.setTermID(sysParam.get(SysParam.TERMINAL_ID));
        transData.setAcqCode(sysParam.get(SysParam.ACQUIRER));
        transData.setTransNo(getTransNo());
        transData.setBatchNo(Long.parseLong(sysParam.get(SysParam.BATCH_NO)));
        transData.setDate(Device.getDate().substring(4));
        transData.setTime(Device.getTime());
        Log.d("teg", "transInit time 1:" + transData.getTime());

        //numpang OrigCouponDateTimeTrans buat time bit 90 reversal
        transData.setOrigCouponDateTimeTrans(Long.parseLong(transData.getTime()));
        Log.d("teg", "OrigCouponDateTimeTrans time:" + transData.getOrigCouponDateTimeTrans());


        //sandy
        transData.setMerName(sysParam.get(SysParam.MERCH_EN));
        transData.setMCC(sysParam.get(SysParam.MCC));
        transData.setDateTimeTrans(Long.parseLong(String.format("%s%s", transData.getDate(), transData.getTime())));

        transData.setHeader("603200321301");
        transData.setTpdu(sysParam.get(SysParam.APP_TPDU));
        // 冲正原因
        transData.setReason("06");
        transData.setOper(TransContext.getInstance().getOperID());
        transData.setTransState(ETransStatus.NORMAL.toString());
        if (SysParam.Constant.YES.equals(sysParam.get(SysParam.OTHTC_TRACK_ENCRYPT))) {
            transData.setIsEncTrack(true); // 磁道是否加密
        }
        if (SysParam.Constant.YES.equals(sysParam.get(SysParam.SUPPORT_SM))
                && SysParam.Constant.YES.equals(sysParam.get(SysParam.SUPPORT_SM_PERIOD_2))) {
            transData.setIsSM(true); // 是否支持国密
        } else {
            transData.setIsSM(false);
        }

        //for installment. default value.
        transData.setFirstAmount("000000000000");
        transData.setInstalCurrCode(FinancialApplication.getSysParam().getCurrency().getCode());
        transData.setFeeTotalAmount("000000000000");

        return transData;
    }

    /**
     * 交易初始化
     *
     * @param transData
     */
    public static void transInit(TransData transData) {
        SysParam sysParam = FinancialApplication.getSysParam();
        transData.setMerchID(sysParam.get(SysParam.MERCH_ID));
        transData.setTermID(sysParam.get(SysParam.TERMINAL_ID));
        transData.setAcqCode(sysParam.get(SysParam.ACQUIRER));
        transData.setTransNo(getTransNo());
        transData.setBatchNo(Long.parseLong(sysParam.get(SysParam.BATCH_NO)));
        transData.setDate(Device.getDate().substring(4));
        transData.setTime(Device.getTime());
        Log.d("teg", "transInit time :" + transData.getTime());
        transData.setHeader("603200321301");
        transData.setTpdu(sysParam.get(SysParam.APP_TPDU));
        // 冲正原因
        transData.setReason("06");
        transData.setOper(TransContext.getInstance().getOperID());
        transData.setTransState(ETransStatus.NORMAL.toString());
        if (SysParam.Constant.YES.equals(sysParam.get(SysParam.OTHTC_TRACK_ENCRYPT))) {
            transData.setIsEncTrack(true);
        }
        if (SysParam.Constant.YES.equals(sysParam.get(SysParam.SUPPORT_SM))
                && SysParam.Constant.YES.equals(sysParam.get(SysParam.SUPPORT_SM_PERIOD_2))) {
            transData.setIsSM(true);
        } else {
            transData.setIsSM(false);
        }
    }

    // 获取流水号
    private static long getTransNo() {
        long transNo = 0;
        try {
            transNo = Long.parseLong(FinancialApplication.getSysParam().get(SysParam.TRANS_NO));
        } catch (NumberFormatException e) {
            Log.e(TAG, "", e);
        }
        if (transNo == 0) {
            transNo += 1;
            FinancialApplication.getSysParam().set(SysParam.TRANS_NO, String.valueOf(transNo));
        }
        return transNo;
    }


    /**
     * convert {@link TransData} to {@link InputParam} for EMV and CLSS
     *
     * @param transData {@link TransData}
     * @return {@link InputParam}
     */
    public static InputParam toInputParam(TransData transData) {
        InputParam inputParam = new InputParam();
        convertTransData2InputParam(transData, inputParam);
        return inputParam;
    }

    public static ClssInputParam toClssInputParam(TransData transData) {
        ClssInputParam inputParam = new ClssInputParam();
        convertTransData2InputParam(transData, inputParam);
        inputParam.setAmtZeroNoAllowedFlg(0);
        inputParam.setCrypto17Flg(true);
        inputParam.setStatusCheckFlg(false);
        inputParam.setReaderTTQ("36008000");
        inputParam.setDomesticOnly(false);
        List<ECvmResult> list = new ArrayList<>();
        list.add(ECvmResult.REQ_SIG);
        list.add(ECvmResult.REQ_ONLINE_PIN);
        inputParam.setCvmReq(list);
        inputParam.setEnDDAVerNo((byte) 0);
        return inputParam;
    }

    private static void convertTransData2InputParam(TransData transData, InputParam inputParam) {
        ETransType transType = ETransType.valueOf(transData.getTransType());
        // add abdul
        inputParam.setTransType(transData.getTransType());
        //
        String amount = transData.getAmount();
        if (amount == null || amount.length() == 0) {
            amount = "0";
        }
        inputParam.setAmount(String.format("%012d", Long.parseLong(amount)));
        inputParam.setCashBackAmount("0");

        if (transType == ETransType.SALE
                || transType == ETransType.OVERBOOK_INQUIRY // add abdul
                || transType == ETransType.SETOR_TUNAI // add abdul
                || transType == ETransType.TARIK_TUNAI // add abdul
                || transType == ETransType.TARIK_TUNAI_2 // add sandy
                || transType == ETransType.OVERBOOKING_PULSA_DATA // add abdul
                || transType == ETransType.BPJS_OVERBOOKING // add sandy
                || transType == ETransType.PEMBUKAAN_REK // add tri
                || transType == ETransType.PEMBATAL_REK // add tri
                || transType == ETransType.DIRJEN_ANGGARAN // add tri
                || transType == ETransType.DIRJEN_BEA_CUKAI // add tri
                || transType == ETransType.DIRJEN_PAJAK // add tri
                || transType == ETransType.E_SAMSAT // add tri
                || transType == ETransType.E_SAMSAT_INQUIRY // add tri
                || transType == ETransType.PASCABAYAR_OVERBOOKING // add tri
                || transType == ETransType.PDAM_OVERBOOKING // add tri
//                || transType == ETransType.CETAK_ULANG // add tri
                || transType == ETransType.TRANSFER // add tri
                || transType == ETransType.TRANSFER_2 // add sandy
                || transType == ETransType.TRANSFER_INQ // add tri
                || transType == ETransType.TRANSFER_INQ_2 // add sandy
                || transType == ETransType.OVERBOOKING // add tri
                || transType == ETransType.OVERBOOKING_2 // add sandy
                || transType == ETransType.MINISTATEMENT
                || transType == ETransType.PBB_INQ
                || transType == ETransType.PBB_PAY
                || transType == ETransType.BALANCE_INQUIRY
                || transType == ETransType.BALANCE_INQUIRY_2
                || transType == ETransType.ACCOUNT_LIST
                || transType == ETransType.REDEEM_POIN_DATA_INQ //add sandy
                || transType == ETransType.REDEEM_POIN_DATA_PAY//add sandy

                || transType == ETransType.INSTAL_SALE
                || transType == ETransType.EC_SALE
                || transType == ETransType.QUERY
                || transType == ETransType.AUTH
                || transType == ETransType.EC_LOAD
                || transType == ETransType.EC_CASH_LOAD
                || transType == ETransType.EC_CASH_LOAD_VOID
                || transType == ETransType.COUPON_VERIFY) {
            if (transData.getEnterMode() == EnterMode.INSERT) {
                inputParam.setFlowType(EFlowType.COMPLETE);
            } else {
                inputParam.setFlowType(EFlowType.QPBOC);
            }
            inputParam.setEnableCardAuth(true);
            if (transType == ETransType.EC_CASH_LOAD) {
                inputParam.setSupportCVM(false);
            } else {
                inputParam.setSupportCVM(true);
            }
        } else if (transType == ETransType.EC_TRANSFER_LOAD_OUT) {
            // 非指定账户圈存插入转出卡,按照PBOC流程仅执行至脱机数据认证
            inputParam.setFlowType(EFlowType.SIMPLE);
            inputParam.setEnableCardAuth(true);

        } else if (transType == ETransType.EC_TRANSFER_LOAD) {
            inputParam.setFlowType(EFlowType.COMPLETE);
            inputParam.setEnableCardAuth(true);
            inputParam.setSupportCVM(true);
        } else {
            // 联机Q非消费，余额查询，预授权均走简单Q流程
            if (transData.getEnterMode() == EnterMode.QPBOC) {
                inputParam.setFlowType(EFlowType.SIMPLE);
            } else {
                inputParam.setFlowType(EFlowType.SIMPLE);
            }
            inputParam.setEnableCardAuth(false);
            inputParam.setSupportCVM(false);
        }

        byte[] procCode = FinancialApplication.getConvert()
                .strToBcd(transType.getProcCode(), EPaddingPosition.PADDING_RIGHT);

        inputParam.setTag9CValue(procCode[0]);

        //test
        /*if (transType == ETransType.ACCOUNT_LIST){
            inputParam.setTag9CValue((byte)0x51);
//            inputParam.setTag9CValue((byte)0x52);
        }else {
            inputParam.setTag9CValue(procCode[0]);
        }*/
        //end

        if (transType == ETransType.EC_SALE) { // 电子现金，8-1-1，快捷键1
            inputParam.setForceOnline(false);
        } else if (transType == ETransType.SALE || transType == ETransType.INSTAL_SALE) { // 消费入口进，优先借货记
            inputParam.setForceOnline(false);
            inputParam.setTag9CValue((byte) 0x00);
            inputParam.setFlowType(EFlowType.COMPLETE);
            if (transData.getEnterMode() == EnterMode.QPBOC) { // 非接还要判断，非接交易通道开关
                inputParam.setFlowType(EFlowType.QPBOC);
                if (SysParam.Constant.NO.equals(FinancialApplication.getSysParam().get(SysParam.QUICK_PASS_TRANS_SWITCH))) {
                    inputParam.setForceOnline(true);
                }
                inputParam.setTag9CValue((byte) 0x00);
            }

        } else {
            // 根据交易类型判断是否强制联机
            inputParam.setForceOnline(true);
        }

        Time tm = new Time();
        tm.setToNow();
        String year = String.format("%04d", tm.year);
        inputParam.setTransDate(year + transData.getDate());
        inputParam.setTransTime(transData.getTime());
        inputParam.setTransTraceNo(String.format("%06d", transData.getTransNo()));

        // 终端不支持脱机交易
        if (FinancialApplication.getSysParam().get(SysParam.SUPPORT_OFFLINE_TRANS).equals(SysParam.Constant.NO)) {
            inputParam.setForceOnline(true);
        }
    }

    public static Config genCommonEmvConfig() {
        Config cfg = new Config();

        Currency currency = FinancialApplication.getSysParam().getCurrency();
        cfg.setCountryCode(FinancialApplication.getConvert().bcdToStr(currency.getCodeBcdBytes()));
        cfg.setForceOnline(false);
        cfg.setGetDataPIN(true);
        cfg.setMerchCateCode("0000");
        cfg.setReferCurrCode(FinancialApplication.getConvert().bcdToStr(currency.getCodeBcdBytes()));
        cfg.setReferCurrCon(1000);
        cfg.setReferCurrExp((byte) 0x02);
        cfg.setSurportPSESel(true);
        cfg.setTermType((byte) 0x22);
        cfg.setTransCurrCode(FinancialApplication.getConvert().bcdToStr(currency.getCodeBcdBytes()));
        cfg.setTransCurrExp(currency.getDecimalBcdBytes()[0]);
        cfg.setTermId(FinancialApplication.getSysParam().get(SysParam.TERMINAL_ID));
        cfg.setMerchId(FinancialApplication.getSysParam().get(SysParam.MERCH_ID));
        cfg.setMerchName(FinancialApplication.getSysParam().get(SysParam.MERCH_EN));
        cfg.setTermAIP("7c00");
        cfg.setBypassPin(true); // 输密码支持bypass
        cfg.setBatchCapture((byte) 1);
        cfg.setUseTermAIPFlag(true);
        cfg.setBypassAllFlag(true);
        return cfg;
    }

    /**
     * @return:true-免密 false-未知
     */
    private static boolean clssCDCVMProcss() {
        if (FinancialApplication.getSysParam().get(SysParam.QUICK_PASS_TRANS_CDCVM_FLAG).equals(SysParam.Constant.YES)) {
            byte[] value = FinancialApplication.getEmv().getTlv(0x9f6c);
            if ((value[1] & CD_CVM) == CD_CVM && (value[0] & ONLINEPIN_CVM) != ONLINEPIN_CVM) {
                return true;
            }
        }

        return false;
    }

    /**
     * Determine whether there is need to input pin by CVM type.
     *
     * @param transData, transaction data.
     * @return true: no pin, false: need pin.
     */
    private static boolean clssCVMProcess(TransData transData) {
        // Get the card type by AID params.
        byte[] aid = null;
        try {
            aid = FinancialApplication.getEmv().getTlv(0x4F);
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }

        // Get cvm type and cvm limit amount by CVM params.
        byte[] cvm = null;
        try {
            cvm = FinancialApplication.getEmv().getTlv(0x9f6c);
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
        if (aid == null || cvm == null) {
            transData.setPinFree(false);
            transData.setSignFree(false);
            return false;
        }

        boolean isPinFree = (cvm[0] & ONLINEPIN_CVM) != ONLINEPIN_CVM;
        boolean isSignFree = (cvm[0] & SIGNATURE_CVM) != SIGNATURE_CVM;
        boolean isCredit = isCredit(FinancialApplication.getConvert().bcdToStr(aid));
        EmvAid aidParam = EmvAid.readAid(FinancialApplication.getConvert().bcdToStr(aid));
        long cvmLimitAmount = Long.parseLong(aidParam.getRdCVMLmt());

        String amount = transData.getAmount().replace(".", "");
        Currency currency = FinancialApplication.getSysParam().getCurrency();
        amount = FinancialApplication.getConvert().amountMinUnitToMajor(amount, currency
                .getCurrencyExponent(), false);
        // Transaction amount is less than cvm limit amount.
        if (Double.parseDouble(amount) <= cvmLimitAmount) {
            if (!isCredit && !isPinFree) {
                transData.setPinFree(isPinFree);
                transData.setSignFree(isSignFree);
                return false;
            }
            transData.setPinFree(true);
            transData.setSignFree(true);
            return true;
        }


        // Transaction amount is more than cvm limit amount.
        if (!isPinFree && isCredit) {
            transData.setPinFree(false);
            transData.setSignFree(false);
        } else if (isPinFree && isCredit) {
            transData.setPinFree(true);
            transData.setSignFree(false);
        } else if (!isPinFree && !isCredit) {
            transData.setPinFree(false);
            transData.setSignFree(true);
        } else {
            transData.setPinFree(true);
            transData.setSignFree(false);
        }

        return isPinFree;
    }

    /**
     * Qpboc判定卡片是否需要输入联机pin, 只有外卡才需要通过9f6c来判断, 内卡默认都要输pin
     *
     * @return true：需要， false：不需要
     */
    public static boolean isQpbocNeedOnlinePin() {
        if (!isCupOutSide()) {
            return true;
        }
        byte[] value = FinancialApplication.getEmv().getTlv(0x9f6c);

        return (value[0] & ONLINEPIN_CVM) == ONLINEPIN_CVM;
    }

    /**
     * 判断是否是银联外卡
     */
    private static boolean isCupOutSide() {
        int[] tags = new int[]{0x9F51, 0xDF71}; // tag9F51：第一货币 tagDF71：第二货币
        int flag = 0;
        byte[] val = null;
        for (int tag : tags) {
            val = FinancialApplication.getEmv().getTlv(tag);
            if (val == null) {
                continue;
            }
            flag = 1; // 能获取到货币代码值
            if ("0156".equals(FinancialApplication.getConvert().bcdToStr(val))) {
                return false;
            }
        }

        return val != null || flag != 0;
    }

    /**
     * 根据AID判断是否是贷记卡或准贷记卡
     *
     * @param aid
     * @return true: 贷记卡或准贷记卡 false: 其他
     */
    private static boolean isCredit(String aid) {
        final String UNIONPAY_DEBITAID = "A000000333010101";
        final String UNIONPAY_CREDITAID = "A000000333010102";
        final String UNIONPAY_QUASICREDITAID = "A000000333010103";

        return UNIONPAY_CREDITAID.equals(aid) || UNIONPAY_QUASICREDITAID.equals(aid);
    }

    /**
     * Quick payment service process.
     *
     * @param transData Transaction data
     * @return true - no pin, false - need pin
     */
    public static boolean clssQPSProcess(TransData transData) {
        if (FinancialApplication.getSysParam().get(SysParam.QUICK_PASS_TRANS_SWITCH).equals(SysParam.Constant.NO)) {
            return false;
        }
        int enterMode = transData.getEnterMode();
        if (enterMode != EnterMode.CLSS_PBOC && enterMode != EnterMode.QPBOC) {
            return false;
        }

        String transType = transData.getTransType();
        if (ETransType.SALE.toString().equals(transType) || ETransType.EC_SALE.toString().equals(transType)
                || ETransType.AUTH.toString().equals(transType)) {
            return clssCVMProcess(transData);
        }

        return false;
    }

    /**
     * Determine whether signature is required.
     *
     * @param transData
     * @return true: no signature, false: need signature
     */
    public static boolean isSignatureFree(TransData transData) {
        ETransType transType = ETransType.valueOf(transData.getTransType());

        if (ETransType.EC_SALE == transType) {
            return true;
        }

        if (ETransType.SALE != transType
                && ETransType.COUPON_SALE != transType
                && ETransType.EMV_QR_SALE != transType) {
            return false;
        }

        return transData.getSignFree();
    }

    /**
     * 磁道加密
     *
     * @param trackData
     * @return
     */
    public static String encryptTrack(String trackData) {
        String encryptData = null;
        if (trackData == null || trackData.length() == 0) {
            return null;
        }
        int len = trackData.length();
        if (trackData.length() % 2 > 0) {
            trackData += "0";
        }
        byte[] tb = new byte[8];
        byte[] bTrack = FinancialApplication.getGl().getConvert().strToBcd(trackData, EPaddingPosition.PADDING_LEFT);
        System.arraycopy(bTrack, bTrack.length - 9, tb, 0, 8);
        byte[] block = new byte[8];
        try {
            block = FinancialApplication.getDal().getPed(EPedType.INTERNAL).calcDes(Constants.INDEX_TDK, tb,
                    EPedDesMode.ENCRYPT);
        } catch (PedDevException e) {
            Log.e(TAG, "", e);
        }
        System.arraycopy(block, 0, bTrack, bTrack.length - 9, 8);
        encryptData = FinancialApplication.getGl().getConvert().bcdToStr(bTrack).substring(0, len);

        return encryptData;
    }

    /**
     * 检查Neptune是否有安装，如果未安装，给提示
     *
     * @return
     */
    public static boolean neptuneInstalled(Context context, OnDismissListener onDismissListener) {
        PackageInfo packageInfo = null;
        try {
            packageInfo = context.getPackageManager().getPackageInfo("com.pax.ipp.neptune", 0);
        } catch (NameNotFoundException e) {
            Log.e(TAG, "", e);
        }

        if (packageInfo == null) {

            CustomAlertDialog dialog = new CustomAlertDialog(context, CustomAlertDialog.ERROR_TYPE, 5);
            dialog.setContentText(context.getString(R.string.please_install_neptune));
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
            dialog.setOnDismissListener(onDismissListener);

            return false;
        }
        return true;
    }

    /**
     * To judge if the transaction is a debit transaction.
     *
     * @param transType transaction type
     * @return true - debit trans, false - credit trans
     * @author Richard 20170511
     */
    public static boolean isDebitTransaction(ETransType transType) {

        switch (transType) {
            case VOID:
            case REFUND:
            case EC_REFUND:
            case AUTHVOID:
            case AUTHCMVOID:
            case QR_VOID:
            case QR_REFUND:
            case MOTO_VOID:
            case MOTO_REFUND:
            case MOTO_AUTHVOID:
            case MOTO_AUTHCMVOID:
            case RECURRING_VOID:
            case INSTAL_VOID:
            case COUPON_VERIFY_VOID:
            case COUPON_SALE_VOID:
            case DANA_QR_VOID:
                return false;

            case SALE:
            case EC_SALE:
            case AUTH:
            case AUTHCM:
            case AUTH_SETTLEMENT:
            case OFFLINE_SETTLE:
            case SETTLE_ADJUST:
            case SETTLE_ADJUST_TIP:
            case QR_SALE:
            case MOTO_SALE:
            case MOTO_AUTH:
            case MOTO_AUTH_SETTLEMENT:
            case MOTO_AUTHCM:
            case RECURRING_SALE:
            case INSTAL_SALE:
            case COUPON_SALE:
            default:
                return true;
        }
    }

    public static String getPaddedNumber(long num, int digit) {
        NumberFormat nf = NumberFormat.getInstance(Locale.US);
        nf.setGroupingUsed(false);
        nf.setMaximumIntegerDigits(digit);
        nf.setMinimumIntegerDigits(digit);
        return nf.format(num);
    }

    public static String getPaddedString(String str, int maxLen, char ch) {
        return FinancialApplication.getConvert().stringPadding(str, ch, maxLen, IConvert.EPaddingPosition.PADDING_LEFT);
    }

    public static String getPaddedString(String str, int maxLen, char ch, EPaddingPosition paddingPosition) {
        try {
            String ret = FinancialApplication.getConvert().stringPadding(str, ch, maxLen, paddingPosition);
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
