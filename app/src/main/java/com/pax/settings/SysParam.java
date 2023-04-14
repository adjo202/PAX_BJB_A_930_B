/*
 * ============================================================================
 * COPYRIGHT
 *               Pax CORPORATION PROPRIETARY INFORMATION
 *    This software is supplied under the terms of a license agreement or
 *    nondisclosure agreement with Pax Corporation and may not be copied
 *    or disclosed except in accordance with the terms in that agreement.
 *       Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 *  Module Date: 2017-5-22 5:37
 *  Module Author: liliang
 *  Description:
 *  ============================================================================
 */
package com.pax.settings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.pax.pay.app.FinancialApplication;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.utils.AppLog;
import com.pax.settings.currency.Currency;
import com.pax.up.bjb.R;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

@SuppressLint("SdCardPath")
public class SysParam {
    private static final String TAG = "SysParam";
    private static final String INIT_FILE_NAME = "sys_param.p";
    /**
     * 系统管理员ID
     */
    public static final String OPER_SYS = "99";
    /**
     * 主管ID
     */
    public static final String OPER_MANAGE = "00";

    /**
     * 是否是BCTC测试 BCTC测试案例与实际使用有很多不一致的地方, 所以得加以区分
     * BCTC测试用
     */
    public static final String BCTC_TEST_SWITCH = getString(R.string.pref_bctc_test);


    /******************************************************************
     * 商户参数
     ******************************************************************/
    /**
     * 商户号
     */
    public static final String MERCH_ID = getString(R.string.pref_merch_id);
    /**
     * 终端号
     */
    public static final String TERMINAL_ID = getString(R.string.pref_terminal_id);
    /**
     * 中文商户名
     */
    public static final String MERCH_CN = getString(R.string.pref_merch_cn);
    /**
     * 英文商户名
     */
    public static final String MERCH_EN = getString(R.string.pref_merch_en);

    // add abdul
    public static final String ADDR1 = getString(R.string.pref_addr1_en);

    public static final String ADDR2 = getString(R.string.pref_addr2_en);

    //sandy
    public static final String SHOW_ADMIN_FEE = getString(R.string.pref_show_admin_fee);

    public static final String TERMINAL_DATE_TIME = getString(R.string.pref_terminal_time_date);
    /**
     * 收单机构
     */
    public static final String ACQUIRER = getString(R.string.pref_acquirer);

    /******************************************************************
     * 交易管理
     ******************************************************************/
    /**
     * 传统交易开关
     */
    public static final String TTS = getString(R.string.pref_tts);
    /**
     * 消费开关_传统交易
     */
    public static final String TTS_SALE = getString(R.string.pref_tts_sale);
    /**
     * 消费撤销开关_传统交易
     */
    public static final String TTS_VOID = getString(R.string.pref_tts_void);
    /**
     * 退货开关_传统交易
     */
    public static final String TTS_REFUND = getString(R.string.pref_tts_refund);
    /**
     * 余额查询开关_传统交易
     */
    public static final String TTS_BALANCE = getString(R.string.pref_tts_balance);
    /**
     * 预授权开关_传统交易
     */
    public static final String TTS_PREAUTH = getString(R.string.pref_tts_preauth);
    /**
     * 预授权撤销开关_传统交易
     */
    public static final String TTS_PAVOID = getString(R.string.pref_tts_pavoid);
    /**
     * 预授权完成请求开关_传统交易
     */
    public static final String TTS_PACREQUEST = getString(R.string.pref_tts_pacrequest);
    /**
     * 预授权完成通知开关_传统交易
     */
    public static final String TTS_PACADVISE = getString(R.string.pref_tts_pacadvice);
    /**
     * 预授权完成撤销开关_传统交易
     */
    public static final String TTS_PACVOID = getString(R.string.pref_tts_pacvoid);
    /**
     * 离线结算开关_传统交易
     */
    public static final String TTS_OFFLINE_SETTLE = getString(R.string.pref_tts_offline_settle);
    /**
     * 结算调整开关_传统交易
     */
    public static final String TTS_ADJUST = getString(R.string.pref_tts_adjust);

    /**
     * 电子现金交易开关
     */
    public static final String ECTS = getString(R.string.pref_ects);
    /**
     * 电子现金消费
     */
    public static final String ECTS_SALE = getString(R.string.pref_ects_sale);
    /**
     * 指定账户圈存开关_电子现金
     */
    public static final String ECTS_LOAD = getString(R.string.pref_ects_load);
    /**
     * 非指定账户圈存开关_电子现金
     */
    public static final String ECTS_TLOAD = getString(R.string.pref_ects_tload);
    /**
     * 现金充值开关_电子现金
     */
    public static final String ECTS_CALOAD = getString(R.string.pref_ects_caload);
    /**
     * 现金充值撤销开关_电子现金
     */
    public static final String ECTS_CALOADVOID = getString(R.string.pref_ects_caloadvoid);
    /**
     * 脱机退货开关_电子现金
     */
    public static final String ECTS_REFUND = getString(R.string.pref_ects_refund);
    /**
     * 余额查询_电子现金
     */
    public static final String ECTS_QUERY = getString(R.string.pref_ects_query);

    /**
     * 扫码类交易
     */
    /**
     * 扫码交易开关
     */
    public static final String SCTS = getString(R.string.pref_scts);
    /**
     * 扫码消费_扫码交易
     */
    public static final String SCTS_SALE = getString(R.string.pref_scts_sale);
    /**
     * 扫码撤销_扫码交易
     */
    public static final String SCTS_VOID = getString(R.string.pref_scts_void);
    /**
     * 扫码退货_扫码交易
     */
    public static final String SCTS_REFUND = getString(R.string.pref_scts_refund);

    /**
     * 其他交易开关
     */
    public static final String OTTS = "其他交易开关";
    public static final String OTTS_INSTALLMENT = "分期付款消费";
    public static final String OTTS_INSTALLMENTVOID = "分期付款消费撤销";
    public static final String OTTS_IC_SCRIPT_PROCESS_RESULT_ADVICE = "IC卡脚本处理结果通知";
    public static final String OTTS_MOTO = "MOTO";
    public static final String OTTS_RECURRING = "Recurring";

    /**
     * 主界面交易类型
     */
    /**
     * 主界面交易类型
     */
    public static final String HOME_TRANS = getString(R.string.pref_home_trans);


    /**
     * 交易输密控制
     */
    /**
     * 消费撤销输密
     */
    public static final String IPTC_VOID = getString(R.string.pref_sale_void_pwd);
    /**
     * 预授权撤销输密
     */
    public static final String IPTC_PAVOID = getString(R.string.pref_preauth_void_pwd);
    /**
     * 预授权完成撤销输密
     */
    public static final String IPTC_PACVOID = getString(R.string.pref_preauth_cmp_void_pwd);
    /**
     * 预授权完成(请求)输密
     */
    public static final String IPTC_PAC = getString(R.string.pref_preauth_cmp_pwd);

    /**
     * 交易刷卡控制
     */
    /**
     * 消费撤销刷卡
     */
    public static final String UCTC_VOID = getString(R.string.pref_sale_void_swipe_card);
    /**
     * 预授权完成撤销刷卡
     */
    public static final String UCTC_PACVOID = getString(R.string.pref_pac_void_swipe_card);


    /**
     * 结算交易控制
     */
    /**
     * 结算后自动签退
     */
    public static final String SETTLETC_AUTOLOGOUT = getString(R.string.pref_settle_auto_logout);
    /**
     * 结算打印明细提示
     */
    public static final String SETTLETC_PRNDETAIL = getString(R.string.pref_settle_print_detail);


    /**
     * 离线交易控制
     */
    /**
     * 离线上送方式
     */
    public static final String OFFLINETC_UPLOAD_TYPE = getString(R.string
            .pref_offline_upload_type);
    /**
     * 离线上送次数
     */
    public static final String OFFLINETC_UPLOADTIMES = getString(R.string
            .pref_offline_upload_times);
    /**
     * 自动上送累计笔数
     */
    public static final String OFFLINETC_UPLOADNUM = getString(R.string
            .pref_offline_auto_upload_num);

