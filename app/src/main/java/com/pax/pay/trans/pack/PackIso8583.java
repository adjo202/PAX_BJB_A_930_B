/*
 * ============================================================================
 * COPYRIGHT
 *               Pax CORPORATION PROPRIETARY INFORMATION
 *    This software is supplied under the terms of a license agreement or
 *    nondisclosure agreement with Pax Corporation and may not be copied
 *    or disclosed except in accordance with the terms in that agreement.
 *       Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 *  Module Date: 2017-8-28 10:3
 *  Module Author: liliang
 *  Description:
 *  ============================================================================
 */

package com.pax.pay.trans.pack;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.pax.abl.core.ipacker.IPacker;
import com.pax.abl.core.ipacker.PackListener;
import com.pax.dal.IPrinter;
import com.pax.dal.entity.EFontTypeAscii;
import com.pax.dal.entity.EFontTypeExtCode;
import com.pax.dal.exceptions.PrinterDevException;
import com.pax.gl.convert.IConvert.EPaddingPosition;
import com.pax.gl.packer.IIso8583;
import com.pax.gl.packer.Iso8583Exception;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.trans.model.TransData.EnterMode;
import com.pax.pay.utils.Fox;
import com.pax.settings.SysParam;
import com.pax.settings.currency.Currency;

import org.apache.commons.lang.StringUtils;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

/**
 * Created by liliang on 2017/8/28.
 */

public abstract class PackIso8583 implements IPacker<TransData, byte[]> {
    private static final String TAG = "PackIso8583";

    private IIso8583 iso8583;
    protected IIso8583.IIso8583Entity entity;
    protected PackListener listener;

    public PackIso8583(PackListener listener) {
        this.listener = listener;
        initEntity();
    }

    //    Add PR Load bitmap Secondary
    public PackIso8583(PackListener listener, Boolean SecondaryBitmap) {
        this.listener = listener;
        if (SecondaryBitmap) {
            initEntityS();
        } else {
            initEntity();
        }
    }
    //    End Add

    private void initEntity() {
        iso8583 = FinancialApplication.getPacker().getIso8583(); //获取Iso8583接口
        try {
            entity = iso8583.getEntity(); //Get an Iso8583 entity, use this handle to set field values, field properties, etc.
            entity.loadTemplate(FinancialApplication.getAppContext().getResources().getAssets().open
                    ("cup8583.xml"));
        } catch (Iso8583Exception | IOException | XmlPullParserException e) {
            Log.e(TAG, "initEntity---->", e);
        }
    }

    //   Add PR Load bitmap Secondary
    private void initEntityS() {
        iso8583 = FinancialApplication.getPacker().getIso8583();
        try {
            entity = iso8583.getEntity();
            entity.loadTemplate(FinancialApplication.getAppContext().getResources().getAssets().open("cup8583s.xml"));
        } catch (Iso8583Exception | IOException | XmlPullParserException e) {
            Log.e(TAG, "initEntityS---->", e);
        }
    }
    //    End load

    protected abstract int[] getRequiredFields();

    @Override
    public byte[] pack(TransData transData) {
        int[] fields = getRequiredFields();
        try {
            for (int i : fields) {
                addField(i, transData);
            }

            //transData.setHeader("");
            // h
            String pHeader = transData.getTpdu();

            entity.setFieldValue("h", pHeader); //设置域值
            // m
            ETransType transType = ETransType.valueOf(transData.getTransType());
            if (transData.getIsReversal()) {
                entity.setFieldValue("m", transType.getDupMsgType()); //冲正消息类型码
            } else {
                entity.setFieldValue("m", transType.getMsgType()); //消息类型码
            }
            Log.i("teg", "f35 : " + transData.getTrack2());

            byte[] packData = iso8583.pack(); //基于ISO8583实体的设置和值进行组包
            if (packData == null || packData.length == 0) {
                return new byte[0];
            }

            if (entity.hasField("64")) {
                int len = packData.length;
                byte[] calMacBuf = new byte[len - 11 - 8];// 去掉header和mac
                System.arraycopy(packData, 11, calMacBuf, 0, len - 11 - 8);
                byte[] mac = listener.onCalcMac(calMacBuf);
                if (mac == null || mac.length == 0) {
                    return new byte[0];
                }
                System.arraycopy(mac, 0, packData, len - 8, 8);
            }

            return packData;
        } catch (Iso8583Exception e) {
            Log.e(TAG, "", e);
        }

        return new byte[0];
    }

