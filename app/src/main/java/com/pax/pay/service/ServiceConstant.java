package com.pax.pay.service;

public class ServiceConstant {
    /**
     * 交易类型
     */
    public static final String TRANS_TYPE = "transType";

    /**
     * 应用ID
     */
    public static final String APP_ID = "appId";
    /**
     * 应答码
     */
    public static final String RSP_CODE = "rspCode";
    /**
     * 应答信息
     */
    public static final String RSP_MSG = "rspMsg";
    /**
     * 商户名称
     */
    public static final String MERCH_NAME = "merchName";
    /**
     * 商户ID
     */
    public static final String MERCH_ID = "merchId";

    /**
     * 终端编号
     */
    public static final String TERM_ID = "termId";
    /**
     * 卡号
     */
    public static final String CARD_NO = "cardNo";
    /**
     * 凭证号
     */
    public static final String VOUCHER_NO = "voucherNo";
    /**
     * 付款凭证号(扫码)
     */
    public static final String C2B_VOUCHER_NO = "c2bVoucherNo";
    /**
     * 支付码号(扫码)
     */
    public static final String C2B = "c2b";
    /**
     * 批次号
     */
    public static final String BATCH_NO = "batchNo";
    /**
     * 发卡行号
     */
    public static final String ISSER_CODE = "isserCode";
    /**
     * 收单行号
     */
    public static final String ACQ_CODE = "acqCode";
    /**
     * 授权码
     */
    public static final String AUTH_NO = "authNo";
    /**
     * 参考号
     */
    public static final String REF_NO = "refNo";
    /**
     * 交易时间
     */
    public static final String TRANS_TIME = "transTime";
    /**
     * 交易日期
     */
    public static final String TRANS_DATE = "transDate";
    /**
     * 交易金额
     */
    public static final String TRANS_AMOUNT = "transAmount";
    /**
     * 支付码号
     */
    public static final String TRANS_C2B = "c2b";
    /**
     * 原授权码
     */
    public static final String ORIG_AUTH_NO = "origAuthNo";
    /**
     * 原凭证号
     */
    public static final String ORIG_VOUCHER_NO = "origVoucherNo";
    /**
     * 原参考号
     */
    public static final String ORIG_REF_NO = "origRefNo";
    /**
     * 原交易日期
     */
    public static final String ORIG_DATE = "origDate";
    /**
     * 原付款凭证号(扫码)
     */
    public static final String ORIG_C2B_VOUCHER_NO = "origC2bVoucherNo";

    // ////////////////////////////////////////////////////////////////////
    /**
     * 主管密码
     */
    public static final String MNG_PD = "managerPwd";
    /**
     * 操作员号
     */
    public static final String OPER_ID = "operId";
    // /////////////////////////////////////////////////////////////////////////////
    /**
     * 图片
     */
    public static final String PRN_BMP = "bitmap";
    // //////////////////////////////交易类型定义//////////////////////////////////////////
    // //////////////////////////////交易类//////////////////////////////////////////

    /**
     * 消费
     */
    public static final String TRANS_SALE = "SALE";
    /**
     * 撤销
     */
    public static final String TRANS_VOID = "VOID";
    /**
     * 退货
     */
    public static final String TRANS_REFUND = "REFUND";
    /**
     * 预授权
     */
    public static final String TRANS_AUTH = "AUTH";
    /**
     * 预授权撤销
     */
    public static final String TRANS_AUTH_VOID = "AUTH_VOID";
    /**
     * 预授权完成请求
     */
    public static final String TRANS_AUTH_CM = "AUTH_CM";
    /**
     * 预授权完成请求撤销
     */
    public static final String TRANS_AUTH_CM_VOID = "AUTH_CM_VOID";
    /**
     * 预授权完成通知
     */
    public static final String TRANS_AUTH_ADV = "AUTH_ADV";
    /**
     * 余额查询
     */
    public static final String TRANS_BALANCE = "BALANCE";
    /**
     * 扫码消费
     */
    public static final String TRANS_QR_SALE = "QR_SALE";
    /**
     * 扫码撤销
     */
    public static final String TRANS_QR_VOID = "QR_VOID";
    /**
     * 扫码退货
     */
    public static final String TRANS_QR_REFUND = "QR_REFUND";

    // //////////////////////////////管理类//////////////////////////////////////////
    /**
     * 签到
     */
    public static final String TRANS_LOGON = "LOGON";
    /**
     * 签退
     */
    public static final String TRANS_LOGOFF = "LOGOFF";
    /**
     * 结算
     */
    public static final String TRANS_SETTLE = "SETTLE";
    /**
     * 重打最后一笔
     */
    public static final String TRANS_PRN_LAST = "PRN_LAST";
    /**
     * 重打任意笔
     */
    public static final String TRANS_PRN_ANY = "PRN_ANY";
    /**
     * 打印交易明细
     */
    public static final String TRANS_PRN_DETAIL = "PRN_DETAIL";
    /**
     * 打印交易汇总
     */
    public static final String TRANS_PRN_TOTAL = "PRN_TOTAL";
    /**
     * 重打结算单
     */
    public static final String TRANS_PRN_LAST_BATCH = "PRN_LAST_BATCH";
    /**
     * 获取卡号
     */
    public static final String TRANS_GET_CARD_NO = "GET_CARD_NO";
    /**
     * 终端参数设置
     */
    public static final String TRANS_SETTING = "SETTING";
    /**
     * 图片打印
     */
    public static final String PRN_BITMAP = "PRN_BITMAP";
    /**
     * 交易查询
     */
    public static final String TRANS_QUERY = "TRANS_QUERY";

}
