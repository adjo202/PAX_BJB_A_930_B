package com.pax.pay.trans;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

import com.pax.abl.core.AAction;
import com.pax.abl.core.AAction.ActionEndListener;
import com.pax.abl.core.AAction.ActionStartListener;
import com.pax.abl.core.ATransaction;
import com.pax.abl.core.ActionResult;
import com.pax.abl.utils.TrackUtils;
import com.pax.dal.ICardReaderHelper;
import com.pax.dal.IPicc;
import com.pax.dal.ISignPad;
import com.pax.dal.entity.EPiccRemoveMode;
import com.pax.dal.entity.EPiccType;
import com.pax.dal.entity.EReaderType;
import com.pax.dal.entity.PollingResult;
import com.pax.dal.exceptions.IccDevException;
import com.pax.dal.exceptions.MagDevException;
import com.pax.dal.exceptions.PiccDevException;
import com.pax.device.Device;
import com.pax.eemv.enums.ETransResult;
import com.pax.pay.app.ActivityStack;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.constant.Constants;
import com.pax.pay.trans.action.ActionSearchCard.CardInformation;
import com.pax.pay.trans.action.ActionSearchCard.SearchMode;
import com.pax.pay.trans.action.ActionTransPreDeal;
import com.pax.pay.trans.component.Component;
import com.pax.pay.trans.model.ETransType;
import com.pax.pay.trans.model.TransData;
import com.pax.pay.trans.model.TransData.EnterMode;
import com.pax.pay.trans.transmit.ModemCommunicate;
import com.pax.pay.trans.transmit.TransOnline;
import com.pax.pay.trans.transmit.TransProcessListenerImpl;
import com.pax.pay.utils.AppLog;
import com.pax.settings.SysParam;
import com.pax.settings.SysParam.Constant;
import com.pax.up.bjb.R;
import com.pax.view.dialog.CustomAlertDialog;
import com.pax.view.dialog.DialogUtils;

public abstract class BaseTrans extends ATransaction {
    private static final String TAG = "BaseTrans";

    protected Context context;
    protected Handler handler;
    // 当前交易类型
    protected ETransType transType;
    protected TransData transData;
    /**
     * 交易监听器
     */
    protected TransEndListener transListener;

    public BaseTrans(Context context, Handler handler, ETransType transType, TransEndListener transListener) {
        super();
        this.context = context;
        this.handler = handler;
        this.transType = transType;
        this.transListener = transListener;

    }

    public BaseTrans() {

    }

    /**
     * 设置交易类型
     * 
     * @param transType
     */
    public void setTransType(ETransType transType) {
        this.transType = transType;
    }

    protected void setTransListener(TransEndListener transListener) {
        this.transListener = transListener;
    }