    private void addField(int fieldId, TransData transData) throws Iso8583Exception {
        /**
         if (fieldId < 1 || fieldId > 64) {
         throw new IllegalArgumentException("filedId == " + fieldId +
         ", fieldId should be 1-64");
         }**/

        switch (fieldId) {
            case 1:
                setBitData1(transData);
                break;
            case 2:
                setBitData2(transData);
                break;
            case 3:
                setBitData3(transData);
                break;
            case 4:
                setBitData4(transData);
                break;
            case 5:
                setBitData5(transData);
                break;
            case 6:
                setBitData6(transData);
                break;
            case 7:
                setBitData7(transData);
                break;
            case 8:
                setBitData8(transData);
                break;
            case 9:
                setBitData9(transData);
                break;
            case 10:
                setBitData10(transData);
                break;
            case 11:
                setBitData11(transData);
                break;
            case 12:
                setBitData12(transData);
                break;
            case 13:
                setBitData13(transData);
                break;
            case 14:
                setBitData14(transData);
                break;
            case 15:
                setBitData15(transData);
                break;
            case 16:
                setBitData16(transData);
                break;
            case 17:
                setBitData17(transData);
                break;
            case 18:
                setBitData18(transData);
                break;
            case 19:
                setBitData19(transData);
                break;
            case 20:
                setBitData20(transData);
                break;
            case 21:
                setBitData21(transData);
                break;
            case 22:
                setBitData22(transData);
                break;
            case 23:
                setBitData23(transData);
                break;
            case 24:
                setBitData24(transData);
                break;
            case 25:
                setBitData25(transData);
                break;
            case 26:
                setBitData26(transData);
                break;
            case 27:
                setBitData27(transData);
                break;
            case 28:
                setBitData28(transData);
                break;
            case 29:
                setBitData29(transData);
                break;
            case 30:
                setBitData30(transData);
                break;
            case 31:
                setBitData31(transData);
                break;
            case 32:
                setBitData32(transData);
                break;
            case 33:
                setBitData33(transData);
                break;
            case 34:
                setBitData34(transData);
                break;
            case 35:
                setBitData35(transData);
                break;
            case 36:
                setBitData36(transData);
                break;
            case 37:
                setBitData37(transData);
                break;
            case 38:
                setBitData38(transData);
                break;
            case 39:
                setBitData39(transData);
                break;
            case 40:
                setBitData40(transData);
                break;
            case 41:
                setBitData41(transData);
                break;
            case 42:
                setBitData42(transData);
                break;
            case 43:
                setBitData43(transData);
                break;
            case 44:
                setBitData44(transData);
                break;
            case 45:
                setBitData45(transData);
                break;
            case 46:
                setBitData46(transData);
                break;
            case 47:
                setBitData47(transData);
                break;
            case 48:
                setBitData48(transData);
                break;
            case 49:
                setBitData49(transData);
                break;
            case 50:
                setBitData50(transData);
                break;
            case 51:
                setBitData51(transData);
                break;
            case 52:
                setBitData52(transData);
                break;
            case 53:
                setBitData53(transData);
                break;
            case 54:
                setBitData54(transData);
                break;
            case 55:
                setBitData55(transData);
                break;
            case 56:
                setBitData56(transData);
                break;
            case 57:
                setBitData57(transData);
                break;
            case 58:
                setBitData58(transData);
                break;
            case 59:
                setBitData59(transData);
                break;
            case 60:
                setBitData60(transData);
                break;
            case 61:
                setBitData61(transData);
                break;
            case 62:
                setBitData62(transData);
                break;
            case 63:
                setBitData63(transData);
                break;
            case 64:
                setBitData64(transData);
                break;
            case 90:
                setBitData90(transData);
                break;
            case 100:
                setBitData100(transData);
                break;
            case 102:
                setBitData102(transData);
                break;
            case 103:
                setBitData103(transData);
                break;
            case 106:
                setBitData106(transData);
                break;
            case 107:
                setBitData107(transData);
                break;
            case 110:
                setBitData110(transData);
                break;
            case 113:
                setBitData113(transData);
                break;
            case 125:
                setBitData125(transData);
                break;
            case 127:
                setBitData127(transData);
                break;
            default:
                break;
        }
    }

