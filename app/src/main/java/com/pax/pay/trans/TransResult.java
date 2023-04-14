package com.pax.pay.trans;

import android.content.Context;
import android.content.res.Resources;

import com.pax.up.bjb.R;

public class TransResult {
    /**
     * 交易成功
     */
    public static final int SUCC = 0;
    /**
     * 超时
     */
    public static final int ERR_TIMEOUT = -1;
    /**
     * 连接超时
     */
    public static final int ERR_CONNECT = -2;
    /**
     * 发送失败
     */
    public static final int ERR_SEND = -3;
    /**
     * 接收失败
     */
    public static final int ERR_RECV = -4;
    /**
     * 打包失败
     */
    public static final int ERR_PACK = -5;
    /**
     * 解包失败
     */
    public static final int ERR_UNPACK = -6;
    /**
     * 非法包
     */
    public static final int ERR_BAG = -7;
    /**
     * 解包mac错
     */
    public static final int ERR_MAC = -8;
    /**
     * 处理码不一致
     */
    public static final int ERR_PROC_CODE = -9;
    /**
     * 消息类型不一致
     */
    public static final int ERR_MSG = -10;
    /**
     * 交易金额不符
     */
    public static final int ERR_TRANS_AMT = -11;
    /**
     * 流水号不一致
     */
    public static final int ERR_TRACE_NO = -12;
    /**
     * 终端号不一致
     */
    public static final int ERR_TERM_ID = -13;
    /**
     * 商户号不一致
     */
    public static final int ERR_MERCH_ID = -14;
    /**
     * 无交易
     */
    public static final int ERR_NO_TRANS = -15;
    /**
     * 无原始交易
     */
    public static final int ERR_NO_ORIG_TRANS = -16;
    /**
     * 此交易已撤销
     */
    public static final int ERR_HAS_VOID = -17;
    /**
     * 此交易不可撤销
     */
    public static final int ERR_VOID_UNSUPPORT = -18;
    /**
     * 打开通讯口错误
     */
    public static final int ERR_COMM_CHANNEL = -19;
    /**
     * 失败
     */
    public static final int ERR_HOST_REJECT = -20;
    /**
     * 交易终止（终端不需要提示信息）
     */
    public static final int ERR_ABORTED = -21;
    /**
     * 预处理相关 终端未签到
     */
    public static final int ERR_NOT_LOGON = -22;
    /**
     * 预处理相关 交易笔数超限，立即结算
     */
    public static final int ERR_NEED_SETTLE_NOW = -23;
    /**
     * 预处理相关 交易笔数超限，稍后结算
     */
    public static final int ERR_NEED_SETTLE_LATER = -24;
    /**
     * 预处理相关 存储空间不足
     */
    public static final int ERR_NO_FREE_SPACE = -25;
    /**
     * 预处理相关 终端不支持该交易
     */
    public static final int ERR_NOT_SUPPORT_TRANS = -26;
    /**
     * 卡号不一致
     */
    public static final int ERR_CARD_NO = -27;
    /**
     * 密码错误
     */
    public static final int ERR_PASSWORD = -28;
    /**
     * 参数错误
     */
    public static final int ERR_PARAM = -29;

    /**
     * 终端批上送未完成
     */
    public static final int ERR_BATCH_UP_NOT_COMPLETED = -31;
    /**
     * 金额超限
     */
    public static final int ERR_AMOUNT = -33;
    /**
     * 平台批准卡片拒绝
     */
    public static final int ERR_CARD_DENIED = -34;
    /**
     * 纯电子现金联机拒绝
     */
    public static final int ERR_PURE_CARD_CAN_NOT_ONLINE = -35;
    /**
     * 此交易不可调整
     */
    public static final int ERR_ADJUST_UNSUPPORT = -36;
    /**
     * 预授权类交易不能联机
     */
    public static final int ERR_AUTH_TRANS_CAN_NOT_USE_PURE_CARD = -37;
    /**
     * 无有效交易
     */
    public static final int ERR_NO_VALID_TRANS = -38;
    /**
     * 工作密钥长度错误
     */
    public static final int ERR_TWK_LENGTH = -39;

    /**
     * 后台应答不为 00  //added by brianWang
     */
    public static final int ERR_REAPONSE = -40;

    /**
     * 往PED里写RSA PUK 失败
     */
    public static final int ERR_WRITE_RSA_PUK = -41;

    /**
     * 往ped里存密钥失败
     */
    public static final int ERR_TMK_TO_PED = -42;

    /**
     * TMK  kcv值不正确
     */
    public static final int ERR_TMK_KCV = -43;

    /**
     * 应用列表无应用
     */
    public static final int ERR_NO_APP = -44;

    /**
     * Fallback 降级交易
     */
    public static final int FALL_BACK = -45;
    /**
     * Settle adjust amount error.
     */
    public static final int ERR_ADJUST_AMOUNT = -46;

    /**
     * 还有交易流水，先结算(Added by Steven.T 2017-6-12 11:36:31)
     */
    public static final int ERR_HAVE_TRANS = -47;

    /**
     * 非接设备预处理失败
     */
    public static final int ERR_CLSS_PRE_PROC = -48;

    /**
     * Invalid EMV QR code.
     */
    public static final int ERR_INVALID_EMV_QR = -49;

    /***
     * Invalid coupon number.
     */
    public static final int ERR_COUPON_NUM = -50;

    //tri
    public static final int ERR_INVALID_RESPONSE_DATA = -51;