    /**
     * 其它交易控制
     */
    /**
     * 支持EMV芯片卡交易
     */
    public static final String OTHTC_SUPP_EMV = getString(R.string.pref_supp_emv);
    /**
     * 判断EMV卡方式
     */
    public static final String OTHTC_EMV_OPR = getString(R.string.pref_judge_emv_mode);

    /**
     * 支持汇率转换
     */
    public static final String OTHTC_SUPP_EXRATE = getString(R.string.pref_supp_rate_exch);
    /**
     * 是否强制联机
     */
    public static final String OTHTC_FORCE_ONLINE = getString(R.string.pref_force_online);

    /**
     * 撤销退货类交易输入主管密码
     */
    public static final String OTHTC_VERIFY = getString(R.string.pref_mas_opr_veirfy_void_refund);
    /**
     * 磁道加密设置
     */
    public static final String OTHTC_TRACK_ENCRYPT = getString(R.string.pref_track_encrypt);
    /**
     * 允许手输卡号
     */
    public static final String OTHTC_KEYIN = getString(R.string.pref_allow_manual_card_no);
    /**
     * 支持电子签名
     */
    public static final String OTHTC_SINGATURE = getString(R.string.pref_supp_signature);
    /**
     * 最大退货金额
     */
    public static final String OTHTC_REFUNDLIMT = getString(R.string.pref_refund_limit_amount);

    /**
     * 免签免密交易控制
     */
    /**
     * 非接交易通道开关
     */
    public static final String QUICK_PASS_TRANS_SWITCH = getString(R.string.pref_quick_pass_switch);
    /**
     * CDCVM标识
     */
    public static final String QUICK_PASS_TRANS_CDCVM_FLAG = getString(R.string
            .pref_quick_pass_cdcvm_flag);
    /**
     * 免签免密限额
     */
    public static final String QUICK_PASS_TRANS_SIGN_PIN_FREE_AMOUNT = getString(R.string
            .pref_quick_pass_sign_pin_free_limit);
    /**
     * 闪卡当笔重刷处理时间
     */
    public static final String QUICK_PASS_TRANS_BRUSH_TIMES = getString(R.string
            .pref_quick_pass_trans_brush_times);
    /**
     * 闪卡记录可处理时间
     */
    public static final String QUICK_PASS_TRANS_TIMES = getString(R.string
            .pref_quick_pass_trans_times);

    /******************************************************************
     * 系统参数
     ******************************************************************/
    /**
     * 流水号
     */
    public static final String TRANS_NO = getString(R.string.pref_trans_no);
    /**
     * 批次号
     */
    public static final String BATCH_NO = getString(R.string.pref_batch_no);
    /**
     * 收单行信息打印
     */
    public static final String ACQUIRE_PRINT = getString(R.string.pref_acquire_print);
    /**
     * 发卡行信息打印
     */
    public static final String ISSUER_PRINT = getString(R.string.pref_issure_print);
    /**
     * CUR_YEAR
     */
    public static final String CUR_YEAR = getString(R.string.pref_cur_year);// 暂未使用
    /**
     * 商行代码
     */
    public static final String MERCH_MERCHCODE = getString(R.string.pref_merch_merchcode);// 暂未使用
    /**
     * 地区代码
     */
    public static final String MERCH_AREACODE = getString(R.string.pref_merch_areacode);
    /**
     * 打印凭单联数
     */
    public static final String PRINT_VOUCHER_NUM = getString(R.string.pref_voucher_copy_num);
    /**
     * 打印机灰度
     */
    public static final String PRINT_GRAY = getString(R.string.pref_printer_gray_level);
    /**
     * 签购单打印英文
     */
    public static final String VOUCHER_PRINTEN = getString(R.string.pref_voucher_print_en);
    /**
     * 重发次数
     */
    public static final String RESEND_TIMES = getString(R.string.pref_resend_times);
    /**
     * 签名重发次数
     */
    public static final String RESEND_SIG_TIMES = getString(R.string.pref_sign_resend_times);
    /**
     * 冲正重发次数
     */
    public static final String REVERSL_CTRL = getString(R.string.pref_reversal_resend_times);
    /**
     * 最大交易笔数
     */
    public static final String MAX_TRANS_COUNT = getString(R.string.pref_max_trans_num);
    /**
     * 消费模式
     */
    public static final String TIP_MODE = getString(R.string.pref_tip_mode);
    /**
     * 支持小费
     */
    public static final String SUPPORT_TIP = getString(R.string.pref_supp_tip);
    /**
     * 小费比例
     */
    public static final String TIP_RATE = getString(R.string.pref_tip_rate);
    /**
     * 是否支持脱机交易
     */
    public static final String SUPPORT_OFFLINE_TRANS = getString(R.string
            .pref_supp_offline_trans);
    /**
     * 消费遮罩卡号
     */
    public static final String SALE_MASK_CARD_NUMBER = getString(R.string.pref_sale_mask_card_no);
    /**
     * 预授权遮罩卡号
     */
    public static final String PREAUTH_MASK_CARD_NUMBER = getString(R.string
            .pref_preauth_mask_card_no);
    /**
     * 电子现金遮罩卡号
     */
    public static final String ECASH_MASK_CARD_NUMBER = getString(R.string
            .pref_ecash_mask_card_no);
    /**
     * 外置非接设置
     */
    public static final String EX_CONTACTLESS_SET = getString(R.string.pref_ex_contactless_set);
    /**
     * 外置非接设置选择
     */
    public static final String EX_CONTACTLESS_CHOOSE = getString(R.string
            .pref_ex_contactless_choose);
    /**
     * 非接设备串口号
     */
    public static final String EX_CONTACTLESS_SERIAL = getString(R.string
            .pref_ex_contactless_serial);
    /**
     * 外置非接波特率
     */
    public static final String EX_ONTACTLESS_BAUD_RANT = getString(R.string
            .pref_ex_contactless_baut_rate);
    /**
     * 签名板选择
     */
    public static final String SIGNATURE_SELECTOR = getString(R.string.pref_sign_pad_choose);
    /**
     * "强制下载黑名单
     */
    public static final String FORCE_DL_BLACK = getString(R.string.pref_force_dl_black);
    /**
     * 是否支持国密
     */
    public static final String SUPPORT_SM = getString(R.string.pref_supp_sm);
    /**
     * 是否支持国密二期
     */
    public static final String SUPPORT_SM_PERIOD_2 = getString(R.string.pref_supp_sm_period_2);
    /**
     * 小额代授权
     */
    public static final String SUPPORT_SMALL_AUTH = getString(R.string.pref_supp_small_auth);
    /**
     * 外置扫码
     */
    public static final String SUPPORT_EXTERNAL_SCANNER = getString(R.string
            .pref_supp_external_scanner);
    /**
     * 外置扫码选择
     */
    public static final String EXTERNAL_SCANNER = getString(R.string.pref_external_scanner);
    /**
     * 内置扫码摄像头选择
     */
    public static final String INTERNAL_SCANNER = getString(R.string.pref_internal_scanner);


    /******************************************************************
     * 通讯参数
     ******************************************************************/
    /**
     * TPDU
     */
    public static final String APP_TPDU = getString(R.string.pref_tpdu);
    /**
     * 收单后台通讯方式
     */
    public static final String APP_COMM_TYPE_ACQUIRER = getString(R.string
            .pref_acquirer_comm_type);
    /**
     * 收单后台SSL方式
     */
    public static final String APP_COMM_TYPE_SSL = getString(R.string.pref_acquirer_ssl_type);
    /**
     * 通讯超时时间
     */
    public static final String COMM_TIMEOUT = getString(R.string.pref_comm_timeout);

