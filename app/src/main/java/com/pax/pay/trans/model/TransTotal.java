package com.pax.pay.trans.model;

import android.util.Log;

import com.pax.device.Device;
import com.pax.gl.db.DbException;
import com.pax.gl.db.IDb.AEntityBase;
import com.pax.gl.db.IDb.Column;
import com.pax.gl.db.IDb.IDao;
import com.pax.gl.db.IDb.IDbListener;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.trans.TransContext;
import com.pax.pay.trans.model.TransData.ETransStatus;
import com.pax.settings.SysParam;

import java.io.Serializable;

/**
 * 交易总计
 *
 * @author Steven.W
 */
public class TransTotal extends AEntityBase implements Serializable {
    public static final String TAG = "TransTotal";

    private static final long serialVersionUID = 1L;
    /**
     * 商户号
     */
    @Column
    private String merchantID;
    /**
     * 终端号
     */
    @Column
    private String terminalID;
    /**
     * 操作员号
     */
    @Column
    private String operatorID;
    /**
     * 批次号
     */
    @Column
    private String batchNo;
    /**
     * 日期
     */
    @Column
    private String date;
    /**
     * 时间
     */
    @Column
    private String time;

    /**
     * 内卡借记总金额
     */
    @Column
    private long rmbDebitAmount;
    /**
     * 内卡借记总笔数
     */
    @Column
    private long rmbDebitNum;
    /**
     * 内卡贷记总金额
     */
    @Column
    private long rmbCreditAmount;
    /**
     * 内卡贷记总笔数
     */
    @Column
    private long rmbCreditNum;
    /**
     * 外卡借记总金额
     */
    @Column
    private long frnDebitAmount;
    /**
     * 外卡借记总笔数
     */
    @Column
    private long frnDebitNum;
    /**
     * 外卡贷记总金额
     */
    @Column
    private long frnCreditAmount;
    /**
     * 外卡贷记总笔数
     */
    @Column
    private long frnCreditNum;

    /**
     * 内卡消费总金额
     */
    @Column
    private long saleTotalAmt;
    /**
     * 内卡消费总笔数
     */
    @Column
    private long saleTotalNum;

    /**
     * 内卡电子现金消费总金额
     */
    @Column
    private long ecSaleTotalAmt;
    /**
     * 内卡电子现金消费总笔数
     */
    @Column
    private long ecSaleTotalNum;

    /**
     * 内卡撤销总金额
     */
    @Column
    private long voidTotalAmt;
    /**
     * 内卡撤销总笔数
     */
    @Column
    private long voidTotalNum;
    /**
     * 内卡退货总金额
     */
    @Column
    private long refundTotalAmt;
    /**
     * 内卡退货总笔数
     */
    @Column
    private long refundTotalNum;
    /**
     * 内卡预授权总金额
     */
    @Column
    private long authTotalAmt;
    /**
     * 内卡预授权总笔数
     */
    @Column
    private long authTotalNum;
    /**
     * 内卡预授权撤销总金额
     */
    @Column
    private long authVoidTotalAmt;
    /**
     * 内卡预授权撤销总笔数
     */
    @Column
    private long authVoidTotalNum;
    /**
     * 内卡预授权完成请求总金额
     */
    @Column
    private long preAuthCmpTotalAmt;
    /**
     * 内卡预授权完成请求总笔数
     */
    @Column
    private long preAuthCmpTotalNum;
    /**
     * 内卡预授权完成请求撤销总金额
     */
    @Column
    private long authCMVoidTotalAmt;
    /**
     * 内卡预授权完成请求撤销总笔数
     */
    @Column
    private long authCMVoidTotalNum;
    /**
     * 内卡预授权完成通知总金额
     */
    @Column
    private long preAuthCmpAdvTotalAmt;
    /**
     * 内卡预授权完成通知总笔数
     */
    @Column
    private long preAuthCmpAdvTotalNum;

    /**
     * 内卡离线交易总金额
     */
    @Column
    private long offlineTotalAmt;

    /**
     * 内卡离线交易总笔数
     */
    @Column
    private long offlineTotalNum;

    /**
     * 内卡电子现金圈存交易总笔数
     */
    @Column
    private long ecLoadTotalNum;
    /**
     * 内卡电子现金圈存交易总金额
     */
    @Column
    private long ecLoadTotalAmt;

    /**
     * 内卡电子现金充值总笔数
     */
    @Column
    private long ecCashLoadTotalNum;
    /**
     * 内卡电子现金充值总金额
     */
    @Column
    private long ecCashLoadTotalAmt;

    /**
     * 内卡电子现金充值撤销总笔数
     */
    @Column
    private long ecCashLoadVoidTotalNum;
    /**
     * 内卡电子现金充值撤销总金额
     */
    @Column
    private long ecCashLoadVoidTotalAmt;

    /**
     * 内卡电子现金脱机退货总笔数
     */
    @Column
    private long ecRefundTotalNum;
    /**
     * 内卡电子现金脱机退货总金额
     */
    @Column
    private long ecRefundTotalAmt;

    /**
     * 外卡消费总金额
     */
    @Column
    private long frnSaleTotalAmt;
    /**
     * 外卡消费总笔数
     */
    @Column
    private long frnSaleTotalNum;

    /**
     * 外卡电子现金消费总金额
     */
    @Column
    private long frnEcSaleTotalAmt;
    /**
     * 外卡电子现金消费总笔数
     */
    @Column
    private long frnEcSaleTotalNum;

    /**
     * 外卡撤销总金额
     */
    @Column
    private long frnVoidTotalAmt;
    /**
     * 外卡撤销总笔数
     */
    @Column
    private long frnVoidTotalNum;
    /**
     * 外卡退货总金额
     */
    @Column
    private long frnRefundTotalAmt;
    /**
     * 外卡退货总笔数
     */
    @Column
    private long frnRefundTotalNum;
    /**
     * 外卡预授权总金额
     */
    @Column
    private long frnAuthTotalAmt;
    /**
     * 外卡预授权总笔数
     */
    @Column
    private long frnAuthTotalNum;
    /**
     * 外卡预授权撤销总金额
     */
    @Column
    private long frnAuthVoidTotalAmt;
    /**
     * 外卡预授权撤销总笔数
     */
    @Column
    private long frnAuthVoidTotalNum;
    /**
     * 外卡预授权完成请求总金额
     */
    @Column
    private long frnPreAuthCmpTotalAmt;
    /**
     * 外卡预授权完成请求总笔数
     */
    @Column
    private long frnPreAuthCmpTotalNum;
    /**
     * 外卡预授权完成请求撤销总金额
     */
    @Column
    private long frnAuthCMVoidTotalAmt;
    /**
     * 外卡预授权完成请求撤销总笔数
     */
    @Column
    private long frnAuthCMVoidTotalNum;
    /**
     * 外卡预授权完成通知总金额
     */
    @Column
    private long frnPreAuthCmpAdvTotalAmt;
    /**
     * 外卡预授权完成通知总笔数
     */
    @Column
    private long frnPreAuthCmpAdvTotalNum;

    /**
     * 外卡离线交易总金额
     */
    @Column
    private long frnOfflineTotalAmt;

    /**
     * 外卡离线交易总笔数
     */
    @Column
    private long frnOfflineTotalNum;

    /**
     * 外卡圈存交易总笔数
     */
    @Column
    private long frnEcLoadTotalNum;
    /**
     * 外卡圈存交易总金额
     */
    @Column
    private long frnEcLoadTotalAmt;

    /**
     * 外卡电子现金充值总笔数
     */
    @Column
    private long frnEcCashLoadTotalNum;
    /**
     * 外卡电子现金充值总金额
     */
    @Column
    private long frnEcCashLoadTotalAmt;

    /**
     * 外卡电子现金充值撤销总笔数
     */
    @Column
    private long frnEcCashLoadVoidTotalNum;
    /**
     * 外卡电子现金充值撤销总金额
     */
    @Column
    private long frnEcCashLoadVoidTotalAmt;

