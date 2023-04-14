package com.pax.pay.record;

public class TransDBHelper {

	public static final String TB_NAME = "trans";
	public static final String ID = "id";
	public static final String ACQ_CENTER_CODE = "acqCenterCode";
	public static final String ACQ_CODE = "acqCode";
	public static final String AID = "aid";
	public static final String AMOUNT = "amount";
	public static final String SELL_PRICE = "sellPrice";
	
	public static final String ARPC = "arpc";
	public static final String ARQC = "arqc";
	public static final String ATC = "atc";
	public static final String AUTH_CODE = "authCode";
	public static final String AUTH_INSCODE = "authInsCode";
	public static final String AUTH_MODE = "authMode";
	public static final String BALANCE = "balance";
	public static final String BALANCE_FLAG = "balanceFlag";
	
	public static final String TVR = "tvr";
	public static final String C2B = "c2b";
	public static final String C2B_VOUCHER = "c2bVoucher";
	public static final String CARD_SERIALNO = "cardSerialNo";
	public static final String CENTER_RESP = "centerResp";
	public static final String DATE = "date";
	
	public static final String DUP_ICC_DATA = "dupIccData";
	public static final String EMV_APP_LABEL = "emvAppLabel";
	public static final String EMV_APP_NAME = "emvAppName";
	public static final String TSI = "tsi";
	public static final String TRANSFER_PAN = "transferPan";
	public static final String EXP_DATE = "expDate";
	public static final String TRANS_TYPE = "transType";
	
	public static final String INTER_ORG_CODE = "interOrgCode";
	public static final String TRANS_STATE = "transState";
	public static final String TRACK3 = "track3";
	public static final String TRACK2 = "track2";
	public static final String TRACK1 = "track1";
	public static final String TIP_AMOUNT = "tipAmount";
	public static final String TIME = "time";
	public static final String ISSER_CODE = "isserCode";
	public static final String ISSUER_RESP = "issuerResp";
	public static final String ORIG_AUTH_CODE = "origAuthCode";
	
	public static final String TC = "tc";
	public static final String ORIG_C2B_VOUCHER = "origC2bVoucher";
	public static final String ORIG_DATE = "origDate";
	public static final String ORIG_REFNO = "origRefNo";
	public static final String ORIG_TERMID = "origTermID";
	public static final String ORIG_TRANS_TYPE = "origTransType";
	public static final String PAN = "pan";
	public static final String PHONE_NO = "phoneNo";
	public static final String SIGN_DATA = "signData";
	
	public static final String PROC_CODE = "procCode";
	public static final String REASON = "reason";
	public static final String RECV_BANK_RESP = "recvBankResp";
	public static final String REFNO = "refNo";
	public static final String RESERVED = "reserved";
	public static final String SCRIPT_DATA = "scriptData";
	public static final String SETTLE_DATE = "settleDate";
	public static final String SEND_ICC_DATA = "sendIccData";
	public static final String SEND_TIMES = "sendTimes";
	public static final String BATCHNO = "batchNo";
	public static final String TRANSNO = "transNo";
	public static final String DUP_TRANSNO = "dupTransNo";
	public static final String ORIG_BATCHNO = "origBatchNo";
	public static final String ORIG_TRANSNO = "origTransNo";
	
	public static final String SIGN_UPLOAD = "signUpload";
	public static final String SIGN_SEND_STATE = "signSendState";
	public static final String SIGN_FREE = "signFree";
	public static final String IS_UPLOAD = "isUpload";
	public static final String IS_ONLINE_TRANS = "isOnlineTrans";
	public static final String IS_OFF_UPLOAD_STATE = "isOffUploadState";
	public static final String IS_ENCTRACK = "isEncTrack";
	public static final String IS_CDCVM = "isCDCVM";
	public static final String PIN_FREE = "pinFree";
	
	public static final String IS_ADJUST_AFTER_UPLOAD = "isAdjustAfterUpload";
	public static final String HAS_PIN = "hasPin";
	public static final String TRANSFER_ENTER_MODE = "transferEnterMode";
	public static final String ENTER_MODE = "enterMode";
	public static final String EMV_RESULT = "emvResult";
	public static final String SEND_FAIL_FLAG = "sendFailFlag";

	//The following is Added by Steven.T 2017-6-7 16:50:53
	public static final String CARD_CURRENCY_CODE = "cardCurrencyCode";
	public static final String TERMINAL_CURRENCY_CODE = "terminalCurrencyCode";
	public static final String TERMINAL_CURRENCY_NAME = "terminalCurrencyName";
	public static final String TERMINAL_CURRENCY_DECIMALS = "termianlCurrencyDecimals";
	public static final String CURRENCY_RATE = "currencyRate";

	public static final String INSTAL_NUM = "instalNum";   //分期付款的分期数  L=3
	public static final String PRJ_CODE = "prjCode";      //分期付款的项目编码     L=0~30
	public static final String FIRST_AMOUNT = "firstAmount"; //分期付款的首付金额,(BCD)   L=12
	public static final String FEE_TOTAL_AMOUNT = "feeTotalAmount";  //分期付款手续费总金额/一次性付款手续费   L=1
	public static final String INSTAL_CURRCODE = "instalCurrCode";  //分期付款币种       344  L=3

	//sandy
	public static final String DISCOUNT_AMOUNT = "discountAmount";  //
	public static final String ACTUAL_AMOUNT = "actualPayAmount";  //
	public static final String COUPON_NO = "couponNo";  //
	public static final String DATE_TIME = "dateTimeTrans";  //
	public static final String ORIG_COUPON_NO = "origCouponRefNo";  //
	public static final String ORIG_DATE_TIME = "origDateTimeTrans";  //
	public static final String ORIG_COUPON_DATE_TIME = "origCouponDateTimeTrans";  //
	public static final String FIELD110 = "field110";

	//tri
	public static final String REPRINT_DATA = "reprintData";
	public static final String BILLING_ID = "billingId";
	public static final String F102 = "field102";
	public static final String F103 = "field103";
	public static final String NTB = "ntb";
	public static final String DEST_BANK = "destBank";
	public static final String FIELD59 = "field59";
	public static final String FIELD28 = "field28";
	public static final String FIELD127 = "field127";
	public static final String FIELD36 = "field36";
	public static final String FIELD48 = "field48";

	//end
	public static final String F47 = "field47";
	public static final String ACC_NO = "accNo";
	public static final String FIELD61 = "field61";
	public static final String F48 = "field48";
	public static final String PHONE_NUMBER = "phoneNo";
	public static final String PRODUCT_CODE = "product_code";
	public static final String TYPE_PRODUCT = "type_product";
	public static final String OPERATOR = "operator";
	public static final String KETERANGAN = "keterangan";
	public static final String PRINT_TIMEOUT = "printTimeout";
}
