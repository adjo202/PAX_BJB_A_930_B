package com.pax.pay;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;

import com.pax.abl.core.AAction;
import com.pax.abl.core.AAction.ActionEndListener;
import com.pax.abl.core.AAction.ActionStartListener;
import com.pax.abl.core.ATransaction;
import com.pax.abl.core.ATransaction.TransEndListener;
import com.pax.abl.core.ActionResult;
import com.pax.device.Device;
import com.pax.pay.app.ActivityStack;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.constant.Constants;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.record.Printer;
import com.pax.pay.record.TransQueryActivity;
import com.pax.pay.service.ParseReq;
import com.pax.pay.service.ParseReq.RequestData;
import com.pax.pay.service.ParseResp;
import com.pax.pay.service.Payment;
import com.pax.pay.service.ServiceConstant;
import com.pax.pay.trans.AuthCMTrans;
import com.pax.pay.trans.AuthCMVoidTrans;
import com.pax.pay.trans.AuthSettlementTrans;
import com.pax.pay.trans.AuthTrans;
import com.pax.pay.trans.AuthVoidTrans;
import com.pax.pay.trans.BalanceTrans;
import com.pax.pay.trans.PosLogon;
import com.pax.pay.trans.PosLogout;
import com.pax.pay.trans.QrRefundTrans;
import com.pax.pay.trans.QrSaleTrans;
import com.pax.pay.trans.QrSaleVoidTrans;
import com.pax.pay.trans.ReadCardTrans;
import com.pax.pay.trans.RefundTrans;
import com.pax.pay.trans.SaleTrans;
import com.pax.pay.trans.SaleVoidTrans;
import com.pax.pay.trans.SettleTrans;
import com.pax.pay.trans.TransContext;
import com.pax.pay.trans.TransResult;
import com.pax.pay.trans.action.ActionInputPasword;
import com.pax.pay.trans.action.ActionInputTransData;
import com.pax.pay.trans.action.ActionInputTransData.EInputType;
import com.pax.pay.trans.model.Controller;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.trans.receipt.PrintListenerImpl;
import com.pax.pay.trans.receipt.ReceiptPrintBitmap;
import com.pax.pay.utils.ToastUtils;
import com.pax.settings.SettingsActivity;
import com.pax.settings.SysParam;
import com.pax.settings.SysParam.UpdateListener;
import com.pax.up.bjb.R;
import com.pax.view.dialog.CustomAlertDialog;
import com.pax.view.dialog.DialogUtils;

import org.json.JSONObject;

public class PaymentActivity extends Activity {
    public static final String TAG = "PaymentActivity";

