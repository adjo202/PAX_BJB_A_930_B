package com.pax.pay.trans.model;

import com.pax.abl.core.ipacker.PackListener;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.trans.pack.PackAccountList;
import com.pax.pay.trans.pack.PackActivateTmk;
import com.pax.pay.trans.pack.PackAuth;
import com.pax.pay.trans.pack.PackAuthCM;
import com.pax.pay.trans.pack.PackAuthCMVoid;
import com.pax.pay.trans.pack.PackAuthVoid;
import com.pax.pay.trans.pack.PackBalance;
import com.pax.pay.trans.pack.PackBalanceInquiry;
import com.pax.pay.trans.pack.PackBalanceInquiryRevers;
import com.pax.pay.trans.pack.PackBatchUp;
import com.pax.pay.trans.pack.PackBatchUpNotice;
import com.pax.pay.trans.pack.PackBinDownload;
import com.pax.pay.trans.pack.PackBlackDownload;
import com.pax.pay.trans.pack.PackBpjsTkInquiry;
import com.pax.pay.trans.pack.PackBpjsTkPayment;
import com.pax.pay.trans.pack.PackBpjsTkRegister;
import com.pax.pay.trans.pack.PackCetakUlang;
import com.pax.pay.trans.pack.PackCouponReversal;
import com.pax.pay.trans.pack.PackCouponSale;
import com.pax.pay.trans.pack.PackCouponSaleVoid;
import com.pax.pay.trans.pack.PackCouponVerify;
import com.pax.pay.trans.pack.PackCouponVerifyReversal;
import com.pax.pay.trans.pack.PackDownloadParam;
import com.pax.pay.trans.pack.PackDownloadProductPulsaData;
import com.pax.pay.trans.pack.PackESamsat;
import com.pax.pay.trans.pack.PackEcRefund;
import com.pax.pay.trans.pack.PackEcSale;
import com.pax.pay.trans.pack.PackEcho;
import com.pax.pay.trans.pack.PackEmvParamDownload;
import com.pax.pay.trans.pack.PackEmvParamQuery;
import com.pax.pay.trans.pack.PackEmvPosStatusSubmission;
import com.pax.pay.trans.pack.PackICScript;
import com.pax.pay.trans.pack.PackIcTcBat;
import com.pax.pay.trans.pack.PackInqPulsaData;
import com.pax.pay.trans.pack.PackInquiryVoucher;
import com.pax.pay.trans.pack.PackInstalSale;
import com.pax.pay.trans.pack.PackInstalVoid;
import com.pax.pay.trans.pack.PackIso8583;
import com.pax.pay.trans.pack.PackLogon;
import com.pax.pay.trans.pack.PackLogout;
import com.pax.pay.trans.pack.PackMiniStatement;
import com.pax.pay.trans.pack.PackMotoSale;
import com.pax.pay.trans.pack.PackMpn;
import com.pax.pay.trans.pack.PackMpnInquiry;
import com.pax.pay.trans.pack.PackOfflineBat;
import com.pax.pay.trans.pack.PackOfflineTransSend;
import com.pax.pay.trans.pack.PackOverbooking;
import com.pax.pay.trans.pack.PackOverbookingBPJSTkReversal;
import com.pax.pay.trans.pack.PackOverbookingInquiry;
import com.pax.pay.trans.pack.PackOverbookingInquiry1;
import com.pax.pay.trans.pack.PackOverbookingPDAMReversal;
import com.pax.pay.trans.pack.PackOverbookingPascabayarReversal;
import com.pax.pay.trans.pack.PackOverbookingPulsaData;
import com.pax.pay.trans.pack.PackOverbookingReversal;
import com.pax.pay.trans.pack.PackPBBInquiry;
import com.pax.pay.trans.pack.PackPaymentVoucher;
import com.pax.pay.trans.pack.PackPembatalanRek;
import com.pax.pay.trans.pack.PackPembatalanRekInq;
import com.pax.pay.trans.pack.PackPembatalanRekReversal;
import com.pax.pay.trans.pack.PackPembukaanRekening;
import com.pax.pay.trans.pack.PackPembukaanRekeningReversal;
import com.pax.pay.trans.pack.PackPurchasePulsaData;
import com.pax.pay.trans.pack.PackQRGenerate;
import com.pax.pay.trans.pack.PackQRInquiry;
import com.pax.pay.trans.pack.PackQRSaleQris;
import com.pax.pay.trans.pack.PackQRSaleQrisVoid;
import com.pax.pay.trans.pack.PackQrRefund;
import com.pax.pay.trans.pack.PackQrSale;
import com.pax.pay.trans.pack.PackQrSaleVoid;
import com.pax.pay.trans.pack.PackRateDownload;
import com.pax.pay.trans.pack.PackRefund;
import com.pax.pay.trans.pack.PackReveral;
import com.pax.pay.trans.pack.PackReversalMinistatement;
import com.pax.pay.trans.pack.PackRsaKeyDown;
import com.pax.pay.trans.pack.PackSale;
import com.pax.pay.trans.pack.PackSaleVoid;
import com.pax.pay.trans.pack.PackSetorTunai;
import com.pax.pay.trans.pack.PackSetorTunaiReversal;
import com.pax.pay.trans.pack.PackSettle;
import com.pax.pay.trans.pack.PackSettleAdjust;
import com.pax.pay.trans.pack.PackSettleOffline;
import com.pax.pay.trans.pack.PackSignatureUpload;
import com.pax.pay.trans.pack.PackTarikTunai;
import com.pax.pay.trans.pack.PackTarikTunaiReversal;
import com.pax.pay.trans.pack.PackTmkDown;
import com.pax.pay.trans.pack.PackTotalProductPulsaData;
import com.pax.pay.trans.pack.PackTransfer;
import com.pax.pay.trans.pack.PackTransferAntarRekeningReversal;
import com.pax.pay.trans.pack.PackTransferInq;
import com.pax.pay.trans.pack.PackTransferReversal;
import com.pax.pay.trans.pack.PackVerifyPin;
import com.pax.up.bjb.R;