    /**
     * 交易结果提示
     */
    protected void transEnd(final ActionResult result) {
        AppLog.i(TAG, transType.toString() + " TRANS--END--");
        dispLogoOnPad(); //外置签名板显示logo
        dispResult(transType.getTransName(), result, new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface arg0) {
                promptDialog = null;
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        removeCard(); //拔卡检查, 调用时需要起线程
                        try {
                            IPicc picc = FinancialApplication.getDal().getPicc(EPiccType.INTERNAL); //获取非接读卡器
                            picc.close(); //关闭PICC模块，使该模块处于下电状态。
                        } catch (PiccDevException e) {
                            Log.e(TAG, "", e);
                        }

                        TransProcessListenerImpl listenerImpl = new TransProcessListenerImpl(TransContext.getInstance()
                                .getCurrentContext());

//                        if (transData.getIsOnlineTrans()) { //是否为联机交易
//                            // 联机电子签名上送
//                            TransOnline.onlineSignatureSend(transData.getTransNo(), listenerImpl);
//                        } //removed by richard 20170414, don't need to send the signature now.

                        // 脱机交易处理
                        if (!transData.getIsOnlineTrans()) {
                            TransOnline.offlineTransSend(listenerImpl, false);
                        } else if (transData.getIsOnlineTrans()
                                && !transData.getTransType().equals(ETransType.LOGOUT.toString())) {// 联机交易,但不是签到和签退
                            TransOnline.offlineTransSend(listenerImpl, true);
                        }

                        // 离线的电子签名上送
//                        if (transData.getIsOnlineTrans()) {
//                            TransOnline.offlineSignatureSend(listenerImpl);
//                        } //removed by richard 20170414, don't need to send the signature now.

                        TransOnline.downLoadCheck(true, false, listenerImpl);
                        if (listenerImpl != null) {
                            listenerImpl.onHideProgress();
                        }
                        ModemCommunicate.getInstance().onClose(); // 关闭MODEM通讯连接
                        ActivityStack.getInstance().popAllButBottom();
                        TransContext.getInstance().setCurrentAction(null);
                        if (transListener != null) {
                            transListener.onEnd(result);
                        }
                        //Clear the fallback flag
                        if(transData.getIsFallback()){
                            transData.setIsFallback(false);
                        }
                        SystemClock.sleep(200);
                        setTransRunning(false);
                    }
                }).start();
            }
        });
    }

    protected void transEndBalanceInq(final ActionResult result) {
        AppLog.i(TAG, transType.toString() + " TRANS--END--");
        dispLogoOnPad(); //外置签名板显示logo
        dispResultBalanceInq(transType.getTransName(), result, new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface arg0) {
                promptDialog = null;
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        removeCard(); //拔卡检查, 调用时需要起线程
                        try {
                            IPicc picc = FinancialApplication.getDal().getPicc(EPiccType.INTERNAL); //获取非接读卡器
                            picc.close(); //关闭PICC模块，使该模块处于下电状态。
                        } catch (PiccDevException e) {
                            Log.e(TAG, "", e);
                        }

                        TransProcessListenerImpl listenerImpl = new TransProcessListenerImpl(TransContext.getInstance()
                                .getCurrentContext());

//                        if (transData.getIsOnlineTrans()) { //是否为联机交易
//                            // 联机电子签名上送
//                            TransOnline.onlineSignatureSend(transData.getTransNo(), listenerImpl);
//                        } //removed by richard 20170414, don't need to send the signature now.

                        // 脱机交易处理
                        if (!transData.getIsOnlineTrans()) {
                            TransOnline.offlineTransSend(listenerImpl, false);
                        } else if (transData.getIsOnlineTrans()
                                && !transData.getTransType().equals(ETransType.LOGOUT.toString())) {// 联机交易,但不是签到和签退
                            TransOnline.offlineTransSend(listenerImpl, true);
                        }

                        // 离线的电子签名上送
//                        if (transData.getIsOnlineTrans()) {
//                            TransOnline.offlineSignatureSend(listenerImpl);
//                        } //removed by richard 20170414, don't need to send the signature now.

                        TransOnline.downLoadCheck(true, false, listenerImpl);
                        if (listenerImpl != null) {
                            listenerImpl.onHideProgress();
                        }
                        ModemCommunicate.getInstance().onClose(); // 关闭MODEM通讯连接
                        ActivityStack.getInstance().popAllButBottom();
                        TransContext.getInstance().setCurrentAction(null);
                        if (transListener != null) {
                            transListener.onEnd(result);
                        }
                        //Clear the fallback flag
                        if(transData.getIsFallback()){
                            transData.setIsFallback(false);
                        }
                        SystemClock.sleep(200);
                        setTransRunning(false);
                    }
                }).start();
            }
        });
    }

    /**
     * 重写父类的execute， 增加交易是否已运行检查和交易预处理
     */
    @Override
    public synchronized void execute() {
        AppLog.i(TAG, transType.toString() + " TRANS--START--");
        if (isTransRunning()) {
//            SystemClock.sleep(1000);// 延时1s等待交易结束
//            setTransRunning(false);
            return;
        }
        setTransRunning(true);

        // 初始化transData
        transData = Component.transInit();
        // 设置当前context
        TransContext.getInstance().setCurrentContext(context);
        ActionTransPreDeal preDealAction = new ActionTransPreDeal(new ActionStartListener() {

            @Override
            public void onStart(AAction action) {
                ((ActionTransPreDeal) action).setParam(getCurrentContext(), transType);
            }
        });
        preDealAction.setEndListener(new ActionEndListener() {

            @Override
            public void onEnd(AAction action, ActionResult result) {
                if (result.getRet() != TransResult.SUCC) {
                    transEnd(result);
                    return;
                }
                transData.setTransType(transType.toString());
                exe();
            }
        });
        //这个方法中会执行监听器的onStart()方法，并且进行交易预处理
        //交易预处理中的setResult()方法会调用监听器的onEnd()方法
        preDealAction.execute();
    }

    /**
     * 执行父类的execute方法
     * 
     * @param
     */
    private void exe() {
        super.execute();
    }

    /**
     * 交易是否已执行， 此标准是全局性的， 真的所有交易， 如果某个交易中间需要插入其他交易时， 自己控制此状态。
     */
    private static boolean isTransRunning = false;

    /**
     * 获取交易执行状态
     * 
     * @return
     */
    public static boolean isTransRunning() {
        return isTransRunning;
    }

    /**
     * 设置交易执行状态
     * 
     * @param isTransRunning
     */
    public static void setTransRunning(boolean isTransRunning) {
        BaseTrans.isTransRunning = isTransRunning;
    }

    /**
     * 外置签名板显示logo
     */
    private void dispLogoOnPad() {

        // 签名板选择
        String signPadStr = FinancialApplication.getSysParam().get(SysParam.SIGNATURE_SELECTOR);
        // 密码键盘选择
        String pinPadStr = FinancialApplication.getSysParam().get(SysParam.EX_PINPAD);

        if (Constant.PAD_S200.equals(pinPadStr) || Constant.PAD_S200.equals(signPadStr)) {
            ISignPad signPad = FinancialApplication.getDal().getSignPad();
            signPad.showIdleScreen();
        } else if (Constant.PAD_SP20.equals(pinPadStr)) {
        }

    }

    /**
     * 保存寻卡后的卡信息及输入方式
     * 
     * @param cardInfo
     * @param transData
     */
    public void saveCardInfo(CardInformation cardInfo, TransData transData, boolean isPreDialFlg) {
        // 手输卡号处理
        byte mode = cardInfo.getSearchMode();
        if (mode == SearchMode.KEYIN) {
            transData.setPan(cardInfo.getPan());
            transData.setExpDate(cardInfo.getExpDate());
            transData.setEnterMode(EnterMode.MANUAL);
        } else if (mode == SearchMode.SWIPE) {
            transData.setTrack1(cardInfo.getTrack1());
            transData.setTrack2(cardInfo.getTrack2());
            transData.setTrack3(cardInfo.getTrack3());
            transData.setPan(cardInfo.getPan());
            transData.setExpDate(TrackUtils.getExpDate(cardInfo.getTrack2()));
            //add fallback treatment
            if(transData.getIsFallback()){
                transData.setEnterMode(EnterMode.FALLBACK);
            }else{
                transData.setEnterMode(EnterMode.SWIPE);
            }
        } else if (mode == SearchMode.INSERT || mode == SearchMode.TAP) {
            transData.setEnterMode(mode == SearchMode.INSERT ? EnterMode.INSERT : EnterMode.QPBOC);
        }

        // MODEM预拨号
        if (isPreDialFlg) {
            ModemCommunicate.getInstance().preDial();
        }

    }

    /**
     * 交易结果提示，及拔卡处理
     * 
     * @param transName
     * @param result
     * @param dismissListener
     */
    protected void dispResult(String transName, final ActionResult result, OnDismissListener dismissListener) {
        if (result.getRet() == TransResult.SUCC) {
            DialogUtils.showSuccMessage(getCurrentContext(), handler, transName, dismissListener,
                    Constants.SUCCESS_DIALOG_SHOW_TIME);
        } else if (result.getRet() == TransResult.ERR_ABORTED || result.getRet() == TransResult.ERR_HOST_REJECT) {
            // ERR_ABORTED AND ERR_HOST_REJECT 之前已提示错误信息， 此处不需要再提示
            dismissListener.onDismiss(null);
        } else {
            DialogUtils.showErrMessage(getCurrentContext(), handler, transName,
                    TransResult.getMessage(context, result.getRet()), dismissListener,
                    Constants.FAILED_DIALOG_SHOW_TIME);
        }
    }

    protected void dispResultBalanceInq(String transName, final ActionResult result, OnDismissListener dismissListener) {
        if (result.getRet() == TransResult.SUCC) {
            DialogUtils.showSuccMessage(getCurrentContext(), handler, transName, dismissListener,
                    Constants.SUCCESS_DIALOG_SHOW_TIME);
        } else if (result.getRet() == TransResult.ERR_ABORTED || result.getRet() == TransResult.ERR_HOST_REJECT) {
            // ERR_ABORTED AND ERR_HOST_REJECT 之前已提示错误信息， 此处不需要再提示
            dismissListener.onDismiss(null);
        } else {
            DialogUtils.showErrMessage(getCurrentContext(), handler, transName,
                    TransResult.getMessage(context, result.getRet()), dismissListener,
                    Constants.FAILED_DIALOG_SHOW_TIME);
        }
    }

    /**
     * 拔卡检查, 调用时需要起线程
     */
    protected void removeCard() {
        while (true) {
            try {
                try {
                    // Get contactless card reader
                    // According to the specified mode, send a shutdown command to the card;
                    // or send a deactivation command; or reset the carrier wave,
                    // and determine whether the card has moved out of the induction area.
                    FinancialApplication.getDal().getPicc(EPiccType.INTERNAL).remove(EPiccRemoveMode.REMOVE, (byte) 0);
                } catch (PiccDevException e) {
                    Log.e(TAG, "", e);
                }

                ICardReaderHelper helper = FinancialApplication.getDal().getCardReaderHelper(); //获取ICardReaderHelper 寻卡接口
                PollingResult result = helper.polling(EReaderType.ICC_PICC, 100); //寻卡接口:卡片类型，超时时间，返回轮询结果
                if (result.getReaderType() == EReaderType.ICC || result.getReaderType() == EReaderType.PICC) {
                    // 提示拔卡
                    if (result.getReaderType() == EReaderType.ICC) {
                        showWarning(getCurrentContext().getString(R.string.pull_card));
                    } else {
                        // 提示移卡
                        showWarning(getCurrentContext().getString(R.string.remove_card));
                    }
                    SystemClock.sleep(300);
                    Device.beepErr();
                } else {
                    if (promptDialog != null) {
                        promptDialog.dismiss();
                    }
                    break;
                }
            } catch (MagDevException e) {
                Log.e(TAG, "", e);
                if (promptDialog != null) {
                    promptDialog.dismiss();
                }

                break;
            } catch (IccDevException e) {
                Log.e(TAG, "", e);
                if (promptDialog != null) {
                    promptDialog.dismiss();
                }
                break;
            } catch (PiccDevException e) {
                Log.e(TAG, "", e);
                if (promptDialog != null) {
                    promptDialog.dismiss();
                }
                break;
            }
        }
    }

    private CustomAlertDialog promptDialog;

    /**
     * 提示警告信息
     * 
     * @param warning
     */
    private void showWarning(final String warning) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (promptDialog == null) {
                    promptDialog = new CustomAlertDialog(getCurrentContext(), CustomAlertDialog.WARN_TYPE);
                    promptDialog.show();
                    promptDialog.setImage(BitmapFactory.decodeResource(getCurrentContext().getResources(),
                            R.drawable.ic16));
                    promptDialog.setCancelable(false);
                    promptDialog.setTitleText(transType.getTransName());
                }

                promptDialog.setContentText(warning);
            }
        });
    }

    @Override
    protected void bind(String state, AAction action) {
        super.bind(state, action);
        if (action != null) {
            action.setEndListener(new ActionEndListener() {

                @Override
                public void onEnd(AAction action, final ActionResult result) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                onActionResult(currentState, result);
                            } catch (Exception e) {
                                Log.e(TAG, "", e);
                                transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
                            }

                        }
                    });
                }
            });
        }
    }

    private String currentState;

    @Override
    public void gotoState(String state) {
        this.currentState = state;
        AppLog.i(TAG, transType.toString() + " ACTION--" + currentState + "--start");
        super.gotoState(state);
    }

    /**
     * action结果处理
     * 
     * @param currentState
     *            ：当前State
     * @param result
     *            ：当前Action执行的结果
     */
    public abstract void onActionResult(String currentState, ActionResult result);

    protected Context getCurrentContext() {
        return TransContext.getInstance().getCurrentContext();
    }

    /**
     *EMV Transaction abnormal result process
     *@Author Richard 20170511
     */
    protected void emvAbnormalResultProcess(ETransResult transResult) {
        if (transResult != null) {
            switch (transResult) {
                case ONLINE_DENIED:
                    transEnd(new ActionResult(TransResult.ERR_HOST_REJECT, null));
                    break;
                case ONLINE_CARD_DENIED:
                    transEnd(new ActionResult(TransResult.ERR_CARD_DENIED, null));
                    break;
                case ABORT_TERMINATED:
                case SIMPLE_FLOW_END:
                    transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
                    break;
                case OFFLINE_DENIED:
                default:
                    Device.beepErr();
                    transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
                    break;
            }
        } else {
            Device.beepErr();
            transEnd(new ActionResult(TransResult.ERR_ABORTED, null));
        }

    }

    //sandy added afterEnterMount to ensure the amount is add decimal point
    protected void afterEnterAmount(){


    }



}