    /**
     * MODEM参数
     */
    /**
     * 电话号码1_MODEM"
     */
    public static final String PTAG_MODEM_TELNO1 = getString(R.string.pref_modem_telno_1);
    /**
     * 电话号码2_MODEM
     */
    public static final String PTAG_MODEM_TELNO2 = getString(R.string.pref_modem_telno_2);
    /**
     * 电话号码3_MODEM
     */
    public static final String PTAG_MODEM_TELNO3 = getString(R.string.pref_modem_telno_3);
    /**
     * 外线设置_MODEM
     */
    public static final String PTAG_MODEM_NEED_EXTER_LINE = getString(R.string
            .pref_modem_need_ext_line);
    /**
     * 外线号码_MODEM"
     */
    public static final String PTAG_MODEM_PABX = getString(R.string.pref_modem_ext_line_no);
    /**
     * 外线延时_MODEM
     */
    public static final String PTAG_MODEM_PABXDELAY = getString(R.string
            .pref_modem_ext_line_delay);
    /**
     * 预拨号
     */
    public static final String PTAG_MODEM_PRE_DIAL = getString(R.string.pref_modem_pre_dail);
    /**
     * 重拨次数_MODEM
     */
    public static final String PTAG_MODEM_DTIMES = getString(R.string.pref_modem_redail_times);
    /**
     * 通讯超时_MODEM"
     */
    public static final String PTAG_MODEM_TIMEOUT = getString(R.string.pref_modem_timeout);
    /**
     * 拨号电平_MODEM
     */
    public static final String PTAG_MODEM_LEVEL = getString(R.string.pref_modem_dial_level);
    /**
     * 音频_拨号脉冲_MODEM
     */
    public static final String PTAG_MODEM_DP = getString(R.string.pref_modem_dial_mode);
    /**
     * 检测拨号音_MODEM
     */
    public static final String PTAG_MODEM_CHDT = getString(R.string.pref_modem_check_dial);
    /**
     * 摘机到拨号的等待时间_MODEM
     */
    public static final String PTAG_MODEM_DT1 = getString(R.string.pref_modem_dial_wait_time);
    /**
     * 拨外线时等待时间_MODEM
     */
    public static final String PTAG_MODEM_DT2 = getString(R.string
            .pref_modem_outline_dial_wiat_time);
    /**
     * 双音拨号单一号码保持时间_MODEM
     */
    public static final String PTAG_MODEM_HT = getString(R.string.pref_modem_single_no_keep_time);
    /**
     * 双音拨号两个号码之间的间隔时间_MODEM
     */
    public static final String PTAG_MODEM_WT = getString(R.string.pref_modem_two_no_interval);
    /**
     * 通讯字节设置_MODEM
     */
    public static final String PTAG_MODEM_SSETUP = getString(R.string.pref_modem_byte);
    /**
     * 异步通讯方式_MODEM"
     */
    public static final String PTAG_MODEM_ASMODE = getString(R.string.pref_modem_async_type);

    /**
     * 移动网络
     */
    /**
     * 无线是否为长连接_移动网络
     */
    public static final String MOBILE_LONG_LINK = getString(R.string.pref_mobile_long_link);
    /**
     * 接入号码_移动网络
     */
    public static final String MOBILE_WLTELNO = getString(R.string.pref_mobile_access_no);
    /**
     * APN_移动网络
     */
    public static final String MOBILE_APN = getString(R.string.pref_mobile_apn);
    /**
     * 需要用户_移动网络
     */
    public static final String MOBILE_NEED_USER = getString(R.string.pref_mobile_need_user);
    /**
     * 用户名_移动网络
     */
    public static final String MOBILE_USER = getString(R.string.pref_mobile_user_name);
    /**
     * 用户密码_移动网络
     */
    public static final String MOBILE_PWD = getString(R.string.pref_mobile_pwd);
    /**
     * 主机地址_移动网络
     */
    public static final String MOBILE_HOSTIP = getString(R.string.pref_mobile_host_ip);
    /**
     * 主机端口_移动网络"
     */
    public static final String MOBILE_HOSTPORT = getString(R.string.pref_mobile_host_port);
    /**
     * 主机备份地址_移动网络
     */
    public static final String MOBILE_BAK_HOSTIP = getString(R.string.pref_mobile_back_host_ip);
    /**
     * 主机备份端口_移动网络
     */
    public static final String MOBILE_BAK_HOSTPORT = getString(R.string
            .pref_mobile_back_host_port);

    /**
     * 以太网参数
     */
    /**
     * 自动获取IP_LAN"
     */
    public static final String LAN_DHCP = getString(R.string.pref_lan_dhcp);
    /**
     * 本地IP_LAN"
     */
    public static final String LAN_LOCALIP = getString(R.string.pref_lan_local_ip);
    /**
     * 子网掩码_LAN"
     */
    public static final String LAN_SUBNETMASK = getString(R.string.pref_lan_subnet_mask);
    /**
     * 网关_LAN
     */
    public static final String LAN_GATEWAY = getString(R.string.pref_lan_gateway);
    /**
     * DNS1_LAN
     */
    public static final String LAN_DNS1 = getString(R.string.pref_lan_dns1);
    /**
     * DNS2_LAN"
     */
    public static final String LAN_DNS2 = getString(R.string.pref_lan_dns2);
    /**
     * 主机IP_LAN"
     */
    public static final String LAN_HOSTIP = getString(R.string.pref_lan_host_ip);
    /**
     * 主机端口_LAN
     */
    public static final String LAN_HOSTPORT = getString(R.string.pref_lan_host_port);
    /**
     * 主机备份IP_LAN
     */
    public static final String LAN_BAK_HOSTIP = getString(R.string.pref_lan_back_host_ip);
    /**
     * 主机备份端口_LAN
     */
    public static final String LAN_BAK_HOSTPORT = getString(R.string.pref_lan_back_host_port);

    /******************************************************************
     * 密钥管理
     ******************************************************************/
    /**
     * 主密钥索引（0-49）
     */
    public static final String MK_INDEX = getString(R.string.pref_mk_index);
    /**
     * 密钥算法
     */
    public static final String KEY_ALGORITHM = getString(R.string.pref_key_algorithm);
    /**
     * 密码键盘选择
     */
    public static final String EX_PINPAD = getString(R.string.pref_choose_pinpad);
    /**
     * 内置密码键盘UI类型
     */
    public static final String INTERNAL_PED_UI_STYLE = getString(R.string
            .pref_internal_ped_ui_style);
    /**
     * 手工设置主密钥值
     */
    public static final String INPUT_KEY_MANUALLY = getString(R.string.pref_input_key_manually);
    /**
     * 下载TMK
     */
    public static final String MK_DOWNLOAD = getString(R.string.pref_mk_download);

    /******************************************************************
     * 密码管理
     ******************************************************************/
    /**
     * 系统管理员密码
     */
    public static final String SEC_SYSPWD = getString(R.string.pref_sys_admin_pwd);
    /**
     * 主管密码
     */
    public static final String SEC_MNGPWD = getString(R.string.pref_master_operator_pwd);
    /**
     * 安全密码
     */
    public static final String SEC_SECPWD = getString(R.string.pref_security_pwd);

    /******************************************************************
     * Currency management
     ******************************************************************/
    /**
     * Predefined currency
     */
    public static final String CURRENCY_SYS = getString(R.string.pref_currency_sys);
    /**
     * Using custom currency
     */
    public static final String CURRENCY_USE_CUSTOM = getString(R.string.pref_custom_currency);
    /**
     * Currency name
     */
    public static final String CURRENCY_CUSTOM_NAME = getString(R.string.pref_currency_name);
    /**
     * Currency code
     */
    public static final String CURRENCY_CUSTOM_CODE = getString(R.string.pref_currency_code);
    /**
     * Currency decimals
     */
    public static final String CURRENCY_CUSTOM_DECIMALS = getString(R.string
            .pref_currency_decimals);
    /**
     * Initial default currency
     */
    public static final Currency CURRENCY_INITIAL_DEFAULT = Currency.IDR;