    public static String getMessage(Context context, int ret) {
        String message = "";
        Resources resource = context.getResources();
        switch (ret) {
            case SUCC:
                message = resource.getString(R.string.trans_succ);
                break;
            case ERR_TIMEOUT:
                message = resource.getString(R.string.err_timeout);
                break;
            case ERR_CONNECT:
                message = resource.getString(R.string.err_connect);
                break;
            case ERR_SEND:
                message = resource.getString(R.string.err_send);
                break;
            case ERR_RECV:
                message = resource.getString(R.string.err_recv);
                break;
            case ERR_PACK:
                message = resource.getString(R.string.err_pack);
                break;
            case ERR_UNPACK:
                message = resource.getString(R.string.err_unpack);
                break;
            case ERR_BAG:
                message = resource.getString(R.string.err_bag);
                break;
            case ERR_MAC:
                message = resource.getString(R.string.err_mac);
                break;
            case ERR_PROC_CODE:
                message = resource.getString(R.string.err_proc_code);
                break;
            case ERR_MSG:
                message = resource.getString(R.string.err_msg);
                break;
            case ERR_TRANS_AMT:
                message = resource.getString(R.string.err_trans_amt);
                break;
            case ERR_TRACE_NO:
                message = resource.getString(R.string.err_trace_no);
                break;
            case ERR_TERM_ID:
                message = resource.getString(R.string.err_term_id);
                break;
            case ERR_MERCH_ID:
                message = resource.getString(R.string.err_merch_id);
                break;
            case ERR_NO_TRANS:
                message = resource.getString(R.string.err_no_trans);
                break;
            case ERR_NO_ORIG_TRANS:
                message = resource.getString(R.string.err_no_orig_trans);
                break;
            case ERR_HAS_VOID:
                message = resource.getString(R.string.err_has_void);
                break;
            case ERR_VOID_UNSUPPORT:
                message = resource.getString(R.string.err_void_unsupport);
                break;
            case ERR_COMM_CHANNEL:
                message = resource.getString(R.string.err_comm_channel);
                break;
            case ERR_HOST_REJECT:
                message = resource.getString(R.string.err_host_reject);
                break;
            case ERR_NOT_LOGON:
                message = resource.getString(R.string.err_not_logon);
                break;
            case ERR_NEED_SETTLE_NOW:
                message = resource.getString(R.string.err_need_settle_now);
                break;
            case ERR_NEED_SETTLE_LATER:
                message = resource.getString(R.string.err_need_settle_later);
                break;
            case ERR_NO_FREE_SPACE:
                message = resource.getString(R.string.err_no_free_space);
                break;
            case ERR_NOT_SUPPORT_TRANS:
                message = resource.getString(R.string.err_not_support_trans);
                break;
            case ERR_BATCH_UP_NOT_COMPLETED:
                message = resource.getString(R.string.err_batch_up_break_need_continue);
                break;
            case ERR_CARD_NO:
                message = resource.getString(R.string.err_original_cardno);
                break;
            case ERR_PASSWORD:
                message = resource.getString(R.string.err_manager_password);
                break;
            case ERR_PARAM:
                message = resource.getString(R.string.err_param);
                break;
            case ERR_AMOUNT:
                message = resource.getString(R.string.err_amount);
                break;
            case ERR_CARD_DENIED:
                message = resource.getString(R.string.err_card_denied);
                break;
            case ERR_PURE_CARD_CAN_NOT_ONLINE:
                message = resource.getString(R.string.emv_err_pure_card_can_not_online);
                break;
            case ERR_ADJUST_UNSUPPORT:
                message = resource.getString(R.string.err_adjust_unsupport);
                break;
            case ERR_AUTH_TRANS_CAN_NOT_USE_PURE_CARD:
                message = resource.getString(R.string.emv_err_auth_trans_can_not_use_pure_card);
                break;
            case ERR_NO_VALID_TRANS:
                message = resource.getString(R.string.err_no_valid_trans);
                break;
            case ERR_TWK_LENGTH:
                message = resource.getString(R.string.err_twk_length);
                break;
            case ERR_REAPONSE:    //added by brianWang
                message = resource.getString(R.string.err_response);
                break;
            case ERR_WRITE_RSA_PUK:
                message = resource.getString(R.string.err_write_rsa_puk);
                break;
            case ERR_TMK_TO_PED:
                message = resource.getString(R.string.err_write_to_ped);
                break;
            case ERR_TMK_KCV:
                message = resource.getString(R.string.err_tmk_kcv);
                break;
            case ERR_NO_APP:
                message = resource.getString(R.string.no_app);
                break;
            case FALL_BACK:
                message = resource.getString(R.string.fall_back_treatment);
                break;
            case ERR_ADJUST_AMOUNT:
                message = context.getString(R.string.settle_adjust_amount_error);
                break;
            case ERR_HAVE_TRANS:  //Added by Steven.T 2017-6-12 11:38:13
                message = resource.getString(R.string.set_settle);
                break;
            case ERR_CLSS_PRE_PROC:
                message = resource.getString(R.string.err_clss_preproc_fail);
                break;
            case ERR_INVALID_EMV_QR:
                message = resource.getString(R.string.err_invalid_qr);
                break;
            case ERR_COUPON_NUM:
                message = resource.getString(R.string.err_coupon_num);
                break;
            case ERR_INVALID_RESPONSE_DATA:
                message = resource.getString(R.string.err_invalid_data);
                break;
            default:
                message = resource.getString(R.string.err_undefine) + "[" + ret + "]";
                break;
        }
        return message;
    }
}