    public static final String REQUEST = "REQUEST";
    private static final int REQUEST_CODE = 100;
    private boolean isInstalledNeptune = true;
    private JSONObject json;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_null);

        Intent intent = getIntent();
        ActivityStack.getInstance().push(this);

        try {
            FinancialApplication.init();
        } catch (Exception e) {
            Log.e(TAG, "", e);

            CustomAlertDialog dialog = new CustomAlertDialog(this, CustomAlertDialog.ERROR_TYPE, 5);
            dialog.setContentText(e.getMessage());
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();
            dialog.setOnDismissListener(new OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface arg0) {
                    System.exit(0);
                }
            });
            isInstalledNeptune = false;
            return;
        }

        // 设置操作员签到
        FinancialApplication.getController().set(Controller.OPERATOR_LOGON_STATUS, Controller.Constant.YES);

        Device.enableStatusBar(false);
        Device.enableHomeRecentKey(false);

        try {
            String jsonStr = intent.getExtras().getString(REQUEST);
            json = new JSONObject(jsonStr);
        } catch (Exception e) {
            transFinish(new ActionResult(TransResult.ERR_PARAM, null));
            return;
        }

        handler = new Handler();
        initDev();
    }

    /**
     * 设备初始化，绑定IPPS
     */
    private void initDev() {
        int ret = ParseReq.getInstance().check(json);
        if (ret != TransResult.SUCC) {
            ActionResult result = new ActionResult(ret, null);
            transFinish(result);
            return;
        }
        if (FinancialApplication.getController().get(Controller.POS_LOGON_STATUS) == Controller.Constant.NO) {

            handler.post(new Runnable() {

                @Override
                public void run() {
                    new PosLogon(PaymentActivity.this, handler, new ATransaction.TransEndListener() {

                        @Override
                        public void onEnd(ActionResult result) {
                            if (result.getRet() != TransResult.SUCC) {
                                transFinish(result);
                                return;
                            }

                            handler.post(new Runnable() {

                                @Override
                                public void run() {
                                    doTrans();
                                }
                            });
                        }
                    }).execute();
                }
            });
        } else {
            doTrans();
        }
    }

    private void doTrans() {
        RequestData requestData = ParseReq.getInstance().getRequestData();
        String transType = requestData.getTransType();

        if (TransContext.getInstance().getOperID() == null || TransContext.getInstance().getOperID().length() == 0) {
            TransContext.getInstance().setOperID("01");
        }

        if (transType.equals(ServiceConstant.TRANS_SALE)) {// 消费
            doSale(requestData);
        } else if (transType.equals(ServiceConstant.TRANS_VOID)) {// 消费撤销
            doVoid(requestData);
        } else if (transType.equals(ServiceConstant.TRANS_AUTH)) {// 预授权
            doAuth(requestData);
        } else if (transType.equals(ServiceConstant.TRANS_AUTH_CM)) {// 预授权完成请求
            doAuthCM(requestData);
        } else if (transType.equals(ServiceConstant.TRANS_AUTH_ADV)) {// 预授权完成通知
            doAuthAdv(requestData);
        } else if (transType.equals(ServiceConstant.TRANS_AUTH_VOID)) {// 预授权撤销
            doAuthVoid(requestData);
        } else if (transType.equals(ServiceConstant.TRANS_AUTH_CM_VOID)) {// 预授权完成撤销
            doAuthCMVoid(requestData);
        } else if (transType.equals(ServiceConstant.TRANS_REFUND)) {// 退货
            doRefund(requestData);
        } else if (transType.equals(ServiceConstant.TRANS_QR_SALE)) {// 扫码消费
            doTransQrSale(requestData);
        } else if (transType.equals(ServiceConstant.TRANS_QR_VOID)) {// 扫码撤销
            doTransQrVoid(requestData);
        } else if (transType.equals(ServiceConstant.TRANS_QR_REFUND)) {// 扫码退货
            doTransQrRefund(requestData);
        } else if (transType.equals(ServiceConstant.TRANS_BALANCE)) {// 余额查询
            doBalance(requestData);
        } else if (transType.equals(ServiceConstant.TRANS_LOGON)) {// 签到
            doLogon(requestData);
        } else if (transType.equals(ServiceConstant.TRANS_LOGOFF)) {// 签退
            doLogoff(requestData);
        } else if (transType.equals(ServiceConstant.TRANS_SETTLE)) {// 结算
            doSettle(requestData);
        } else if (transType.equals(ServiceConstant.TRANS_PRN_LAST)) {// 打印最后一笔
            doPrnLast(requestData);
        } else if (transType.equals(ServiceConstant.TRANS_PRN_ANY)) {// 打印任意一笔
            doPrnAny(requestData);
        } else if (transType.equals(ServiceConstant.TRANS_PRN_DETAIL)) {// 打印明细
            doPrnDetail(requestData);
        } else if (transType.equals(ServiceConstant.TRANS_PRN_TOTAL)) {// 打印交易汇总
            doPrnTotal(requestData);
        } else if (transType.equals(ServiceConstant.TRANS_PRN_LAST_BATCH)) {// 打印上批总计
            doPrnLastBatch(requestData);
        } else if (transType.equals(ServiceConstant.TRANS_GET_CARD_NO)) {// 卡号查询
            doReadCard(requestData);
        } else if (transType.equals(ServiceConstant.TRANS_SETTING)) {// 终端设置
            doSetting(requestData);
        } else if (transType.equals(ServiceConstant.PRN_BITMAP)) {// 打印图片
            doPrnBitmap(requestData);
        } else if (transType.equals(ServiceConstant.TRANS_QUERY)) {// 交易查询
            doTransQuery(requestData);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isInstalledNeptune) {
            return;
        }
        SysParam.setUpdateListener(new UpdateListener() {

            @Override
            public void onErr(String prompt) {
                DialogUtils.showUpdateDialog(PaymentActivity.this, prompt, new CustomAlertDialog.OnCustomClickListener() {
                    @Override
                    public void onClick(CustomAlertDialog alertDialog) {
                        alertDialog.dismiss();
                        new SettleTrans(PaymentActivity.this, handler, null).execute();
                    }
                });
            }
        });
        SysParam.init(PaymentActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            transFinish(new ActionResult(TransResult.SUCC, null));
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void transFinish(ActionResult result) {
        ActivityStack.getInstance().popAll();
        Payment.getInstance(PaymentActivity.this).setResult(ParseResp.getInstance().parse(result));

    }

    /***************************************************** 交易实现 *****************************************************/

    /**
     * 消费
     * 
     * @param requestData
     */
    private void doSale(RequestData requestData) {
        new SaleTrans(PaymentActivity.this, handler, requestData.getTransAmount(), true,
                new TransEndListener() {

                    @Override
                    public void onEnd(ActionResult result) {
                        transFinish(result);
                    }
                }).execute();
    }

    /**
     * 消费撤销
     * 
     * @param requestData
     */
    private void doVoid(RequestData requestData) {
        String voucherNo = requestData.getVoucherNo();
        if (voucherNo == null || voucherNo.length() == 0) {
            new SaleVoidTrans(PaymentActivity.this, handler, new TransEndListener() {

                @Override
                public void onEnd(ActionResult result) {
                    transFinish(result);
                }
            }).execute();
        } else {
            new SaleVoidTrans(PaymentActivity.this, handler, voucherNo, new TransEndListener() {

                @Override
                public void onEnd(ActionResult result) {
                    transFinish(result);
                }
            }).execute();
        }
    }

    /**
     * 退货
     * 
     * @param requestData
     */
    private void doRefund(RequestData requestData) {
        TransData transData = new TransData();
        String amount = requestData.getTransAmount();
        String refNo = requestData.getOriRefNo();
        String date = requestData.getOrigDate();
        if (amount == null || amount.length() == 0) {// 进入金额输入界面
            if (refNo == null || refNo.length() == 0 || date == null || date.length() == 0) {// 输入金额后进入参考号/日期输入界面
                new RefundTrans(PaymentActivity.this, handler, new TransEndListener() {

                    @Override
                    public void onEnd(ActionResult result) {
                        transFinish(result);
                    }
                }).execute();
            } else {// 输入金额后进入刷卡界面
                transData.setRefNo(refNo);
                transData.setDate(date);
                new RefundTrans(PaymentActivity.this, handler, transData, true, false, new TransEndListener() {

                    @Override
                    public void onEnd(ActionResult result) {
                        transFinish(result);
                    }
                }).execute();
            }

        } else if (refNo == null || refNo.length() == 0 || date == null || date.length() == 0) {// 进入参考号/日期输入界面
            transData.setAmount(amount);
            new RefundTrans(PaymentActivity.this, handler, transData, false, true, new TransEndListener() {

                @Override
                public void onEnd(ActionResult result) {
                    transFinish(result);
                }
            }).execute();
        } else {// 进入刷卡界面
            transData.setAmount(amount);
            transData.setRefNo(refNo);
            transData.setDate(date);
            new RefundTrans(PaymentActivity.this, handler, transData, false, false, new TransEndListener() {

                @Override
                public void onEnd(ActionResult result) {
                    transFinish(result);
                }
            }).execute();
        }
    }

    /**
     * 预授权
     * 
     * @param requestData
     */
    private void doAuth(RequestData requestData) {
        new AuthTrans(PaymentActivity.this, handler, requestData.getTransAmount(), new TransEndListener() {

            @Override
            public void onEnd(ActionResult result) {
                transFinish(result);
            }
        }).execute();
    }

    /**
     * 预授权撤销
     * 
     * @param requestData
     */
    private void doAuthVoid(RequestData requestData) {
        TransData transData = new TransData();
        String amount = requestData.getTransAmount();
        String authCode = requestData.getOrigAuthNo();
        String date = requestData.getOrigDate();
        if (amount == null || amount.length() == 0) {// 进入金额输入界面
            if (authCode == null || authCode.length() == 0 || date == null || date.length() == 0) {// 输入金额后进入授权码/日期输入界面
                new AuthVoidTrans(PaymentActivity.this, handler, new TransEndListener() {

                    @Override
                    public void onEnd(ActionResult result) {
                        transFinish(result);
                    }
                }).execute();
            } else {// 输入金额后进入刷卡界面
                transData.setAuthCode(authCode);
                transData.setDate(date);
                new AuthVoidTrans(PaymentActivity.this, handler, transData, true, false, new TransEndListener() {

                    @Override
                    public void onEnd(ActionResult result) {
                        transFinish(result);
                    }
                }).execute();
            }

        } else if (authCode == null || authCode.length() == 0 || date == null || date.length() == 0) {// 进入授权码/日期输入界面
            transData.setAmount(amount);
            new AuthVoidTrans(PaymentActivity.this, handler, transData, false, true, new TransEndListener() {

                @Override
                public void onEnd(ActionResult result) {
                    transFinish(result);
                }
            }).execute();
        } else {// 进入刷卡界面
            transData.setAmount(amount);
            transData.setAuthCode(authCode);
            transData.setDate(date);
            new AuthVoidTrans(PaymentActivity.this, handler, transData, false, false, new TransEndListener() {

                @Override
                public void onEnd(ActionResult result) {
                    transFinish(result);
                }
            }).execute();
        }
    }

    /**
     * 预授权完成请求
     * 
     * @param requestData
     */
    private void doAuthCM(RequestData requestData) {
        TransData transData = new TransData();
        String amount = requestData.getTransAmount();
        String authCode = requestData.getOrigAuthNo();
        String date = requestData.getOrigDate();
        if (amount == null || amount.length() == 0) {// 进入金额输入界面
            if (authCode == null || authCode.length() == 0 || date == null || date.length() == 0) {// 输入金额后进入授权码/日期输入界面
                new AuthCMTrans(PaymentActivity.this, handler, new TransEndListener() {

                    @Override
                    public void onEnd(ActionResult result) {
                        transFinish(result);
                    }
                }).execute();
            } else {// 输入金额后进入刷卡界面
                transData.setAuthCode(authCode);
                transData.setDate(date);
                new AuthCMTrans(PaymentActivity.this, handler, transData, true, false, new TransEndListener() {

                    @Override
                    public void onEnd(ActionResult result) {
                        transFinish(result);
                    }
                }).execute();
            }
        } else if (authCode == null || authCode.length() == 0 || date == null || date.length() == 0) {// 进入授权码/日期输入界面
            transData.setAmount(amount);
            new AuthCMTrans(PaymentActivity.this, handler, transData, false, true, new TransEndListener() {
                @Override
                public void onEnd(ActionResult result) {
                    transFinish(result);
                }
            }).execute();
        } else {// 进入刷卡界面
            transData.setAmount(amount);
            transData.setAuthCode(authCode);
            transData.setDate(date);
            new AuthCMTrans(PaymentActivity.this, handler, transData, false, false, new TransEndListener() {

                @Override
                public void onEnd(ActionResult result) {
                    transFinish(result);
                }
            }).execute();
        }

    }

    /**
     * 预授权完成通知
     * 
     * @param requestData
     */
    private void doAuthAdv(RequestData requestData) {
        TransData transData = new TransData();
        String amount = requestData.getTransAmount();
        String authCode = requestData.getOrigAuthNo();
        String date = requestData.getOrigDate();
        if (amount == null || amount.length() == 0) {// 进入金额输入界面
            if (authCode == null || authCode.length() == 0 || date == null || date.length() == 0) {// 输入金额后进入授权码/日期输入界面
                new AuthSettlementTrans(PaymentActivity.this, handler, new TransEndListener() {

                    @Override
                    public void onEnd(ActionResult result) {
                        transFinish(result);
                    }
                }).execute();
            } else {// 输入金额后进入刷卡界面
                transData.setAuthCode(authCode);
                transData.setDate(date);
                new AuthSettlementTrans(PaymentActivity.this, handler, transData, true, false, new TransEndListener() {

                    @Override
                    public void onEnd(ActionResult result) {
                        transFinish(result);
                    }
                }).execute();
            }

        } else if (authCode == null || authCode.length() == 0 || date == null || date.length() == 0) {// 进入授权码/日期输入界面
            transData.setAmount(amount);
            new AuthSettlementTrans(PaymentActivity.this, handler, transData, false, true, new TransEndListener() {

                @Override
                public void onEnd(ActionResult result) {
                    transFinish(result);
                }
            }).execute();
        } else {// 进入刷卡界面
            transData.setAmount(amount);
            transData.setAuthCode(authCode);
            transData.setDate(date);
            new AuthSettlementTrans(PaymentActivity.this, handler, transData, false, false, new TransEndListener() {

                @Override
                public void onEnd(ActionResult result) {
                    transFinish(result);
                }
            }).execute();
        }
    }

    /**
     * 预授权完成撤销
     * 
     * @param requestData
     */
    private void doAuthCMVoid(RequestData requestData) {
        String voucherNo = requestData.getVoucherNo();
        if (voucherNo == null || voucherNo.length() == 0) {
            new AuthCMVoidTrans(PaymentActivity.this, handler, new TransEndListener() {

                @Override
                public void onEnd(ActionResult result) {
                    transFinish(result);
                }
            }).execute();
        } else {
            new AuthCMVoidTrans(PaymentActivity.this, handler, voucherNo, new TransEndListener() {

                @Override
                public void onEnd(ActionResult result) {
                    transFinish(result);
                }
            }).execute();
        }
    }

    /**
     * 余额查询
     * 
     * @param requestData
     */
    private void doBalance(RequestData requestData) {
        new BalanceTrans(PaymentActivity.this, handler, new TransEndListener() {

            @Override
            public void onEnd(ActionResult result) {
                transFinish(result);
            }
        }).execute();
    }

    /**
     * 扫码消费
     * 
     * @param requestData
     */
    private void doTransQrSale(RequestData requestData) {
        new QrSaleTrans(PaymentActivity.this, handler, requestData.getTransAmount(), new TransEndListener() {

            @Override
            public void onEnd(ActionResult result) {
                transFinish(result);
            }
        }).execute();
    }

    /**
     * 扫码撤销
     * 
     * @param requestData
     */
    private void doTransQrVoid(RequestData requestData) {
        String origC2bVoucher = requestData.getOrigC2bVoucher();
        if (origC2bVoucher == null || origC2bVoucher.length() == 0) {
            new QrSaleVoidTrans(PaymentActivity.this, handler, new TransEndListener() {

                @Override
                public void onEnd(ActionResult result) {
                    transFinish(result);
                }
            }).execute();
        } else {
            new QrSaleVoidTrans(PaymentActivity.this, handler, requestData.getOrigC2bVoucher(), new TransEndListener() {

                @Override
                public void onEnd(ActionResult result) {
                    transFinish(result);
                }
            }).execute();
        }
    }

    /**
     * 扫码退货
     * 
     * @param requestData
     */
    private void doTransQrRefund(RequestData requestData) {
        String origC2bVoucher = requestData.getOrigC2bVoucher();
        String amount = requestData.getTransAmount();
        if (origC2bVoucher == null || origC2bVoucher.length() == 0) {
            if (amount == null || amount.length() == 0) {
                new QrRefundTrans(PaymentActivity.this, handler, new TransEndListener() {

                    @Override
                    public void onEnd(ActionResult result) {
                        transFinish(result);
                    }
                }).execute();
            } else {
                TransData transData = new TransData();
                transData.setAmount(amount);
                new QrRefundTrans(PaymentActivity.this, handler, transData, false, true, new TransEndListener() {

                    @Override
                    public void onEnd(ActionResult result) {
                        transFinish(result);
                    }
                }).execute();
            }

        } else {
            if (amount == null || amount.length() == 0) {
                TransData transData = new TransData();
                transData.setC2bVoucher(origC2bVoucher);
                new QrRefundTrans(PaymentActivity.this, handler, transData, true, false, new TransEndListener() {

                    @Override
                    public void onEnd(ActionResult result) {
                        transFinish(result);
                    }
                }).execute();
            } else {
                TransData transData = new TransData();
                transData.setAmount(amount);
                transData.setC2bVoucher(origC2bVoucher);
                new QrRefundTrans(PaymentActivity.this, handler, transData, false, false, new TransEndListener() {

                    @Override
                    public void onEnd(ActionResult result) {
                        transFinish(result);
                    }
                }).execute();
            }
        }
    }

    /**
     * 签到
     * 
     * @param requestData
     */
    private void doLogon(RequestData requestData) {
        // 设置操作员号
        TransContext.getInstance().setOperID(requestData.getOperId());

        new PosLogon(PaymentActivity.this, handler, new TransEndListener() {

            @Override
            public void onEnd(ActionResult result) {
                transFinish(result);
            }
        }).execute();
    }

    /**
     * 签退
     * 
     * @param requestData
     */
    private void doLogoff(RequestData requestData) {
        new PosLogout(PaymentActivity.this, handler, new TransEndListener() {

            @Override
            public void onEnd(ActionResult result) {
                transFinish(result);
            }
        }).execute();
    }

    /**
     * 结算
     * 
     * @param requestData
     */
    private void doSettle(RequestData requestData) {
        new SettleTrans(PaymentActivity.this, handler, new TransEndListener() {

            @Override
            public void onEnd(ActionResult result) {
                transFinish(result);
            }
        }).execute();
    }

    /**
     * 打印最后一笔
     * 
     * @param requestData
     */
    private void doPrnLast(RequestData requestData) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int result = Printer.printLastTrans(PaymentActivity.this, handler);

                transFinish(new ActionResult(result, null));
            }
        }).start();
    }

    /**
     * 打印明细
     * 
     * @param requestData
     */
    private void doPrnDetail(RequestData requestData) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int result = Printer.printTransDetail(getString(R.string.trans_detail_list), PaymentActivity.this,
                        handler);

                transFinish(new ActionResult(result, null));
            }
        }).start();
    }

    /**
     * 打印交易汇总
     * 
     * @param requestData
     */
    private void doPrnTotal(RequestData requestData) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Printer.printTransTotal(PaymentActivity.this, handler, false);

                transFinish(new ActionResult(TransResult.SUCC, null));
            }
        }).start();
    }

    /**
     * 打印上批总计
     * 
     * @param requestData
     */
    private void doPrnLastBatch(RequestData requestData) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int result = Printer.printLastBatch(PaymentActivity.this, handler);

                transFinish(new ActionResult(result, null));
            }
        }).start();
    }

    /**
     * 打印任意一笔
     * 
     * @param requestData
     */
    private void doPrnAny(RequestData requestData) {
        ActionInputTransData prnAnyAction = new ActionInputTransData(new ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionInputTransData) action).setParam(PaymentActivity.this, handler,
                        getString(R.string.manage_menu_deal_inquiry)).setInfoTypeSale(
                        getString(R.string.prompt_input_transno), EInputType.NUM, 6, false);

                TransContext.getInstance().setCurrentAction(action);
            }
        }, 1);
        prnAnyAction.setEndListener(new ActionEndListener() {

            @Override
            public void onEnd(AAction action, ActionResult result) {
                if (result.getRet() != TransResult.SUCC) {
                    transFinish(new ActionResult(TransResult.ERR_HOST_REJECT, null));
                    return;
                }

                String content = (String) result.getData();
                if (content == null || content.length() == 0) {
                    ToastUtils.showMessage(PaymentActivity.this, getString(R.string.please_input_again));
                    return;
                }
                long transNo = Long.parseLong(content);
                final TransData transData = TransData.readTrans(transNo);

                if (transData == null) {
                    transFinish(new ActionResult(TransResult.ERR_NO_ORIG_TRANS, null));
                    return;
                }

                ActivityStack.getInstance().pop();
                TransContext.getInstance().setCurrentContext(PaymentActivity.this);
                transData.setOper(TransContext.getInstance().getOperID());

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Printer.printTransAgain(PaymentActivity.this, handler, transData);
                        transFinish(new ActionResult(TransResult.SUCC, null));
                    }
                }).start();
            }
        });
        prnAnyAction.execute();
    }

    /**
     * 读取卡号
     * 
     * @param requestData
     */
    private void doReadCard(RequestData requestData) {
        new ReadCardTrans(PaymentActivity.this, handler, new TransEndListener() {

            @Override
            public void onEnd(ActionResult result) {
                transFinish(result);
            }
        }).execute();
    }

    /**
     * 终端设置
     * 
     * @param requestData
     */
    private void doSetting(RequestData requestData) {
        ActionInputPasword inputPaswordAction = new ActionInputPasword(new ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionInputPasword) action).setParam(PaymentActivity.this, handler, 8,
                        getString(R.string.prompt_sys_pwd), null);
                //TransContext.getInstance().setCurrentAction(action);
            }
        });

        inputPaswordAction.setEndListener(new ActionEndListener() {

            @Override
            public void onEnd(AAction action, ActionResult result) {

                if (result.getRet() != TransResult.SUCC) {
                    transFinish(result);
                    return;
                }

                String data = (String) result.getData();
                if (!data.equals(FinancialApplication.getSysParam().get(SysParam.SEC_SYSPWD))) {
                    DialogUtils.showErrMessage(PaymentActivity.this, handler, getString(R.string.settings_title),
                            getString(R.string.err_password), new OnDismissListener() {

                                @Override
                                public void onDismiss(DialogInterface arg0) {
                                    transFinish(new ActionResult(TransResult.ERR_PASSWORD, null));
                                }
                            }, Constants.FAILED_DIALOG_SHOW_TIME);
                    return;
                }

                Intent intent = new Intent(PaymentActivity.this, SettingsActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString(EUIParamKeys.NAV_TITLE.toString(), getString(R.string.settings_title));
                bundle.putBoolean(EUIParamKeys.NAV_BACK.toString(), true);
                intent.putExtras(bundle);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

        inputPaswordAction.execute();
    }

    /**
     * 打印图片
     * 
     * @param requestData
     */
    private void doPrnBitmap(RequestData requestData) {
        final String bitmapStr = requestData.getBitmap();
        new Thread(new Runnable() {
            @Override
            public void run() {
                ReceiptPrintBitmap.getInstance().print(bitmapStr, new PrintListenerImpl(PaymentActivity.this, handler));
                transFinish(new ActionResult(TransResult.SUCC, null));
            }
        }).start();
    }

    /**
     * 交易查询
     * 
     * @param requestData
     */
    private void doTransQuery(RequestData requestData) {
        Intent intent = new Intent(PaymentActivity.this, TransQueryActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(EUIParamKeys.NAV_TITLE.toString(), getString(R.string.trans_query));
        bundle.putBoolean(EUIParamKeys.NAV_BACK.toString(), true);
        bundle.putBoolean(EUIParamKeys.SUPPORT_DO_TRANS.toString(), false);
        intent.putExtras(bundle);
        startActivityForResult(intent, REQUEST_CODE);

    }

}