public enum ETransType {

    /************************************************ 管理类 ****************************************************/
    LOGON("0800", "", "", "", "00", "001", FinancialApplication.getAppContext().getString(R.string.pos_logon),
            false, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackLogon(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }
    },

    LOGOUT("0820", "", "", "", "00", "002", FinancialApplication.getAppContext().getString(R.string.pos_logout), false, false,
            false) {

        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackLogout(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }
    },

   //消息类型码  冲正消息类型码  处理码  服务码  功能码  网络管理码  交易名称  是否冲正上送  是否脚本结果上送  是否脱机交易上送
    POS_RSA_KEY_DOWN("0800", "","", "", "00", "352", FinancialApplication.getAppContext().getString(R.string.pos_rsakey_down),
           false, false, false){
       @Override
       public PackIso8583 getpackager(PackListener listener) {
           return new PackRsaKeyDown(listener);
       }

       @Override
       public PackIso8583 getDupPackager(PackListener listener) {
           return null;
       }
   },

    POS_TMK_DOWN("0800", "", "", "", "00", "3050", FinancialApplication.getAppContext().getString(R.string.pos_tmk_down),
            false, false, false){
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackTmkDown(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }
    },

    POS_ACTIVATE_TMK("0800", "", "", "", "00", "351", FinancialApplication.getAppContext().getString(R.string.pos_activate_tmk),
            false, false, false){
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackActivateTmk(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }
    },

