package com.pax.pay.constant;

public enum EUIParamKeys {
    /**
     * 提示信息1
     */
    PROMPT_1,
    /**
     * 提示信息2
     */
    PROMPT_2,
    /**
     * 输入1数据类型, {@link com.pax.pay.trans.action.EnterInfoAction.EInputType}
     */
    INPUT_TYPE_1,
    /**
     * 输入2数据类型, {@link com.pax.pay.trans.action.EnterInfoAction.EInputType}
     */
    INPUT_TYPE_2,
    /**
     * 输入1数据最大长度
     */
    INPUT_MAX_LEN_1,
    /**
     * 输入1数据最小长度
     */
    INPUT_MIN_LEN_1,
    /**
     * 输入2数据最大长度
     */
    INPUT_MAX_LEN_2,
    /**
     * 输入2数据最小长度
     */
    INPUT_MIN_LEN_2,
    /**
     * 显示内容
     */
    CONTENT,
    /**
     * 交易金额
     */
    TRANS_AMOUNT,
    /**
     * 交易日期
     */
    TRANS_DATE,

    /**
     * 寻卡界面类型
     */
    SEARCH_CARD_UI_TYPE,

    /**
     * 是否可直接撤销最后一笔交易
     */
    VOID_LAST_TRANS_UI,
    /**
     * 电子签名特征码
     */
    SIGN_FEATURE_CODE,

    /**
     * 列表1的值
     */
    ARRAY_LIST_1,
    /**
     * 列表2的值
     */
    ARRAY_LIST_2,
    /**
     * 支持扫码
     */
    SUPPORT_SCAN,

    /**
     * 导航栏抬头
     */
    NAV_TITLE,
    /**
     * 导航栏是否显示返回按钮
     */
    NAV_BACK,
    /**
     * 寻卡模式
     */
    CARD_SEARCH_MODE,
    /**
     * 寻卡界面显示授权码
     */
    AUTH_CODE,
    /**
     * 寻卡界面刷卡提醒
     */
    SEARCH_CARD_PROMPT,
    /**
     * 界面定时器
     */
    TIKE_TIME,
    /**
     * 卡号
     */
    PANBLOCK,
    /**
     * 凭密
     */
    SUPPORTBYPASS,
    /**
     * 输密类型
     */
    ENTERPINTYPE,
    /**
     * 
     */
    OPTIONS,
    RSA_PIN_KEY,
    /**
     * 输入内容自动补零
     */
    INPUT_AUTH_ZERO,
    /**
     * 交易查询界面支持交易
     */
    SUPPORT_DO_TRANS, 
    
    /**
     * RSAPinKey 参数
     */
    RSA_PIN_KEY_MODULUSLEN,
    RSA_PIN_KEY_MODULUS,
    RSA_PIN_KEY_EXPONENT,
    RSA_PIN_KEY_ICC_RANDAM,
    
    ;
}