    // add abdul
    public static final String PRINT_DEBUG = getString(R.string.pref_print_debug);

    /******************************************************************
     * 其他管理
     ******************************************************************/
    /**
     * 签购单抬头中文
     */
    public static final String OTHER_HEAD_CONTENT = getString(R.string.pref_voucher_cn_title);

    //sandy
    public static final String INDOPAY_MODE = getString(R.string.pref_indopay);
    public static final String MCC = getString(R.string.pref_mcc);

    public static final String BPJS_LOCATION_DATA = getString(R.string.pref_bpjs_location_data);
    public static final String BPJS_BRANCH_OFFICE_DATA = getString(R.string.pref_bpjs_branch_office_data);
    public static final String BPJS_DISTRICT_DATA = getString(R.string.pref_bpjs_district_data);
    public static final String BPJS_PROVINCE_DATA = getString(R.string.pref_bpjs_province_data);
    public static final String BPJS_PAYMENT_REQUEST_DATA = getString(R.string.pref_bpjs_payment_request_data);
    public static final String BPJS_REGISTER_REQUEST_DATA = getString(R.string.pref_bpjs_register_request_data);
    public static final String BPJS_INQUIRY_REQUEST_DATA = getString(R.string.pref_bpjs_payment_inquiry_data);
    public static final String BPJS_INQUIRY_COUNTER_FORM = getString(R.string.pref_bpjs_inquiry_counter_form);




    private static Set<String> stringKeyMap = new HashSet<String>() {
        private static final long serialVersionUID = 1L;

        {
            // 通讯参数
            // add abdul
            add("download");
            add("prefix");
            //sandy
            add(BPJS_LOCATION_DATA);
            add(BPJS_BRANCH_OFFICE_DATA);
            add(BPJS_DISTRICT_DATA);
            add(BPJS_PROVINCE_DATA);
            add(BPJS_INQUIRY_REQUEST_DATA);
            add(BPJS_PAYMENT_REQUEST_DATA);
            add(BPJS_REGISTER_REQUEST_DATA);
            add(BPJS_INQUIRY_COUNTER_FORM);


            // TPDU
            add(APP_TPDU);
            // 超时时间
            add(COMM_TIMEOUT);
            // 通讯方式
            add(APP_COMM_TYPE_ACQUIRER);
            add(APP_COMM_TYPE_SSL);

            // 移动网络参数
            add(MOBILE_WLTELNO);
            add(MOBILE_APN);
            add(MOBILE_USER);
            add(MOBILE_PWD);
            add(MOBILE_HOSTIP);
            add(MOBILE_HOSTPORT);
            add(MOBILE_BAK_HOSTIP);
            add(MOBILE_BAK_HOSTPORT);

            // 以太网参数
            add(LAN_LOCALIP);
            add(LAN_SUBNETMASK);
            add(LAN_GATEWAY);
            add(LAN_BAK_HOSTIP);
            add(LAN_BAK_HOSTPORT);
            add(LAN_HOSTIP);
            add(LAN_HOSTPORT);
            add(LAN_DNS1);
            add(LAN_DNS2);

            // MODEM
            add(PTAG_MODEM_TELNO1);
            add(PTAG_MODEM_TELNO2);
            add(PTAG_MODEM_TELNO3);
            add(PTAG_MODEM_PABX);
            add(PTAG_MODEM_TIMEOUT);
            add(PTAG_MODEM_PABXDELAY);
            add(PTAG_MODEM_LEVEL);
            add(PTAG_MODEM_DP);

            add(PTAG_MODEM_DT1);
            add(PTAG_MODEM_DT2);
            add(PTAG_MODEM_HT);
            add(PTAG_MODEM_WT);
            add(PTAG_MODEM_SSETUP);
            add(PTAG_MODEM_DTIMES);
            add(PTAG_MODEM_ASMODE);

            // 商户参数
            add(MERCH_ID); // 商户号
            add(TERMINAL_ID); // 终端号
            add(MERCH_CN); // 中文商户名
            add(MERCH_EN); // 英文商户名
            add(ADDR1);
            add(ADDR2);
            add(ACQUIRER); //收单机构

            //交易管理
            add(OFFLINETC_UPLOADTIMES); // 离线上送次数
            add(OFFLINETC_UPLOADNUM); // 自动上送累计笔数
            add(OFFLINETC_UPLOAD_TYPE); // 离线上送方式
            // 交易管理 -其它交易控制
            add(HOME_TRANS); // 主界面交易方式
            //add(OTHTC_ECR_TOUT); // ECR连接超时时间
            add(OTHTC_EMV_OPR);    //判断EMV卡方式
            add(OTHTC_REFUNDLIMT); // 最大退货金额

            // 系统参数
            add(TRANS_NO); // 流水号
            add(BATCH_NO); // 批次号
            add(CUR_YEAR); // 当前年份
            add(MERCH_MERCHCODE); // 商行代码
            add(MERCH_AREACODE); // 地区代码
            add(PRINT_VOUCHER_NUM); // 打印凭单联数
            add(PRINT_GRAY);   //打印机灰度
            add(RESEND_TIMES); // 重发次数
            add(RESEND_SIG_TIMES);// 签名重发次数
            add(REVERSL_CTRL); // 冲正控制
            add(MAX_TRANS_COUNT); // 最大交易笔数
            add(TIP_MODE); //消费设置
            add(TIP_RATE); // 小费比例
            add(EX_CONTACTLESS_CHOOSE);
            add(EX_CONTACTLESS_SERIAL);
            add(EX_ONTACTLESS_BAUD_RANT);
            // 终端密钥管理
            add(MK_INDEX); // 主密码索引
            add(KEY_ALGORITHM); // 密钥算法

            // 电子签名控制

            // 密码管理
            add(SEC_SYSPWD); // 系统管理员密码
            add(SEC_MNGPWD); // 主管密码
            add(SEC_SECPWD); // 安全密码

            //货币管理
            add(CURRENCY_SYS);//选定的预定义货币
            add(CURRENCY_CUSTOM_CODE);//自定义货币代码
            add(CURRENCY_CUSTOM_NAME);//自定义货币名称
            add(CURRENCY_CUSTOM_DECIMALS);//自定义货币小数位数

            // 其它管理
            add(OTHER_HEAD_CONTENT);

            // 闪付
            add(QUICK_PASS_TRANS_BRUSH_TIMES);
            add(QUICK_PASS_TRANS_SIGN_PIN_FREE_AMOUNT);     //Jerry add
            add(QUICK_PASS_TRANS_TIMES);

            add(EXTERNAL_SCANNER);
            add(INTERNAL_SCANNER);
            add(SIGNATURE_SELECTOR);

            add(EX_PINPAD);
            add(INTERNAL_PED_UI_STYLE);

            //sandy
            add(MCC);
            add(SHOW_ADMIN_FEE);
        }
    };