    /**
     * IC卡公钥下载状态上送
     */
    EMV_MON_CA("0820", "", "", "", "00", "372", FinancialApplication.getAppContext().getString(R.string.emv_mon_ca), false, false,
            false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {

            return new PackEmvParamQuery(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {

            return null;
        }
    },
    /**
     * IC卡公钥下载
     */
    EMV_CA_DOWN("0800", "", "", "", "00", "370", FinancialApplication.getAppContext().getString(R.string.emv_ca_down), false,
            false, false) {

        @Override
        public PackIso8583 getpackager(PackListener listener) {

            return new PackEmvParamDownload(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {

            return null;
        }
    },
    /**
     * IC卡公钥下载结束
     */
    EMV_CA_DOWN_END("0800", "", "", "", "00", "371", FinancialApplication.getAppContext().getString(R.string.emv_ca_down_end),
            false, false, false) {

        @Override
        public PackIso8583 getpackager(PackListener listener) {

            return new PackEmvParamDownload(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {

            return null;
        }
    },
    /**
     * IC卡参数下载状态上送
     */
    EMV_MON_PARAM("0820", "", "", "", "00", "382", FinancialApplication.getAppContext().getString(R.string.emv_mon_param), false,
            false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackEmvParamQuery(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {

            return null;
        }
    },
    /**
     * POS状态参数上送(Added by Steven 2017-4-12 10:57:00)
     */
    EMV_POS_STATUS_UPLOAD("0820", "", "", "", "00", "362", FinancialApplication.getAppContext().getString(R.string.emv_pos_status_upload), false,
            false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackEmvPosStatusSubmission(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {

            return null;
        }
    },
    /**
     * IC卡参数下载
     */
    EMV_PARAM_DOWN("0800", "", "", "", "00", "380", FinancialApplication.getAppContext().getString(R.string.emv_param_down),
            false, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {

            return new PackEmvParamDownload(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {

            return null;
        }
    },
    /**
     * 回响功能
     */
    ECHO("0820", "", "", "", "00", "301", FinancialApplication.getAppContext().getString(R.string.echo), false, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {

            return new PackEcho(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {

            return null;
        }
    },
    /**
     * 参数传递
     */
    PARAM_TRANSMIT("0800", "", "", "", "00", "360", FinancialApplication.getAppContext().getString(R.string.param_transmit),
            false, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {

            return new PackEcho(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {

            return null;
        }
    },
    /**
     * IC卡参数下载结束
     */
    EMV_PARAM_DOWN_END("0800", "", "", "", "00", "351", FinancialApplication.getAppContext()
            .getString(R.string.emv_param_down_end), false, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {

            return new PackEmvParamDownload(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {

            return null;
        }

    },
    /**
     * 黑名单下载
     */
    BLACK_DOWN("0800", "", "", "", "00", "390", FinancialApplication.getAppContext().getString(R.string.black_down), false, false,
            false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {

            return new PackBlackDownload(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {

            return null;
        }
    },

    /**
     * 黑名单下载结束
     */
    BLACK_DOWN_END("0800", "", "", "", "00", "391", FinancialApplication.getAppContext().getString(R.string.black_down_end),
            false, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {

            return new PackEmvParamDownload(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {

            return null;
        }
    },
    /**
     * 汇率下载
     */
    RATE_DOWN("0300", "", "", "", "01", "800", FinancialApplication.getAppContext().getString(R.string.rate_down), false, false,
            false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {

            return new PackRateDownload(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {

            return null;
        }
    },

    /**
     * IC卡脚本结果上送
     */
    IC_SCR_SEND("0620", "", "", "", "00", "951", FinancialApplication.getAppContext().getString(R.string.ic_scr_send), false,
            false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {

            return new PackICScript(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {

            return null;
        }
    },
    /**
     * 磁条卡参数下载
     */
    DOWNLOAD_PARAM("0800", "", "", "", "00", "360", FinancialApplication.getAppContext().getString(R.string.download_param),
            false, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {

            return new PackDownloadParam(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {

            return null;
        }
    },

    /**
     * 非接参数下载
     */
    PICC_DOWNLOAD_PARAM("0800", "", "", "", "00", "394", FinancialApplication.getAppContext()
            .getString(R.string.picc_download_param), false, false, false) {

        @Override
        public PackIso8583 getpackager(PackListener listener) {

            return new PackDownloadParam(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {

            return null;
        }
    },
    /**
     * BIN B
     */
    BIN_B_DOWNLOAD("0800", "", "", "", "00", "396", FinancialApplication.getAppContext().getString(R.string.bin_b_download),
            false, false, false) {

        @Override
        public PackIso8583 getpackager(PackListener listener) {

            return new PackBinDownload(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {

            return null;
        }
    },
    /**
     * BIN B
     */
    BIN_B_DOWNLOAD_END("0800", "", "", "", "00", "397", FinancialApplication.getAppContext()
            .getString(R.string.bin_b_download_end), false, false, false) {

        @Override
        public PackIso8583 getpackager(PackListener listener) {

            return new PackBinDownload(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {

            return null;
        }
    },
    /**
     * BIN C
     */
    BIN_C_DOWNLOAD("0800", "", "", "", "00", "398", FinancialApplication.getAppContext().getString(R.string.bin_c_download),
            false, false, false) {

        @Override
        public PackIso8583 getpackager(PackListener listener) {

            return new PackBinDownload(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {

            return null;
        }
    },
    /**
     * BIN C
     */
    BIN_C_DOWNLOAD_END("0800", "", "", "", "00", "399", FinancialApplication.getAppContext()
            .getString(R.string.bin_c_download_end), false, false, false) {

        @Override
        public PackIso8583 getpackager(PackListener listener) {

            return new PackBinDownload(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {

            return null;
        }
    },
    /**
     * 非接参数下载结束
     */
    PICC_DOWNLOAD_PARAM_END("0800", "", "", "", "00", "395", FinancialApplication.getAppContext()
            .getString(R.string.picc_download_param_end), false, false, false) {

        @Override
        public PackIso8583 getpackager(PackListener listener) {

            return new PackDownloadParam(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {

            return null;
        }
    },

    SETTLE("0500", "", "", "", "00", "201", FinancialApplication.getAppContext().getString(R.string.settle), true, true, true) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackSettle(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {

            return null;
        }

    },

    BATCH_UP("0320", "", "", "", "00", "201", FinancialApplication.getAppContext().getString(R.string.batch_up), false, false,
            false) {

        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackBatchUp(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }

    },
    /**
     * 通知类交易披上送，包括 退货、预授权完成通知、离线结算、结算调整、结算调整小费
     */

    NOTICE_TRANS_BAT("0320", "", "200000", "00", "25", "000", FinancialApplication.getAppContext().getString(R.string.batch_up),
            false, false, false) {

        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackBatchUpNotice(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }
    },

    IC_TC_BAT("0320", "", "", "", "00", "203", FinancialApplication.getAppContext().getString(R.string.batch_up), false, false,
            false) {

        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackIcTcBat(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }

    },
    IC_FAIL_BAT("0320", "", "", "", "00", "204", FinancialApplication.getAppContext().getString(R.string.batch_up), false, false,
            false) {

        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackIcTcBat(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }

    },
    BATCH_UP_END("0320", "", "", "", "00", "207", FinancialApplication.getAppContext().getString(R.string.batch_up_end), false,
            false, false) {

        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackBatchUp(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }

    },
    /************************************************ 交易类 ****************************************************/

    SALE("0200", "0400", "000000", "00", "22", "000", FinancialApplication.getAppContext().getString(R.string.sale_trans), true,
            true, true) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {

            return new PackSale(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {

            return new PackReveral(listener);
        }
    },

    // 扫码支付
    QR_SALE("0200", "0400", "000000", "00", "22", "000", FinancialApplication.getAppContext().getString(R.string.sale_scan), true,
            true, true) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {

            return new PackQrSale(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {

            return new PackReveral(listener);
        }
    },

    QUERY("0200", "", "310000", "00", "01", "000", FinancialApplication.getAppContext().getString(R.string.trans_balance), true,
            true, true) {

        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackBalance(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }
    },
    /**********************************************************************************************************/
    VOID("0200", "0400", "200000", "00", "23", "000", FinancialApplication.getAppContext().getString(R.string.void_trans), true,
            true, true) {

        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackSaleVoid(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return new PackReveral(listener);
        }

    },

    QR_VOID("0200", "0400", "200000", "00", "23", "000", FinancialApplication.getAppContext().getString(R.string.scan_code_void),
            true, true, true) {

        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackQrSaleVoid(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }

    },
    /**********************************************************************************************************/
    REFUND("0220", "0420", "200000", "00", "25", "000", FinancialApplication.getAppContext().getString(R.string.trans_refund),
            true, true, true) {

        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackRefund(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }

    },
    QR_REFUND("0220", "", "200000", "00", "25", "000", FinancialApplication.getAppContext().getString(R.string.scan_code_refund),
            true, true, true) {

        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackQrRefund(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }

    },
    /******************************************** INSTALLMENT *************************************************/
    INSTAL_SALE("0200", "0400", "000000", "64", "22", "000", FinancialApplication.getAppContext().getString(R.string.installment_Sale), true,
            true, true) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {

            return new PackInstalSale(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {

            return new PackReveral(listener);
        }
    },

    INSTAL_VOID("0200", "0400", "200000", "64", "23", "000", FinancialApplication.getAppContext().getString(R.string.installment_Void), true,
            true, true) {

        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackInstalVoid(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return new PackReveral(listener);
        }

    },

    /**********************************************************************************************************/
    AUTH("0100", "0400", "030000", "06", "10", "000", FinancialApplication.getAppContext().getString(R.string.auth_trans), true,
            true, true) {

        @Override
        public PackIso8583 getpackager(PackListener listener) {

            return new PackAuth(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {

            return new PackReveral(listener);
        }

    },
    /************************************************************************************************************/
    AUTHCM("0200", "0400", "000000", "06", "20", "000", FinancialApplication.getAppContext().getString(R.string.auth_cm), true,
            true, true) {

        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackAuthCM(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return new PackReveral(listener);
        }

    },
    /***************************************************************************************************************/
    AUTHCMVOID("0200", "0400", "200000", "06", "21", "000", FinancialApplication.getAppContext()
            .getString(R.string.auth_cm_void_all), true, true, true) {

        @Override
        public PackIso8583 getpackager(PackListener listener) {

            return new PackAuthCMVoid(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {

            return new PackReveral(listener);
        }

    },
    /************************************************************************************************************/
    AUTH_SETTLEMENT("0220", "", "000000", "06", "24", "000", FinancialApplication.getAppContext()
            .getString(R.string.auth_cm_adv_all), true, true, true) {

        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackAuthCM(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }

    },
    /************************************************************************************************************/
    AUTHVOID("0100", "0400", "200000", "06", "11", "000", FinancialApplication.getAppContext().getString(R.string.auth_void),
            true, true, true) {

        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackAuthVoid(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return new PackReveral(listener);
        }

    },

    READCARDNO("", "", "000000", "", "", "", FinancialApplication.getAppContext().getString(R.string.trans_readcard), false,
            false, false) {

        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return null;
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }

    },
    /********************************************************************************************/
    EC_SALE("0200", "", "000000", "00", "22", "000", FinancialApplication.getAppContext().getString(R.string.ec_sale), true, true,
            true) {

        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackEcSale(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return new PackReveral(listener);
        }

    },
    EC_QUERY("0200", "", "310000", "00", "01", "000", FinancialApplication.getAppContext().getString(R.string.ec_cash_balance),
            true, true, true) {

        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return null;
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }
    },
    /********************************************************************************************/
    EC_LOAD("0200", "", "600000", "91", "45", "000",
            FinancialApplication.getAppContext().getString(R.string.qc_allocated_account), true, true, true) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return null;
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }

    },
    EC_TRANSFER_LOAD_OUT("0200", "", "620000", "91", "47", "000", FinancialApplication.getAppContext()
            .getString(R.string.qc_non_allocated_account), true, true, true) {

        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return null;
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }
    },

    EC_TRANSFER_LOAD("0200", "", "620000", "91", "47", "000", FinancialApplication.getAppContext()
            .getString(R.string.qc_non_allocated_account), true, true, true) {

        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return null;
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }

    },
    EC_DETAIL("", "", "000000", "", "", "", FinancialApplication.getAppContext().getString(R.string.ec_details_query), true, true, true) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return null;
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }
    },
    EC_LOAD_DETAIL("", "", "", "", "", "", FinancialApplication.getAppContext().getString(R.string.ec_load_detail), true, true,
            true) {

        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return null;
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }
    },
    EC_REFUND("0220", "", "200000", "00", "27", "000", FinancialApplication.getAppContext().getString(R.string.ec_cash_refund),
            true, true, true) {

        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackEcRefund(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }

    },

    EC_CASH_LOAD("0200", "", "630000", "91", "46", "000", FinancialApplication.getAppContext().getString(R.string.qc_cash_charge),
            true, true, true) {

        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return null;
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }
    },
    EC_CASH_LOAD_VOID("0200", "0400", "170000", "91", "51", "000", FinancialApplication.getAppContext()
            .getString(R.string.qc_cash_charge_void), true, true, true) {

        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return null;
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return new PackReveral(listener);
        }
    },

    /***************************************************************************************************************/
    MOTO_SALE("0200", "0400", "000000", "08", "22", "000", FinancialApplication.getAppContext().getString(R.string.trans_moto_sale), true,
            true, true) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {

            return new PackMotoSale(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {

            return new PackReveral(listener);
        }
    },

    MOTO_VOID("0200", "0400", "200000", "08", "23", "000", FinancialApplication.getAppContext().getString(R.string.trans_moto_void), true,
            true, true) {

        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackSaleVoid(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return new PackReveral(listener);
        }

    },

    MOTO_REFUND("0220", "0420", "200000", "08", "25", "000", FinancialApplication.getAppContext().getString(R.string.trans_moto_refund),
            true, true, true) {

        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackRefund(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }

    },

    MOTO_AUTH("0100", "0400", "030000", "18", "10", "000", FinancialApplication.getAppContext().getString(R.string.trans_moto_preauth), true,
            true, true) {

        @Override
        public PackIso8583 getpackager(PackListener listener) {

            return new PackAuth(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {

            return new PackReveral(listener);
        }
    },

    MOTO_AUTHCM("0200", "0400", "000000", "18", "20", "000", FinancialApplication.getAppContext().getString(R.string.trans_moto_preauth_comp), true,
            true, true) {

        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackAuthCM(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return new PackReveral(listener);
        }

    },

    MOTO_AUTHCMVOID("0200", "0400", "200000", "18", "21", "000", FinancialApplication.getAppContext()
            .getString(R.string.trans_moto_preauth_comp_void), true, true, true) {

        @Override
        public PackIso8583 getpackager(PackListener listener) {

            return new PackAuthCMVoid(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {

            return new PackReveral(listener);
        }

    },

    MOTO_AUTH_SETTLEMENT("0220", "", "000000", "18", "24", "000", FinancialApplication.getAppContext()
            .getString(R.string.trans_moto_preauth_comp_advise), true, true, true) {

        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackAuthCM(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }

    },

    MOTO_AUTHVOID("0100", "0400", "200000", "18", "11", "000", FinancialApplication.getAppContext().getString(R.string.trans_moto_preauth_void),
            true, true, true) {

        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackAuthVoid(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return new PackReveral(listener);
        }

    },

    /***************************************************************************************************************/
    RECURRING_SALE("0200", "0400", "000000", "28", "22", "000", FinancialApplication.getAppContext().getString(R.string.trans_recurring), true,
            true, true) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {

            return new PackSale(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {

            return new PackReveral(listener);
        }
    },

    RECURRING_VOID("0200", "0400", "200000", "28", "23", "000", FinancialApplication.getAppContext().getString(R.string.trans_recurring_void), true,
            true, true) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackSaleVoid(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return new PackReveral(listener);
        }
    },

    /***************************************************************************************************************/
    OFFLINE_TRANS_SEND("0200", "", "000000", "00", "36", "000", FinancialApplication.getAppContext()
            .getString(R.string.offline_trans_send), true, true, true) {

        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackOfflineTransSend(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }
    },
    /***************************************************************************************************************/
    OFFLINE_TRANS_SEND_BAT("0320", "", "000000", "00", "36", "000", FinancialApplication.getAppContext()
            .getString(R.string.offline_trans_send_bat), true, true, true) {

        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackOfflineBat(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }
    },

    /*************************************************************************************/
    SIG_SEND("0820", "", "", "", "07", "800", FinancialApplication.getAppContext().getString(R.string.sign_send), true, true, true) {

        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackSignatureUpload(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }
    },

    /*************************************************************************************/
    OFFLINE_SETTLE("0220", "", "000000", "00", "30", "000", FinancialApplication.getAppContext()
            .getString(R.string.offline_settle), true, true, true) {

        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackSettleOffline(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }
    },
    SETTLE_ADJUST("0220", "", "000000", "00", "32", "000", FinancialApplication.getAppContext().getString(R.string.settle_adjust),
            true, true, true) {

        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackSettleAdjust(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }
    },
    SETTLE_ADJUST_TIP("0220", "", "000000", "00", "34", "000", FinancialApplication.getAppContext()
            .getString(R.string.settle_adjust), true, true, true) {

        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackSettleAdjust(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }
    },

    COUPON_VERIFY("0200", "0400", "000000", "63", "22", "000", FinancialApplication.getAppContext()
            .getString(R.string.coupon_sale), true, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackCouponVerify(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return new PackCouponVerifyReversal(listener);
        }
    },

    COUPON_SALE("0200", "0400", "000000", "00", "22", "000", FinancialApplication.getAppContext().getString(R
            .string.coupon_sale), true, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackCouponSale(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return new PackCouponReversal(listener);
        }
    },

    COUPON_SALE_VOID("0200", "0400", "200000", "00", "23", "000",
            FinancialApplication.getAppContext().getString(R.string.coupon_void), true, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackCouponSaleVoid(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return new PackCouponReversal(listener);
        }
    },

    COUPON_VERIFY_VOID("0400", "0400", "000000", "63", "22", "000",
            FinancialApplication.getAppContext().getString(R.string.coupon_void), false, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackCouponVerifyReversal(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }
    },

    EMV_QR_SALE("0200", "0400", "000000", "00", "22", "000",
            FinancialApplication.getAppContext().getString(R.string.sale_trans_qr), true, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackSale(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return new PackReveral(listener);
        }
    },



    /**
     * @param msgType：消息类型码
     * @param dupMsgType: 冲正消息类型码
     * @param procCode: 处理码
     * @param serviceCode：服务码
     * @param funcCode：功能码
     * @param netCode：网络管理码
     * @param transName: 交易名称
     * @param isDupSend：是否冲正上送
     * @param isScriptSend：是否脚本结果上送
     * @param isOfflineSend：是否脱机交易上送
     */
    EMV_QR_VOID("0200", "0400", "200000", "00", "23", "000",
            FinancialApplication.getAppContext().getString(R.string.void_trans), true, true, true) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackSaleVoid(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return new PackReveral(listener);
        }
    },
    DANA_QR_SALE("0200", "0400", "000020", "00", "22", "000",
                FinancialApplication.getAppContext().getString(R.string.qris_trans_qr), true, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackQRSaleQris(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return new PackReveral(listener);
        }
    },
    DANA_QR_VOID("0200", "0400", "200020", "00", "22", "000",
            FinancialApplication.getAppContext().getString(R.string.qris_void_trans_qr), true, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackQRSaleQrisVoid(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return new PackReveral(listener);
        }
    },

    QR_GENERATE("0800", "0400", "000000", "00", "63", "000", FinancialApplication.getAppContext().getString(R.string.qr_generate), true,true, true) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackQRGenerate(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }
    },
    QR_INQUIRY("0800", "0400", "000000", "00", "64", "000", FinancialApplication.getAppContext().getString(R.string.qr_inquiry), true, true, true) {

        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackQRInquiry(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }
    },


    ACCOUNT_LIST("0200", "0400", "319900", "63", "22", "000", FinancialApplication.getAppContext()
            .getString(R.string.trans_account_list), true, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackAccountList(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }
    },

    BALANCE_INQUIRY("0200", "0400", "301000", "63", "22", "000", FinancialApplication.getAppContext()
            .getString(R.string.trans_balance_information), true, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackBalanceInquiry(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return new PackBalanceInquiryRevers(listener);
        }
    },

    BALANCE_INQUIRY_2("0200", "0400", "302000", "63", "22", "000", FinancialApplication.getAppContext()
            .getString(R.string.trans_balance_information), true, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackBalanceInquiry(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return new PackBalanceInquiryRevers(listener);
        }
    },




    OVERBOOK_INQUIRY("0200", "0400", "391000", "63", "22", "000", FinancialApplication.getAppContext()
            .getString(R.string.trans_overbooking), true, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackOverbookingInquiry(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }
    },


    SETOR_TUNAI("0200", "0400", "210010", "00", "22", "000", FinancialApplication.getAppContext()
            .getString(R.string.trans_deposit), true, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackSetorTunai(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return new PackSetorTunaiReversal(listener);
        }
    },

    TARIK_TUNAI("0200", "0400", "011000", "00", "22", "000", FinancialApplication.getAppContext()
            .getString(R.string.trans_withdrawal), true, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackTarikTunai(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return new PackTarikTunaiReversal(listener);
        }
    },

    TARIK_TUNAI_2("0200", "0400", "012000", "00", "22", "000", FinancialApplication.getAppContext()
            .getString(R.string.trans_withdrawal), true, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackTarikTunai(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return new PackTarikTunaiReversal(listener);
        }
    },





    MINISTATEMENT("0200", "0400", "380000", "00", "22", "000", FinancialApplication.getAppContext()
            .getString(R.string.trans_mini_statement), true, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackMiniStatement(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return new PackReversalMinistatement(listener);
        }
    },

    VERIFY_PIN("0100", "", "000000", "00", "22", "000", FinancialApplication.getAppContext()
            .getString(R.string.trans_change_pin), true, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackVerifyPin(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }
    },

    CHANGE_PIN("0100", "", "040000", "00", "22", "000", FinancialApplication.getAppContext()
            .getString(R.string.trans_change_pin), false, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackVerifyPin(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }
    },
    //inquiry transfer sesama bank
    OVERBOOK_INQUIRY1("0200", "0400", "391000", "63", "22", "000", FinancialApplication.getAppContext()
            .getString(R.string.trans_transfer_sesama), false, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackOverbookingInquiry1(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }
    },
    OVERBOOK_INQUIRY2("0200", "0400", "392000", "63", "22", "000", FinancialApplication.getAppContext()
            .getString(R.string.trans_transfer_sesama), false, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackOverbookingInquiry1(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }
    },
    //transfer antar rekening
    OVERBOOKING("0200", "0400", "401000", "00", "22", "000", FinancialApplication.getAppContext()
            .getString(R.string.trans_transfer_sesama), true, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackOverbooking(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return new PackTransferAntarRekeningReversal(listener);
        }
    },
    OVERBOOKING_2("0200", "0400", "402000", "00", "22", "000", FinancialApplication.getAppContext()
            .getString(R.string.trans_transfer_sesama), true, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackOverbooking(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return new PackTransferAntarRekeningReversal(listener);
        }
    },


    PEMBUKAAN_REK("0200", "0400", "510000", "00", "22", "000", FinancialApplication.getAppContext()
            .getString(R.string.trans_opening_account), true, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackPembukaanRekening(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return new PackPembukaanRekeningReversal(listener);
//            return null;
        }
    },

    PEMBATAL_REK_INQ("0200", "", "320000", "00", "22", "000", FinancialApplication.getAppContext()
            .getString(R.string.trans_cancelation_account), true, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackPembatalanRekInq(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }
    },

    PEMBATAL_REK("0200", "0400", "520000", "00", "22", "000", FinancialApplication.getAppContext()
            .getString(R.string.trans_cancelation_account), true, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackPembatalanRek(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return new PackPembatalanRekReversal(listener);
//            return null;
        }
    },

    TRANSFER_INQ("0200", "", "391010", "00", "22", "000", FinancialApplication.getAppContext()
            .getString(R.string.trans_fund_transfer), true, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackTransferInq(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }
    },
    TRANSFER_INQ_2("0200", "", "392010", "00", "22", "000", FinancialApplication.getAppContext()
            .getString(R.string.trans_fund_transfer), true, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackTransferInq(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }
    },


    TRANSFER("0200", "0400", "401020", "00", "22", "000", FinancialApplication.getAppContext()
            .getString(R.string.trans_fund_transfer), true, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackTransfer(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
//            return null;
            return new PackTransferReversal(listener);
        }
    },
    TRANSFER_2("0200", "0400", "402020", "00", "22", "000", FinancialApplication.getAppContext()
            .getString(R.string.trans_fund_transfer), true, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackTransfer(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
//            return null;
            return new PackTransferReversal(listener);
        }
    },



    CETAK_ULANG("0200", "0400", "400100", "00", "22", "000", FinancialApplication.getAppContext()
            .getString(R.string.trans_cetak_ulang), false, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackCetakUlang(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }
    },



    DIRJEN_PAJAK_INQUIRY("0200", "0400", "310100", "00", "22", "000", FinancialApplication.getAppContext()
            .getString(R.string.trans_djp), true, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackMpnInquiry(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }
    },

    DIRJEN_PAJAK("0200", "0400", "510100", "00", "22", "000", FinancialApplication.getAppContext()
            .getString(R.string.trans_djp), false, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackMpn(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }
    },

    DIRJEN_BEA_CUKAI_INQUIRY("0200", "0400", "310100", "00", "22", "000", FinancialApplication.getAppContext()
            .getString(R.string.trans_djbc), true, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackMpnInquiry(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }
    },

    DIRJEN_BEA_CUKAI("0200", "0400", "510100", "00", "22", "000", FinancialApplication.getAppContext()
            .getString(R.string.trans_djbc), false, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackMpn(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }
    },


    DIRJEN_ANGGARAN_INQUIRY("0200", "0400", "310100", "00", "22", "000", FinancialApplication.getAppContext()
            .getString(R.string.trans_dja), true, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackMpnInquiry(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }
    },

    DIRJEN_ANGGARAN("0200", "0400", "510100", "00", "22", "000", FinancialApplication.getAppContext()
            .getString(R.string.trans_dja), false, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackMpn(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }
    },

    PBB_INQ("0200", "", "341019", "00", "22", "000", FinancialApplication.getAppContext()
            .getString(R.string.trans_pbb_p2), false, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackPBBInquiry(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }
    },

    PBB_PAY("0200", "", "541019", "00", "22", "000", FinancialApplication.getAppContext()
            .getString(R.string.trans_pbb_p2), false, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackPBBInquiry(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }
    },

    // PPOB
    TOTAL_PRODUCT_PULSA_DATA("0100", "", "111171", "00", "22", "000",
            FinancialApplication.getAppContext().getString(R.string.trans_download_pulsa_and_data), true, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackTotalProductPulsaData(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }
    },


    DOWNLOAD_PRODUCT_PULSA_DATA("0100", "", "111172", "00", "22", "000",
            FinancialApplication.getAppContext().getString(R.string.trans_download_pulsa_and_data), false, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackDownloadProductPulsaData(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }
    },

    OVERBOOKING_PULSA_DATA("0200", "0400", "500000", "00", "22", "000", FinancialApplication.getAppContext()
            .getString(R.string.trans_prabayar), true, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackOverbookingPulsaData(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return new PackOverbookingReversal(listener);
        }
    },

    INQ_PULSA_DATA("0100", "", "200065", "00", "22", "000", FinancialApplication.getAppContext()
            .getString(R.string.trans_prabayar), true, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackInqPulsaData(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }
    },

    PURCHASE_PULSA_DATA("0100", "", "100065", "00", "22", "000", FinancialApplication.getAppContext()
            .getString(R.string.trans_prabayar), true, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackPurchasePulsaData(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }
    },

    E_SAMSAT_INQUIRY("0200", "", "301099", "00", "22", "000", FinancialApplication.getAppContext()
            .getString(R.string.trans_esamsat), true, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackESamsat(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }
    },

    E_SAMSAT("0200", "", "541099", "00", "22", "000", FinancialApplication.getAppContext()
            .getString(R.string.trans_esamsat), true, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackESamsat(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }
    },

    //ppob inquiry
    PDAM_INQUIRY("0100", "", "400065", "00", "22", "000", FinancialApplication.getAppContext()
            .getString(R.string.trans_pdam), true, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackInqPulsaData(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }
    },

    PDAM_PURCHASE("0100", "", "300065", "00", "22", "000", FinancialApplication.getAppContext()
            .getString(R.string.trans_pdam), true, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackPurchasePulsaData(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }
    },
    PDAM_OVERBOOKING("0200", "0400", "500000", "00", "22", "000", FinancialApplication.getAppContext()
            .getString(R.string.trans_pdam), true, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackOverbookingPulsaData(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return new PackOverbookingPDAMReversal(listener);
        }
    },

    PASCABAYAR_INQUIRY("0100", "", "400065", "00", "22", "000", FinancialApplication.getAppContext()
            .getString(R.string.trans_pascabayar), true, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackInqPulsaData(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }
    },

    PASCABAYAR_PURCHASE("0100", "", "300065", "00", "22", "000", FinancialApplication.getAppContext()
            .getString(R.string.trans_pascabayar), true, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackPurchasePulsaData(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }
    },
    PASCABAYAR_OVERBOOKING("0200", "0400", "500000", "00", "22", "000", FinancialApplication.getAppContext()
            .getString(R.string.trans_pascabayar), true, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackOverbookingPulsaData(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return new PackOverbookingPascabayarReversal(listener);
        }
    },
    REDEEM_POIN_DATA_INQ("0100", "", "100067", "00", "22", "000",
            FinancialApplication.getAppContext().getString(R.string.trans_redeem), false, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackInquiryVoucher(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }
    },REDEEM_POIN_DATA_PAY("0100", "", "200067", "00", "22", "000",
            FinancialApplication.getAppContext().getString(R.string.trans_redeem), false, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackPaymentVoucher(listener);
        }
        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }
    },BPJS_TK_VERIFICATION("0100", "0400", "800069", "00", "22", "000", FinancialApplication.getAppContext()
            .getString(R.string.detail_bpjs_tk_verification), false, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackBpjsTkRegister(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }
    },
    BPJS_TK_PENDAFTARAN("0100", "0400", "100069", "00", "22", "000", FinancialApplication.getAppContext()
            .getString(R.string.detail_bpjs_tk_pendaftaran), true, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackBpjsTkRegister(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }
    },BPJS_TK_INQUIRY("0100", "0400", "200069", "00", "22", "000", FinancialApplication.getAppContext()
            .getString(R.string.detail_bpjs_tk_inquiry), false, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackBpjsTkInquiry(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }

    },BPJS_TK_PEMBAYARAN("0100", "0400", "300069", "00", "22", "000", FinancialApplication.getAppContext()
            .getString(R.string.detail_bpjs_tk_pembayaran), false, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackBpjsTkPayment(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }
    },BPJS_OVERBOOKING("0200", "0400", "500000", "00", "22", "000", FinancialApplication.getAppContext()
            .getString(R.string.trans_bpjs_tk), true, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackOverbookingPulsaData(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return new PackOverbookingBPJSTkReversal(listener);
        }
    },TOTAL_PRODUCT_BPJS_TK("0100", "", "400069", "00", "22", "000",
            FinancialApplication.getAppContext().getString(R.string.detail_bpjs_download), false, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackTotalProductPulsaData(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }
    },
    DOWNLOAD_LOCATION_DATA_BPJS_TK("0100", "", "500069", "00", "22", "000",
            FinancialApplication.getAppContext().getString(R.string.trans_bpjs_location), false, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackDownloadProductPulsaData(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }
    },DOWNLOAD_BRANCH_OFFICE_DATA_BPJS_TK("0100", "", "600069", "00", "22", "000",
            FinancialApplication.getAppContext().getString(R.string.trans_bpjs_branch_office), false, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackDownloadProductPulsaData(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }
    },DOWNLOAD_DISTRICT_DATA_BPJS_TK("0100", "", "700069", "00", "22", "000",
            FinancialApplication.getAppContext().getString(R.string.trans_bpjs_district_data), false, false, false) {
        @Override
        public PackIso8583 getpackager(PackListener listener) {
            return new PackDownloadProductPulsaData(listener);
        }

        @Override
        public PackIso8583 getDupPackager(PackListener listener) {
            return null;
        }
    }
    ;

    private String msgType;
    private String dupMsgType;
    private String procCode;
    private String serviceCode;
    private String funcCode;
    private String netCode;
    private String transName;
    private boolean isDupSend;
    private boolean isScriptSend;
    private boolean isOfflineSend;

    /**
     *
     * @param msgType：消息类型码
     * @param dupMsgType: 冲正消息类型码
     * @param procCode: 处理码
     * @param serviceCode：服务码
     * @param funcCode：功能码
     * @param netCode：网络管理码
     * @param transName: 交易名称
     * @param isDupSend：是否冲正上送
     * @param isScriptSend：是否脚本结果上送
     * @param isOfflineSend：是否脱机交易上送
     */
    ETransType(String msgType, String dupMsgType, String procCode, String serviceCode,
                      String funcCode,
            String netCode, String transName, boolean isDupSend, boolean isScriptSend, boolean isOfflineSend) {
        this.msgType = msgType;
        this.dupMsgType = dupMsgType;
        this.procCode = procCode;
        this.serviceCode = serviceCode;
        this.funcCode = funcCode;
        this.netCode = netCode;
        this.transName = transName;
        this.isDupSend = isDupSend;
        this.isScriptSend = isScriptSend;
        this.isOfflineSend = isOfflineSend;
    }

    public String getMsgType() {
        return msgType;
    }

    public String getDupMsgType() {
        return dupMsgType;
    }

    public String getProcCode() {
        return procCode;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public String getFuncCode() {
        return funcCode;
    }

    public String getNetCode() {
        return netCode;
    }

    public String getTransName() {
        return transName;
    }

    public boolean isDupSend() {
        return isDupSend;
    }

    public boolean isScriptSend() {
        return isScriptSend;
    }

    public boolean isOfflineSend() {
        return isOfflineSend;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public void setDupMsgType(String dupMsgType) {
        this.dupMsgType = dupMsgType;
    }

    public void setProcCode(String procCode) {
        this.procCode = procCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public void setFuncCode(String funcCode) {
        this.funcCode = funcCode;
    }

    public void setNetCode(String netCode) {
        this.netCode = netCode;
    }

    public void setTransName(String transName) {
        this.transName = transName;
    }

    public void setDupSend(boolean isDupSend) {
        this.isDupSend = isDupSend;
    }

    public void setScriptSend(boolean isScriptSend) {
        this.isScriptSend = isScriptSend;
    }

    public void setOfflineSend(boolean isOfflineSend) {
        this.isOfflineSend = isOfflineSend;
    }

    public abstract PackIso8583 getpackager(PackListener listener);

    public abstract PackIso8583 getDupPackager(PackListener listener);

}