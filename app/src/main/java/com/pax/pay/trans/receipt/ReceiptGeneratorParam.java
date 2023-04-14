/*******************************************************************************
 * ============================================================================
 * COPYRIGHT
 *               Pax CORPORATION PROPRIETARY INFORMATION
 *    This software is supplied under the terms of a license agreement or
 *    nondisclosure agreement with Pax Corporation and may not be copied
 *    or disclosed except in accordance with the terms in that agreement.
 *       Copyright (C) 2017 - ? Pax Corporation. All rights reserved.
 *  Module Date: 17-5-22 上午11:24
 *  Module Author: liliang
 *  Description:
 *  ============================================================================
 ******************************************************************************/

package com.pax.pay.trans.receipt;

import android.content.Context;
import android.graphics.Bitmap;

import com.pax.gl.imgprocessing.IImgProcessing;
import com.pax.gl.imgprocessing.IImgProcessing.IPage;
import com.pax.gl.imgprocessing.IImgProcessing.IPage.EAlign;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.trans.TransContext;
import com.pax.settings.SysParam;
import com.pax.up.bjb.R;

/**
 * 终端参数生成器
 * 
 */
class ReceiptGeneratorParam implements IReceiptGenerator {

    String title;

    public ReceiptGeneratorParam(String title) {
        this.title = title;
    }