    /**
     * 外卡电子现金脱机退货总笔数
     */
    @Column
    private long frnEcRefundTotalNum;
    /**
     * 外卡电子现金脱机退货总金额
     */
    @Column
    private long frnEcRefundTotalAmt;
    /**
     * the num of sale that have tip.
     */
    @Column
    private long saleTotalTipNum;
    /**
     * the total amount of tip.
     */
    @Column
    private long saleTotalTipAmt;
    /**
     * Total number of tips of non-cup card.
     */
    @Column
    private long frnSaleTotalTipNum;
    /**
     * Total amount of tips of non-cup card.
     */
    @Column
    private long frnSaleTotalTipAmt;
    /**
     * the number of installment txn.
     */
    @Column
    private long installTotalNum;
    /**
     * the total amount of installment txn.
     */
    @Column
    private long installTotalAmt;
    /**
     * Total number of installment of non-cup card.
     */
    @Column
    private long frnInstallTotalNum;
    /**
     * Total amount of installment of non-cup card.
     */
    @Column
    private long frnInstallTotalAmt;
    /**
     * the total number of MOTO auth complete.
     */
    @Column
    private long motoAuthCmpTotalNum;
    /**
     * the total amount of MOTO auth complete.
     */
    @Column
    private long motoAuthCmpTotalAmt;
    /**
     * the total number of moto auth cmp of non-cup card.
     */
    @Column
    private long frnMotoAuthCmpTotalNum;
    /**
     * the total amount of moto auth cmp of non-cup card.
     */
    @Column
    private long frnMotoAuthCmpTotalAmt;
    /**
     * the total number of MOTO auth complete advice.
     */
    @Column
    private long motoAuthCmpAdvTotalNum;
    /**
     * the total amount of MOTO auth complete advice.
     */
    @Column
    private long motoAuthCmpAdvTotalAmt;
    /**
     * the total number of MOTO auth complete advice of non-cup card.
     */
    @Column
    private long frnMotoAuthCmpAdvTotalNum;
    /**
     * the total amount of MOTO auth complete advice of non-cup card.
     */
    @Column
    private long frnMotoAuthCmpAdvTotalAmt;

    //sandy
    @Column
    private long saleCouponTotalAmt;
    /**
     * 内卡消费总笔数
     */
    @Column
    private long saleCouponTotalNum;

    //add denny
    @Column
    private long saleFeeTotalAmt;
    @Column
    private long saleFeeTotalNum;
    //feeamt
    @Column
    private long[][] transTotalAmt; //[count][totalAmount][totalFee] setor, Tarik, Transfer, Pbb, Mpn, Pulsa, Infosaldo, Ministement


    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getMerchantID() {
        return merchantID;
    }

    public void setMerchantID(String merchantID) {
        this.merchantID = merchantID;
    }

    public String getTerminalID() {
        return terminalID;
    }

    public void setTerminalID(String terminalID) {
        this.terminalID = terminalID;
    }

    public String getOperatorID() {
        return operatorID;
    }