    private static Set<String> booleanKeyMap = new HashSet<String>() {
        private static final long serialVersionUID = 1L;

        {
            add(LAN_DHCP);
            // 系统参数
            add(SUPPORT_TIP); // 支持小费
            add(SUPPORT_SMALL_AUTH);// 支持小额代授权
            add(FORCE_DL_BLACK); // 强制下载黑名单
            add(SUPPORT_SM);// 支持国密
            add(SUPPORT_SM_PERIOD_2);// 国密二期
            add(EX_CONTACTLESS_SET);

            add(VOUCHER_PRINTEN);
            add(ISSUER_PRINT);
            add(ACQUIRE_PRINT);
            add(SALE_MASK_CARD_NUMBER);
            add(PREAUTH_MASK_CARD_NUMBER);
            add(ECASH_MASK_CARD_NUMBER);

            // 通讯
            add(MOBILE_LONG_LINK);
            add(LAN_DHCP);

            // 交易输密控制
            add(IPTC_VOID); // 消费撤销输密
            add(IPTC_PAVOID); // 预授权撤销输密
            add(IPTC_PACVOID); // 预授权完成撤销输密
            add(IPTC_PAC); // 预授权完成(请求)输密
            // 交易刷卡控制
            add(UCTC_VOID); // 消费撤销刷卡
            add(UCTC_PACVOID); // 预授权完成撤销刷卡
            // 结算交易控制
            add(SETTLETC_AUTOLOGOUT); // 结算后自动签退
            add(SETTLETC_PRNDETAIL); // 结算打印明细提示
            // 其它交易控制
            add(OTHTC_SUPP_EMV);        //支持EMV芯片卡交易
            //add(OTHTC_SUPP_ECR);        //支持ECR接口
            add(OTHTC_SUPP_EXRATE);        //支持汇率转换
            add(OTHTC_FORCE_ONLINE);        //是否强制联机
            //add(OTHTC_FORCE_SDA);        //是否强制SDA
            add(OTHTC_VERIFY); // 撤销退货类交易输入主管密码
            add(OTHTC_TRACK_ENCRYPT); // 磁道加密
            add(OTHTC_KEYIN); // 允许手输卡号
            add(OTHTC_SINGATURE);// 允许电子签名

            // 闪付
            add(QUICK_PASS_TRANS_CDCVM_FLAG);
            add(QUICK_PASS_TRANS_SWITCH);

            add(BCTC_TEST_SWITCH);

            add(SUPPORT_EXTERNAL_SCANNER);
            add(SUPPORT_OFFLINE_TRANS);

            add(PTAG_MODEM_NEED_EXTER_LINE);
            add(PTAG_MODEM_PRE_DIAL);
            add(PTAG_MODEM_CHDT);

            //货币管理
            add(CURRENCY_USE_CUSTOM);//是否使用自定义货币
            //sandy
            add(INDOPAY_MODE);
            // add abdul
            add(PRINT_DEBUG);


        }
    };

    private static Set<String> setKeyMap = new HashSet<String>() {
        private static final long serialVersionUID = 1L;

        {

            // 交易开关
            // 传统交易开关
            add(TTS_SALE); // 消费开关_传统交易
            add(TTS_VOID); // 消费撤销开关_传统交易
            add(TTS_REFUND); // 退货开关_传统交易
            add(TTS_BALANCE); // 余额查询开关_传统交易
            add(TTS_PREAUTH); // 预授权开关_传统交易
            add(TTS_PAVOID); // 预授权撤销开关_传统交易
            add(TTS_PACREQUEST); // 预授权完成请求开关_传统交易
            add(TTS_PACADVISE); // 预授权完成通知开关_传统交易
            add(TTS_PACVOID); // 预授权完成撤销开关_传统交易
            add(TTS_OFFLINE_SETTLE); // 离线结算开关_传统交易
            add(TTS_ADJUST); // 结算调整开关_传统交易
            // 电子现金交易开关
            add(ECTS_SALE); // 电子现金消费
            add(ECTS_LOAD); // 指定账户圈存开关_电子现金
            add(ECTS_TLOAD); // 非指定账户圈存开关_电子现金
            add(ECTS_CALOAD); // 现金充值开关_电子现金
            add(ECTS_CALOADVOID); // 现金充值撤销开关_电子现金
            add(ECTS_REFUND); // 脱机退货开关_电子现金
            add(ECTS_QUERY);// 余额查询_电子现金

            // 扫码交易开关
            add(SCTS_SALE);// 扫码消费_扫码交易
            add(SCTS_VOID);// 扫码撤销_扫码交易
            add(SCTS_REFUND);// 扫码退货_扫码交易

            //其他交易开关
            add(OTTS_INSTALLMENT);
            add(OTTS_IC_SCRIPT_PROCESS_RESULT_ADVICE);
            add(OTTS_INSTALLMENTVOID);
            add(OTTS_MOTO);
            add(OTTS_RECURRING);
        }
    };

    private static Context mContext;
    private static SysParam mSysParam;

    public static synchronized SysParam getInstance(Context context) {
        mContext = context.getApplicationContext();
        if (mSysParam == null) {
            mSysParam = new SysParam();
            if (!init(mContext))
                load(); // 加载参数内容到SysParam中
        }

        return mSysParam;
    }

    public interface UpdateListener {
        void onErr(String prompt);
    }

    private static UpdateListener updateListener;

    public static void setUpdateListener(UpdateListener listener) {
        updateListener = listener;
    }

    /**
     * 下载文件param.ini到files目录的方式初始化参数
     *
     * @return
     */
    @SuppressLint({"NewApi", "ShowToast"})
    public static boolean init(Context context) {
        File file = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "ParamDownload/" + INIT_FILE_NAME);
        if (!file.exists() || file.length() == 0) {
            AppLog.e(TAG, "file not found!");
            return false;
        }
        if (TransData.getTransCount() > 0) {
            if (updateListener != null) {
                updateListener.onErr(context.getString(R.string.param_need_upate_please_settle));
            }
            return false;
        }
        HashMap<String, Object> map = new HashMap<>();
        HashMap<String, Object> params = new HashMap<>();
        //Gets a SharedPreferences instance that points to the default file that is used by the
        // preference framework in the given context
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        Editor editor = sharedPreferences.edit();
        try {
            Set<String> setTtsKey = new HashSet<>();
            Set<String> setEctsKey = new HashSet<>();
            Set<String> setSctsKey = new HashSet<>();
            Set<String> setOttsKey = new HashSet<>();

            NodeList list = parseXML(file);
            if (list == null) {
                return false;
            }

            for (int i = 1; i < list.getLength(); i += 2) {
                Node node = list.item(i);
                if ((node == null)) { // 文件格式不对
                    return false;
                }

                String tag = node.getNodeName();
                String value = node.getTextContent();
                if (setKeyMap.contains(tag)) {
                    if (TTS_SALE.equals(tag) || TTS_VOID.equals(tag) || TTS_REFUND.equals(tag)
                            || TTS_BALANCE.equals(tag) || TTS_PREAUTH.equals(tag)
                            || TTS_PAVOID.equals(tag) || TTS_PACREQUEST.equals(tag)
                            || TTS_PACADVISE.equals(tag) || TTS_PACVOID.equals(tag)
                            || TTS_OFFLINE_SETTLE.equals(tag) || TTS_ADJUST.equals(tag)) {
                        // 传统类交易
                        if ("Y".equals(value)) {
                            setTtsKey.add(tag);
                        }
                        params.put(tag, value);
                    } else if (ECTS_SALE.equals(tag) || ECTS_LOAD.equals(tag) || ECTS_TLOAD.equals(tag)
                            || ECTS_CALOAD.equals(tag) || ECTS_CALOADVOID.equals(tag)
                            || ECTS_REFUND.equals(tag) || ECTS_QUERY.equals(tag)) {
                        // 电子现金类交易
                        if ("Y".equals(value)) {
                            setEctsKey.add(tag);
                        }
                        params.put(tag, value);
                    } else if (SCTS_SALE.equals(tag) || SCTS_VOID.equals(tag)
                            || SCTS_REFUND.equals(tag)) {
                        // 扫码类交易
                        if ("Y".equals(value)) {
                            setSctsKey.add(tag);
                        }
                        params.put(tag, value);
                    } else if (OTTS_INSTALLMENT.equals(tag) ||
                            OTTS_IC_SCRIPT_PROCESS_RESULT_ADVICE.equals(tag)
                            || OTTS_INSTALLMENTVOID.equals(tag) || OTTS_MOTO.equals(tag)
                            || OTTS_RECURRING.equals(tag)) {
                        // 其他交易
                        if ("Y".equals(value)) {
                            setOttsKey.add(value);
                        }
                        params.put(tag, value);
                    }
                } else {
                    params.put(tag, value);
                    if ("Y".equals(value)) {
                        editor.putBoolean(tag, true);
                    } else if ("N".equals(value)) {
                        editor.putBoolean(tag, false);
                    } else {
                        editor.putString(tag, value);
                    }
                }
            }

            editor.putStringSet(TTS, setTtsKey);
            setTtsKey.clear();

            editor.putStringSet(ECTS, setEctsKey);
            setEctsKey.clear();

            editor.putStringSet(SCTS, setSctsKey);
            setSctsKey.clear();

            editor.putStringSet(OTTS, setOttsKey);
            setOttsKey.clear();

            editor.commit();
            map.clear();

            return true;
        } catch (Exception e) {
            Log.e(TAG, "", e);
        } finally {
            file.delete();
        }