    @Override
    public int unpack(TransData transData, byte[] rsp) {
        HashMap<String, byte[]> map;
        try {
            byte[] c = rsp;
            //sandy : just mark here for skipping secondary bitmap....
            //if you register the transType here, it will be only use single bitmap
            switch (transData.getTransTypeEnum()) {
                //case PDAM_INQUIRY:
                //case PDAM_PURCHASE:
                //case PASCABAYAR_INQUIRY:
                // case INQ_PULSA_DATA:
                case PURCHASE_PULSA_DATA:
                case OVERBOOKING_PULSA_DATA:
                case BPJS_OVERBOOKING:
                case PDAM_OVERBOOKING:
                case PASCABAYAR_OVERBOOKING:
                case PASCABAYAR_PURCHASE:
                    if (!transData.getIsReversal()) {
                        initEntity();
                        break;
                    }
            }

            map = iso8583.unpack(rsp, true);
            // Debug information, log input unpacked data
            entity.dump();
        } catch (Iso8583Exception e) {
            Log.e(TAG, "Unpack error-> PackIso8583.java", e);
            if (transData.getTransType().equals(ETransType.CHANGE_PIN.toString())
                    || transData.getTransType().equals(ETransType.VERIFY_PIN.toString())) {
                try {
                    initEntityS();
                    map = iso8583.unpack(rsp, true);
                    entity.dump();
                } catch (Iso8583Exception ex) {
                    Log.e(TAG, "Unpack error-> PackIso8583.java", e);
                    return TransResult.ERR_UNPACK;
                }
            } else {
                Log.e(TAG, "Unpack error", e);
                return TransResult.ERR_UNPACK;
            }

        }

        try {
            // 报文头
            byte[] header = map.get("h");
            transData.setHeader(new String(header).substring(10));

            ETransType transType = ETransType.valueOf(transData.getTransType());

            byte[] buff;
            // 检查39域应答码
            if (transType != ETransType.SETTLE) {
                buff = map.get("39"); //Field 39 Response Code
                if (buff == null) {
                    return TransResult.ERR_BAG; //非法包
                }
                transData.setResponseCode(new String(buff));
            }

            // 检查返回包的关键域， 包含field4
            boolean isCheckAmt = true;
            if (transType == ETransType.SETTLE || transType == ETransType.QUERY || transType == ETransType.DOWNLOAD_PRODUCT_PULSA_DATA
                    || transType == ETransType.ACCOUNT_LIST || transType == ETransType.PBB_INQ || transType == ETransType.PBB_PAY) { //余额查询
                isCheckAmt = false;
            }
            int ret = checkRecvData(map, transData, isCheckAmt); //检查请求和返回的关键域field4, field11,
            // field41, field42
            if (ret != TransResult.SUCC) {
                return ret;
            }

            // field 2 主账号

            // field 3 交易处理码
            buff = map.get("3");
            if (buff != null && buff.length > 0) {
                String origField3 = transData.getField3();
                if (!TextUtils.isEmpty(origField3) && !origField3.equals(new String(buff))) {
                    return TransResult.ERR_PROC_CODE;
                }
            }
            // field 4 交易金额
            buff = map.get("4");
            if (buff != null && buff.length > 0 && transType != ETransType.PBB_PAY) {
                transData.setAmount(new String(buff));
                Log.i("abdul", "amount dari host = " + transData.getAmount());
            }

            // field 11 流水号
            buff = map.get("11");
            if (buff != null && buff.length > 0) {
                if (transType == ETransType.COUPON_VERIFY_VOID) {
                    transData.setOrigTransNo(Long.parseLong(new String(buff)));
                } else {
                    transData.setTransNo(Long.parseLong(new String(buff)));
                }
            }

            // field 12 受卡方所在地时间
            buff = map.get("12");
            if (buff != null && buff.length > 0) {
                transData.setTime(new String(buff));
            }
            // field 13 受卡方所在地日期
            buff = map.get("13");
            if (buff != null) {
                transData.setDate(new String(buff));
            }
            // field 14 卡有效期
            buff = map.get("14");
            if (buff != null && buff.length > 0) {
                String expDate = new String(buff);
                if (!"0000".equals(expDate)) {
                    transData.setExpDate(expDate);
                }
            }

            // field 15清算日期
            buff = map.get("15");
            if (buff != null && buff.length > 0) {
                transData.setSettleDate(new String(buff));
            }

            // field 22

            // field 23 卡片序列号
            buff = map.get("23");
            if (buff != null && buff.length > 0) {
                transData.setCardSerialNo(new String(buff));
            }
            // field 25
            // field 26

            // field 28, biaya admin
            buff = map.get("28");
            if (buff != null && buff.length > 0) {
                transData.setField28(new String(buff));
            }

            // field 32 受理方标识码
            buff = map.get("32");
            if (buff != null && buff.length > 0) {
                transData.setAcqCenterCode(new String(buff)); // 受理方标识码,pos中心号(返回包时用)
            }

            // field 35
            // field 36
            buff = map.get("36");
            if (buff != null && buff.length > 0) {
                transData.setField36(new String(buff));
            }

            // field 37 检索参考号
            buff = map.get("37");
            Log.i("abdul", "unpack bit37 = " + buff);
            if (buff != null && buff.length > 0) {
                transData.setRefNo(new String(buff));
            }

            // field 38 授权码
            buff = map.get("38");
            if (buff != null && buff.length > 0) {
                transData.setAuthCode(new String(buff));
            }

            // field 41 校验终端号
            buff = map.get("41");
            if (buff != null && buff.length > 0) {
                transData.setTermID(new String(buff));
            }

            // field 42 校验商户号
            buff = map.get("42");
            if (buff != null && buff.length > 0) {
                transData.setMerchID(new String(buff));
            }

            // field 43

            // field 44
            buff = map.get("44");
            if (buff != null && buff.length > 11) {
                String temp = new String(buff).substring(0, 11).trim();
                transData.setIsserCode(temp); // 发卡行标识码
                if (buff.length > 11) {
                    temp = new String(buff).substring(11).trim();
                    transData.setAcqCode(temp); // 收单机构标识码
                }
            }

            //sandy
            // field 47
            buff = map.get("47");
            if (buff != null && buff.length > 0) {
                transData.setField47(new String(buff));
            }


            // field 48
            buff = map.get("48");
            if (buff != null && buff.length > 0) {
                Log.d("teg", "f48 : " + new String(buff));
                transData.setField48(new String(buff));
            }


            // field 52

            // field 53

            // field 54
            buff = map.get("54");
            if (buff != null && buff.length > 0) {
                String temp = new String(buff);
                transData.setBalanceFlag(temp.substring(7, 8));
                transData.setBalance(temp.substring(temp.length() - 12, temp.length()));
                Log.i("abdul", "balance = " + transData.getBalance());
            }

            // field 55
            buff = map.get("55");
            if (buff != null && buff.length > 0) {
                transData.setRecvIccData(FinancialApplication.getConvert().bcdToStr(buff));
            }

            //tri
            buff = map.get("56");
            if (buff != null && buff.length > 0) {
                switch (transType) {
                    case OVERBOOKING_PULSA_DATA:
                    case BPJS_OVERBOOKING:
                    case PASCABAYAR_OVERBOOKING:
                    case PDAM_OVERBOOKING:
                        transData.setNtb(new String(buff)); //minjem variabel
                        break;
                }

            }

            // field 58
            // filed59域 解tlv
            buff = map.get("59");
            if (transType == ETransType.QR_VOID || transType == ETransType.QR_REFUND) {
                transData.setC2bVoucher(""); // 55域 应答TagA4 扫码付付款凭证码
            } else if (transType == ETransType.QR_SALE && buff != null && buff.length > 0) {
                byte[] tmp = new byte[buff.length - 5];
                System.arraycopy(buff, 5, tmp, 0, tmp.length); // 前面有5个字节的长度
                transData.setC2bVoucher(new String(tmp)); // 55域 应答TagA4 扫码付付款凭证码
            } else if ((transType == ETransType.QR_INQUIRY ||
                    transType == ETransType.INQ_PULSA_DATA ||
                    transType == ETransType.PURCHASE_PULSA_DATA ||
                    transType == ETransType.OVERBOOKING_PULSA_DATA ||
                    transType == ETransType.BPJS_OVERBOOKING ||

                    transType == ETransType.PDAM_INQUIRY ||
                    transType == ETransType.PDAM_PURCHASE ||
                    transType == ETransType.PDAM_OVERBOOKING ||

                    transType == ETransType.PASCABAYAR_INQUIRY ||
                    transType == ETransType.PASCABAYAR_PURCHASE ||
                    transType == ETransType.PASCABAYAR_OVERBOOKING ||

                    transType == ETransType.TRANSFER_INQ) &&
                    buff != null && buff.length > 0) {
                Log.d("teg", "59 : " + transData.getField59());
                transData.setField59(new String(buff));
            }


            // field 60 : Reserved Private
            buff = map.get("60");
            if (buff != null && buff.length > 0) {
                transData.setBatchNo(Long.parseLong(new String(buff).substring(2, 8))); //f60.2[N6]
            }

            // field 61
            buff = map.get("61");
            //tri
            if (transType == ETransType.E_SAMSAT_INQUIRY || transType == ETransType.E_SAMSAT) {
                if (buff != null && buff.length > 0) {
                    Log.d("teg", "E-SAMSAT[field 61] : " + new String(buff));
                    transData.setField61(new String(buff));
                }
            } else {
                if (buff != null && buff.length > 0) {
                    transData.setField61(FinancialApplication.getConvert().bcdToStr(buff));//asli
                }
            }


            // field 62: Reserved for private use
            buff = map.get("62");
            if (buff != null && buff.length > 0) {
                transData.setField62(FinancialApplication.getConvert().bcdToStr(buff));
            }

            buff = map.get("63");
            if (buff != null && buff.length > 0) {
                //tri
                if (transType == ETransType.PDAM_PURCHASE           ||
                        transType == ETransType.PDAM_INQUIRY        ||
                        transType == ETransType.PURCHASE_PULSA_DATA ||
                        transType == ETransType.INQ_PULSA_DATA      ||
                        transType == ETransType.PASCABAYAR_PURCHASE ||
                        transType == ETransType.PASCABAYAR_INQUIRY  ||
                        transType == ETransType.BPJS_TK_PENDAFTARAN ||
                        transType == ETransType.BPJS_TK_VERIFICATION ||
                        transType == ETransType.BPJS_TK_INQUIRY     ||
                        transType == ETransType.BPJS_TK_PEMBAYARAN
                ) {
                    transData.setField63(new String(buff));
                } else {
                    transData.setField63(FinancialApplication.getConvert().bcdToStr(buff)); //asli
                }

            }

            // field 64: Message Authentication Code
            // 解包校验mac
            byte[] data = new byte[rsp.length - 11 - 8];
            System.arraycopy(rsp, 11, data, 0, data.length);
            buff = map.get("64");
            if (buff != null && buff.length > 0 && listener != null) {
                byte[] mac = listener.onCalcMac(data);
                if (!FinancialApplication.getGl().getUtils().isByteArrayValueSame(buff, 0, mac, 0, 8)) {
                    return TransResult.ERR_MAC;
                }
            }

            if (entity.getSecondaryBitmapOnOff()) {

                buff = map.get("102");
                if (buff != null && buff.length != 0) {
                    transData.setField102(new String(buff));
                    Log.i("dia21", "field102 = " + transData.getField102());
                }


                //Sandy :  add 110
                buff = map.get("110");
                if (buff != null && buff.length != 0) {
                    transData.setField110(new String(buff));
                    Log.i(TAG, "field110 = " + transData.getField110());

                }


                // add 120
                buff = map.get("120");
                if (buff != null && buff.length != 0) {
                    transData.setField120(new String(buff));
                    Log.i("abdul", "field120 = " + transData.getField120());
                }

                if (transType == ETransType.DIRJEN_PAJAK_INQUIRY ||
                        transType == ETransType.DIRJEN_BEA_CUKAI_INQUIRY ||
                        transType == ETransType.DIRJEN_ANGGARAN_INQUIRY) {

                    buff = map.get("102");
                    if (buff != null && buff.length != 0) {
                        transData.setAccNo(new String(buff));
                        Log.i("teg", "field102 = " + transData.getAccNo());
                    }

                }

                return TransResult.SUCC;
            } else {
                // field 63: Reserved for private use
                buff = map.get("63");
                if (buff == null || buff.length == 0) {
                    //如果内卡不返回,则设置默认值CUP
                    transData.setInterOrgCode("CUP");
                    return TransResult.SUCC;
                }
            }
            buff = map.get("63");
            if (buff != null || buff.length != 0) {
                if (transType != ETransType.VERIFY_PIN && transType != ETransType.CHANGE_PIN) {
                    try {
                        // 国际组织代码
                        transData.setInterOrgCode(new String(buff).substring(0, 3)); //f63.1[AN3]
                        // 63域附加信息域
                        byte[] reserved = new byte[buff.length - 3];
                        System.arraycopy(buff, 3, reserved, 0, reserved.length);
                        transData.setReserved(new String(reserved, "GBK").trim()); //63域附加域[ANS…120(LLLVAR)]

                        int len = buff.length - 3 > 20 ? 20 : buff.length - 3;
                        if (buff.length > 3) {
                            // 发卡行信息
                            byte[] issuerResp = new byte[20];
                            System.arraycopy(buff, 3, issuerResp, 0, len);
                            transData.setIssuerResp(new String(issuerResp, "GBK").trim()); // 发卡方保留域
                        }
                        if (buff.length > 23) {
                            // 中心信息
                            len = buff.length - 23 > 20 ? 20 : buff.length - 23;
                            byte[] centerResp = new byte[20];
                            System.arraycopy(buff, 23, centerResp, 0, len);
                            transData.setCenterResp(new String(centerResp, "GBK").trim()); // 中国银联保留域
                        }
                        if (buff.length > 43) {
                            //02.10版小费流程控制
                            if (buff.length == 45 && buff[43] == 0x54 && FinancialApplication
                                    .getSysParam().get(SysParam.TIP_MODE) == "2") {
                                if (buff[44] == 0x59) {
                                    transData.setTipSupport(1);
                                } else {
                                    transData.setTipSupport(0);
                                }
                            } else {
                                transData.setTipSupport(0);
                            }

                            // 收单行信息
                            len = buff.length - 43 > 20 ? 20 : buff.length - 43;
                            byte[] recvBankResp = new byte[20];
                            System.arraycopy(buff, 43, recvBankResp, 0, len);
                            transData.setRecvBankResp(new String(recvBankResp, "GBK").trim()); //
                            // 受理机构保留域
                            return TransResult.SUCC;
                        }
                    } catch (UnsupportedEncodingException e) {
                        Log.e(TAG, "", e);
                    }
                }
            }
            return TransResult.SUCC;
        } catch (Exception e) {
            Log.e("abdul", " error unpack " + e);
            e.printStackTrace();
            return TransResult.ERR_UNPACK;
        }

    }

