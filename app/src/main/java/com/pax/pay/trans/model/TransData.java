package com.pax.pay.trans.model;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.pax.gl.db.DbException;
import com.pax.gl.db.IDb.AEntityBase;
import com.pax.gl.db.IDb.Column;
import com.pax.gl.db.IDb.IDao;
import com.pax.gl.db.IDb.IDbListener;
import com.pax.gl.db.IDb.Unique;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.record.TransDBHelper;
import com.pax.pay.utils.CollectionUtils;
import com.pax.settings.SysParam;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TransData extends AEntityBase implements Serializable, Cloneable {
    public static final String TAG = "TransData";

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public static final String REASON_NO_RECV = "98";
    public static final String REASON_MACWRONG = "A0";
    public static final String REASON_OTHERS = "06";

    /**
     * 交易状态
     *
     * @author Steven.W
     *
     */
    public enum ETransStatus {
        /**
         * 正常
         */
        NORMAL,
        /**
         * 已撤销
         */
        VOID,
        /**
         * 已调整
         */
        ADJUST
    }

    /* 脱机上送失败原因 */

    public static class OfflineStatus {
        /**
         * offline not sent
         */
        public static final int OFFLINE_NOT_SENT = 0x00;
        /**
         * offline sent
         */
        public static final int OFFLINE_SENT = 0x01;
        /**
         * 脱机上送失败
         */
        public static final int OFFLINE_ERR_SEND = 0x02;
        /**
         * 脱机上送平台拒绝(返回码非00)
         */
        public static final int OFFLINE_ERR_RESP = 0x03;
        /**
         * 脱机上送未知失败原因
         */
        public static final int OFFLINE_ERR_UNKN = 0xff;
    }

    /**
     * 电子签名上送状态
     */
    public static class SignSendStatus {
        public static final int SEND_SIG_NO = 0x00; // 未上送
        public static final int SEND_SIG_SUCC = 0x01; // 上送成功
        public static final int SEND_SIG_ERR = 0X02; // 上送失败
    }

    public static class EnterMode {
        /**
         * 手工输入
         */
        public static final int MANUAL = 1;
        /**
         * 刷卡
         */
        public static final int SWIPE = 2;
        /**
         * 插卡
         */
        public static final int INSERT = 3;
        /**
         * IC卡回退
         */
        public static final int FALLBACK = 4;
        /**
         * 预约支付
         */
        public static final int PHONE = 5;
        /**
         * 非接快速支付
         */
        public static final int QPBOC = 6;
        /**
         * 非接完整PBOC
         */
        public static final int CLSS_PBOC = 7;
        /**
         * 非接读取CUPMobile
         */
        public static final int MOBILE = 8;
        /**
         * 扫码支付
         */
        public static final int QR = 9;
    }

    /**
     * 授权方式
     */
    public static class AuthMode {
        /**
         * 本系统预授权的结算
         */
        public static final String POS = "00";
        /**
         * 电话授权的结算
         */
        public static final String PHONE = "01";
        /**
         * 小额代授权的结算
         */
        public static final String SMALL_GEN_AUTH = "02";

    }

    /**
     * Tip mode
     */
    public static class TipMode {
        /**
         * No need to adjust.
         */
        public static final String NO_ADJUST = "1";

        /**
         * Need to adjust.
         */
        public static final String ADJUST = "2";
    }

    // ============= Need to Store ==========================
    @Column(canBeNull = true)
    private String transState; // Transaction Status
    @Column
    private boolean isUpload; // 是否已批上送
    @Column
    private boolean isOffUploadState; // 是否已脱机上送,true:脱机上送成功
    @Column
    private int sendFailFlag; // 脱机上送失败类型 ：上送失败/平台拒绝
    @Column
    private int sendTimes; // 已批上送次数
    @Column
    private String transType; // 交易类型
    @Column(canBeNull = true)
    private String origTransType; // 原交易类型
    @Column(canBeNull = true)
    private String procCode; // 处理码，39域
    @Column(canBeNull = true)
    private String amount="0"; // 交易金额
    // add abdul sell price pulsa data
    @Column(canBeNull = true)
    private String sellPrice="0"; // 交易金额
    @Column(canBeNull = true)
    private String tipAmount; // 小费金额
    @Column(canBeNull = true)
    private String balance; // 余额
    @Column(canBeNull = true)
    private String balanceFlag; // 余额标识C/D
    @Column
    @Unique
    private long transNo; // pos流水号
    @Column
    private long origTransNo; // 原pos流水号
    @Column
    private long batchNo; // 批次号
    @Column
    private long origBatchNo; // 原批次号

    /*@Column(canBeNull = true)
    private long dupTransNo;*/

    //sandy
    @Column
    private long dateTimeTrans;

    @Column(canBeNull = true)
    private Long origDateTimeTrans;


    @Column(canBeNull = true)
    private String pan; // 主账号

    @Column(canBeNull = true)
    private String transferPan; // 转入卡卡号

    @Column(canBeNull = true)
    private String time; // 交易时间
    @Column(canBeNull = true)
    private String date; // 交易日期
    @Column(canBeNull = true)
    private String origDate; // 原交易日期
    @Column(canBeNull = true)
    private String settleDate; // 清算日期
    @Column(canBeNull = true)
    private String expDate; // 卡有效期
    @Column(canBeNull = true)
    private int enterMode; // 输入模式
    @Column(canBeNull = true)
    private int transferEnterMode; // 转入卡的输入模式
    @Column(canBeNull = true)
    private String refNo; // 系统参考号
    @Column(canBeNull = true)
    private String origRefNo; // 原系统参考号
    @Column(canBeNull = true)
    private String authCode; // 授权码
    @Column(canBeNull = true)
    private String origAuthCode; // 原授权码
    @Column(canBeNull = true)
    private String isserCode; // 发卡行标识码
    @Column(canBeNull = true)
    private String acqCode; // 收单机构标识码
    @Column(canBeNull = true)
    private String acqCenterCode; // 受理方标识码,pos中心号(返回包时用)
    @Column(canBeNull = true)
    private String interOrgCode; // 国际组织代码

    private int tipSupport;   //该笔交易是否支持小费1支持0不支持

    @Column
    private boolean hasPin; // 是否有输密码
    @Column(canBeNull = true)
    private String track1; // 磁道一信息

    @Column(canBeNull = true)
    private String track2; // 磁道二数据
    @Column(canBeNull = true)
    private String track3; // 磁道三数据
    @Column
    private boolean isEncTrack; // 磁道是否加密
    @Column(canBeNull = true)
    private String reason; // 冲正原因
    @Column(canBeNull = true)
    private String reserved; // 63域附加域
    @Column(canBeNull = true)
    private String issuerResp; // 发卡方保留域
    @Column(canBeNull = true)
    private String centerResp; // 中国银联保留域
    @Column(canBeNull = true)
    private String recvBankResp;// 受理机构保留域
    @Column(canBeNull = true)
    private String scriptData; // 脚本数据

    @Column(canBeNull = true)
    private String authMode; // 授权方式
    @Column(canBeNull = true)
    private String authInsCode; // 授权机构代码
    @Column
    private boolean isAdjustAfterUpload; // 离线结算上送后被调整，标识为true

    // 增加扫码数据
    @Column(canBeNull = true)
    private String c2b; // 55域TagA3 扫码付C2B信息码
    @Column(canBeNull = true)
    private String c2bVoucher; // 55域 应答TagA4 扫码付付款凭证码
    @Column(canBeNull = true)
    private String origC2bVoucher; // 原付款凭证码

    // =================EMV数据=============================
    @Column
    private boolean pinFree; // 免密
    @Column
    private boolean signFree; // 免签
    @Column
    private boolean isCDCVM; // CDCVM标识

    @Column
    private boolean isOnlineTrans; // 是否为联机交易
    // 电子签名专用
    @Column(canBeNull = true)
    private byte[] signData; // signData

    @Column
    private int signSendState; // 上送状态：0，未上送；1，上送成功；2，上送失败
    @Column
    private boolean signUpload; // 1:已重上送；0：未重上送

    private String receiptElements; // 电子签名时，55域签购单信息
    // =================EMV数据=============================
    /**
     * EMV交易的执行状态
     */
    @Column(canBeNull = true)
    private byte emvResult; // EMV交易的执行状态
    @Column(canBeNull = true)
    private String cardSerialNo; // 23 域，卡片序列号
    @Column(canBeNull = true)
    private String sendIccData; // IC卡信息,55域
    @Column(canBeNull = true)
    private String dupIccData; // IC卡冲正信息,55域
    @Column(canBeNull = true)
    private String tc; // IC卡交易证书(TC值)tag9f26,(BIN)
    @Column(canBeNull = true)
    private String arqc; // 授权请求密文(ARQC)
    @Column(canBeNull = true)
    private String arpc; // 授权响应密文(ARPC)
    @Column(canBeNull = true)
    private String tvr; // 终端验证结果(TVR)值tag95
    @Column(canBeNull = true)
    private String aid; // 应用标识符AID
    @Column(canBeNull = true)
    private String emvAppLabel; // 应用标签
    @Column(canBeNull = true)
    private String emvAppName; // 应用首选名称
    @Column(canBeNull = true)
    private String tsi; // 交易状态信息(TSI)tag9B
    @Column(canBeNull = true)
    private String atc; // 应用交易计数器(ATC)值tag9f36
    @Column(canBeNull = true)
    private String field47;
    @Column(canBeNull = true)
    private String accNo;

    // =================Currency data=========================
    @Column(canBeNull = true)
    private String cardCurrencyCode;
    @Column(canBeNull = true)
    private String terminalCurrencyCode;
    @Column(canBeNull = true)
    private String terminalCurrencyName;
    @Column(canBeNull = true)
    private String termianlCurrencyDecimals;
    @Column(canBeNull = true)
    private String currencyRate;
    @Column(canBeNull = true)
    private String foreignAmount;

    // =================Installment data======================
    //Jerry add 20170517
    @Column(canBeNull = true)
    private String instalNum;			    //分期付款的分期数  L=3
    @Column(canBeNull = true)
    private String prjCode;        	    //分期付款的项目编码     L=0~30
    @Column(canBeNull = true)
    private String firstAmount;       	//分期付款的首付金额,(BCD)   L=12
    @Column(canBeNull = true)
    private String feeTotalAmount;        //分期付款手续费总金额/一次性付款手续费   L=1
    @Column(canBeNull = true)
    private String instalCurrCode;   		//分期付款币种       344  L=3

    // =================Coupon sale data======================
    @Column(canBeNull = true)
    private String couponNo;
    @Column(canBeNull = true)
    private String discountAmount;
    @Column(canBeNull = true)
    private String actualPayAmount;




    // ================不需要存储=============================
    /**
     * 是否Fall Back降级处理
     */
    private boolean isFallback;
    /**
     * 消息类型
     */
    private String msgID;
    /**
     * 个人密码(密文)
     */
    private String pin;
    /**
     * 安全控制信息
     */
    private String srcInfo;
    /**
     * 操作员号
     */
    private String oper;
    /**
     * 响应码
     */
    private String responseCode;
    /**
     * 相应码对应的错误信息
     */
    private String responseMsg;
    /**
     * 终端号
     */
    private String termID;

    /**
     * 原交易终端号
     */
    @Column(canBeNull = true)
    private String origTermID;

    @Column(canBeNull =  true)
    private String field61;
    /**
     * 商户号
     */
    private String merchID;

    private String header;
    private String tpdu;

    private boolean isReversal;
    @Column(canBeNull =  true)
    private String field48;

    private String field54;

    @Column(canBeNull =  true)
    private String field59;
    private String field60;
    private String field62;
    private String field63;
    private String field120;
    private String accType;
    @Column(canBeNull =  true)
    private String phoneNo; // pulsa/data
    @Column(canBeNull =  true)
    private String product_code;
    private String type_product;
    private String operator;
    private String product_name;
    private String keterangan;
    private String feeTrx;

    //E-SAMSAT
    private String samsatKodeBayar;
    private String samsatMerchantKode;

    @Column(canBeNull = true)
    private String field28;
    @Column(canBeNull = true)
    private String field36;
    @Column(canBeNull = true)
    private String field127;
    @Column(canBeNull = true)
    private String field103;
    @Column(canBeNull = true)
    private String field110;

    @Column(canBeNull = true)
    private String field102;
    private String field107;
    @Column(canBeNull = true)
    private String destBank;
    private String refferenceNo;
    private String dupf48;
    @Column(canBeNull = true)
    private String printTimeout;

    @Column(canBeNull = true)
    private String billingId;
    @Column(canBeNull = true)
    private String ntb;
    @Column(canBeNull = true)
    private String reprintData;

    private boolean isSM; // 是否支持国密
    private String recvIccData;
    private String field3;

    private String cardCVN2; //3 bytes CVN2 data, Richard 20170506, don't save to database now.

    //sandy
    private String merName; //Card acceptor Name
    private String MCC; //Card acceptor Name
    @Column(canBeNull = true)
    private int origEnterMode;
    @Column(canBeNull = true)
    private boolean origHasPin;
    @Column(canBeNull = true)
    private String origCouponRefNo;

    @Column(canBeNull = true)
    private long origCouponDateTimeTrans;


    //sandy

    public long getOrigCouponDateTimeTrans() { return origCouponDateTimeTrans; }

    public void setOrigCouponDateTimeTrans(long origCouponDateTimeTrans) { this.origCouponDateTimeTrans = origCouponDateTimeTrans; }

    public String getOrigCouponRefNo() { return origCouponRefNo; }

    public void setOrigCouponRefNo(String origCouponRefNo) { this.origCouponRefNo = origCouponRefNo; }

    public long getDateTimeTrans() { return dateTimeTrans; }

    public void setDateTimeTrans(long dateTimeTrans) { this.dateTimeTrans = dateTimeTrans; }

    public Long getOrigDateTimeTrans() { return origDateTimeTrans; }

    public void setOrigDateTimeTrans(Long origDateTimeTrans) { this.origDateTimeTrans = origDateTimeTrans; }

    public int getOrigEnterMode() { return origEnterMode; }

    public void setOrigEnterMode(int origEnterMode) { this.origEnterMode = origEnterMode; }

    public boolean getOrigHasPin() {
        return origHasPin;
    }

    public void setOrigHasPin(boolean origHasPin) {
        this.origHasPin = origHasPin;
    }



    public String getMerName() { return merName; }

    public void setMerName(String merName) {
        this.merName = merName;
    }

    public String getMCC() { return MCC; }

    public void setMCC(String MCC) { this.MCC = MCC; }

    public String getTransState() {
        return transState;
    }

    public void setTransState(String transState) {
        this.transState = transState;
    }

    public boolean getIsUpload() {
        return isUpload;
    }

    public void setIsUpload(boolean isUpload) {
        this.isUpload = isUpload;
    }

    public boolean getIsOffUploadState() {
        return isOffUploadState;
    }

    public void setIsOffUploadState(boolean isOffSend) {
        this.isOffUploadState = isOffSend;
    }

    public int getSendTimes() {
        return sendTimes;
    }

    public void setSendTimes(int sendTimes) {
        this.sendTimes = sendTimes;
    }

    public String getTransType() {
        return transType;
    }

    public ETransType getTransTypeEnum() {
        return ETransType.valueOf(transType);
    }

    public void setTransType(String transType) {
        this.transType = transType;
    }

    public String getOrigTransType() {
        return origTransType;
    }

    public void setOrigTransType(String origTransType) {
        this.origTransType = origTransType;
    }

    public String getProcCode() {
        return procCode;
    }

    public void setProcCode(String procCode) {
        this.procCode = procCode;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getSellPrice() {
        return sellPrice;
    }

    public void setSellPrice(String SellPrice) {
        this.sellPrice = SellPrice;
    }

    public String getTipAmount() {
        return tipAmount;
    }

    public void setTipAmount(String tipAmount) {
        this.tipAmount = tipAmount;
    }

    public String getIssuerResp() {
        return issuerResp;
    }

    public void setIssuerResp(String issuerResp) {
        this.issuerResp = issuerResp;
    }

    public String getCenterResp() {
        return centerResp;
    }

    public void setCenterResp(String centerResp) {
        this.centerResp = centerResp;
    }

    public String getRecvBankResp() {
        return recvBankResp;
    }

    public void setRecvBankResp(String recvBankResp) {
        this.recvBankResp = recvBankResp;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getBalanceFlag() {
        return balanceFlag;
    }

    public void setBalanceFlag(String balanceFlag) {
        this.balanceFlag = balanceFlag;
    }

    public long getTransNo() {
        return transNo;
    }

    public void setTransNo(long transNo) {
        this.transNo = transNo;
    }

    /*public long getDupTransNo() {
        return dupTransNo;
    }

    public void setDupTransNo(long dupTransNo) {
        this.dupTransNo = dupTransNo;
    }*/

    public long getOrigTransNo() {
        return origTransNo;
    }

    public void setOrigTransNo(long origTransNo) {
        this.origTransNo = origTransNo;
    }

    public int getSendFailFlag() {
        return sendFailFlag;
    }

    public void setSendFailFlag(int sendFailFlag) {
        this.sendFailFlag = sendFailFlag;
    }

    public long getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(long batchNo) {
        this.batchNo = batchNo;
    }

    public long getOrigBatchNo() {
        return origBatchNo;
    }

    public void setOrigBatchNo(long origBatchNo) {
        this.origBatchNo = origBatchNo;
    }

    public String getPan() {
        return pan;
    }

    public void setPan(String pan) {
        this.pan = pan;
    }

    public String getTransferPan() {
        return transferPan;
    }

    public void setTransferPan(String transferPan) {
        this.transferPan = transferPan;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        Log.d("teg time :", ""+time);
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getOrigDate() {
        return origDate;
    }

    public void setOrigDate(String origDate) {
        this.origDate = origDate;
    }

    public String getSettleDate() {
        return settleDate;
    }

    public void setSettleDate(String settleDate) {
        this.settleDate = settleDate;
    }

    public String getExpDate() {
        return expDate;
    }

    public void setExpDate(String expDate) {
        this.expDate = expDate;
    }

    public int getEnterMode() {
        return enterMode;
    }

    public void setEnterMode(int enterMode) {
        this.enterMode = enterMode;
    }

    public int getTransferEnterMode() {
        return transferEnterMode;
    }

    public void setTransferEnterMode(int transferEnterMode) {
        this.transferEnterMode = transferEnterMode;
    }

    public String getRefNo() {
        return refNo;
    }

    public void setRefNo(String refNo) {
        this.refNo = refNo;
    }

    public String getOrigRefNo() {
        return origRefNo;
    }

    public void setOrigRefNo(String origRefNo) {
        this.origRefNo = origRefNo;
    }

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public String getOrigAuthCode() {
        return origAuthCode;
    }

    public void setOrigAuthCode(String origAuthCode) {
        this.origAuthCode = origAuthCode;
    }

    public String getIsserCode() {
        return isserCode;
    }

    public void setIsserCode(String isserCode) {
        this.isserCode = isserCode;
    }

    public String getAcqCode() {
        return acqCode;
    }

    public void setAcqCode(String acqCode) {
        this.acqCode = acqCode;
    }

    public String getAcqCenterCode() {
        return acqCenterCode;
    }

    public void setAcqCenterCode(String acqCenterCode) {
        this.acqCenterCode = acqCenterCode;
    }

    public String getInterOrgCode() {
        return interOrgCode;
    }

    public void setInterOrgCode(String interOrgCode) {
        this.interOrgCode = interOrgCode;
    }

    public boolean getHasPin() {
        return hasPin;
    }

    public void setHasPin(boolean hasPin) {
        this.hasPin = hasPin;
    }

    public String getTrack1() {
        return track1;
    }

    public void setTrack1(String track1) {
        this.track1 = track1;
    }

    public String getTrack2() {
        return track2;
    }

    public void setTrack2(String track2) {
        this.track2 = track2;
    }

    public String getTrack3() {
        return track3;
    }

    public void setTrack3(String track3) {
        this.track3 = track3;
    }

    public boolean getIsEncTrack() {
        return isEncTrack;
    }

    public void setIsEncTrack(boolean isEncTrack) {
        this.isEncTrack = isEncTrack;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getAuthInsCode() {
        return authInsCode;
    }

    public void setAuthInsCode(String authInsCode) {
        this.authInsCode = authInsCode;
    }

    public byte getEmvResult() {
        return emvResult;
    }

    public void setEmvResult(byte emvResult) {
        this.emvResult = emvResult;
    }

    public String getCardSerialNo() {
        return cardSerialNo;
    }

    public void setCardSerialNo(String cardSerialNo) {
        this.cardSerialNo = cardSerialNo;
    }

    public String getSendIccData() {
        return sendIccData;
    }

    public void setSendIccData(String sendIccData) {
        this.sendIccData = sendIccData;
    }

    public String getDupIccData() {
        return dupIccData;
    }

    public void setDupIccData(String dupIccData) {
        this.dupIccData = dupIccData;
    }

    public String getTc() {
        return tc;
    }

    public void setTc(String tc) {
        this.tc = tc;
    }

    public String getArqc() {
        return arqc;
    }

    public void setArqc(String arqc) {
        this.arqc = arqc;
    }

    public String getArpc() {
        return arpc;
    }

    public void setArpc(String arpc) {
        this.arpc = arpc;
    }

    public String getTvr() {
        return tvr;
    }

    public void setTvr(String tvr) {
        this.tvr = tvr;
    }

    public String getAid() {
        return aid;
    }

    public void setAid(String aid) {
        this.aid = aid;
    }

    public String getEmvAppLabel() {
        return emvAppLabel;
    }

    public void setEmvAppLabel(String emvAppLabel) {
        this.emvAppLabel = emvAppLabel;
    }

    public String getEmvAppName() {
        return emvAppName;
    }

    public void setEmvAppName(String emvAppName) {
        this.emvAppName = emvAppName;
    }

    public String getTsi() {
        return tsi;
    }

    public void setTsi(String tsi) {
        this.tsi = tsi;
    }

    public String getAtc() {
        return atc;
    }

    public void setAtc(String atc) {
        this.atc = atc;
    }

    public String getMsgID() {
        return msgID;
    }

    public void setMsgID(String msgID) {
        this.msgID = msgID;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getSrcInfo() {
        return srcInfo;
    }

    public void setSrcInfo(String srcInfo) {
        this.srcInfo = srcInfo;
    }

    public int getTipSupport() { return tipSupport; }
    public void setTipSupport(int tipSupport) { this.tipSupport = tipSupport; }

    public String getOper() {
        return oper;
    }

    public void setOper(String oper) {
        this.oper = oper;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseMsg() {
        return responseMsg;
    }

    public void setResponseMsg(String responseMsg) {
        this.responseMsg = responseMsg;
    }

    public String getTermID() {
        return termID;
    }

    public void setTermID(String termID) {
        this.termID = termID;
    }

    public String getMerchID() {
        return merchID;
    }

    public void setMerchID(String merchID) {
        this.merchID = merchID;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getTpdu() {
        return tpdu;
    }

    public void setTpdu(String tpdu) {
        this.tpdu = tpdu;
    }


    public String getField47() {
        return field47;
    }

    public void setField47(String field47) {
        this.field47 = field47;
    }

    public String getField48() {
        return field48;
    }

    public void setField48(String field48) {
        this.field48 = field48;
    }

    public void setField54(String field54) {
        this.field54 = field54;
    }

    public String getField54() {
        return field54;
    }

    public void setField59(String field59) {
        this.field59 = field59;
    }

    public String getField59() {
        return field59;
    }

    public String getField60() {
        return field60;
    }

    public void setField60(String field60) {
        this.field60 = field60;
    }

    public String getField61() {
        return field61;
    }

    public void setField61(String field61) {
        this.field61 = field61;
    }

    public String getField62() {
        return field62;
    }

    public void setField62(String field62) {
        this.field62 = field62;
    }

    public String getField63() {
        return field63;
    }

    public void setField63(String field63) {
        this.field63 = field63;
    }

    public String getField110() {
        return field110;
    }

    public void setField110(String field110) {
        this.field110 = field110;
    }

    public String getField120() {
        return field120;
    }

    public void setField120(String field120) {
        this.field120 = field120;
    }

    public String getField103() {
        return field103;
    }

    public void setField103(String field103) {
        this.field103 = field103;
    }

    public String getDestBank() {
        return destBank;
    }

    public void setDestBank(String destBank) {
        this.destBank = destBank;
    }

    public String getRefferenceNo() {
        return refferenceNo;
    }

    public void setRefferenceNo(String refferenceNo) {
        this.refferenceNo = refferenceNo;
    }

    public String getBillingId() {
        return billingId;
    }

    public void setBillingId(String billingId) {
        this.billingId = billingId;
    }

    public String getNtb() {
        return ntb;
    }

    public void setNtb(String ntb) {
        this.ntb = ntb;
    }

    public String getReprintData() {
        return reprintData;
    }

    public void setReprintData(String reprintData) {
        this.reprintData = reprintData;
    }

    public String getDupf48() {
        return dupf48;
    }

    public void setDupf48(String dupf48) {
        this.dupf48 = dupf48;
    }

    public String getPrintTimeout() {
        return printTimeout;
    }

    public void setPrintTimeout(String printTimeout) {
        this.printTimeout = printTimeout;
    }

    public String getField127() {
        return field127;
    }


    public void setField127(String field127) {
        this.field127 = field127;
    }

    public String getField28() {
        return field28;
    }

    public void setField28(String field28) {
        this.field28 = field28;
    }

    public String getField36() {
        return field36;
    }

    public void setField36(String field36) {
        this.field36 = field36;
    }

    public String getAccType() {
        return accType;
    }

    public void setAccType(String AccType) {
        this.accType = AccType;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String Phone) {
        this.phoneNo = Phone;
    }

    public String getFeeTrx() {
        return feeTrx;
    }

    public void setFeeTrx(String FeeTrx) {
        this.feeTrx = FeeTrx;
    }

    public String getSamsatKodeBayar() {
        return samsatKodeBayar;
    }

    public void setSamsatKodeBayar(String samsatKodeBayar) {
        this.samsatKodeBayar = samsatKodeBayar;
    }

    public String getSamsatMerchantKode() {
        return samsatMerchantKode;
    }

    public void setSamsatMerchantKode(String samsatMerchantKode) {
        this.samsatMerchantKode = samsatMerchantKode;
    }

    public String getProduct_code() {
        return product_code;
    }

    public void setProduct_code(String Product_code) {
        this.product_code = Product_code;
    }

    public String getTypeProduct() {
        return type_product;
    }

    public void setTypeProduct(String TypeProduct) {
        this.type_product = TypeProduct;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String Operator) {
        this.operator = Operator;
    }

    public String getProduct_name() {
        return product_name;
    }

    public void setProduct_name(String product_name) {
        this.product_name = product_name;
    }

    public String getKeterangan() {
        return keterangan;
    }

    public void setKeterangan(String Keterangan) {
        this.keterangan = Keterangan;
    }

    public String getAccNo() {
        return accNo;
    }

    /*public void setF48bukaRek(String s) {
        this.f48bukaRek = s;
    }

    public String getF48bukaRek() {
        return f48bukaRek;
    }*/

    public void setAccNo(String AccNo) {
        this.accNo = AccNo;
    }

    public String getReserved() {
        return reserved;
    }

    public void setReserved(String reserved) {
        this.reserved = reserved;
    }

    public boolean getPinFree() {
        return pinFree;
    }

    public void setPinFree(boolean pinFree) {
        this.pinFree = pinFree;
    }

    public boolean getSignFree() {
        return signFree;
    }

    public void setSignFree(boolean signFree) {
        this.signFree = signFree;
    }

    public boolean getIsCDCVM() {
        return isCDCVM;
    }

    public void setIsCDCVM(boolean isCDCVM) {
        this.isCDCVM = isCDCVM;
    }

    public byte[] getSignData() {
        return signData;
    }

    public void setSignData(byte[] signData) {
        this.signData = signData;
    }

    public boolean getIsReversal() {
        return isReversal;
    }

    public void setIsReversal(boolean isReversal) {
        this.isReversal = isReversal;
    }

    public String getRecvIccData() {
        return recvIccData;
    }

    public void setRecvIccData(String recvIccData) {
        this.recvIccData = recvIccData;
    }

    public String getScriptData() {
        return scriptData;
    }

    public void setScriptData(String scriptData) {
        this.scriptData = scriptData;
    }

    public boolean getIsOnlineTrans() {
        return isOnlineTrans;
    }

    public void setIsOnlineTrans(boolean isOnlineTrans) {
        this.isOnlineTrans = isOnlineTrans;
    }

    public boolean getIsSM() {
        return isSM;
    }

    public void setIsSM(boolean isSM) {
        this.isSM = isSM;
    }

    public String getCouponNo() {
        return couponNo;
    }

    public void setCouponNo(String couponNo) {
        this.couponNo = couponNo;
    }

    public String getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(String discountAmount) {
        this.discountAmount = discountAmount;
    }

    public String getActualPayAmount() {
        return actualPayAmount;
    }

    public void setActualPayAmount(String actualPayAmount) {
        this.actualPayAmount = actualPayAmount;
    }

    public String getForeignAmount() {
        return foreignAmount;
    }

    public void setForeignAmount(String foreignAmount) {
        this.foreignAmount = foreignAmount;
    }

    public TransData clone() {
        TransData obj = null;
        try {
            obj = (TransData) super.clone();
        } catch (CloneNotSupportedException e) {
            Log.e(TAG, "TransData clone error", e);
        }
        return obj;
    }

    public String getOrigTermID() {
        return origTermID;
    }

    public void setOrigTermID(String origTermID) {
        this.origTermID = origTermID;
    }

    public String getReceiptElements() {
        return receiptElements;
    }

    public void setReceiptElements(String receiptElements) {
        this.receiptElements = receiptElements;
    }

    public int getSignSendState() {
        return signSendState;
    }

    public void setSignSendState(int signSendState) {
        this.signSendState = signSendState;
    }

    public boolean getSignUpload() {
        return signUpload;
    }

    public void setSignUpload(boolean signUpload) {
        this.signUpload = signUpload;
    }

    public String getAuthMode() {
        return authMode;
    }

    public void setAuthMode(String authMode) {
        this.authMode = authMode;
    }

    public String getField3() {
        return field3;
    }

    public void setField3(String field3) {
        this.field3 = field3;
    }

    public void setIsAdjustAfterUpload(boolean isAdjustAfterUpload) {
        this.isAdjustAfterUpload = isAdjustAfterUpload;
    }

    public boolean getIsAdjustAfterUpload() {
        return isAdjustAfterUpload;
    }

    public String getC2b() {
        return c2b;
    }

    public void setC2b(String c2b) {
        this.c2b = c2b;
    }

    public String getC2bVoucher() {
        return c2bVoucher;
    }

    public void setC2bVoucher(String c2bVoucher) {
        this.c2bVoucher = c2bVoucher;
    }

    public String getOrigC2bVoucher() {
        return origC2bVoucher;
    }

    public void setOrigC2bVoucher(String origC2bVoucher) {
        this.origC2bVoucher = origC2bVoucher;
    }

    public void setInstalNum(String instalNum) {
        this.instalNum = instalNum;
    }

    public String getInstalNum() {
        return instalNum;
    }

    public void setPrjCode(String prjCode) {
        this.prjCode = prjCode;
    }

    public String getPrjCode() {
        return prjCode;
    }

    public String getFirstAmount() {
        return firstAmount;
    }
    public void setFirstAmount(String firstAmount) {
        this.firstAmount = firstAmount;
    }

    public String getFeeTotalAmount() {
        return feeTotalAmount;
    }
    public void setFeeTotalAmount(String feeTotalAmount) {
        this.feeTotalAmount = feeTotalAmount;
    }

    public String getInstalCurrCode() {
        return instalCurrCode;
    }
    public void setInstalCurrCode(String instalCurrCode) {
        this.instalCurrCode = instalCurrCode;
    }
//    private String cFeeType;              	//分期付款的手续费方式'0'-一次性 '1'-分期  L=1
//    private String szFirstFee;          	//分期付款的首付手续费,(BCD)  L=12
//    private String szFee;               	//分期付款的每期手续费,(BCD)  L=12
//    private String szPoint;          		//分期付款的奖励积分,(BCD)   L=12

    public String getCardCVN2() {
        return cardCVN2;
    }

    public void setCardCVN2(String cardCVN2) {
        this.cardCVN2 = cardCVN2;
    }

    public String getCardCurrencyCode() {
        return cardCurrencyCode;
    }

    public void setCardCurrencyCode(String cardCurrencyCode) {
        this.cardCurrencyCode = cardCurrencyCode;
    }

    public String getTerminalCurrencyCode() {
        return terminalCurrencyCode;
    }

    public void setTerminalCurrencyCode(String terminalCurrencyCode) {
        this.terminalCurrencyCode = terminalCurrencyCode;
    }

    public String getTerminalCurrencyName() {
        return terminalCurrencyName;
    }

    public void setTerminalCurrencyName(String terminalCurrencyName) {
        this.terminalCurrencyName = terminalCurrencyName;
    }

    public String getTermianlCurrencyDecimals() {
        return termianlCurrencyDecimals;
    }

    public void setTermianlCurrencyDecimals(String termianlCurrencyDecimals) {
        this.termianlCurrencyDecimals = termianlCurrencyDecimals;
    }

    public String getCurrencyRate() {
        return currencyRate;
    }

    public void setCurrencyRate(String currencyRate) {
        this.currencyRate = currencyRate;
    }

    public String getField107() {
        return field107;
    }
    public void setField107(String field107) {
        this.field107 = field107;
    }

    public String getField102() {
        return field102;
    }
    public void setField102(String field102) {
        this.field102 = field102;
    }

    /**
     * @author Jerry 20170608
     */
    public boolean getIsFallback() {
        return isFallback;
    }
    public void setIsFallback(boolean isFallback) {
        this.isFallback = isFallback;
    }

    /******************************** 数据库信息定义 ******************************/
    /**
     * 交易记录数据库信息, 当表结构有变化时， {@link DbInfo#VER} 加1
     *
     * @author Steven.W
     *
     */
    class DbInfo {
        /**
         * 版本号
         */
        public static final int VER = 3;
        /**
         * 交易记录数据库名称
         */
        public static final String DB_NAME = "transRecord.db";

        /**
         * 交易记录表名
         */
        public static final String TABLE_NAME_TRANS = "trans";
        /**
         * 冲正表名
         */
        public static final String TABLE_NAME_DUP = "dup";
        /**
         * 脚本结果表名
         */
        public static final String TABLE_NAME_SCRIPT = "script";

    }

    // ********************************获取数据句柄********************************/

    /**
     * 获取交易数据库句柄
     *
     * @return
     * @throws DbException
     */
    private static IDao<TransData> getTransDao() throws DbException {
        IDao<TransData> dao = FinancialApplication.getDb().getDb(DbInfo.VER, DbInfo.DB_NAME, DbInfo.TABLE_NAME_TRANS,
                TransData.class, new IDbListener<TransData>() {

                    @Override
                    public IDao<TransData> onUpdate(IDao<TransData> arg0, int arg1, int arg2) {
                        try {
                            arg0.dropTable(); //删除数据表
                            getDupDao().dropTable();
                            getScriptDao().dropTable();
                            return FinancialApplication.getDb().getDb(DbInfo.VER, DbInfo.DB_NAME, DbInfo.TABLE_NAME_TRANS,
                                    TransData.class, null);
                        } catch (DbException e) {
                            Log.e(TAG, "", e);
                        }
                        return null;

                    }
                });

        return dao;
    }

    /**
     * 获取冲正数据库句柄
     *
     * @return
     * @throws DbException
     */
    private static IDao<TransData> getDupDao() throws DbException {
        IDao<TransData> dao = FinancialApplication.getDb().getDb(DbInfo.VER, DbInfo.DB_NAME, DbInfo.TABLE_NAME_DUP,
                TransData.class, new IDbListener<TransData>() {

                    @Override
                    public IDao<TransData> onUpdate(IDao<TransData> arg0, int arg1, int arg2) {
                        try {
                            arg0.dropTable();
                            getTransDao().dropTable();
                            getScriptDao().dropTable();
                            return FinancialApplication.getDb().getDb(DbInfo.VER, DbInfo.DB_NAME, DbInfo.TABLE_NAME_DUP,
                                    TransData.class, null);
                        } catch (DbException e) {

                            Log.e(TAG, "", e);
                        }
                        return null;

                    }
                });

        return dao;
    }

    /**
     * 获取脚本数据库句柄
     *
     * @return
     * @throws DbException
     */
    private static IDao<TransData> getScriptDao() throws DbException {
        IDao<TransData> dao = FinancialApplication.getDb().getDb(DbInfo.VER, DbInfo.DB_NAME, DbInfo.TABLE_NAME_SCRIPT,
                TransData.class, new IDbListener<TransData>() {

                    @Override
                    public IDao<TransData> onUpdate(IDao<TransData> arg0, int arg1, int arg2) {
                        try {
                            arg0.dropTable();
                            getDupDao().dropTable();
                            getTransDao().dropTable();
                            return FinancialApplication.getDb().getDb(DbInfo.VER, DbInfo.DB_NAME, DbInfo.TABLE_NAME_SCRIPT,
                                    TransData.class, null);
                        } catch (DbException e) {

                            Log.e(TAG, "", e);
                        }
                        return null;

                    }
                });

        return dao;
    }






    /******************************** 交易数据库操作 ********************************/

    /**
     * 写交易记录
     *
     * @param
     * @return
     */
    public boolean saveTrans() {
        IDao<TransData> dao = null;
        try {
            dao = getTransDao();
            dao.beginTransaction();
            try { //没有冲正交易时，做离线会抛异常，导致数据库没有关闭，下次操作会出问题
                dao.switchToTable(DbInfo.TABLE_NAME_DUP);
                if (isOnlineTrans) {
                    dao.deleteAll();
                }
            } catch (Exception e) {
                Log.e(TAG, "", e);
            }
            dao.switchToTable(DbInfo.TABLE_NAME_TRANS);
            dao.save(this);
            dao.endTransaction();
        } catch (DbException e) {
            Log.e(TAG, "", e);
            return false;
        } finally {
            if (dao != null) {
                try {
                    dao.switchToTable(DbInfo.TABLE_NAME_TRANS);
                } catch (DbException e) {
                    Log.e(TAG, "", e);
                }
            }

        }
        return true;
    }

    /**
     * 读指定交易记录
     *
     * @return
     */
    public static TransData readTrans(long transNo) {
        try {
            IDao<TransData> dao = getTransDao();
            String sql = String.format("transNo = %d", transNo);
            List<TransData> list = dao.findByCondition(sql);

            if ((list != null) && (list.size() == 1)) {
                return list.get(0);
            }
        } catch (DbException e) {
            Log.e(TAG, "", e);
        }

        return null;
    }





    /**
     * 读扫码指定交易记录
     *
     * @return
     */
    public static TransData readTransByVoucher(String c2bVoucher) {
        try {
            IDao<TransData> dao = getTransDao();
            String sql = "select * from trans where ";
            sql += "c2bVoucher = '" + c2bVoucher + "'";
            //List<TransData> list = dao.findByCondition(sqls);
            List<TransData> list = readTransByCondition(dao, sql);
            if ((list != null) && (list.size() == 1)) {
                return list.get(0);
            }
        } catch (DbException e) {
            Log.e(TAG, "", e);
        }

        return null;
    }

    /**
     * 读所有交易记录
     *
     * @return
     */
    public static List<TransData> readAllTrans() {
//        try {
//            IDao<TransData> dao = getTransDao();
//            List<TransData> data = dao.findAll();
//            return data;
//        } catch (DbException e) {
//            Log.e(TAG, "", e);
//        }

        return getTradeRecordList();
    }

    /**
     * 读最后一笔联机（电子签名未上送）的交易记录
     * add by 170420
     * @return
     */
    public static TransData readLastOnlineNoSendTrans(long currTransNo) {
        try {
            IDao<TransData> dao = getTransDao();
            String sql = "select * from trans where ";
            sql += "signData is not null and signData !=''  and isOnlineTrans = 1 and signSendState="
                    + SignSendStatus.SEND_SIG_NO + " and transNo !=" + currTransNo + " order by transNo desc limit 1";
            //List<TransData> list = dao.findByCondition(sql);
            List<TransData> list = readTransByCondition(dao, sql);
            if (!CollectionUtils.isEmpty(list)) {
                return list.get(0);
            }
        } catch (DbException e) {
            Log.e(TAG, "", e);
        }
        return null;
    }

    /**
     * 读离线未上送的电子签名
     * add by 170420
     * @return
     */
    public static List<TransData> readOfflineNoSendTrans() {
        try {
            IDao<TransData> dao = getTransDao();
            String sql = "select * from trans where ";
            sql += "signData is not null and signData !=''  and isOnlineTrans = 0 and signSendState="
                    + SignSendStatus.SEND_SIG_NO;
            List<TransData> list = readTransByCondition(dao, sql);
            //List<TransData> list = dao.findByCondition(sql);
            return list;
        } catch (DbException e) {
            Log.e(TAG, "", e);
        }
        return null;
    }

    /**
     * 读最后一笔交易记录
     *
     * @return
     */
    public static TransData readLastTrans() {
//        try {
//            IDao<TransData> dao = getTransDao();
//            TransData data = dao.findLast();
//            return data;
//        } catch (DbException e) {
//            Log.e(TAG, "", e);
//        }
        try {
            IDao<TransData> dao = getTransDao();
            String sql = SQL_READ_LASTTRANS;
            return readOneTrans(dao, sql);
        } catch (DbException e) {
            Log.e(TAG, "", e);
        }

        return null;
    }

    /**
     * 更新交易记录
     *
     * @param
     * @return
     */
    public boolean updateTrans() {
        long transNo = getTransNo();
        try {
            IDao<TransData> dao = getTransDao();
            String sql = String.format("transNo = %d", transNo);
            List<TransData> list = dao.findByCondition(sql);
            if ((list != null) && (list.size() == 1)) {
                setId(list.get(0).getId());
            }
            dao.update(this);
            return true;
        } catch (DbException e) {
            Log.e(TAG, "", e);
        }

        return false;
    }

    /**
     * Modified by Steven.T 2017-6-7 16:08:31
     * 读指定交易类型的交易记录
     * @return
     */
    public static List<TransData> readTrans(List<ETransType> types, String addSql) {
        try {
            IDao<TransData> dao = getTransDao();
            String sql = "select * from trans where (";
            boolean flag = false;
            for (ETransType type : types) {
                if (flag) {
                    sql += String.format(" or " + "transType = '%s'", type.toString());
                } else {
                    sql += String.format("transType = '%s'", type.toString());
                    flag = true;
                }
            }
            sql += ") and transState != 'VOID'";
            /**添加*/
            if (addSql != null) {
                sql = sql + " " + addSql;
            }

            //List<TransData> list = dao.findByCondition(sql);
            List<TransData> resultsList = new ArrayList<>();
            List<TransData> list = readTransByCondition(dao, sql);
            if (!CollectionUtils.isEmpty(list)) {
                for (TransData transData : list) {
                    if (transData.getTransType().equals(ETransType.SALE.toString())
                            && transData.getTransState().equals(ETransStatus.ADJUST.toString())) {
                        continue;
                    }
                    if (transData.getTransType().equals(ETransType.QR_SALE.toString())
                            && transData.getTransState().equals(ETransStatus.ADJUST.toString())) {
                        continue;
                    }
                    resultsList.add(transData);
                }
                return resultsList;
            }
        } catch (DbException e) {
            Log.e(TAG, "", e);
        }

        return null;
    }

    /**
     * 读指定交易类型的交易记录
     *
     * @return
     */
    public static List<TransData> readTrans(List<ETransType> types) {
        return readTrans(types, null);
    }
    /**
     * 获取内卡借记笔数和借记总金额
     *
     * 用于结算请求或者交易汇总
     *
     * @return obj[0] 笔数 obj[1] 金额
     */
    public static long[] getRmbDebitNumAndAmount() {
        SQLiteDatabase dataBase = null;
        Cursor cursor = null;
        long[] obj = new long[2];
        long[] tmpObj = new long[2];
        try {
            IDao<TransData> dao = getTransDao();

            String sql = "select count(id),sum(amount) from "
                    + DbInfo.TABLE_NAME_TRANS
                    + " where ((transType in (?,?,?,?,?,?,?,?,?,?,?,?,?,?) and transState <>?) or (transType =? and isAdjustAfterUpload =?)) and (interOrgCode is null or interOrgCode =?)";

            dataBase = dao.getDb();
            cursor = dataBase.rawQuery(sql, new String[] { "SALE", "QR_SALE","DANA_QR_SALE", "QR_INQUIRY", "AUTHCM", "AUTH_SETTLEMENT", "EC_SALE",
                    "SETTLE_ADJUST", "SETTLE_ADJUST_TIP", "EC_TRANSFER_LOAD","MOTO_SALE", "MOTO_AUTHCM", "MOTO_AUTH_SETTLEMENT","RECURRING_SALE",
                    "VOID", "OFFLINE_SETTLE", "0", "CUP" });

            while (cursor.moveToNext()) {
                obj[0] = cursor.getLong(0);
                obj[1] = Long.parseLong(cursor.getString(1) != null ? cursor.getString(1) : "0");
            }

            //Sandy : SUM the COUPON SALE
            sql = "select count(id),sum(actualPayAmount) from "
                    + DbInfo.TABLE_NAME_TRANS
                    + " where ((transType in (?) and transState <>?) or (transType =? and isAdjustAfterUpload =?)) and (interOrgCode is null or interOrgCode =?)";
            dataBase = dao.getDb();
            cursor = dataBase.rawQuery(sql, new String[] { "COUPON_SALE" , "VOID", "OFFLINE_SETTLE", "0", "CUP" });

            while (cursor.moveToNext()) {
                tmpObj[0] = cursor.getLong(0);
                tmpObj[1] = Long.parseLong(cursor.getString(1) != null ? cursor.getString(1) : "0");
            }

            obj[0] = obj[0] + tmpObj[0];
            obj[1] = obj[1] + tmpObj[1];
            //cursor.close();
            return obj;

        } catch (DbException e) {
            Log.e(TAG, "", e);
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (dataBase != null) {
                dataBase.close();
            }
        }

    }

    /**
     * 获取内卡贷记总笔数和贷记总金额
     *
     * @return obj[0] 笔数 obj[1] 金额
     */

    public static long[] getRmbCreditNumAndAmount() {
        SQLiteDatabase dataBase = null;
        Cursor cursor = null;
        long[] obj = new long[2];
        long[] tmpObj = new long[2];
        try {
            IDao<TransData> dao = getTransDao();
            String sql = "select count(id),sum(amount) from " + DbInfo.TABLE_NAME_TRANS
                    + " where transType in (?,?,?,?,?,?,?,?,?,?) and  (interOrgCode is null or interOrgCode =?)";
            dataBase = dao.getDb();
            cursor = dataBase.rawQuery(sql, new String[] { "VOID","DANA_QR_VOID","QR_VOID", "REFUND", "QR_REFUND", "AUTHCMVOID",
                    "MOTO_VOID", "MOTO_REFUND", "MOTO_AUTHCMVOID","RECURRING_VOID","CUP" });
            while (cursor.moveToNext()) {
                obj[0] = cursor.getLong(0);
                obj[1] = Long.parseLong(cursor.getString(1) != null ? cursor.getString(1) : "0");
            }

            //Sandy : SUM the COUPON SALE VOID
            sql = "select count(id),sum(actualPayAmount) from " + DbInfo.TABLE_NAME_TRANS
                    + " where transType=? AND transState=? and  (interOrgCode is null or interOrgCode =?)";
            dataBase = dao.getDb();
            cursor = dataBase.rawQuery(sql, new String[] { "COUPON_SALE", "VOID" ,"CUP" });

            while (cursor.moveToNext()) {
                tmpObj[0] = cursor.getLong(0);
                tmpObj[1] = Long.parseLong(cursor.getString(1) != null ? cursor.getString(1) : "0");
            }

            obj[0] = obj[0] + tmpObj[0];
            obj[1] = obj[1] + tmpObj[1];

            return obj;

        } catch (DbException e) {
            Log.e(TAG, "", e);
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (dataBase != null) {
                dataBase.close();
            }
        }

    }

    /**
     * 根据交易类型计算总计(交易状态是normal的)
     *
     * 用于打单
     *
     * @param transType
     *            :交易类型
     * @param isCup
     *            ：是否是内卡
     * @return obj[0] 笔数 obj[1] 金额
     */
    public static long[] getTransNumAndAmount(String transType, ETransStatus status, boolean isCup) {
        SQLiteDatabase dataBase = null;
        Cursor cursor = null;
        long[] obj = new long[2];
        try {
            IDao<TransData> dao = getTransDao();
            StringBuilder sql = new StringBuilder();
            sql.append("select count(id),");
            //sandy
            if(transType.equals(ETransType.COUPON_SALE.toString()) )
                sql.append("sum(actualPayAmount) ");
            else
                sql.append("sum(amount) ");

            sql.append("from " + DbInfo.TABLE_NAME_TRANS + " ");
            sql.append("where (transType =? and transState =?) and isAdjustAfterUpload=?");

            if (isCup)
                sql.append(" and (interOrgCode is null or interOrgCode =?)");
            else {
                sql.append(" and (interOrgCode is not null and interOrgCode <>?)");
            }

            dataBase = dao.getDb();
            cursor = dataBase.rawQuery(sql.toString(), new String[] { transType, status.toString(), "0", "CUP" });

            while (cursor.moveToNext()) {
                obj[0] = cursor.getLong(0);
                obj[1] = Long.parseLong(cursor.getString(1) != null ? cursor.getString(1) : "0");
            }

            return obj;

        } catch (DbException e) {
            Log.e(TAG, "", e);
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }

            if (dataBase != null) {
                dataBase.close();
            }
        }

    }

    public static long[] getTransNumAndAmountPulsa(String transType, ETransStatus status, boolean isCup) {
        SQLiteDatabase dataBase = null;
        Cursor cursor = null;
        long[] obj = new long[2];
        try {
            IDao<TransData> dao = getTransDao();
            StringBuilder sql = new StringBuilder();
            sql.append("select count(id),");
            //sandy
            if(transType.equals(ETransType.COUPON_SALE.toString()) )
                sql.append("sum(actualPayAmount) ");
            else
                sql.append("sum(sellPrice) ");

            sql.append("from " + DbInfo.TABLE_NAME_TRANS + " ");
            sql.append("where (transType =? and transState =?) and isAdjustAfterUpload=?");

            if (isCup)
                sql.append(" and (interOrgCode is null or interOrgCode =?)");
            else {
                sql.append(" and (interOrgCode is not null and interOrgCode <>?)");
            }

            dataBase = dao.getDb();
            cursor = dataBase.rawQuery(sql.toString(), new String[] { transType, status.toString(), "0", "CUP" });

            while (cursor.moveToNext()) {
                obj[0] = cursor.getLong(0);
                obj[1] = Long.parseLong(cursor.getString(1) != null ? cursor.getString(1) : "0");
            }

            return obj;

        } catch (DbException e) {
            Log.e(TAG, "", e);
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }

            if (dataBase != null) {
                dataBase.close();
            }
        }

    }

    /**
     *
     * 获取外卡借记笔数和借记总金额 <br>
     * 用于结算请求或者交易汇总
     *
     * @return obj[0] 笔数 obj[1] 金额
     */

    public static long[] getFrnDebitNumAndAmount() {
        SQLiteDatabase dataBase = null;
        Cursor cursor = null;
        long[] obj = new long[2];
        try {
            IDao<TransData> dao = getTransDao();
            String sql = "select count(id),sum(amount) from "
                    + DbInfo.TABLE_NAME_TRANS
                    + " where ((transType in (?,?,?,?,?,?,?,?,?,?,?,?)  and transState <>?) or (transType =? and isAdjustAfterUpload =?)) and (interOrgCode is not null and interOrgCode <>?)";
            dataBase = dao.getDb();
            cursor = dataBase.rawQuery(sql, new String[] { "SALE", "QR_SALE", "AUTHCM", "AUTH_SETTLEMENT", "EC_SALE",
                    "SETTLE_ADJUST", "SETTLE_ADJUST_TIP", "EC_TRANSFER_LOAD", "MOTO_SALE", "MOTO_AUTHCM", "MOTO_AUTH_SETTLEMENT","RECURRING_SALE",
                    "ADJUST", "OFFLINE_SETTLE", "0", "CUP" });
            while (cursor.moveToNext()) {
                obj[0] = cursor.getLong(0);
                obj[1] = Long.parseLong(cursor.getString(1) != null ? cursor.getString(1) : "0");
            }
            return obj;

        } catch (DbException e) {
            Log.e(TAG, "", e);
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (dataBase != null) {
                dataBase.close();
            }
        }

    }

    /**
     * 获取外卡贷记总笔数和贷记总金额
     *
     * @return obj[0] 笔数 obj[1] 金额
     */
    public static long[] getFrnCreditNumAndAmount() {
        SQLiteDatabase dataBase = null;
        Cursor cursor = null;
        long[] obj = new long[2];
        try {
            IDao<TransData> dao = getTransDao();
            String sql = "select count(id),sum(amount) from " + DbInfo.TABLE_NAME_TRANS
                    + " where transType in (?,?,?,?,?,?,?,?,?) and (interOrgCode is not null and interOrgCode <>?)";

            dataBase = dao.getDb();
            cursor = dataBase.rawQuery(sql, new String[] { "VOID", "QR_VOID", "REFUND", "QR_REFUND", "AUTHCMVOID",
                    "MOTO_VOID", "MOTO_REFUND", "MOTO_AUTHCMVOID", "RECURRING_VOID", "CUP" });
            while (cursor.moveToNext()) {
                obj[0] = cursor.getLong(0);
                obj[1] = Long.parseLong(cursor.getString(1) != null ? cursor.getString(1) : "0");
            }
            return obj;

        } catch (DbException e) {
            Log.e(TAG, "", e);
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (dataBase != null) {
                dataBase.close();
            }
        }
    }

    /**
     * Get total tip num and amount
     * @author richard, 20170425
     * @return obj[0] num obj[1] amount
     */
    public static long[] getTipNumAndAmount(boolean isCup) {
        SQLiteDatabase dataBase = null;
        Cursor cursor = null;
        long[] obj = new long[2];
        try {
            IDao<TransData> dao = getTransDao();
            String sql = "select count(id),sum(tipAmount) from " + DbInfo.TABLE_NAME_TRANS
                    + " where (transType in (?,?,?)) and (transState = ?) and (tipAmount <> 0)";

            if (isCup)
                sql += " and interOrgCode =?";
            else {
                sql += " and (interOrgCode isnull or interOrgCode <>?)";
            }

            dataBase = dao.getDb();
            cursor = dataBase.rawQuery(sql, new String[] { ETransType.SALE.toString(), ETransType
                    .QR_SALE.toString(), ETransType.SETTLE_ADJUST_TIP.toString(),
                    ETransStatus.NORMAL.toString(), "CUP"});
            while (cursor.moveToNext()) {
                obj[0] = cursor.getLong(0);
                obj[1] = Long.parseLong(cursor.getString(1) != null ? cursor.getString(1) : "0");
            }
            return obj;
        } catch (DbException e) {
            Log.e(TAG, "", e);
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (dataBase != null) {
                dataBase.close();
            }
        }
    }


    /**
     * 获取交易总笔数
     *
     * @return
     */
    public static long getTransCount() {
        try {
            IDao<TransData> dao = getTransDao();
            return dao.getCount();
        } catch (DbException e) {
            Log.e(TAG, "", e);
        }
        return 0;
    }

    /**
     * 删除所有交易记录
     *
     */
    public static boolean deleteAllTrans() {
        try {
            IDao<TransData> dao = getTransDao();
            dao.deleteAll();
            return true;
        } catch (DbException e) {
            Log.e(TAG, "", e);
        }
        return false;
    }

    /************************************** 冲正数据库操作 ******************************************************/

    /**
     * 写冲正记录
     *
     */
    public boolean saveDup() {
        try {
            IDao<TransData> dao = getDupDao();
            dao.save(this);
            return true;
        } catch (DbException e) {

            Log.e(TAG, "", e);
        }

        return false;
    }

    /**
     * 读冲正记录
     *
     * @return
     */
    public static TransData readDupRecord() {
        try {
            IDao<TransData> dao = getDupDao();
            return dao.findLast();
        } catch (DbException e) {
            Log.e(TAG, "Read dup error.", e);
        }

        return null;
    }

    /**
     * 更新冲正记录
     *
     * @param transData
     * @return
     */
    public static boolean updateDupRecord(TransData transData) {
        try {
            IDao<TransData> dao = getDupDao();
            dao.update(transData);
            return true;
        } catch (DbException e) {
            Log.e(TAG, "", e);
        }

        return false;
    }

    /**
     * 更新冲正原因
     *
     * @param reason
     *
     */
    public static void updateDupReason(String reason) {
        TransData dupRecord = readDupRecord();
        if (dupRecord == null) {
            return;
        }
        dupRecord.setReason(reason);
        updateDupRecord(dupRecord);
    }

    /**
     * 更新冲正记录的原交易时间
     *
     * @param date
     *
     */
    public static void updateDupDate(String date) {
        TransData dupRecord = readDupRecord();
        if (dupRecord == null) {
            return;
        }
        dupRecord.setOrigDate(date);
        updateDupRecord(dupRecord);
    }

    /**
     * 平台批准卡片拒绝更新55
     *
     * @param f55
     */
    public static void updateDupF55(String f55) {
        TransData dupRecord = readDupRecord();
        if (dupRecord == null) {
            return;
        }
        dupRecord.setDupIccData(f55); // 在组冲正报文的55域所用的数据是DupIccData.
        updateDupRecord(dupRecord);
    }

    /**
     * 删除交易记录
     *
     */
    public static boolean deleteDupRecord() {
        try {
            IDao<TransData> dao = getDupDao();
            dao.deleteAll();
            return true;
        } catch (DbException e) {
            Log.e(TAG, "", e);
        }
        return false;
    }

    /************************************** 脚本结果数据 *************************************/
    /**
     * 写脚本结果记录
     *
     * @param
     * @return
     */
    public boolean saveScript() {
        try {
            IDao<TransData> dao = getScriptDao();
            dao.save(this);
            return true;
        } catch (DbException e) {
            Log.e(TAG, "", e);
        }

        return false;
    }

    /**
     * 读脚本结果记录
     *
     * @return
     */
    public static TransData readScript() {
//        try {
//            IDao<TransData> dao = getScriptDao();
//            return dao.findLast();
//        } catch (DbException e) {
//            Log.e(TAG, "", e);
//        }
        try {
            IDao<TransData> dao = getScriptDao();
            String sql = SQL_READ_SCRIPT;
            return readOneTrans(dao, sql);
        } catch (DbException e) {
            Log.e(TAG, "", e);
        }

        return null;
    }

    /**
     * 删除脚本结果记录
     *
     * @return
     */
    public static boolean deleteScript() {
        try {
            IDao<TransData> dao = getScriptDao();
            dao.deleteAll();
            return true;
        } catch (DbException e) {
            Log.e(TAG, "", e);
        }
        return false;
    }

    /**
     * To determine whether the tip amount need to be adjusted.
     * @return true, need to adjust; false, no need to adjust.
     */
    public boolean isAdjustTipAmount() {
        //Tip mode：1, need to adjust; 2, no need to adjust
        String tipMode = FinancialApplication.getSysParam().get(SysParam.TIP_MODE, TipMode.NO_ADJUST);
        return !TextUtils.isEmpty(recvBankResp)
                && recvBankResp.charAt(0) == 'T'
                && recvBankResp.charAt(1) == 'Y'
                && TipMode.ADJUST.equals(tipMode);
    }

    /**
     * 分批获取数据库记录,获取数据库指定偏移量offset中的total条记录
     * @param total
     * @param offset
     * @return
     */
    public static List<TransData> readTransByLimitNo(int total, int offset) {
        try {
            IDao<TransData> dao = getTransDao();
            String sql = "select * from trans where 1=1 order by transNo desc limit " + total + " offset " + offset;
            List<TransData> list = readTransByCondition(dao, sql);

            return list;
        } catch (DbException e) {
            Log.e(TAG, "", e);
        }
        return null;
    }

    //tri
    public static List<TransData> readTransByLimit(int total, int offset, String[] cond) {
        try {
            IDao<TransData> dao = getTransDao();
            String sql = "select * from trans where transtype not in (?,?) order by transNo desc limit " + total + " offset " + offset;

            List<TransData> list = readTransByCondition(dao, sql, cond);

            return list;
        } catch (DbException e) {
            Log.e(TAG, "", e);
        }
        return null;
    }

    public static List<TransData> readTransMpn(int total, int offset, String[] cond) {
        try {
            IDao<TransData> dao = getTransDao();
            String sql = "select * from trans where transtype in (?,?,?) and billingId = ? and refNo = ? limit " + total + " offset " + offset;

            List<TransData> list = readTransByCondition(dao, sql, cond);

            return list;
        } catch (DbException e) {
            Log.e(TAG, "", e);
        }
        return null;
    }
    //end

    /**
     * 获取所有的交易记录
     * @return
     */
    public static ArrayList<TransData> getTradeRecordList() {
        SQLiteDatabase db = null;
        ArrayList<TransData> itemlist = new ArrayList<>();
        Cursor cursor = null;
        String sql;
        try {

            IDao<TransData> dao = getTransDao();

            sql = "select * from " + TransDBHelper.TB_NAME;

            db = dao.getDb();
            cursor = db.rawQuery(sql, null);
            while (cursor.moveToNext()) {
                TransData item = getTransData(cursor);
                itemlist.add(item);
            }

        } catch (Exception e) {
            Log.e(TAG, "", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }

        return itemlist;
    }

    /**
     * 根据条件读取记录
     * @param dao
     * @param sql
     * @return
     */
    private static List<TransData> readTransByCondition(IDao<TransData> dao, String sql) {

        SQLiteDatabase db = dao.getDb();
        Cursor cursor = db.rawQuery(sql, null);
        List<TransData> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            TransData transData = getTransData(cursor);
            list.add(transData);
        }
        cursor.close();
        db.close();
        return list;
    }

    //tri
    private static List<TransData> readTransByCondition(IDao<TransData> dao, String sql, String[] conditon) {

        SQLiteDatabase db = dao.getDb();
        Cursor cursor = db.rawQuery(sql, conditon);
        List<TransData> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            TransData transData = getTransData(cursor);
            list.add(transData);
        }
        cursor.close();
        db.close();
        return list;
    }

    /**
     * 读取指定一条记录
     * @param dao
     * @param sql
     * @return
     */
    private static TransData readOneTrans(IDao<TransData> dao, String sql) {
        TransData transData = null;
        SQLiteDatabase db = dao.getDb();
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.moveToFirst()) {
            transData = getTransData(cursor);
        }
        cursor.close();
        db.close();
        return transData;
    }


    /**
     * 给对象赋值
     * @param cursor
     * @return
     */
    private static TransData getTransData(Cursor cursor) {
        TransData item = new TransData();

        item.setAcqCenterCode(cursor.getString(cursor.getColumnIndex(TransDBHelper.ACQ_CENTER_CODE)));
        item.setAcqCode(cursor.getString(cursor.getColumnIndex(TransDBHelper.ACQ_CODE)));
        item.setAid(cursor.getString(cursor.getColumnIndex(TransDBHelper.AID)));
        item.setAmount(cursor.getString(cursor.getColumnIndex(TransDBHelper.AMOUNT)));
        //item.setAmount("100000");
        item.setSellPrice(cursor.getString(cursor.getColumnIndex(TransDBHelper.SELL_PRICE)));
        item.setArpc(cursor.getString(cursor.getColumnIndex(TransDBHelper.ARPC)));
        item.setArqc(cursor.getString(cursor.getColumnIndex(TransDBHelper.ARQC)));
        item.setAtc(cursor.getString(cursor.getColumnIndex(TransDBHelper.ATC)));
        item.setAuthCode(cursor.getString(cursor.getColumnIndex(TransDBHelper.AUTH_CODE)));
        item.setAuthInsCode(cursor.getString(cursor.getColumnIndex(TransDBHelper.AUTH_INSCODE)));
        item.setAuthMode(cursor.getString(cursor.getColumnIndex(TransDBHelper.AUTH_MODE)));
        item.setBalance(cursor.getString(cursor.getColumnIndex(TransDBHelper.BALANCE)));
        item.setBalanceFlag(cursor.getString(cursor.getColumnIndex(TransDBHelper.BALANCE_FLAG)));
        item.setTvr(cursor.getString(cursor.getColumnIndex(TransDBHelper.TVR)));
        item.setC2b(cursor.getString(cursor.getColumnIndex(TransDBHelper.C2B)));
        item.setC2bVoucher(cursor.getString(cursor.getColumnIndex(TransDBHelper.C2B_VOUCHER)));
        item.setCardSerialNo(cursor.getString(cursor.getColumnIndex(TransDBHelper.CARD_SERIALNO)));
        item.setCenterResp(cursor.getString(cursor.getColumnIndex(TransDBHelper.CENTER_RESP)));
        item.setDate(cursor.getString(cursor.getColumnIndex(TransDBHelper.DATE)));
        item.setDupIccData(cursor.getString(cursor.getColumnIndex(TransDBHelper.DUP_ICC_DATA)));
        item.setEmvAppLabel(cursor.getString(cursor.getColumnIndex(TransDBHelper.EMV_APP_LABEL)));
        item.setEmvAppName(cursor.getString(cursor.getColumnIndex(TransDBHelper.EMV_APP_NAME)));
        item.setTsi(cursor.getString(cursor.getColumnIndex(TransDBHelper.TSI)));
        item.setTransferPan(cursor.getString(cursor.getColumnIndex(TransDBHelper.TRANSFER_PAN)));
        item.setExpDate(cursor.getString(cursor.getColumnIndex(TransDBHelper.EXP_DATE)));
        item.setTransType(cursor.getString(cursor.getColumnIndex(TransDBHelper.TRANS_TYPE)));
        item.setInterOrgCode(cursor.getString(cursor.getColumnIndex(TransDBHelper.INTER_ORG_CODE)));
        item.setTransState(cursor.getString(cursor.getColumnIndex(TransDBHelper.TRANS_STATE)));
        item.setTrack3(cursor.getString(cursor.getColumnIndex(TransDBHelper.TRACK3)));
        item.setTrack2(cursor.getString(cursor.getColumnIndex(TransDBHelper.TRACK2)));
        item.setTrack1(cursor.getString(cursor.getColumnIndex(TransDBHelper.TRACK1)));
        item.setTipAmount(cursor.getString(cursor.getColumnIndex(TransDBHelper.TIP_AMOUNT)));
        item.setTime(cursor.getString(cursor.getColumnIndex(TransDBHelper.TIME)));
        item.setIsserCode(cursor.getString(cursor.getColumnIndex(TransDBHelper.ISSER_CODE)));
        item.setIssuerResp(cursor.getString(cursor.getColumnIndex(TransDBHelper.ISSUER_RESP)));
        item.setOrigAuthCode(cursor.getString(cursor.getColumnIndex(TransDBHelper.ORIG_AUTH_CODE)));
        item.setTc(cursor.getString(cursor.getColumnIndex(TransDBHelper.TC)));
        item.setOrigC2bVoucher(cursor.getString(cursor.getColumnIndex(TransDBHelper.ORIG_C2B_VOUCHER)));
        item.setOrigDate(cursor.getString(cursor.getColumnIndex(TransDBHelper.ORIG_DATE)));
        item.setOrigRefNo(cursor.getString(cursor.getColumnIndex(TransDBHelper.ORIG_REFNO)));
        item.setOrigTermID(cursor.getString(cursor.getColumnIndex(TransDBHelper.ORIG_TERMID)));
        item.setOrigTransType(cursor.getString(cursor.getColumnIndex(TransDBHelper.ORIG_TRANS_TYPE)));
        item.setPan(cursor.getString(cursor.getColumnIndex(TransDBHelper.PAN)));
        item.setProcCode(cursor.getString(cursor.getColumnIndex(TransDBHelper.PROC_CODE)));
        item.setReason(cursor.getString(cursor.getColumnIndex(TransDBHelper.REASON)));
        item.setRecvBankResp(cursor.getString(cursor.getColumnIndex(TransDBHelper.RECV_BANK_RESP)));
        item.setRefNo(cursor.getString(cursor.getColumnIndex(TransDBHelper.REFNO)));
        item.setReserved(cursor.getString(cursor.getColumnIndex(TransDBHelper.RESERVED)));
        item.setScriptData(cursor.getString(cursor.getColumnIndex(TransDBHelper.SCRIPT_DATA)));
        item.setSettleDate(cursor.getString(cursor.getColumnIndex(TransDBHelper.SETTLE_DATE)));
        item.setSendIccData(cursor.getString(cursor.getColumnIndex(TransDBHelper.SEND_ICC_DATA)));
        item.setSendTimes(cursor.getInt(cursor.getColumnIndex(TransDBHelper.SEND_TIMES)));
        item.setBatchNo(cursor.getLong(cursor.getColumnIndex(TransDBHelper.BATCHNO)));
        item.setTransNo(cursor.getLong(cursor.getColumnIndex(TransDBHelper.TRANSNO)));
        //item.setDupTransNo(cursor.getLong(cursor.getColumnIndex(TransDBHelper.DUP_TRANSNO)));
        item.setOrigBatchNo(cursor.getLong(cursor.getColumnIndex(TransDBHelper.ORIG_BATCHNO)));
        item.setOrigTransNo(cursor.getLong(cursor.getColumnIndex(TransDBHelper.ORIG_TRANSNO)));
        item.setSignUpload(cursor.getInt(cursor.getColumnIndex(TransDBHelper.SIGN_UPLOAD)) == 1);
        item.setSignSendState(cursor.getInt(cursor.getColumnIndex(TransDBHelper.SIGN_SEND_STATE)));
        item.setSignFree(cursor.getInt(cursor.getColumnIndex(TransDBHelper.SIGN_FREE)) == 1);
        item.setIsUpload(cursor.getInt(cursor.getColumnIndex(TransDBHelper.IS_UPLOAD)) == 1);
        item.setIsOffUploadState(cursor.getInt(cursor.getColumnIndex(TransDBHelper.IS_OFF_UPLOAD_STATE)) == 1);
        item.setIsEncTrack(cursor.getInt(cursor.getColumnIndex(TransDBHelper.IS_ENCTRACK)) == 1);
        item.setIsCDCVM(cursor.getInt(cursor.getColumnIndex(TransDBHelper.IS_CDCVM)) == 1);
        item.setPinFree(cursor.getInt(cursor.getColumnIndex(TransDBHelper.PIN_FREE)) == 1);
        item.setHasPin(cursor.getInt(cursor.getColumnIndex(TransDBHelper.HAS_PIN)) == 1);
        item.setTransferEnterMode(cursor.getInt(cursor.getColumnIndex(TransDBHelper.TRANSFER_ENTER_MODE)));
        item.setEnterMode(cursor.getInt(cursor.getColumnIndex(TransDBHelper.ENTER_MODE)));
        item.setIsAdjustAfterUpload(cursor.getInt(cursor.getColumnIndex(TransDBHelper.IS_ADJUST_AFTER_UPLOAD)) == 1);
        item.setIsOnlineTrans(cursor.getInt(cursor.getColumnIndex(TransDBHelper.IS_ONLINE_TRANS)) == 1);
        item.setSignData(cursor.getBlob(cursor.getColumnIndex(TransDBHelper.SIGN_DATA)));
        item.setSendFailFlag(cursor.getInt(cursor.getColumnIndex(TransDBHelper.SEND_FAIL_FLAG)));

        if (cursor.getString(cursor.getColumnIndex(TransDBHelper.EMV_RESULT)) != null)
            item.setEmvResult(Byte.parseByte(cursor.getString(cursor.getColumnIndex(TransDBHelper.EMV_RESULT))));

        //The following is Added by Steven.T 2017-6-7 17:04:45
        item.setCardCurrencyCode(cursor.getString(cursor.getColumnIndex(TransDBHelper.CARD_CURRENCY_CODE)));
        item.setTerminalCurrencyCode(cursor.getString(cursor.getColumnIndex(TransDBHelper.TERMINAL_CURRENCY_CODE)));
        item.setTerminalCurrencyName(cursor.getString(cursor.getColumnIndex(TransDBHelper.TERMINAL_CURRENCY_NAME)));
        item.setTermianlCurrencyDecimals(cursor.getString(cursor.getColumnIndex(TransDBHelper.TERMINAL_CURRENCY_DECIMALS)));
        item.setCurrencyRate(cursor.getString(cursor.getColumnIndex(TransDBHelper.CURRENCY_RATE)));
        item.setInstalNum(cursor.getString(cursor.getColumnIndex(TransDBHelper.INSTAL_NUM)));
        item.setPrjCode(cursor.getString(cursor.getColumnIndex(TransDBHelper.PRJ_CODE)));
        item.setFirstAmount(cursor.getString(cursor.getColumnIndex(TransDBHelper.FIRST_AMOUNT)));
        item.setFeeTotalAmount(cursor.getString(cursor.getColumnIndex(TransDBHelper.FEE_TOTAL_AMOUNT)));
        item.setInstalCurrCode(cursor.getString(cursor.getColumnIndex(TransDBHelper.INSTAL_CURRCODE)));
        //sandy
        item.setDiscountAmount(cursor.getString(cursor.getColumnIndex(TransDBHelper.DISCOUNT_AMOUNT)));
        item.setActualPayAmount(cursor.getString(cursor.getColumnIndex(TransDBHelper.ACTUAL_AMOUNT)));
        item.setCouponNo(cursor.getString(cursor.getColumnIndex(TransDBHelper.COUPON_NO)));
        item.setOrigDateTimeTrans(cursor.getLong(cursor.getColumnIndex(TransDBHelper.ORIG_DATE_TIME)));
        item.setDateTimeTrans(cursor.getLong(cursor.getColumnIndex(TransDBHelper.DATE_TIME)));
        item.setOrigCouponRefNo(cursor.getString(cursor.getColumnIndex(TransDBHelper.ORIG_COUPON_NO)));
        item.setOrigCouponDateTimeTrans(cursor.getLong(cursor.getColumnIndex(TransDBHelper.ORIG_COUPON_DATE_TIME)));
        item.setBillingId(cursor.getString(cursor.getColumnIndex(TransDBHelper.BILLING_ID)));
        item.setDestBank(cursor.getString(cursor.getColumnIndex(TransDBHelper.DEST_BANK)));
        item.setField102(cursor.getString(cursor.getColumnIndex(TransDBHelper.F102)));
        item.setField103(cursor.getString(cursor.getColumnIndex(TransDBHelper.F103)));
        item.setNtb(cursor.getString(cursor.getColumnIndex(TransDBHelper.NTB)));
        item.setReprintData(cursor.getString(cursor.getColumnIndex(TransDBHelper.REPRINT_DATA)));
        item.setField47(cursor.getString(cursor.getColumnIndex(TransDBHelper.F47)));
        item.setAccNo(cursor.getString(cursor.getColumnIndex(TransDBHelper.ACC_NO)));
        item.setField61(cursor.getString(cursor.getColumnIndex(TransDBHelper.FIELD61)));
        item.setPrintTimeout(cursor.getString(cursor.getColumnIndex(TransDBHelper.PRINT_TIMEOUT)));
        item.setField59(cursor.getString(cursor.getColumnIndex(TransDBHelper.FIELD59)));
        item.setField28(cursor.getString(cursor.getColumnIndex(TransDBHelper.FIELD28)));
        item.setField127(cursor.getString(cursor.getColumnIndex(TransDBHelper.FIELD127)));
        item.setField36(cursor.getString(cursor.getColumnIndex(TransDBHelper.FIELD36)));
        item.setField48(cursor.getString(cursor.getColumnIndex(TransDBHelper.FIELD48)));
        item.setField110(cursor.getString(cursor.getColumnIndex(TransDBHelper.FIELD110)));


        return item;
    }

    private static final String SQL_READ_SCRIPT = "select * from script where 1=1 order by id desc limit 1";
    private static final String SQL_READ_LASTTRANS = "select * from trans where 1=1 order by id desc limit 1";

    public static long[] getTransFeeNumAndAmount(String transType, ETransStatus status, boolean isCup) {
        SQLiteDatabase dataBase = null;
        Cursor cursor = null;
        long[] obj = new long[2];
        try {
            IDao<TransData> dao = getTransDao();
            StringBuilder sql = new StringBuilder();
            sql.append("select count(id),");
            //denny
            sql.append("sum(feeTotalAmount) ");

            sql.append("from " + DbInfo.TABLE_NAME_TRANS + " ");
            sql.append("where (transType =? and transState =?) and isAdjustAfterUpload=?");

            if (isCup)
                sql.append(" and (interOrgCode is null or interOrgCode =?)");
            else {
                sql.append(" and (interOrgCode is not null and interOrgCode <>?)");
            }

            dataBase = dao.getDb();
            cursor = dataBase.rawQuery(sql.toString(), new String[] { transType, status.toString(), "0", "CUP" });

            while (cursor.moveToNext()) {
                obj[0] = cursor.getLong(0);
                obj[1] = Long.parseLong(cursor.getString(1) != null ? cursor.getString(1) : "0");
            }

            return obj;

        } catch (DbException e) {
            Log.e(TAG, "", e);
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }

            if (dataBase != null) {
                dataBase.close();
            }
        }

    }
}