        return false;
    }

    // 系统参数加载，如果db中不存在则添加
    @SuppressLint("NewApi")
    private static void load() {
        // 通讯参数
        Log.d(TAG,"sandy.load");

        // TPDU
        if (isParamFileExist()) {
            return;
        }
        Log.d(TAG,"sandy.after.load");
        // 设置默认参数值
        Set<String> set = new HashSet<>();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences (mContext);
        Editor editor = sharedPreferences.edit();
        if (editor != null) {
            //Sandy : set the default value here.....
            editor.putString(APP_TPDU, "6000110000");
            // 超时时间
            editor.putString(COMM_TIMEOUT, "30");
            // 通讯方式
            editor.putString(APP_COMM_TYPE_ACQUIRER, Constant.COMMTYPE_WIFI);
            editor.putString(APP_COMM_TYPE_SSL, Constant.COMM_NO_SSL);

            // MOBILE参数
            editor.putString(MOBILE_WLTELNO, "8888888");
            editor.putString(MOBILE_APN, "indopay");
            editor.putString(MOBILE_USER, "");
            editor.putString(MOBILE_PWD, "");
            editor.putString(MOBILE_HOSTIP, "10.88.17.2");
            editor.putString(MOBILE_HOSTPORT, "5510");
            editor.putString(MOBILE_BAK_HOSTIP, "0.0.0.0");
            editor.putString(MOBILE_BAK_HOSTPORT, "0");

            // 以太网参数
            editor.putString(LAN_LOCALIP, "172.16.10.125");
            editor.putString(LAN_SUBNETMASK, "255.255.255.0");
            editor.putString(LAN_GATEWAY, "172.16.10.1");
            editor.putString(LAN_BAK_HOSTIP, "0.0.0.0");
            editor.putString(LAN_BAK_HOSTPORT, "0");
            editor.putBoolean(LAN_DHCP, false);
            editor.putString(LAN_HOSTIP, "182.253.222.220");
            editor.putString(LAN_HOSTPORT, "5525");
            /*editor.putString(LAN_HOSTIP, "192.168.43.153");
            editor.putString(LAN_HOSTPORT, "5500");*/
            editor.putString(LAN_DNS1, "0.0.0.0");
            editor.putString(LAN_DNS2, "0.0.0.0");

            // MODEM
            editor.putString(PTAG_MODEM_TELNO1, "4008200358");
            editor.putString(PTAG_MODEM_TELNO2, "00000000");
            editor.putString(PTAG_MODEM_TELNO3, "00000000");
            editor.putString(PTAG_MODEM_PABX, "0");
            editor.putBoolean(PTAG_MODEM_NEED_EXTER_LINE, true);
            editor.putBoolean(PTAG_MODEM_PRE_DIAL, true);
            editor.putString(PTAG_MODEM_TIMEOUT, "2");
            editor.putString(PTAG_MODEM_PABXDELAY, "2");
            editor.putString(PTAG_MODEM_LEVEL, "1");
            editor.putString(PTAG_MODEM_DP, Constant.MODEM_DP_00);
            editor.putBoolean(PTAG_MODEM_CHDT, true);
            editor.putString(PTAG_MODEM_DT1, "40");
            editor.putString(PTAG_MODEM_DT2, "10");
            editor.putString(PTAG_MODEM_HT, "100");
            editor.putString(PTAG_MODEM_WT, "10");
            editor.putString(PTAG_MODEM_SSETUP, "4");
            //editor.putString(PTAG_MODEM_DTIMES, "3");
            editor.putString(PTAG_MODEM_ASMODE, "1");

            // 商户参数
            editor.putString(MERCH_ID, "673036070115000"); // 商户号
            editor.putString(TERMINAL_ID, "50001003"); // 终端号
            editor.putString(MERCH_CN, "银联商务"); // 中文商户名
            editor.putString(MERCH_EN, "ums"); // 英文商户名
            editor.putString(ADDR1, "addr1 param");
            editor.putString(ADDR2, "addr2 param");
            editor.putString(ACQUIRER,"32130360"); // 收单机构
            //交易参数
            editor.putString(HOME_TRANS, Constant.HOME_TRANS_SALE); //界面交易类型
            editor.putString(OTHTC_EMV_OPR, Constant.EMV_OPER_SELECT);  //操作员EMV卡输入
            //editor.putString(OTHTC_ECR_TOUT, "90");     //ECR超时时间
            // 系统参数
            editor.putString(TRANS_NO, "000001"); // 流水号
            editor.putString(BATCH_NO, "000001"); // 批次号
            editor.putBoolean(ACQUIRE_PRINT, false);
            editor.putBoolean(ISSUER_PRINT, false);
            editor.putString(PRINT_VOUCHER_NUM, "2"); // 打印凭单联数
            editor.putString(PRINT_GRAY, "500"); //打印机灰度
            editor.putBoolean(VOUCHER_PRINTEN, true);
            editor.putString(RESEND_SIG_TIMES, "3");// 签名重发次数
            editor.putString(REVERSL_CTRL, "3"); // 冲正控制
            editor.putString(MAX_TRANS_COUNT, "500");
            editor.putString(TIP_MODE, "0"); //小费设置
            editor.putBoolean(SUPPORT_TIP, false);// 支持4小费
            editor.putString(TIP_RATE, "5"); // 小费比例
            editor.putBoolean(SALE_MASK_CARD_NUMBER, true);
            editor.putBoolean(PREAUTH_MASK_CARD_NUMBER, true);
            editor.putBoolean(ECASH_MASK_CARD_NUMBER, true);

            editor.putBoolean(EX_CONTACTLESS_SET, false);
            editor.putString(EX_CONTACTLESS_CHOOSE, "SP20");
            editor.putString(EX_CONTACTLESS_SERIAL, "PINPAD");
            editor.putString(EX_ONTACTLESS_BAUD_RANT, "9600");
            editor.putString(EXTERNAL_SCANNER, "1");
            editor.putString(INTERNAL_SCANNER, "2");

            //Modified by Steven in order to adopt to English string change. 2017-4-14 11:36:48
            editor.putString(SIGNATURE_SELECTOR, "Internal");
            editor.putString(EX_PINPAD, "Internal");

            editor.putString(INTERNAL_PED_UI_STYLE, "1");   //Init the style is Defalut

            editor.putString(MERCH_AREACODE, "0000"); // 地区代码

            editor.putString(RESEND_TIMES, "3"); // 重发次数
            editor.putString(RESEND_SIG_TIMES, "3");// 签名重发次数

            editor.putBoolean(FORCE_DL_BLACK, false); // 强制下载黑名单
            editor.putBoolean(SUPPORT_SM, false);// 支持国密
            editor.putBoolean(SUPPORT_SM_PERIOD_2, false); // 国密二期
            editor.putBoolean(SUPPORT_SMALL_AUTH, false);// 支持小额代授权
            editor.putBoolean(SUPPORT_EXTERNAL_SCANNER, false);
            editor.putBoolean(SUPPORT_OFFLINE_TRANS, true); // 支持脱机交易
            // 终端密钥管理
            editor.putString(MK_INDEX, "1"); // 主密码索引
            editor.putString(KEY_ALGORITHM, Constant.TRIP_DES); // 密钥算法

            // 交易开关
            // 传统交易开关
            set.clear();
            set.add(TTS_SALE); // 消费开关_传统交易
            set.add(TTS_VOID); // 消费撤销开关_传统交易
            set.add(TTS_REFUND); // 退货开关_传统交易
            set.add(TTS_BALANCE); // 余额查询开关_传统交易
            set.add(TTS_PREAUTH); // 预授权开关_传统交易
            set.add(TTS_PAVOID); // 预授权撤销开关_传统交易
            set.add(TTS_PACREQUEST); // 预授权完成请求开关_传统交易
            set.add(TTS_PACADVISE); // 预授权完成通知开关_传统交易
            set.add(TTS_PACVOID); // 预授权完成撤销开关_传统交易
            set.add(TTS_OFFLINE_SETTLE); // 离线结算开关_传统交易
            set.add(TTS_ADJUST); // 结算调整开关_传统交易
            editor.putStringSet(TTS, set);
            // 电子现金交易开关
            set.clear();
            set.add(ECTS_SALE); // 电子现金消费
            set.add(ECTS_LOAD); // 指定账户圈存开关_电子现金
            set.add(ECTS_TLOAD); // 非指定账户圈存开关_电子现金
            set.add(ECTS_CALOAD); // 现金充值开关_电子现金
            set.add(ECTS_CALOADVOID); // 现金充值撤销开关_电子现金
            set.add(ECTS_REFUND); // 脱机退货开关_电子现金
            set.add(ECTS_QUERY);// 余额查询_电子现金
            editor.putStringSet(ECTS, set);
            // 扫码类交易开关
            set.clear();
            set.add(SCTS_SALE);
            set.add(SCTS_VOID);
            set.add(SCTS_REFUND);
            editor.putStringSet(SCTS, set);

            // 其他交易开关
            set.clear();
            set.add(OTTS_INSTALLMENT);
            set.add(OTTS_INSTALLMENTVOID);
            set.add(OTTS_RECURRING);
            set.add(OTTS_IC_SCRIPT_PROCESS_RESULT_ADVICE);
            set.add(OTTS_MOTO);
            editor.putStringSet(OTTS, set);

            // 交易输密控制
            editor.putBoolean(IPTC_VOID, false); // 消费撤销输密
            editor.putBoolean(IPTC_PAVOID, false); // 预授权撤销输密
            editor.putBoolean(IPTC_PACVOID, false); // 预授权完成撤销输密
            editor.putBoolean(IPTC_PAC, false); // 预授权完成(请求)输密
            // 交易刷卡控制
            editor.putBoolean(UCTC_VOID, false); // 消费撤销刷卡
            editor.putBoolean(UCTC_PACVOID, false); // 预授权完成撤销刷卡
            // 结算交易控制
            editor.putBoolean(SETTLETC_AUTOLOGOUT, false); // 结算后自动签退
            editor.putBoolean(SETTLETC_PRNDETAIL, false); // 结算打印明细提示
            // 离线交易控制
            // editor.putBoolean(OFFLINETC_MICRO_PAYMENT, true); // 支持小额代付方式
            editor.putString(OFFLINETC_UPLOAD_TYPE, Constant.OFFLINETC_UPLOAD_NEXT); // 离线上送方式
            editor.putString(OFFLINETC_UPLOADTIMES, "3"); // 离线上送次数
            editor.putString(OFFLINETC_UPLOADNUM, "10"); // 自动上送累计笔数
            // 其它交易控制
            editor.putBoolean(OTHTC_SUPP_EMV, true);     //支持EMV芯片卡交易";
            editor.putBoolean(OTHTC_SUPP_EXRATE, false);     //支持汇率转换";
            editor.putBoolean(OTHTC_FORCE_ONLINE, false);     //是否强制联机";
            //editor.putBoolean(OTHTC_FORCE_SDA, false);     //是否强制SDA";

            editor.putBoolean(OTHTC_VERIFY, true); // 撤销退货类交易输入主管密码
            editor.putBoolean(OTHTC_TRACK_ENCRYPT, false); // 磁道加密
            editor.putBoolean(OTHTC_KEYIN, true); // 允许手输卡号
            editor.putString(OTHTC_REFUNDLIMT, "20000"); // 最大退货金额
            // 电子签名控制
            editor.putBoolean(OTHTC_SINGATURE, true); // 允许电子签名

            // 密码管理
            editor.putString(SEC_SYSPWD, "88888888"); // 系统管理员密码
            editor.putString(SEC_MNGPWD, "123456"); // 主管密码
            editor.putString(SEC_SECPWD, "12345678"); // 安全密码

            editor.putString(OTHER_HEAD_CONTENT, "PT. Indopay Merchant Services");

            editor.putBoolean(QUICK_PASS_TRANS_SWITCH, true);
            editor.putBoolean(QUICK_PASS_TRANS_CDCVM_FLAG, false);
            editor.putString(QUICK_PASS_TRANS_SIGN_PIN_FREE_AMOUNT, "30000");
            editor.putString(QUICK_PASS_TRANS_BRUSH_TIMES, "10");
            editor.putString(QUICK_PASS_TRANS_TIMES, "60");


            editor.putBoolean(BCTC_TEST_SWITCH, false);

            //货币管理
            editor.putBoolean(CURRENCY_USE_CUSTOM, true);//默认不使用自定义货币
            //sandy
            editor.putBoolean(INDOPAY_MODE, true);
            //sandy
            editor.putString(MCC,"5999");
            editor.putString(SHOW_ADMIN_FEE,"1");
            // add abdul
            editor.putBoolean(PRINT_DEBUG, false);

            editor.commit();
        }

    }

    @SuppressLint("NewApi")
    public synchronized String get(String name) {
        String value = null;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        if (stringKeyMap.contains(name)) {
            value = sharedPreferences.getString(name, null);
        } else if (booleanKeyMap.contains(name)) {
            boolean b = sharedPreferences.getBoolean(name, false);
            value = (b ? "Y" : "N");
        } else if (setKeyMap.contains(name)) {
            Set<String> set1 = sharedPreferences.getStringSet(TTS, null);
            Set<String> set2 = sharedPreferences.getStringSet(ECTS, null);
            Set<String> set3 = sharedPreferences.getStringSet(SCTS, null);
            Set<String> set4 = sharedPreferences.getStringSet(OTTS, null);
            if ((set1 != null && set1.contains(name)) ||
                    (set2 != null && set2.contains(name)) ||
                    (set3 != null && set3.contains(name)) ||
                    (set4 != null && set4.contains(name))) {
                value = "Y";
            } else {
                value = "N";
            }
        }
        return value;
    }

    /**
     * If failed to get the value of the specific "name", return the "defaultValue" you set
     * Added by Steven 2017-4-7 17:15:59
     *
     * @param name
     * @param defaultValue
     * @return
     */
    @SuppressLint("NewApi")
    public synchronized String get(String name, String defaultValue) {
        String value = get(name);
        if (value == null) {
            return defaultValue;
        } else {
            return value;
        }
    }

    @SuppressLint("NewApi")
    public synchronized void set(String name, String value) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        Editor editor = sharedPreferences.edit();
        if (stringKeyMap.contains(name)) {
            editor.putString(name, value);
        } else if (booleanKeyMap.contains(name)) {
            editor.putBoolean(name, "Y".equals(value) ? true : false);
        }
        editor.commit();
    }

    public synchronized void updateTSParam(String actFlag, String name, String keyGroup) {
        if (!setKeyMap.contains(name)) {
            return;
        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences
                (mContext);
        Set<String> set1 = sharedPreferences.getStringSet(TTS, null);
        Set<String> set2 = sharedPreferences.getStringSet(ECTS, null);
        Set<String> set3 = sharedPreferences.getStringSet(SCTS, null);
        Set<String> set4 = sharedPreferences.getStringSet(OTTS, null);
        Set<String> setTts = new HashSet<>(set1);
        Set<String> setEcts = new HashSet<>(set2);
        Set<String> setScts = new HashSet<>(set3);
        Set<String> setOtts = null;
        if (set4 != null) {
            setOtts = new HashSet<>(set4);
        }

        if (Constant.ACTION_ADD.equals(actFlag)) {
            if (keyGroup.equals(TTS)) {
                setTts.add(name);
            } else if (keyGroup.equals(ECTS)) {
                setEcts.add(name);
            } else if (keyGroup.equals(SCTS)) {
                setScts.add(name);
            } else if (keyGroup.equals(OTTS) && setOtts != null) {
                setOtts.add(name);
            }
        } else if (Constant.ACTION_DELETE.equals(actFlag)) {
            if (keyGroup.equals(TTS)) {
                setTts.remove(name);
            } else if (keyGroup.equals(ECTS)) {
                setEcts.remove(name);
            } else if (keyGroup.equals(SCTS)) {
                setScts.remove(name);
            } else if (keyGroup.equals(OTTS) && setOtts != null) {
                setOtts.remove(name);
            }
        }

        Editor editor = sharedPreferences.edit();
        editor.putStringSet(TTS, setTts);
        editor.putStringSet(ECTS, setEcts);
        editor.putStringSet(SCTS, setScts);
        if (setOtts != null) {
            editor.putStringSet(OTTS, setOtts);
        }
        editor.commit();
    }

    public static NodeList parseXML(File file) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(file);
            NodeList nodeList = document.getChildNodes();
            Node node = nodeList.item(0);
            if (node == null) {
                return null;
            }
            return node.getChildNodes();
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
        return null;
    }


    public static HashMap<String, String> getAll() {
        HashMap<String, String> params = new HashMap<>();
        SysParam sysParam = SysParam.getInstance(FinancialApplication.getAppContext());

        for (String stringKey : stringKeyMap) {
            String stringValue = sysParam.get(stringKey);
            params.put(stringKey, stringValue);
        }
        for (String booleanKey : booleanKeyMap) {
            String booleanValue = sysParam.get(booleanKey);
            params.put(booleanKey, booleanValue);
        }
        for (String setKey : setKeyMap) {
            String setValue = sysParam.get(setKey);
            params.put(setKey, setValue);
        }
        return params;
    }

    /**
     * 联机上送参数
     */
    public static void uploadParamOnline(final HashMap<String, String> params) {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                PaxAppStoreTool.setParam(mContext, params);
//            }
//        }).start();
    }

    private static boolean isParamFileExist() {
        String dir = "/data/data/" + mContext.getPackageName() + File.separator + "shared_prefs/"
                + mContext.getPackageName() + "_preferences.xml";
        File file = new File(dir);
        return file.exists();
    }

    /**
     * Get current currency setting parameters.
     *
     * @return Return currency setting parameters.
     */
    public Currency getCurrency() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences
                (mContext);

        String currencyCode;
        String currencyName;
        String currencyDecimals;
        Currency selectedCurrency;
        boolean isUseCustomCurrency = sharedPreferences
                .getBoolean(SysParam.CURRENCY_USE_CUSTOM, false);

        if (isUseCustomCurrency) {
            currencyCode = sharedPreferences.getString(SysParam.CURRENCY_CUSTOM_CODE, "360");
            currencyName = sharedPreferences.getString(SysParam.CURRENCY_CUSTOM_NAME, "RP");
            currencyDecimals = sharedPreferences.getString(SysParam.CURRENCY_CUSTOM_DECIMALS, "0");

            selectedCurrency = Currency.CUSTOM;
            selectedCurrency.setCode(currencyCode);
            selectedCurrency.setName(currencyName);
            try {
                selectedCurrency.setDecimals(Integer.valueOf(currencyDecimals));
            } catch (NumberFormatException e) {
                Log.e(TAG, "", e);
            }
        } else {
            currencyCode = sharedPreferences.getString(SysParam.CURRENCY_SYS, null);
            selectedCurrency = Currency.queryCurrency(currencyCode, CURRENCY_INITIAL_DEFAULT);
        }

        return selectedCurrency;
    }

    // add abdul
    public boolean getPrintDebug() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences
                (mContext);
        boolean isPrintDebug = sharedPreferences.getBoolean(PRINT_DEBUG, false);
        return isPrintDebug;
    }

    public void setPrintDebug(boolean debug) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        Editor editor = sharedPreferences.edit();
        editor.putBoolean(PRINT_DEBUG, debug);

        editor.commit();
    }

    public static class Constant {

        //主界面交易类型
        /**
         * Sale
         */
        public static final String HOME_TRANS_SALE = "1";
        /**
         * Pre-Auth
         */
        public static final String HOME_TRANS_PRE_AUTH = "2";

        //判断EMV卡方式
        /**
         * Judge by operator
         */
        public static final String EMV_OPER_SELECT = "1";
        /**
         * Judge by POS
         */
        public static final String EMV_POS_SELECT = "2";

        //通讯类型
        /**
         * MODEM
         */
        public static final String COMMTYPE_MODEM = "1";
        /**
         * 移动网络
         */
        public static final String COMMTYPE_MOBILE = "2";
        /**
         * 以太网
         */
        public static final String COMMTYPE_LAN = "3";
        /**
         * WIFI
         */
        public static final String COMMTYPE_WIFI = "4";

        /**
         * 不启用SSL
         */
        public static final String COMM_NO_SSL = "1";
        /**
         * 银联SSL
         */
        public static final String COMM_CUP_SSL = "2";


        /**
         * des算法
         */
        public static final String DES = "des";
        public static final String TRIP_DES = "3des";

        /**
         * 密码键盘类型
         */
        public static final String PED = "ped";
        public static final String PINPAD = "pinpad";

        /**
         * 对应于肯定值, 是\支持\等
         */
        public static final String YES = "Y";
        // 对应于否定值, 否\不支持\等
        public static final String NO = "N";

        /**
         * 字体大小
         */
        public static final String BIG = "B";
        public static final String MIDDLE = "M";
        public static final String SMALL = "S";

        /**
         * 外置密码键盘S200
         */
        public static final String PAD_S200 = "S200";
        /**
         * 外置密码键盘SP20
         */
        public static final String PAD_SP20 = "SP20";
        /**
         * 内置密码键盘 Modified by Steven 2017-4-12 20:31:07
         */
        public static final String PAD_INTERNAL = "Internal";
        /**
         * 内置密码键盘UI风格 -- 默认
         */
        public static final String PAD_INTERNAL_UI_DEFAULT = "1";
        /**
         * 内置密码键盘UI风格 -- 自定义
         */
        public static final String PAD_INTERNAL_UI_CUSTOM = "2";

        /**
         * MODEM 拨号方式
         */
        /**
         * 双音多频
         */
        public static final String MODEM_DP_00 = "1";
        /**
         * 脉冲一
         */
        public static final String MODEM_DP_01 = "2";
        /**
         * 脉冲二
         */
        public static final String MODEM_DP_02 = "3";

        /**
         * 离线上送方式
         */
        /**
         * Upload next time
         */
        public static final String OFFLINETC_UPLOAD_NEXT = "1";
        /**
         * Batch upload
         */
        public static final String OFFLINETC_UPLOAD_BATCH = "2";

        /**
         * acton of update parameter
         */
        public static final String ACTION_ADD = "ADD";
        public static final String ACTION_DELETE = "DELETE";

        //Internal scanner type
        public static final String SCANNER_FRONT = "1";
        public static final String SCANNER_REAR = "2";
        public static final String SCANNER_LEFT = "3";
        public static final String SCANNER_RIGHT = "4";
    }


    public static String getString(int resId) {
        return FinancialApplication.getAppContext().getString(resId);
    }

}