    /**
     * 检查请求和返回的关键域field4, field11, field41, field42
     *
     * @param map        解包后的map
     * @param transData  请求
     * @param isCheckAmt 是否检查field4
     * @return
     */
    protected int checkRecvData(HashMap<String, byte[]> map, TransData transData,
                                boolean isCheckAmt) {
        String temp;
        byte[] data;
        ETransType transType = ETransType.valueOf(transData.getTransType());

        // 交易金额
        if (isCheckAmt) {
            data = map.get("4");
            String amount;
            if (transType == ETransType.COUPON_SALE ||
                    transType == ETransType.COUPON_SALE_VOID ||
                    transType == ETransType.E_SAMSAT_INQUIRY ||
                    transType == ETransType.PEMBATAL_REK_INQ ||
                    transType == ETransType.PEMBUKAAN_REK ||
                    transType == ETransType.TRANSFER_INQ ||
                    transType == ETransType.DIRJEN_BEA_CUKAI_INQUIRY ||
                    transType == ETransType.DIRJEN_ANGGARAN_INQUIRY ||
                    transType == ETransType.CETAK_ULANG ||
                    transType == ETransType.PDAM_PURCHASE ||
                    transType == ETransType.PDAM_INQUIRY ||
                    transType == ETransType.PASCABAYAR_PURCHASE ||
                    transType == ETransType.PASCABAYAR_INQUIRY  ||
                    transType == ETransType.DIRJEN_PAJAK_INQUIRY ||
                    transType == ETransType.BPJS_TK_INQUIRY) {
                amount = transData.getActualPayAmount();
                Log.d("teg", "amount 1 : " + amount);
            } else {
                amount = transData.getAmount();
                Log.d("teg", "amount 2 : " + amount);
            }

            if (!(transType == ETransType.OVERBOOKING_PULSA_DATA    ||
                    transType == ETransType.PDAM_OVERBOOKING        ||
                    transType == ETransType.BPJS_TK_PENDAFTARAN     || //sandy
                    transType == ETransType.BPJS_OVERBOOKING        || //sandy
                    transType == ETransType.PASCABAYAR_OVERBOOKING)) {
                if ((data != null && data.length > 0) && (amount != null && amount.length() > 0)) {
                    temp = new String(data);
                    if (Long.parseLong(temp) != Long.parseLong(amount)) {
                        return TransResult.ERR_TRANS_AMT;
                    }
                }
            }
        }
        // 校验11域:交易流水号
        data = map.get("11");
        if (data != null && data.length > 0) {
            temp = new String(data);
            long transNo;
            if (transType == ETransType.COUPON_VERIFY_VOID) {
                transNo = transData.getOrigTransNo();
            } else {
                transNo = transData.getTransNo();
            }
            //Sandy :
            //Edited here, because the COUPON_SALE has 1 flow at 1 time, so the field 11 should have a different values
            //1. Verify Coupon
            //2. Sale Coupon
            if (!temp.equals(String.format("%06d", transNo)) && transType != ETransType.COUPON_SALE) {
                return TransResult.ERR_TRACE_NO;
            }
        }
        // 校验终端号
        data = map.get("41");
        if (data != null && data.length > 0) {
            temp = new String(data);
            if (!temp.equals(transData.getTermID())) {
                return TransResult.ERR_TERM_ID;
            }
        }
        // 校验商户号
        data = map.get("42");
        if (data != null && data.length > 0) {
            temp = new String(data);
            if (!temp.equals(transData.getMerchID())) {
                return TransResult.ERR_MERCH_ID;
            }
        }
        return TransResult.SUCC;
    }