    public void setOperatorID(String operatorID) {
        this.operatorID = operatorID;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public long getRmbDebitAmount() {
        return rmbDebitAmount;
    }

    public void setRmbDebitAmount(long rmbDebitAmount) {
        this.rmbDebitAmount = rmbDebitAmount;
    }

    public long getRmbDebitNum() {
        return rmbDebitNum;
    }

    public void setRmbDebitNum(long rmbDebitNum) {
        this.rmbDebitNum = rmbDebitNum;
    }

    public long getRmbCreditAmount() {
        return rmbCreditAmount;
    }

    public void setRmbCreditAmount(long rmbCreditAmount) {
        this.rmbCreditAmount = rmbCreditAmount;
    }

    public long getRmbCreditNum() {
        return rmbCreditNum;
    }

    public void setRmbCreditNum(long rmbCreditNum) {
        this.rmbCreditNum = rmbCreditNum;
    }

    public long getFrnDebitAmount() {
        return frnDebitAmount;
    }

    public void setFrnDebitAmount(long frnDebitAmount) {
        this.frnDebitAmount = frnDebitAmount;
    }

    public long getFrnDebitNum() {
        return frnDebitNum;
    }

    public void setFrnDebitNum(long frnDebitNum) {
        this.frnDebitNum = frnDebitNum;
    }

    public long getFrnCreditAmount() {
        return frnCreditAmount;
    }

    public void setFrnCreditAmount(long frnCreditAmount) {
        this.frnCreditAmount = frnCreditAmount;
    }

    public long getFrnCreditNum() {
        return frnCreditNum;
    }

    public void setFrnCreditNum(long frnCreditNum) {
        this.frnCreditNum = frnCreditNum;
    }

    public long getSaleTotalAmt() {
        return saleTotalAmt;
    }

    public void setSaleTotalAmt(long saleTotalAmt) {
        this.saleTotalAmt = saleTotalAmt;
    }

    public long getSaleTotalNum() {
        return saleTotalNum;
    }

    public void setSaleTotalNum(long saleTotalNum) {
        this.saleTotalNum = saleTotalNum;
    }

    public long getEcSaleTotalAmt() {
        return ecSaleTotalAmt;
    }

    public void setEcSaleTotalAmt(long ecSaleTotalAmt) {
        this.ecSaleTotalAmt = ecSaleTotalAmt;
    }

    public long getEcSaleTotalNum() {
        return ecSaleTotalNum;
    }

    public void setEcSaleTotalNum(long ecSaleTotalNum) {
        this.ecSaleTotalNum = ecSaleTotalNum;
    }

    public long getVoidTotalAmt() {
        return voidTotalAmt;
    }

    public void setVoidTotalAmt(long voidTotalAmt) {
        this.voidTotalAmt = voidTotalAmt;
    }

    public long getVoidTotalNum() {
        return voidTotalNum;
    }

    public void setVoidTotalNum(long voidTotalNum) {
        this.voidTotalNum = voidTotalNum;
    }

    public long getRefundTotalAmt() {
        return refundTotalAmt;
    }

    public void setRefundTotalAmt(long refundTotalAmt) {
        this.refundTotalAmt = refundTotalAmt;
    }

    public long getRefundTotalNum() {
        return refundTotalNum;
    }

    public void setRefundTotalNum(long refundTotalNum) {
        this.refundTotalNum = refundTotalNum;
    }

    public long getAuthTotalAmt() {
        return authTotalAmt;
    }

    public void setAuthTotalAmt(long authTotalAmt) {
        this.authTotalAmt = authTotalAmt;
    }

    public long getAuthTotalNum() {
        return authTotalNum;
    }

    public void setAuthTotalNum(long authTotalNum) {
        this.authTotalNum = authTotalNum;
    }

    public long getAuthVoidTotalAmt() {
        return authVoidTotalAmt;
    }

    public void setAuthVoidTotalAmt(long authVoidTotalAmt) {
        this.authVoidTotalAmt = authVoidTotalAmt;
    }

    public long getAuthVoidTotalNum() {
        return authVoidTotalNum;
    }

    public void setAuthVoidTotalNum(long authVoidTotalNum) {
        this.authVoidTotalNum = authVoidTotalNum;
    }

    public long getPreAuthCmpTotalAmt() {
        return preAuthCmpTotalAmt;
    }

    public void setPreAuthCmpTotalAmt(long preAuthCmpTotalAmt) {
        this.preAuthCmpTotalAmt = preAuthCmpTotalAmt;
    }

    public long getPreAuthCmpTotalNum() {
        return preAuthCmpTotalNum;
    }

    public void setPreAuthCmpTotalNum(long preAuthCmpTotalNum) {
        this.preAuthCmpTotalNum = preAuthCmpTotalNum;
    }

    public long getAuthCMVoidTotalAmt() {
        return authCMVoidTotalAmt;
    }

    public void setAuthCMVoidTotalAmt(long authCMVoidTotalAmt) {
        this.authCMVoidTotalAmt = authCMVoidTotalAmt;
    }

    public long getAuthCMVoidTotalNum() {
        return authCMVoidTotalNum;
    }

    public void setAuthCMVoidTotalNum(long authCMVoidTotalNum) {
        this.authCMVoidTotalNum = authCMVoidTotalNum;
    }

    public long getPreAuthCmpAdvTotalAmt() {
        return preAuthCmpAdvTotalAmt;
    }

    public void setPreAuthCmpAdvTotalAmt(long preAuthCmpAdvTotalAmt) {
        this.preAuthCmpAdvTotalAmt = preAuthCmpAdvTotalAmt;
    }

    public long getPreAuthCmpAdvTotalNum() {
        return preAuthCmpAdvTotalNum;
    }

    public void setPreAuthCmpAdvTotalNum(long preAuthCmpAdvTotalNum) {
        this.preAuthCmpAdvTotalNum = preAuthCmpAdvTotalNum;
    }

    public long getOfflineTotalAmt() {
        return offlineTotalAmt;
    }

    public void setOfflineTotalAmt(long offlineTotalAmt) {
        this.offlineTotalAmt = offlineTotalAmt;
    }

    public long getOfflineTotalNum() {
        return offlineTotalNum;
    }

    public void setOfflineTotalNum(long offlineTotalNum) {
        this.offlineTotalNum = offlineTotalNum;
    }

    public long getEcLoadTotalNum() {
        return ecLoadTotalNum;
    }

    public void setEcLoadTotalNum(long ecLoadTotalNum) {
        this.ecLoadTotalNum = ecLoadTotalNum;
    }

    public long getEcLoadTotalAmt() {
        return ecLoadTotalAmt;
    }

    public void setEcLoadTotalAmt(long ecLoadTotalAmt) {
        this.ecLoadTotalAmt = ecLoadTotalAmt;
    }

    public long getEcCashLoadTotalNum() {
        return ecCashLoadTotalNum;
    }

    public void setEcCashLoadTotalNum(long ecCashLoadTotalNum) {
        this.ecCashLoadTotalNum = ecCashLoadTotalNum;
    }

    public long getEcCashLoadTotalAmt() {
        return ecCashLoadTotalAmt;
    }

    public void setEcCashLoadTotalAmt(long ecCashLoadTotalAmt) {
        this.ecCashLoadTotalAmt = ecCashLoadTotalAmt;
    }

    public long getEcCashLoadVoidTotalNum() {
        return ecCashLoadVoidTotalNum;
    }

    public void setEcCashLoadVoidTotalNum(long ecCashLoadVoidTotalNum) {
        this.ecCashLoadVoidTotalNum = ecCashLoadVoidTotalNum;
    }

    public long getEcCashLoadVoidTotalAmt() {
        return ecCashLoadVoidTotalAmt;
    }

    public void setEcCashLoadVoidTotalAmt(long ecCashLoadVoidTotalAmt) {
        this.ecCashLoadVoidTotalAmt = ecCashLoadVoidTotalAmt;
    }

    public long getEcRefundTotalNum() {
        return ecRefundTotalNum;
    }

    public void setEcRefundTotalNum(long ecRefundTotalNum) {
        this.ecRefundTotalNum = ecRefundTotalNum;
    }

    public long getEcRefundTotalAmt() {
        return ecRefundTotalAmt;
    }

    public void setEcRefundTotalAmt(long ecRefundTotalAmt) {
        this.ecRefundTotalAmt = ecRefundTotalAmt;
    }

    public long getFrnSaleTotalAmt() {
        return frnSaleTotalAmt;
    }

    public void setFrnSaleTotalAmt(long frnSaleTotalAmt) {
        this.frnSaleTotalAmt = frnSaleTotalAmt;
    }

    public long getFrnSaleTotalNum() {
        return frnSaleTotalNum;
    }

    public void setFrnSaleTotalNum(long frnSaleTotalNum) {
        this.frnSaleTotalNum = frnSaleTotalNum;
    }

    public long getFrnEcSaleTotalAmt() {
        return frnEcSaleTotalAmt;
    }

    public void setFrnEcSaleTotalAmt(long frnEcSaleTotalAmt) {
        this.frnEcSaleTotalAmt = frnEcSaleTotalAmt;
    }

    public long getFrnEcSaleTotalNum() {
        return frnEcSaleTotalNum;
    }

    public void setFrnEcSaleTotalNum(long frnEcSaleTotalNum) {
        this.frnEcSaleTotalNum = frnEcSaleTotalNum;
    }

    public long getFrnVoidTotalAmt() {
        return frnVoidTotalAmt;
    }

    public void setFrnVoidTotalAmt(long frnVoidTotalAmt) {
        this.frnVoidTotalAmt = frnVoidTotalAmt;
    }

    public long getFrnVoidTotalNum() {
        return frnVoidTotalNum;
    }

    public void setFrnVoidTotalNum(long frnVoidTotalNum) {
        this.frnVoidTotalNum = frnVoidTotalNum;
    }

    public long getFrnRefundTotalAmt() {
        return frnRefundTotalAmt;
    }

    public void setFrnRefundTotalAmt(long frnRefundTotalAmt) {
        this.frnRefundTotalAmt = frnRefundTotalAmt;
    }

    public long getFrnRefundTotalNum() {
        return frnRefundTotalNum;
    }

    public void setFrnRefundTotalNum(long frnRefundTotalNum) {
        this.frnRefundTotalNum = frnRefundTotalNum;
    }

    public long getFrnAuthTotalAmt() {
        return frnAuthTotalAmt;
    }

    public void setFrnAuthTotalAmt(long frnAuthTotalAmt) {
        this.frnAuthTotalAmt = frnAuthTotalAmt;
    }

    public long getFrnAuthTotalNum() {
        return frnAuthTotalNum;
    }

    public void setFrnAuthTotalNum(long frnAuthTotalNum) {
        this.frnAuthTotalNum = frnAuthTotalNum;
    }

    public long getFrnAuthVoidTotalAmt() {
        return frnAuthVoidTotalAmt;
    }

    public void setFrnAuthVoidTotalAmt(long frnAuthVoidTotalAmt) {
        this.frnAuthVoidTotalAmt = frnAuthVoidTotalAmt;
    }

    public long getFrnAuthVoidTotalNum() {
        return frnAuthVoidTotalNum;
    }

    public void setFrnAuthVoidTotalNum(long frnAuthVoidTotalNum) {
        this.frnAuthVoidTotalNum = frnAuthVoidTotalNum;
    }

    public long getFrnPreAuthCmpTotalAmt() {
        return frnPreAuthCmpTotalAmt;
    }

    public void setFrnPreAuthCmpTotalAmt(long frnPreAuthCmpTotalAmt) {
        this.frnPreAuthCmpTotalAmt = frnPreAuthCmpTotalAmt;
    }

    public long getFrnPreAuthCmpTotalNum() {
        return frnPreAuthCmpTotalNum;
    }

    public void setFrnPreAuthCmpTotalNum(long frnPreAuthCmpTotalNum) {
        this.frnPreAuthCmpTotalNum = frnPreAuthCmpTotalNum;
    }

    public long getFrnAuthCMVoidTotalAmt() {
        return frnAuthCMVoidTotalAmt;
    }

    public void setFrnAuthCMVoidTotalAmt(long frnAuthCMVoidTotalAmt) {
        this.frnAuthCMVoidTotalAmt = frnAuthCMVoidTotalAmt;
    }

    public long getFrnAuthCMVoidTotalNum() {
        return frnAuthCMVoidTotalNum;
    }

    public void setFrnAuthCMVoidTotalNum(long frnAuthCMVoidTotalNum) {
        this.frnAuthCMVoidTotalNum = frnAuthCMVoidTotalNum;
    }

    public long getFrnPreAuthCmpAdvTotalAmt() {
        return frnPreAuthCmpAdvTotalAmt;
    }

    public void setFrnPreAuthCmpAdvTotalAmt(long frnPreAuthCmpAdvTotalAmt) {
        this.frnPreAuthCmpAdvTotalAmt = frnPreAuthCmpAdvTotalAmt;
    }

    public long getFrnPreAuthCmpAdvTotalNum() {
        return frnPreAuthCmpAdvTotalNum;
    }

    public void setFrnPreAuthCmpAdvTotalNum(long frnPreAuthCmpAdvTotalNum) {
        this.frnPreAuthCmpAdvTotalNum = frnPreAuthCmpAdvTotalNum;
    }

    public long getFrnOfflineTotalAmt() {
        return frnOfflineTotalAmt;
    }

    public void setFrnOfflineTotalAmt(long frnOfflineTotalAmt) {
        this.frnOfflineTotalAmt = frnOfflineTotalAmt;
    }

    public long getFrnOfflineTotalNum() {
        return frnOfflineTotalNum;
    }

    public void setFrnOfflineTotalNum(long frnOfflineTotalNum) {
        this.frnOfflineTotalNum = frnOfflineTotalNum;
    }

    public long getFrnEcLoadTotalNum() {
        return frnEcLoadTotalNum;
    }

    public void setFrnEcLoadTotalNum(long frnEcLoadTotalNum) {
        this.frnEcLoadTotalNum = frnEcLoadTotalNum;
    }

    public long getFrnEcLoadTotalAmt() {
        return frnEcLoadTotalAmt;
    }

    public void setFrnEcLoadTotalAmt(long frnEcLoadTotalAmt) {
        this.frnEcLoadTotalAmt = frnEcLoadTotalAmt;
    }

    public long getFrnEcCashLoadTotalNum() {
        return frnEcCashLoadTotalNum;
    }

    public void setFrnEcCashLoadTotalNum(long frnEcCashLoadTotalNum) {
        this.frnEcCashLoadTotalNum = frnEcCashLoadTotalNum;
    }

    public long getFrnEcCashLoadTotalAmt() {
        return frnEcCashLoadTotalAmt;
    }

    public void setFrnEcCashLoadTotalAmt(long frnEcCashLoadTotalAmt) {
        this.frnEcCashLoadTotalAmt = frnEcCashLoadTotalAmt;
    }

    public long getFrnEcCashLoadVoidTotalNum() {
        return frnEcCashLoadVoidTotalNum;
    }

    public void setFrnEcCashLoadVoidTotalNum(long frnEcCashLoadVoidTotalNum) {
        this.frnEcCashLoadVoidTotalNum = frnEcCashLoadVoidTotalNum;
    }

    public long getFrnEcCashLoadVoidTotalAmt() {
        return frnEcCashLoadVoidTotalAmt;
    }

    public void setFrnEcCashLoadVoidTotalAmt(long frnEcCashLoadVoidTotalAmt) {
        this.frnEcCashLoadVoidTotalAmt = frnEcCashLoadVoidTotalAmt;
    }

    public long getFrnEcRefundTotalNum() {
        return frnEcRefundTotalNum;
    }

    public void setFrnEcRefundTotalNum(long frnEcRefundTotalNum) {
        this.frnEcRefundTotalNum = frnEcRefundTotalNum;
    }

    public long getFrnEcRefundTotalAmt() {
        return frnEcRefundTotalAmt;
    }

    public void setFrnEcRefundTotalAmt(long frnEcRefundTotalAmt) {
        this.frnEcRefundTotalAmt = frnEcRefundTotalAmt;
    }

    public long getSaleTotalTipNum() {
        return saleTotalTipNum;
    }

    public void setSaleTotalTipNum(long saleTotalTipNum) {
        this.saleTotalTipNum = saleTotalTipNum;
    }

    public long getSaleTotalTipAmt() {
        return saleTotalTipAmt;
    }

    public void setSaleTotalTipAmt(long saleTotalTipAmt) {
        this.saleTotalTipAmt = saleTotalTipAmt;
    }

    public long getFrnSaleTotalTipNum() {
        return frnSaleTotalTipNum;
    }

    public void setFrnSaleTotalTipNum(long frnSaleTotalTipNum) {
        this.frnSaleTotalTipNum = frnSaleTotalTipNum;
    }

    public long getFrnSaleTotalTipAmt() {
        return frnSaleTotalTipAmt;
    }

    public void setFrnSaleTotalTipAmt(long frnSaleTotalTipAmt) {
        this.frnSaleTotalTipAmt = frnSaleTotalTipAmt;
    }

    public long getInstallTotalNum() {
        return installTotalNum;
    }

    public void setInstallTotalNum(long installTotalNum) {
        this.installTotalNum = installTotalNum;
    }

    public long getInstallTotalAmt() {
        return installTotalAmt;
    }

    public void setInstallTotalAmt(long installTotalAmt) {
        this.installTotalAmt = installTotalAmt;
    }

    public long getFrnInstallTotalNum() {
        return frnInstallTotalNum;
    }

    public void setFrnInstallTotalNum(long frnInstallTotalNum) {
        this.frnInstallTotalNum = frnInstallTotalNum;
    }

    public long getFrnInstallTotalAmt() {
        return frnInstallTotalAmt;
    }

    public void setFrnInstallTotalAmt(long frnInstallTotalAmt) {
        this.frnInstallTotalAmt = frnInstallTotalAmt;
    }

    public long getMotoAuthCmpTotalNum() {
        return motoAuthCmpTotalNum;
    }

    public void setMotoAuthCmpTotalNum(long motoAuthCmpTotalNum) {
        this.motoAuthCmpTotalNum = motoAuthCmpTotalNum;
    }

    public long getMotoAuthCmpTotalAmt() {
        return motoAuthCmpTotalAmt;
    }

    public void setMotoAuthCmpTotalAmt(long motoAuthCmpTotalAmt) {
        this.motoAuthCmpTotalAmt = motoAuthCmpTotalAmt;
    }

    public long getFrnMotoAuthCmpTotalNum() {
        return frnMotoAuthCmpTotalNum;
    }

    public void setFrnMotoAuthCmpTotalNum(long frnMotoAuthCmpTotalNum) {
        this.frnMotoAuthCmpTotalNum = frnMotoAuthCmpTotalNum;
    }

    public long getFrnMotoAuthCmpTotalAmt() {
        return frnMotoAuthCmpTotalAmt;
    }

    public void setFrnMotoAuthCmpTotalAmt(long frnMotoAuthCmpTotalAmt) {
        this.frnMotoAuthCmpTotalAmt = frnMotoAuthCmpTotalAmt;
    }

    public long getMotoAuthCmpAdvTotalNum() {
        return motoAuthCmpAdvTotalNum;
    }

    public void setMotoAuthCmpAdvTotalNum(long motoAuthCmpAdvTotalNum) {
        this.motoAuthCmpAdvTotalNum = motoAuthCmpAdvTotalNum;
    }

    public long getMotoAuthCmpAdvTotalAmt() {
        return motoAuthCmpAdvTotalAmt;
    }

    public void setMotoAuthCmpAdvTotalAmt(long motoAuthCmpAdvTotalAmt) {
        this.motoAuthCmpAdvTotalAmt = motoAuthCmpAdvTotalAmt;
    }

    public long getFrnMotoAuthCmpAdvTotalNum() {
        return frnMotoAuthCmpAdvTotalNum;
    }

    public void setFrnMotoAuthCmpAdvTotalNum(long frnMotoAuthCmpAdvTotalNum) {
        this.frnMotoAuthCmpAdvTotalNum = frnMotoAuthCmpAdvTotalNum;
    }

    public long getFrnMotoAuthCmpAdvTotalAmt() {
        return frnMotoAuthCmpAdvTotalAmt;
    }

    public void setFrnMotoAuthCmpAdvTotalAmt(long frnMotoAuthCmpAdvTotalAmt) {
        this.frnMotoAuthCmpAdvTotalAmt = frnMotoAuthCmpAdvTotalAmt;
    }

    public long getAuthCmpTotalNum() {
        return preAuthCmpTotalNum + preAuthCmpAdvTotalNum + motoAuthCmpTotalNum +
                motoAuthCmpAdvTotalNum;
    }

    public long getAuthCmpTotalAmt() {
        return preAuthCmpTotalAmt + preAuthCmpAdvTotalAmt + motoAuthCmpTotalAmt +
                motoAuthCmpAdvTotalAmt;
    }

    public long getFrnAuthCmpTotalNum() {
        return frnPreAuthCmpTotalNum + frnPreAuthCmpAdvTotalNum + frnMotoAuthCmpTotalNum +
                frnMotoAuthCmpAdvTotalNum;
    }

    public long getFrnAuthCmpTotalAmt() {
        return frnPreAuthCmpTotalAmt + frnPreAuthCmpAdvTotalAmt + frnMotoAuthCmpTotalAmt +
                frnMotoAuthCmpAdvTotalAmt;
    }


    public long getCouponSaleTotalAmt() {
        return saleCouponTotalAmt;
    }

    public void setCouponSaleTotalAmt(long saleCouponTotalAmt) {
        this.saleCouponTotalAmt = saleCouponTotalAmt;
    }

    public long getCouponSaleTotalNum() {
        return saleCouponTotalNum;
    }

    public void setCouponSaleTotalNum(long saleCouponTotalNum) {
        this.saleCouponTotalNum = saleCouponTotalNum;
    }

    //add denny
    public long getSaleFeeTotalAmt() {
        return saleFeeTotalAmt;
    }

    public void setSaleFeeTotalAmt(long saleFeeTotalAmt) {
        this.saleFeeTotalAmt = saleFeeTotalAmt;
    }

    public long getSaleFeeTotalNum() {
        return saleFeeTotalNum;
    }

    public void setSaleFeeTotalNum(long saleFeeTotalNum) {
        this.saleFeeTotalNum = saleFeeTotalNum;
    }

//    public long[][] getFeeTotal() { return feeTotal; }
//    public void setFeeTotal(long[][] feeTotal) { this.feeTotal = feeTotal; }

    public long[][] getTransTotalAmt() {
        return transTotalAmt;
    }

    public void setTransTotalAmt(long[][] transTotalAmt) {
        this.transTotalAmt = transTotalAmt;
    }


    /******************************** 上批总计数据库信息定义 ****************************************************/
    /**
     * 上批结算总计数据库信息
     *
     * @author Steven.W
     */
    class DbInfo {
        /**
         * 版本号
         */
        public static final int VER = 1;
        /**
         * 上批次交易汇总数据库名称
         */
        public static final String DB_NAME = "lastBatchTotal.db";
        /**
         * 上批次交易汇总表名
         */
        public static final String TABLE_NAME = "lastBatchTotal";
    }

    /******************************** 上批总计数据库句柄 ****************************************************/

    /**
     * 获取交易数据库句柄
     *
     * @return
     * @throws DbException
     */
    private static IDao<TransTotal> getLastBatchTotalDao() throws DbException {
        IDao<TransTotal> dao = FinancialApplication.getDb().getDb(DbInfo.VER, DbInfo.DB_NAME, DbInfo.TABLE_NAME,
                TransTotal.class, new IDbListener<TransTotal>() {

                    @Override
                    public IDao<TransTotal> onUpdate(IDao<TransTotal> arg0, int arg1, int arg2) {
                        try {
                            arg0.dropTable();
                            return FinancialApplication.getDb().getDb(DbInfo.VER, DbInfo.DB_NAME, DbInfo.TABLE_NAME,
                                    TransTotal.class, null);
                        } catch (DbException e) {

                            Log.e(TAG, "", e);
                        }
                        return null;

                    }
                });

        return dao;
    }

    /**
     * 获取上批总计，内卡借贷记总笔数总金额；外卡借贷记总笔数总金额
     *
     * @return
     */
    public static TransTotal getLastBatchToatlNumAndAmount() {
        try {
            IDao<TransTotal> dao = getLastBatchTotalDao();
            TransTotal data = dao.findLast();
            return data;
        } catch (DbException e) {
            String ex = e.getMessage();
            Log.e(TAG, "", e);
        }
        return null;
    }

    /**
     * 写上批总计记录
     *
     * @return
     */
    public boolean save() {
        try {
            IDao<TransTotal> dao = getLastBatchTotalDao();
            //dao.beginTransaction();
            dao.deleteAll();
            dao.save(this);
            //dao.endTransaction();
            return true;
        } catch (DbException e) {
            //DB#101(param error during save operation)
            e.printStackTrace();
            Log.e(TAG, "", e);
        }

        return false;
    }

    /**
     * 读上批总计
     *
     * @return
     */
    public static TransTotal readLastBatchTotal() {
        try {
            IDao<TransTotal> dao = getLastBatchTotalDao();
            return dao.findLast();
        } catch (DbException e) {
            Log.e(TAG, "", e);
        }

        return null;
    }

    public static TransTotal calcTotal() {
        TransTotal total = new TransTotal();
        long[] obj = null;
        /*obj = TransData.getRmbDebitNumAndAmount(); // 内卡借记总笔数和总金额
        total.setRmbDebitNum(obj[0]);
        total.setRmbDebitAmount(obj[1]);
        obj = TransData.getRmbCreditNumAndAmount(); // 内卡贷记总笔数和总金额
        total.setRmbCreditNum(obj[0]);
        total.setRmbCreditAmount(obj[1]);
        obj = TransData.getFrnDebitNumAndAmount();// 外卡借记总笔数和总金额
        total.setFrnDebitNum(obj[0]);
        total.setFrnDebitAmount(obj[1]);
        obj = TransData.getFrnCreditNumAndAmount(); // 外卡贷记总笔数和总金额
        total.setFrnCreditNum(obj[0]);
        total.setFrnCreditAmount(obj[1]);*/

        /***************** 内卡 **************************/

        long[] obj1 = null, obj2 = null, obj3 = null, obj4 = null, obj5 = null, obj6 = null,
                obj7 = null, obj8 = null, obj9 = null,
                objSetor = null, objTarik = null,objTarik2 = null,
                objTransfer = null,objTransfer2 = null, objPbb = null, objPajak = null,
                objBeacukai = null, objAnggaran = null,
                objPulsa = null, objTransferSesama,objTransferSesama2,
                objPdam, objPascabayar, objSamsat,objBpjsTKPendaftaran, objBpjsTKPembayaran;

//        // 电子现金消费
//        obj1 = TransData.getTransNumAndAmount(ETransType.EC_SALE.toString(), ETransStatus.NORMAL,
//                true);
//        total.setEcSaleTotalNum(obj1[0]);
//        total.setEcSaleTotalAmt(obj1[1]);

//        // 电子现金充值
//        obj1 = TransData.getTransNumAndAmount(ETransType.EC_CASH_LOAD.toString(),
//                ETransStatus.NORMAL, true);
//        total.setEcCashLoadTotalNum(obj1[0]);
//        total.setEcCashLoadTotalAmt(obj1[1]);
        // 电子现金充值撤销
//        obj1 = TransData.getTransNumAndAmount(ETransType.EC_CASH_LOAD_VOID.toString(),
//                ETransStatus.NORMAL, true);
//        total.setEcCashLoadVoidTotalNum(obj1[0]);
//        total.setEcCashLoadVoidTotalAmt(obj1[1]);
        // 电子现金脱机退货
//        obj1 = TransData.getTransNumAndAmount(ETransType.EC_REFUND.toString(), ETransStatus.NORMAL,
//                true);
//        total.setEcRefundTotalNum(obj1[0]);
//        total.setEcRefundTotalAmt(obj1[1]);
//        // 圈存类交易（非指定账户圈存）
//        obj1 = TransData.getTransNumAndAmount(ETransType.EC_TRANSFER_LOAD.toString(),
//                ETransStatus.NORMAL, true);
//        total.setEcLoadTotalNum(obj1[0]);
//        total.setEcLoadTotalAmt(obj1[1]);

        // Sale
        obj1 = TransData.getTransNumAndAmount(ETransType.SALE.toString(), ETransStatus.NORMAL, true);//消费
        obj2 = TransData.getTransNumAndAmount(ETransType.SETTLE_ADJUST_TIP.toString(), ETransStatus.NORMAL, true); //结算调整
        obj3 = TransData.getTransNumAndAmount(ETransType.EC_SALE.toString(), ETransStatus.NORMAL, true);
        obj4 = TransData.getTransNumAndAmount(ETransType.MOTO_SALE.toString(), ETransStatus.NORMAL, true);
        obj5 = TransData.getTransNumAndAmount(ETransType.RECURRING_SALE.toString(), ETransStatus.NORMAL, true);
        obj6 = TransData.getTransNumAndAmount(ETransType.QR_SALE.toString(), ETransStatus.NORMAL, true); //扫码消费
        obj7 = TransData.getTransNumAndAmount(ETransType.COUPON_SALE.toString(), ETransStatus.NORMAL, true);
        obj8 = TransData.getTransNumAndAmount(ETransType.DANA_QR_SALE.toString(), ETransStatus.NORMAL, true);
        obj9 = TransData.getTransNumAndAmount(ETransType.QR_INQUIRY.toString(), ETransStatus.NORMAL, true);

        //denny add
        objSetor = TransData.getTransNumAndAmount(ETransType.SETOR_TUNAI.toString(), ETransStatus.NORMAL, true);
        objTarik = TransData.getTransNumAndAmount(ETransType.TARIK_TUNAI.toString(), ETransStatus.NORMAL, true);
        objTarik2 = TransData.getTransNumAndAmount(ETransType.TARIK_TUNAI_2.toString(), ETransStatus.NORMAL, true);


        objTransfer = TransData.getTransNumAndAmount(ETransType.TRANSFER.toString(), ETransStatus.NORMAL, true);
        objTransfer2 = TransData.getTransNumAndAmount(ETransType.TRANSFER_2.toString(), ETransStatus.NORMAL, true);

        objPbb = TransData.getTransNumAndAmount(ETransType.PBB_PAY.toString(), ETransStatus.NORMAL, true);
        objPajak = TransData.getTransNumAndAmount(ETransType.DIRJEN_PAJAK.toString(), ETransStatus.NORMAL, true);
        objBeacukai = TransData.getTransNumAndAmount(ETransType.DIRJEN_BEA_CUKAI.toString(), ETransStatus.NORMAL, true);
        objAnggaran = TransData.getTransNumAndAmount(ETransType.DIRJEN_ANGGARAN.toString(), ETransStatus.NORMAL, true);
        objPulsa = TransData.getTransNumAndAmountPulsa(ETransType.INQ_PULSA_DATA.toString(), ETransStatus.NORMAL, true);
        //objPulsa = TransData.getTransNumAndAmount(ETransType.OVERBOOKING_PULSA_DATA.toString(), ETransStatus.NORMAL, true);
        objTransferSesama = TransData.getTransNumAndAmount(ETransType.OVERBOOKING.toString(), ETransStatus.NORMAL, true);
        objTransferSesama2 = TransData.getTransNumAndAmount(ETransType.OVERBOOKING_2.toString(), ETransStatus.NORMAL, true);

        //tri add
        objPdam = TransData.getTransNumAndAmount(ETransType.PDAM_INQUIRY.toString(), ETransStatus.NORMAL, true);
        objPascabayar = TransData.getTransNumAndAmount(ETransType.PASCABAYAR_INQUIRY.toString(), ETransStatus.NORMAL, true);

        objSamsat = TransData.getTransNumAndAmount(ETransType.E_SAMSAT.toString(), ETransStatus.NORMAL, true);
        objBpjsTKPendaftaran = TransData.getTransNumAndAmount(ETransType.BPJS_TK_PENDAFTARAN.toString(), ETransStatus.NORMAL, true);
        objBpjsTKPembayaran = TransData.getTransNumAndAmount(ETransType.BPJS_TK_PEMBAYARAN.toString(), ETransStatus.NORMAL, true);



        //add denny fee total
        long[] feeSetor = null, feeTarik = null, feeTarik2 = null, feeTransfer = null,feeTransfer2 = null, feePbb = null, feePajak = null, feeBeacukai = null, feeAnggaran = null,
                feePulsa = null, feeInfosaldo = null,feeInfosaldo2 = null, feeMinistatement = null, feeTrfSesama = null,
                feeTrfSesama2=null,
                feePdam, feePascabayar, feeSamsat, feeBPJSTkPendaftaran, feeBPJSTkPembayaran;

        feeSetor = TransData.getTransFeeNumAndAmount(ETransType.SETOR_TUNAI.toString(), ETransStatus.NORMAL, true);
        feeTarik = TransData.getTransFeeNumAndAmount(ETransType.TARIK_TUNAI.toString(), ETransStatus.NORMAL, true);
        feeTarik2 = TransData.getTransFeeNumAndAmount(ETransType.TARIK_TUNAI_2.toString(), ETransStatus.NORMAL, true);

        feeTransfer = TransData.getTransFeeNumAndAmount(ETransType.TRANSFER.toString(), ETransStatus.NORMAL, true);
        feeTransfer2 = TransData.getTransFeeNumAndAmount(ETransType.TRANSFER_2.toString(), ETransStatus.NORMAL, true);

        feePbb = TransData.getTransFeeNumAndAmount(ETransType.PBB_PAY.toString(), ETransStatus.NORMAL, true);
        feePajak = TransData.getTransFeeNumAndAmount(ETransType.DIRJEN_PAJAK.toString(), ETransStatus.NORMAL, true);
        feeBeacukai = TransData.getTransFeeNumAndAmount(ETransType.DIRJEN_BEA_CUKAI.toString(), ETransStatus.NORMAL, true);
        feeAnggaran = TransData.getTransFeeNumAndAmount(ETransType.DIRJEN_ANGGARAN.toString(), ETransStatus.NORMAL, true);
        feePulsa = TransData.getTransFeeNumAndAmount(ETransType.INQ_PULSA_DATA.toString(), TransData.ETransStatus.NORMAL, true);
        feeInfosaldo = TransData.getTransFeeNumAndAmount(ETransType.BALANCE_INQUIRY.toString(), ETransStatus.NORMAL, true);
        feeInfosaldo2 = TransData.getTransFeeNumAndAmount(ETransType.BALANCE_INQUIRY_2.toString(), ETransStatus.NORMAL, true);

        feeMinistatement = TransData.getTransFeeNumAndAmount(ETransType.MINISTATEMENT.toString(), ETransStatus.NORMAL, true);
        feeTrfSesama = TransData.getTransFeeNumAndAmount(ETransType.OVERBOOKING.toString(), ETransStatus.NORMAL, true);
        feeTrfSesama2 = TransData.getTransFeeNumAndAmount(ETransType.OVERBOOKING_2.toString(), ETransStatus.NORMAL, true);

        //tri add
        feePdam = TransData.getTransFeeNumAndAmount(ETransType.PDAM_INQUIRY.toString(), ETransStatus.NORMAL, true);
        feePascabayar = TransData.getTransFeeNumAndAmount(ETransType.PASCABAYAR_INQUIRY.toString(), ETransStatus.NORMAL, true);

        feeSamsat = TransData.getTransFeeNumAndAmount(ETransType.E_SAMSAT.toString(), ETransStatus.NORMAL, true);
        feeBPJSTkPendaftaran = TransData.getTransFeeNumAndAmount(ETransType.BPJS_TK_PENDAFTARAN.toString(), ETransStatus.NORMAL, true);
        feeBPJSTkPembayaran = TransData.getTransFeeNumAndAmount(ETransType.BPJS_TK_PEMBAYARAN.toString(), ETransStatus.NORMAL, true);



        total.setSaleFeeTotalNum(feeSetor[0] + feeTarik[0] + feeTarik2[0] + feeTransfer[0] +feeTransfer2[0]+ feePbb[0] + feePajak[0] + feeBeacukai[0] + feeAnggaran[0]
                + feePulsa[0] + feeInfosaldo[0] + feeInfosaldo2[0] + feeMinistatement[0] + feeTrfSesama[0]+ feeTrfSesama2[0] + feePdam[0] + feePascabayar[0] + feeSamsat[0] +
                feeBPJSTkPendaftaran[0] + feeBPJSTkPembayaran[0]);

        total.setSaleFeeTotalAmt(feeSetor[1] + feeTarik[1] + feeTarik2[1]+ feeTransfer[1]+ feeTransfer2[1] + feePbb[1] + feePajak[1] + feeBeacukai[1] + feeAnggaran[1]
                + feePulsa[1] + feeInfosaldo[1] + feeInfosaldo2[1] + feeMinistatement[1] + feeTrfSesama[1]+ feeTrfSesama2[1] + feePdam[1] + feePascabayar[1] + feeSamsat[1]
        +feeBPJSTkPendaftaran[1] + feeBPJSTkPembayaran[1]);

        //total Fee by trans
        long[][] feeTotal = new long[18][18];
        feeTotal[0] = feeSetor;
        feeTotal[1] = feeTarik;
        feeTotal[2] = feeTransfer;
        feeTotal[3] = feePbb;
        Long totalFeeMpnAmt, totalFeeMpnNum;
        totalFeeMpnNum = feePajak[0] + feeBeacukai[0] + feeAnggaran[0];
        totalFeeMpnAmt = feePajak[1] + feeBeacukai[1] + feeAnggaran[1];
        feeTotal[4] = new long[]{totalFeeMpnNum, totalFeeMpnAmt};
        feeTotal[5] = feePulsa;
        feeTotal[6] = feeInfosaldo;
        feeTotal[7] = feeMinistatement;
        feeTotal[8] = feeTrfSesama;
        feeTotal[9] = feePdam;
        feeTotal[10] = feePascabayar;
        feeTotal[11] = feeSamsat;
        //sandy
        feeTotal[12] = feeTransfer2;
        feeTotal[13] = feeTarik2;
        feeTotal[14] = feeTrfSesama2;
        feeTotal[15] = feeInfosaldo2;

        feeTotal[16] = feeBPJSTkPendaftaran;
        feeTotal[17] = feeBPJSTkPembayaran;



//        total.setFeeTotal(feeTotal);

        //total Amount by trans
        long[][] transTotal = new long[18][18];
        transTotal[0] = new long[]{objSetor[0], objSetor[1], feeSetor[1]};
        transTotal[1] = new long[]{objTarik[0], objTarik[1], feeTarik[1]};
        transTotal[2] = new long[]{objTransfer[0], objTransfer[1], feeTransfer[1]};
        transTotal[3] = new long[]{objPbb[0], objPbb[1], feePbb[1]};
        long totaltransMpnAmt, totaltransMpnNum;
        totaltransMpnNum = objPajak[0] + objBeacukai[0] + objAnggaran[0];
        totaltransMpnAmt = objPajak[1] + objBeacukai[1] + objAnggaran[1];
        transTotal[4] = new long[]{totaltransMpnNum, totaltransMpnAmt, totalFeeMpnAmt};
        transTotal[5] = new long[]{objPulsa[0], objPulsa[1], feePulsa[1]};
        transTotal[6] = new long[]{feeInfosaldo[0], 0, feeInfosaldo[1]};
        transTotal[7] = new long[]{feeMinistatement[0], 0, feeMinistatement[1]};
        transTotal[8] = new long[]{objTransferSesama[0], objTransferSesama[1], feeTrfSesama[1]};
        transTotal[9] = new long[]{objPdam[0], objPdam[1], feePdam[1]};
        transTotal[10] = new long[]{objPascabayar[0], objPascabayar[1], feePascabayar[1]};
        transTotal[11] = new long[]{objSamsat[0], objSamsat[1], feeSamsat[1]};
        //sandy
        transTotal[12] = new long[]{objTarik2[0], objTarik2[1], feeTarik2[1]};
        transTotal[13] = new long[]{objTransfer2[0], objTransfer2[1], feeTransfer2[1]};
        transTotal[14] = new long[]{objTransferSesama2[0], objTransferSesama2[1], feeTrfSesama2[1]};
        transTotal[15] = new long[]{feeInfosaldo2[0], 0, feeInfosaldo2[1]};

        transTotal[16] = new long[]{objBpjsTKPendaftaran[0], feeBPJSTkPendaftaran[1], objBpjsTKPendaftaran[1]};
        transTotal[17] = new long[]{objBpjsTKPembayaran[0], feeBPJSTkPembayaran[1], objBpjsTKPembayaran[1]};


        total.setTransTotalAmt(transTotal);

        //================================================================================================================================//
        total.setSaleTotalNum(obj1[0] + obj2[0] + obj3[0] + obj4[0] + obj5[0] + obj6[0] + obj8[0] + obj9[0] + objSetor[0] + objTarik[0]
                + objTarik2[0] + objTransfer[0]+ objTransfer2[0] + objPbb[0] + objPajak[0] + objBeacukai[0] + objAnggaran[0] + objPulsa[0]
                + objTransferSesama[0] + objTransferSesama2[0] + objPdam[0] + objPascabayar[0] + objSamsat[0]
        +objBpjsTKPendaftaran[0] + objBpjsTKPembayaran[0]);

        total.setSaleTotalAmt(obj1[1] + obj2[1] + obj3[1] + obj4[1] + obj5[1] + obj6[1] + obj8[1] + obj9[1] + objSetor[1]
                + objTarik[1]+ objTarik2[1] + objTransfer[1]+ objTransfer2[1] + objPbb[1] + objPajak[1] + objBeacukai[1] + objAnggaran[1] + objPulsa[1]
                + objTransferSesama[1] + objTransferSesama2[1]
                + objPdam[1] + objPascabayar[1] + objSamsat[1]
                + objBpjsTKPendaftaran[1] + objBpjsTKPembayaran[1]);

        //================================================================================================================================//

        total.setCouponSaleTotalNum(obj7[0]);
        total.setCouponSaleTotalAmt(obj7[1]);


        // Tips
        obj1 = TransData.getTipNumAndAmount(true);
        total.setSaleTotalTipNum(obj1[0]);
        total.setSaleTotalTipAmt(obj1[1]);

//        // 撤销
//        obj1 = TransData.getTransNumAndAmount(ETransType.VOID.toString(), ETransStatus.NORMAL,
//                true); //消费撤销
//        obj2 = TransData.getTransNumAndAmount(ETransType.QR_VOID.toString(), ETransStatus.NORMAL,
//                true); //扫码撤销
//        total.setVoidTotalNum(obj1[0] + obj2[0]);
//        total.setVoidTotalAmt(obj1[1] + obj2[1]);

        // Refund
        obj1 = TransData.getTransNumAndAmount(ETransType.REFUND.toString(), ETransStatus.NORMAL, true); //退货
        obj2 = TransData.getTransNumAndAmount(ETransType.MOTO_REFUND.toString(), ETransStatus.NORMAL, true); //退货
        obj3 = TransData.getTransNumAndAmount(ETransType.QR_REFUND.toString(), ETransStatus.NORMAL, true); //扫码退货
        total.setRefundTotalNum(obj1[0] + obj2[0] + obj3[0]);
        total.setRefundTotalAmt(obj1[1] + obj2[1] + obj3[1]);

//        // 预授权
//        obj1 = TransData.getTransNumAndAmount(ETransType.AUTH.toString(), ETransStatus.NORMAL,
//                true);
//        //预授权
//        total.setAuthTotalNum(obj1[0]);
//        total.setAuthTotalAmt(obj1[1]);
//        // 预授权撤销      这里似乎有问题！！！！！！！！！！！！！！！！！！！！！！
//        obj1 = TransData.getTransNumAndAmount(ETransType.AUTHCMVOID.toString(), ETransStatus
//                .NORMAL, true); //
//        total.setAuthVoidTotalNum(obj1[0]);
//        total.setAuthVoidTotalAmt(obj1[1]);
//
//        // 预授权完成请求撤销
//        obj1 = TransData.getTransNumAndAmount(ETransType.AUTHCMVOID.toString(), ETransStatus
//                .NORMAL, true); //预授权完成撤销
//        total.setAuthCMVoidTotalNum(obj1[0]);
//        total.setAuthCMVoidTotalAmt(obj1[1]);

        // Pre-Auth complete request
        obj1 = TransData.getTransNumAndAmount(ETransType.AUTHCM.toString(), ETransStatus.NORMAL, true); //预授权完成请求
        total.setPreAuthCmpTotalNum(obj1[0]);
        total.setPreAuthCmpTotalAmt(obj1[1]);

        // Pre-Auth complete advice
        obj1 = TransData.getTransNumAndAmount(ETransType.AUTH_SETTLEMENT.toString(), ETransStatus.NORMAL, true); //预授权完成通知
        total.setPreAuthCmpAdvTotalNum(obj1[0]);
        total.setPreAuthCmpAdvTotalAmt(obj1[1]);

        //MOTO-Auth complete
        obj1 = TransData.getTransNumAndAmount(ETransType.MOTO_AUTHCM.toString(), ETransStatus.NORMAL, true);
        total.setMotoAuthCmpTotalNum(obj1[0]);
        total.setMotoAuthCmpTotalAmt(obj1[1]);

        //MOTO-Auth complete advice
        obj1 = TransData.getTransNumAndAmount(ETransType.MOTO_AUTH_SETTLEMENT.toString(), ETransStatus.NORMAL, true);
        total.setMotoAuthCmpAdvTotalNum(obj1[0]);
        total.setMotoAuthCmpAdvTotalAmt(obj1[1]);


        // Offline(Offline settlement、Settlement adjust)
        obj1 = TransData.getTransNumAndAmount(ETransType.OFFLINE_SETTLE.toString(), ETransStatus.NORMAL, true); //离线结算
        obj2 = TransData.getTransNumAndAmount(ETransType.SETTLE_ADJUST.toString(), ETransStatus.NORMAL, true); //结算调整
        total.setOfflineTotalNum(obj1[0] + obj2[0]);
        total.setOfflineTotalAmt(obj1[1] + obj2[1]);

        //Installment
        obj1 = TransData.getTransNumAndAmount(ETransType.INSTAL_SALE.toString(), ETransStatus.NORMAL, true);
        total.setInstallTotalNum(obj1[0]);
        total.setInstallTotalAmt(obj1[1]);


        /***************** 外卡 **************************/

//        // 电子现金消费
//        obj1 = TransData.getTransNumAndAmount(ETransType.EC_SALE.toString(), ETransStatus.NORMAL, false); //电子现金消费
//        total.setFrnEcSaleTotalNum(obj1[0]);
//        total.setFrnEcSaleTotalAmt(obj1[1]);
//
//        // 电子现金充值
//        obj1 = TransData.getTransNumAndAmount(ETransType.EC_CASH_LOAD.toString(), ETransStatus.NORMAL, false); //现金充值
//        total.setFrnEcCashLoadTotalNum(obj1[0]);
//        total.setFrnEcCashLoadTotalAmt(obj1[1]);
//        // 电子现金充值撤销
//        obj1 = TransData.getTransNumAndAmount(ETransType.EC_CASH_LOAD_VOID.toString(), ETransStatus.NORMAL, false); //现金充值撤销
//        total.setFrnEcCashLoadVoidTotalNum(obj1[0]);
//        total.setFrnEcCashLoadVoidTotalAmt(obj1[1]);
//
//        // 电子现金脱机退货
//        obj1 = TransData.getTransNumAndAmount(ETransType.EC_REFUND.toString(), ETransStatus.NORMAL, false); //电子现金脱机退货
//        total.setFrnEcRefundTotalNum(obj1[0]);
//        total.setFrnEcRefundTotalAmt(obj1[1]);
//
//        // 圈存类交易（非指定账户圈存）
//        obj1 = TransData.getTransNumAndAmount(ETransType.EC_TRANSFER_LOAD.toString(), ETransStatus.NORMAL, false); //非指定账户圈存
//        total.setFrnEcLoadTotalNum(obj1[0]);
//        total.setFrnEcLoadTotalAmt(obj1[1]);

        //Refund Sale
        obj1 = TransData.getTransNumAndAmount(ETransType.SALE.toString(), ETransStatus.NORMAL, false);//消费
        obj2 = TransData.getTransNumAndAmount(ETransType.SETTLE_ADJUST_TIP.toString(), ETransStatus.NORMAL, false); //结算调整
        obj3 = TransData.getTransNumAndAmount(ETransType.EC_SALE.toString(), ETransStatus.NORMAL, false);
        obj4 = TransData.getTransNumAndAmount(ETransType.MOTO_SALE.toString(), ETransStatus.NORMAL, false);
        obj5 = TransData.getTransNumAndAmount(ETransType.RECURRING_SALE.toString(), ETransStatus.NORMAL, false);
        obj6 = TransData.getTransNumAndAmount(ETransType.QR_SALE.toString(), ETransStatus.NORMAL, false); //扫码消费
        total.setFrnSaleTotalNum(obj1[0] + obj2[0] + obj3[0] + obj4[0] + obj5[0] + obj6[0]);
        total.setFrnSaleTotalAmt(obj1[1] + obj2[1] + obj3[1] + obj4[1] + obj5[1] + obj6[1]);

        // Tips
        obj1 = TransData.getTipNumAndAmount(false);
        total.setFrnSaleTotalTipNum(obj1[0]);
        total.setFrnSaleTotalTipAmt(obj1[1]);

//        // 撤销
//        obj1 = TransData.getTransNumAndAmount(ETransType.VOID.toString(), ETransStatus.NORMAL, false); //消费撤销
//        obj5 = TransData.getTransNumAndAmount(ETransType.QR_VOID.toString(), ETransStatus.NORMAL, false); //扫码撤销
//        total.setFrnVoidTotalNum(obj1[0] + obj5[0]);
//        total.setFrnVoidTotalAmt(obj1[1] + obj5[1]);

        // Refund
        obj1 = TransData.getTransNumAndAmount(ETransType.REFUND.toString(), ETransStatus.NORMAL, false); //退货
        obj2 = TransData.getTransNumAndAmount(ETransType.MOTO_REFUND.toString(), ETransStatus.NORMAL, false); //退货
        obj3 = TransData.getTransNumAndAmount(ETransType.QR_REFUND.toString(), ETransStatus.NORMAL, false); //扫码退货
        total.setFrnRefundTotalNum(obj1[0] + obj2[0] + obj3[0]);
        total.setFrnRefundTotalAmt(obj1[1] + obj2[1] + obj3[1]);

//        // 预授权
//        obj1 = TransData.getTransNumAndAmount(ETransType.AUTH.toString(), ETransStatus.NORMAL, false); //预授权
//        total.setFrnAuthTotalNum(obj1[0]);
//        total.setFrnAuthTotalAmt(obj1[1]);
//        // 预授权撤销
//        obj1 = TransData.getTransNumAndAmount(ETransType.AUTHCMVOID.toString(), ETransStatus.NORMAL, false); //
//        total.setFrnAuthVoidTotalNum(obj1[0]);
//        total.setFrnAuthVoidTotalAmt(obj1[1]);
//        // 预授权完成请求撤销
//        obj1 = TransData.getTransNumAndAmount(ETransType.AUTHCMVOID.toString(), ETransStatus.NORMAL, false); //预授权完成撤销
//        total.setFrnAuthCMVoidTotalNum(obj1[0]);
//        total.setFrnAuthCMVoidTotalAmt(obj1[1]);

        // Pre-Auth complete request
        obj1 = TransData.getTransNumAndAmount(ETransType.AUTHCM.toString(), ETransStatus.NORMAL, false); //预授权完成请求
        total.setFrnPreAuthCmpTotalNum(obj1[0]);
        total.setFrnPreAuthCmpTotalAmt(obj1[1]);

        // Pre-Auth complete advice
        obj1 = TransData.getTransNumAndAmount(ETransType.AUTH_SETTLEMENT.toString(), ETransStatus.NORMAL, false); //预授权完成通知
        total.setFrnPreAuthCmpAdvTotalNum(obj1[0]);
        total.setFrnPreAuthCmpAdvTotalAmt(obj1[1]);

        //MOTO-Auth complete
        obj1 = TransData.getTransNumAndAmount(ETransType.MOTO_AUTHCM.toString(), ETransStatus.NORMAL, false);
        total.setFrnMotoAuthCmpTotalNum(obj1[0]);
        total.setFrnMotoAuthCmpTotalAmt(obj1[1]);

        //MOTO-Auth complete advice
        obj1 = TransData.getTransNumAndAmount(ETransType.MOTO_AUTH_SETTLEMENT.toString(), ETransStatus.NORMAL, false);
        total.setFrnMotoAuthCmpAdvTotalNum(obj1[0]);
        total.setFrnMotoAuthCmpAdvTotalAmt(obj1[1]);

        // Offline(Offline settlement、Settlement adjust)
        obj1 = TransData.getTransNumAndAmount(ETransType.OFFLINE_SETTLE.toString(), ETransStatus.NORMAL, false); //离线结算
        obj2 = TransData.getTransNumAndAmount(ETransType.SETTLE_ADJUST.toString(), ETransStatus.NORMAL, false); //结算调整
        total.setFrnOfflineTotalNum(obj1[0] + obj2[0]);
        total.setFrnOfflineTotalAmt(obj1[1] + obj2[1]);

        //total installment amount, add by richard 20170527
        obj = TransData.getTransNumAndAmount(ETransType.INSTAL_SALE.toString(), ETransStatus.NORMAL, false);
        total.setFrnInstallTotalNum(obj[0]);
        total.setFrnInstallTotalAmt(obj[1]);

        total.setMerchantID(FinancialApplication.getSysParam().get(SysParam.MERCH_ID));
        total.setTerminalID(FinancialApplication.getSysParam().get(SysParam.TERMINAL_ID));
        total.setOperatorID(TransContext.getInstance().getOperID());
        total.setBatchNo(FinancialApplication.getSysParam().get(SysParam.BATCH_NO));
        total.setDate(Device.getDate());
        total.setTime(Device.getTime());

        return total;
    }

}