    @Override
    public Bitmap generate() {
        IPage page = FinancialApplication.getGl().getImgProcessing().createPage();
        Context context = FinancialApplication.getAppContext();
        page.setTypeFace(TYPE_FACE);
        SysParam sysParam = FinancialApplication.getSysParam();
        // 凭单抬头
        page.addLine().addUnit(title, FONT_BIG, EAlign.CENTER);

        // 商户名称
        page.addLine().addUnit(context.getString(R.string.receipt_merchant_name) + sysParam.get(SysParam.MERCH_CN),
                FONT_NORMAL);

        // 商户编号
        page.addLine().addUnit(context.getString(R.string.receipt_merchant_code) + sysParam.get(SysParam.MERCH_ID),
                FONT_NORMAL);

        // 终端编号
        page.addLine()
                .addUnit(context.getString(R.string.receipt_terminal_code_space) + sysParam.get(SysParam.TERMINAL_ID),
                        FONT_NORMAL);

        //操作员号
        page.addLine().addUnit(context.getString(R.string.receipt_oper_id_space) + TransContext.getInstance().getOperID(),
                        FONT_NORMAL);

        page.addLine().addUnit(
                context.getString(R.string.st_general_menu_sale) + "：" + SysParam.getAll().get(SysParam.TTS_SALE),
                FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.st_general_menu_unsale) + "：" + SysParam.getAll().get(SysParam.TTS_VOID),
                FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.st_general_menu_refund) + "：" + SysParam.getAll().get(SysParam.TTS_REFUND),
                FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.st_general_menu_query) + "：" + SysParam.getAll().get(SysParam.TTS_BALANCE),
                FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.st_general_menu_preauth) + "："
                        + SysParam.getAll().get(SysParam.TTS_PREAUTH), FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.st_general_menu_unpreauth) + "："
                        + SysParam.getAll().get(SysParam.TTS_PAVOID), FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.st_general_menu_preauth_complete) + "："
                        + SysParam.getAll().get(SysParam.TTS_PACREQUEST), FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.st_general_menu_preauth_offcomplete) + "："
                        + SysParam.getAll().get(SysParam.TTS_PACADVISE), FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.st_general_menu_preauth_uncomplete) + "："
                        + SysParam.getAll().get(SysParam.TTS_PACVOID), FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.st_general_menu_offsettle) + "："
                        + SysParam.getAll().get(SysParam.TTS_OFFLINE_SETTLE), FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.st_general_menu_settleadjust) + "："
                        + SysParam.getAll().get(SysParam.TTS_ADJUST), FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.st_ec_menu_ecsale_contact) + "："
                        + SysParam.getAll().get(SysParam.ECTS_SALE), FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.st_ec_menu_qc_acc) + "：" + SysParam.getAll().get(SysParam.ECTS_LOAD),
                FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.st_ec_menu_qc_noacc) + "：" + SysParam.getAll().get(SysParam.ECTS_TLOAD),
                FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.st_ec_menu_cash) + "：" + SysParam.getAll().get(SysParam.ECTS_CALOAD),
                FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.st_ec_menu_uncash) + "：" + SysParam.getAll().get(SysParam.ECTS_CALOADVOID),
                FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.st_ec_menu_offrefund) + "：" + SysParam.getAll().get(SysParam.ECTS_REFUND),
                FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.tm_passwd_menu_unsale) + "：" + SysParam.getAll().get(SysParam.IPTC_PAVOID),
                FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.tm_passwd_menu_unpreauth) + "："
                        + SysParam.getAll().get(SysParam.IPTC_PAVOID), FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.tm_passwd_menu_preauth_uncomplete) + "："
                        + SysParam.getAll().get(SysParam.IPTC_PACVOID), FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.tm_passwd_menu_preauth_complete) + "："
                        + SysParam.getAll().get(SysParam.IPTC_PAC), FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.tm_brushcard_menu_unsale) + "：" + SysParam.getAll().get(SysParam.UCTC_VOID),
                FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.tm_brushcard_menu_preauth_uncomplete) + "："
                        + SysParam.getAll().get(SysParam.UCTC_PACVOID), FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.tm_settle_menu_auto_logout) + "："
                        + SysParam.getAll().get(SysParam.SETTLETC_AUTOLOGOUT), FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.tm_settle_menu_is_printdetail) + "："
                        + SysParam.getAll().get(SysParam.SETTLETC_PRNDETAIL), FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.tm_offtrade_menu_upload_times) + "："
                        + SysParam.getAll().get(SysParam.OFFLINETC_UPLOADTIMES), FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.tm_offsale_menu_auto_upload_num) + "："
                        + SysParam.getAll().get(SysParam.OFFLINETC_UPLOADNUM), FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.tm_offsale_menu_offline_upload_type) + "："
                        + SysParam.getAll().get(SysParam.OFFLINETC_UPLOAD_TYPE), FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.tm_other_menu_input_admin_password) + "："
                        + SysParam.getAll().get(SysParam.OTHTC_VERIFY), FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.tm_other_menu_track_encrypt) + "："
                        + SysParam.getAll().get(SysParam.OTHTC_TRACK_ENCRYPT), FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.tm_other_menu_manual_cardno) + "："
                        + SysParam.getAll().get(SysParam.OTHTC_KEYIN), FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.tm_other_menu_signature) + "："
                        + SysParam.getAll().get(SysParam.OTHTC_SINGATURE), FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.tm_other_menu_max_refund_amount) + "："
                        + SysParam.getAll().get(SysParam.OTHTC_REFUNDLIMT), FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.tm_quick_pass_switch) + "："
                        + SysParam.getAll().get(SysParam.QUICK_PASS_TRANS_SWITCH), FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.tm_quick_pass_cdcvm) + "："
                        + SysParam.getAll().get(SysParam.QUICK_PASS_TRANS_CDCVM_FLAG), FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.tm_quick_pass_sign_pin_free_amount) + "："
                        + SysParam.getAll().get(SysParam.QUICK_PASS_TRANS_SIGN_PIN_FREE_AMOUNT), FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.systempara_menu_voucher_no) + SysParam.getAll().get(SysParam.TRANS_NO),
                FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.systempara_menu_batch_no) + SysParam.getAll().get(SysParam.BATCH_NO),
                FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.systempara_menu_is_acquirer_bank)
                        + SysParam.getAll().get(SysParam.ACQUIRE_PRINT), FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.systempara_menu_is_issuer_bank)
                        + SysParam.getAll().get(SysParam.ISSUER_PRINT), FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.systempara_menu_print_timers)
                        + SysParam.getAll().get(SysParam.PRINT_VOUCHER_NUM), FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.systempara_menu_is_voucher_bank_english)
                        + SysParam.getAll().get(SysParam.VOUCHER_PRINTEN), FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.systempara_menu_reversal_times) + "："
                        + SysParam.getAll().get(SysParam.REVERSL_CTRL), FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.systempara_menu_max_trade_num)
                        + SysParam.getAll().get(SysParam.MAX_TRANS_COUNT), FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.systempara_menu_is_ex_no_touch) + "："
                        + SysParam.getAll().get(SysParam.EX_CONTACTLESS_SET), FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.systempara_menu_is_ex_no_touch_choose)
                        + SysParam.getAll().get(SysParam.EX_CONTACTLESS_CHOOSE), FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.systempara_menu_signature_choose) + "："
                        + SysParam.getAll().get(SysParam.SIGNATURE_SELECTOR), FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.systempara_menu_is_ex_no_touch_serial_num)
                        + SysParam.getAll().get(SysParam.EX_CONTACTLESS_SERIAL), FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.systempara_menu_is_ex_no_touch_baud_rate)
                        + SysParam.getAll().get(SysParam.EX_ONTACTLESS_BAUD_RANT), FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.systempara_menu_load_blacklist)
                        + SysParam.getAll().get(SysParam.FORCE_DL_BLACK), FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.systempara_menu_support_tip) + "："
                        + SysParam.getAll().get(SysParam.SUPPORT_TIP), FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.systempara_menu_tips_rate) + SysParam.getAll().get(SysParam.TIP_RATE),
                FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.systempara_menu_support_small_auth) + "："
                        + SysParam.getAll().get(SysParam.SUPPORT_SMALL_AUTH), FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.systempara_menu_support_external_scanner) + "："
                        + SysParam.getAll().get(SysParam.SUPPORT_EXTERNAL_SCANNER), FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.external_scanner_select) + "："
                        + SysParam.getAll().get(SysParam.EXTERNAL_SCANNER), FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.internal_scanner_select) + "："
                        + SysParam.getAll().get(SysParam.INTERNAL_SCANNER), FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.tm_other_menu_support_exrate)
                        + SysParam.getAll().get(SysParam.OTHTC_SUPP_EXRATE), FONT_NORMAL);
        // 通讯参数
        page.addLine().addUnit(
                context.getString(R.string.commpara_menu_tpdu) + "：" + SysParam.getAll().get(SysParam.APP_TPDU),
                FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.commpara_menu_comm_mode_issuer) + "："
                        + SysParam.getAll().get(SysParam.APP_COMM_TYPE_ACQUIRER), FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.commpara_menu_ssl_mode) + "："
                        + SysParam.getAll().get(SysParam.APP_COMM_TYPE_SSL), FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.commpara_menu_other_comm_timeout) + "："
                        + SysParam.getAll().get(SysParam.COMM_TIMEOUT), FONT_NORMAL);
        // 移动网络
        page.addLine().addUnit(
                context.getString(R.string.commpara_menu_mobile_wifi_long_link) + "："
                        + SysParam.getAll().get(SysParam.MOBILE_LONG_LINK), FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.commpara_menu_mobile_dial_no) + "："
                        + SysParam.getAll().get(SysParam.MOBILE_WLTELNO), FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.commpara_menu_mobile_apn)
                        + "："
                        + ("".equals(SysParam.getAll().get(SysParam.MOBILE_APN)) ? "N" : SysParam.getAll().get(
                                SysParam.MOBILE_APN)), FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.commpara_menu_mobile_ip1) + SysParam.getAll().get(SysParam.MOBILE_HOSTIP),
                FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.commpara_menu_mobile_port1)
                        + SysParam.getAll().get(SysParam.MOBILE_HOSTPORT), FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.commpara_menu_mobile_ip2) + "："
                        + SysParam.getAll().get(SysParam.MOBILE_BAK_HOSTIP), FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.commpara_menu_mobile_port2) + "："
                        + SysParam.getAll().get(SysParam.MOBILE_BAK_HOSTPORT), FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.commpara_menu_mobile_need_user) + "："
                        + (SysParam.getAll().get(SysParam.MOBILE_NEED_USER) == null ? "N" : "Y"), FONT_NORMAL);
        if (SysParam.getAll().get(SysParam.MOBILE_NEED_USER) != null) {
            page.addLine().addUnit(
                    context.getString(R.string.commpara_menu_mobile_user_name) + "："
                            + SysParam.getAll().get(SysParam.MOBILE_USER), FONT_NORMAL);
            page.addLine().addUnit(
                    context.getString(R.string.commpara_menu_mobile_user_password) + "："
                            + SysParam.getAll().get(SysParam.MOBILE_PWD), FONT_NORMAL);
        }

        // 以太网
        page.addLine().addUnit(
                context.getString(R.string.commpara_menu_net_server_ip1) + SysParam.getAll().get(SysParam.LAN_HOSTIP),
                FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.commpara_menu_net_server_port1)
                        + SysParam.getAll().get(SysParam.LAN_HOSTPORT), FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.commpara_menu_net_is_dhcp) + "：" + SysParam.getAll().get(SysParam.LAN_DHCP),
                FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.commpara_menu_net_client_ip) + SysParam.getAll().get(SysParam.LAN_LOCALIP),
                FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.commpara_menu_net_mask) + SysParam.getAll().get(SysParam.LAN_SUBNETMASK),
                FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.commpara_menu_net_gateway) + SysParam.getAll().get(SysParam.LAN_GATEWAY),
                FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.commpara_menu_net_server_ip2)
                        + SysParam.getAll().get(SysParam.LAN_BAK_HOSTIP), FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.commpara_menu_net_server_port2)
                        + SysParam.getAll().get(SysParam.LAN_BAK_HOSTPORT), FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.commpara_menu_net_dns1) + SysParam.getAll().get(SysParam.LAN_DNS1),
                FONT_NORMAL);
        page.addLine().addUnit(
                context.getString(R.string.commpara_menu_net_dns2) + SysParam.getAll().get(SysParam.LAN_DNS2),
                FONT_NORMAL);
        page.addLine().addUnit("\n\n\n\n", FONT_NORMAL);

        IImgProcessing imgProcessing = FinancialApplication.getGl().getImgProcessing();
        return imgProcessing.pageToBitmap(page, 384);
    }

}