    protected final void setBitData(String field, String value) throws Iso8583Exception {
        if (!TextUtils.isEmpty(value)) {
            entity.setFieldValue(field, value);
        }
    }

    protected final void setBitData(String field, byte[] value) throws Iso8583Exception {
        if (value != null && value.length > 0) {
            entity.setFieldValue(field, value);
        }
    }

    protected void setBitData1(@NonNull TransData transData) throws Iso8583Exception {
        //do nothing
    }

    protected void setBitData2(@NonNull TransData transData) throws Iso8583Exception {
        int enterMode = transData.getEnterMode();
        if (enterMode == EnterMode.MANUAL
                || enterMode == EnterMode.SWIPE
                || enterMode == EnterMode.INSERT
                || enterMode == EnterMode.QPBOC
                || enterMode == EnterMode.CLSS_PBOC
                || enterMode == EnterMode.QR) {
            setBitData("2", transData.getPan());
        }
    }

    protected void setBitData3(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("3", transData.getTransTypeEnum().getProcCode());
    }

    protected void setBitData4(@NonNull TransData transData) throws Iso8583Exception {
        String amt = transData.getAmount();
        setBitData("4", amt);

    }

    protected void setBitData5(@NonNull TransData transData) throws Iso8583Exception {
        //do nothing
    }

    protected void setBitData6(@NonNull TransData transData) throws Iso8583Exception {
        //do nothing
    }

    protected void setBitData7(@NonNull TransData transData) throws Iso8583Exception {
        //sandy
        //added bit 7
        setBitData("7", String.valueOf(transData.getDateTimeTrans()));
    }

    protected void setBitData8(@NonNull TransData transData) throws Iso8583Exception {
        //do nothing
    }

    protected void setBitData9(@NonNull TransData transData) throws Iso8583Exception {
        //do nothing
    }

    protected void setBitData10(@NonNull TransData transData) throws Iso8583Exception {
        //do nothing
    }

    protected void setBitData11(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("11", String.valueOf(transData.getTransNo()));
    }

    protected void setBitData12(@NonNull TransData transData) throws Iso8583Exception {
        String time = transData.getTime();
        if (!TextUtils.isEmpty(time)) {
            setBitData("12", time);
        }
    }

    protected void setBitData13(@NonNull TransData transData) throws Iso8583Exception {
        String date = transData.getDate();
        if (!TextUtils.isEmpty(date)) {
            setBitData("13", date);
        }
    }

    protected void setBitData14(@NonNull TransData transData) throws Iso8583Exception {
        int enterMode = transData.getEnterMode();
        if (enterMode == EnterMode.MANUAL
                || enterMode == EnterMode.INSERT
                || enterMode == EnterMode.QPBOC
                || enterMode == EnterMode.CLSS_PBOC
                || enterMode == EnterMode.QR)
            setBitData("14", transData.getExpDate());
    }

    protected void setBitData15(@NonNull TransData transData) throws Iso8583Exception {
        String settleDate = transData.getSettleDate();
        if (!TextUtils.isEmpty(settleDate)) {
            setBitData("15", settleDate);
        }
    }

