package com.pax.pay.trans.action.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pax.abl.core.ActionResult;
import com.pax.dal.ISignPad;
import com.pax.dal.entity.SignPadResp;
import com.pax.gl.imgprocessing.IImgProcessing;
import com.pax.pay.BaseActivityWithTickForAction;
import com.pax.pay.app.FinancialApplication;
import com.pax.pay.constant.EUIParamKeys;
import com.pax.pay.trans.TransResult;
import com.pax.pay.utils.ToastUtils;
import com.pax.settings.SysParam;
import com.pax.settings.SysParam.Constant;
import com.pax.settings.currency.Currency;
import com.pax.up.bjb.R;
import com.pax.view.ElectronicSignatureView;
import com.pax.view.dialog.DispExDeviceDialog;

public class SignatureActivity extends BaseActivityWithTickForAction implements OnClickListener {

    private ElectronicSignatureView mSignatureView;
    private RelativeLayout writeUserName = null;
    private Button clearBtn;
    private Button confirmBtn;

    private String waterMarker;
    private String amount;

    private boolean processing = false;

    // 保存签名图片
    private byte[] data;

    private DispExDeviceDialog dialog;// 外置签名dialog

    private OnKeyListener onkeyListener = new OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_ENTER) {
                confirmBtn.performClick();
                return true;
            }
            return false;
        }
    };

    @Override
    protected int getLayoutId() {
        return R.layout.activity_authgraph_layout;
    }

    @Override
    protected void loadParam() {
        Bundle bundle = getIntent().getExtras();
        amount = bundle.getString(EUIParamKeys.TRANS_AMOUNT.toString());
        waterMarker = bundle.getString(EUIParamKeys.SIGN_FEATURE_CODE.toString());
    }

    @Override
    protected void initViews() {
        Currency currency = FinancialApplication.getSysParam().getCurrency();

        TextView headerText = (TextView) findViewById(R.id.header_title);
        headerText.setText(R.string.signature);

        ImageView backBtn = (ImageView) findViewById(R.id.header_back);
        backBtn.setVisibility(View.GONE);

        TextView amountLabel = (TextView) findViewById(R.id.trans_amount_label);
        amountLabel.setText(String.format("%s ",currency.getName()));


        TextView amountText = (TextView) findViewById(R.id.trans_amount_tv);
        amount = FinancialApplication.getConvert().amountMinUnitToMajor(String.valueOf(Long.parseLong(amount)),
                currency.getCurrencyExponent(), true);
        amountText.setText(amount);

        writeUserName = (RelativeLayout) findViewById(R.id.writeUserNameSpace);
        clearBtn = (Button) findViewById(R.id.clear_btn);
        confirmBtn = (Button) findViewById(R.id.confirm_btn);
        confirmBtn.requestFocus();

        if (Constant.PAD_S200.equals(FinancialApplication.getSysParam().get(SysParam.SIGNATURE_SELECTOR))) {
            clearBtn.setEnabled(false);
            confirmBtn.setEnabled(false);

            dialog = new DispExDeviceDialog(SignatureActivity.this, R.drawable.ex_signature,
                    getString(R.string.prompt_ex_signature), 0);
            tickTimerStop();
            dialog.show();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    ISignPad signPad = FinancialApplication.getDal().getSignPad();
                    signPad.displayWord((short) 0, (short) 0, (byte) 0x06, (byte) 0x01, (byte) 0x01, 100);
                    SignPadResp resp = signPad.signStart(FinancialApplication.getConvert().bcdToStr(waterMarker.getBytes()));
                    if (resp != null && resp.getSignBmp() != null) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(resp.getSignBmp(), 0, resp.getSignBmp().length);
                        data = FinancialApplication.getGl().getImgProcessing().bitmapToJbig(bitmap,
                                new IImgProcessing.IRgbToMonoAlgorithm() {

                                    @Override
                                    public int evaluate(int r, int g, int b) {
                                        int v = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                                        // set new pixel color to output bitmap
                                        if (v < 200) {
                                            return 0;
                                        } else {
                                            return 1;
                                        }
                                    }
                                });

                    }

                    if (dialog != null) {
                        dialog.dismiss();
                    }

                    finish(new ActionResult(TransResult.SUCC, data));
                }
            }).start();

        } else {
            // 内置签名板
            mSignatureView = new ElectronicSignatureView(SignatureActivity.this);
            mSignatureView.setBitmap(new Rect(0, 0, 224, 160), 0, Color.WHITE);

            writeUserName.addView(mSignatureView);
            mSignatureView.setOnKeyListener(onkeyListener);

            TextView markerText = new TextView(SignatureActivity.this);
            markerText.setHintTextColor(Color.rgb(153, 153, 153));
            markerText.setHint(waterMarker);

            markerText.setTextSize(38f);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT);
            lp.addRule(RelativeLayout.CENTER_IN_PARENT);
            markerText.setLayoutParams(lp);// 设置布局参数
            writeUserName.addView(markerText);
        }

    }

    @Override
    protected void setListeners() {
        clearBtn.setOnClickListener(this);
        confirmBtn.setOnClickListener(this);

        writeUserName.setOnKeyListener(onkeyListener);
        clearBtn.setOnKeyListener(onkeyListener);
        confirmBtn.setOnKeyListener(onkeyListener);

    }

    @Override
    protected void handleMsg(Message msg) {
        // Do nothing
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_DEL) {
            clearBtn.performClick();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.clear_btn:
                if (isProcessing()) {
                    return;
                }
                setProcessFlag();
                mSignatureView.clear();
                clearProcessFlag();
                break;
            case R.id.confirm_btn:
                if (isProcessing()) {
                    return;
                }
                setProcessFlag();
                if (!mSignatureView.getTouched()) {
                    finish(new ActionResult(TransResult.SUCC, null));
                    return;
                }

                Bitmap bitmap = mSignatureView.save(true, 0);
                data = FinancialApplication.getGl().getImgProcessing().bitmapToJbig(bitmap,
                        new IImgProcessing.IRgbToMonoAlgorithm() {

                            @Override
                            public int evaluate(int r, int g, int b) {
                                int v = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                                // set new pixel color to output bitmap
                                if (v < 200) {
                                    return 0;
                                } else {
                                    return 1;
                                }
                            }
                        });

                if (data.length > 999) {
                    ToastUtils.showMessage(SignatureActivity.this, getString(R.string.pls_re_signature));
                    setProcessFlag();
                    mSignatureView.clear();
                    clearProcessFlag();
                    return;
                }
                clearProcessFlag();
                finish(new ActionResult(TransResult.SUCC, data));

                break;
            default:
                break;
        }

    }

    protected void setProcessFlag() {
        processing = true;
    }

    protected void clearProcessFlag() {
        processing = false;
    }

    protected boolean isProcessing() {
        return processing;
    }

    /**
     * 在bitmap图像上添加水印文字
     * 
     * @param bitmap
     * @param mark
     * @return
     */
    public static Bitmap addMarkerOnBitmap(Bitmap bitmap, String mark) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        Paint paint = new Paint();
        // 水印的颜色
        paint.setColor(Color.rgb(153, 153, 153));
        // 水印的字体大小
        paint.setTextSize(38);
        paint.setAntiAlias(true);// 去锯齿
        Rect bounds = new Rect();
        paint.getTextBounds(mark, 0, mark.length(), bounds);
        int x = (bitmap.getWidth() - bounds.width()) / 2;
        int y = (bitmap.getHeight() + bounds.height()) / 2;
        canvas.drawBitmap(bitmap, 0, 0, paint);
        // 添加在图片中间位置
        canvas.drawText(mark, x, y, paint);
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
        return bmp;
    }

}
