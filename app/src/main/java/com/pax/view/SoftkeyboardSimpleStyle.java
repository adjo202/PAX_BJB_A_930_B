package com.pax.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.Keyboard.Key;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;

import com.pax.up.bjb.R;

import java.util.List;


public class SoftkeyboardSimpleStyle extends KeyboardView {

    public static final int STYLE_SIMPLE_NUM_KEY = 1;
    public static final int STYLE_PAX_NUM_KEY = 2;

    private Context mContext;
    private int mKeyStyleType = STYLE_SIMPLE_NUM_KEY;

    public SoftkeyboardSimpleStyle(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    public SoftkeyboardSimpleStyle(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mContext = context;
    }

    public int getmKeyStyleType() {
        return mKeyStyleType;
    }

    public void setKeyStyleType(int mKeyStyleType) {
        this.mKeyStyleType = mKeyStyleType;
    }

    /**
     * 重新画一些按键
     */
    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Keyboard keyboard = getKeyboard();
        List<Key> keys = keyboard.getKeys();
        for (Key key : keys) {
            // 数字键盘的处理
            if (mKeyStyleType == STYLE_SIMPLE_NUM_KEY) {
                drawSimpleNumSpecialKey(key, canvas);
            }else if (mKeyStyleType == STYLE_PAX_NUM_KEY) {
                drawPaxStyleNumSpecialKey(key, canvas);
            }
        }
    }
    //百富风格键盘
    private void drawPaxStyleNumSpecialKey(Key key, Canvas canvas) {
        switch (key.codes[0]) {
            case 48:
                drawKeyBackground(R.drawable.selection_button_0, canvas, key);
                break;
            case 49:
                drawKeyBackground(R.drawable.selection_button_1, canvas, key);
                break;
            case 50:
                drawKeyBackground(R.drawable.selection_button_2, canvas, key);
                break;
            case 51:
                drawKeyBackground(R.drawable.selection_button_3, canvas, key);
                break;
            case 52:
                drawKeyBackground(R.drawable.selection_button_4, canvas, key);
                break;
            case 53:
                drawKeyBackground(R.drawable.selection_button_5, canvas, key);
                break;
            case 54:
                drawKeyBackground(R.drawable.selection_button_6, canvas, key);
                break;
            case 55:
                drawKeyBackground(R.drawable.selection_button_7, canvas, key);
                break;
            case 56:
                drawKeyBackground(R.drawable.selection_button_8, canvas, key);
                break;
            case 57:
                drawKeyBackground(R.drawable.selection_button_9, canvas, key);
                break;
            case -5:
                drawKeyBackground(R.drawable.selection_button_delete, canvas, key);
                break;
            case 57418:
                drawKeyBackground(R.drawable.selection_button_cancel, canvas, key);
                break;
            case 57419:
                drawKeyBackground(R.drawable.selection_button_000, canvas, key);
                break;
            case 57420:
                drawKeyBackground(R.drawable.selection_button_confirm, canvas, key);
                break;
            case 57421:
                drawKeyBackground(R.drawable.selection_button_00, canvas, key);
                break;
            default:
                break;
        }
    }

    // 数字键盘
    private void drawSimpleNumSpecialKey(Key key, Canvas canvas) {
        switch (key.codes[0]) {
            case 48:
                drawKeyBackground(R.drawable.selection_btn_0, canvas, key);
                break;
            case 49:
                drawKeyBackground(R.drawable.selection_btn_1, canvas, key);
                break;
            case 50:
                drawKeyBackground(R.drawable.selection_btn_2, canvas, key);
                break;
            case 51:
                drawKeyBackground(R.drawable.selection_btn_3, canvas, key);
                break;
            case 52:
                drawKeyBackground(R.drawable.selection_btn_4, canvas, key);
                break;
            case 53:
                drawKeyBackground(R.drawable.selection_btn_5, canvas, key);
                break;
            case 54:
                drawKeyBackground(R.drawable.selection_btn_6, canvas, key);
                break;
            case 55:
                drawKeyBackground(R.drawable.selection_btn_7, canvas, key);
                break;
            case 56:
                drawKeyBackground(R.drawable.selection_btn_8, canvas, key);
                break;
            case 57:
                drawKeyBackground(R.drawable.selection_btn_9, canvas, key);
                break;
            case -5:
                drawKeyBackground(R.drawable.selection_btn_delete, canvas, key);
                break;
            case -3:
                drawKeyBackground(R.drawable.selection_btn_back, canvas, key);
                break;
            default:
                break;
        }
    }

    private void drawKeyBackground(int drawableId, Canvas canvas, Key key) {
        Drawable npd = mContext.getResources().getDrawable(drawableId);
        int[] drawableState = key.getCurrentDrawableState();
        if (key.codes[0] != 0) {
            npd.setState(drawableState);
        }
        npd.setBounds(key.x, key.y, key.x + key.width, key.y + key.height);
        npd.draw(canvas);
    }

    //绘制文本draw方法
    private void drawText(Canvas canvas, Key key) {
        Rect bounds = new Rect();
        Paint paint = new Paint();
        paint.setTextAlign(Paint.Align.CENTER);
        if (key.codes[0] == 46) {
            paint.setTextSize(70);
        } else {
            paint.setTextSize(40);
        }
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        if (mKeyStyleType == STYLE_SIMPLE_NUM_KEY) {
            if (key.label != null) {
                paint.getTextBounds(key.label.toString(), 0, key.label.toString().length(), bounds);
                canvas.drawText(key.label.toString(), key.x + (key.width / 2),
                        (key.y + key.height / 2) + bounds.height() / 2, paint);
            } else if (key.codes[0] == -3) {
                key.icon.setBounds(key.x + 9 * key.width / 20, key.y + 3 * key.height / 8, key.x + 11 * key.width / 20,
                        key.y + 5 * key.height / 8);
                key.icon.draw(canvas);
            } else if (key.codes[0] == -5) {
                key.icon.setBounds(key.x + (int) (0.4 * key.width), key.y + (int) (0.328 * key.height), key.x
                        + (int) (0.6 * key.width), key.y + (int) (0.672 * key.height));
                key.icon.draw(canvas);
            }
        }
    }
}