    protected void setBitData16(@NonNull TransData transData) throws Iso8583Exception {
        //do nothing
    }

    protected void setBitData17(@NonNull TransData transData) throws Iso8583Exception {
        //do nothing
    }

    //sandy
    protected void setBitData18(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("18", transData.getMCC());
    }

    protected void setBitData19(@NonNull TransData transData) throws Iso8583Exception {
        //do nothing
    }

    protected void setBitData20(@NonNull TransData transData) throws Iso8583Exception {
        //do nothing
    }

    protected void setBitData21(@NonNull TransData transData) throws Iso8583Exception {
        //do nothing
    }

    protected void setBitData22(@NonNull TransData transData) throws Iso8583Exception {
        //setBitData("22", getInputMethod(transData.getEnterMode(), transData.getHasPin()));
        Log.d("teg", "[f22]" + getInputMethod2(transData.getEnterMode(), transData.getHasPin()));
        setBitData("22", getInputMethod2(transData.getEnterMode(), transData.getHasPin()));
    }

    protected void setBitData23(@NonNull TransData transData) throws Iso8583Exception {
        int enterMode = transData.getEnterMode();
        if (enterMode == EnterMode.INSERT
                || enterMode == EnterMode.QPBOC
                || enterMode == EnterMode.CLSS_PBOC
                || enterMode == EnterMode.QR) {
            setBitData("23", transData.getCardSerialNo());
        }
    }

    protected void setBitData24(@NonNull TransData transData) throws Iso8583Exception {
        // do nothing
    }

    protected void setBitData25(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("25", transData.getTransTypeEnum().getServiceCode());
    }

    protected void setBitData26(@NonNull TransData transData) throws Iso8583Exception {
        if (transData.getHasPin()) {
            setBitData("26", "12");
        }
    }

    protected void setBitData27(@NonNull TransData transData) throws Iso8583Exception {
        //do nothing
    }

    protected void setBitData28(@NonNull TransData transData) throws Iso8583Exception {
        //do nothing
    }

    protected void setBitData29(@NonNull TransData transData) throws Iso8583Exception {
        //do nothing
    }

    protected void setBitData30(@NonNull TransData transData) throws Iso8583Exception {
        //do nothing
    }

    protected void setBitData31(@NonNull TransData transData) throws Iso8583Exception {
        //do nothing
    }

    protected void setBitData32(@NonNull TransData transData) throws Iso8583Exception {
        String temp = transData.getAcqCenterCode();
        if (transData.getTransType().equals(ETransType.ACCOUNT_LIST.toString())) {
            setBitData("32", "000110");
        } else if (!TextUtils.isEmpty(temp)) {
            setBitData("32", temp);
        }
    }

    protected void setBitData33(@NonNull TransData transData) throws Iso8583Exception {
        //do nothing
        if (transData.getTransType().equals(ETransType.ACCOUNT_LIST.toString()) ||
                transData.getTransType().equals(ETransType.BALANCE_INQUIRY.toString()) ||
                transData.getTransType().equals(ETransType.BALANCE_INQUIRY_2.toString())

        ) {
            setBitData("33", "000110");
        }
    }

    protected void setBitData34(@NonNull TransData transData) throws Iso8583Exception {
        //do nothing
    }

    protected void setBitData35(@NonNull TransData transData) throws Iso8583Exception {

        int enterMode = transData.getEnterMode();
        if (enterMode != TransData.EnterMode.SWIPE && enterMode != TransData.EnterMode.FALLBACK && enterMode != EnterMode.INSERT) {
            return;
        }

        String track2 = transData.getTrack2();
        if (!TextUtils.isEmpty(track2) && transData.getIsEncTrack()) {
            // 加密
            track2 = new String(listener.onEncTrack(track2.getBytes()));
        }

        if (!TextUtils.isEmpty(track2)) {
            if (track2.length() > 37) {
                String trak2abdul = track2.substring(0, 37);
                setBitData("35", trak2abdul);
            } else {
                setBitData("35", track2);
            }
        }

        //??
        /*try {
            Log.i("abdul", "cek bit35 = " + track2);
            String trak2abdul = track2.substring(0, 37);
            setBitData("35", trak2abdul);
        } catch (Exception e) {
            e.printStackTrace();
            setBitData("35", track2);
        }*/
        //setBitData("35", "622011990000000299D2603221");
    }

    protected void setBitData36(@NonNull TransData transData) throws Iso8583Exception {
        int enterMode = transData.getEnterMode();
        if (enterMode != TransData.EnterMode.SWIPE && enterMode != EnterMode.FALLBACK && enterMode != EnterMode.INSERT) {
            return;
        }
        String track3 = transData.getTrack3();
        if (!TextUtils.isEmpty(track3) && transData.getIsEncTrack()) {
            // 加密
            track3 = new String(listener.onEncTrack(track3.getBytes()));
        }
        setBitData("36", track3);
    }

    protected void setBitData37(@NonNull TransData transData) throws Iso8583Exception {
        //setBitData("37", transData.getOrigRefNo());
        // test
        String padd = String.valueOf(transData.getTransNo());
        String bit7 = String.valueOf(transData.getDateTimeTrans());
        bit7 = StringUtils.leftPad(bit7, 10, "0");
        padd = StringUtils.leftPad(padd, 6, "0");
        setBitData("37", bit7 + padd.substring(4, 6)); // format 37 pakai datetime MMddHHmmss Pak
    }

    protected void setBitData38(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("38", transData.getOrigAuthCode());
    }

    protected void setBitData39(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("39", transData.getReason());
    }

    protected void setBitData40(@NonNull TransData transData) throws Iso8583Exception {
        //do nothing
    }

    protected void setBitData41(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("41", transData.getTermID());
    }

    protected void setBitData42(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("42", transData.getMerchID());
    }

    //sandy
    protected void setBitData43(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("43", transData.getMerName());
    }

    protected void setBitData44(@NonNull TransData transData) throws Iso8583Exception {
        String temp = FinancialApplication.getSysParam().getCurrency().getCode() +
                FinancialApplication.getConvert().stringPadding(transData.getAmount(), '0', 12,
                        EPaddingPosition.PADDING_LEFT) +
                transData.getCurrencyRate();
        entity.setFieldValue("44", temp);
    }

    protected void setBitData45(@NonNull TransData transData) throws Iso8583Exception {
        //do nothing
    }

    protected void setBitData46(@NonNull TransData transData) throws Iso8583Exception {
        //do nothing
    }

    protected void setBitData47(@NonNull TransData transData) throws Iso8583Exception {
        //do nothing
    }

    protected void setBitData48(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("48", transData.getField48());
    }

    protected void setBitData49(@NonNull TransData transData) throws Iso8583Exception {
        Currency currency = FinancialApplication.getSysParam().getCurrency();
        setBitData("49", currency.getCode());
    }

    protected void setBitData50(@NonNull TransData transData) throws Iso8583Exception {
        //do nothing
    }

    protected void setBitData51(@NonNull TransData transData) throws Iso8583Exception {
        //do nothing
    }

    protected void setBitData52(@NonNull TransData transData) throws Iso8583Exception {
        if (!transData.getHasPin() || TextUtils.isEmpty(transData.getPin())) {
            return;
        }
        if (transData.getIsSM()) {
            setBitData("52", new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                    0x00, 0x00});
        } else {
            setBitData("52", FinancialApplication.getConvert().strToBcd(transData.getPin(),
                    EPaddingPosition.PADDING_LEFT));
        }
    }

    protected void setBitData53(@NonNull TransData transData) throws Iso8583Exception {
        if (!transData.getHasPin() && !entity.hasField("35") && !entity.hasField("36")) {
            return;
        }

        StringBuilder field53 = new StringBuilder();
        int enterMode = transData.getEnterMode();
        if (enterMode == EnterMode.SWIPE ||        // 刷卡
                enterMode == EnterMode.FALLBACK) {     //FallBack
            field53.append("1600000000000000");
        } else {
            field53.append("2600000000000000");
        }
        if (!transData.getHasPin()) {
            field53.replace(0, 1, "0");
        }
        if (transData.getIsSM()) {
            field53.replace(1, 2, "3");
        }
        if (transData.getIsEncTrack() && (entity.hasField("35") || entity.hasField
                ("36"))) {
            field53.replace(2, 3, "1");
        }

        setBitData("53", field53.toString());
    }

    protected void setBitData54(@NonNull TransData transData) throws Iso8583Exception {

    }

    protected void setBitData55(@NonNull TransData transData) throws Iso8583Exception {
        String iccData = transData.getSendIccData();
        if (!TextUtils.isEmpty(iccData)) {
            setBitData("55", FinancialApplication.getConvert().strToBcd(iccData,
                    EPaddingPosition.PADDING_LEFT));
        }
    }

    protected void setBitData56(@NonNull TransData transData) throws Iso8583Exception {

    }

    protected void setBitData57(@NonNull TransData transData) throws Iso8583Exception {

    }

    protected void setBitData58(@NonNull TransData transData) throws Iso8583Exception {

    }

    protected void setBitData59(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("59", transData.getField59());
    }

    protected void setBitData60(@NonNull TransData transData) throws Iso8583Exception {
        ETransType transType = ETransType.valueOf(transData.getTransType());
        StringBuilder f60 = new StringBuilder(transType.getFuncCode()); // f60.1：transaction
        // type// code[N2]
        f60.append(String.format("%06d", transData.getBatchNo())); // f60.2: Batch number[N6]
        f60.append(transType.getNetCode());// f60.3: network management information code[N3]


        if (transData.getIsFallback()) {
            f60.append("620000"); //f60.4~f60.7, Richard 20170506
        } else {
            f60.append("600000"); //f60.4~f60.7, Richard 20170506
        }

        int decimals = FinancialApplication.getSysParam().getCurrency().getDecimals(); //f60.8
        if ((decimals >= 1) && (decimals <= 3)) {
            f60.append(Integer.toString(decimals));
        } else {
            f60.append('a');
        }
        f60.append("00");
        setBitData("60", f60.toString());
    }

    protected void setBitData61(@NonNull TransData transData) throws Iso8583Exception {
        String temp;
        StringBuilder f61 = new StringBuilder();

        temp = String.format("%06d", transData.getOrigBatchNo());
        if (!TextUtils.isEmpty(temp)) {
            f61.append(temp);
        } else {
            f61.append("000000");
        }

        temp = String.format("%06d", transData.getOrigTransNo());
        if (!TextUtils.isEmpty(temp)) {
            f61.append(temp);
        } else {
            f61.append("000000");
        }
        temp = transData.getOrigDate();
        if (!TextUtils.isEmpty(temp)) {
            f61.append(temp);
        } else {
            f61.append("0000");
        }
        setBitData("61", f61.toString());
    }

    protected void setBitData62(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("62", transData.getField62());
    }

    // set field 63
    protected void setBitData63(@NonNull TransData transData) throws Iso8583Exception {
        String interOrgCode = transData.getInterOrgCode();
        if (!TextUtils.isEmpty(interOrgCode)) {
            interOrgCode = "000";
        }
        setBitData("63", interOrgCode);
    }

    protected void setBitData64(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("64", new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                0x00});
    }

    protected void setBitData70(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("70", "001");
    }

    protected void setBitData90(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("90", "");
    }

    protected void setBitData100(@NonNull TransData transData) throws Iso8583Exception {
        //do nothing
        setBitData("100", "");
    }

    protected void setBitData102(@NonNull TransData transData) throws Iso8583Exception {
        //do nothing
    }

    protected void setBitData103(@NonNull TransData transData) throws Iso8583Exception {
        //do nothing
    }

    protected void setBitData107(@NonNull TransData transData) throws Iso8583Exception {
        //do nothing
    }

    protected void setBitData110(@NonNull TransData transData) throws Iso8583Exception {
        setBitData("110", transData.getField110());
    }


    protected void setBitData106(@NonNull TransData transData) throws Iso8583Exception {
        //do nothing
    }

    //sandy
    protected void setBitData113(@NonNull TransData transData) throws Iso8583Exception {
        //do nothing
    }

    protected void setBitData125(@NonNull TransData transData) throws Iso8583Exception {
        //do nothing
    }

    protected void setBitData127(@NonNull TransData transData) throws Iso8583Exception {
        //do nothing
    }


    protected String getInputMethod(int enterMode, boolean hasPin) {
        String inputMethod = "";
        switch (enterMode) {
            case TransData.EnterMode.MANUAL:
                inputMethod = "01";
                break;
            case TransData.EnterMode.SWIPE:
                inputMethod = "02";
                break;
            case TransData.EnterMode.INSERT:
                inputMethod = "05";
                break;
            case TransData.EnterMode.QPBOC:
                inputMethod = "07";
                break;
            case TransData.EnterMode.FALLBACK:
                inputMethod = "90";     //"80" -> "90"
                break;
            case TransData.EnterMode.PHONE:
                inputMethod = "92";
                break;
            case TransData.EnterMode.MOBILE:
                inputMethod = "96";
                break;
            case TransData.EnterMode.CLSS_PBOC:
                inputMethod = "98";
                break;
            case TransData.EnterMode.QR:
                inputMethod = "03"; // 文档上要求04，实际上银联要求03
                break;

            default:
                break;
        }

        if (hasPin) {
            inputMethod += "1";
        } else {
            inputMethod += "2";
        }

        return inputMethod;
    }

    protected String getInputMethod2(int enterMode, boolean hasPin) {
        String inputMethod = "";
        switch (enterMode) {
            case TransData.EnterMode.MANUAL:
                inputMethod = "1";
                break;
            case TransData.EnterMode.SWIPE:
                inputMethod = "2";
                break;
            case TransData.EnterMode.INSERT:
                inputMethod = "5";
                break;
            case TransData.EnterMode.QPBOC:
                inputMethod = "7";
                break;
            case TransData.EnterMode.FALLBACK:
                inputMethod = "90";     //"80" -> "90"
                break;
            case TransData.EnterMode.PHONE:
                inputMethod = "92";
                break;
            case TransData.EnterMode.MOBILE:
                inputMethod = "96";
                break;
            case TransData.EnterMode.CLSS_PBOC:
                inputMethod = "98";
                break;
            case TransData.EnterMode.QR:
                inputMethod = "03"; // 文档上要求04，实际上银联要求03
                break;

            default:
                break;
        }

        if (hasPin) {
            inputMethod += "1";
        } else {
            inputMethod += "2";
        }

        inputMethod = StringUtils.rightPad(inputMethod, 3, "0");

        return inputMethod;
    }

    public static void hex_dump_char(String pzTitle, byte[] buff, long len) {
        int a = 0, dump = 8;
        byte[] buffdata = new byte[1024];
        byte[][] line = new byte[512][dump];

        try {
            // copy the data to another memory
            System.arraycopy(buff, 0, buffdata, 0, buff.length);

//            private Bitmap generateBitmap(){
//
//            }

            // print title, len and memory address first
            printStr(pzTitle);
            printStr(String.format("Len:%d ", len) + "Addrs: " + buff);

            // if there is no data in the buffer
            // memory is empty
            if (len == 0 || buffdata.length == 0) return;
            if (buff.length != len) {
                printStr("Error DUMP len not equals!!!!!!");
                return;
            }

            int lenTotal = (int) len / dump;
            int sisa = (int) len % dump;

            try {
                for (a = 0; a <= lenTotal; a++) {
                    System.arraycopy(buff, a * dump, line[a], 0, dump);
                    String print = "";
                    for (int b = 0; b < dump; b++) {
                        print = print + Fox.Byte2Hex(line[a][b]) + " ";
                    }
                    String dataAllPrint = Component.getPaddedNumber(a, 3) + "| " + print + "    " + Fox.GetByte2Txt(line[a]);
                    printStr(dataAllPrint);
                }
            } catch (Exception e) {
            }

            if (sisa > 0) {
                System.arraycopy(buff, lenTotal * dump, line[lenTotal], 0, sisa);
                String print = "";
                for (int b = 0; b < sisa; b++) {
                    print = print + Fox.Byte2Hex(line[a][b]) + " ";
                }
                String dataAllPrint = Component.getPaddedNumber(a, 3) + "| " + print + Component.getPaddedString("", 28 - sisa * 2 - sisa, ' ') + Fox.GetByte2Txt(line[lenTotal]);
                printStr(dataAllPrint);
            }

            printStr("\n\n");
        } catch (Exception ex) {
            printStr("Error DUMP Brrrrr!!!!!!");
        }
    }

    public static int printStr(String str) {
        IPrinter printer = FinancialApplication.getDal().getPrinter();
        try {
            printer.init();
            printer.setGray(4);
            printer.spaceSet((byte) 0, (byte) 0);//word,line
            printer.fontSet(EFontTypeAscii.FONT_8_16, EFontTypeExtCode.FONT_16_16);
            printer.printStr(str, null);
            return printer.start();
        } catch (PrinterDevException e) {
            Log.e("printStr kd: ", "", e);
        }
        return -1;
    }

    public int dumpIso(byte[] rsp) {
        //kd add
        byte[] buff;
        HashMap<String, byte[]> map;

        try {
            map = iso8583.unpack(rsp, true);
            byte[] header = map.get("h");
            byte[] mti = map.get("m");
            Fox.printStr("TPDU : " + Fox.Hex2Txt(Fox.Byte2Hex(header)) + "   MTI : " + Fox.Hex2Txt(Fox.Byte2Hex(mti)));
            for (int i = 0; i <= 128; i++) {
                buff = map.get(Integer.toString(i));
                if (buff != null) {
                    Fox.printStr("Bit " + Integer.toString(i) + ": " + Fox.Hex2Txt(Fox.Byte2Hex(buff)));
                    /*Fox.printStr("Bit "+ Integer.toString(i) + ": " + Fox.Byte2Hex(buff));*/
                }
            }
            Fox.printStr("\n\n\n\n\n");
        } catch (Iso8583Exception | ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }

        return 0;
    }
}
